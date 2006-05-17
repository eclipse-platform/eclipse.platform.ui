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

import org.eclipse.swt.graphics.Point;

/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}.
 * <p>
 * It provides
 * <ul>
 * <li>text presentation invalidation enhancements</li>
 * <li>text hover management enhancements</li>
 * <li>a replacement for auto indent strategies</li>
 * <li>support for custom painters</li>
 * </ul>
 *
 * It extends the means for text presentation invalidation by allowing a
 * specific region of the presentation to get invalidated. It replaces
 * {@link org.eclipse.jface.text.ITextViewer#setTextHover(ITextHover, String)}
 * with a new method that allows to specify state masks for a better control of
 * the hover behavior.
 * <p>
 * An {@link org.eclipse.jface.text.IAutoEditStrategy} is a generalization of
 * the original {@link org.eclipse.jface.text.IAutoIndentStrategy}. Auto edit
 * strategies can be arranged in a list that is executed like a pipeline when
 * the viewer content is changed.
 * <p>
 * A {@link org.eclipse.jface.text.IPainter}is creating and managing visual
 * decorations on the viewer's text widget. Viewer's can have an open number of
 * painters. Painters are informed about changes to the viewer content and state
 * and can take the necessary action in responds to the notification.
 *
 * @since 2.1
 */
public interface ITextViewerExtension2 {

	 /**
	  * The state mask of the default hover (value <code>0xff</code>).
	  */
	 final int DEFAULT_HOVER_STATE_MASK= 0xff;

	/**
	 * Invalidates the viewer's text presentation for the given range.
	 *
	 * @param offset the offset of the first character to be redrawn
	 * @param length the length of the range to be redrawn
	 */
	void invalidateTextPresentation(int offset, int length);

	/**
	 * Sets this viewer's text hover for the given content type and the given state mask. If the given text hover
	 * is <code>null</code>, any hover installed for the given content type and state mask is removed.
	 *
	 * @param textViewerHover the new hover or <code>null</code>
	 * @param contentType the type for which the hover is to be registered or unregistered
	 * @param stateMask the SWT event state mask; <code>DEFAULT_HOVER_STATE_MASK</code> indicates that
	 * 			the hover is installed as the default hover.
	 */
	void setTextHover(ITextHover textViewerHover, String contentType, int stateMask);

	/**
	 * Removes all text hovers for the given content type independent from their state mask.
	 * <p>
	 * Note: To remove a hover for a given content type and state mask
	 * use {@link #setTextHover(ITextHover, String, int)} with <code>null</code>
	 * as parameter for the text hover.
	 * </p>
	 * @param contentType the type for which all text hovers are to be unregistered
	 */
	void removeTextHovers(String contentType);

	/**
	 * Returns the currently displayed text hover if any, <code>null</code> otherwise.
	 *
	 * @return the currently displayed text hover or <code>null</code>
	 */
	ITextHover getCurrentTextHover();

	/**
	 * Returns the location at which the most recent mouse hover event
	 * has occurred.
	 *
	 * @return the location of the most recent mouse hover event
	 */
	Point getHoverEventLocation();

	/**
	 * Prepends the given auto edit strategy to the existing list of strategies
	 * for the specified content type. The strategies are called in the order in
	 * which they appear in the list of strategies.
	 *
	 * @param strategy the auto edit strategy
	 * @param contentType the content type
	 */
	void prependAutoEditStrategy(IAutoEditStrategy strategy, String contentType);

	/**
	 * Removes the first occurrence of the given auto edit strategy in the list of strategies
	 * registered under the specified content type.
	 *
	 * @param strategy the auto edit strategy
	 * @param contentType the content type
	 */
	void removeAutoEditStrategy(IAutoEditStrategy strategy, String contentType);

	/**
	 * Adds the given painter to this viewer.
	 *
	 * @param painter the painter to be added
	 */
	void addPainter(IPainter painter);

	/**
	 * Removes the given painter from this viewer. If the painter has not been
	 * added to this viewer, this call is without effect.
	 *
	 * @param painter the painter to be removed
	 */
	void removePainter(IPainter painter);
}
