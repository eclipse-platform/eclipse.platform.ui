package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.internal.core.UpdateManagerPlugin;
 
 /**
  *
  * 
  */
 
public interface ISiteFactory {


	/**
	 * extension point ID
	 */
	public static final String SIMPLE_EXTENSION_ID = "siteTypes";	
	
	/**
	 * Returns a site based on the URL
	 * @return a feature
	 * @since 2.0 
	 */

	ISite createSite(URL url) throws CoreException, InvalidSiteTypeException;
		
}


