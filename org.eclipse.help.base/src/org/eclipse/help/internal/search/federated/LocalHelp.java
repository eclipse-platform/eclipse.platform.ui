/*
 * Created on Jan 12, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.eclipse.help.internal.search.federated;

import org.eclipse.core.runtime.*;
import org.eclipse.help.internal.base.*;
import org.eclipse.help.internal.search.*;

/**
 * @author dorian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
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
        SearchResults localResults = new SearchResults(null, MAX_HITS, Platform.getNL());
        BaseHelpSystem.getSearchManager().search(searchQuery, localResults, monitor);

    }

    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngine#cancel()
     */
    public void cancel() {
    }

}
