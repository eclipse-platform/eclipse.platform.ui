/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.util;


/**
 * Utility interface for displaying an error message.
 * Basic help (no UI) will just output to the System.out, 
 * but when a UI is present, it will display a message
 */
public interface IErrorUtil {
	public void displayError(String msg);
	public void displayError(String msg, Thread uiThread);
}
