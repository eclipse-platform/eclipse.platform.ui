package org.eclipse.update.core;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * A content consumer manages the storage or archives, plugins and
  * feature inside an <code>IFeature</code> or an <code> ISite</code>
  * 
  * A FeatureContentConsumer has a <i>parent</i> which is the entry (IFeature, IPluginEntry
  * or INonPluginEntry) that is going to be used for storage.
  * 
  * Only a FeatureContentConsumer with an IFeature parent can open sub-FeatureContentConsumer.
  */
 
public interface IFeatureContentConsumer {

	
	/**
	 * Stores a content reference into the FeatureContentConsumer
	 * @param ContentReference the content reference to store
	 * @param IProgressMonitor the progress monitor
	 * @throws CoreException if an error occurs storing the content reference
	 * @since 2.0 
	 */
	void store(ContentReference contentReference, IProgressMonitor monitor) throws CoreException;

	/**
	 * closes the opened FeatureContentConsumer
	 * @since 2.0 
	 */
	IFeatureReference close() throws CoreException;

	/**
	 * opens a Non plugin Entry for storage
	 * @return the new FeatureContentConsumer for this <code>INonPluginEntry</code>
	 * @throws CoreException if the opens is done on a FeatureContentConsumer parent other than an IFeature.
	 * @since 2.0 
	 */

	IContentConsumer open(INonPluginEntry nonPluginEntry) throws CoreException;

	/**
	 * opens a Non plugin Entry for storage
	 * @return the new FeatureContentConsumer for this <code>IPluginEntry</code>
	 * @throws CoreException if the opens is done on a FeatureContentConsumer parent other than an IFeature.
	 * @since 2.0 
	 */

	IContentConsumer open(IPluginEntry pluginEntry) throws CoreException;
	
		
	/**
	 * abort the opened FeatureContentConsumer
	 * @since 2.0 
	 */

	void abort();		
	
	/**
	 * sets the feature for this content consumer
	 * @param the IFeature 
	 * @since 2.0
	 */
	void setFeature(IFeature feature);
			
}


