package org.eclipse.debug.internal.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.widgets.Shell;

/**
 * This class serves as a location for utility methods for the debug UI.
 */
public class DebugUIUtils {

	private static ResourceBundle fgResourceBundle;

	/**
	 * Utility method with conventions
	 */
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message= null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	 * Utility method
	 */
	public static String getResourceString(String key) {
		if (fgResourceBundle == null) {
			fgResourceBundle= getResourceBundle();
		}
		if (fgResourceBundle != null) {
			return fgResourceBundle.getString(key);
		} else {
			return "!" + key + "!";
		}
	}

	/**
	 * Returns the resource bundle used by all parts of the debug ui package.
	 */
	public static ResourceBundle getResourceBundle() {
		try {
			return ResourceBundle.getBundle("org.eclipse.debug.internal.ui.DebugUIResources");
		} catch (MissingResourceException e) {
			MessageDialog.openError(DebugUIPlugin.getActiveWorkbenchWindow().getShell(), "Error", e.toString());
		}
		return null;
	}

	/**
	 * Convenience method to log internal UI errors
	 */
	public static void logError(Exception e) {
		if (DebugUIPlugin.getDefault().isDebugging()) {
			// this message is intentionally not internationalized, as an exception may
			// be due to the resource bundle itself
			System.out.println("Internal error logged from UI: ");
			e.printStackTrace();
			System.out.println();
		}
	}
	
	/**
	 * Defined here as an API-legal way to get the console document for the 
	 * current process.
	 */
	public static IDocument getCurrentConsoleDocument() {
		DebugUIPlugin plugin = DebugUIPlugin.getDefault();
		IProcess currentProcess = plugin.getCurrentProcess();
		IDocument document = plugin.getConsoleDocument(currentProcess, true);
		return document;
	}
}

