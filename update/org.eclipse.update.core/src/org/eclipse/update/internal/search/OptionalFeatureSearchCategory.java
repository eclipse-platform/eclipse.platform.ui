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
package org.eclipse.update.internal.search;

import java.net.*;
import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.search.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OptionalFeatureSearchCategory extends UpdateSearchCategory {
	private IUpdateSearchQuery[] queries;
	private ArrayList vids;
	private static final String CATEGORY_ID =
		"org.eclipse.update.core.unified-search";

	private class OptionalQuery implements IUpdateSearchQuery {
		public void run(
			ISite site,
			String[] categoriesToSkip,
			IUpdateSearchFilter filter,
			IUpdateSearchResultCollector collector,
			IProgressMonitor monitor) {

			monitor.beginTask("", vids.size());
			for (int i = 0; i < vids.size(); i++) {
				VersionedIdentifier vid = (VersionedIdentifier) vids.get(i);
				monitor.subTask(vid.toString());
				IFeature feature =
					createFeature(
						site,
						vid,
						new SubProgressMonitor(monitor, 1));
				if (feature!=null && filter.accept(feature))
					collector.accept(feature);
			}
		}

		private IFeature createFeature(
			ISite site,
			VersionedIdentifier vid,
			IProgressMonitor monitor) {
			try {
				URL siteURL = site.getURL();
				//TODO This assumption stands only in the default case
				// In general, feature archive URL may be mapped on site.
				// Also, feature type may be something else (not packaged).
				// We may need additional information (not only id and version)
				// in order to create a feature on a site.
				String relative = vid.toString();
				URL featureURL = new URL(siteURL, "features/" + relative+".jar");
				return site.createFeature(
					"org.eclipse.update.core.packaged",
					featureURL,
					monitor);
			} catch (Exception e) {
				return null;
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.search.ISearchQuery#getSearchSite()
		 */
		public IQueryUpdateSiteAdapter getQuerySearchSite() {
			return null;
		}
	}

	public void addVersionedIdentifier(VersionedIdentifier vid) {
		vids.add(vid);
	}

	public void clear() {
		vids.clear();
	}

	public OptionalFeatureSearchCategory() {
		super(CATEGORY_ID);
		vids = new ArrayList();
		queries = new IUpdateSearchQuery[] { new OptionalQuery()};
	}

	public IUpdateSearchQuery[] getQueries() {
		return queries;
	}
}
