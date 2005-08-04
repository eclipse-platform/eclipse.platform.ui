/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.connection;

 
import java.io.InterruptedIOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ccvs.core.*;

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
			status.add(new CVSStatus(IStatus.ERROR, CVSMessages.CVSCommunicationException_interruptCause)); 
			status.add(new CVSStatus(IStatus.ERROR, CVSMessages.CVSCommunicationException_interruptSolution)); 
			status.add(new CVSStatus(IStatus.ERROR, CVSMessages.CVSCommunicationException_alternateInterruptCause)); 
			status.add(new CVSStatus(IStatus.ERROR, CVSMessages.CVSCommunicationException_alternateInterruptSolution)); 
			return status;
		}
		return new CVSStatus(IStatus.ERROR, getMessageFor(e), e);
	}
	
	public static String getMessageFor(Throwable throwable) {
        String message = Policy.getMessage(getMessageKey(throwable));
        if (message == null) {
            message = NLS.bind(CVSMessages.CVSCommunicationException_io, (new Object[] {throwable.toString()}));
        } else {
            message = NLS.bind(message, (new Object[] {throwable.getMessage()}));
        }
		return message;
	}
    
    private static String getMessageKey(Throwable t) {
        String name = t.getClass().getName();
        name = name.replace('.', '_');
        return name;
    }
}
