/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.runtime;

import java.net.URL;

public interface IProduct {
	
	/**
	 * Returns the applicatoin associated with this product.  This information is used 
	 * to guide the runtime as to what application extension to create and execute.
	 * @return this product's application or null if none
	 */
	public String getApplication();
	/**
	 * Returns the name of this product.  The name is typcially used in the title
	 * bar of UI windows.
	 * @return the name of this product or null if none
	 */
	public String getName();
	/**
	 * Returns the text desciption of this product
	 * @return the description of this product or null if none
	 */
	public String getDescription();
	/**
	 * Returns the URL for this product's image.  The image is used in the about dialog
	 * and should be either full-sized (no larger than 500x330 pixels) or half-sized 
	 * (no larger than 250x330 pixels).  
	 * @return the image for this product or null if none
	 */
	public URL getImage();
	/**
	 * Returns the id of the welcome extension to use for this product
	 * @return the id of this product's welcome page or null if none
	 */
	public String getDefaultPerspective();
	
	/** Returns the unique product id of this product.
	 * @return the id of this product
	 */
	public String getId();
	
}
