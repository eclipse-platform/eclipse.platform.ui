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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Feature factory interface.
 * A feature factory is used to construct new instances of concrete
 * features. 
 * <p>
 * Clients may implement this interface. However, in most cases clients should 
 * directly instantiate or subclass the provided implementation of this 
 * interface.
 * </p>
 * @see org.eclipse.update.core.BaseFeatureFactory
 * @since 2.0
 */
public interface IFeatureFactory {

	/**
	 * Returns a feature defined by the supplied URL. The feature
	 * is associated with the specified site.
	 * <p>
	 * The actual interpretation of the URL is feature-type specific.
	 * In most cases the URL will point to some feature-specific
	 * file that can be used (directly or indirectly) to construct
	 * the feature object.
	 * </p>
	 * @param url URL interpreted by the feature
	 * @param site site to be associated with the feature
	 * @return concrete feature object
	 * @exception CoreException
	 * @deprecated use createFeature(URL, ISite, IProgressMonitor) instead
	 * @since 2.0 
	 */
	public IFeature createFeature(URL url, ISite site) throws CoreException;
	
	/**
	 * Returns a feature defined by the supplied URL. The feature
	 * is associated with the specified site.
	 * <p>
	 * The actual interpretation of the URL is feature-type specific.
	 * In most cases the URL will point to some feature-specific
	 * file that can be used (directly or indirectly) to construct
	 * the feature object.
	 * </p>
	 * @param url URL interpreted by the feature
	 * @param site site to be associated with the feature
	 * @return concrete feature object
	 * @exception CoreException
	 * @since 2.1 
	 */
	public IFeature createFeature(URL url, ISite site, IProgressMonitor monitor) throws CoreException;	
}
