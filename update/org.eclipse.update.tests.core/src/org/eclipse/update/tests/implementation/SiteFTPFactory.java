package org.eclipse.update.tests.implementation;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteFactory;
import org.eclipse.update.core.model.InvalidSiteTypeException;

public class SiteFTPFactory implements ISiteFactory {

	public static final String FILE = "a/b/c/";

	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public ISite createSite(URL url) throws CoreException, InvalidSiteTypeException {
		ISite site;
		try {
			site = new SiteFTP(new URL("http://eclipse.org/"+FILE));
		} catch (MalformedURLException e){
			throw new CoreException(new Status(IStatus.ERROR,"org.eclipse.update.tests",IStatus.OK,"Url error",e));
		}
		return site;
	}

}
