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
package org.eclipse.update.search;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.*;
import org.eclipse.update.internal.operations.*;
import org.eclipse.update.internal.search.*;
import org.eclipse.update.internal.search.UpdatePolicy;

/**
 * This class is central to update search. The search pattern
 * is encapsulated in update search category, while the search
 * scope is defined in the scope object. When these two objects
 * are defined and set, search can be performed using the 
 * provided method. Search results are reported to the
 * result collector, while search progress is tracked using
 * the progress monitor.
 * <p>Classes that implement <samp>IUpdateSearchResultCollector</samp>
 * should call 'accept' to test if the match should be
 * accepted according to the filters added to the request.
 * 
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under development and expected to
 * change significantly before reaching stability. It is being made available at this early stage to solicit feedback
 * from pioneering adopters on the understanding that any code that uses this API will almost certainly be broken
 * (repeatedly) as the API evolves.
 * </p>
 * @see UpdateSearchScope
 * @see IUpdateSearchCategory
 * @since 3.0
 */
public class UpdateSearchRequest {
	private IUpdateSearchCategory category;
	private UpdateSearchScope scope;
	private boolean searchInProgress = false;
	private AggregateFilter aggregateFilter = new AggregateFilter();

	class MirroredUpdateSiteAdapter extends UpdateSiteAdapter {
		public MirroredUpdateSiteAdapter(IURLEntry mirror) {
			super(mirror.getAnnotation(), mirror.getURL());
		}
	}
	
	class AggregateFilter implements IUpdateSearchFilter {
		private ArrayList filters;
		public void addFilter(IUpdateSearchFilter filter) {
			if (filters == null)
				filters = new ArrayList();
			if (filters.contains(filter) == false)
				filters.add(filter);
		}
		
		public void removeFilter(IUpdateSearchFilter filter) {
			if (filters == null)
				return;
			filters.remove(filter);
		}
	
		public boolean accept(IFeature match) {
			if (filters == null)
				return true;
			for (int i = 0; i < filters.size(); i++) {
				IUpdateSearchFilter filter = (IUpdateSearchFilter) filters.get(i);
				if (filter.accept(match) == false)
					return false;
			}
			return true;
		}
		
		public boolean accept(IFeatureReference match) {
			if (filters == null)
				return true;
			for (int i = 0; i < filters.size(); i++) {
				IUpdateSearchFilter filter = (IUpdateSearchFilter) filters.get(i);
				if (filter.accept(match) == false)
					return false;
			}
			return true;
		}
	}

	/**
	 * The constructor that accepts the search category and 
	 * scope objects.
	 * @param category the actual search pattern that should be applied
	 * @param scope a list of sites that need to be scanned during the search
	 */
	public UpdateSearchRequest(
		IUpdateSearchCategory category,
		UpdateSearchScope scope) {
		this.category = category;
		this.scope = scope;
	}
	/**
	 * Returns the search catagory used in this request.
	 * @return the search category
	 */
	
	public IUpdateSearchCategory getCategory() {
		return category;
	}
	
	/**
	 * Returns the scope of this search request.
	 * @return search scope
	 */
	
	public UpdateSearchScope getScope() {
		return scope;
	}
	/**
	 * Adds a filter to this request. This method does nothing
	 * if search is alrady in progress. 
	 * @param filter the filter 
	 * @see UpdateSearchRequest#removeFilter
	 */
	public void addFilter(IUpdateSearchFilter filter) {
		if (searchInProgress)
			return;
		aggregateFilter.addFilter(filter);
	}
	/**
	 * Removes the filter from this request. This method does
	 * nothing if search is alrady in progress.
	 * @param filter the filter to remove
	 * @see UpdateSearchRequest#addFilter
	 */

	public void removeFilter(IUpdateSearchFilter filter) {
		if (searchInProgress)
			return;
		aggregateFilter.removeFilter(filter);
	}

	/**
	 * Sets the scope object. It is possible to reuse the search request
	 * object by modifying the scope and re-running the search.
	 * @param scope the new search scope
	 */
	public void setScope(UpdateSearchScope scope) {
		this.scope = scope;
	}
	/**
	 * Tests whether this search request is current running.
	 * @return <samp>true</samp> if the search is currently running, <samp>false</samp> otherwise.
	 */
	public boolean isSearchInProgress() {
		return searchInProgress;
	}

