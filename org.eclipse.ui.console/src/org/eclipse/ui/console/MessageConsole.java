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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.internal.console.MessageConsolePage;
import org.eclipse.ui.internal.console.MessageConsolePartitioner;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays messages. A message console may have one or
 * more streams connected to it (<code>MessageConsoleStream</code>).
 * Text written to streams is buffered and processed in a background
 * thread.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.0
 */
public class MessageConsole extends AbstractConsole {
	
	/** 
	 * The font used by this console
	 */
	private Font fFont = null;
	
	/**
	 * Property constant indicating the font of this console has changed. 
	 */
	public static final String P_FONT = ConsolePlugin.getUniqueIdentifier() + ".P_FONT"; //$NON-NLS-1$
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final String P_STREAM_COLOR = ConsolePlugin.getUniqueIdentifier()  + ".P_STREAM_COLOR";	 //$NON-NLS-1$
	
	/**
	 * Property constant indicating tab size has changed 
	 */
	public static final String P_TAB_SIZE = ConsolePlugin.getUniqueIdentifier()  + ".P_TAB_SIZE";	 //$NON-NLS-1$
	
	/**
	 * The default tab size
	 */
	public static final int DEFAULT_TAB_SIZE = 8;
	
	// document partitioner
	private MessageConsolePartitioner fPartitioner = null;

	// current tab size
	private int tabWidth = DEFAULT_TAB_SIZE;


	/** 
	 * Constructs a new message console.
	 * 
	 * @param name console name
	 * @param imageDescriptor console image descriptor or <code>null</code>
	 *   if none
	 */
	public MessageConsole(String name, ImageDescriptor imageDescriptor) {
		super(name, imageDescriptor);
		fPartitioner = new MessageConsolePartitioner();
	}
	
	/**
	 * Returns the maximum number of characters that the console will display at
	 * once. This is analagous to the size of the text buffer this console
	 * maintains.
	 * 
	 * @return the maximum number of characters that the console will display
	 */
	public int getHighWaterMark() {
		return fPartitioner.getHighWaterMark();
	}

	/**
	 * Sets the text buffer size for this console. The high water mark indicates
	 * the maximum number of characters stored in the buffer. The low water mark
	 * indicates the number of characters remaining in the buffer when the high
	 * water mark is exceeded.
	 * 
	 * @param low the number of characters remaining in the buffer when the high
	 *  water mark is exceeded
	 * @param high the maximum number of characters this console will cache in
	 *  its text buffer
	 * @exception IllegalArgumentException if low >= high
	 */
	public void setWaterMarks(int low, int high) {
		fPartitioner.setWaterMarks(low, high);
	}
		
	/**
	 * Returns the number of characters that will remain in this console
	 * when its high water mark is exceeded.
	 *  
	 * @return the number of characters that will remain in this console
	 *  when its high water mark is exceeded
	 */
	public int getLowWaterMark() {
		return fPartitioner.getLowWaterMark();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.AbstractConsole#dispose()
	 */
	protected void dispose() {
		fPartitioner.disconnect();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.console.IConsole#createPage(org.eclipse.ui.console.IConsoleView)
	 */
	public IPageBookViewPage createPage(IConsoleView view) {
		return new MessageConsolePage(view, this);
	}
	
	/**
	 * Sets the font used by this console
	 * 
	 * @param font font
	 */
	public void setFont(Font font) {
		Font old = fFont;
		fFont = font;
		firePropertyChange(this, P_FONT, old, font);
	}
	
	/**
	 * Returns the font for this console
	 * 
	 * @return font for this console
	 */
	public Font getFont() {
		if (fFont == null) {
			return JFaceResources.getTextFont();
		} 
		return fFont;
	}
	
	/**
	 * Returns a new message stream connected to this console.
	 * 
	 * @return a new message stream connected to this console
	 */
	public MessageConsoleStream newMessageStream() {
		return new MessageConsoleStream(this);
	}
	
	/** 
	 * Appends the given message to this console, from the specified stream.
	 * 
	 * @param text message
	 * @param stream stream the message belongs to
	 */
	protected synchronized void appendToDocument(String text, MessageConsoleStream stream) {
		fPartitioner.appendToDocument(text, stream);
	}

	/**
	 * Returns the document this console writes to.
	 * 
	 * @return the document this console wites to
	 */
	public IDocument getDocument() {
		return fPartitioner.getDocument();
	}
	
	/**
	 * Sets the tab width.
	 * @param tabSize The tab width 
	 */
	public void setTabWidth(int tabWidth) {
		int old = this.tabWidth;
		this.tabWidth = tabWidth;
		firePropertyChange(this, P_TAB_SIZE, new Integer(old), new Integer(tabWidth));
	}
	
	/**
	 * Returns the tab width.
	 * @return tab width
	 */
	public int getTabWidth() {
		return tabWidth;
	}
}
