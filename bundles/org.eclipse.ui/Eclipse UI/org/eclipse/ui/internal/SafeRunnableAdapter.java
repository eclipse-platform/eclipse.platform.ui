package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Implements a adapter for ISafeRunnable.
 * The default implementation of <code>handleException(Exception)</code>
 * will open a message dilog.
 */
public abstract class SafeRunnableAdapter implements ISafeRunnable {
	String message;
	static boolean ignoreErrors = false;
/**
 * Creates a new instance of SafeRunnableAdapter.
 */
public SafeRunnableAdapter() {}

/**
 * Creates a new instance of SafeRunnableAdapter.
 */
public SafeRunnableAdapter(String message) {
	this.message = message;
}
/* (non-Javadoc)
 * Method declared on ISafeRunnable.
 */
public void handleException(Throwable e) {
	// Workaround to avoid interactive error dialogs during automated testing
	if (!ignoreErrors) {
		e.printStackTrace();
		if(message == null)
			message = WorkbenchMessages.getString("SafeRunnable.errorMessage"); //$NON-NLS-1$
		MessageDialog.openError(null, WorkbenchMessages.getString("Error"), message); //$NON-NLS-1$
	}
}

/**
 * Workaround to avoid interactive error dialogs during automated testing.
 */
public static boolean getIgnoreErrors(boolean flag) {
	return ignoreErrors;
}

/**
 * Workaround to avoid interactive error dialogs during automated testing.
 */
public static void setIgnoreErrors(boolean flag) {
	ignoreErrors = flag;
}
}
