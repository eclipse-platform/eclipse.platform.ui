package org.eclipse.jface.viewers;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * IDecoration is the interface that defines the result of 
 * a decoration of an Object.
 * */

public interface IDecoration {

	/**
	 * Add a prefix to the result.
	 * @param prefixString
	 */
	public void addPrefix(String prefixString);

	/**
	 * Add a suffix to the result.
	 * @param suffixString
	 */
	public void addSuffix(String suffixString);
	
	/**
	 * Add an overlay to the result.
	 * @param overlay
	 */
	public void addOverlay(ImageDescriptor overlay);
}