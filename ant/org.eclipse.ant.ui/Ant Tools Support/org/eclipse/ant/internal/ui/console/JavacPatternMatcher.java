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

import org.eclipse.ant.internal.ui.AntUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class JavacPatternMatcher implements IPatternMatchListenerDelegate {

    private TextConsole fConsole;

    public void connect(TextConsole console) {
        fConsole = console;
    }

    public void disconnect() {
        fConsole = null;
    }

    /*
     *     [javac] /Users/kevinbarnes/Eclipse/runtime-workspace/Foo/src/CarriageReturn.java:4: ';' expected
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

        int numEnd = matchedText.lastIndexOf(':');
        int numStart= matchedText.lastIndexOf(':', numEnd - 1);
        
        int index = matchedText.indexOf("]"); //$NON-NLS-1$
        String filePath = matchedText.substring(index + 1, numStart);
        filePath = filePath.trim();

        int fileStart = matchedText.indexOf(filePath);
        eventOffset += fileStart;
        eventLength = filePath.length();
        
        String lineNumberString = matchedText.substring(numStart + 1, numEnd);
        int lineNumber = Integer.parseInt(lineNumberString);
            
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
        
        FileLink link = new FileLink(file, null, -1, -1, lineNumber);
        try {
            fConsole.addHyperlink(link, eventOffset, eventLength);
        } catch (BadLocationException e) {
            AntUIPlugin.log(e);
        }
    }
}