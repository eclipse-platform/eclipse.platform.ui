package org.eclipse.team.internal.core.target;

public class BaseIdentifierNotInitializedException extends Exception {
	/**
	 * Default constructor for a <code>TeamProviderException</code>.
	 */
	public BaseIdentifierNotInitializedException() {
		super();
	}

	/**
	 * Constructor for a <code>TeamProviderException</code> that takes
	 * a string description of the cause of the exception.
	 * 
 	 * @param message a message describing the cause of the exception.
	 */
	public BaseIdentifierNotInitializedException(String message) {
		super(message);
	}
}