	/**
	 * Runs the search using the category and scope configured into
	 * this request. As results arrive, they are passed to the
	 * search result collector object.
	 * @param collector matched features are passed to this object
	 * @param monitor used to track the search progress
	 * @throws CoreException
	 */
	public void performSearch(
		IUpdateSearchResultCollector collector,
		IProgressMonitor monitor)
		throws CoreException {
		ArrayList statusList = new ArrayList();

		searchInProgress = true;
		IUpdateSearchQuery[] queries = category.getQueries();
		IUpdateSearchSite[] candidates = scope.getSearchSites();
		URL updateMapURL = scope.getUpdateMapURL();
		boolean searchFeatureProvidedSites = scope.isFeatureProvidedSitesEnabled();

		if (!monitor.isCanceled()) {
			int nsearchsites = 0;
			for (int i = 0; i < queries.length; i++) {
				if (queries[i].getQuerySearchSite() != null)
					nsearchsites++;
			}
			int ntasks = nsearchsites + queries.length * candidates.length;
			if (updateMapURL!=null) ntasks++;

			monitor.beginTask(Policy.bind("UpdateSearchRequest.searching"), ntasks); //$NON-NLS-1$

			try {
				UpdatePolicy updatePolicy=null;
				if (updateMapURL!=null) {
					updatePolicy = new UpdatePolicy();
					IStatus status =UpdateUtils.loadUpdatePolicy(updatePolicy, updateMapURL, new SubProgressMonitor(monitor, 1));
					if (status != null)
						statusList.add(status);
				}
				for (int i = 0; i < queries.length; i++) {
					IUpdateSearchQuery query = queries[i];
					IQueryUpdateSiteAdapter qsite = query.getQuerySearchSite();
					// currently, the next conditional is only executed (qsite!=null) when
					// running an update search. 
					if (qsite != null && searchFeatureProvidedSites) {
						// check for mapping
						IUpdateSiteAdapter mappedSite = getMappedSite(updatePolicy, qsite);
						// when there is no mapped site the feature is not updatable
						if (mappedSite == null || mappedSite.getURL() == null)
							continue;
						SubProgressMonitor subMonitor =
							new SubProgressMonitor(monitor, 1);
						IStatus status =
							searchOneSite(
								mappedSite,
								null,
								query,
								collector,
								subMonitor,
								true);
						if (status != null)
							statusList.add(status);
						if (monitor.isCanceled())
							break;
					}
					for (int j = 0; j < candidates.length; j++) {
						if (monitor.isCanceled()) {
							break;
						}
						IUpdateSearchSite source = candidates[j];
						SubProgressMonitor subMonitor =
							new SubProgressMonitor(monitor, 1);
						IStatus status =
							searchOneSite(
								source,
								source.getCategoriesToSkip(),
								query,
								collector,
								subMonitor,
								true);
						if (status != null)
							statusList.add(status);
					}
					if (monitor.isCanceled())
						break;
				}
			} catch (CoreException e) {
				searchInProgress = false;
				monitor.done();
				throw e;
			}
		}
		searchInProgress = false;
		monitor.done();

		if (statusList.size() > 0) {
			IStatus[] children =
				(IStatus[]) statusList.toArray(new IStatus[statusList.size()]);
			MultiStatus multiStatus =
				new MultiStatus(
					"org.eclipse.update.core", //$NON-NLS-1$
					ISite.SITE_ACCESS_EXCEPTION,
					children,
					Policy.bind("Search.networkProblems"), //$NON-NLS-1$
					null);
			throw new CoreException(multiStatus);
		}
	}


/*
 * See if this query site adapter is mapped in the map file
 * to a different URL.
 */
	private IUpdateSiteAdapter getMappedSite(UpdatePolicy policy, IQueryUpdateSiteAdapter qsite) {
		if (policy!=null && policy.isLoaded()) {
			IUpdateSiteAdapter mappedSite = policy.getMappedSite(qsite.getMappingId());
			if (mappedSite!=null) 
				return mappedSite;
			else // no match - use original site if fallback allowed, or nothing.
				return policy.isFallbackAllowed()? qsite : null;
		}
		return qsite;
	}

/*
 * Search one site using the provided query.
 */
	private IStatus searchOneSite(
		IUpdateSiteAdapter siteAdapter,
		String[] categoriesToSkip,
		IUpdateSearchQuery query,
		IUpdateSearchResultCollector collector,
		SubProgressMonitor monitor,
		boolean checkMirrors)
		throws CoreException {
		String text = Policy.bind("UpdateSearchRequest.contacting") + siteAdapter.getLabel() + "..."; //$NON-NLS-1$ //$NON-NLS-2$
		monitor.subTask(text);
		monitor.beginTask("", 10); //$NON-NLS-1$
		URL siteURL = siteAdapter.getURL();

		ISite site;
		try {
			site =
				SiteManager.getSite(
					siteURL,
					new SubProgressMonitor(monitor, 1));
			
			// If frozen connection was canceled, there will be no site.
			if (site == null) {
				monitor.worked(9);
				return null;
			}
			
			// prompt the user to pick up a site
			if ((collector instanceof IUpdateSearchResultCollectorFromMirror)
				&& (site instanceof ISiteWithMirrors)) {
				IURLEntry mirror = ((IUpdateSearchResultCollectorFromMirror)collector).getMirror((ISiteWithMirrors)site, siteAdapter.getLabel());
				if (mirror != null) 
					return searchOneSite(new MirroredUpdateSiteAdapter(mirror), categoriesToSkip, query, collector, new SubProgressMonitor(monitor,1), false);
			}
		} catch (CoreException e) {
			// Test the exception. If the exception is
			// due to the site connection problems,
			// allow the search to move on to 
			// the next site. Otherwise,
			// rethrow the exception, causing the search
			// to terminate.
			IStatus status = e.getStatus();
			if (status == null)
//				|| status.getCode() != ISite.SITE_ACCESS_EXCEPTION)
				throw e;
			monitor.worked(10);
			return status;
		}

		text = Policy.bind("UpdateSearchRequest.checking") + siteAdapter.getLabel() + "..."; //$NON-NLS-1$ //$NON-NLS-2$
		monitor.getWrappedProgressMonitor().subTask(text);

		query.run(
			site,
			categoriesToSkip,
			aggregateFilter,
			collector,
			new SubProgressMonitor(monitor, 9));
		return null;
	}
}
