package org.eclipse.team.internal.ccvs.core.connection;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;

// NIK: this class is nerver used (once in a catch statment)

public class CVSFileException extends CVSException {

	/**
	 * Creates a new <code>CVSFileException</code>.
	 *
	 * @param message a message describing the exception in detail.
	 * @param path    the file's path that has caused the exception.
	 */
	public CVSFileException(String message, IPath path) {
		super(message, path, null);
	}
	/**
	 * Creates a new <code>CVSFileException</code>.
	 *
	 * @param path      the file's path that has caused the exception.
	 * @param throwable the caught exception that has caused the communication
	 *  exception.
	 */
	public CVSFileException(IPath path, Throwable throwable) {
		super(Policy.bind("CVSFileException.io"), path, throwable);//$NON-NLS-1$ 
	}
}
