package org.eclipse.search2.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.search.ui.ISearchJob;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultManager;

import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search2.internal.ui.text.PositionTracker;

public class InternalSearchUI {
	//The shared instance.
	private static InternalSearchUI plugin;
	private HashMap fSearchJobs;
	
	private ISearchResultManager fSearchResultsManager;
	private PositionTracker fPositionTracker;

	private static final String SEARCH_VIEW_ID= "org.eclipse.search.ui.views.SearchView"; //$NON-NLS-1$

	private class SearchJobRecord {
		public ISearchJob fSearchJob;
		public Job fJob;
		public boolean fBackground;

		SearchJobRecord(ISearchJob job, boolean bg) {
			fSearchJob= job;
			fBackground= bg;
		}
	}
	
	private static class InternalSearchJob extends Job {
		SearchJobRecord fSearchJobRecord;
		public InternalSearchJob(SearchJobRecord sjr) {
			super(sjr.fSearchJob.getName());
			fSearchJobRecord= sjr;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			fSearchJobRecord.fJob= this;
			IStatus status= fSearchJobRecord.fSearchJob.run(monitor);
			fSearchJobRecord.fJob= null;
			return status;
		}
		
	}

	/**
	 * The constructor.
	 */
	public InternalSearchUI() {
		plugin= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new SearchResultsManager();
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

	public IViewPart getSearchView() {
		return SearchPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(SEARCH_VIEW_ID);
	}

	public boolean runSearchInBackground(ISearchResult search, final ISearchJob job) {
		if (search.isRunning())
			return false;
		SearchJobRecord sjr= new SearchJobRecord(job, true);
		fSearchJobs.put(search, sjr);
		doRunSearchInBackground(sjr);
		return true;
	}

	public boolean runSearchInForeground(IRunnableContext context, ISearchResult search, final ISearchJob job) {
		if (search.isRunning())
			return false;
		SearchJobRecord sjr= new SearchJobRecord(job, false);
		fSearchJobs.put(search, sjr);
		
		doRunSearchInForeground(sjr, context);
		return true;
	}
	
	private void doRunSearchInBackground(SearchJobRecord jobRecord) {
		if (jobRecord.fJob == null) {
			jobRecord.fJob= new InternalSearchJob(jobRecord);
			jobRecord.fJob.setPriority(Job.BUILD);
		}
		jobRecord.fJob.schedule();
	}

	public boolean runAgain(ISearchResult search) {
		final SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(search);
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

	private void doRunSearchInForeground(final SearchJobRecord rec, IRunnableContext context) {
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					rec.fSearchJob.run(monitor);
				}
			});
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// means we were cancelled.
		}
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

	public void cancelSearch(ISearchResult search) {
		SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(search);
		if (rec != null && rec.fJob != null)
			rec.fJob.cancel();
	}

	public void activateSearchView() {
		try {
			SearchPlugin.getActiveWorkbenchWindow().getActivePage().showView(SEARCH_VIEW_ID);
		} catch (PartInitException ex) {
			// TODO Auto-generated catch block
		}
	}

	public ISearchResultManager getSearchManager() {
		return fSearchResultsManager;
	}

	public PositionTracker getPositionTracker() {
		return fPositionTracker;
	}

}
