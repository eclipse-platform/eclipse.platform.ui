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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Font;

/**
 * A console that displays messages.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 3.0
 */
public class MessageConsole implements IConsole {
	
	// document partitioner
	private MessageConsolePartitioner fPartitioner = null;
	
	// console name
	private String fName = ""; //$NON-NLS-1$
	
	// console image
	private ImageDescriptor fImageDescriptor = null;
	
	// list of registered pages
	private List fPages = new ArrayList();
	
	/** 
	 * Constructs a new message console.
	 */
	public MessageConsole() {
		fPartitioner = new MessageConsolePartitioner();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getName()
	 */
	public String getName() {
		return fName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return fImageDescriptor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.ui.console.IConsole#createPage(org.eclipse.debug.internal.ui.console.IConsoleView)
	 */
	public IConsolePage createPage(IConsoleView view) {
		return new MessageConsolePage(view, this);
	}
	
	/**
	 * Sets the name of this console.
	 * 
	 * @param name name of this console - cannot be <code>null</code>
	 */
	public void setName(String name) {
		fName = name;
		titleChanged();
	}
	
	/**
	 * Notifies pages that the title has changed for this console
	 */
	private void titleChanged() {
		Iterator pages = fPages.iterator();
		while (pages.hasNext()) {
			MessageConsolePage page = (MessageConsolePage) pages.next();
			page.fireTitleChanged();
		}
	}
	
	/**
	 * Notifies pages to refresh
	 */
	protected void refreshPages() {
		Iterator pages = fPages.iterator();
		while (pages.hasNext()) {
			MessageConsolePage page = (MessageConsolePage) pages.next();
			page.refresh();
		}
	}	

	/**
	 * Sets the image for this console.
	 * 
	 * @param imageDescriptor image for this console, or <code>null</code> if none
	 */
	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		fImageDescriptor = imageDescriptor;
		titleChanged();
	}
	
	/**
	 * Sets the font used by this console
	 * 
	 * @param font font
	 */
	public void setFont(Font font) {
		Iterator pages = fPages.iterator();
		while (pages.hasNext()) {
			MessageConsolePage page = (MessageConsolePage) pages.next();
			page.setFont(font);
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
	
	/**
	 * Register the given page, as open on this console.
	 * 
	 * @param page a page opened on this console
	 */
	protected void addPage(MessageConsolePage page) {
		fPages.add(page);
	}
	
	/**
	 * Notification the given pages has been closed.
	 * 
	 * @param page a page that closed for this console
	 */
	protected void removePage(MessageConsolePage page) {
		fPages.remove(page);
	}
}
 