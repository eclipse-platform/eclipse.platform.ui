package org.eclipse.update.internal.core;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.update.core.*;
import org.eclipse.update.core.ISite;

public class SiteFileFactory implements ISiteFactory {


	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public ISite createSite(URL url) throws CoreException, InvalidSiteTypeException {
		return new SiteFile(url);
	}

}
