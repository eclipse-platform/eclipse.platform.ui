package org.eclipse.debug.internal.ui.views.console;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleListener;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.ListenerList;

/**
 * Tracks text appended to the console and notifies listeners in terms of whole
 * lines.
 */
public class ConsoleDocumentLineTracker {
	
	/**
	 * Number of lines processed in the console
	 */
	private int fLinesProcessed = 0;
	
	/**
	 * Console listeners
	 */
	private ListenerList fListeners = new ListenerList(2);

	/**
	 * Constructor for ConsoleDocumentLineTracker.
	 */
	public ConsoleDocumentLineTracker() {
		super();
	}

	/**
	 * Notification the console has changed based on the given event
	 */
	public void consoleChanged(IConsole console, DocumentEvent event) {
		IDocument document = event.getDocument();
		int lines = document.getNumberOfLines();
		for (int line = fLinesProcessed; line <= lines; line++) {
			String delimiter;
			try {
				delimiter = document.getLineDelimiter(line);
			} catch (BadLocationException e) {
				DebugUIPlugin.log(e);
				return;
			}
			if (delimiter != null) {
				fLinesProcessed++;
				IRegion lineRegion = null;
				try {
					lineRegion = document.getLineInformation(line);
				} catch (BadLocationException e) {
					DebugUIPlugin.log(e);
					return;
				}
				Object[] listeners = fListeners.getListeners();
				for (int i = 0; i < listeners.length; i++) {
					IConsoleListener listener = (IConsoleListener)listeners[i];
					listener.lineAppended(console, lineRegion);
				}
			}
		}
		// TODO: what about the last line?
	}
	
	/**
	 * Adds the given listener to the list of listeners notified when a line of
	 * text is appended to the console.
	 * 
	 * @param listener
	 */
	public void addConsoleListener(IConsoleListener listener) {
		fListeners.add(listener);
	}

	/**
	 * Removes the given listener from the list of listeners notified when a
	 * line of text is appended to the console.
	 *
	 * @param listener
	 */
	public void removeConsoleListener(IConsoleListener listener) {
		fListeners.remove(listener);
	}
}
