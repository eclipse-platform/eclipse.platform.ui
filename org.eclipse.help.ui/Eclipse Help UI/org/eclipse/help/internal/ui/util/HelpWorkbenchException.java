package org.eclipse.help.internal.ui.util;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

// possibly use platform CoreException later. For now, this is
// handled by the Logger class in base.

public class HelpWorkbenchException extends Exception {

	public HelpWorkbenchException() {
		super();
	}
	public HelpWorkbenchException(String message) {
		super(message);
	}
}
