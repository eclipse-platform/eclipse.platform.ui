/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.views.console;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

/**
 * Tracks text appended to the console and notifies listeners in terms of whole
 * lines.
 */
public class ConsoleLineNotifier implements IPatternMatchListener, IPropertyChangeListener {
	/**
	 * Console listeners
	 */
	private List fListeners = new ArrayList(2);

	/**
	 * The console this notifier is tracking 
	 */
	private ProcessConsole fConsole = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IPatternMatchListenerDelegate#connect(org.eclipse.ui.console.TextConsole)
	 */
	public void connect(TextConsole console) {
	    if (console instanceof ProcessConsole) {
	        fConsole = (ProcessConsole)console;

	        IConsoleLineTracker[] lineTrackers = DebugUIPlugin.getDefault().getProcessConsoleManager().getLineTrackers(fConsole.getProcess());
	        for (int i = 0; i < lineTrackers.length; i++) {
	            lineTrackers[i].init(fConsole);
                addConsoleListener(lineTrackers[i]);
            }
	        
	        fConsole.addPropertyChangeListener(this);
	    }
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IPatternMatchListener#disconnect()
	 */
	public synchronized void disconnect() {
        try {
            IDocument document = fConsole.getDocument();
            if (document != null) {
                int lastLine = document.getNumberOfLines() - 1;
                if (document.getLineDelimiter(lastLine) == null) {
                    IRegion lineInformation = document.getLineInformation(lastLine);
                    lineAppended(lineInformation);
                }
            }
        } catch (BadLocationException e) {
        }
    }

    /**
     * Notification the console's streams have been closed
     */
    public synchronized void consoleClosed() {
        int size = fListeners.size();
        for (int i = 0; i < size; i++) {
            IConsoleLineTracker tracker = (IConsoleLineTracker) fListeners.get(i);
            if (tracker instanceof IConsoleLineTrackerExtension) {
                ((IConsoleLineTrackerExtension) tracker).consoleClosed();
            }
            tracker.dispose();
        }

        fConsole = null;
        fListeners = null;
    }
	
	/**
     * Adds the given listener to the list of listeners notified when a line of
     * text is appended to the console.
     * 
     * @param listener the listener to add 
     */
	public void addConsoleListener(IConsoleLineTracker listener) {
        if (!fListeners.contains(listener))
            fListeners.add(listener);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public void matchFound(PatternMatchEvent event) {
        try  {
            IDocument document = fConsole.getDocument();
            int lineOfOffset = document.getLineOfOffset(event.getOffset());
            String delimiter = document.getLineDelimiter(lineOfOffset);
            int strip = delimiter==null ? 0 : delimiter.length();
            Region region = new Region(event.getOffset(), event.getLength()-strip); 
            lineAppended(region);
        } catch (BadLocationException e) {}
    }
    
    public void lineAppended(IRegion region) {
        int size = fListeners.size();
        for (int i=0; i<size; i++) {
            IConsoleLineTracker tracker = (IConsoleLineTracker) fListeners.get(i);
            tracker.lineAppended(region);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getProperty().equals(IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE)) {
            fConsole.removePropertyChangeListener(this);
            consoleClosed();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#getPattern()
     */
    public String getPattern() {
        return ".*\\r(\\n?)|.*\\n"; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#getCompilerFlags()
     */
    public int getCompilerFlags() {
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#getLineQualifier()
     */
    public String getLineQualifier() {
        return "\\n|\\r"; //$NON-NLS-1$
    }

}
