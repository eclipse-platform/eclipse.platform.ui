package org.eclipse.jface.viewers;


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
	 * Calculate decorations based on element. 
	 * @param element
	 * @param decoration
	 */
	public void decorate(Object element, IDecoration decoration);

}
