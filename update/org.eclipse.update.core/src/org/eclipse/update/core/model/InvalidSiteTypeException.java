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
package org.eclipse.update.core.model;

/**
 * Exception thrown when the type of the site discovered in the site manifest
 * does not correspond to the type expected by the concrete site factory.
 * 
 * @see org.eclipse.update.core.model.SiteModelFactory#canParseSiteType(String)
 * @since 2.0
 */

public class InvalidSiteTypeException extends Exception {

	private String newSiteType;

	/**
	 * Construct the exception indicating the detected site type
	 * 
	 * @since 2.0
	 */
	public InvalidSiteTypeException(String newType) {
		super();
		newSiteType = newType;
	}

	/**
	 * Returns the site type detected in the parsed site manifest
	 * 
	 * @return site type
	 * @since 2.0
	 */
	public String getNewType() {
		return newSiteType;
	}
}
