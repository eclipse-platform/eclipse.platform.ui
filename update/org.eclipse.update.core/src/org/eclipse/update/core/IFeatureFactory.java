package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
 
 /**
  *
  * 
  */
 
public interface IFeatureFactory {


	/**
	 * extension point ID
	 */
	public static final String SIMPLE_EXTENSION_ID = "featureTypes";
	public static final String INSTALLABLE_FEATURE_TYPE = "jar";
	public static final String EXECUTABLE_FEATURE_TYPE = "exe";	
	
	/**
	 * Returns a feature based on the URL and the site in which the DefaultFeature is.
	 * @return a feature
	 * @since 2.0 
	 */

	IFeature createFeature(URL url,ISite site) throws CoreException;
	
	IFeature createFeature(ISite site) throws CoreException;
		
}


