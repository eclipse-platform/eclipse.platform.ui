package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
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
	 * Returns a feature based on the URL and the site in which the DefaultFeature is.
	 * URL can be null.
	 * @return a feature
	 * @since 2.0 
	 */
	IFeature createFeature(URL url,ISite site) throws CoreException;
	
		
}


