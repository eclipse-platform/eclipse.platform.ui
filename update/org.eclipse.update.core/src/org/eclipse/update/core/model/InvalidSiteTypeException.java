package org.eclipse.update.core.model;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
 /**
  * Exception thrown when the type of the site discovered in the site.xml 
  * does not correspond to the type expected based on the protocol of the URL
  * of the Site
  */
 
public class InvalidSiteTypeException extends Exception {

	private String newSiteType;
	
	public InvalidSiteTypeException(String newType){
		super();
		newSiteType = newType;
	}
	
	public String getNewType(){
		return newSiteType;
	}
}

