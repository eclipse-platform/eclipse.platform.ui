/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.jface.text;


/**
 * Default document implementation. Uses a gap text store as text store and
 * installs a line tracker considering the following strings as line delimiters
 * "\n", "\r", "\r\n". The document is ready to use. It has a default position
 * category for which a default position updater is installed.
 *
 * @see GapTextStore
 */
public class Document extends AbstractDocument {
	
	
	/**
	 * Creates a new empty document.
	 */
	public Document() {
		super();
		setTextStore(new GapTextStore(50, 300));
		setLineTracker(new DefaultLineTracker());
		completeInitialization();
	}
	
	/**
	 * Creates a new document with the given initial content.
	 *
	 * @param initialContent the document's initial content
	 */
	public Document(String initialContent) {
		super();
		setTextStore(new GapTextStore(50, 300));
		setLineTracker(new DefaultLineTracker());	
		getStore().set(initialContent);
		getTracker().set(initialContent);
		completeInitialization();
	}
	
	/*
	 * @see IDocumentExtension#startSequentialRewrite(boolean)
	 * @since 2.0
	 */
	public void startSequentialRewrite(boolean normalized) {
		ITextStore store= new SequentialRewriteTextStore(getStore());
		setTextStore(store);
	}
	
	/*
	 * @see IDocumentExtension#stopSequentialRewrite()
	 * @since 2.0
	 */
	public void stopSequentialRewrite() {
		if (getStore() instanceof SequentialRewriteTextStore) {
			SequentialRewriteTextStore srws= (SequentialRewriteTextStore) getStore();
			ITextStore source= srws.getSourceStore();
			setTextStore(source);
			srws.dispose();
		}
	}
}
