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
package org.eclipse.debug.internal.ui.console;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.part.IPageBookViewPage;

/**
 * A console that displays messages.
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
	public static final int PROP_FONT = 100;
	
	/**
	 * Property constant indicating the color of a stream has changed. 
	 */
	public static final int PROP_STREAM_COLOR = 101;	
	
	
	// document partitioner
	private MessageConsolePartitioner fPartitioner = null;
		
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

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#createPage(org.eclipse.debug.internal.ui.console.IConsoleView)
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
		fFont = font;
		firePropertyChange(PROP_FONT);
	}
	
	/**
	 * Returns the font for this console
	 * 
	 * @return font for this console
	 */
	public Font getFont() {
		if (fFont == null) {
			return JFaceResources.getTextFont();
		} else {
			return fFont;
		}
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
	protected synchronized void appendToDocument(final String text, final MessageConsoleStream stream) {
		fPartitioner.appendToDocument(text, stream);
	}

	/**
	 * Returns the document this console writes to.
	 * 
	 * @return the document this console wites to
	 */
	protected IDocument getDocument() {
		return fPartitioner.getDocument();
	}
	
}
 