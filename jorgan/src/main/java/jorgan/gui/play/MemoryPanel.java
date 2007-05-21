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
package jorgan.gui.play;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import jorgan.disposition.Memory;
import jorgan.disposition.event.OrganEvent;
import jorgan.disposition.event.OrganListener;
import jorgan.gui.OrganSession;
import jorgan.swing.BaseAction;
import jorgan.swing.table.StringCellEditor;
import jorgan.swing.table.TableUtils;
import swingx.docking.DockedPanel;
import bias.Configuration;
import bias.swing.MessageBox;
import bias.util.MessageBuilder;

/**
 * Panel for editing of a {@link jorgan.disposition.Memory}.
 */
public class MemoryPanel extends DockedPanel {

	private static Configuration config = Configuration.getRoot().get(
			MemoryPanel.class);

	private JTable table = new JTable();

	private MemoryModel model = new MemoryModel();

	private OrganSession session;

	private Memory memory;

	/**
	 * Constructor.
	 */
	public MemoryPanel() {

		table.setModel(model);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getColumnModel().getColumn(1).setCellEditor(
				new StringCellEditor());
		TableUtils.hideHeader(table);
		TableUtils.fixColumnWidth(table, 0, "888");
		TableUtils.pleasantLookAndFeel(table);
		setScrollableBody(table, true, false);

		addTool(new PreviousAction());
		addTool(new NextAction());
		addTool(new SwapAction());
		addTool(new ClearAction());

		setMemory(null);
	}

	/**
	 * Set the organ to control memory of.
	 * 
	 * @param session
	 *            session
	 */
	public void setOrgan(OrganSession session) {
		if (this.session != null) {
			this.session.removeOrganListener(model);

			setMemory(null);
		}

		this.session = session;

		if (this.session != null) {
			this.session.addOrganListener(model);

			findMemory();
		}
	}

	private void findMemory() {
		List memories = this.session.getOrgan().getElements(Memory.class);
		if (memories.isEmpty()) {
			setMemory(null);
		} else {
			setMemory((Memory) memories.get(0));
		}
	}

	private void setMemory(Memory memory) {
		this.memory = memory;

		model.fireTableDataChanged();

		table.setVisible(memory != null);

		if (memory == null) {
			setMessage(config.get("noMemory").read(new MessageBuilder())
					.build());
		} else {
			setMessage(null);

			updateSelection();
		}
	}

	private void updateSelection() {
		// remove listener to avoid infinite loop
		table.getSelectionModel().removeListSelectionListener(model);

		int index = memory.getValue();
		if (index != table.getSelectedRow()) {
			if (table.getCellEditor() != null) {
				table.getCellEditor().cancelCellEditing();
				table.setCellEditor(null);
			}
			table.getSelectionModel().setSelectionInterval(index, index);
			table.scrollRectToVisible(table.getCellRect(index, 0, false));
		}
		table.setColumnSelectionInterval(0, 0);

		// re-add listener
		table.getSelectionModel().addListSelectionListener(model);
	}

	private class MemoryModel extends AbstractTableModel implements
			OrganListener, ListSelectionListener {

		public int getColumnCount() {
			return 2;
		}

		public int getRowCount() {
			if (memory == null) {
				return 0;
			} else {
				return 128;
			}
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return "" + (rowIndex + 1);
			}
			return memory.getTitle(rowIndex);
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return (columnIndex == 1);
		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			memory.setTitle(rowIndex, (String) aValue);
		}

		public void elementAdded(OrganEvent event) {
			if (event.getElement() instanceof Memory) {
				setMemory((Memory) event.getElement());
			}
		}

		public void elementChanged(OrganEvent event) {
			if (event.getElement() == memory) {
				setMemory(memory);
			}
		}

		public void elementRemoved(OrganEvent event) {
			if (event.getElement() instanceof Memory) {
				findMemory();
			}
		}

		public void referenceAdded(OrganEvent event) {
		}

		public void referenceChanged(OrganEvent event) {
		}

		public void referenceRemoved(OrganEvent event) {
		}

		public void valueChanged(ListSelectionEvent e) {
			if (table.getSelectedRowCount() == 1) {
				int row = table.getSelectedRow();
				if (row != -1 && row != memory.getValue()) {
					memory.setValue(row);
				}
			}
		}
	}

	private class NextAction extends BaseAction implements
			ListSelectionListener {
		private NextAction() {
			config.get("next").read(this);

			setEnabled(false);

			table.getSelectionModel().addListSelectionListener(this);
		}

		public void actionPerformed(ActionEvent ev) {
			memory.increment(+1);
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(table.getSelectedRowCount() == 1);
		}
	}

	private class PreviousAction extends BaseAction implements
			ListSelectionListener {
		private PreviousAction() {
			config.get("previous").read(this);

			setEnabled(false);

			table.getSelectionModel().addListSelectionListener(this);
		}

		public void actionPerformed(ActionEvent ev) {
			memory.increment(-1);
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(table.getSelectedRowCount() == 1);
		}
	}

	private class SwapAction extends BaseAction implements
			ListSelectionListener {
		private SwapAction() {
			config.get("swap").read(this);

			setEnabled(false);

			table.getSelectionModel().addListSelectionListener(this);
		}

		public void actionPerformed(ActionEvent ev) {
			int[] rows = table.getSelectedRows();
			memory.swap(rows[0], rows[1]);
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(table.getSelectedRowCount() == 2);
		}
	}

	private class ClearAction extends BaseAction implements
			ListSelectionListener {
		private ClearAction() {
			config.get("clear").read(this);

			setEnabled(false);

			table.getSelectionModel().addListSelectionListener(this);
		}

		public void actionPerformed(ActionEvent ev) {
			if (confirm()) {
				int[] rows = table.getSelectedRows();
				for (int r = 0; r < rows.length; r++) {
					memory.clear(rows[r]);
				}
			}
		}

		private boolean confirm() {

			MessageBox box = config.get("clearConfirm").read(
					new MessageBox(MessageBox.OPTIONS_YES_NO));
			return box.show(MemoryPanel.this) == MessageBox.OPTION_YES;
		}

		public void valueChanged(ListSelectionEvent e) {
			setEnabled(table.getSelectedRowCount() > 0);
		}
	}
}