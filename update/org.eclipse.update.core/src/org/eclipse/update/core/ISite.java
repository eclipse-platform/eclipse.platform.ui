package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;

public interface ISite extends IPluginContainer {
	
	/**
	 * Returns an array of feature this site contains
	 * 
	 * @return the list of features. Returns an empty array
	 * if there are no feature.
	 * @since 2.0 
	 */

	IFeatureReference [] getFeatureReferences() ;
	
	/**
	 * Notify listener of installation of the feature
	 * returns the newly created feature reference
	 * @param feature the Feature to install
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	IFeatureReference install(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * 
	 * @param feature the Feature to remove
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	
	/**
	 * @since 2.0 
	 */
	void addSiteChangedListener(ISiteChangedListener listener);
	/**
	 * @since 2.0 
	 */
	void removeSiteChangedListener(ISiteChangedListener listener);

	/**
	 * 
	 * @return teh URL of the site
	 * @since 2.0 
	 */

	URL getURL() ;
	
	
	/**
	 * @since 2.0 
	 */
	URL getInfoURL();

	/**
	 * Returns an array of categories for this site
	 * 
	 * @return the list of categories. Returns an empty array
	 * if there are no categories.
	 * @since 2.0 
	 */

	ICategory[] getCategories()  ;

	/**
	 * Returns an array of archives this site contains
	 * 
	 * @return the list of archives. Returns an empty array
	 * if there are no archive.
	 * @since 2.0 
	 */

	IInfo[] getArchives();
	
	/**
	 * Creates a new categoy within the Site
	 * The validity of the Category is not checked
	 * @since 2.0 
	 */

	void addCategory(ICategory category);
	
	/**
	 * Saves the site in a persitent form
	 * @since 2.0 
	 */

	void save() throws CoreException;
	
	
	
}