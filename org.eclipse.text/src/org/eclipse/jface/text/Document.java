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

package org.eclipse.jface.text;


/**
 * Default document implementation. Uses a
 * {@link org.eclipse.jface.text.GapTextStore} wrapped inside a 
 * {@link org.eclipse.jface.text.CopyOnWriteTextStore} as default text store and a
 * {@link org.eclipse.jface.text.SequentialRewriteTextStore} when in sequential
 * rewrite mode.
 * <p>
 * The used line tracker considers the following strings as line delimiters
 * "\n", "\r", "\r\n".
 * <p>
 * The document is ready to use. It has a default position category for which a
 * default position updater is installed.
 *
 * @see org.eclipse.jface.text.GapTextStore
 * @see org.eclipse.jface.text.SequentialRewriteTextStore
 * @see org.eclipse.jface.text.CopyOnWriteTextStore
 */
public class Document extends AbstractDocument {


	/**
	 * Creates a new empty document.
	 */
	public Document() {
		super();
		setTextStore(new CopyOnWriteTextStore(new GapTextStore(50, 300)));
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
		setTextStore(new CopyOnWriteTextStore(new GapTextStore(50, 300)));
		setLineTracker(new DefaultLineTracker());
		getStore().set(initialContent);
		getTracker().set(initialContent);
		completeInitialization();
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentExtension#startSequentialRewrite(boolean)
	 * @since 2.0
	 */
	public void startSequentialRewrite(boolean normalized) {
		ITextStore store= new SequentialRewriteTextStore(getStore());
		setTextStore(store);
	}

	/*
	 * @see org.eclipse.jface.text.IDocumentExtension#stopSequentialRewrite()
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
