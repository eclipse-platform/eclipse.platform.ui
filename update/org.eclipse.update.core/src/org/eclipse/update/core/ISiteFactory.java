package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.ParsingException;
 
 /**
  *
  * 
  */
 
public interface ISiteFactory {

	
	/**
	 * Returns a site based on the URL
	 * @param url the URL passed to teh Factory to create a Site
	 * @param forceCreation true if the factory should attempt to create a site if it doesn't already exist
	 * @return a feature
	 * @exception ParsingException when a parsing error occured
	 * @exception IOException when an IOException occured in the Stream
	 * @exception InvalidSiteTypeException when the type of the site is different from the one expected
	 * @since 2.0 
	 */
	// VK: does the forceCreation make sense as API ??? (what does it mean to create a site?)

	ISite createSite(URL url, boolean forceCreation) throws IOException, ParsingException, InvalidSiteTypeException;
		
}


