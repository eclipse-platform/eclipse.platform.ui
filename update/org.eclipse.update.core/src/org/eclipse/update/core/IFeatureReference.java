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
 
public interface IFeatureReference {
	
	
	/**
	 * Returns the URL that points at the Feature.
	 * This URL is the unique identifier of the feature
	 * within the site.
	 * 
	 * The URL is declared in the <code>feature.xml</code> file.	
	 * 
	 * @return the URL identifying feature in the Site.
	 */
	URL getURL();
	
	/**
	 * Returns the array of categories the feature belong to.
	 * 
	 * The categories are declared in the <code>site.xml</code> file.
	 * 
	 * @see ICategory
	 * @return the array of categories this feature belong to. Returns an empty array
	 * if there are no cateopries.
	 */
	ICategory[] getCategories();

	
	/**
	 * Returns the feature this reference points to
	 *  @return teh feature on teh Site
	 */
	IFeature getFeature() throws CoreException;
	
	
}


