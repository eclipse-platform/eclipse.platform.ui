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
package org.eclipse.jface.util;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;

/**
 * Implements a default implementation of ISafeRunnable.
 * The default implementation of <code>handleException</code>
 * opens a message dialog.
 * <p><b>Note:<b> This class can open an error dialog and should not 
 *  be used outside of the UI Thread.</p>
 */
public abstract class SafeRunnable implements ISafeRunnable {
	private String message;
	private static boolean ignoreErrors = false;

/**
 * Creates a new instance of SafeRunnable with a default error message.
 */
public SafeRunnable() {
    // do nothing
}

/**
 * Creates a new instance of SafeRunnable with the given error message.
 * 
 * @param message the error message to use
 */
public SafeRunnable(String message) {
	this.message = message;
}

/* (non-Javadoc)
 * Method declared on ISafeRunnable.
 */
public void handleException(Throwable e) {
	// Workaround to avoid interactive error dialogs during automated testing
	if (!ignoreErrors) {
		if(message == null)
			message = JFaceResources.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$
		MessageDialog.openError(null, JFaceResources.getString("Error"), message); //$NON-NLS-1$
	}
}

/**
 * Flag to avoid interactive error dialogs during automated testing.
 * @deprecated use getIgnoreErrors()
 */
public static boolean getIgnoreErrors(boolean flag) {
	return ignoreErrors;
}

/**
 * Flag to avoid interactive error dialogs during automated testing.
 * 
 * @since 3.0
 */
public static boolean getIgnoreErrors() {
	return ignoreErrors;
}

/**
 * Flag to avoid interactive error dialogs during automated testing.
 */
public static void setIgnoreErrors(boolean flag) {
	ignoreErrors = flag;
}
}
