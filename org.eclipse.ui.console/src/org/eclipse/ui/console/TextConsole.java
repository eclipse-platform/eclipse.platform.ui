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

package org.eclipse.ui.console;

import java.util.HashMap;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.console.ConsoleDocument;
import org.eclipse.ui.internal.console.ConsoleHyperlinkPosition;
import org.eclipse.ui.internal.console.ConsolePatternMatcher;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * An abstract console that supports regular expression matching.
 * <p>
 * Pattern match listeners can be registered with a console programmatically
 * or via the <code>org.eclipse.ui.console.consolePatternMatchListeners</code>
 * extension point. Listeners are notified of matches in the console.
 * </p>
 * <p>
 * Subclasses must provide a document partitioner.
 * </p>
 * @since 3.1
 */
public abstract class TextConsole extends AbstractConsole {

    /**
     * The current width of the console. Used for fixed width consoles.
     * A value of <=0 means does not have a fixed width.
     */
    private int fConsoleWidth;
    /**
     * The current tab width
     */
    private int fTabWidth;
    /** 
	 * The font used by this console
	 */
    private Font fFont;
    /**
     * Whether the console srolls to show the end of text as output
     * is appended.
     */
    private boolean fAutoScroll;
    
    /**
     * The Console's regular expression pattern matcher
     */
    private ConsolePatternMatcher fPatternMatcher;
    
    /**
     * The Console's document
     */
    private ConsoleDocument fDocument;
    
   /**
    * indication that the console's partitioner is not expecting more input
    */
    private boolean fPartitionerFinished = false;
    
    /**
     * Indication that the console's pattern matcher has finished.
     * (all matches have been found and all listeners notified)
     */
    private boolean fMatcherFinished = false;
    
