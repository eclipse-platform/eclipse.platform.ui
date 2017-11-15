/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.progress.internal.legacy;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.ui.progress.IProgressConstants;
import org.eclipse.e4.ui.progress.internal.ProgressMessages;

/**
 * Utility class to create status objects.
 *
 * @private - This class is an internal implementation class and should
 * not be referenced or subclassed outside of the workbench
 */
public class StatusUtil {

    public static Throwable getCause(Throwable exception) {
        // Figure out which exception should actually be logged -- if the given exception is
        // a wrapper, unwrap it
        Throwable cause = null;
        if (exception != null) {
			cause = exception.getCause();

            if (cause == null) {
                cause = exception;
            }
        }

        return cause;
    }

	/**
	 * This method must not be called outside the workbench.
	 *
	 * Utility method for creating status.
	 * @param severity
	 * @param message
	 * @param exception
	 * @return {@link IStatus}
	 */
	public static IStatus newStatus(int severity, String message,
            Throwable exception) {

        String statusMessage = message;
        if (message == null || message.trim().length() == 0) {
            if (exception.getMessage() == null) {
				statusMessage = exception.toString();
			} else {
				statusMessage = exception.getMessage();
			}
        }

        return new Status(severity, IProgressConstants.PLUGIN_ID, severity,
                statusMessage, getCause(exception));
    }

    /**
     * Returns a localized message describing the given exception. If the given exception does not
     * have a localized message, this returns the string "An error occurred".
     *
     * @param exception
     * @return
     */
    public static String getLocalizedMessage(Throwable exception) {
        String message = exception.getLocalizedMessage();

        if (message != null) {
            return message;
        }

        // Workaround for the fact that CoreException does not implement a getLocalizedMessage() method.
        // Remove this branch when and if CoreException implements getLocalizedMessage()
        if (exception instanceof CoreException) {
            CoreException ce = (CoreException)exception;
            return ce.getStatus().getMessage();
        }

        return ProgressMessages.StatusUtil_errorOccurred;
    }



}
