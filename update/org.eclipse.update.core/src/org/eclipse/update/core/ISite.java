package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.configuration.*;


/**
 * 
 */

public interface ISite {

	/**
	 * 
	 * @since 2.0
	 */
	public static final String DEFAULT_INSTALLED_FEATURE_TYPE = "org.eclipse.update.core.installed";	 //$NON-NLS-1$		

	/**
	 * 
	 * @since 2.0
	 */
	public static final String DEFAULT_PACKAGED_FEATURE_TYPE = "org.eclipse.update.core.packaged";	 //$NON-NLS-1$		

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
	 * returns the newly created feature Reference
	 * @param feature the Feature to install
	 * @param verifier FIXME
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	IFeatureReference install(IFeature feature,IVerificationListener verificationListener, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * 
	 * @param feature the DefaultFeature to remove
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;
	
	
	/**
	 * 
	 * @return teh URL of the site
	 * @since 2.0 
	 */

	URL getURL() ;
	
	/**
	 * 
	 * @return the description of the site
	 * @since 2.0 
	 */

	IURLEntry getDescription() ;
	
	
	/**
	 * 
	 * @return teh type of the site
	 * @since 2.0 
	 */

	String getType() ;
	
	
	/**
	 * @since 2.0 
	 * @deprecated use getDescription().getURL()
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

	IArchiveReference[] getArchives();

	/**
	 * returns the default type for an package feature on this site
	 * @return String the type
	 * @since 2.0
	 */
	String getDefaultPackagedFeatureType();

	
	/**
	 * Returns an array of plug-ins managed by the Site
	 * 
	 * @return the accessible plug-ins. Returns an empty array
	 * if there are no plug-ins.
	 */
	IPluginEntry [] getPluginEntries()  ;

	/**
	 * Returns the number of managed plug-ins
	 * @return the number of plug-ins
	 */
	int getPluginEntryCount() ;
	
	/**
	 * returns the install size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the install size is unknown and equal to <code>-1</code>.
	 * @since 2.0 
	 */
	long getInstallSizeFor(IFeature site);

	/**
	 * returns the download size
	 * of the feature to be installed on the site.
	 * If the site is <code>null</code> returns the maximum size
	 * 
	 * If one plug-in entry has an unknown size.
	 * then the download size is unknown and equal to <code>-1</code>
	 * 
	 * @since 2.0 
	 */
	long getDownloadSizeFor(IFeature site) ;	
	
	/**
	 * 
	 * @since 2.0
	 */
	IPluginEntry[] getPluginEntriesOnlyReferencedBy(IFeature feature) throws CoreException;
	
	
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
	
		
	/**
	 * returns the FeatureReference of this Feature inside the Site
	 * returns null if this site does not manage this feature
	 */
	IFeatureReference getFeatureReference(IFeature feature) ;

	}