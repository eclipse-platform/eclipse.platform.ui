package org.eclipse.search2.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.util.Assert;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

import org.eclipse.search.ui.ISearchQuery;
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
	private HashSet fJobListeners;

	private static final String SEARCH_VIEW_ID= "org.eclipse.search.ui.views.SearchView"; //$NON-NLS-1$

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
		ISearchResult fResult;
		public InternalSearchJob(SearchJobRecord sjr, ISearchResult result) {
			super(sjr.fQuery.getName());
			fSearchJobRecord= sjr;
			fResult= result;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			fSearchJobRecord.fJob= this;
			searchJobStarted(fSearchJobRecord);
			IStatus status= null;
			try{
				status= fSearchJobRecord.fQuery.run(monitor, fResult);
			} finally {
				searchJobFinished(fSearchJobRecord);
			}
			fSearchJobRecord.fJob= null;
			return status;
		}


	}

	private void searchJobStarted(SearchJobRecord record) {
		record.fIsRunning= true;
		HashSet clonedSet= (HashSet) fJobListeners.clone();
		for (Iterator listeners= clonedSet.iterator(); listeners.hasNext();) {
			ISearchQueryListener listener= (ISearchQueryListener) listeners.next();
			listener.searchQueryStarted(record.fQuery);
		}
	}
	
	private void searchJobFinished(SearchJobRecord record) {
		record.fIsRunning= false;
		HashSet clonedSet= (HashSet) fJobListeners.clone();
		for (Iterator listeners= clonedSet.iterator(); listeners.hasNext();) {
			ISearchQueryListener listener= (ISearchQueryListener) listeners.next();
			listener.searchQueryFinished(record.fQuery);
		}
	}
	
	void addSearchQueryListener(ISearchQueryListener l) {
		fJobListeners.add(l);
	}
	
	void removeSearchQueryListener(ISearchQueryListener l) {
		fJobListeners.remove(l);
	}
	/**
	 * The constructor.
	 */
	public InternalSearchUI() {
		plugin= this;
		fSearchJobs= new HashMap();
		fSearchResultsManager= new SearchResultsManager();
		fPositionTracker= new PositionTracker();
		fJobListeners= new HashSet();
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

	public boolean runSearchInBackground(ISearchQuery job, ISearchResult result) {
		Assert.isTrue(fSearchJobs.get(job) == null);
		
		getSearchManager().addSearchResult(result);
		
		if (isQueryRunning(job))
			return false;
		SearchJobRecord sjr= new SearchJobRecord(job, true);
		fSearchJobs.put(job, sjr);
		doRunSearchInBackground(sjr, result);
		return true;
	}

	boolean isQueryRunning(ISearchQuery job) {
		SearchJobRecord sjr= (SearchJobRecord) fSearchJobs.get(job);
		return sjr != null && sjr.fIsRunning;
	}

	public boolean runSearchInForeground(IRunnableContext context, final ISearchQuery job, ISearchResult result) {
		Assert.isTrue(fSearchJobs.get(job) == null);
		getSearchManager().addSearchResult(result);
		if (isQueryRunning(job))
			return false;
		SearchJobRecord sjr= new SearchJobRecord(job, false);
		fSearchJobs.put(job, sjr);
		
		doRunSearchInForeground(sjr, result, context);
		return true;
	}
	
	private void doRunSearchInBackground(SearchJobRecord jobRecord, ISearchResult result) {
		if (jobRecord.fJob == null) {
			jobRecord.fJob= new InternalSearchJob(jobRecord, result);
			jobRecord.fJob.setPriority(Job.BUILD);
		}
		jobRecord.fJob.schedule();
	}

	public boolean runAgain(ISearchQuery job, ISearchResult result) {
		final SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
		if (rec == null)
			return false;
		if (rec.fBackground) {
			doRunSearchInBackground(rec, result);
		} else {
			ProgressMonitorDialog pmd= new ProgressMonitorDialog(getSearchView().getSite().getShell());
			doRunSearchInForeground(rec, result, pmd);
		}
		return true;
	}
	
	private void doRunSearchInForeground(final SearchJobRecord rec, final ISearchResult result, IRunnableContext context) {
		try {
			context.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					searchJobStarted(rec);
					try {
						rec.fQuery.run(monitor, result);
					} finally {
						searchJobFinished(rec);
					}
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

	public void cancelSearch(ISearchQuery job) {
		SearchJobRecord rec= (SearchJobRecord) fSearchJobs.get(job);
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
