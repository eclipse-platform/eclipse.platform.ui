/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.update.internal.search;

import java.util.*;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.search.*;

/**
 * Searches an update site
 */
public class SiteSearchCategory extends BaseSearchCategory {
	private IUpdateSearchQuery[] queries;
	private static final String CATEGORY_ID =
		"org.eclipse.update.core.unified-search"; //$NON-NLS-1$

	private static class Query implements IUpdateSearchQuery {
		public void run(
			ISite site,
			String[] categoriesToSkip,
			IUpdateSearchFilter filter,
			IUpdateSearchResultCollector collector,
			IProgressMonitor monitor) {
			ISiteFeatureReference[] refs = site.getFeatureReferences();
			HashSet ignores = new HashSet();
			if (categoriesToSkip != null) {
				for (int i = 0; i < categoriesToSkip.length; i++) {
					ignores.add(categoriesToSkip[i]);
				}
			}

			monitor.beginTask("", refs.length); //$NON-NLS-1$

			for (int i = 0; i < refs.length; i++) {
				ISiteFeatureReference ref = refs[i];
				boolean skipFeature = false;
				if (monitor.isCanceled())
					break;
				if (ignores.size() > 0) {
					ICategory[] categories = ref.getCategories();

					for (int j = 0; j < categories.length; j++) {
						ICategory category = categories[j];
						if (ignores.contains(category.getName())) {
							skipFeature = true;
							break;
						}
					}
				}
				try {
					if (!skipFeature) {
						if (filter.accept(ref)) {
							IFeature feature = ref.getFeature(null);
							if (filter.accept(feature))
								collector.accept(feature);
							monitor.subTask(feature.getLabel());
						}
					}
				} catch (CoreException e) {
					System.out.println(e);
				} finally {
					monitor.worked(1);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.update.internal.ui.search.ISearchQuery#getSearchSite()
		 */
		public IQueryUpdateSiteAdapter getQuerySearchSite() {
			return null;
		}
	}

	public SiteSearchCategory() {
		super(CATEGORY_ID);
		queries = new IUpdateSearchQuery[] { new Query()};
	}

	public IUpdateSearchQuery[] getQueries() {
		return queries;
	}
}
