package org.eclipse.update.configuration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.update.core.*;
import org.eclipse.update.core.IFeature;
import org.eclipse.update.core.IFeatureReference;
import org.eclipse.update.core.ISite;



/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
/**
 * Interface defining the configuration of a site.
 * 
 * The SiteConfguration reflects the policy used on a site.
 * It also returns if you can write in this site or not
 */ 
public interface IConfiguredSite {
	
	
	/**
	 * Returns the Site 
	 * @since 2.0 
	 */

	ISite getSite();

	/**
	 * Returns true if features can be installed in this Site
	 * @since 2.0 
	 */

	boolean isUpdateable();

	
	/**
	 * sets if the site is an installable site
	 * @since 2.0 
	 */

	void isUpdateable(boolean installable);


	
		
		
		
	/**
	 * 
	 * @param feature the Feature to install
	 * @param verifier FIXME
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	IFeatureReference install(IFeature feature,IVerificationListener verificationListener, IProgressMonitor monitor) throws CoreException;

	/**
	 * 
	 * @param feature the Feature to remove
	 * @param monitor the Progress Monitor
	 * @since 2.0 
	 */

	void remove(IFeature feature, IProgressMonitor monitor) throws CoreException;

	/**
	 * returns true if the Feature is broken (a plugin is missing from the running one).
	 * @return Returns a boolean
	 * @since 2.0
	 */
	boolean isBroken(IFeature feature);

		
	/**
	 * Configure the DefaultFeature to be available at next startup
	 * @since 2.0 
	 */

	void configure(IFeature feature) throws CoreException;

	/**
	 * Unconfigure the feature from the execution path.
	 * returns false if the unconfigure is not sucessful
	 * @since 2.0 
	 */

	boolean unconfigure(IFeature feature) throws CoreException;

	/**
	 * returns the feature used in this configurationSite
	 * This is a subset of the feature of teh site
	 * @since 2.0 
	 */

	IFeatureReference[] getConfiguredFeatures();
	
	/**
	 * returns the InstallConfiguration this Configuration Site is part of
	 * @since 2.0
	 */
	IInstallConfiguration getInstallConfiguration();
	
	/**
	 * @since 2.0 
	 */
	void addConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener);
	/**
	 * @since 2.0 
	 */
	void removeConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener);
	
	
}

