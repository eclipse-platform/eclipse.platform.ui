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


import org.eclipse.debug.ui.console.IConsole;
import org.eclipse.debug.ui.console.IConsoleLineTracker;
import org.eclipse.debug.ui.console.IConsoleLineTrackerExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;

/**
 * Tracks text appended to the console and notifies listeners in terms of whole
 * lines.
 */
public class ConsoleLineNotifier implements IPatternMatchListener {
	/**
	 * Console listeners
	 */
	private ListenerList fListeners = new ListenerList(2);

	/**
	 * The console this notifier is tracking 
	 */
	private IConsole fConsole = null;

    private String[] lineDelimiters;
	
	
	/**
	 * Connects this notifier to the given console.
	 *  
	 * @param console
	 */
	public void connect(IConsole console) {
		fConsole = console;
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
			listener.init(console);
		}
		lineDelimiters = fConsole.getDocument().getLegalLineDelimiters();
		fConsole.addPatternMatchListener(this);
	}
	
	/**
	 * Disposes this notifier 
	 */
	public synchronized void disconnect() {
	    if (fConsole == null) {
	        return; //already disconnected
	    }
	    fConsole.removePatternMatchListener(this);
	    Object[] listeners = fListeners.getListeners();
	    for (int i = 0; i < listeners.length; i++) {
	        IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
	        listener.dispose();
	    }
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
     * @see org.eclipse.ui.console.IPatternMatchListener#getPattern()
     */
    public String getPattern() {
//      .*[\r,\n,\r\n]
        StringBuffer buffer = new StringBuffer(".*["); //$NON-NLS-1$
        for (int i = 0; i < lineDelimiters.length; i++) {
            String ld = lineDelimiters[i];
            buffer.append(ld);
            if (i != lineDelimiters.length-1) {
                buffer.append(","); //$NON-NLS-1$
            }
        }
        buffer.append("]"); //$NON-NLS-1$
        return buffer.toString(); 
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IPatternMatchListener#matchFound(org.eclipse.ui.console.PatternMatchEvent)
     */
    public void matchFound(PatternMatchEvent event) {
        //the match includes the delimiter. Need to create a region that does 
        //not include it. Windows /r/n is two characters.
        try  {
            IDocument document = fConsole.getDocument();
            int line = document.getLineOfOffset(event.getOffset());
            String delimiter = document.getLineDelimiter(line);
            // int delimLength = delimiter != null ? delimiter.length() : 0;
            // TODO: looks like a bug in text support - on Windows, line delim is /r/n
            // but the event on includes /r
            String text = document.get(event.getOffset(), event.getLength());
            int delimStart = text.lastIndexOf(delimiter.charAt(0));
            Region region = new Region(event.getOffset(), delimStart); 
            lineAppended(region);
        } catch (BadLocationException e) {}
    }
    
    
    int numLines = 0;
    public void lineAppended(IRegion region) {
        numLines++;
        Object[] listeners = fListeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            IConsoleLineTracker listener = (IConsoleLineTracker)listeners[i];
            listener.lineAppended(region);
        }
    }
}
