package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
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
	if(message == null)
		message = "An error has occurred";
	if (e != null && e.getMessage() != null)
		message += ": " + e.getMessage();
	if (!message.endsWith("."))
		message += ".";
	message += "  See error log for more details.";
	MessageDialog.openError(null, "Error", message);
}
}
