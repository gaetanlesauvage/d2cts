package org.exceptions;

public class EmptyResourcesException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3602471040960476684L;

	public EmptyResourcesException (){
		super("there is no resource in the mission scheduler at this time !");
	}
}
