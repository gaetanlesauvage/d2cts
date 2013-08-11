package org.display.panes;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class TableCellWithIcon extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8665724339423550495L;
	private ImageIcon img;
	
	public TableCellWithIcon (String txt, ImageIcon icon){
		super(txt);
		this.img = icon;
	}
	
	public ImageIcon getImg(){
		return img;
	}
	
	public void setImg(ImageIcon img){
		this.img = img;
	}
}
