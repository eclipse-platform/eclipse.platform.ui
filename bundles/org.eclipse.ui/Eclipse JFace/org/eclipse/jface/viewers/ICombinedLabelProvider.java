package org.eclipse.jface.viewers;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
/**
* Extends <code>IBaseLabelProvider</code> with the methods
* to provide the text and image for the label of a given element. 
*/
public interface ICombinedLabelProvider extends IBaseLabelProvider {

	/**
	 * Returns the CombinedLabel result for the the given element.  The image
	 * and text is owned by the label provider and must not be disposed directly.
	 * Instead, dispose the label provider when no longer needed.
	 *
	 * @param element the element for which to provide the label
	 * @return the CombinedLabel used to label the element. If no decoration
	 * is applied the CombinedLabel will contain the original undecorated
	 * text and image.
	 */
	public CombinedLabel getCombinedLabel(Object element);

}