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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.*;
import org.eclipse.help.IHelpResource;
import org.eclipse.help.search.*;

/**
 * Simple representation of Google in the federated search.
 */
public class EclipseOrg implements ISearchEngine {
    public static final String ALL = "all"; //$NON-NLS-1$
    public static final String ARTICLES = "articles"; //$NON-NLS-1$
    public static final String DOC = "docs"; //$NON-NLS-1$
    public static final String MAIL = "mailing_lists"; //$NON-NLS-1$
    public static final String NEWS = "newsgroups"; //$NON-NLS-1$
    
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
            return "Click on this link to see results from Eclipse.org";
        }

        public String getHref() {
            String href = null;

            if (ARTICLES.equals(type))
                href = "http://eclipse.org/search/search.cgi?ps=20&m=all&ul=%2Farticles%2F&q=";
            else if (DOC.equals(type))
                href = "http://eclipse.org/search/search.cgi?ps=20&m=all&ul=%2Fdocumentation%2F&q=";
            else if (MAIL.equals(type))
                href = "http://eclipse.org/search/search.cgi?ps=20&m=all&ul=%2Fmhonarc%2F&q=";
            else if (NEWS.equals(type))
                href = "http://eclipse.org/search/search.cgi?ps=20&m=all&ul=%2Fnewslists%2F&q=";
            else
                href = "http://eclipse.org/search/search.cgi?ps=20&m=all&q=";
            try {
                href += URLEncoder.encode(query, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                href += query;
            }
            return href;
        }

        public String getLabel() {
            return "Eclipse.org search results";
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
