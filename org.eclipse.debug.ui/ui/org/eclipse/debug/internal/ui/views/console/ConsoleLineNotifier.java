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


import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;

/**
 * Tracks text appended to the console and notifies listeners in terms of whole
 * lines.
 */
public class ConsoleLineNotifier implements IPatternMatchListener, IPropertyChangeListener {
	/**
	 * Console listeners
	 */
	private ListenerList fListeners = new ListenerList(2);

	/**
	 * The console this notifier is tracking 
	 */
	private ProcessConsole fConsole = null;
	
	/**
	 * Connects this notifier to the given console.
	 *  
	 * @param console
	 */
	public void connect(IConsole console) {
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
	
	/**
	 * Disposes this notifier 
	 */
	public synchronized void disconnect() {
	    if (fConsole == null) {
	        return; //already disconnected
	    }

	    Object[] listeners = fListeners.getListeners();
	    for (int i = 0; i < listeners.length; i++) {
	        IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
	        listener.dispose();
	    }
	
	    fConsole.removePropertyChangeListener(this);
	    
	    fListeners = null;
	    fConsole = null;
	}
		
	/**
	 * Notification the console's streams have been closed
	 */
	public synchronized void streamsClosed() {
	    try {
	        if (fConsole != null) {
	            IDocument document = fConsole.getDocument();
	            if (document != null) {
	                int lastLine = document.getNumberOfLines()-1;	       
	                if (document.getLineDelimiter(lastLine) == null) {
	                    IRegion lineInformation = document.getLineInformation(lastLine);
	                    lineAppended(lineInformation);
	                }
	            }
	        }
	    } catch (BadLocationException e) {
	        e.printStackTrace();
	    }
	    
	    Object[] listeners= fListeners.getListeners();
	    for (int i = 0; i < listeners.length; i++) {
	        Object obj = listeners[i];
	        if (obj instanceof IConsoleLineTrackerExtension) {
	            ((IConsoleLineTrackerExtension)obj).consoleClosed();
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
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public void matchFound(PatternMatchEvent event) {
        try  {
            IDocument document = fConsole.getDocument();
            String text = document.get(event.getOffset(), event.getLength());
            int strip = 1;
            int length = text.length();
            if (length >= 2) {
                char c = text.charAt(length - 2);
                if (c == '\r') {
                    strip = 2;
                }
                text = text.substring(0, length - strip);
            } else {
                text = ""; //$NON-NLS-1$
            }
            Region region = new Region(event.getOffset(), text.length()); 
            lineAppended(region);
        } catch (BadLocationException e) {}
    }
    
    public void lineAppended(IRegion region) {
        Object[] listeners = fListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
            listener.lineAppended(region);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getProperty().equals(IOConsole.P_CONSOLE_OUTPUT_COMPLETE)) {
            streamsClosed();
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
        return null;
    }

}
