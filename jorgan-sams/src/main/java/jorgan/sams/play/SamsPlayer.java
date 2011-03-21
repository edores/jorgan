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
package jorgan.sams.play;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import jorgan.disposition.InterceptMessage;
import jorgan.disposition.Message;
import jorgan.disposition.Input.InputMessage;
import jorgan.disposition.Output.OutputMessage;
import jorgan.midi.MessageUtils;
import jorgan.midi.mpl.Context;
import jorgan.play.ControllerPlayer;
import jorgan.problem.Severity;
import jorgan.sams.disposition.Sams;
import jorgan.sams.disposition.Sams.CancelTabOff;
import jorgan.sams.disposition.Sams.CancelTabOn;
import jorgan.sams.disposition.Sams.TabMessage;
import jorgan.sams.disposition.Sams.TabTurnedOff;
import jorgan.sams.disposition.Sams.TabTurnedOn;
import jorgan.sams.disposition.Sams.TabTurningOff;
import jorgan.sams.disposition.Sams.TabTurningOn;
import jorgan.time.WakeUp;

/**
 * Player for a {@link Sams}.
 */
public class SamsPlayer extends ControllerPlayer<Sams> {

	private static final int TAB_COUNT = 128;

	private PlayerContext interceptContext = new PlayerContext();

	private Tab[] tabs = new Tab[TAB_COUNT];

	public SamsPlayer(Sams sams) {
		super(sams);

		for (int t = 0; t < tabs.length; t++) {
			tabs[t] = new Tab(t);
		}
	}

	@Override
	protected void closeImpl() {
		for (Tab tab : tabs) {
			tab.reset();
		}

		super.closeImpl();
	}

	private Tab getTab(Message message, int index) {
		if (index < 0 || index >= tabs.length) {
			addProblem(Severity.ERROR, message, "tabInvalid", index);
			return null;
		}
		return tabs[index];
	}

	/**
	 * Overriden to intercept.
	 * 
	 * @throws InvalidMidiDataException
	 */
	@Override
	public void send(byte[] datas) throws InvalidMidiDataException {
		for (OutputMessage message : getElement().getMessages(
				OutputMessage.class)) {
			if (message instanceof InterceptMessage) {
				if (interceptContext.process(message, datas, false)) {
					if (message instanceof TabTurningOn) {
						Tab tab = getTab(message, (int) interceptContext
								.get(TabTurningOn.TAB));
						if (tab != null) {
							tab.turnOn();
						}
					} else if (message instanceof TabTurningOff) {
						Tab tab = getTab(message, (int) interceptContext
								.get(TabTurningOff.TAB));
						if (tab != null) {
							tab.turnOff();
						}
					}
				}
			}
		}

		super.send(datas);
	}

	/**
	 * Overriden to intercept.
	 */
	@Override
	protected void receive(MidiMessage midiMessage) {
		super.receive(midiMessage);

		onReceived(MessageUtils.getDatas(midiMessage));
	}

	@Override
	protected void onInput(InputMessage message, Context context) {
		if (message instanceof TabTurnedOn) {
			Tab tab = getTab(message, (int) context.get(TabMessage.TAB));
			if (tab != null) {
				tab.onTurnedOn();
			}
		} else if (message instanceof TabTurnedOff) {
			Tab tab = getTab(message, (int) context.get(TabMessage.TAB));
			if (tab != null) {
				tab.onTurnedOff();
			}
		}
	}

	private void output(Class<? extends OutputMessage> type, int tab) {
		for (OutputMessage message : getElement().getMessages(type)) {
			interceptContext.set(TabMessage.TAB, tab);

			output(message, interceptContext);
		}
	}

	protected void onOutput(byte[] datas, Context context)
			throws InvalidMidiDataException {
		// let super implementation send
		super.send(datas);
	}

	public class Tab {

		private int index;

		private Magnet onMagnet = new Magnet();

		private Magnet offMagnet = new Magnet();

		public Tab(int index) {
			this.index = index;
		}

		public void reset() {
			offMagnet.off();
			onMagnet.off();
		}

		public void turnOn() {
			offMagnet.off();
			onMagnet.on();
		}

		public void turnOff() {
			onMagnet.off();
			offMagnet.on();
		}

		public void onTurnedOn() {
			onMagnet.off();
		}

		public void onTurnedOff() {
			offMagnet.off();
		}

		private class Magnet {
			private boolean on = false;

			public void on() {
				if (!this.on) {
					this.on = true;

					long duration = getElement().getDuration();

					getOrganPlay().alarm(new CancelWakeUp(this), duration);
				}
			}

			public void off() {
				if (this.on) {
					this.on = false;

					if (onMagnet == this) {
						output(CancelTabOn.class, index);
					} else {
						output(CancelTabOff.class, index);
					}
				}
			}
		}

		private class CancelWakeUp implements WakeUp {

			private Magnet magnet;

			public CancelWakeUp(Magnet magnet) {
				this.magnet = magnet;
			}

			@Override
			public boolean replaces(WakeUp wakeUp) {
				if (wakeUp instanceof CancelWakeUp) {
					return ((CancelWakeUp) wakeUp).magnet == this.magnet;
				}
				return false;
			}

			@Override
			public void trigger() {
				magnet.off();
			}
		}
	}
}