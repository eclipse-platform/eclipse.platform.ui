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


import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.widgets.Control;


/**
 * Extension interface for {@link org.eclipse.jface.text.ITextViewer}.
 * <p>
 * This extension interface replaces the event consumer mechanism (
 * {@link org.eclipse.jface.text.ITextViewer#setEventConsumer(IEventConsumer)})
 * with a set of methods that allow to manage a sequence of
 * {@link org.eclipse.swt.custom.VerifyKeyListener}objects. It also adds
 * <ul>
 * <li>access to the control of this viewer</li>
 * <li>marked region support as in emacs</li>
 * <li>control of the viewer's redraw behavior by introducing
 *     <code>setRedraw(boolean)</code>
 * <li>access to the viewer's rewrite target.
 * </ul>
 *
 * A rewrite target ({@link org.eclipse.jface.text.IRewriteTarget}) represents
 * an facade offering the necessary methods to manipulate a document that is the
 * input document of a text viewer.
 *
 * @since 2.0
 */
public interface ITextViewerExtension {

	/**
	 * Inserts the verify key listener at the beginning of the viewer's list of
	 * verify key listeners. If the listener is already registered with the
	 * viewer this call moves the listener to the beginning of the list.
	 *
	 * @param listener the listener to be inserted
	 */
	void prependVerifyKeyListener(VerifyKeyListener listener);

	/**
	 * Appends a verify key listener to the viewer's list of verify key
	 * listeners. If the listener is already registered with the viewer this
	 * call moves the listener to the end of the list.
	 *
	 * @param listener the listener to be added
	 */
	void appendVerifyKeyListener(VerifyKeyListener listener);

	/**
	 * Removes the verify key listener from the viewer's list of verify key listeners.
	 * If the listener is not registered with this viewer, this call has no effect.
	 *
	 * @param listener the listener to be removed
	 */
	void removeVerifyKeyListener(VerifyKeyListener listener);

	/**
	 * Returns the control of this viewer.
	 *
	 * @return the control of this viewer
	 */
	Control getControl();

	/**
	 * Sets a mark at the given offset or clears the mark if the specified
	 * offset is <code>-1</code>. If a mark is set and the selection is
	 * empty, cut and copy actions performed on this text viewer work on the
	 * region described by the positions of the mark and the cursor.
	 *
	 * @param offset the offset of the mark
	 */
	void setMark(int offset);

	/**
	 * Returns the position of the mark, <code>-1</code> if the mark is not set.
	 *
	 * @return the position of the mark or <code>-1</code> if no mark is set
	 */
	int getMark();

	/**
	 * Enables/disables the redrawing of this text viewer. This temporarily
	 * disconnects the viewer from its underlying
	 * {@link org.eclipse.swt.custom.StyledText}widget. While being
	 * disconnected only the viewer's selection may be changed using
	 * <code>setSelectedRange</code>. Any direct manipulation of the widget
	 * as well as calls to methods that change the viewer's presentation state
	 * (such as enabling the segmented view) are not allowed. When redrawing is
	 * disabled the viewer does not send out any selection or view port change
	 * notification. When redrawing is enabled again, a selection change
	 * notification is sent out for the selected range and this range is
	 * revealed causing a view port changed notification.
	 *
	 * @param redraw <code>true</code> to enable redrawing, <code>false</code>
	 *            otherwise
	 */
	void setRedraw(boolean redraw);

	/**
	 * Returns the viewer's rewrite target.
	 *
	 * @return the viewer's rewrite target
	 */
	IRewriteTarget getRewriteTarget();
}
