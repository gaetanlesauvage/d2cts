package org.display;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class PopupListCell extends JLabel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8432802375042338630L;
	private String type;
	private ImageIcon img;
	
	public PopupListCell(String txt, String type, ImageIcon img){
		super(txt);
		this.type = type;
		this.img = img;
	}
	public String getType(){
		return type;
	}
	public ImageIcon getImg(){
		return img;
	}
}