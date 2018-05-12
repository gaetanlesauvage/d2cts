package org.util.display;

import javax.swing.table.DefaultTableModel;

import org.display.NewSimulationDialog;

public class NonEditableTableModel extends DefaultTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8523046679888518277L;
	public NonEditableTableModel(Object[] columnNames, int rowCount) {
      super(columnNames,rowCount);
    }
	public boolean isCellEditable(int iRowIndex, int iColumnIndex) { 
		if(iColumnIndex == NewSimulationDialog.SCHEDULING_ALGORITHM_COLUMN_INDEX) return true;
		else return false;
	} 
	
	/*public void setValueAt(Object aValue,int rowIndex,int columnIndex){
		super.setValueAt(aValue, rowIndex, columnIndex);
		super.fireTableCellUpdated(rowIndex, columnIndex);
		System.out.println("Value changed at "+rowIndex+" "+columnIndex+" => "+aValue);
	}*/
}
