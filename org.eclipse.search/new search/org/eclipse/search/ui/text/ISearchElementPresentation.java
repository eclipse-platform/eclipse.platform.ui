package org.eclipse.search.ui.text;

import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionGroup;

/**
 * Presentation of elements in a search result. Clients 
 * must implement this interface.
 * This API is preliminary and subject to change at any time.
 * @since 3.0
 */
public interface ISearchElementPresentation {
	/**
	 * Returns the image for the label of the given element.  The image
	 * is owned by the presentation and must not be disposed directly.
	 * Instead, dispose the presentation when no longer needed.
	 *
	 * @param element the element for which to provide the label image
	 * @return the image used to label the element, or <code>null</code>
	 *   if there is no image for the given object
	 */
	public Image getImage(Object element);
	/**
	 * Returns the text for the label of the given element.
	 *
	 * @param element the element for which to provide the label text
	 * @return the text string used to label the element, or <code>null</code>
	 *   if there is no text label for the given object
	 */
	public String getText(Object element);
	/**
	 * returns a string representation of attributes to sort search result entries
	 * by. The <code>flat</code>> flag indicates if the sorting is to be done in a 
	 * flat list or in the structure implied by <code>getParent()</code>.
	 * @param flat
	 * @return
	 */
	String[] getSortingAtributes(boolean flat);
	/**
	 * Informs this category that the sort order was switched to the given
	 * attribute names (in order of appearence in the string array). 
	 * Any label providers should be configured accordingly.
	 * @param flat
	 * @return
	 */
	void setSortOrder(String[] attributeNames, boolean flat);
	/**
	 * Get the attribute <code>attributeName</code> represented as a String.
	 * The String will be used for sorting.
	 * @param flat
	 * @return
	 */
	String getAttribute(Object element, String attributeName);
	/**
	 * Show the given element in an editor and select the given position.
	 * @return
	 */
	void showMatch(Object element, int start, int length) throws PartInitException;
	/**
	 * Returns an action group that should be used to shown
	 * in the context menu, toolbar, etc. for this 
	 * ISearchElementPresentation.
	 * @return The action group.
	 */
	ActionGroup getActionGroup();
	/**
	 * Release all resources held by this category.
	 */
	void dispose();
}
