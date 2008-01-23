/*
 * jOrgan - Java Virtual Organ
 * Copyright (C) 2003 Sven Meier
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package jorgan.session;

import jorgan.disposition.Console;
import jorgan.disposition.Organ;
import jorgan.disposition.event.OrganAdapter;
import jorgan.disposition.event.OrganEvent;
import jorgan.disposition.event.OrganListener;
import jorgan.play.OrganPlay;
import jorgan.play.event.PlayListener;
import jorgan.session.event.ElementSelectionListener;
import jorgan.session.event.ProblemListener;
import spin.Spin;

/**
 * A session playing or constructing an organ via a GUI. <br>
 * Note that <em>Spin</em> ensures that listener methods are called on the
 * EDT, although a change in disposition or players might be triggered by a
 * change on a MIDI thread.
 * 
 * TODO remove spin dependencies - non-GUI clients dont' need spin
 */
public class OrganSession {

    private Organ organ;

    private OrganPlay play;

    private ElementSelection selectionModel;
    
    private ElementProblems problems;

    public OrganSession() {
        this(createDefaultOrgan());
    }

    public OrganSession(Organ organ) {
        if (organ == null) {
            throw new IllegalArgumentException("organ must not be null");
        }
        this.organ = organ;

        this.selectionModel = new ElementSelection();
        this.problems = new ElementProblems();

        this.play = new OrganPlay(organ, problems);
        
        this.organ.addOrganListener((OrganListener) Spin
                .over(new OrganAdapter() {
                    @Override
					public void added(OrganEvent event) {
                    	if (event.self()) {
                            selectionModel.setSelectedElement(event.getElement());
                    	}
                    }

                    @Override
					public void removed(OrganEvent event) {
                    	if (event.self()) {
                    		selectionModel.clear(event.getElement());
                    	}
                    }
                }));
    }

    public Organ getOrgan() {
        return organ;
    }

    public OrganPlay getPlay() {
        return play;
    }

    public ElementSelection getSelectionModel() {
        return selectionModel;
    }

    public void addSelectionListener(ElementSelectionListener listener) {
        selectionModel.addSelectionListener(listener);
    }

    public void removeSelectionListener(ElementSelectionListener listener) {
        selectionModel.removeSelectionListener(listener);
    }

    public void addOrganListener(OrganListener listener) {
        organ.addOrganListener((OrganListener) Spin.over(listener));
    }

    public void removeOrganListener(OrganListener listener) {
        organ.removeOrganListener((OrganListener) Spin.over(listener));
    }

    public void addPlayerListener(PlayListener listener) {
        play.addPlayerListener((PlayListener) Spin.over(listener));
    }

    public void removePlayerListener(PlayListener listener) {
        play.removePlayerListener((PlayListener) Spin.over(listener));
    }

    public void addProblemListener(ProblemListener listener) {
    	problems.addProblemListener((ProblemListener) Spin.over(listener));
    }

    public void removeProblemListener(ProblemListener listener) {
    	problems.removeProblemListener((ProblemListener) Spin.over(listener));
    }
    
    public ElementProblems getProblems() {
    	return problems;
    }
    
    private static Organ createDefaultOrgan() {
        Organ organ = new Organ();

        organ.addElement(new Console());

        return organ;
    }
}