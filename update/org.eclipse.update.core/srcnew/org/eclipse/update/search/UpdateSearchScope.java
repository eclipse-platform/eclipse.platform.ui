/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.search;

import java.net.URL;
import java.util.Vector;

import org.eclipse.update.internal.search.UpdateSiteAdapter;

/**
 * This class encapsulates update scope of the update search.
 * Sites that need to be visited should be added to the scope.
 * If some categories should be skipped, their names must be
 * passed as array of strings to the method.
 *
 *@see UpdateSearchRequest
 */

public class UpdateSearchScope {
	private Vector sites;

	private static class UpdateSearchSite
		extends UpdateSiteAdapter
		implements IUpdateSearchSite {
		private String[] categoriesToSkip;

		public UpdateSearchSite(
			String label,
			URL siteURL,
			String[] categoriesToSkip) {
			super(label, siteURL);
			this.categoriesToSkip = categoriesToSkip;
		}
		public String[] getCategoriesToSkip() {
			return categoriesToSkip;
		}
	}

	/**
	 * The default constructor.
	 */
	public UpdateSearchScope() {
		sites = new Vector();
	}

	/**
	 * Adds the site to scan to the search scope.
	 * @param label the presentation name of the site to visit.
	 * @param siteURL the URL of the site to visit.
	 * @param categoriesToSkip an array of category names that should be skipped or <samp>null</samp> if all features should be considered.
	 */
	public void addSearchSite(
		String label,
		URL siteURL,
		String[] categoriesToSkip) {
		sites.add(new UpdateSearchSite(label, siteURL, categoriesToSkip));
	}

	/**
	 * Returns the sites that should be visited during the search.
	 * @return an array of site adapters
	 */
	public IUpdateSearchSite[] getSearchSites() {
		return (UpdateSearchSite[]) sites.toArray(
			new UpdateSearchSite[sites.size()]);
	}
}