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
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.console.IOConsoleHyperlinkPosition;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.internal.console.IOConsolePartitioner;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays messages and accepts input from users.
 * The console may have multiple output streams connected to it and
 * provides one input stream.
 * Mechanisms are also provided for matching of the document
 * to regular expressions and adding hyperlinks to the document.
 * 
 * @since 3.1
 *
 */
public class IOConsole extends AbstractConsole implements IDocumentListener {
    	
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
	 * Property constant indicating that output queued on a finished console has been processed.
	 */
	public static final String P_CONSOLE_OUTPUT_COMPLETE = ConsolePlugin.getUniqueIdentifier() + "IOConsole.P_CONSOLE_OUTPUT_COMPLETE"; //$NON-NLS-1$
	
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
    
    
    private ArrayList patterns = new ArrayList();
    
    /**
     * A identifier for the console type (may be null). 
     */
    private String type;
    
    private HashMap attributes = new HashMap();
   
    private boolean autoScroll = true;
    
    private boolean partitionerFinished = false;
    
    private MatchJob matchJob = new MatchJob();

    public IOConsole(String title, String consoleType, ImageDescriptor imageDescriptor) {
        super(title, imageDescriptor);
        type = consoleType;
        inputStream = new IOConsoleInputStream(this);
        partitioner = new IOConsolePartitioner(inputStream, this);
        Document document = new Document();
        document.addPositionCategory(IOConsoleHyperlinkPosition.HYPER_LINK_CATEGORY);
        partitioner.connect(document);
        document.addDocumentListener(this);
    }
    
