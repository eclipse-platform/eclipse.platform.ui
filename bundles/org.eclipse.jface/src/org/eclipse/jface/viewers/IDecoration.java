package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Defines the result of decorating an element.
 */
public interface IDecoration {

	/**
	 * Adds a prefix to the element's label.
	 * 
	 * @param prefix the prefix
	 */
	public void addPrefix(String prefix);

	/**
	 * Adds a suffix to the element's label.
	 * 
	 * @param suffix the suffix
	 */
	public void addSuffix(String suffix);
	
	/**
	 * Adds an overlay to the element's image.
	 * 
	 * @param overlay the overlay image descriptor
	 */
	public void addOverlay(ImageDescriptor overlay);
}