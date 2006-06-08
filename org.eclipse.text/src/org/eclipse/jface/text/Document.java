/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
 * Default document implementation. Uses a {@link org.eclipse.jface.text.GapTextStore} wrapped
 * inside a {@link org.eclipse.jface.text.CopyOnWriteTextStore} as text store.
 * <p>
 * The used line tracker considers the following strings as line delimiters: "\n", "\r", "\r\n".
 * </p>
 * <p>
 * The document is ready to use. It has a default position category for which a default position
 * updater is installed.
 * </p>
 * <p>
 * <strong>Performance:</strong> The implementation should perform reasonably well for typical
 * source code documents. It is not designed for very large documents of a size of several
 * megabytes. Space-saving implementations are initially used for both the text store and the line
 * tracker; the first modification after a {@link #set(String) set} incurs the cost to transform the
 * document structures to efficiently handle updates.
 * </p>
 * See {@link GapTextStore} and {@link TreeLineTracker} for algorithmic behavior of the used
 * document structures.
 * <p>
 * </p>
 * 
 * @see org.eclipse.jface.text.GapTextStore
 * @see org.eclipse.jface.text.CopyOnWriteTextStore
 */
public class Document extends AbstractDocument {
	/**
	 * Creates a new empty document.
	 */
	public Document() {
		super();
		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
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
		setTextStore(new CopyOnWriteTextStore(new GapTextStore()));
		setLineTracker(new DefaultLineTracker());
		getStore().set(initialContent);
		getTracker().set(initialContent);
		completeInitialization();
	}
}
