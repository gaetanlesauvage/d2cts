package org.vehicles;

import java.awt.Color;

public enum StraddleCarrierColor {
	red(new Color(141,0,0)),
	blue(new Color(0,28,118)),
	green(new Color(10,59,0)),
	yellow(new Color(190,188,0)),
	sienna(new Color(148,72,34)),
	orange(new Color(255,96,0)),
	magenta(new Color(165,0,167)),
	olive(new Color(123,122,1)),
	DarkOrchid(new Color(153,50,204)),
	LightSeaGreen(new Color(32,178,170));
	
	private Color c;
	
	private StraddleCarrierColor(Color c){
		this.c = c;
	}
	
	public Color getColor(){
		return c;
	}
	
	public String getName(Color c){
		for(StraddleCarrierColor sc : StraddleCarrierColor.values()){
			if(sc.getColor().getRGB() == c.getRGB()) return sc.name();
		}
		return "unknown color";
	}
	
	public static Color getColor(String name){
		for(StraddleCarrierColor sc : StraddleCarrierColor.values()){
			if(sc.name().equals(name)) return sc.getColor();
		}
		return null;
	}
}
