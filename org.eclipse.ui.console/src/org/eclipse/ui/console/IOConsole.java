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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.console.IOConsoleDocument;
import org.eclipse.ui.internal.console.IOConsoleHyperlinkPosition;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.internal.console.IOConsolePartitioner;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays text, accepts keyboard input from users,
 * provides hyperlinks, and supports regular expression matching.
 * The console may have multiple output streams connected to it and
 * provides one input stream connected to the keyboard.
 * <p>
 * Pattern match listeners can be registered with a console programmatically
 * or via the <code>org.eclipse.ui.console.consolePatternMatchListeners</code>
 * extension point. Listeners are notified of matches in the console.
 * </p>
 * <p>
 * Clients may instantiate and subclass this class.
 * </p>
 * @since 3.1
 *
 */
public class IOConsole extends AbstractConsole {
    	
	/**
	 * Property constant indicating the font of this console has changed. 
	 */
	public static final String P_FONT = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_FONT"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating that a font style has changed
	 */
	public static final String P_FONT_STYLE = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_FONT_STYLE"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final String P_STREAM_COLOR = ConsolePlugin.getUniqueIdentifier()  + "IOConsole.P_STREAM_COLOR";	 //$NON-NLS-1$
		
	/**
	 * Property constant indicating tab size has changed 
	 */
	public static final String P_TAB_SIZE = ConsolePlugin.getUniqueIdentifier()  + "IOConsole.P_TAB_SIZE";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating the width of a fixed width console has changed.
	 */
	public static final String P_CONSOLE_WIDTH = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_CONSOLE_WIDTH"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating that all streams connected to this console have been closed
	 * and that all queued output has been processed.
	 */
	public static final String P_CONSOLE_OUTPUT_COMPLETE = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_CONSOLE_STREAMS_CLOSED"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating that the auto scrolling should be turned on (or off)
	 */
	public static final String P_AUTO_SCROLL = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_AUTO_SCROLL"; //$NON-NLS-1$
	
	/** 
	 * The font used by this console
	 */
	private Font font = null;
	
	/**
	 * The default tab size
	 */
	public static final int DEFAULT_TAB_SIZE = 8;
	
	/**
	 * The document partitioner
	 */
    private IOConsolePartitioner partitioner;
    
    /**
     * The stream from which user input may be read
     */
    private IOConsoleInputStream inputStream;
    
    /**
     * The current tab width
     */
    private int tabWidth = DEFAULT_TAB_SIZE;
    
    /**
     * The current width of the console. Used for fixed width consoles.
     * A value of <=0 means does not have a fixed width.
     */
    private int consoleWidth = -1;
        
    /**
     * Map of client defined attributes
     */
    private HashMap attributes = new HashMap();
   
    /**
     * Whether the console srolls to show the end of text as output
     * is appended.
     */
    private boolean autoScroll = true;
        
    /**
     * A collection of open streams connected to this console.
     */
    private List openStreams;

