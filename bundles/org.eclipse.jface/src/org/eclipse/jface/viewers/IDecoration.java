/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;


import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Defines the result of decorating an element. 
 * 
 * This interface is not meant to be implemented and will
 * be provided to instances of <code>ILightweightLabelDecorator</code>.
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
