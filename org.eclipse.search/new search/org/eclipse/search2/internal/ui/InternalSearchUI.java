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
package org.eclipse.search2.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.SearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;
import org.eclipse.search.internal.ui.util.ExceptionHandler;

import org.eclipse.search2.internal.ui.text.PositionTracker;

public class InternalSearchUI {
	private static final int HISTORY_COUNT= 10;
	
	//The shared instance.
	private static InternalSearchUI plugin;
	private HashMap fSearchJobs;
	
	private QueryManager fSearchResultsManager;
	private PositionTracker fPositionTracker;

	public static final Object FAMILY_SEARCH = new Object();
	

	private class SearchJobRecord {
		public ISearchQuery fQuery;
		public Job fJob;
		public boolean fBackground;
		public boolean fIsRunning;

		SearchJobRecord(ISearchQuery job, boolean bg) {
			fQuery= job;
			fBackground= bg;
			fIsRunning= false;
		}
	}
	

	private class InternalSearchJob extends Job {
		SearchJobRecord fSearchJobRecord;
		public InternalSearchJob(SearchJobRecord sjr) {
			super(sjr.fQuery.getLabel());
			fSearchJobRecord= sjr;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			ThrottlingProgressMonitor realMonitor= new ThrottlingProgressMonitor(monitor, 0.5f);
			fSearchJobRecord.fJob= this;
			searchJobStarted(fSearchJobRecord);
			IStatus status= null;
			try{
				status= fSearchJobRecord.fQuery.run(realMonitor); 
			} finally {
				searchJobFinished(fSearchJobRecord);
			}
			fSearchJobRecord.fJob= null;
			return status;
		}
		public boolean belongsTo(Object family) {
			return family == InternalSearchUI.FAMILY_SEARCH;
		}

	}

	private void searchJobStarted(SearchJobRecord record) {
		record.fIsRunning= true;
		getSearchManager().queryStarting(record.fQuery);
	}
	
	private void searchJobFinished(SearchJobRecord record) {
		record.fIsRunning= false;
		getSearchManager().queryFinished(record.fQuery);
	}
	
	/**
	 * The constructor.
	 */
	public InternalSearchUI() {
		plugin= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new QueryManager();
		fPositionTracker= new PositionTracker();
	}

	/**
	 * Returns the shared instance.
	 */
	public static InternalSearchUI getInstance() {
		if (plugin ==null)
			plugin= new InternalSearchUI();
		return plugin;
	}

	public ISearchResultViewPart getSearchView() {
		return (ISearchResultViewPart) SearchPlugin.getActivePage().findView(SearchUI.SEARCH_VIEW_ID);
	}

	public boolean runSearchInBackground(ISearchQuery query) {
		Assert.isTrue(fSearchJobs.get(query) == null);
		
		addQuery(query);
		
		if (isQueryRunning(query))
			return false;
		SearchJobRecord sjr= new SearchJobRecord(query, true);
		fSearchJobs.put(query, sjr);
		doRunSearchInBackground(sjr);
		return true;
	}

	public boolean isQueryRunning(ISearchQuery query) {
		SearchJobRecord sjr= (SearchJobRecord) fSearchJobs.get(query);
		return sjr != null && sjr.fIsRunning;
	}

	public IStatus runSearchInForeground(IRunnableContext context, final ISearchQuery query) {
		Assert.isTrue(fSearchJobs.get(query) == null);
		addQuery(query);
		SearchJobRecord sjr= new SearchJobRecord(query, false);
		fSearchJobs.put(query, sjr);
		
		return doRunSearchInForeground(sjr, context);
	}
	
	private void doRunSearchInBackground(SearchJobRecord jobRecord) {
		if (jobRecord.fJob == null) {
			jobRecord.fJob= new InternalSearchJob(jobRecord);
			jobRecord.fJob.setPriority(Job.BUILD);	
			configureJob(jobRecord.fJob);
		}
		getProgressService().schedule(jobRecord.fJob, 0, true);
	}

	private void configureJob(Job job) {
		job.setUser(true);
		try {
			URL install= SearchPlugin.getDefault().getDescriptor().getInstallURL();
			URL icon;
			icon= new URL(install, "icons/full/eview16/searchres.gif"); //$NON-NLS-1$
			job.setProperty(new QualifiedName("org.eclipse.ui.workbench.progress", "icon"), icon);  //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MalformedURLException e) {
			// don't set any icon
		}
	}

