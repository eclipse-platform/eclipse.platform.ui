package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

public class CVSCommunicationException extends CVSException {

	/**
	 * Create a new <code>CVSCommunicationException with the
	 * given message.
	 */
	public CVSCommunicationException(String message) {
		super(message);
	}
	/**
	 * Create a new <code>CVSCommunicationException.
	 *
	 * @param message a message describing the exception in detail.
	 * @param the caught exception that has caused the communication
	 *  exception.
	 */
	public CVSCommunicationException(String message, Exception e) {
		super(message, e);
	}
	/**
	 * Create a new <code>CVSCommunicationException.
	 *
	 * @param the caught exception that has caused the communication
	 *  exception.
	 */
	public CVSCommunicationException(Exception e) {
		this(getMessageFor(e), e);
	}
	
	public static String getMessageFor(Throwable throwable) {
		String message = Policy.bind(throwable.getClass().getName(), new Object[] {throwable.getMessage()});
		if (message.equals(throwable.getClass().getName()))
			message = Policy.bind("CVSCommunicationException.io", new Object[] {throwable.toString()}); //$NON-NLS-1$ 
		return message;
	}
}
