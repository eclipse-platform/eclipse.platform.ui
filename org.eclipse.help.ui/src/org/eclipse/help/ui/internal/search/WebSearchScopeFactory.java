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
package org.eclipse.help.ui.internal.search;

import java.util.Dictionary;

import org.eclipse.help.internal.search.WebSearch;
import org.eclipse.help.search.*;
import org.eclipse.help.ui.ISearchScopeFactory;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Factory for creating scope objects for the generic web search engine
 */
public class WebSearchScopeFactory implements ISearchScopeFactory {
	public final static String P_URL = "url"; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.help.ui.ISearchScopeFactory#createSearchScope(org.eclipse.jface.preference.IPreferenceStore)
     */
    public ISearchScope createSearchScope(IPreferenceStore store, String engineId, Dictionary parameters) {
        String urlTemplate = getProperty(store, engineId, parameters);
        return new WebSearch.Scope(urlTemplate);
    }
    
    private String getProperty(IPreferenceStore store, String engineId, Dictionary parameters) {
    	// try the store first
    	String value = store.getString(engineId+"."+P_URL); //$NON-NLS-1$
    	if (value!=null && value.length()>0) return value;
    	// try the parameters
    	return (String)parameters.get(P_URL);
    }
}
