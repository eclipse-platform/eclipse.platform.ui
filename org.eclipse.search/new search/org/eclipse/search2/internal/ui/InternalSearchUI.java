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
package org.eclipse.search2.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.internal.ui.SearchPreferencePage;
import org.eclipse.search.internal.ui.util.ExceptionHandler;
import org.eclipse.search.ui.IQueryListener;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResultViewPart;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search2.internal.ui.text.PositionTracker;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

public class InternalSearchUI {
	private static final int HISTORY_COUNT= 10;
	
	//The shared instance.
	private static InternalSearchUI fgInstance;
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
		fgInstance= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new QueryManager();
		fPositionTracker= new PositionTracker();
		URL iconURL= SearchPlugin.getDefault().getBundle().getEntry("icons/full/eview16/searchres.gif"); //$NON-NLS-1$
		ImageDescriptor image= ImageDescriptor.createFromURL(iconURL);
		PlatformUI.getWorkbench().getProgressService().registerIconForFamily(image, FAMILY_SEARCH);
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
		}
		jobRecord.fJob.setUser(true);
		IWorkbenchSiteProgressService service= getProgressService();
		if (service != null)
			service.schedule(jobRecord.fJob, 0, true);
		else 
			jobRecord.fJob.schedule();
	}

	public IWorkbenchSiteProgressService getProgressService() {
		ISearchResultViewPart view= getSearchView();
		if (view != null) {
			IWorkbenchPartSite site= view.getSite();
			if (site != null)
				return (IWorkbenchSiteProgressService)view.getSite().getAdapter(IWorkbenchSiteProgressService.class);
		}
		return null;
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
		if (context == null)
			context= getContext();
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					searchJobStarted(rec);
					try { 
						IStatus status= rec.fQuery.run(monitor);
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
			return new Status(IStatus.ERROR, SearchPlugin.getID(), 0, SearchMessages.getString("InternalSearchUI.error.unexpected"), innerException);  //$NON-NLS-1$
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS;
		}
		return Status.OK_STATUS;
	}

	private IRunnableContext getContext() {
		return new ProgressMonitorDialog(null);
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
		String defaultPerspectiveId= NewSearchUI.getDefaultPerspectiveId();
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
			ISearchResultViewPart viewPart= (ISearchResultViewPart) SearchPlugin.getActivePage().findView(NewSearchUI.SEARCH_VIEW_ID);
			if (viewPart == null || SearchPreferencePage.isViewBroughtToFront()) {
				viewPart= (ISearchResultViewPart) SearchPlugin.getActivePage().showView(NewSearchUI.SEARCH_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
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
		for (Iterator queries= fSearchJobs.keySet().iterator(); queries.hasNext();) {
			ISearchQuery query= (ISearchQuery) queries.next();
			cancelSearch(query);
		}
		fSearchJobs.clear();
		getSearchManager().removeAll();
	}
}
