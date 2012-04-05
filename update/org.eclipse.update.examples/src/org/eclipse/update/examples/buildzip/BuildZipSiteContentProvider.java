/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.examples.buildzip;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.SiteContentProvider;

public class BuildZipSiteContentProvider extends SiteContentProvider {

	/**
	 * Constructor for SiteContentProvider.
	 */
	public BuildZipSiteContentProvider(URL url) {
		super(url);
	}
	
	/*
	 * @see ISiteContentProvider#getArchiveReference(String)
	 */
	public URL getArchiveReference(String id) throws CoreException {
		// build zip features do not have associated archives
		// the zip file also contains the archives files
		return null;
	}

}
