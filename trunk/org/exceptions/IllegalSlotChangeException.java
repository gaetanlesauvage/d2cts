package org.exceptions;

public class IllegalSlotChangeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2379056065834740194L;
	public IllegalSlotChangeException (){
		super("can't change this slot by the given one !");
	}
}