    public IOConsole(String title, ImageDescriptor imageDescriptor) {
        this(title, null, imageDescriptor);
    }

    
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
    }
    
    
    /**
     * @return Returns the attribute matching the specified key.
     */
    public Object getAttribute(String key) {
        return attributes.get(key);
    }
    /**
     * @param key The key used to store the attribute
     * @param value The attribute to set.
     */
    public void setAttribute(String key, Object value) {
        synchronized(attributes) {
            attributes.put(key, value);
        }
    }
    
	/**
	 * Returns the document this console writes to.
	 * 
	 * @return the document this console wites to
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
     * Creates a new IOConsoleOutputStream which may be used to write to the console.
     * A console may be connected to more than one OutputStream at once.
     * 
     * @return A new output stream connected to this console
     */
    public IOConsoleOutputStream newOutputStream() {
        return new IOConsoleOutputStream(this);
    }
    
    /**
     * Returns an IOConsoleInputStream which may be used to read a users input.
     * Every console has one input stream only.
     * 
     * @return The input stream connected to this console.
     */
    public IOConsoleInputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the consoles document partitioner.
     * @return The console's document partitioner
     */
    public IDocumentPartitioner getPartitioner() {
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
	
	public boolean getAutoScroll() {
	    return autoScroll;
	}
	
	/**
	 * Sets the tab width.
	 * @param tabSize The tab width 
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
	 * @return tab width
	 */
    public int getTabWidth() {
        return tabWidth;
    }

	/**
	 * Returns the font for this console
	 * 
	 * @return font for this console
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
     * Should be called to inform the console that output has been completed.
     */
    public void setFinished() {
        partitioner.consoleFinished();
    }
    
    /**
     * Notification from the document partitioner that all pending partitions have been 
     * processed and the console's lifecycle is complete.
     */
    public void partitionerFinished() {
        partitionerFinished = true;
    }
    
    /**
     * Returns the current width of this console. A value of zero of less 
     * implies the console has no fixed width.
     * 
     * @return The current width of this console
     */
    public int getConsoleWidth() {
        return consoleWidth;
    }
    
    /**
     * Sets the width of this console. Any value greater than zero will cause
     * this console to have a fixed width.
     * @param width The width to make this console. Values of 0 or less imply
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
     * Adds an IConsoleHyperlink to the document.
     * @param hyperlink The hyperlink to add.
     * @param offset The offset in the document at which the hyperlink should be added.
     * @param length The length of the text which should be hyperlinked.
     * @throws BadLocationException Thrown if the specified location is not valid.
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
     * @param link
     * @return
     */
    public IRegion getRegion(IHyperlink link) {
        return partitioner.getRegion(link);
    }
    
    /**
     * disposes of this console.
     */
    protected void dispose() {
        partitioner.disconnect();
        try {
            inputStream.close();
            //FIXME: should close output streams? Need to store references.
        } catch (IOException ioe) {
        }
        inputStream = null;
        synchronized (patterns) {
            Iterator iterator = patterns.iterator();
            while (iterator.hasNext()) {
                CompiledPatternMatchListener notifier = (CompiledPatternMatchListener) iterator.next();
                notifier.dispose();
            }
            patterns.clear();
        }
        synchronized(attributes) {
            attributes.clear();
        }
        matchJob = null;
    }
    
    /**
     * Adds a matchHandler to match a pattern to the document's content.
     * @param matchListener The IPatternMatchHandler to add.
     */
    public void addPatternMatchListener(IPatternMatchListener matchListener) {
        synchronized(patterns) {
            if (matchListener == null || matchListener.getPattern() == null) {
                throw new IllegalArgumentException("Pattern cannot be null"); //$NON-NLS-1$
            }
            
            Pattern pattern = Pattern.compile(matchListener.getPattern(), matchListener.getCompilerFlags());
            String qualifier = matchListener.getLineQualifier();
            Pattern qPattern = null;
            if (qualifier != null) {
            	qPattern = Pattern.compile(qualifier, matchListener.getCompilerFlags());
            }
            CompiledPatternMatchListener notifier = new CompiledPatternMatchListener(pattern, qPattern, matchListener);
            patterns.add(notifier);
            matchListener.connect(this);
            matchJob.schedule(100);
        }
    }
    
    /**
     * Removes an IPatternMatchHandler so that its pattern will no longer get matched to the document.
     * @param matchHandler The IPatternMatchHandler to remove.
     */
    public void removePatternMatchListener(IPatternMatchListener matchListener) {
        synchronized(patterns){
            for (Iterator iter = patterns.iterator(); iter.hasNext();) {
                CompiledPatternMatchListener element = (CompiledPatternMatchListener) iter.next();
                if (element.listener == matchListener) {
                    iter.remove();
                    matchListener.disconnect();
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentAboutToBeChanged(DocumentEvent event) {
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
     */
    public void documentChanged(DocumentEvent event) {
    	if (event.getOffset() == 0 && event.getLength() == 0) {
    		// buffer has been emptied, reset match listeners
    		synchronized (patterns) {
    			Iterator iter = patterns.iterator();
    			while (iter.hasNext()) {
    				CompiledPatternMatchListener notifier = (CompiledPatternMatchListener)iter.next();
    				notifier.end = 0;
    			}
    		}
    	}
    	matchJob.schedule(50);
    }
    
    private class CompiledPatternMatchListener {
        Pattern pattern;
        Pattern qualifier;
        IPatternMatchListener listener;
        int end = 0;
        
        CompiledPatternMatchListener(Pattern pattern, Pattern qualifier, IPatternMatchListener matchListener) {
            this.pattern = pattern;
            this.listener = matchListener;
            this.qualifier = qualifier;
        }
        
        public void dispose() {
            listener.disconnect();
            pattern = null;
            qualifier = null;
            listener = null;
        }
    }
    
    private class MatchJob extends Job {
        
        MatchJob() {
            super("Match Job"); //$NON-NLS-1$
            setSystem(true);
        }
                
        protected IStatus run(IProgressMonitor monitor) {
        	IDocument doc = getDocument();
        	String text = null;
        	int prevBaseOffset = -1;
        	if (doc != null) {
        	    boolean allDone = true;
	        	int docLength = doc.getLength();
	        	for (int i = 0; i < patterns.size(); i++) {
	        		CompiledPatternMatchListener notifier = (CompiledPatternMatchListener) patterns.get(i);
	        		int baseOffset = notifier.end;
					int length = docLength - baseOffset;
					if (length > 0) {
						try {
							if (prevBaseOffset != baseOffset) {
								// reuse the text string if possible
								text = doc.get(baseOffset, length);
							}
							Matcher reg = notifier.pattern.matcher(text);
							Matcher quick = null;
							if (notifier.qualifier != null) {
								quick = notifier.qualifier.matcher(text);
							}
							int start = 0;
							while (start < length) {
								if (quick != null) {
									if (quick.find(start)) {
										int line = doc.getLineOfOffset(baseOffset + quick.start());
										start = doc.getLineOffset(line) - notifier.end;
									} else {
										start = length;
									}
								}
								if (start < length) {
									if (reg.find(start)) {
										start = reg.end();
										int regStart = reg.start();
										notifier.listener.matchFound(new PatternMatchEvent(IOConsole.this, baseOffset + regStart, start - regStart));
									} else {
										start = length;
									}
								}
								notifier.end = baseOffset + start;
							}
		        		} catch (BadLocationException e) {
		        			// TODO:
		            		e.printStackTrace();
		            	}
					}
					prevBaseOffset = baseOffset;
					allDone = allDone && (notifier.end >= doc.getLength()); 
		        }
	        	if (allDone && partitionerFinished) {
	        	    firePropertyChange(this, IOConsole.P_CONSOLE_OUTPUT_COMPLETE, null, null);
	        	}
        	}
            return Status.OK_STATUS;
        } 

    }
}
