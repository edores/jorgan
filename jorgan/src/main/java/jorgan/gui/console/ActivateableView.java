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
package jorgan.gui.console;

import java.awt.*;
import java.awt.event.*;

import jorgan.disposition.*;

/**
 * A view for an activateable.
 */
public class ActivateableView extends View {

  private static final int SIZE  = 13;
  private static final int INSET = 4;
  
  private boolean pressed = false;

  public ActivateableView(Activateable registratable) {
    super(registratable);
  }

  protected Activateable getActivateable() {
    return (Activateable)getElement();
  }
  
  protected Dimension getNonStyleSize() {
       
    Dimension dim = getNameSize();

    dim.width  += SIZE + 3*INSET;
    dim.height += 2*INSET;

    return dim;
  }

  protected void paintNonStyle(Graphics2D g) {
    Dimension size = getNonStyleSize();

    g.setColor(Color.black);
    paintRegistratable(g, INSET, (size.height - SIZE)/2, SIZE, SIZE);

    g.setColor(Color.black);
    g.setFont(getNonStyleFont());
    paintName(g, INSET + SIZE, 0, size.width - (INSET + SIZE), size.height);   
  }

  private void paintRegistratable(Graphics2D g, int x, int y, int width, int height) {  
    Activateable activateable = getActivateable();

    g.drawRect(x, y, width - 1, height - 1);
    if (pressed) {
      g.drawRect(x + 1, y + 1, width - 3, height - 3);
    }

    if (activateable.isActive()) {
      g.drawLine(x + 3, y + 5, x + 3, y + 7);
      g.drawLine(x + 4, y + 6, x + 4, y + 8);
      g.drawLine(x + 5, y + 7, x + 5, y + 9);
      g.drawLine(x + 6, y + 6, x + 6, y + 8);
      g.drawLine(x + 7, y + 5, x + 7, y + 7);
      g.drawLine(x + 8, y + 4, x + 8, y + 6);
      g.drawLine(x + 9, y + 3, x + 9, y + 5);
    }
  }
  
  protected boolean isNonStylePressable(int x, int y, MouseEvent ev) {
    return true; 
  }
  
  public void pressed(int x, int y, MouseEvent ev) {
    pressed = true;

    Activateable activateable = getActivateable();

    if (activateable.isLocking()) {
      activateable.setActive(!activateable.isActive());
    } else {
      activateable.setActive(true);
    }
  } 

  public void released(int x, int y, MouseEvent ev) {
    pressed = false;

    Activateable activateable = getActivateable();
    
    if (activateable.isLocking()) {
      // issue repaint since activateable is actually not changed
      repaint();
    } else {
      activateable.setActive(false);
    }
  }
  
  public void pressed(KeyEvent ev) {

    Activateable activateable = getActivateable();

    Shortcut shortcut = activateable.getShortcut();
    if (shortcut != null && shortcut.match(ev)) {
      activateable.setActive(!activateable.isActive());
    }
  } 

  protected int getStateIndex() {

    Activateable activateable = getActivateable();

    return Math.min(style.getStateCount() - 1, activateable.isActive() ? 1 : 0);
  }
}