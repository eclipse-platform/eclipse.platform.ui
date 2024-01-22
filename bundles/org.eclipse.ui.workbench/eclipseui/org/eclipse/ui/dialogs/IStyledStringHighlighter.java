package org.eclipse.ui.dialogs;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;

/**
 * Used in {@link FilteredItemsSelectionDialog} to highlight matches in the list
 * of matching items
 *
 * @since 3.115
 */
public interface IStyledStringHighlighter {

	/**
	 * Used to highlight matches
	 *
	 * @param text    The text in which matches should be highlighted
	 * @param pattern The pattern that defines what to highlight
	 * @param styler  The styler used to highlight the matches
	 * @return A {@link StyledString} representation of <code>text</code>
	 */
	public StyledString highlight(String text, String pattern, Styler styler);

}