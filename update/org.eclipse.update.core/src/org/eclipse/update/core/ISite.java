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
	 */
	IFeatureReference [] getFeatureReferences() ;
	
	/**
	 * 
	 * @param feature the Feature to install
	 * @param monitor the Progress Monitor
	 */
	void install(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * 
	 * @param feature the Feature to remove
	 * @param monitor the Progress Monitor
	 */
	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	
	void addSiteChangedListener(ISiteChangedListener listener);
	void removeSiteChangedListener(ISiteChangedListener listener);

	/**
	 * 
	 * @return teh URL of the site
	 */
	URL getURL() ;
	
	
	URL getInfoURL();

	/**
	 * Returns an array of categories for this site
	 * 
	 * @return the list of categories. Returns an empty array
	 * if there are no categories.
	 */
	ICategory[] getCategories()  ;

	/**
	 * Returns an array of archives this site contains
	 * 
	 * @return the list of archives. Returns an empty array
	 * if there are no archive.
	 */
	IInfo[] getArchives();
	
	/**
	 * Creates a new categoy within the Site
	 * The validity of the Category is not checked
	 */
	void addCategory(ICategory category);
	
	
	
}