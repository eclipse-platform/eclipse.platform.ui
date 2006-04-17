/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.search;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.StringTokenizer;

import org.eclipse.help.internal.search.InfoCenter;
import org.eclipse.help.search.*;
import org.eclipse.help.ui.ISearchScopeFactory;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Creates the scope for local search using the help working sets
 */
public class InfoCenterSearchScopeFactory implements ISearchScopeFactory {
	public static final String P_URL = "url"; //$NON-NLS-1$
	public static final String P_SEARCH_SELECTED = "searchSelected"; //$NON-NLS-1$
	public static final String P_TOCS = "tocs"; //$NON-NLS-1$
	public static final String TOC_SEPARATOR = ";"; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.help.ui.ISearchScopeFactory#createSearchScope(org.eclipse.jface.preference.IPreferenceStore)
     */
    public ISearchScope createSearchScope(IPreferenceStore store, String engineId, Dictionary parameters) {
        String url = getProperty(P_URL, store, engineId, parameters);
        String ssvalue = getProperty(P_SEARCH_SELECTED, store, engineId, parameters);
        boolean searchSelected = ssvalue!=null && ssvalue.equalsIgnoreCase("true"); //$NON-NLS-1$
        String [] tocs=null;
        if (searchSelected) {
        	String tvalue = getProperty(P_TOCS, store, engineId, parameters);
        	if (tvalue!=null && tvalue.length()>0) {
        		StringTokenizer stok = new StringTokenizer(tvalue, TOC_SEPARATOR);
        		ArrayList list = new ArrayList();
        		while (stok.hasMoreTokens()) {
        			String toc = stok.nextToken();
        			list.add(toc);
        		}
        		if (list.size()>0)
        			tocs = (String[])list.toArray(new String[list.size()]);
        	}
        }
        return new InfoCenter.Scope(url, searchSelected, tocs);
    }
    
    private String getProperty(String key, IPreferenceStore store, String engineId, Dictionary parameters) {
    	// try the store first
    	String value = store.getString(engineId+"."+key); //$NON-NLS-1$
    	if (value!=null && value.length()>0) return value;
    	// try the parameters
    	return (String)parameters.get(key);
    }
}
