package org.display.panes;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellRenderer;

public class TableCellRendererCentered extends JLabel implements TableCellRenderer {
	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.
	/**
	 * 
	 */
	private static final long serialVersionUID = -2819706996316462738L;

	public Component getTableCellRendererComponent(JTable table,Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		String s = null;
		
		
		if(value instanceof JLabel) {
			JLabel cell = (JLabel)value;
			s = cell.getText();
		}
		else if(value instanceof JTextField){
			JTextField cell = (JTextField)value;
			s = cell.getText();
		}
		else if(value != null) 
			s = value.toString();
		else 
			System.err.println("here");

		if(s != null){
		setText(s);
		setOpaque(true);
		if (isSelected||hasFocus) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		setHorizontalAlignment(CENTER);
		setEnabled(table.isEnabled());
		setFont(table.getFont());
		}
		return this;
	}
}

