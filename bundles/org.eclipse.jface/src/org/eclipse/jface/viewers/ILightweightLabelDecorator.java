package org.eclipse.jface.viewers;


/**
 * The <code>ILightweightLabelDecorator</code> is a decorator that decorates
 * using a prefix, suffix and overlay image rather than doing all 
 * of the image and text management itself like an <code>ILabelDecorator</code>.
 */
public interface ILightweightLabelDecorator extends IBaseLabelProvider {
	
	/**
	 * Calculates decorations based on element. 
	 * 
	 * @param element the element to decorate
	 * @param decoration the decoration to set
	 */
	public void decorate(Object element, IDecoration decoration);

}
