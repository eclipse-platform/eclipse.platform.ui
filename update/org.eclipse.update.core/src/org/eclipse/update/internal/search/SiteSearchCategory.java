/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
			

			List siteFeatureReferences = new ArrayList(Arrays.asList(refs));
			monitor.beginTask("", refs.length); //$NON-NLS-1$
			ThreadGroup featureDownloaders = new ThreadGroup("FeatureDownloader"); //$NON-NLS-1$
			int numberOfThreads = (refs.length > 5)? 5: refs.length;
			
			for( int i = 0; i < numberOfThreads; i++) {
				Thread featureDownloader = new Thread(featureDownloaders, new FeatureDownloader(siteFeatureReferences, collector, filter, ignores, monitor));
				featureDownloader.start();
			}
			
			
			while(featureDownloaders.activeCount() != 0) {
				
				if (monitor.isCanceled()) {
					synchronized(siteFeatureReferences) { 
						siteFeatureReferences.clear();
					}
				}
				Thread[] temp = new Thread[featureDownloaders.activeCount()];
				featureDownloaders.enumerate(temp);
				if (temp[0] != null) {
					try	{
						temp[0].join(250);
					} catch (InterruptedException ie) {
						//FIX ME:
						ie.printStackTrace();
					}
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
	
	
	private static class FeatureDownloader implements Runnable {
		
		private List siteFeatureReferences;
		
		private IProgressMonitor monitor;
		
		private IUpdateSearchFilter filter;
		
		private IUpdateSearchResultCollector collector;
		
		private HashSet ignores;

		private FeatureDownloader() {			
		}
		
		public FeatureDownloader(List siteFeatureReferences, IUpdateSearchResultCollector collector, IUpdateSearchFilter filter, HashSet ignores, IProgressMonitor monitor) {
			super();

			this.collector = collector;
			this.filter = filter;
			this.ignores = ignores;
			this.monitor = monitor;
			this.siteFeatureReferences = siteFeatureReferences;
		}

		public void run() {
			
			ISiteFeatureReference siteFeatureReference = null;
			
			while (siteFeatureReferences.size() != 0) {
				
				synchronized(siteFeatureReferences) { 
					if (siteFeatureReferences.size() != 0) {
						siteFeatureReference = (ISiteFeatureReference)siteFeatureReferences.remove(0);
					} else {
						siteFeatureReference = null;
					}
				}
				if (siteFeatureReference != null) {
					boolean skipFeature = false;
					if (monitor.isCanceled())
						break;
					if (ignores.size() > 0) {
						ICategory[] categories = siteFeatureReference.getCategories();
						
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
							if (filter.accept(siteFeatureReference)) {
								IFeature feature = siteFeatureReference.getFeature(null);
								synchronized(siteFeatureReferences) {
									if (filter.accept(feature))								
										collector.accept(feature);							    
									monitor.subTask(feature.getLabel());
								}
							}
						}
					} catch (CoreException e) {
						System.out.println(e);
					} finally {
						monitor.worked(1);
					}
				}
			}
			
		}
	}
}
