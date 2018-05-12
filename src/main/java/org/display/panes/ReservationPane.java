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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.conf.parameters.ReturnCodes;
import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.system.Reservation;
import org.time.Time;

public class ReservationPane extends JPanel implements ListSelectionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6267113200371375454L;

	private Hashtable<String, String[]> datas;

	private JTable table;
	private ThreadSafeTableModel dm;
	private ListSelectionListener lsl;

	public ReservationPane() {
		super(new BorderLayout());

		boolean ok = false;

		while(!ok){
			dm = new ThreadSafeTableModel(ReservationColumns.values());

			datas = new Hashtable<String, String[]>();
			table = new JTable(dm);
			table.setFont(GraphicDisplay.font);
			table.getTableHeader().setFont(GraphicDisplay.fontBold);
			table.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					MainFrame.getInstance().setFocusOnJTerminal();
				}
			});
			// indexes = new Hashtable<String, Integer>();
			lsl = this;
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
					table.getModel());
			DefaultTableCellRenderer alignCenter = new DefaultTableCellRenderer();
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

			if(table.getColumnCount() == ReservationColumns.values().length){
				ok = true;
			}
		}

		for (ReservationColumns mc : ReservationColumns.values()) {
			// table.getColumnModel().getColumn(mc.getIndex()).setMaxWidth(mc.getWidth());
			try{
				table.getColumnModel().getColumn(mc.getIndex())
				.setMinWidth(mc.getWidth());
			} catch(ArrayIndexOutOfBoundsException e){
				e.printStackTrace();
				table.getColumnModel().getColumn(mc.getIndex()).setMinWidth(mc.getWidth());
			}
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(lsl);

		JScrollPane jsp = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp, BorderLayout.CENTER);

		table.setFillsViewportHeight(true);
	}

	public void addReservation(final Reservation r) {
		final String[] row = new String[ReservationColumns.values().length];
		row[ReservationColumns.ROAD.getIndex()] = r.getRoadId();
		row[ReservationColumns.VEHICLE.getIndex()] = r.getStraddleCarrierId();
		row[ReservationColumns.TIMEWINDOW.getIndex()] = "" + r.getTimeWindow();
		row[ReservationColumns.PRIORITY.getIndex()] = r.getPriority() + "";
		row[ReservationColumns.DATE.getIndex()] = r.getDate() + "";
		row[ReservationColumns.UNRESERVATIONDATE.getIndex()] = "";
		dm.addRow(row);
		datas.put(r.toString(), row);
	}

	public void removeReservation(final Reservation r,
			final Time unreservationTime) {
		int index = -1;
		for (int i = 0; i < dm.getRowCount() && index == -1; i++) {
			if (dm.getValueAt(i, ReservationColumns.ROAD.getIndex()).equals(
					r.getRoadId())
					&& dm.getValueAt(i, ReservationColumns.VEHICLE.getIndex())
					.equals(r.getStraddleCarrierId())
					&& dm.getValueAt(i,
							ReservationColumns.TIMEWINDOW.getIndex()).equals(
									r.getTimeWindow() + "")
									&& dm.getValueAt(i, ReservationColumns.PRIORITY.getIndex())
									.equals(r.getPriority() + "")
									&& dm.getValueAt(i, ReservationColumns.DATE.getIndex())
									.equals(r.getDate() + "")) {
				index = i;
			}
		}
		if (index >= 0) {
			dm.setValueAt(unreservationTime + "", index,
					ReservationColumns.UNRESERVATIONDATE.getIndex());
		} else
			new Exception("Reservation " + r.getRoadId() + " "
					+ r.getTimeWindow() + " not found !!!").printStackTrace();
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
	}
}
