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
import java.text.NumberFormat;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.display.GraphicDisplay;
import org.display.MainFrame;
import org.system.Terminal;
import org.util.Location;
import org.vehicles.StraddleCarrier;

public class StraddleCarrierPane extends JPanel implements
		ListSelectionListener, MouseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6267113200371375454L;

	private NumberFormat format = NumberFormat.getInstance();
	private JTable table;
	private ThreadSafeTableModel dm;
	private Hashtable<String, JLabel[]> datas;
	private Hashtable<String, Integer> indexes;
	private String lastSelectedStraddleID = "";

	public StraddleCarrierPane() {
		super(new BorderLayout());
		PaneColumn[] cols = new PaneColumn[StraddleCarrierColumns.values().length
				+ StraddleCarrierColumnsWithIcon.values().length];
		for (StraddleCarrierColumns c : StraddleCarrierColumns.values()) {
			cols[c.getIndex()] = c;
		}
		for (StraddleCarrierColumnsWithIcon c : StraddleCarrierColumnsWithIcon
				.values()) {
			cols[c.getIndex()] = c;
		}

		dm = new ThreadSafeTableModel(cols);
		format.setMaximumFractionDigits(2);

		datas = new Hashtable<String, JLabel[]>();
		table = new JTable(dm);
		indexes = new Hashtable<String, Integer>();
		table.setFont(GraphicDisplay.font);
		table.getTableHeader().setFont(GraphicDisplay.fontBold);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				table.getModel());
		TableCellRenderer alignCenter = new TableCellRendererCentered();
		TableCellRendererWithIcon idRenderer = new TableCellRendererWithIcon();

		table.setDefaultRenderer(StraddleCarrierColumns.class, alignCenter);
		table.setDefaultRenderer(StraddleCarrierColumnsWithIcon.class,
				idRenderer);

		sorter.setSortsOnUpdates(true);
		table.setRowSorter(sorter);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		for (StraddleCarrierColumns mc : StraddleCarrierColumns.values()) {
			table.getColumnModel().getColumn(mc.getIndex())
					.setMinWidth(mc.getWidth());
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);

		JScrollPane jsp = new JScrollPane(table,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp, BorderLayout.CENTER);

		table.setFillsViewportHeight(true);
		// if(terminal == null) terminal = Terminal.getRMIInstance();
	}

	public void addStraddleCarrier(StraddleCarrier sc) {
		String mID = "NA";
		String contID = "NA";

		if (!sc.getHandledContainerId().equals(""))
			contID = sc.getHandledContainerId();
		if (sc.getWorkload() != null
				&& sc.getWorkload().getCurrentLoad() != null)
			mID = sc.getWorkload().getCurrentLoad().getMission().getId();
		TableCellWithIcon jlID = new TableCellWithIcon(sc.getId(), sc.getIcon());
		// jlID.setOpaque(false);
		JLabel jlLoad = new JLabel(contID);
		// jlLoad.setOpaque(false);
		JLabel jlMID = new JLabel(mID);
		// jlMID.setOpaque(false);

		JLabel jlLocation = new JLabel(sc.getLocation().getRoad().getId()
				+ " (" + format.format(sc.getLocation().getPourcent()) + ")");
		// jlLocation.setOpaque(false);
		JLabel jlSpeed = new JLabel("NA");
		// jlSpeed.setOpaque(false);
		JLabel[] row = { jlID, jlLoad, jlMID, jlLocation, jlSpeed };
		dm.addRow(row);
		datas.put(sc.getId(), row);
		indexes.put(sc.getId(), dm.getRowCount() - 1);
	}

	public void setContainer(final String scID, final String contID) {
		final int index = getIndex(scID);
		if (index >= 0) {
			JLabel jl = (JLabel) dm.getValueAt(index,
					StraddleCarrierColumns.LOAD.getIndex());
			jl.setText(contID);
			dm.setValueAt(jl, index, StraddleCarrierColumns.LOAD.getIndex());
		}
	}

	private int getIndex(final String scID) {
		int index = -1;
		for (int i = 0; i < table.getRowCount() && index == -1; i++) {
			JLabel jl = (JLabel) dm.getValueAt(i,
					StraddleCarrierColumnsWithIcon.ID.getIndex());
			if (jl.getText().equals(scID)) {
				index = i;
			}
		}
		return index;
	}

	public void setLocation(final String scID, final Location location) {
		final int index = getIndex(scID);
		if (index >= 0) {
			JLabel jl = (JLabel) dm.getValueAt(index,
					StraddleCarrierColumns.LOCATION.getIndex());
			if (!jl.getText().equals(location.getRoad().getId())) {
				jl.setText(location.getRoad().getId() + " ("
						+ format.format(location.getPourcent()) + ")");
				dm.setValueAt(jl, index,
						StraddleCarrierColumns.LOCATION.getIndex());
				TableCellWithIcon t = (TableCellWithIcon) dm.getValueAt(index,
						StraddleCarrierColumnsWithIcon.ID.getIndex());
				t.setImg(Terminal.getInstance().getStraddleCarrier(scID)
						.getIcon());

			}
		}
	}

	public void setMission(final String scID, final String mID) {
		final int index = getIndex(scID);
		if (index >= 0) {
			JLabel jl = (JLabel) dm.getValueAt(index,
					StraddleCarrierColumns.MISSION.getIndex());
			jl.setText(mID);
			dm.setValueAt(jl, index, StraddleCarrierColumns.MISSION.getIndex());
			TableCellWithIcon t = (TableCellWithIcon) dm.getValueAt(index,
					StraddleCarrierColumnsWithIcon.ID.getIndex());
			t.setImg(Terminal.getInstance().getStraddleCarrier(scID).getIcon());

		}
	}

	public void setSpeed(final String scID, final String speed) {
		final int index = getIndex(scID);
		if (index >= 0) {
			JLabel jl = (JLabel) dm.getValueAt(index,
					StraddleCarrierColumns.SPEED.getIndex());
			jl.setText(speed);
			dm.setValueAt(jl, index, StraddleCarrierColumns.SPEED.getIndex());
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();

		if (row >= 0
				&& lastSelectedStraddleID.equals(table.getValueAt(row,
						StraddleCarrierColumnsWithIcon.ID.getIndex()))) {
			// Deselect
			Terminal.getInstance().listener.containerSelected("");
			Terminal.getInstance().listener.vehicleSelected("");
			Terminal.getInstance().listener.slotSelected("");
			lastSelectedStraddleID = "";
			table.clearSelection();
		} else if (row >= 0 && col >= 0) {
			String cont = ((JLabel) (dm.getValueAt(row,
					StraddleCarrierColumns.LOAD.getIndex()))).getText();
			String vehicle = ((JLabel) (table.getValueAt(row,
					StraddleCarrierColumnsWithIcon.ID.getIndex()))).getText();

			lastSelectedStraddleID = vehicle;
			if (!cont.equals("NA"))
				Terminal.getInstance().listener.containerSelected(cont);
			else
				Terminal.getInstance().listener.containerSelected("");

			Terminal.getInstance().listener.vehicleSelected(vehicle);
			Terminal.getInstance().listener.slotSelected("");
			System.out.println(Terminal.getInstance()
					.getStraddleCarrier(vehicle).getWorkload());

		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1)
			valueChanged(new ListSelectionEvent(table, table.getSelectedRow(),
					table.getSelectedRow(), false));
		else if (e.getButton() == MouseEvent.BUTTON3) {
			Terminal.getInstance().listener.containerSelected("");
			Terminal.getInstance().listener.vehicleSelected("");
			Terminal.getInstance().listener.slotSelected("");
			lastSelectedStraddleID = "";
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

	public void select(final String id) {
		final int index = getIndex(id);
		if (index >= 0) {
			table.setRowSelectionInterval(index, index);
			table.scrollRectToVisible(table.getCellRect(index, 0, true));
		}
	}

}