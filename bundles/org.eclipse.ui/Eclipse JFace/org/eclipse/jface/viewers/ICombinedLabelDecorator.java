package org.eclipse.jface.viewers;

/**
 * The ICombinedLabelDecorator is a label decorator that allows
 * decoration of the image and text at the same time.
 */

public interface ICombinedLabelDecorator extends ILabelDecorator{

	/**
	* Finds a String and an Image that is based on the given image
	* and text in the CombinedLabel but decorated with additional information relating to the state
	* of the provided element.
	*
	* @param element the element whose image and text is being decorated
	* @param DecorationResult, the result this is being applied to.
	*/
	public void decorateLabel(Object element, CombinedLabel result);


}