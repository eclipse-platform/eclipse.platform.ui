package org.eclipse.jface.viewers;

/**
 * The ICombinedLabelDecorator is a label decorator that allows
 * decoration of the image and text at the same time.
 */

public interface ICombinedLabelDecorator extends IBaseLabelProvider{

	/**
	 * Decorates the label for an element by modifying the text and/or 
	 * the image in the given <code>CombinedLabel</code>.
	 * Normally the decorations provide additional information relating to the state
	 * of the given element.
	 * If no decoration is provided, the label should not be modified.
	 *
	 * @param element the element whose label is being decorated
	 * @param label the label to decorate
	 */
	public void decorateLabel(Object element, CombinedLabel label);


}