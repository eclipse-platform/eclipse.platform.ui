package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
 
 /**
  * Provides 
  * 
  */
 
public interface IFeatureContentProvider {
	
	/**
	 * Returns the feature url
	 * 
	 * @return the feature url
	 * @since 2.0
	 */
	URL getURL();	

	/**
	 * Returns the feature manifest 
	 * 
	 * @param monitor optional progress monitor
	 * @return the feature manifest
	 * @since 2.0
	 */
	ContentReference getFeatureManifestReference(InstallMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the whole Feature
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getArchiveReferences(InstallMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the IFeature
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryArchiveReferences(InstallMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the INONPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs		 
	 * @since 2.0 
	 */

	ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry, InstallMonitor monitor) throws CoreException;
	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryContentReferences(InstallMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry, InstallMonitor monitor) throws CoreException;
		
	/**
	 * Returns the feature for this content provider
	 * @return feature for this content provider
	 * @since 2.0
	 */
	IFeature getFeature();
	
	/**
	 * sets the feature for this content provider
	 * @param the IFeature 
	 * @since 2.0
	 */
	void setFeature(IFeature feature);
	
	/**
	 * Returns the size ....
	 * @since 2.0
	 */
	long getDownloadSizeFor(IPluginEntry[] pluginEntries, INonPluginEntry[] nonPluginEntries);
	
	/**
	 * 
	 * @since 2.0
	 */
	long getInstallSizeFor(IPluginEntry[] pluginEntries, INonPluginEntry[] nonPluginEntries);

	
	/**
	 * Returns the verifier for ths feature or null if the feature canot be verified
	 * @since 2.0
	 */	
	IVerifier getVerifier() throws CoreException ;
			
}


