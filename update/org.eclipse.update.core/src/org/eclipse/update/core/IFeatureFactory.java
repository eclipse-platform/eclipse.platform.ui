package org.eclipse.update.core;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.net.URL;

import org.eclipse.core.runtime.CoreException;

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
	 * @since 2.0 
	 */
	IFeature createFeature(URL url, ISite site) throws CoreException;
}