package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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