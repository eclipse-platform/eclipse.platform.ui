package org.eclipse.jface.viewers;

/**
 * The IBatchLabelDecorator is a label decorator that allows
 * decoration of the image and text at the same time.
 */

public interface IBatchLabelDecorator extends ILabelDecorator {

	/**
	* Finds a String and an Image that is based on the given image,
	* but decorated with additional information relating to the state
	* of the provided element.
	*
	* @param element the element whose image is being decorated
	* @param DecorationResult, the result this is being applied to.
	*/
	public void decorateTextAndImage(Object element, Decoration result);


}