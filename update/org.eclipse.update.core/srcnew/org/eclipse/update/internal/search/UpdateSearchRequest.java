/*
 * Created on May 22, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.eclipse.update.internal.search;

import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.*;
import org.eclipse.update.core.*;
import org.eclipse.update.internal.core.UpdateManagerUtils;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class UpdateSearchRequest {
	private IUpdateSearchCategory category;
	private UpdateSearchScope scope;
	private boolean searchInProgress = false;
	private boolean filterEnvironment = true;
	
	public UpdateSearchRequest(
		IUpdateSearchCategory category,
		UpdateSearchScope scope) {
		this.category = category;
		this.scope = scope;
	}
	
	public void setFilterEnvironment(boolean value) {
		this.filterEnvironment = value;
	}
	
	public boolean getFilterEnvironment() {
		return filterEnvironment;
	}

	public boolean select(IFeature match) {
		return !getFilterEnvironment() || UpdateManagerUtils.isValidEnvironment(match);
	}

	public void setScope(UpdateSearchScope scope) {
		this.scope = scope;
	}

	public boolean isSearchInProgress() {
		return searchInProgress;
	}

	public void performSearch(
		IUpdateSearchResultCollector collector,
		IProgressMonitor monitor)
		throws CoreException {
		ArrayList statusList = new ArrayList();

		searchInProgress = true;
		IUpdateSearchQuery[] queries = category.getQueries();
		IUpdateSearchSite[] candidates = scope.getSearchSites();

		if (!monitor.isCanceled()) {
			int nsearchsites = 0;
			for (int i = 0; i < queries.length; i++) {
				if (queries[i].getQuerySearchSite() != null)
					nsearchsites++;
			}
			int ntasks = nsearchsites + queries.length * candidates.length;

			monitor.beginTask("Searching...", ntasks);

			try {
				for (int i = 0; i < queries.length; i++) {
					IUpdateSearchQuery query = queries[i];
					IUpdateSiteAdapter site = query.getQuerySearchSite();
					if (site != null) {
						SubProgressMonitor subMonitor =
							new SubProgressMonitor(monitor, 1);
						IStatus status =
							searchOneSite(
								site,
								null,
								query,
								collector,
								subMonitor);
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
								subMonitor);
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
					"org.eclipse.update.core",
					ISite.SITE_ACCESS_EXCEPTION,
					children,
					"Search.networkProblems",
					null);
			throw new CoreException(multiStatus);
		}
	}

	private IStatus searchOneSite(
		IUpdateSiteAdapter siteAdapter,
		String[] categoriesToSkip,
		IUpdateSearchQuery query,
		IUpdateSearchResultCollector collector,
		SubProgressMonitor monitor)
		throws CoreException {
		String text = "Contacting " + siteAdapter.getLabel() + "...";
		monitor.subTask(text);
		monitor.beginTask("", 10);
		URL siteURL = siteAdapter.getURL();

		ISite site;
		try {
			site =
				SiteManager.getSite(
					siteURL,
					new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			// Test the exception. If the exception is
			// due to the site connection problems,
			// allow the search to move on to 
			// the next site. Otherwise,
			// rethrow the exception, causing the search
			// to terminate.
			IStatus status = e.getStatus();
			if (status == null
				|| status.getCode() != ISite.SITE_ACCESS_EXCEPTION)
				throw e;
			monitor.worked(10);
			return status;
		}
		// If frozen connection was canceled, there will be no site.
		if (site == null) {
			monitor.worked(9);
			return null;
		}

		text = "Checking " + siteAdapter.getLabel() + "...";
		monitor.getWrappedProgressMonitor().subTask(text);

		query.run(
			site,
			categoriesToSkip,
			collector,
			new SubProgressMonitor(monitor, 9));
		return null;
	}
}