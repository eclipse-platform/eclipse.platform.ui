package org.eclipse.update.configuration;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;

/**
 * Configured Site.
 * Represents an installation site "filtered" by configuration information.
 * Configured site is the target of the feature update operations (install
 * feature, remove feature, configure feature, unconfigure feature).
 * 
 * @since 2.0
 */
public interface IConfiguredSite extends IAdaptable {

	/**
	 * Returns the underlying "unfiltered" site.
	 * 
	 * @return the underlying site 
	 * @since 2.0 
	 */
	public ISite getSite();

	/**
	 * Indicates whether updates can be applied to the site.
	 * 
	 * @return <code>true</code> if the site can be updated, 
	 * <code>false</code> otherwise
	 * @since 2.0 
	 */
	public boolean isUpdatable();

	/**
	 * Sets the site as updatable.
	 * 
	 * @param value <code>true</code> if the site can be updated, 
	 * <code>false</code> otherwise
	 * @since 2.0 
	 */
	public void isUpdatable(boolean value);

	/**
	 * Install the specified feature on this site.
	 * 
	 * @param feature feature to install
	 * @param verificationListener verification listener, or <code>null</code>
	 * @param monitor progress monitor, or <code>null</code>
	 * @since 2.0 
	 */
	public IFeatureReference install(
		IFeature feature,
		IVerificationListener verificationListener,
		IProgressMonitor monitor)
		throws CoreException;

	/**
	 * Remove (uninstall) the specified feature from this site
	 * 
	 * @param feature feature to remove
	 * @param monitor progress monitor, or <code>null</code>
	 * @since 2.0 
	 */
	public void remove(IFeature feature, IProgressMonitor monitor)
		throws CoreException;

	/**
	 * Indicates if the specified feature is "broken". A feature is considered
	 * to be broken in the context of this site, if some of the plug-ins
	 * referenced by the feature are not installed on this site.
	 * 
	 * @param feature the feature
	 * @return <code>true</code> if the feature is broken on this
	 * site, <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean isBroken(IFeature feature);

	/**
	 * Indicates if the specified feature is configured on this site.
	 * 
	 * @param feature the feature
	 * @return <code>true</code> if the feature is configured,
	 * <code>false</code> otherwise
	 * @since 2.0
	 */
	public boolean isConfigured(IFeature feature);

	/**
	 * Configure the specified feature on this site. The configured
	 * feature will be included on next startup.
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public void configure(IFeature feature) throws CoreException;

	/**
	 * Unconfigure the specified feature from this site. The unconfigured
	 * feature will be omitted on the next startup.
	 * 
	 * @param feature the feature
	 * @since 2.0 
	 */
	public boolean unconfigure(IFeature feature) throws CoreException;

	/**
	 * Return references to features configured on this site.
	 * 
	 * @return an array of feature references, or an empty array.
	 * @since 2.0 
	 */
	public IFeatureReference[] getConfiguredFeatures();

	/**
	 * Return all features installed on this site (configured as well
	 * as unconfigured). Note, that if the site requires reconciliation,
	 * the result may not match the result of the corresponding method
	 * on the underlying site.
	 * 
	 * @see ISite#getFeatureReferences()
	 * @return an array of feature references, or an empty array.
	 * @since 2.0 
	 */
	public IFeatureReference[] getFeatureReferences();

	/**
	 * Returns the install configuration object this site is part of.
	 * 
	 * @return install configuration object
	 * @since 2.0
	 */
	public IInstallConfiguration getInstallConfiguration();

	/**
	 * Adds a change listener to the configured site.
	 * 
	 * @param listener the listener to add
	 * @since 2.0 
	 */
	public void addConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener);
	
	/**
	 * Removes a change listener from the configured site.
	 * 
	 * @param listener the listener to remove
	 * @since 2.0 
	 */
	public void removeConfiguredSiteChangedListener(IConfiguredSiteChangedListener listener);

}