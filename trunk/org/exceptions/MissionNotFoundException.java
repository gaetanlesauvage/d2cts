package org.exceptions;

public class MissionNotFoundException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1858123391498197250L;

	public MissionNotFoundException(String mID){
		super("Mission "+mID+" not found !");
	}
}
