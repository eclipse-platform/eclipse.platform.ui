package org.eclipse.jface.text;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.widgets.Control;

 
/**
 * A text viewer  extension is for extending
 * <code>ITextViewer</code> instances with new functionality.
 */
public interface ITextViewerExtension {

	/*
	 * All subsequent methods dealing with verify key listeners
	 * are indended to allow clients a fine grained management of
	 * key event consumption. Will be merged/ will replace
	 * the event consumer mechanism available on ITextViewer.
	 */
	 
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
	 * If a mark is set and the selection is empty, cut and copy actions performed on the
	 * text viewer peform on the region limited by the positions of the mark and the cursor.
	 */
	void setMark(int offset);

	/**
	 * Returns the mark position, <code>-1</code> if mark is not set.
	 */
	int getMark();

}
