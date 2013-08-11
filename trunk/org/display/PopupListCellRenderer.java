package org.display;

import java.awt.Component;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class PopupListCellRenderer extends JLabel implements
		ListCellRenderer<Object> {
	public static final String POPUP_SC_ICON_URL = "/etc/images/sc_icon.png";
	public static final String POPUP_CONTAINER_ICON_URL = "/etc/images/container.png";
	public static final String POPUP_SLOT_ICON_URL = "/etc/images/slotIcon.png";

	/**
	 * 
	 */
	private static final long serialVersionUID = 1846627320980925630L;
	private final static ImageIcon scIcon = new ImageIcon(
			PopupListCellRenderer.class.getResource(POPUP_SC_ICON_URL));
	private final static ImageIcon containerIcon = new ImageIcon(
			PopupListCellRenderer.class.getResource(POPUP_CONTAINER_ICON_URL));
	private final static ImageIcon slotIcon = new ImageIcon(
			PopupListCellRenderer.class.getResource(POPUP_SLOT_ICON_URL));

	private final static int SIZE = 15;

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(JList<?> list, // the list
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // does the cell have focus
	{
		PopupListCell cell = (PopupListCell) value;

		String s = cell.getText();
		setText(s);
		String type = cell.getType();
		ImageIcon img = null;
		if (cell.getImg() != null)
			img = cell.getImg();
		else {
			if (type.equals("straddleCarrier"))
				img = scIcon;
			else if (type.equals("container"))
				img = containerIcon;
			else if (type.equals("slot"))
				img = slotIcon;
		}
		setIcon(new ImageIcon(img.getImage().getScaledInstance(SIZE, SIZE,
				Image.SCALE_DEFAULT)));

		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		setEnabled(list.isEnabled());
		setFont(GraphicDisplay.font);
		setOpaque(true);

		setFont(cell.getFont());
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(cell.getBackground());
		}
		return this;
	}
}
