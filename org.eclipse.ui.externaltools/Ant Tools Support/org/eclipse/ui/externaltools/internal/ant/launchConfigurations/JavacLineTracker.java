package org.eclipse.ui.externaltools.internal.ant.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;

/**
 * Generates hyperlinks for javac output
 */
public class JavacLineTracker implements IConsoleLineTracker {
	
	private IConsole fConsole;
	private StringMatcher fErrorMatcher;

	/**
	 * Constructor for JavacLineTracker.
	 */
	public JavacLineTracker() {
		super();
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		fErrorMatcher = new StringMatcher("*[javac]*ERROR in*.java*(at line*)*",false, false); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#lineAppended(org.eclipse.jface.text.IRegion)
	 */
	public void lineAppended(IRegion line) {
		try {
			int lineOffset = line.getOffset();
			int lineLength = line.getLength();
			String text = fConsole.getDocument().get(lineOffset, lineLength);
			if (fErrorMatcher.match(text)) {
				int index = text.indexOf("ERROR in"); //$NON-NLS-1$
				int fileStart = index + 9;
				index = text.lastIndexOf("(at line "); //$NON-NLS-1$
				int fileEnd = index - 1;
				int numberStart = index + 9;
				index = text.lastIndexOf(')');
				int numberEnd = index;
				String fileName = text.substring(fileStart, fileEnd).trim();
				String lineNumber = text.substring(numberStart, numberEnd).trim();
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
