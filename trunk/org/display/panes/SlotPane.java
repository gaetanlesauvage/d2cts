/*
 * This file is part of D²CTS : Dynamic and Distributed Container Terminal Simulator.
 *
 * Copyright (C) 2009-2012  Gaëtan Lesauvage
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.display.panes;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.system.Terminal;
import org.system.container_stocking.Level;
import org.system.container_stocking.Slot;

public class SlotPane extends JPanel implements ListSelectionListener,
		MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6267113200371375454L;

	private Hashtable<String, Object[]> datas;
	public static final int TABLEWIDTH = 596;

	private JTable table;
	private ThreadSafeTableModel dm;

	private JLabel jlPave, jlLane, jlSlotId, jlTeu;
	private JLabel[] levels;
	private String lastSelectedSlotID = "";

	public SlotPane() {
		super(new BorderLayout(50, 0));
		dm = new ThreadSafeTableModel(SlotColumns.values());

		datas = new Hashtable<String, Object[]>();
		table = new JTable(dm);
		table.setFont(GraphicDisplay.font);
		table.getTableHeader().setFont(GraphicDisplay.fontBold);
		final MouseListener ml = this;

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dm);
		TableCellRendererCentered alignCenter = new TableCellRendererCentered();

		alignCenter.setFont(GraphicDisplay.font);
		try {
			table.setDefaultRenderer(Class.forName("java.lang.Object"),
					alignCenter);
		} catch (ClassNotFoundException ex) {
			System.exit(ReturnCodes.EXIT_ON_UNKNOWN_ERROR.getCode());
		}

		sorter.setSortsOnUpdates(true);
		table.setRowSorter(sorter);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		for (SlotColumns sc : SlotColumns.values()) {
			// table.getColumnModel().getColumn(sc.getIndex()).setMaxWidth(sc.getWidth());
			table.getColumnModel().getColumn(sc.getIndex())
					.setMinWidth(sc.getWidth());
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(ml);

		// table.setMinimumSize(new Dimension(TABLEWIDTH,
		// GraphicDisplay.HEIGHT));
		// table.setMaximumSize(new Dimension(TABLEWIDTH,
		// GraphicDisplay.HEIGHT));

		JScrollPane jsp = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp, BorderLayout.WEST);

		// add(table,BorderLayout.WEST);
		table.setFillsViewportHeight(true);

		JPanel pInfo = new JPanel(new GridLayout(SlotColumns.values().length
				+ Slot.SLOT_MAX_LEVEL, 2));
		JLabel jlPaveLavel = new JLabel("Pave: ");
		jlPaveLavel.setFont(GraphicDisplay.fontBold);
		pInfo.add(jlPaveLavel);
		jlPave = new JLabel("");
		jlPave.setFont(GraphicDisplay.font);
		pInfo.add(jlPave);

		JLabel jlLaneLabel = new JLabel("Lane: ");
		jlLaneLabel.setFont(GraphicDisplay.fontBold);
		pInfo.add(jlLaneLabel);
		jlLane = new JLabel("");
		jlLane.setFont(GraphicDisplay.font);
		pInfo.add(jlLane);

		JLabel jlSlotLabel = new JLabel("Slot: ");
		jlSlotLabel.setFont(GraphicDisplay.fontBold);
		pInfo.add(jlSlotLabel);
		jlSlotId = new JLabel("");
		jlSlotId.setFont(GraphicDisplay.font);
		pInfo.add(jlSlotId);

		JLabel jlTEULabel = new JLabel("TEU: ");
		jlTEULabel.setFont(GraphicDisplay.fontBold);
		pInfo.add(jlTEULabel);

		jlTeu = new JLabel("");
		jlTeu.setFont(GraphicDisplay.font);
		pInfo.add(jlTeu);

		levels = new JLabel[Slot.SLOT_MAX_LEVEL];
		for (int i = 0; i < Slot.SLOT_MAX_LEVEL; i++) {
			JLabel jlLevelLabel = new JLabel("Level " + i + " : ");
			jlLevelLabel.setFont(GraphicDisplay.fontBold);
			pInfo.add(jlLevelLabel);
			levels[i] = new JLabel("");
			levels[i].setFont(GraphicDisplay.font);
			pInfo.add(levels[i]);

		}
		// pInfo.setMinimumSize(new Dimension(GraphicDisplay.WIDTH-TABLEWIDTH,
		// GraphicDisplay.HEIGHT));
		// pInfo.setMaximumSize(new Dimension(GraphicDisplay.WIDTH-TABLEWIDTH,
		// GraphicDisplay.HEIGHT));
		add(pInfo, BorderLayout.CENTER);
	}

	public void showInformation() {
		int row = table.getSelectedRow();

		if (row >= 0) {
			String slotId = ""
					+ table.getValueAt(row, SlotColumns.SLOTID.getIndex());
			Object[] info = datas.get(slotId);
			jlPave.setText(info[SlotColumns.PAVE.getIndex()] + "");
			jlLane.setText(info[SlotColumns.LANE.getIndex()] + "");
			jlSlotId.setText(info[SlotColumns.SLOTID.getIndex()] + "");
			jlTeu.setText(info[SlotColumns.TEU.getIndex()] + "");
			String levelsData = info[SlotColumns.CONTENT.getIndex()] + "";
			StringTokenizer st = new StringTokenizer(levelsData, "\n");
			int i = 0;
			while (st.hasMoreTokens()) {
				String s = st.nextToken();
				levels[i].setText(s);
				i++;
			}
			while (i < levels.length) {
				levels[i].setText("empty");
				i++;
			}
		} else {
			jlPave.setText("");
			jlLane.setText("");
			jlSlotId.setText("");
			jlTeu.setText("");
			int i = 0;
			while (i < levels.length) {
				levels[i].setText("empty");
				i++;
			}
		}
	}

	public void addSlot(final Slot s) {
		Object[] row = new String[SlotColumns.values().length];

		row[SlotColumns.PAVE.getIndex()] = s.getPaveId();
		row[SlotColumns.LANE.getIndex()] = s.getLocation().getRoad().getId();
		row[SlotColumns.SLOTID.getIndex()] = s.getId();
		row[SlotColumns.TEU.getIndex()] = s.getTEU() + "";
		List<Level> levels = s.getLevels();
		String content = "";
		for (int i = 0; i < levels.size(); i++) {
			content += levels.get(i).getStringContent() + "\n";
		}
		row[SlotColumns.CONTENT.getIndex()] = content;

		datas.put(s.getId(), row);
		dm.addRow(row);
	}

	public void contentChanged(final Slot s) {
		Object[] row = datas.get(s.getId());

		if (row != null) {
			row[SlotColumns.PAVE.getIndex()] = s.getPaveId();
			row[SlotColumns.LANE.getIndex()] = s.getLocation().getRoad()
					.getId();
			row[SlotColumns.SLOTID.getIndex()] = s.getId();
			row[SlotColumns.TEU.getIndex()] = s.getTEU() + "";
			List<Level> levels = s.getLevels();
			String content = "";
			for (int i = 0; i < levels.size(); i++) {
				content += levels.get(i).getStringContent() + "\n";
			}
			row[SlotColumns.CONTENT.getIndex()] = content;

			datas.put(s.getId(), row);
			boolean found = false;
			for (int i = 0; i < dm.getRowCount() && !found; i++) {

				if (dm.getValueAt(i, SlotColumns.SLOTID.getIndex()).equals(
						s.getId())) {
					found = true;
					final int ligne = i;
					for (int j = 0; j < dm.getColumnCount(); j++) {
						dm.setValueAt(row[j], ligne, j);
					}
				}
			}
			if (!found) {
				dm.addRow(row);
			}
		} else
			addSlot(s);
		showInformation();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();

		if (row >= 0
				&& lastSelectedSlotID.equals(table.getValueAt(row,
						SlotColumns.SLOTID.getIndex()))) {
			// Deselect
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");
			t.listener.slotSelected("");
			lastSelectedSlotID = "";
			table.clearSelection();
		} else if (row >= 0 && col >= 0) {
			// Select
			String slot = ""
					+ table.getValueAt(row, SlotColumns.SLOTID.getIndex());
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");

			t.listener.slotSelected(slot);
			lastSelectedSlotID = table.getValueAt(row,
					SlotColumns.SLOTID.getIndex())
					+ "";
		}
		showInformation();
	}

	class LevelStringDatas {
		String[] datas;

		public LevelStringDatas(int size) {
			if (size == 0) {
				datas = new String[1];
				datas[0] = "empty";
			} else
				datas = new String[size];

		}

		public String get(int index) {
			return datas[index];
		}

		public int size() {
			return datas.length;
		}

		public void set(int index, String value) {
			datas[index] = value;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			valueChanged(new ListSelectionEvent(table, table.getSelectedRow(),
					table.getSelectedRow(), false));
		else if (e.getButton() == MouseEvent.BUTTON3) {
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");
			t.listener.slotSelected("");
			lastSelectedSlotID = "";
			table.clearSelection();
			showInformation();
		}
		MainFrame.getInstance().setFocusOnJTerminal();
	}

	@Override
	public void mousePressed(MouseEvent e) {

	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	public void select(final String id) {
		int index = -1;
		for (int i = 0; i < table.getRowCount() && index == -1; i++) {
			if (table.getValueAt(i, SlotColumns.SLOTID.getIndex()).equals(id)) {
				index = i;
			}
		}
		final int findex = index;
		if (findex != -1) {
			table.setRowSelectionInterval(findex, findex);
			table.scrollRectToVisible(table.getCellRect(findex, 0, true));
			valueChanged(new ListSelectionEvent(table, table.getSelectedRow(),
					table.getSelectedRow(), false));
		}
	}
}
