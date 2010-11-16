/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.base.HelpBasePlugin;
import org.eclipse.help.internal.base.remote.RemoteHelp;
import org.eclipse.help.internal.base.remote.RemoteSearchManager;
import org.eclipse.help.internal.search.federated.FederatedSearchEntry;
import org.eclipse.help.internal.search.federated.FederatedSearchJob;

/*
 * Manages both local and remote searching, as well as merging of results.
 */
public class SearchManager {

	private LocalSearchManager localManager = new LocalSearchManager();
	private RemoteSearchManager remoteManager = new RemoteSearchManager();
	
	private class SearchState {

		public IProgressMonitor localMonitor;
		public IProgressMonitor remoteMonitor;

		public ISearchQuery searchQuery;
		public BufferedSearchHitCollector bufferedCollector = new BufferedSearchHitCollector();

		public Job localSearchJob;
		public Job remoteSearchJob;

		public SearchState() {
			/*
			 * We use these jobs to perform the local and remote searches in parallel in the
			 * background.
			 */
			localSearchJob = new Job("localSearchJob") { //$NON-NLS-1$

				protected IStatus run(IProgressMonitor monitor) {
					localManager.search(searchQuery, bufferedCollector, localMonitor);
					return Status.OK_STATUS;
				}
			};
			remoteSearchJob = new Job("remoteSearchJob") { //$NON-NLS-1$

				protected IStatus run(IProgressMonitor monitor) {
					remoteManager.search(searchQuery, bufferedCollector, remoteMonitor);
					return Status.OK_STATUS;
				}
			};
			localSearchJob.setSystem(true);
			remoteSearchJob.setSystem(true);
		}
	}

	/*
	 * Constructs a new SearchManager.
	 */
	public SearchManager() {
		
	}
	
	/*
	 * Perform the given search both locally and remotely if configured.
	 */
	public void search(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		if (RemoteHelp.isEnabled()) {
			searchLocalAndRemote(searchQuery, collector, pm);
		}
		else {
			searchLocal(searchQuery, collector, pm);
		}
	}

	/*
	 * Perform the given search locally only.
	 */
	public void searchLocal(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		localManager.search(searchQuery, collector, pm);
	}

	/*
	 * Perform the given search both locally and remotely.
	 */
	public void searchLocalAndRemote(ISearchQuery searchQuery, ISearchHitCollector collector, IProgressMonitor pm)
			throws QueryTooComplexException {
		SearchState state = new SearchState();
		state.searchQuery = searchQuery;
		
		pm.beginTask("", 100); //$NON-NLS-1$
		
		// allocate half of the progress bar for each
		state.localMonitor = new SubProgressMonitor(pm, 50, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
		state.remoteMonitor = new SubProgressMonitor(pm, 50, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
		
		// start both searches in parallel
		state.localSearchJob.schedule();
		state.remoteSearchJob.schedule();
		
		// wait until finished
		try {
			state.localSearchJob.join();
			state.remoteSearchJob.join();
		}
		catch (InterruptedException e) {
			String msg = "Unexpected InterruptedException while waiting for help search jobs to finish"; //$NON-NLS-1$
			HelpBasePlugin.logError(msg, e);
		}

		// results are in; send them off to the collector
		state.bufferedCollector.flush(collector);
		pm.done();
	}
	
	/**
	 * Performs the federated search.
	 */
	public void search(String expression, FederatedSearchEntry[] entries) {
		for (int i = 0; i < entries.length; i++) {
			FederatedSearchJob job = new FederatedSearchJob(expression, entries[i]);
			job.schedule();
		}
	}
	
	/*
	 * Returns the manager responsible for handling local searching.
	 */
	public LocalSearchManager getLocalSearchManager() {
		return localManager;
	}

	/*
	 * Returns the manager responsible for handling remote searching.
	 */
	public RemoteSearchManager getRemoteSearchManager() {
		return remoteManager;
	}

	/*
	 * Performs any necessary cleanup (workbench is shutting down).
	 */
	public void close() {
		localManager.close();
	}
	
	/*
	 * Buffers hits, and only sends them off to the wrapped collector
	 * when flush() is called.
	 */
	private class BufferedSearchHitCollector implements ISearchHitCollector {
		private Set allHits = new HashSet();
		private String wordsSearched = null;
		
		/* (non-Javadoc)
		 * @see org.eclipse.help.internal.search.ISearchHitCollector#addHits(java.util.List, java.lang.String)
		 */
		public void addHits(List hits, String wordsSearched) {
			if (wordsSearched != null) {
				this.wordsSearched = wordsSearched;
			}
			allHits.addAll(hits);
		}
		
		/*
		 * Send all the buffered hits to the underlying collector,
		 * and reset the buffers.
		 */
		public void flush(ISearchHitCollector collector) {
			// sort by score
			List hitsList = new ArrayList(allHits);
			Collections.sort(hitsList);
			collector.addHits(hitsList, wordsSearched);
			allHits.clear();
			wordsSearched = null;
		}

		public void addQTCException(QueryTooComplexException exception) throws QueryTooComplexException {
			throw exception;
		}
	}
}
