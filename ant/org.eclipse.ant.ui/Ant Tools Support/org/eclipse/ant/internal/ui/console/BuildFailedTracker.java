/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;


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
 * Generates hyperlinks for build failures
 */
public class BuildFailedTracker implements IConsoleLineTracker {
	
	private IConsole fConsole;
	private StringMatcher fErrorMatcher;
	private StringMatcher fErrorMatcher2;
	private boolean fBuildFailed= false;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#init(org.eclipse.debug.ui.console.IConsole)
	 */
	public void init(IConsole console) {
		fConsole = console;
		//BUILD FAILED: file:c:/1115/test/buildFiles/23638.xml:12:
		fErrorMatcher = new StringMatcher("*BUILD FAILED: *.xml*", false, false); //$NON-NLS-1$
		fErrorMatcher2= new StringMatcher("*.xml*", false, false); //$NON-NLS-1$
	}

	/* (non-Javadoc)
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
			int index= -1;
			if (fErrorMatcher.match(text)) {
				fBuildFailed= true;
				index = text.indexOf("file:"); //$NON-NLS-1$
				if (index > 0) {
					fileStart = index + 5;
				} else {
					fileStart = text.indexOf("BUILD FAILED:") + 14; //$NON-NLS-1$
					index= fileStart;
				}
			} else if (fBuildFailed && fErrorMatcher2.match(text)) {
				//output resulting from failures which occurred in nested build from using the ant task:
				//BUILD FAILED: C:\Darins\Debugger\20021213\eclipse\runtime-workspace\Mine\build.xml:4: Following error occured while executing this line
				//C:\Darins\Debugger\20021213\eclipse\runtime-workspace\Mine\subbuild.xml:4: srcdir attribute must be set!
				index= 0;
				fileStart= 0;
			}
			if (index > -1) {
				index = text.indexOf("xml", index); //$NON-NLS-1$
				if (index > 0) {
					int numberStart= index + 4;
					int numberEnd= text.indexOf(':', numberStart);
					int fileEnd = index + 3;
					if (numberStart > 0 && fileEnd > 0) {
						fileName = text.substring(fileStart, fileEnd).trim();
						if (numberEnd > 0) {
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
				IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(fileName));
				IFile file= null;
				if (files.length > 0) {
					file= files[0];
				}
				if (file != null && file.exists()) {
					FileLink link = new FileLink(file, null, -1, -1, num);
					fConsole.addLink(link, lineOffset + fileStart, lineLength - fileStart);
				}
			}
		} catch (BadLocationException e) {
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.console.IConsoleLineTracker#dispose()
	 */
	public void dispose() {
		fConsole = null;
	}
}