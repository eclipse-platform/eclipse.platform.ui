/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.util.impl.resources;

import java.io.IOException;
import java.net.MalformedURLException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;

public class OSGiResourceLocator implements IResourceLocator {
	
	private String startLocation;

	public OSGiResourceLocator(String start) {
		startLocation = start;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IURIResolver#resolve(java.lang.String)
	 */
	public String resolve(String uri) {
		try {
			URL resolvedURL = FileLocator.resolve(
					new URL(startLocation + uri));
			return resolvedURL.toString();
		} catch (MalformedURLException e) {
			
		} catch (IOException e) {
			
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getInputStream(java.lang.String)
	 */
	public InputStream getInputStream(String uri) throws Exception {
		return FileLocator.resolve(
					new URL(startLocation + uri)
				).openStream();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.e4.ui.css.core.util.resources.IResourceLocator#getReader(java.lang.String)
	 */
	public Reader getReader(String uri) throws Exception {
		return new InputStreamReader(getInputStream(uri));
	}


}
