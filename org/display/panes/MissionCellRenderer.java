package org.display.panes;

import java.awt.Component;
import java.awt.Color;

import javax.swing.JTable;

import javax.swing.table.DefaultTableCellRenderer;

import org.display.GraphicDisplay;


public class MissionCellRenderer extends DefaultTableCellRenderer 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2847261096382699182L;


	static final Color waintingColor = new Color(106,127,128,255);
	static final Color pickupColor = new Color(96,160,164,150);
	static final Color loadingColor = new Color(84,183,189,150);
	static final Color deliveryColor = new Color(84,118,189,150);
	static final Color unloadingColor = new Color(94,115,159,150);
	static final Color achievedColor = new Color(75,86,109,255);

	public MissionCellRenderer(){
		super();
		super.setOpaque(true);
		super.setFont(GraphicDisplay.font);
	}

	public Component getTableCellRendererComponent	(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
	{
		String status = (String)table.getModel().getValueAt(table.convertRowIndexToModel(row), MissionColumns.STATUS.getIndex());
		Color c =waintingColor;
		if(status.contains("Pickup")) c =  pickupColor;
		else if(status.contains("Loading")) c =  loadingColor;
		else if(status.contains("Delivery")) c = deliveryColor;
		else if(status.contains("Unloading")) c = unloadingColor;
		else if(status.contains("Achieved")) c = achievedColor;

		Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		cell.setBackground(c);
		return cell;
	}

}