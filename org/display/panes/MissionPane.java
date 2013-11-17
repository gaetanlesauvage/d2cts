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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.missions.Load;
import org.missions.Mission;
import org.missions.Workload;
import org.system.Terminal;

public class MissionPane extends JPanel implements ListSelectionListener,
		MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6267113200371375454L;
	private JTable table;
	private ThreadSafeTableModel dm;
	private Map<String, String[]> datas;
	private Map<String, Integer> indexes;
	private String lastSelectedRowMissionID = "";
	private Lock lock;

	public MissionPane() {
		super(new BorderLayout());
		lock = new ReentrantLock();
		MissionColumns[] values = MissionColumns.values();
		dm = new ThreadSafeTableModel(values);
		
		datas = new HashMap<>();
		
		table = new JTable(dm);
		
		indexes = new HashMap<String, Integer>();
		table.setFont(GraphicDisplay.font);

		table.getTableHeader().setFont(GraphicDisplay.fontBold);

		TableRowSorter<ThreadSafeTableModel> sorter = new TableRowSorter<ThreadSafeTableModel>(
				dm);
		MissionCellRenderer renderer = new MissionCellRenderer();
		renderer.setHorizontalAlignment(JLabel.CENTER);
		renderer.setFont(GraphicDisplay.font);
		try {
			table.setDefaultRenderer(Class.forName("java.lang.Object"),
					renderer);
		} catch (ClassNotFoundException ex) {
			System.exit(ReturnCodes.EXIT_ON_UNKNOWN_ERROR.getCode());
		}

		sorter.setSortsOnUpdates(true);
		table.setRowSorter(sorter);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		while(table.getColumnCount() != MissionColumns.values().length){
			System.err.println("MissionPane Waiting ...");
			dm = new ThreadSafeTableModel(MissionColumns.values());
			table.setModel(dm);
			Thread.yield();
		}
		System.err.println("Size ok.");
		for (MissionColumns mc : MissionColumns.values()) {
			table.getColumnModel().getColumn(mc.getIndex()).setMinWidth(mc.getWidth());
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);

		JScrollPane jsp = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp, BorderLayout.CENTER);

		table.setFillsViewportHeight(true);


	}

	public void addMission(Mission m) {
		lock.lock();
		String[] row = { m.getId(), m.getContainerId(),
				m.getPickupTimeWindow().toString(),
				m.getDestination().getSlotId(),
				m.getDeliveryTimeWindow().toString(), Workload.NON_AFFECTED,
				"Waiting"/* , new Time(0)+"" */};
		dm.addRow(row);
		datas.put(m.getId(), row);
		indexes.put(m.getId(), dm.getRowCount() - 1);
		lock.unlock();
	}

	private int getIndex(String mId) {
		int index = -1;
		for (int i = 0; i < table.getRowCount() && index == -1; i++) {
			if (dm.getValueAt(i, MissionColumns.ID.getIndex()).equals(mId)) {
				index = i;
			}
		}
		return index;
	}

	public void setVehicle(final String mId, final String vehicle) {
		lock.lock();
		final int index = getIndex(mId);

		if (index != -1) {

			dm.setValueAt(vehicle, index, MissionColumns.VEHICLE.getIndex());
		}
		lock.unlock();
	}

	public void setMissionState(final String missionId, final String newState) {
		lock.lock();

		final int index = getIndex(missionId);
		if (index != -1) {
			dm.setValueAt(newState, index, MissionColumns.STATUS.getIndex());
		}

		lock.unlock();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getFirstIndex() < table.getRowCount() && e.getFirstIndex() >= 0) {
			int row = table.convertRowIndexToModel(e.getFirstIndex());
			int col = table.getSelectedColumn();

			if (row >= 0
					&& lastSelectedRowMissionID.equals(dm.getValueAt(row,
							MissionColumns.ID.getIndex()))) {
				// Deselect
				Terminal t = Terminal.getInstance();
				t.listener.containerSelected("");
				t.listener.vehicleSelected("");
				t.listener.slotSelected("");
				lastSelectedRowMissionID = "";
				table.clearSelection();
			} else if (row >= 0 && col >= 0) {
				// Select
				String cont = ""
						+ dm.getValueAt(row,
								MissionColumns.CONTAINER.getIndex());
				String vehicle = ""
						+ dm.getValueAt(row, MissionColumns.VEHICLE.getIndex());
				String slot = ""
						+ dm.getValueAt(row, MissionColumns.TARGET.getIndex());
				Terminal t = Terminal.getInstance();
				t.listener.containerSelected(cont);
				if (!vehicle.equals("NA"))
					t.listener.vehicleSelected(vehicle);
				t.listener.slotSelected(slot);
				lastSelectedRowMissionID = dm.getValueAt(row,
						MissionColumns.ID.getIndex())
						+ "";
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int row = table.getSelectedRow();
			valueChanged(new ListSelectionEvent(table, row, row, false));
		}

		else if (e.getButton() == MouseEvent.BUTTON3) {
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");
			t.listener.slotSelected("");
			lastSelectedRowMissionID = "";
			table.clearSelection();
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

	private String getState(Load l) {
		String s = "";
		switch (l.getState()) {
		case STATE_CURRENT:
			s = l.getPhase().getLabel();
			break;
		
		case STATE_TODO:
			s = "Waiting";
			break;
		case STATE_ACHIEVED:
			s = "Achieved";
			break;
		}
		
		return s;
	}

	public void setMission(final Load l) {
		lock.lock();

		final Mission m = l.getMission();
		final int index = getIndex(m.getId());
		if (index != -1) {
			dm.setValueAt(m.getPickupTimeWindow(), index,
					MissionColumns.PICKUP_TW.getIndex());
			dm.setValueAt(m.getDeliveryTimeWindow(), index,
					MissionColumns.DELIVERY_TW.getIndex());
			dm.setValueAt(m.getDestination().getSlotId(), index,
					MissionColumns.TARGET.getIndex());
			dm.setValueAt(getState(l), index, MissionColumns.STATUS.getIndex());
		}

		lock.unlock();
	}
}