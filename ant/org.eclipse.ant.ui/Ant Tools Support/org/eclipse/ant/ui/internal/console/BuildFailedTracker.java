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
package org.eclipse.ant.ui.internal.console;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.externaltools.internal.model.StringMatcher;

/**
 * Generates hyperlinks for build failures resulting from XML syntax errors
 */
public class BuildFailedTracker implements IConsoleLineTracker {
	
	private IConsole fConsole;
	private StringMatcher fErrorMatcher;
	
	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		//BUILD FAILED: file:c:/1115/test/buildFiles/23638.xml:12:
		fErrorMatcher = new StringMatcher("*BUILD FAILED: file:*.xml*",false, false); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		try {
			int lineOffset = line.getOffset();
			int lineLength = line.getLength();
			String text = fConsole.getDocument().get(lineOffset, lineLength);
			String fileName = null;
			String lineNumber = ""; //$NON-NLS-1$
			int fileStart = -1;
			if (fErrorMatcher.match(text)) {
				int index = text.indexOf("file:"); //$NON-NLS-1$
				if (index > 0) {
					fileStart = index + 5;
					index = text.indexOf(' ', index); //$NON-NLS-1$
					if (index > 0) {
						int fileEnd = index-3;
						int numberStart = index - 2;
						index = text.indexOf(':', numberStart);
						if (index > 0) {
							int numberEnd = index;
							fileName = text.substring(fileStart, fileEnd).trim();
							lineNumber = text.substring(numberStart, numberEnd).trim();
						}
					}
				}
			} 
			if (fileName != null) {
				int num = -1;
				try {
					num = Integer.parseInt(lineNumber);
				} catch (NumberFormatException e) {
				}
				IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
				if (file != null && file.exists()) {
					FileLink link = new FileLink(file, null, -1, -1, num);
					fConsole.addLink(link, lineOffset + fileStart, lineLength - fileStart);
				}
			}
		} catch (BadLocationException e) {
		}
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		fConsole = null;
	}

}
