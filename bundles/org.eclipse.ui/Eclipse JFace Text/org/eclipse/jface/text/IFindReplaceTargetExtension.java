package org.eclipse.jface.text;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */


/**
 * For internal use only. Not API. <p>
 * A find replace target extension is for extending
 * <code>IFindReplaceTarget</code> instances with new functionality.
 */
public interface IFindReplaceTargetExtension {
	
	/**
	 * Indicates that a session with the target begins.
	 * All calls except <code>beginSession()</code> and <code>endSession()</code> to
	 * <code>IFindReplaceTarget</code> and
	 * <code>IFindReplaceTargetExtension</code> must be embedded within calls to
	 * <code>beginSession()</code> and <code>endSession()</code>.
	 * 
	 * @see endSession()
	 */
	void beginSession();

	/**
	 * Indicates that a session with the target ends.
	 * 
	 * @see beginSession()
	 */
	void endSession();

	/**
	 * Returns the find scope of the target, <code>null</code> for global scope.
	 * 
	 * @return returns the find scope of the target, may be <code>null</code>
	 */
	IRegion getScope();
	
	/**
	 * Sets the find scope of the target to operate on. <code>null</code>
	 * indicates that the global scope should be used.
	 * 
	 * @param scope the find scope of the target, may be <code>null</code>
	 */	
	void setScope(IRegion scope);
	
	/**
	 * Returns the currently selected range of lines as a offset and length.
	 *
	 * @return the currently selected line range
	 */
	Point getLineSelection();
	
	/**
	 * Sets a selection.
	 * 
	 * @param offset the offset of the selection
	 * @param length the length of the selection
	 */
	void setSelection(int offset, int length);
	
	/**
	 * Sets the scope highlight color
	 * 
	 * @param color the color of the scope highlight
	 */
	void setScopeHighlightColor(Color color);
	
}
