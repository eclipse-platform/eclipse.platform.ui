/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.workingset.*;

/**
 * Local Help search engine participant in the federated search.
 */
public class LocalHelp implements ISearchEngine {
    private static final int MAX_HITS = 500;

    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngine#run(java.lang.String, org.eclipse.help.internal.search.ISearchScope, org.eclipse.help.internal.search.federated.ISearchEngineResultCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(String query, ISearchScope scope,
            final ISearchEngineResultCollector collector, IProgressMonitor monitor)
            throws CoreException {
      
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSearchWord(query);
        WorkingSet[] workingSets = null;
        if (scope instanceof WorkingSet)
            workingSets = new WorkingSet[] {(WorkingSet)scope};
        SearchResults localResults = new SearchResults(workingSets, MAX_HITS, Platform.getNL());
        // If the indexer has been started and is currently running,
        // wait for it to finish.
        try {
        	Platform.getJobManager().join(IndexerJob.FAMILY, monitor);
        }
        catch (InterruptedException e) {
        	//TODO we may need to do something here
        }
        BaseHelpSystem.getSearchManager().search(searchQuery, localResults, monitor);
        collector.add(localResults.getSearchHits());

    }

    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngine#cancel()
     */
    public void cancel() {
    }

}
