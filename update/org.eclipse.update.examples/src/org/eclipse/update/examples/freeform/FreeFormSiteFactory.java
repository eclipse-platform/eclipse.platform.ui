package org.eclipse.update.examples.freeform;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.update.core.BaseSiteFactory;
import org.eclipse.update.core.ISite;
import org.eclipse.update.core.Site;
import org.eclipse.update.core.model.InvalidSiteTypeException;
import org.eclipse.update.core.model.ParsingException;
import org.eclipse.update.core.model.URLEntryModel;

public class FreeFormSiteFactory extends BaseSiteFactory {

	/*
	 * @see ISiteFactory#createSite(URL)
	 */
	public ISite createSite(URL url) throws IOException, ParsingException, InvalidSiteTypeException {
		
		// Create site
		Site site = null;
		InputStream is = null;
		try {
			is = url.openStream();
			site = (Site) parseSite(is);
		} finally {
			if (is != null) try { is.close(); } catch(IOException e) {}
		}
		URLEntryModel realSiteRef = site.getDescriptionModel();
		if (realSiteRef == null)
			throw new IOException("Unable to obtain update site reference");
		String siteURLString = realSiteRef.getURLString();
		if (siteURLString == null)
			throw new IOException("Unable to obtain update site reference");
		URL siteURL = new URL(siteURLString);
		FreeFormSiteContentProvider contentProvider = new FreeFormSiteContentProvider(siteURL);
		site.setSiteContentProvider(contentProvider);
		site.resolve(siteURL, null); // resolve any URLs relative to the site

		return site;
	}

	/*
	 * @see SiteModelFactory#canParseSiteType(String)
	 */
	public boolean canParseSiteType(String type) {
		return type!=null && type.equals("org.eclipse.update.examples.site.freeform");
	}

}