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
package jorgan.gui.customize.consoles;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import jorgan.disposition.Console;
import jorgan.disposition.Continuous;
import jorgan.disposition.Elements;
import jorgan.disposition.Switch;
import jorgan.midi.DevicePool;
import jorgan.midi.Direction;
import jorgan.swing.layout.DefinitionBuilder;
import jorgan.swing.layout.DefinitionBuilder.Column;
import jorgan.swing.table.TableUtils;
import bias.Configuration;

/**
 * A panel for a {@link console}.
 */
public class ConsolePanel extends JPanel {

	private static Configuration config = Configuration.getRoot().get(
			ConsolePanel.class);

	private Console console;

	private JComboBox deviceComboBox;

	private SwitchesModel switchesModel = new SwitchesModel();

	private List<Switch> switches;

	private ContinuousModel continuousModel = new ContinuousModel();

	private List<Continuous> continuous;

	public ConsolePanel(Console console) {
		this.console = console;

		DefinitionBuilder builder = new DefinitionBuilder(this);

		Column column = builder.column();

		column.term(config.get("device").read(new JLabel()));
		deviceComboBox = new JComboBox(DevicePool.instance()
				.getMidiDeviceNames(Direction.IN));
		deviceComboBox.setEditable(false);
		deviceComboBox.setSelectedItem(console.getOutput());
		column.definition(deviceComboBox).fillHorizontal();

		initSwitches(console, column);

		initContinuous(console, column);
	}

	private void initSwitches(Console console, Column column) {
		this.switches = new ArrayList<Switch>(console
				.getReferenced(Switch.class));

		column.group(config.get("switches").read(new JLabel()));

		JScrollPane scrollPane = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setPreferredSize(new Dimension(160, 160));
		column.box(scrollPane);

		JTable table = new JTable();
		config.get("switchesTable").read(switchesModel);
		table.setModel(switchesModel);
		TableUtils.pleasantLookAndFeel(table);
		scrollPane.setViewportView(table);
	}

	private void initContinuous(Console console, Column column) {
		this.continuous = new ArrayList<Continuous>(console
				.getReferenced(Continuous.class));

		column.group(config.get("continuous").read(new JLabel()));

		JScrollPane scrollPane = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		scrollPane.setPreferredSize(new Dimension(160, 160));
		column.box(scrollPane);

		JTable table = new JTable();
		config.get("continuousTable").read(continuousModel);
		table.setModel(continuousModel);
		TableUtils.pleasantLookAndFeel(table);
		scrollPane.setViewportView(table);
	}

	public void apply() {
		console.setOutput((String) deviceComboBox.getSelectedItem());
	}

	public class SwitchesModel extends AbstractTableModel {

		private String[] columnNames = new String[3];

		public void setColumnNames(String[] columnNames) {
			this.columnNames = columnNames;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return switches.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Switch aSwitch = switches.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return Elements.getDisplayName(aSwitch);
			case 1:
				return null;
			case 2:
				return null;
			}

			throw new Error();
		}
	}

	public class ContinuousModel extends AbstractTableModel {

		private String[] columnNames = new String[3];

		public void setColumnNames(String[] columnNames) {
			this.columnNames = columnNames;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return continuous.size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			Continuous aContinuous = continuous.get(rowIndex);

			switch (columnIndex) {
			case 0:
				return Elements.getDisplayName(aContinuous);
			case 1:
				return null;
			case 2:
				return aContinuous.getThreshold();
			}

			throw new Error();
		}
	}
}