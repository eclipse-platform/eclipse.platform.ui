/*******************************************************************************
 * Copyright (c) 2008, 2012 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager;
import org.eclipse.e4.ui.css.core.utils.StringUtils;

/**
 * Resources locator manager implementation.
 */
public class ResourcesLocatorManager implements IResourcesLocatorManager {

	/**
	 * ResourcesLocatorManager Singleton
	 */
	public static final IResourcesLocatorManager INSTANCE = new ResourcesLocatorManager();

	/**
	 * List of IResourceLocator instance which was registered.
	 */
	private List uriResolvers = null;

	public ResourcesLocatorManager() {
		registerResourceLocator(new HttpResourcesLocatorImpl());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager#registerResourceLocator(org.eclipse.e4.ui.css.core.util.resources.IResourceLocator)
	 */
	@Override
	public void registerResourceLocator(IResourceLocator resourceLocator) {
		if (uriResolvers == null)
			uriResolvers = new ArrayList();
		if (resourceLocator instanceof OSGiResourceLocator) {
			uriResolvers.add(0, resourceLocator);
		} else {
			uriResolvers.add(resourceLocator);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourcesLocatorManager#unregisterResourceLocator(org.eclipse.e4.ui.css.core.util.resources.IResourceLocator)
	 */
	@Override
	public void unregisterResourceLocator(IResourceLocator resourceLocator) {
		if (uriResolvers == null)
			return;
		uriResolvers.remove(resourceLocator);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.e4.ui.css.core.util.resources.IURIResolver#resolve(java.lang.String)
	 */
	@Override
	public String resolve(String uri) {
		if (StringUtils.isEmpty(uri))
			return null;
		if (uriResolvers == null)
			return null;
		// Loop for IResourceLocator registered and return the uri resolved
		// as soon as an IResourceLocator return an uri resolved which is not
		// null.
		for (Iterator iterator = uriResolvers.iterator(); iterator.hasNext();) {
			IResourceLocator resolver = (IResourceLocator) iterator.next();
			String s = resolver.resolve(uri);
			if (s != null)
				return s;
		}
		return null;
	}

	@Override
	public InputStream getInputStream(String uri) throws Exception {
		if (StringUtils.isEmpty(uri))
			return null;
		if (uriResolvers == null)
			return null;

		// Loop for IResourceLocator registered and return the InputStream from
		// the uri resolved
		// as soon as an IResourceLocator return an uri resolved which is not
		// null.
		for (Iterator iterator = uriResolvers.iterator(); iterator.hasNext();) {
			IResourceLocator resolver = (IResourceLocator) iterator.next();
			String s = resolver.resolve(uri);
			if (s != null) {
				InputStream inputStream = resolver.getInputStream(uri);
				if (inputStream != null)
					return inputStream;
			}
		}
		return null;
	}

	@Override
	public Reader getReader(String uri) throws Exception {
		if (StringUtils.isEmpty(uri))
			return null;
		if (uriResolvers == null)
			return null;
		// Loop for IResourceLocator registered and return the Reader from
		// the uri resolved
		// as soon as an IResourceLocator return an uri resolved which is not
		// null.
		for (Iterator iterator = uriResolvers.iterator(); iterator.hasNext();) {
			IResourceLocator resolver = (IResourceLocator) iterator.next();
			String s = resolver.resolve(uri);
			if (s != null) {
				Reader reader = resolver.getReader(uri);
				if (reader  != null)
					return reader;
			}
		}

		return null;
	}

}
