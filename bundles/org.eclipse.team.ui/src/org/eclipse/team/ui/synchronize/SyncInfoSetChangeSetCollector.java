/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.ui.synchronize;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ui.synchronize.ChangeSetModelProvider;

/**
 * This abstract class provides API for accumulating the <code>SyncInfo</code>
 * from a seed <code>SyncInfoSet</code> into a set of <code>ChangeSet</code>
 * instances. It is used to provide the input to a synchronize page when
 * change sets are enabled.
 * <p>
 * This class does not register as a change listener with the seed set. It
 * is up to clients to invoke either the <code>reset</code> or <code>handleChange</code>
 * methods in reponse to seed set changes. 
 * @since 3.1
 */
public abstract class SyncInfoSetChangeSetCollector extends ChangeSetCollector {
    
    private final ISynchronizePageConfiguration configuration;
    private ChangeSetModelProvider provider;
    
    /*
     * Listener that will remove sets when they become empty.
     */
    ISyncInfoSetChangeListener changeSetListener = new ISyncInfoSetChangeListener() {
        
        /* (non-Javadoc)
         * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void syncInfoSetReset(SyncInfoSet set, IProgressMonitor monitor) {
            handleChangeEvent(set);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoChanged(org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void syncInfoChanged(ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
            handleChangeEvent(event.getSet());
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
         */
        public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
            // TODO Auto-generated method stub
        }
        
        /*
         * The collector removes change sets once they are empty
         */
        private void handleChangeEvent(SyncInfoSet set) {
            if (set.isEmpty()) {
                ChangeSet changeSet = getChangeSet(set);
                if (changeSet != null) {
                    remove(changeSet);
                }
            }
        }
    };

    /**
     * Create a collector that contains the sync info from the given seed set
     * @param seedSet the set used to determine which sync info
     * should be included in the change sets.
     */
    public SyncInfoSetChangeSetCollector(ISynchronizePageConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Add the given resource sync info nodes to the appropriate
     * change sets, adding them inf necessary.
     * @param infos the sync infos to add
     */
    protected abstract void add(SyncInfo[] infos);

    /**
     * Remove the given resources from all sets of this collector.
     * @param resources the resources to be removed
     */
    protected void remove(IResource[] resources) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            set.remove(resources);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.subscribers.ChangeSetCollector#add(org.eclipse.team.core.subscribers.ChangeSet)
     */
    public void add(ChangeSet set) {
        super.add(set);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.subscribers.ChangeSetCollector#getChangeSetChangeListener()
     */
    protected ISyncInfoSetChangeListener getChangeSetChangeListener() {
        return changeSetListener;
    }
    
    /**
     * Repopulate the change sets from the seed set.
     * If <code>null</code> is passed, clear any state
     * but do not repopulate.
     *
     */
    public void reset(SyncInfoSet seedSet) {
        // First, remove all the sets
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set2 = sets[i];
            remove(set2);
        }
        if (seedSet != null) {
            add(seedSet.getSyncInfos());
        }
    }

    public void handleChange(ISyncInfoSetChangeEvent event) {
        List removals = new ArrayList();
        List additions = new ArrayList();
        removals.addAll(Arrays.asList(event.getRemovedResources()));
        additions.addAll(Arrays.asList(event.getAddedResources()));
        SyncInfo[] changed = event.getChangedResources();
        for (int i = 0; i < changed.length; i++) {
            SyncInfo info = changed[i];
            additions.add(info);
            removals.add(info.getLocal());
        }
        if (!removals.isEmpty()) {
            remove((IResource[]) removals.toArray(new IResource[removals.size()]));
        }
        if (!additions.isEmpty()) {
            add((SyncInfo[]) additions.toArray(new SyncInfo[additions.size()]));
        }
    }
    
    public ISynchronizePageConfiguration getConfiguration() {
        return configuration;
    }
    
    protected void runViewUpdate(Runnable runnable, boolean preserveExpansion) {
        provider.runViewUpdate(runnable, preserveExpansion);
    }
    
    /* (non-javadoc)
     * Sets the provider for this collector. This method is for internal use only.
     */
    public void setProvider(ChangeSetModelProvider provider) {
        this.provider = provider;
    }

    /**
     * This method should wait unti any background processing is
     * completed. It is for testing purposes. By default, it does not wait at all.
     * Subclasses that perform work in the background should override.
     * @param monitor a progress monitor
     */
    public void waitUntilDone(IProgressMonitor monitor) {
        // Do nothing, by default
    }
}
