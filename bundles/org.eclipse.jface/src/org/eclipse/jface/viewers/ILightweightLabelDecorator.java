package org.eclipse.jface.viewers;

import org.eclipse.swt.graphics.Image;

/**
 * The ILightweightLabelDecorator is a decorator that decorates
 * using a prefix, suffix and overlay image rather than doing all 
 * of the image and text management itself like an ILabelDecorator.
 * 
 * An instance of this type is created from the decoratorClass specification
 * of org.eclipse.ui.decorators.
 */
public interface ILightweightLabelDecorator extends IBaseLabelProvider {
	/**
	* Get the overlayImage for element.	
	* @param element the element whose image is being decorated
	* @return the overlay image, or <code>null</code> if no decoration is to be applied
	*
	* @see org.eclipse.jface.resource.CompositeImageDescriptor
	*/
	public Image getOverlay(Object element);

	/**
	 * Returns the prefix to be used for decorating the prefix of
	 * an element.
	 *
	 * @param element the element whose image is being decorated
	 * @return the decorated text label, or <code>null</code> if no decoration is to be applied
	 */
	public String getPrefix(Object element);

	/**
	 * Returns the suffix to be used for decorating the prefix of
	 * an element.
	 *
	 * @param element the element whose image is being decorated
	 * @return the decorated text label, or <code>null</code> if no decoration is to be applied
	 */
	public String getSuffix(Object element);

}
