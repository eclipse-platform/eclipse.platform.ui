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

package org.eclipse.jface.text;


import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.widgets.Control;

 
/**
 * Extension interface for <code>ITextViewer</code>. Extends <code>ITextViewer</code> with
 * <ul>
 * <li> a replacement of the event consumer mechanism (methods dealing with <code>VerifyKeyListener</code>)
 * <li> access to the control of this viewer
 * <li> marked region support a la emacs
 * <li> control of the viewer's redraw behavior (@see #setRedraw)
 * <li> access to the viewer's rewrite target
 * </ul>
 * 
 * @since 2.0
 */
public interface ITextViewerExtension {
	 
	/**
	 * Inserts the verify key listener at the beginning of the viewer's 
	 * list of verify key listeners.  If the listener is already registered 
	 * with the viewer this call moves the listener to the beginnng of
	 * the list.
	 *
	 * @param listener the listener to be inserted
	 */
	void prependVerifyKeyListener(VerifyKeyListener listener);
	
	/**
	 * Appends a verify key listener to the viewer's list of verify
	 * key listeners. If the listener is already registered with the viewer
	 * this call moves the listener to the end of the list.
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
	 * Sets or clears the mark. If offset is <code>-1</code>, the mark is cleared.
	 * If a mark is set and the selection is empty, cut and copy actions performed on this
	 * text viewer peform on the region limited by the positions of the mark and the cursor.
	 * 
	 * @param offset the offset of the mark
	 */
	void setMark(int offset);

	/**
	 * Returns the mark position, <code>-1</code> if mark is not set.
	 * 
	 * @return the mark position or <code>-1</code> if no mark is set
	 */
	int getMark();
	
	/**
	 * Enables/disables the redrawing of this text viewer.  This temporarily disconnects
	 * the viewer from its underlying StyledText widget. While being disconnected only 
	 * the viewer's selection may be changed using <code>setSelectedRange</code>. 
	 * Any direct manipulation of the widget as well as calls to methods that change the viewer's
	 * presentation state (such as enabling the segmented view) are not allowed.
	 * When redrawing is disabled the viewer does not send out any selection or 
	 * view port change notification. When redrawing is enabled again, a selection 
	 * change notification is sent out for the selected range and this range is revealed.
	 * 
	 * @param redraw <code>true</code> to enable redrawing, <code>false</code> otherwise
	 */
	void setRedraw(boolean redraw);
	
	/**
	 * Returns the viewer's rewrite target.
	 * 
	 * @return the viewer's rewrite target
	 */
	IRewriteTarget getRewriteTarget();
}
