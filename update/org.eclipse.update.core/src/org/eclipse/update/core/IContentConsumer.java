package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Generic content consumer.
 * A generic content consumer is used to store plug-in and non-plug-in files
 * for a feature.
 * <p>
 * Clients may implement this interface. However, in most cases clients 
 * will only use the content consumer provided by the feature type(s)
 * implemented by the platform.
 * </p>
 * @see org.eclipse.update.core.IFeatureContentConsumer
 * @since 2.0
 */
public interface IContentConsumer {

	/**
	 * Stores a file.
	 * 
	 * @see IFeatureContentConsumer#open(IPluginEntry)
	 * @see IFeatureContentConsumer#open(INonPluginEntry)
	 * @param ContentReference reference to the file to store
	 * @param IProgressMonitor progress monitor, can be <code>null</code>
	 * @exception CoreException
	 * @since 2.0 
	 */
	void store(ContentReference contentReference, IProgressMonitor monitor)
		throws CoreException;

	/**
	 * Indicates successful completion of the store operations for this
	 * consumer
	 * 
	 * @exception CoreException
	 * @since 2.0 
	 */

	void close() throws CoreException;

}