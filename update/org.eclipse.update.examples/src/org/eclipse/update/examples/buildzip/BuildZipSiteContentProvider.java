package org.eclipse.update.examples.buildzip;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

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
		// build zip features do not have plugin archives
		return null;
	}

}
