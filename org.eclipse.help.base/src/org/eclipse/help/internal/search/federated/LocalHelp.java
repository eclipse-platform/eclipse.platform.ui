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
package org.eclipse.help.internal.search.federated;

import java.util.ArrayList;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.help.search.*;

/**
 * Local Help search engine participant in the federated search.
 */
public class LocalHelp implements ISearchEngine2 {
	private static final int MAX_HITS = 500;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.help.internal.search.federated.ISearchEngine#run(java.lang.String,
	 *      org.eclipse.help.internal.search.ISearchScope,
	 *      org.eclipse.help.internal.search.federated.ISearchEngineResultCollector,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void run(String query, ISearchScope scope,
			final ISearchEngineResultCollector collector,
			IProgressMonitor monitor) throws CoreException {

		SearchQuery searchQuery = new SearchQuery();
		searchQuery.setSearchWord(query);
		WorkingSet[] workingSets = null;
		LocalHelpScope localScope = (LocalHelpScope) scope;
		if (localScope.getWorkingSet() != null)
			workingSets = new WorkingSet[] { localScope.getWorkingSet() };
		SearchResults localResults = new SearchResults(workingSets, MAX_HITS,
				Platform.getNL());
		// If the indexer has been started and is currently running,
		// wait for it to finish.
		try {
			Job.getJobManager().join(IndexerJob.FAMILY, monitor);
		} catch (InterruptedException e) {
		}
		BaseHelpSystem.getSearchManager().search(searchQuery, localResults,
				monitor);
		postResults(localResults, collector, localScope.getCapabilityFiltered());
	}

	private void postResults(SearchResults results,
			ISearchEngineResultCollector collector, boolean activityFiltering) {
		if (results.getException() != null) {
			collector.error(new Status(IStatus.ERROR, HelpBasePlugin.PLUGIN_ID, 
					HelpBaseResources.HelpSearch_QueryTooComplex, results.getException()));
		}
		SearchHit[] searchHits = results.getSearchHits();
		if (HelpPlugin.DEBUG_SEARCH) {
			for (int i = 0 ; i < 10 && i < searchHits.length; i++) {
				System.out.println("Score " + searchHits[i].getScore()  //$NON-NLS-1$
						+ ": " + searchHits[i].getLabel()); //$NON-NLS-1$
			}
		}
		if (!activityFiltering) {
			collector.accept(searchHits);
			return;
		}
		// Filtering of results by activities
		ArrayList enabledHits = new ArrayList();
		for (int i = 0; i < searchHits.length; i++) {
			SearchHit hit = searchHits[i];
			if (hit.getParticipantId()!=null) {
				// hit comes from a search participant
				if (HelpBasePlugin.getActivitySupport().isEnabled(hit.getHref()))
					enabledHits.add(hit);
			}
			else if (HelpBasePlugin.getActivitySupport().isEnabledTopic(
					hit.getHref(), Platform.getNL())) {
				enabledHits.add(hit);
			}
		}
		collector.accept((SearchHit[]) enabledHits
				.toArray(new SearchHit[enabledHits.size()]));
	}

	public String toAbsoluteHref(String href, boolean frames) {
		return null;
	}
	
	
	public boolean open(String id) {
		int sep = id.indexOf('/');
		if (sep== -1)
			return false;
		String participantId = id.substring(0, sep);
		id = id.substring(sep+1);
		SearchParticipant participant = BaseHelpSystem.getLocalSearchManager().getGlobalParticipant(participantId);
		if (participant==null)
			return false;
		try {
			return participant.open(id);
		}
		catch (Throwable t) {
			HelpBasePlugin.logError("Error occured in search participant trying to open document with id: " + id + ", participant: " + participant.getClass().getName(), t); //$NON-NLS-1$ //$NON-NLS-2$
			return false;
		}
	}
}
