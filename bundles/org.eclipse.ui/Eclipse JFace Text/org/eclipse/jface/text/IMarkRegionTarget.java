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
