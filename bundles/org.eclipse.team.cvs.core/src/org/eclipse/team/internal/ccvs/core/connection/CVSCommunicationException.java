/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.connection;

 
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.Policy;

public class CVSCommunicationException extends CVSException {

	private static final long serialVersionUID = 1L;

    /**
	 * Create a new <code>CVSCommunicationException with the
	 * given status.
	 */
	private CVSCommunicationException(IStatus status) {
		super(status);
	}
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
		this(getStatusFor(e));
	}
	
	public static IStatus getStatusFor(Exception e) {
		if (e instanceof InterruptedIOException) {
			MultiStatus status = new MultiStatus(CVSProviderPlugin.ID, 0, getMessageFor(e), e);
			status.add(new CVSStatus(IStatus.ERROR, Policy.bind("CVSCommunicationException.interruptCause"))); //$NON-NLS-1$
			status.add(new CVSStatus(IStatus.ERROR, Policy.bind("CVSCommunicationException.interruptSolution"))); //$NON-NLS-1$
			status.add(new CVSStatus(IStatus.ERROR, Policy.bind("CVSCommunicationException.alternateInterruptCause"))); //$NON-NLS-1$
			status.add(new CVSStatus(IStatus.ERROR, Policy.bind("CVSCommunicationException.alternateInterruptSolution"))); //$NON-NLS-1$
			return status;
		}
		return new CVSStatus(IStatus.ERROR, getMessageFor(e), e);
	}
	
	public static String getMessageFor(Throwable throwable) {
		String message = Policy.bind(throwable.getClass().getName(), new Object[] {throwable.getMessage()});
		if (message.equals(throwable.getClass().getName()))
			message = Policy.bind("CVSCommunicationException.io", new Object[] {throwable.toString()}); //$NON-NLS-1$ 
		return message;
	}
}
