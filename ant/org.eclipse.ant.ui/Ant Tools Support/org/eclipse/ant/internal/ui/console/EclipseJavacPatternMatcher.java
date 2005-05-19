/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.console;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class EclipseJavacPatternMatcher implements IPatternMatchListenerDelegate {

    private TextConsole fConsole;

    private Pattern fLineNumberPattern = Pattern.compile("\\d+"); //$NON-NLS-1$

    public void connect(TextConsole console) {
        fConsole = console;
    }

    public void disconnect() {
        fConsole = null;
    }

    
    /* [javac] 1. ERROR in /Users/kevinbarnes/Eclipse/runtime-workspace/org.eclipse.ant.core/src_ant/org/eclipse/ant/internal/core/ant/InternalAntRunner.java
     * [javac]  (at line 66)
     */
    public void matchFound(PatternMatchEvent event) {
        int eventOffset = event.getOffset();
        int eventLength = event.getLength();
        IDocument document = fConsole.getDocument();
        String matchedText;
        try {
            matchedText = document.get(eventOffset, eventLength);
        } catch (BadLocationException e) {
            AntUIPlugin.log(e);
            return;
        }

        int index = matchedText.indexOf("ERROR in"); //$NON-NLS-1$
        String filePath = matchedText.substring(index+8).trim();

        int fileStart = matchedText.indexOf(filePath);
        eventOffset += fileStart;
        eventLength = filePath.length();

        if (filePath == null) {
            return; 
        }

        IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(filePath));
        IFile file = null;
        if (files.length > 0) {
            file = files[0];
        }
        if (file == null) {
            return; 
        }
        
        int lineNumber = getLineNumber(document, eventOffset);
        
        FileLink link = new FileLink(file, null, -1, -1, lineNumber);
        try {
            fConsole.addHyperlink(link, eventOffset, eventLength);
        } catch (BadLocationException e) {
            AntUIPlugin.log(e);
        }
    }

    private int getLineNumber(IDocument document, int eventOffset) {
        try {
            int fileLine = document.getLineOfOffset(eventOffset);
            IRegion region = document.getLineInformation(++fileLine);
            String lineLine = document.get(region.getOffset(), region.getLength());
            Matcher matcher = null;
            synchronized (fLineNumberPattern) {
                matcher = fLineNumberPattern.matcher(lineLine);
            }
            if (matcher.find()) {
                String lineString = matcher.group();
                return Integer.parseInt(lineString);
            }
            
        } catch (BadLocationException e) {
            AntUIPlugin.log(e);
        }
        return -1;
    }

}
