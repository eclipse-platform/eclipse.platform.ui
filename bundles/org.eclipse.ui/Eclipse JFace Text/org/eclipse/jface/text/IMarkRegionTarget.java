/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.jface.text;

/**
 * A mark region target to support marked regions as found in emacs.
 */
public interface IMarkRegionTarget {

	/**
	 * Sets or clears a mark at the current cursor position.
	 * 
	 * @param set sets the mark if <code>true</code>, clears otherwise.
	 */
	void setMarkAtCursor(boolean set);
	
	/**
	 * Swaps the mark and cursor position if cursor is in visible region.
	 */
	void swapMarkAndCursor();

}
