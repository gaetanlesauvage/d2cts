package org.exceptions;

public class DatabaseNotConfiguredException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1144317193085601648L;
	public static final int EXIT_CODE = 50;
	
	public DatabaseNotConfiguredException () {
		super("Database not configured !");
	}
	
}
