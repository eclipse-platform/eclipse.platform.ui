package org.eclipse.update.examples.buildzip;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.SiteContentProvider;

public class ZipSiteContentProvider extends SiteContentProvider {

	/**
	 * Constructor for SiteContentProvider.
	 */
	public ZipSiteContentProvider(URL url) {
		super(url);
	}
	
	/*
	 * @see ISiteContentProvider#getArchiveReference(String)
	 */
	public URL getArchiveReference(String id) throws CoreException {
		return null;
	}

}
