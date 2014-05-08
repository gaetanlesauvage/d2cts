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
import java.util.Hashtable;

import javax.swing.JLabel;
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
import org.system.Terminal;
import org.system.container_stocking.Container;
import org.system.container_stocking.ContainerAlignment;
import org.system.container_stocking.ContainerLocation;



public class ContainerPane extends JPanel implements ListSelectionListener, MouseListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6267113200371375454L;

	private JTable table;
	private ThreadSafeTableModel dm;
	private Hashtable<String, String[]> datas;
	private Hashtable<String, Integer> indexes;
	private String lastSelectedRowContainerID = "";

	public ContainerPane(){
		super(new BorderLayout());
		boolean ok = false;

		while(!ok){
			dm = new ThreadSafeTableModel(ContainersColumns.values());

			datas = new Hashtable<String, String[]>();
			table = new JTable(dm);
			if(table.getColumnCount() == ContainersColumns.values().length){
				ok = true;
			}
		}
		indexes = new Hashtable<String, Integer>();
		table.setFont(GraphicDisplay.font);
		table.getTableHeader().setFont(GraphicDisplay.fontBold);

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(table.getModel());
		DefaultTableCellRenderer alignCenter = new DefaultTableCellRenderer(); 

		alignCenter.setHorizontalAlignment(JLabel.CENTER); 
		alignCenter.setFont(GraphicDisplay.font);
		try
		{
			table.setDefaultRenderer( Class.forName
					( "java.lang.Object" ), alignCenter );
		}
		catch( ClassNotFoundException ex )
		{
			System.exit( ReturnCodes.EXIT_ON_UNKNOWN_ERROR.getCode() );
		}
		sorter.setSortsOnUpdates(true);
		table.setRowSorter(sorter);

		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		for(ContainersColumns mc : ContainersColumns.values()){
			table.getColumnModel().getColumn(mc.getIndex()).setMinWidth(mc.getWidth());
		}

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(this);

		JScrollPane jsp = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		add(jsp,BorderLayout.CENTER);

		table.setFillsViewportHeight(true);
	}

	public void addContainer(Container c) {
		if(c.getContainerLocation()!= null){
			ContainerLocation cl = c.getContainerLocation();
			String[] row = {c.getId(), c.getTEU()+"", cl.getLaneId(), cl.getSlotId(), ""+cl.getLevel(), ContainerAlignment.getStringValue(cl.getAlign())};
			dm.addRow(row);
			datas.put(c.getId(), row);
			indexes.put(c.getId(), dm.getRowCount()-1);
		}
		else{
			String[] row = {c.getId(), c.getTEU()+"", "NA", "NA", "NA", "NA"};
			dm.addRow(row);
			datas.put(c.getId(), row);
			indexes.put(c.getId(), dm.getRowCount()-1);
		}

	}

	private int getIndex(final String containerID){
		int index = -1;
		for(int i=0; i< table.getRowCount()&&index==-1 ; i++){
			if(dm.getValueAt(i, ContainersColumns.ID.getIndex()).equals(containerID)){
				index = i;
			}
		}
		return index;
	}

	public void setContainerLocation(final String containerID, final ContainerLocation cl){
		final int index = getIndex(containerID);
		if(index != -1) {
			if(cl == null){
				dm.setValueAt("NA", index,ContainersColumns.LOCATION.getIndex());
				dm.setValueAt("NA", index,ContainersColumns.SLOTID.getIndex());
				dm.setValueAt("NA", index,ContainersColumns.LEVEL.getIndex());
				dm.setValueAt("NA", index,ContainersColumns.ALIGN.getIndex());
			}
			else{
				dm.setValueAt(cl.getLaneId(), index,ContainersColumns.LOCATION.getIndex());
				dm.setValueAt(cl.getSlotId(), index,ContainersColumns.SLOTID.getIndex());
				dm.setValueAt(cl.getLevel(), index,ContainersColumns.LEVEL.getIndex());
				dm.setValueAt(ContainerAlignment.getStringValue(cl.getAlign()), index,ContainersColumns.ALIGN.getIndex());
			}
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();

		if(row>=0&&lastSelectedRowContainerID.equals(table.getValueAt(row, ContainersColumns.ID.getIndex()))){
			//Deselect
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");
			t.listener.slotSelected("");
			lastSelectedRowContainerID = "";
			table.clearSelection();
		}
		else if(row>=0 && col>=0){
			//Select
			String cont = ""+table.getValueAt(row, ContainersColumns.ID.getIndex());
			Terminal t = Terminal.getInstance();
			t.listener.vehicleSelected("");
			t.listener.containerSelected(cont);
			t.listener.slotSelected("");
			lastSelectedRowContainerID = table.getValueAt(row, ContainersColumns.ID.getIndex())+"";
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton()==MouseEvent.BUTTON1)
			valueChanged(new ListSelectionEvent(table, table.getSelectedRow(), table.getSelectedRow(),false));
		else if(e.getButton()==MouseEvent.BUTTON3){
			Terminal t = Terminal.getInstance();
			t.listener.containerSelected("");
			t.listener.vehicleSelected("");
			t.listener.slotSelected("");
			lastSelectedRowContainerID = "";
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

	public void removeContainer(String containerID) {
		boolean found = false;
		for(int i=0; i<dm.getRowCount()&&!found ; i++){
			if(dm.getValueAt(i, ContainersColumns.ID.getIndex()).equals(containerID))
			{
				found = true;
				dm.setValueAt("OUT", i, ContainersColumns.LOCATION.getIndex());
				dm.setValueAt("OUT", i, ContainersColumns.SLOTID.getIndex());
				dm.setValueAt("OUT", i, ContainersColumns.LEVEL.getIndex());
				dm.setValueAt("OUT", i, ContainersColumns.ALIGN.getIndex());
			}
		}
	}

	public void select(final String id) {
		final int index = getIndex(id);
		if(index != -1) {
			table.setRowSelectionInterval(index, index);
			table.scrollRectToVisible(table.getCellRect(index, 0, true));
		}
	}
}
