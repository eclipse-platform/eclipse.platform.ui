package org.eclipse.ant.tests.ui.testplugin;

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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Simple console line tracker extension point that delegates messages 
 */
public class ConsoleLineTracker implements IConsoleLineTracker {
	
	private static IConsole console;
	private static List lines= new ArrayList(); 
	

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		console = null;
		lines= new ArrayList();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public synchronized void init(IConsole console) {
		ConsoleLineTracker.console= console;
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		lines.add(line);
	}
	
	public static int getNumberOfMessages() {
		return lines.size();
	}
	
	public static String getMessage(int index) {
		if (index < lines.size()){
			IRegion lineRegion= (IRegion)lines.get(index);
			try {
				console.getDocument().get(lineRegion.getOffset(), lineRegion.getLength());
			} catch (BadLocationException e) {
				return null;
			}
		}
		return null;
	}
	
}
