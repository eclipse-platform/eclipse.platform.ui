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

import org.eclipse.help.internal.base.BaseHelpSystem;
import org.eclipse.help.internal.search.federated.LocalHelpScope;
import org.eclipse.help.internal.workingset.WorkingSet;
import org.eclipse.help.search.ISearchScope;
import org.eclipse.help.ui.ISearchScopeFactory;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Creates the scope for local search using the help working sets
 */
public class LocalSearchScopeFactory implements ISearchScopeFactory {
    public final static String ENGINE_ID = "org.eclipse.help.ui.localSearch";
    public final static String WORKING_SET = ENGINE_ID + ".workingSet";
    public final static String CAPABILITY_FILTERING = ENGINE_ID + ".capabilityFiltering"; 
    
    /* (non-Javadoc)
     * @see org.eclipse.help.ui.ISearchScopeFactory#createSearchScope(org.eclipse.jface.preference.IPreferenceStore)
     */
    public ISearchScope createSearchScope(IPreferenceStore store) {
        String name = store.getString(WORKING_SET);
        WorkingSet workingSet = null;
        if (name != null)
            workingSet = BaseHelpSystem.getWorkingSetManager().getWorkingSet(name);
        boolean capabilityFiltering = store.getBoolean(CAPABILITY_FILTERING);
        return new LocalHelpScope(workingSet, !capabilityFiltering);
    }
}
