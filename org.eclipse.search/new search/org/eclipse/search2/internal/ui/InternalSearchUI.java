/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPluginImages;

import org.eclipse.search2.internal.ui.text.PositionTracker;

public class InternalSearchUI {
	private static final int HISTORY_COUNT= 10;
	
	//The shared instance.
	private static InternalSearchUI fgInstance;
	
	// contains all running jobs
	private HashMap fSearchJobs;
	
	private QueryManager fSearchResultsManager;
	private PositionTracker fPositionTracker;
	
	private SearchViewManager fSearchViewManager;

	public static final Object FAMILY_SEARCH = new Object();

	private class SearchJobRecord {
		public ISearchQuery query;
		public Job job;
		public boolean background;
		public boolean isRunning;

		public SearchJobRecord(ISearchQuery job, boolean bg) {
			this.query= job;
			this.background= bg;
			this.isRunning= false;
			this.job= null;
		}
	}
	

	private class InternalSearchJob extends Job {
		
		private SearchJobRecord fSearchJobRecord;
		
		public InternalSearchJob(SearchJobRecord sjr) {
			super(sjr.query.getLabel());
			
			fSearchJobRecord= sjr;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			ThrottlingProgressMonitor realMonitor= new ThrottlingProgressMonitor(monitor, 0.5f);
			fSearchJobRecord.job= this;
			searchJobStarted(fSearchJobRecord);
			IStatus status= null;
			try{
				status= fSearchJobRecord.query.run(realMonitor); 
			} finally {
				searchJobFinished(fSearchJobRecord);
			}
			fSearchJobRecord.job= null;
			return status;
		}
		public boolean belongsTo(Object family) {
			return family == InternalSearchUI.FAMILY_SEARCH;
		}

	}

	private void searchJobStarted(SearchJobRecord record) {
		record.isRunning= true;
		getSearchManager().queryStarting(record.query);
	}
	
	private void searchJobFinished(SearchJobRecord record) {
		record.isRunning= false;
		fSearchJobs.remove(record);
		getSearchManager().queryFinished(record.query);
	}
	
	/**
	 * The constructor.
	 */
	public InternalSearchUI() {
		fgInstance= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new QueryManager();
		fPositionTracker= new PositionTracker();
		
		fSearchViewManager= new SearchViewManager(fSearchResultsManager);
		
		PlatformUI.getWorkbench().getProgressService().registerIconForFamily(SearchPluginImages.DESC_VIEW_SEARCHRES, FAMILY_SEARCH);
	}

	/**
	 * @return returns the shared instance.
	 */
	public static InternalSearchUI getInstance() {
		if (fgInstance ==null)
			fgInstance= new InternalSearchUI();
		return fgInstance;
	}

	public ISearchResultViewPart getSearchView() {
		return (ISearchResultViewPart) SearchPlugin.getActivePage().findView(NewSearchUI.SEARCH_VIEW_ID);
	}

	private IWorkbenchSiteProgressService getProgressService() {
		ISearchResultViewPart view= getSearchView();
		if (view != null) {
			IWorkbenchPartSite site= view.getSite();
			if (site != null)
				return (IWorkbenchSiteProgressService)view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		}
		return null;
	}
	
	public boolean runSearchInBackground(ISearchQuery query) {
		if (isQueryRunning(query))
			return false;
		
		// prepare view
		getSearchViewManager().activateSearchView(true);
				
		addQuery(query);

		SearchJobRecord sjr= new SearchJobRecord(query, true);
		fSearchJobs.put(query, sjr);
				
		Job job= new InternalSearchJob(sjr);
		job.setPriority(Job.BUILD);	
		job.setUser(true);

		IWorkbenchSiteProgressService service= getProgressService();
		if (service != null) {
			service.schedule(job, 0, true);
		} else {
			job.schedule();
		}
		
		return true;
	}

	public boolean isQueryRunning(ISearchQuery query) {
		SearchJobRecord sjr= (SearchJobRecord) fSearchJobs.get(query);
		return sjr != null && sjr.isRunning;
	}

	public IStatus runSearchInForeground(IRunnableContext context, final ISearchQuery query) {
		if (isQueryRunning(query)) {
			return Status.CANCEL_STATUS;
		}
		
		// prepare view
		getSearchViewManager().activateSearchView(true);

		addQuery(query);
		
		SearchJobRecord sjr= new SearchJobRecord(query, false);
		fSearchJobs.put(query, sjr);
		
		if (context == null)
			context= new ProgressMonitorDialog(null);
		
		return doRunSearchInForeground(sjr, context);
	}
	
	private IStatus doRunSearchInForeground(final SearchJobRecord rec, IRunnableContext context) {
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					searchJobStarted(rec);
					try { 
						IStatus status= rec.query.run(monitor);
						if (status.matches(IStatus.CANCEL)) {
							throw new InterruptedException();
						}
						if (!status.isOK()) {
							throw new InvocationTargetException(new CoreException(status));
						}
					} catch (OperationCanceledException e) {
						throw new InterruptedException();
					} finally {
						searchJobFinished(rec);
					}
				}
			});
		} catch (InvocationTargetException e) {
			Throwable innerException= e.getTargetException();
			if (innerException instanceof CoreException) {
				return ((CoreException) innerException).getStatus();
			}
			return new Status(IStatus.ERROR, SearchPlugin.getID(), 0, SearchMessages.InternalSearchUI_error_unexpected, innerException);  
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	public static void shutdown() {
		InternalSearchUI instance= fgInstance;
		if (instance != null) 
			instance.doShutdown();
	}
	
	private void doShutdown() {
		Iterator jobRecs= fSearchJobs.values().iterator();
		while (jobRecs.hasNext()) {
			SearchJobRecord element= (SearchJobRecord) jobRecs.next();
			if (element.job != null)
				element.job.cancel();
		}
		fPositionTracker.dispose();
		
		fSearchViewManager.dispose(fSearchResultsManager);
		
	}

	public void cancelSearch(ISearchQuery job) {
		SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
		if (rec != null && rec.job != null)
			rec.job.cancel();
	}



	public QueryManager getSearchManager() {
		return fSearchResultsManager;
	}
	
	public SearchViewManager getSearchViewManager() {
		return fSearchViewManager;
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
		for (Iterator queries= fSearchJobs.keySet().iterator(); queries.hasNext();) {
			ISearchQuery query= (ISearchQuery) queries.next();
			cancelSearch(query);
		}
		fSearchJobs.clear();
		getSearchManager().removeAll();
	}
}
