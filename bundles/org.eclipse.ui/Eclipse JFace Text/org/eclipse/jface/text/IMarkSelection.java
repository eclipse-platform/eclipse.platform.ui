package org.eclipse.jface.text;

import org.eclipse.jface.viewers.ISelection;

/**
 * A mark selection.
 */
public interface IMarkSelection extends ISelection {

	/**
	 * Returns the marked document.
	 */
	IDocument getDocument();
	
	/**
	 * The mark position. The offset may be <code>-1</code> if there's no marked region.
	 */
	int getOffset();
	
	/**
	 * The length of the mark selection. The length may be negative, if the caret
	 * is before the mark position. The length has no meaning if getOffset() returns <code>-1</code>.
	 */
	int getLength();

}
