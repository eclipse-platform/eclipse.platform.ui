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
	 * @return the feature manifest
	 * @since 2.0
	 */
	ContentReference getFeatureManifestReference() throws CoreException;

	/**
	 * Returns an array of content references for the whole DefaultFeature
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getArchiveReferences() throws CoreException;

	/**
	 * Returns an array of content references for the IPluginEntry
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryArchiveReferences() throws CoreException;

	/**
	 * Returns an array of content references for the IPluginEntry
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs 
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryArchiveReferences(IPluginEntry pluginEntry) throws CoreException;

	/**
	 * Returns an array of content references for the INONPluginEntry
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs		 
	 * @since 2.0 
	 */

	ContentReference[] getNonPluginEntryArchiveReferences(INonPluginEntry nonPluginEntry) throws CoreException;
	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getFeatureEntryContentReferences() throws CoreException;

	/**
	 * Returns an array of content references composing the IPluginEntry
	 * 
	 * @return an array of ContentReference or an empty array if no references are found
	 * @throws CoreException when an error occurs
	 * @since 2.0 
	 */

	ContentReference[] getPluginEntryContentReferences(IPluginEntry pluginEntry) throws CoreException;
	
	/**
	 * sets the feature for this content provider
	 * @param the IFeature 
	 * @since 2.0
	 */
	void setFeature(IFeature feature);
	
	
}


