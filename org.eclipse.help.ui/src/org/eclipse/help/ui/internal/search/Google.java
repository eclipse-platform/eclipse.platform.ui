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
package org.eclipse.help.ui.internal.search;

import java.io.*;
import java.net.*;

import org.eclipse.core.runtime.*;
import org.eclipse.help.*;
import org.eclipse.help.internal.search.*;
import org.eclipse.help.internal.search.federated.*;

/**
 * Simple representation of Google in the federated search.
 */
public class Google implements ISearchEngine {
    public static final String NEWS = "news"; //$NON-NLS-1$
    public static final String WEB = "web"; //$NON-NLS-1$
    
    public static class Scope implements ISearchScope {
        private String type;
        public Scope(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }
    }
    
    public static class SearchResult implements ISearchEngineResult {
        private String query;
        private String type;
        public SearchResult(String query, String type) {
            this.query = query;
            this.type = type;
        }
        public String getDescription() {
            return "Click on this link to see results from Google";
        }

        public String getHref() {
            String href = null;
            if (NEWS.equals(type))
                href = "http://groups.google.com/groups?hl=en&lr=&sa=N&tab=wg&q=";
            else
                href = "http://www.google.com/search?hl=en&q=";
            
            try {
                href += URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                href += query;
            }
            return href;
        }

        public String getLabel() {
            return "Google search hits. Click to see them.";
        }

        public float getScore() {
            return 1;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.help.internal.search.federated.ISearchEngineResult#getCategory()
         */
        public IHelpResource getCategory() {
            return null;
        }
		public boolean getForceExternalWindow() {
			return true;
		}
}
    
    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngine#run(java.lang.String, org.eclipse.help.internal.search.ISearchScope, org.eclipse.help.internal.search.federated.ISearchEngineResultCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(String query, ISearchScope scope,
            ISearchEngineResultCollector collector, IProgressMonitor monitor)
            throws CoreException {
        
        collector.add(new SearchResult(query, ((Scope)scope).getType()));

    }

    /* (non-Javadoc)
     * @see org.eclipse.help.internal.search.federated.ISearchEngine#cancel()
     */
    public void cancel() {
        // TODO Auto-generated method stub
    }

}
