package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * Provides 
  * 
  */
 //FIXME: javadoc
 
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
	ContentReference getFeatureManifestReference(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the whole DefaultFeature
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getArchiveReferences(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryArchiveReferences(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry, IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references for the INONPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs		 
	 * @since 2.0 
	 */

	ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry, IProgressMonitor monitor) throws CoreException;
	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryContentReferences(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @param monitor optional progress monitor
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry, IProgressMonitor monitor) throws CoreException;
	
	/**
	 * sets the feature for this content provider
	 * @param the IFeature 
	 * @since 2.0
	 */
	void setFeature(IFeature feature);
	
	
}


