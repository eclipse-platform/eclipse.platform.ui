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
package org.eclipse.help.ui.internal.search;

import java.util.Dictionary;

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
    public final static String P_WORKING_SET = "workingSet"; //$NON-NLS-1$
    public final static String P_CAPABILITY_FILTERING = "capabilityFiltering";  //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.help.ui.ISearchScopeFactory#createSearchScope(org.eclipse.jface.preference.IPreferenceStore)
     */
    public ISearchScope createSearchScope(IPreferenceStore store, String engineId, Dictionary parameters) {
        String name = store.getString(engineId+"."+P_WORKING_SET); //$NON-NLS-1$
        WorkingSet workingSet = null;
        if (name != null)
            workingSet = BaseHelpSystem.getWorkingSetManager().getWorkingSet(name);
        boolean capabilityFiltering = store.getBoolean(engineId+"."+P_CAPABILITY_FILTERING); //$NON-NLS-1$
        return new LocalHelpScope(workingSet, capabilityFiltering);
    }
}
