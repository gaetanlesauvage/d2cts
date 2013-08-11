package org.display.panes;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class TableCellRendererWithIcon extends JLabel implements TableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1846627320980925630L;

	public static int ICON_SIZE = 10;
	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		TableCellWithIcon cell = (TableCellWithIcon)value;
		String s = cell.getText();
		setText(s);
		ImageIcon icon = cell.getImg();
		Image i = icon.getImage().getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_DEFAULT);
		setIcon(new ImageIcon(i));  
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setHorizontalTextPosition(RIGHT);
		if (isSelected) {
			setBackground(table.getSelectionBackground());
			setForeground(table.getSelectionForeground());
		} else {
			setBackground(table.getBackground());
			setForeground(table.getForeground());
		}
		setEnabled(table.isEnabled());
		setFont(table.getFont());
		
		return this;
	}
}
