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
package org.eclipse.ui.console;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays messages. A message console may have one or
 * more streams connected to it (<code>MessageConsoleStream</code>).
 * Text written to streams is buffered and processed in a Job by the 
 * console's document partitioner.
 * <p>
 * Clients may instantiate this class; not intended to be subclassed.
 * </p>
 * @since 3.0
 */
public class MessageConsole extends IOConsole {
	
	/**
	 * Property constant indicating the font of this console has changed. 
	 * 
	 * @deprecated use {@link IConsoleConstants#P_FONT} 
	 */
	public static final String P_FONT = IConsoleConstants.P_FONT;
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 * 
	 * @deprecated use {@link IConsoleConstants#P_STREAM_COLOR} 
	 */
	public static final String P_STREAM_COLOR = IConsoleConstants.P_STREAM_COLOR;
	
	/**
	 * Property constant indicating tab size has changed
	 * 
	 * @deprecated use {@link IConsoleConstants#P_TAB_SIZE}
	 */
	public static final String P_TAB_SIZE = IConsoleConstants.P_TAB_SIZE;
	
	/**
	 * The default tab size
	 * 
	 * @deprecated use {@link IConsoleConstants#DEFAULT_TAB_SIZE}
	 */
	public static final int DEFAULT_TAB_SIZE = IConsoleConstants.DEFAULT_TAB_SIZE;	

    /**
     * Consturcts a message console with the given name and image.
     * 
     * @param name console name
     * @param imageDescriptor console image descriptor or <code>null</code>
     */
	public MessageConsole(String name, ImageDescriptor imageDescriptor) {
		this(name, imageDescriptor, true);
	}
	
	/**
	 * Constructs a message console.
	 * 
	 * @param name console name
	 * @param imageDescriptor console image descriptor or <code>null</code>
	 * @param autoLifecycle whether lifecycle methods should be called automatically
	 *  when added and removed from the console manager
	 * @since 3.1
	 */
	public MessageConsole(String name, ImageDescriptor imageDescriptor, boolean autoLifecycle) {
		super(name, IConsoleConstants.MESSAGE_CONSOLE_TYPE, imageDescriptor, autoLifecycle);
	}	
		
	/**
	 * Returns a new message stream connected to this console.
	 * 
	 * @return a new message stream connected to this console
	 */
	public MessageConsoleStream newMessageStream() {
		return new MessageConsoleStream(this);
	}

    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IConsole#createPage(org.eclipse.ui.console.IConsoleView)
     */
    public IPageBookViewPage createPage(IConsoleView view) {
        IOConsolePage page = (IOConsolePage) super.createPage(view);
        page.setReadOnly();
        return page;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.console.IOConsole#getInputStream()
     */
    public IOConsoleInputStream getInputStream() {
        throw new UnsupportedOperationException("Message Console does not support user input"); //$NON-NLS-1$
    }
    
	
	/** 
	 * Appends the given message to this console, from the specified stream.
	 * 
	 * @param text message
	 * @param stream stream the message belongs to
	 * @deprecated since 3.1, this method should no longer be called, and has no effect.
	 * Writing to a message console stream updates the document
	 */
	protected void appendToDocument(String text, MessageConsoleStream stream) {
	}    
}
