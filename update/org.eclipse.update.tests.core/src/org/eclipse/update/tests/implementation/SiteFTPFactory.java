package org.eclipse.update.tests.implementation;
/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.ISiteFactory;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.ParsingException;

public class SiteFTPFactory implements ISiteFactory {

	public static final String FILE = "a/b/c/";

	/*
	 * @see ISiteFactory#createSite(URL, boolean)
	 */
	public ISite createSite(URL url, boolean forceCreation) throws IOException, ParsingException, InvalidSiteTypeException {
		ISite site;
		site = new SiteFTP(new URL("http://eclipse.org/"+FILE));
		return site;
	}

}
