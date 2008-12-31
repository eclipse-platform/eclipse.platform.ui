/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 * Text listeners registered with a text viewer are informed about all
 * modifications of an {@link org.eclipse.jface.text.ITextViewer} by means of
 * text events. A text event describes a change as a replace operation.
 * <p>
 * The changes described in the event are the changes applied to the text
 * viewer's widget (i.e., its visual representation) and not those applied to the
 * text viewer's document. The text event can be asked to return the
 * corresponding document event. If the text event does not contain a document
 * event, the modification of the text viewer is a presentation change. For
 * example, changing the visible region of a text viewer, is a presentation
 * change. A completely empty text event represents a change of the viewer's
 * redraw state.</p>
 * <p>
 * If a text listener receives a text event, it is guaranteed that both the
 * document and the viewer's visual representation are synchronized.</p>
 * <p>
 * Clients may implement this interface.</p>
 *
 * @see org.eclipse.jface.text.ITextViewer
 * @see org.eclipse.jface.text.TextEvent
 * @see org.eclipse.jface.text.DocumentEvent
 */
public interface ITextListener {

	/**
	 * The visual representation of a text viewer this listener is registered with
	 * has been changed.
	 *
	 * @param event the description of the change
	 */
	void textChanged(TextEvent event);
}
