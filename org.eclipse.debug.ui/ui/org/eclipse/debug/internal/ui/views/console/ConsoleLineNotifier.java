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
package org.eclipse.debug.internal.ui.views.console;


import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.util.ListenerList;

/**
 * Tracks text appended to the console and notifies listeners in terms of whole
 * lines.
 */
public class ConsoleLineNotifier implements IDebugEventSetListener {
	
	/**
	 * Number of lines processed in the console
	 */
	private int fLinesProcessed = 0;
	
	/**
	 * Console listeners
	 */
	private ListenerList fListeners = new ListenerList(2);

	/**
	 * The console this notifier is tracking 
	 */
	private IConsole fConsole = null;
	
	/**
	 * Connects this notifier to the given console.
	 *  
	 * @param console
	 */
	public void connect(IConsole console) {
		fConsole = console;
		DebugPlugin.getDefault().addDebugEventListener(this);
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
			listener.init(console);
		}
	}
	
	/**
	 * Disposes this notifier 
	 */
	public void disconnect() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
			listener.dispose();
		}
		fListeners = null;
		fConsole = null;
	}
	
	/**
	 * Notification the console has changed based on the given event
	 */
	public void consoleChanged(DocumentEvent event) {
		processNewLines();
	}
	
	/**
	 * Notifies listeners of any new lines appended to the console.
	 */
	protected synchronized void processNewLines() {
		IDocument document = fConsole.getDocument();
		int lines = document.getNumberOfLines();
		Object[] listeners = fListeners.getListeners();
		for (int line = fLinesProcessed; line < lines; line++) {
			String delimiter = null;
			try {
				delimiter = document.getLineDelimiter(line);
			} catch (BadLocationException e) {
				DebugUIPlugin.log(e);
				return;
			}
			if (delimiter == null && !fConsole.getProcess().isTerminated()) {
				// line not complete yet
				return;
			}
			fLinesProcessed++;
			IRegion lineRegion = null;
			try {
				lineRegion = document.getLineInformation(line);
			} catch (BadLocationException e) {
				DebugUIPlugin.log(e);
				return;
			}
			for (int i = 0; i < listeners.length; i++) {
				IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
				listener.lineAppended(lineRegion);
			}
		}		
	}
	
	/**
	 * Adds the given listener to the list of listeners notified when a line of
	 * text is appended to the console.
	 * 
	 * @param listener
	 */
	public void addConsoleListener(IConsoleLineTracker listener) {
		fListeners.add(listener);
	}
	
	protected void setLinesProcessed(int linesProcessed) {
		fLinesProcessed = linesProcessed;
	}

	protected int getLinesProcessed() {
		return fLinesProcessed;
	}
	
	/**
	 * Process the last line of the console when the process terminates, if required.
	 * 
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		if (fConsole != null) {
			for (int i = 0; i < events.length; i++) {
				DebugEvent event = events[i];
				if (event.getSource() == fConsole.getProcess() && event.getKind() == DebugEvent.TERMINATE) {
					DebugPlugin.getDefault().removeDebugEventListener(this);
					processNewLines();
				}
			}
		}
	}

}
