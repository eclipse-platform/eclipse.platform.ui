/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.AbstractDocument;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.GapTextStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.client.listeners.IConsoleListener;

public class ConsoleDocument extends AbstractDocument {
	public static final int COMMAND = 0; // command text
	public static final int MESSAGE = 1; // message received
	public static final int ERROR = 2;   // error received
	public static final int STATUS = 3;  // status text
	public static final int DELIMITER = 4; // delimiter text between runs

	private int[] lineTypes = null;
	private int   currentLine = 0;

	/**
	 * Creates an empty console document.
	 */
	public ConsoleDocument() {
		setTextStore(new GapTextStore(512, 1024));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
	
	/**
	 * Clears the console document.
	 */
	public void clear() {
		lineTypes = null;
		currentLine = 0;
		set(""); //$NON-NLS-1$
	}
	
	/**
	 * Gets the line type for the line containing the specified offset.
	 */
	public int getLineType(int offset) {
		try {
			int line = getLineOfOffset(offset);
			if (line < currentLine) return lineTypes[line];
		} catch (BadLocationException e) {
			CVSProviderPlugin.log(CVSException.wrapException(e));
		}
		return 0;
	}
	
	/**
	 * Appends a line of the specified type to the end of the console.
	 */
	public void appendConsoleLine(int type, String line) {
		if (lineTypes == null) {
			lineTypes = new int[16];
		} else if (currentLine >= lineTypes.length) {
			int[] oldLineTypes = lineTypes;
			lineTypes = new int[oldLineTypes.length * 2];
			System.arraycopy(oldLineTypes, 0, lineTypes, 0, oldLineTypes.length);
		}
		lineTypes[currentLine++] = type;
		try { 
			replace(getLength(), 0, line + "\n"); //$NON-NLS-1$
		} catch (BadLocationException e) {
			CVSProviderPlugin.log(CVSException.wrapException(e));
		}
	}
}
