/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Francis Devereux (francis@devrx.org) - bug 66861
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
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
	private IFile fLastFile;

    /** used to find the end of the javac task label within matches found by fEclipseCompilerMatcher */
	private Pattern fEclipseCompilerTaskEndPattern;

	private Pattern fJavacPattern;
	private Pattern fJikesPattern;
    
    /** used to find the end of the javac task label */
    private Pattern fJavacTaskEndPattern;
	
	// trolling for errors after a Jikes error header was found
	private boolean fTrolling = false;

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
		fEclipseCompilerTaskEndPattern = Pattern.compile("javac.*\\].*ERROR in "); //$NON-NLS-1$
		fJavacPattern = Pattern.compile(".*\\[.*javac.*\\] .*\\.java:.*:.*"); //$NON-NLS-1$
		fJikesPattern = Pattern.compile("\\[javac\\] "); //$NON-NLS-1$
        fJavacTaskEndPattern = Pattern.compile("\\[javac\\] "); //$NON-NLS-1$
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
            
            Matcher eclipseCompilerTaskEndMatcher = fEclipseCompilerTaskEndPattern.matcher(text);
            Matcher javacMatcher = fJavacPattern.matcher(text);
            Matcher jikesMatcher = fJikesPattern.matcher(text);
            Matcher javacTaskEndMatcher = fJavacTaskEndPattern.matcher(text);
            
            if (eclipseCompilerTaskEndMatcher.find()) {
                fTrolling = false;
                int taskEndPos = eclipseCompilerTaskEndMatcher.end();
                
                fileStart = taskEndPos;
                int index = text.lastIndexOf("(at line "); //$NON-NLS-1$
                if (index > 0) {
                    int fileEnd = index - 1;
                    int numberStart = index + 9;
                    index = text.lastIndexOf(')');
                    if (index > 0) {
                        int numberEnd = index;
                        fileName = text.substring(fileStart, fileEnd).trim();
                        lineNumber = text.substring(numberStart, numberEnd).trim();
                    }
                }
            } else if (javacMatcher.find() && javacTaskEndMatcher.find()) {
                fTrolling = false;
                int taskEndPos = javacTaskEndMatcher.end();
                
                fileStart = taskEndPos;
                int index = text.indexOf(".java:", fileStart); //$NON-NLS-1$
                if (index > 0) {
                    int numberStart = index + 6;
                    fileName = text.substring(fileStart, numberStart - 1).trim();
                    index = text.indexOf(":", numberStart); //$NON-NLS-1$
                    if (index > numberStart) {
                        lineNumber = text.substring(numberStart, index);
                    }
                }
                
            } else if (jikesMatcher.find()) {
                fileStart = text.indexOf('"');
                fileStart++;
                int index = text.indexOf(".java\"", fileStart); //$NON-NLS-1$
                if (index > 0) {
                    index += 5;
                    fileName = text.substring(fileStart, index).trim();
                    fTrolling = true;
                }
            } else if (fTrolling && javacTaskEndMatcher.find()) {
                int taskEndPos = javacTaskEndMatcher.end();
                
			    // look for a line number
			    int index = taskEndPos;
			    int numEnd = text.indexOf(".", index); //$NON-NLS-1$
			    if (numEnd > 0) {
			        String number = text.substring(index, numEnd).trim();
			        try {
			            int num = Integer.parseInt(number);
			            int numStart = text.indexOf(number, index);
			            if (fLastFile != null && fLastFile.exists()) {
			                FileLink link = new FileLink(fLastFile, null, -1, -1, num);
			                fConsole.addLink(link, lineOffset + numStart, lineLength - numStart);
			            }
			        } catch (NumberFormatException e) {
			            // not a line number
			        }
				} else {
					fTrolling = false;
				}
			}
			if (fileName != null) {
				int num = -1;
				try {
					num = Integer.parseInt(lineNumber);
				} catch (NumberFormatException e) {
				}
				IFile file= null;
				IPath filePath= new Path(fileName);
				//first check if in the same file...faster
				if (fLastFile != null && fLastFile.getLocation().equals(filePath)) {
					file= fLastFile;
				} else {
					IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(filePath);
					if (files.length != 0) {
						file= files[0];
					}
				}
				fLastFile = file;
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
