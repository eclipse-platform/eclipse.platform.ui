/*
 * Created on May 24, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

import java.net.URL;
import java.util.Vector;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

public class UpdateSearchScope {
	private Vector sites;

	private static class UpdateSearchSite extends UpdateSiteAdapter implements IUpdateSearchSite {
		private String [] categoriesToSkip;

		public UpdateSearchSite(String label, URL siteURL, String [] categoriesToSkip) {
			super(label, siteURL);
			this.categoriesToSkip = categoriesToSkip;
		}
		public String[] getCategoriesToSkip() {
			return categoriesToSkip;
		}
	}

	public UpdateSearchScope() {
		sites = new Vector();
	}

	public void addSearchSite(String label, URL siteURL, String [] categoriesToSkip) {
		sites.add(new UpdateSearchSite(label, siteURL, categoriesToSkip));
	}

	public IUpdateSearchSite [] getSearchSites() {
		return (UpdateSearchSite[])sites.toArray(new UpdateSearchSite[sites.size()]);
	}
}