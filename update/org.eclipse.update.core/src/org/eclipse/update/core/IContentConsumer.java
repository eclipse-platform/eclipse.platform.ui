/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.core;

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
	public void store(ContentReference contentReference, IProgressMonitor monitor)
		throws CoreException;

	/**
	 * Indicates successful completion of the store operations for this
	 * consumer
	 * 
	 * @exception CoreException
	 * @since 2.0 
	 */
	public void close() throws CoreException;

}