    /**
     * indication that the console output complete property has been fired
     */
    private boolean fCompleteFired = false;

    
    /**
     * Map of client defined attributes
     */
    private HashMap fAttributes = new HashMap();
   
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.AbstractConsole#dispose()
     */
    protected void dispose() {
        super.dispose();
        fFont = null;
		synchronized(fAttributes) {
		    fAttributes.clear();
		}
    }
    /**
     * Constructs a console with the given name, image descriptor, and lifecycle
     * 
     * @param name name to display for this console
     * @param consoleType console type identifier or <code>null</code>
     * @param imageDescriptor image to display for this console or <code>null</code>
     * @param autoLifecycle whether lifecycle methods should be called automatically
     *  when this console is added/removed from the console manager
     */
    public TextConsole(String name, String consoleType, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
        super(name, consoleType, imageDescriptor, autoLifecycle);
        fDocument = new ConsoleDocument();
        fDocument.addPositionCategory(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
        fPatternMatcher = new ConsolePatternMatcher(this);
        fDocument.addDocumentListener(fPatternMatcher);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    public IPageBookViewPage createPage(IConsoleView view) {
        return new TextConsolePage(this, view);
    }
    
	/**
	 * Returns this console's document.
	 * 
	 * @return this console's document
	 */
    public IDocument getDocument() {
        return fDocument;
    }
    
    /**
     * Returns the current width of this console. A value of zero of less 
     * indicates this console has no fixed width.
     * 
     * @return the current width of this console
     */
    public int getConsoleWidth() {
        return fConsoleWidth;
    }
    
    /**
     * Sets the width of this console in characters. Any value greater than zero
     * will cause this console to have a fixed width.
     * 
     * @param width the width to make this console. Values of 0 or less imply
     * the console does not have any fixed width.
     */
    public void setConsoleWidth(int width) {
        if (fConsoleWidth != width) {
            int old = fConsoleWidth;
            fConsoleWidth = width;
            
            firePropertyChange(this, IConsoleConstants.P_CONSOLE_WIDTH, new Integer(old), new Integer(fConsoleWidth));
        }
    }

	/**
	 * Sets the tab width.
	 * 
	 * @param newTabWidth the tab width 
	 */
    public void setTabWidth(final int newTabWidth) {
        if (fTabWidth != newTabWidth) {
            final int oldTabWidth = fTabWidth;
            fTabWidth = newTabWidth;
            ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    firePropertyChange(TextConsole.this, IConsoleConstants.P_TAB_SIZE, new Integer(oldTabWidth), new Integer(fTabWidth));           
                }
            });
        }
    }
    
	/**
	 * Returns the tab width.
	 * 
	 * @return tab width
	 */
    public int getTabWidth() {
        return fTabWidth;
    }
    
    /**
	 * Returns the font used by this console. Must be called in the UI thread.
	 * 
	 * @return font used by this console
	 */
    public Font getFont() {
        if (fFont == null) {
            fFont = getDefaultFont();
        }
        return fFont;
    }
    
    /**
     * Returns the default text font.
     * 
     * @return the default text font
     */
    private Font getDefaultFont() {
        return JFaceResources.getFont(JFaceResources.TEXT_FONT);
    }
    
	/**
	 * Sets the font used by this console. Specify <code>null</code> to use
	 * the default text font.
	 * 
	 * @param font font, or <code>null</code> to indicate the default font
	 */
    public void setFont(Font newFont) {
        // ensure font is initialized
        getFont();
        // translate null to default font
        if (newFont == null) {
            newFont = getDefaultFont();
        }
        // fire property change if requried
        if (!fFont.equals(newFont)) {
            Font old = fFont;
            fFont = newFont;
            firePropertyChange(this, IConsoleConstants.P_FONT, old, fFont);
        }
    }
    
	/**
	 * Sets whether this console scrolls automatically to show the end of text as
	 * output is appened to the console.
	 * 
	 * @param scroll whether this console scrolls automatically
	 */
	public void setAutoScroll(boolean scroll) {
	    if (scroll != fAutoScroll) {
	        fAutoScroll = scroll;
	        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
	            public void run() {
	                firePropertyChange(TextConsole.this, IConsoleConstants.P_AUTO_SCROLL, new Boolean(!fAutoScroll), new Boolean(fAutoScroll));
	            }
	        });
	    }
	}
	
	/**
	 * Returns whether this console scrolls automatically to show the end of text as
	 * output is appened to the console.
	 * 
	 * @return whether this console scrolls automatically
	 */	
	public boolean getAutoScroll() {
	    return fAutoScroll;
	}
	
    /**
     * Clears the console.
     */
    public void clearConsole() {
        IConsoleDocumentPartitioner partitioner = getPartitioner();
        if (partitioner != null) {
            partitioner.clearBuffer();
        }
    }

    /**
     * Returns the console's document partitioner.
     * @return
     */
    protected abstract IConsoleDocumentPartitioner getPartitioner();
    
    /**
     * Returns all hyperlinks in this console.
     * 
     * @return all hyperlinks in this console
     */
    public IHyperlink[] getHyperlinks() {
        try {
            Position[] positions = getDocument().getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
            IHyperlink[] hyperlinks = new IHyperlink[positions.length];
            for (int i = 0; i < positions.length; i++) {
                ConsoleHyperlinkPosition position = (ConsoleHyperlinkPosition) positions[i];
                hyperlinks[i] = position.getHyperLink();
            }
            return hyperlinks;
        } catch (BadPositionCategoryException e) {
            return new IHyperlink[0];
        }
    }
    
    /**
     * Returns the hyperlink at the given offset of <code>null</code> if none.
     * 
     * @param offset the hyperlink at the given offset of <code>null</code> if none
     * @return
     */
    public IHyperlink getHyperlink(int offset) {
        try {
        	IDocument document = getDocument();
        	if (document != null) {
	            Position[] positions = document.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
	            Position position = findPosition(offset, positions);
	            if (position instanceof ConsoleHyperlinkPosition) {
	                return ((ConsoleHyperlinkPosition) position).getHyperLink();
	            }
        	}
        } catch (BadPositionCategoryException e) {
        }        
        return null;
    }

	/**
	 * Binary search for the position at a given offset.
	 *
	 * @param offset the offset whose position should be found
	 * @return the position containing the offset, or <code>null</code>
	 */
	private Position findPosition(int offset, Position[] positions) {
		
		if (positions.length == 0)
			return null;
			
		int left= 0;
		int right= positions.length -1;
		int mid= 0;
		Position position= null;
		
		while (left < right) {
			
			mid= (left + right) / 2;
				
			position= positions[mid];
			if (offset < position.getOffset()) {
				if (left == mid)
					right= left;
				else
					right= mid -1;
			} else if (offset > (position.getOffset() + position.getLength() - 1)) {
				if (right == mid)
					left= right;
				else
					left= mid  +1;
			} else {
				left= right= mid;
			}
		}
		
		position= positions[left];
		if (offset >= position.getOffset() && (offset < (position.getOffset() + position.getLength()))) {
			return position;
		}
		return null;
	}

    /**
     * Adds the given pattern match listener to this console. The listener will
     * be connected and receive match notifications.
     * 
     * @param listener the listener to add
     */
    public void addPatternMatchListener(IPatternMatchListener listener) {
        fPatternMatcher.addPatternMatchListener(listener);
    }
    
    /**
     * Removes the given pattern match listener from this console. The listener will be
     * disconnected and will no longer receive match notifications.
     * 
     * @param listener the pattern match listener to remove.
     */
    public void removePatternMatchListener(IPatternMatchListener listener) {
        fPatternMatcher.removePatternMatchListener(listener);
    }    
    
    
    /**
     * Job scheduling rule that prevent the job from running if the console's PatternMatcher
     * is active.
     */
    private class MatcherSchedulingRule implements ISchedulingRule {
        public boolean contains(ISchedulingRule rule) {
            return rule == this;
        }

        public boolean isConflicting(ISchedulingRule rule) {
            if (contains(rule)) {
                return true;
            }
            if (rule != this && rule instanceof MatcherSchedulingRule) {
                return (((MatcherSchedulingRule)rule).getConsole() == TextConsole.this);   
            }
            return false;
        }
        
        public TextConsole getConsole() {
            return TextConsole.this;
        }
    }
    
    /**
     * @return a scheduling rule which can be used to prevent jobs from running
     * while the console's pattern matcher is active.
     */
    public ISchedulingRule getSchedulingRule() {
        return new MatcherSchedulingRule();
    }
    
    /**
     * Partitioners should call this method when they are not expecting any new data
     * to be appended to the document. 
     */
    public void partitionerFinished() {
        fPatternMatcher.forceFinalMatching();
        fPartitionerFinished  = true;
        checkFinished();
    }
    
    /**
     * Called by the Console's Pattern Matcher when matching is complete and all
     * listeners have been notified.
     * 
     * Clients should not call this method.
     */
    public void matcherFinished() {
        fMatcherFinished = true;
        fDocument.removeDocumentListener(fPatternMatcher);
        checkFinished();
    }
    
    /**
     * Fires the console output complete property change event.
     */
    private synchronized void checkFinished() {
        if (!fCompleteFired && fPartitionerFinished && fMatcherFinished ) {
            fCompleteFired = true;
            firePropertyChange(this, IConsoleConstants.P_CONSOLE_OUTPUT_COMPLETE, null, null);
        }
    }
    
    /**
     * Adds a hyperlink to this console.
     * 
     * @param hyperlink the hyperlink to add
     * @param offset the offset in the console document at which the hyperlink should be added
     * @param length the length of the text which should be hyperlinked
     * @throws BadLocationException if the specified location is not valid.
     */
    public void addHyperlink(IHyperlink hyperlink, int offset, int length) throws BadLocationException {
        IDocument document = getDocument();
		ConsoleHyperlinkPosition hyperlinkPosition = new ConsoleHyperlinkPosition(hyperlink, offset, length); 
		try {
			document.addPosition(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY, hyperlinkPosition);
			firePropertyChange(this, IConsoleConstants.P_CONSOLE_HYPERLINK_ADDED, null, null);
		} catch (BadPositionCategoryException e) {
			ConsolePlugin.log(e);
		} 
    }
    
    /**
     * Returns the region assocaited with the given hyperlink.
     * 
     * @param link hyperlink
     * @return the region associated witht the hyperlink
     */
    public IRegion getRegion(IHyperlink link) {
		try {
		    IDocument doc = getDocument();
		    if (doc != null) {
				Position[] positions = doc.getPositions(ConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
				for (int i = 0; i < positions.length; i++) {
					ConsoleHyperlinkPosition position = (ConsoleHyperlinkPosition)positions[i];
					if (position.getHyperLink().equals(link)) {
						return new Region(position.getOffset(), position.getLength());
					}
				}
		    }
		} catch (BadPositionCategoryException e) {
		}
		return null;
    }
    
    /**
     * Returns the attribue associated with the specified key.
     * 
     * @param key attribute key
     * @return Returns the attribue associated with the specified key
     */
    public Object getAttribute(String key) {
        synchronized (fAttributes) {
            return fAttributes.get(key);
        }
    }
    
    /**
     * Sets an attribute value.
     * 
     * @param key attribute key
     * @param value attribute value
     */
    public void setAttribute(String key, Object value) {
        synchronized(fAttributes) {
            fAttributes.put(key, value);
        }
    }
}
