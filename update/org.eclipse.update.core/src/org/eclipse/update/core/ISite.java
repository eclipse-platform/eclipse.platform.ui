package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.InputStream;
import java.net.URL;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.internal.core.UpdateManagerPlugin;

public interface ISite extends IPluginContainer {


	/**
	 * extension point ID
	 */
	public static final String SIMPLE_EXTENSION_ID = "siteTypeProtocols";
		
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
	 * @param feature the DefaultFeature to install
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	IFeatureReference install(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * 
	 * @param feature the DefaultFeature to remove
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
	 * 
	 * @return teh type of the site
	 * @since 2.0 
	 */

	String getType() ;
	
	
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
	 * returns the associated ICategory
	 * @return the ICategory associated to teh key or null if none exist
	 * @since 2.0
	 */
	public ICategory getCategory(String key);
	

	/**
	 * Returns an array of archives this site contains
	 * 
	 * @return the list of archives. Returns an empty array
	 * if there are no archive.
	 * @since 2.0 
	 */

	IArchiveEntry[] getArchives();
	
		
		
	/**
	 * returns the default type for an installable feature on this site
	 * @return String the type
	 * @since 2.0
	 */
	String getDefaultInstallableFeatureType();

	/**
	 * returns the default type for an executable feature on this site
	 * @return String the type
	 * @since 2.0
	 */
	String getDefaultExecutableFeatureType();
	
	
	/**
	 * Saves the site in a persitent form
	 * @since 2.0 
	 */

	void save() throws CoreException;
	
	/**
	 *Returns the ISiteContentConsumer for this site
	 * @param feature
	 * @throws CoreException when the Site does not allow storage.
	 * @since 2.0
	 */
	ISiteContentConsumer createSiteContentConsumer(IFeature feature) throws CoreException;	

	/**
	 * Sets the ISiteContentProvider for this feature
	 * @since 2.0
	 */
	void setSiteContentProvider(ISiteContentProvider siteContentProvider);
	
	/**
	 * Returns the ISiteContentProvider for this feature
	 * @throws CoreException when the content provider is not set
	 * @since 2.0
	 */
	ISiteContentProvider getSiteContentProvider() throws CoreException;
	
		

	}