	public IWorkbenchSiteProgressService getProgressService() {
		IWorkbenchSiteProgressService service = null;
		Object siteService =
			getSearchView().getSite().getAdapter(IWorkbenchSiteProgressService.class);
		if(siteService != null)
			service = (IWorkbenchSiteProgressService) siteService;
		return service;
	}

	public boolean runAgain(ISearchQuery job) {
		final SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
		if (rec == null)
			return false;
		if (rec.fBackground) {
			doRunSearchInBackground(rec);
		} else {
			ProgressMonitorDialog pmd= new ProgressMonitorDialog(getSearchView().getSite().getShell());
			doRunSearchInForeground(rec, pmd);
		}
		return true;
	}
	
	private IStatus doRunSearchInForeground(final SearchJobRecord rec, IRunnableContext context) {
		final IStatus[] temp= new IStatus[1];
		if (context == null)
			context= getContext();
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					searchJobStarted(rec);
					try {
						temp[0]= rec.fQuery.run(monitor);
					} finally {
						searchJobFinished(rec);
					}
				}
			});
		} catch (InvocationTargetException e) {
			// this will not happen.
		} catch (InterruptedException e) {
			// this will not happen
		}
		return temp[0];
	}

	/**
	 * @return
	 */
	private IRunnableContext getContext() {
		return new ProgressMonitorDialog(null);
	}

	public void shutdown() {
		Iterator jobRecs= fSearchJobs.values().iterator();
		while (jobRecs.hasNext()) {
			SearchJobRecord element= (SearchJobRecord) jobRecs.next();
			if (element.fJob != null)
				element.fJob.cancel();
		}
		fPositionTracker.dispose();
	}

	public void cancelSearch(ISearchQuery job) {
		SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
		if (rec != null && rec.fJob != null)
			rec.fJob.cancel();
	}

	public ISearchResultViewPart activateSearchView() {
		String defaultPerspectiveId= SearchUI.getDefaultPerspectiveId();
		if (defaultPerspectiveId != null) {
			IWorkbenchWindow window= window= SearchPlugin.getActiveWorkbenchWindow();
			if (window != null && window.getShell() != null && !window.getShell().isDisposed()) {
				try {
					PlatformUI.getWorkbench().showPerspective(defaultPerspectiveId, window);
				} catch (WorkbenchException ex) {
					// show view in current perspective
				}
			}
		}

		try {
			ISearchResultViewPart viewPart= (ISearchResultViewPart) SearchPlugin.getActivePage().findView(SearchUI.SEARCH_VIEW_ID);
			if (viewPart == null || SearchPreferencePage.isViewBroughtToFront()) {
				viewPart= (ISearchResultViewPart) SearchPlugin.getActivePage().showView(SearchUI.SEARCH_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			}
			return viewPart;
		} catch (PartInitException ex) {
			ExceptionHandler.handle(ex, SearchMessages.getString("Search.Error.openResultView.title"), SearchMessages.getString("Search.Error.openResultView.message")); //$NON-NLS-2$ //$NON-NLS-1$
		}	
		return null;
	}

	public QueryManager getSearchManager() {
		return fSearchResultsManager;
	}

	public PositionTracker getPositionTracker() {
		return fPositionTracker;
	}
	
	public void addQueryListener(IQueryListener l) {
		getSearchManager().addQueryListener(l);
	}
	public ISearchQuery[] getQueries() {
		return getSearchManager().getQueries();
	}
	public void removeQueryListener(IQueryListener l) {
		getSearchManager().removeQueryListener(l);
	}

	public void removeQuery(ISearchQuery query) {
		cancelSearch(query);
		getSearchManager().removeQuery(query);
		fSearchJobs.remove(query);
	}

	public void addQuery(ISearchQuery query) {
		while (getSearchManager().getQueries().length >= HISTORY_COUNT) {
			removeQuery(getSearchManager().getOldestQuery());
		}
		getSearchManager().addQuery(query);
	}

	public void removeAllQueries() {
		fSearchJobs.clear();
		getSearchManager().removeAll();
	}
}