    /**
     * Constructs a console with the given name, type, image, and lifecycle.
     * 
     * @param name name to display for this console
     * @param consoleType console type identifier or <code>null</code>
     * @param imageDescriptor image to display for this console or <code>null</code>
     * @param autoLifecycle whether lifecycle methods should be called automatically
     *  when this console is added/removed from the console manager
     */
    public IOConsole(String name, String consoleType, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
        super(name, imageDescriptor, autoLifecycle);
        setType(consoleType);
        openStreams = new ArrayList();
        inputStream = new IOConsoleInputStream(this);
        openStreams.add(inputStream);
        partitioner = new IOConsolePartitioner(inputStream, this);
        Document document = new IOConsoleDocument();
        document.addPositionCategory(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
        partitioner.connect(document);
    }
    
    /**
     * Constructs a console with the given name, type, and image. Lifecycle methods
     * will be called when this console is added/removed from the console manager.
     * 
     * @param name name to display for this console
     * @param consoleType console type identifier or <code>null</code>
     * @param imageDescriptor image to display for this console or <code>null</code>
     */
    public IOConsole(String name, String consoleType, ImageDescriptor imageDescriptor) {
        this(name, consoleType, imageDescriptor, true);
    }    
    
    /**
     * Constructs a console with the given name and image. Lifecycle methods
     * will be called when this console is added/removed from the console manager.
     * This console will have an unspecified (<code>null</code>) type.
     * 
     * @param name name to display for this console
     * @param imageDescriptor image to display for this console or <code>null</code>
     */
    public IOConsole(String name, ImageDescriptor imageDescriptor) {
        this(name, null, imageDescriptor);
    }
    
    /**
     * Returns the attribue associated with the specified key.
     * 
     * @param key attribute key
     * @return Returns the attribue associated with the specified key
     */
    public Object getAttribute(String key) {
        synchronized (attributes) {
            return attributes.get(key);
        }
    }
    
    /**
     * Sets an attribute value.
     * 
     * @param key attribute key
     * @param value attribute value
     */
    public void setAttribute(String key, Object value) {
        synchronized(attributes) {
            attributes.put(key, value);
        }
    }
    
	/**
	 * Returns this console's document.
	 * 
	 * @return this console's document
	 */
    public IDocument getDocument() {
        return partitioner.getDocument();
    }

    /*
     *  (non-Javadoc)
     * @see org.eclipse.ui.console.IConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    public IPageBookViewPage createPage(IConsoleView view) {
        return new IOConsolePage(this, view);
    }

    /**
     * Creates and returns a new output stream which may be used to write to this console.
     * A console may be connected to more than one output stream at once. Clients are
     * responsible for closing any output streams created on this console.
     * 
     * @return a new output stream connected to this console
     */
    public IOConsoleOutputStream newOutputStream() {
        IOConsoleOutputStream outputStream = new IOConsoleOutputStream(this);
        openStreams.add(outputStream);
        return outputStream;
    }
    
    /**
     * Returns the input stream connected to the keyboard.
     * 
     * @return the input stream connected to the keyboard.
     */
    public IOConsoleInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns this console's document partitioner.
     * 
     * @return this console's document partitioner
     */
    IDocumentPartitioner getPartitioner() {
        return partitioner;
    }

	/**
	 * Returns the maximum number of characters that the console will display at
	 * once. This is analagous to the size of the text buffer this console
	 * maintains.
	 * 
	 * @return the maximum number of characters that the console will display
	 */
	public int getHighWaterMark() {
	    return partitioner.getHighWaterMark();
	}
	
	/**
	 * Returns the number of characters that will remain in this console
	 * when its high water mark is exceeded.
	 *  
	 * @return the number of characters that will remain in this console
	 *  when its high water mark is exceeded
	 */
	public int getLowWaterMark() {
		return partitioner.getLowWaterMark();
	}
	
	/**
	 * Sets the text buffer size for this console. The high water mark indicates
	 * the maximum number of characters stored in the buffer. The low water mark
	 * indicates the number of characters remaining in the buffer when the high
	 * water mark is exceeded.
	 * 
	 * @param low the number of characters remaining in the buffer when the high
	 *  water mark is exceeded (if -1 the console does not limit output)
	 * @param high the maximum number of characters this console will cache in
	 *  its text buffer (if -1 the console does not limit output)
	 * @exception IllegalArgumentException if low >= high
	 */
	public void setWaterMarks(int low, int high) {
	    if (low >= high) {
	        throw new IllegalArgumentException("High water mark must be greater than low water mark"); //$NON-NLS-1$
	    }
		partitioner.setWaterMarks(low, high);
	}
	
	/**
	 * Sets whether this console scrolls automatically to show the end of text as
	 * output is appened to the console.
	 * 
	 * @param scroll whether this console scrolls automatically
	 */
	public void setAutoScroll(boolean scroll) {
	    if (scroll != autoScroll) {
	        autoScroll = scroll;
	        ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
	            public void run() {
	                firePropertyChange(IOConsole.this, P_AUTO_SCROLL, new Boolean(!autoScroll), new Boolean(autoScroll));
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
	    return autoScroll;
	}
	
	/**
	 * Sets the tab width.
	 * 
	 * @param newTabWidth the tab width 
	 */
    public void setTabWidth(final int newTabWidth) {
        if (tabWidth != newTabWidth) {
            final int oldTabWidth = tabWidth;
            tabWidth = newTabWidth;
            ConsolePlugin.getStandardDisplay().asyncExec(new Runnable() {
                public void run() {
                    firePropertyChange(IOConsole.this, P_TAB_SIZE, new Integer(oldTabWidth), new Integer(tabWidth));           
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
        return tabWidth;
    }

	/**
	 * Returns the font used by this console
	 * 
	 * @return font used by this console
	 */
    public Font getFont() {
        return font;
    }
    
	/**
	 * Sets the font used by this console
	 * 
	 * @param font font
	 */
    public void setFont(Font newFont) {
        if (font == null || !font.equals(newFont)) {
            Font old = font;
            font = newFont;
            firePropertyChange(this, IOConsole.P_FONT, old, font);
        }
    }
    
    /**
     * Check if all streams connected to this console are closed. If so,
     * notifiy the partitioner that this console is finished. 
     */
    private void checkFinished() {
        if (openStreams.isEmpty()) {
            partitioner.streamsClosed();
        }
    }
    
    /**
     * Notification that an output stream connected to this console has been closed.
     * 
     * @param stream stream that closed
     */
    void streamClosed(IOConsoleOutputStream stream) {
    	synchronized (openStreams) {
            openStreams.remove(stream);
            checkFinished();
		}
    }
    
    /**
     * Notification that the input stream connected to this console has been closed.
     * 
     * @param stream stream that closed
     */
    void streamClosed(IOConsoleInputStream stream) {
    	synchronized (openStreams) {
            openStreams.remove(stream);
            checkFinished();
		}
    }
    
    /**
     * Returns the current width of this console. A value of zero of less 
     * indicates this console has no fixed width.
     * 
     * @return the current width of this console
     */
    public int getConsoleWidth() {
        return consoleWidth;
    }
    
    /**
     * Sets the width of this console in characters. Any value greater than zero
     * will cause this console to have a fixed width.
     * 
     * @param width the width to make this console. Values of 0 or less imply
     * the console does not have any fixed width.
     */
    public void setConsoleWidth(int width) {
        if (consoleWidth != width) {
            int old = consoleWidth;
            consoleWidth = width;
            
            firePropertyChange(this, IOConsole.P_CONSOLE_WIDTH, new Integer(old), new Integer(consoleWidth));
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
		IOConsoleHyperlinkPosition hyperlinkPosition = new IOConsoleHyperlinkPosition(hyperlink, offset, length); 
		try {
			getDocument().addPosition(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY, hyperlinkPosition);
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
        return partitioner.getRegion(link);
    }
    
    /**
     * Disposes this console.
     */
    protected void dispose() {
        super.dispose();
        partitioner.disconnect();
        try {
            inputStream.close();
        } catch (IOException ioe) {
        }
        inputStream = null;
        synchronized(attributes) {
            attributes.clear();
        }
    }
    
    /**
     * Adds the given pattern match listener to this console. The listener will
     * be connected and receive match notifications.
     * 
     * @param listener the listener to add
     */
    public void addPatternMatchListener(IPatternMatchListener listener) {
        partitioner.addPatternMatchListener(listener);
    }
    
    /**
     * Removes the given pattern match listener from this console. The listener will be
     * disconnected and will no longer receive match notifications.
     * 
     * @param listener the pattern match listener to remove.
     */
    public void removePatternMatchListener(IPatternMatchListener listener) {
        partitioner.removePatternMatchListener(listener);
    }
    
    /**
     * Returns all hyperlinks in this console.
     * 
     * @return all hyperlinks in this console
     */
    public IHyperlink[] getHyperlinks() {
        try {
            Position[] positions = getDocument().getPositions(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
            IHyperlink[] hyperlinks = new IHyperlink[positions.length];
            for (int i = 0; i < positions.length; i++) {
                IOConsoleHyperlinkPosition position = (IOConsoleHyperlinkPosition) positions[i];
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
            Position[] positions = getDocument().getPositions(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
            for (int i = 0; i < positions.length; i++) {
                IOConsoleHyperlinkPosition position = (IOConsoleHyperlinkPosition) positions[i];
                if (offset >= position.getOffset() && offset <= (position.getOffset() + position.getLength())) {
                    return position.getHyperLink();
                }
            }
        } catch (BadPositionCategoryException e) {
        }        
        return null;
    }
}
