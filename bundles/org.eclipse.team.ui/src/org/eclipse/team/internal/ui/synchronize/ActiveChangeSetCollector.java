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
package org.eclipse.team.internal.ui.synchronize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ChangeSet;
import org.eclipse.team.core.subscribers.IChangeSetChangeListener;
import org.eclipse.team.core.subscribers.SubscriberChangeSetCollector;
import org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent;
import org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * Group incoming changes according to the active change set thet are
 * located in
 */
public class ActiveChangeSetCollector implements ISyncInfoSetChangeListener {

    private final ISynchronizePageConfiguration configuration;
    
    /*
     * Map active change sets to infos displayed by the particpant
     */
    private final Map activeSets = new HashMap();
    
    /*
     * Set which contains those changes that are not part of an active set
     */
    private SyncInfoTree rootSet = new SyncInfoTree();

    private final ChangeSetModelProvider provider;

    /*
     * Listener registered with active change set manager
     */
    private IChangeSetChangeListener activeChangeSetListener = new IChangeSetChangeListener() {

        public void setAdded(final ChangeSet set) {
            // Remove any resources that are in the new set
            provider.runViewUpdate(new Runnable() {
                public void run() {
		            remove(set.getResources());
		            createSyncInfoSet(set);
                }
            });
        }
        
        public void defaultSetChanged(final ChangeSet previousDefault, final ChangeSet set) {
            provider.runViewUpdate(new Runnable() {
                public void run() {
                    listener.defaultSetChanged(previousDefault, set);
                }
            });
        }
        
        public void setRemoved(final ChangeSet set) {
            provider.runViewUpdate(new Runnable() {
                public void run() {
                    remove(set);
                }
            });
        }

        public void nameChanged(final ChangeSet set) {
            provider.runViewUpdate(new Runnable() {
                public void run() {
                    listener.nameChanged(set);
                }
            });
        }

        public void resourcesChanged(final ChangeSet set, final IResource[] resources) {
            // Not used by the provider
        }
        
    };

    /**
     * Listener that wants to recieve change events from this collector
     */
    private IChangeSetChangeListener listener;
    
    public ActiveChangeSetCollector(ISynchronizePageConfiguration configuration, ChangeSetModelProvider provider) {
        this.configuration = configuration;
        this.provider = provider;
        getActiveChangeSetManager().addListener(activeChangeSetListener);
    }

    public ISynchronizePageConfiguration getConfiguration() {
        return configuration;
    }
    
    public SubscriberChangeSetCollector getActiveChangeSetManager() {
        return getConfiguration().getParticipant().getChangeSetCapability().getActiveChangeSetManager();
    }
    
    /**
     * Repopulate the change sets from the seed set.
     * If <code>null</code> is passed, the state
     * of the collector is cleared but the set is not
     * repopulated.
     * <p>
     * This method is invoked by the model provider when the
     * model provider changes state. It should not
     * be invoked by other clients. The model provider
     * will invoke this method from a particular thread (which may
     * or may not be the UI thread). Updates done to the collector
     * from within this thread will be thread-safe and update the view
     * properly. Updates done from other threads should use the 
     * <code>performUpdate</code> method to ensure the view is
     * updated properly.
     */
    public void reset(SyncInfoSet seedSet) {
        // First, clean up
        rootSet.clear();
        ChangeSet[] sets = (ChangeSet[]) activeSets.keySet().toArray(new ChangeSet[activeSets.size()]);
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            remove(set);
        }
        activeSets.clear();
        
        // Now repopulate
        if (seedSet != null) {
            if (getConfiguration().getComparisonType() == ISynchronizePageConfiguration.THREE_WAY) {
	            // Show all active change sets even if they are empty
	            sets = getActiveChangeSetManager().getSets();
	            for (int i = 0; i < sets.length; i++) {
	                ChangeSet set = sets[i];
	                add(set);
	            }
	            // The above will add all sync info that are contained in sets.
	            // We still need to add uncontained infos to the root set
	            SyncInfo[] syncInfos = seedSet.getSyncInfos();
	            for (int i = 0; i < syncInfos.length; i++) {
	                SyncInfo info = syncInfos[i];
	                if (isLocalChange(info)) {
	                    ChangeSet[] containingSets = findChangeSets(info);
	                    if (containingSets.length == 0) {
	                        rootSet.add(info);
	                    }
	                }
	            }
            } else {
                add(seedSet.getSyncInfos());
            }
        }
    }
    
    /**
     * Handle a sync info set change event from the provider's
     * seed set.
     * <p>
     * This method is invoked by the model provider when the
     * model provider changes state. It should not
     * be invoked by other clients. The model provider
     * will invoke this method from a particular thread (which may
     * or may not be the UI thread). Updates done to the collector
     * from within this thread will be thread-safe and update the view
     * properly. Updates done from other threads should use the 
     * <code>performUpdate</code> method to ensure the view is
     * updated properly.
     */
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
    
    /**
     * Remove the given resources from all sets of this collector.
     * @param resources the resources to be removed
     */
    protected void remove(IResource[] resources) {
        for (Iterator iter = activeSets.values().iterator(); iter.hasNext();) {
            SyncInfoSet set = (SyncInfoSet) iter.next();
            set.removeAll(resources);
        }
        rootSet.removeAll(resources);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.SyncInfoSetChangeSetCollector#add(org.eclipse.team.core.synchronize.SyncInfo[])
     */
    protected void add(SyncInfo[] infos) {
        for (int i = 0; i < infos.length; i++) {
            SyncInfo info = infos[i];
            if (isLocalChange(info) && select(info)) {
                ChangeSet[] sets = findChangeSets(info);
                if (sets.length == 0) {
                    rootSet.add(info);
                } else {
	                for (int j = 0; j < sets.length; j++) {
	                    ChangeSet set = sets[j];
	                    SyncInfoSet targetSet = (SyncInfoSet)activeSets.get(set);
	                    if (targetSet == null) {
	                        // This will add all the appropriate sync info to the set
	                        createSyncInfoSet(set);
	                    } else {
	                        targetSet.add(info);
	                    }
	                }   
                }
            }
        }
    }

    private ChangeSet[] findChangeSets(SyncInfo info) {
        SubscriberChangeSetCollector manager = getActiveChangeSetManager();
        ChangeSet[] sets = manager.getSets();
        List result = new ArrayList();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
            if (set.contains(info.getLocal())) {
                result.add(set);
            }
        }
        return (ChangeSet[]) result.toArray(new ChangeSet[result.size()]);
    }

    /*
	 * Return if this sync info is an outgoing change.
	 */
	private boolean isLocalChange(SyncInfo info) {
	    if (!info.getComparator().isThreeWay()) {
	        try {
                // Obtain the sync info from the subscriber and use it to see if the change is local
                info = getActiveChangeSetManager().getSubscriber().getSyncInfo(info.getLocal());
            } catch (TeamException e) {
                TeamUIPlugin.log(e);
            }
	    }
		return (info.getComparator().isThreeWay() 
		        && ((info.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.OUTGOING ||
		                (info.getKind() & SyncInfo.DIRECTION_MASK) == SyncInfo.CONFLICTING));
	}
	
    public SyncInfoTree getRootSet() {
        return rootSet;
    }

    /*
     * Add the set from the collector.
     */
    public void add(ChangeSet set) {
        SyncInfoSet targetSet = (SyncInfoSet)activeSets.get(set);
        if (targetSet == null) {
            createSyncInfoSet(set);
        }
        if (listener != null) {
            listener.setAdded(set);
        }
    }
    
    private SyncInfoTree createSyncInfoSet(ChangeSet set) {
        SyncInfoTree sis = (SyncInfoTree)activeSets.get(set);
        // Register the listener last since the add will
        // look for new elements
        boolean added = false;
        if (sis == null) {
            sis = new SyncInfoTree();
            sis.beginInput();
            activeSets.put(set, sis);
            added = true;
        }
        try {
            sis.beginInput();
            if (!sis.isEmpty())
                sis.removeAll(sis.getResources());
            sis.addAll(select(set.getSyncInfoSet().getSyncInfos()));
        } finally {
            sis.endInput(null);
        }
        if (added) {
            set.getSyncInfoSet().addSyncSetChangedListener(this);
            listener.setAdded(set);
        }
        return sis;
    }

    /*
     * Remove the set from the collector.
     */
    public void remove(ChangeSet set) {
        set.getSyncInfoSet().removeSyncSetChangedListener(this);
        activeSets.remove(set);
        if (listener != null) {
            listener.setRemoved(set);
        }
    }

    /*
     * Return the sync info set for the given active change set.
     * Create one if one doesn't already exist.
     */
    public SyncInfoTree getSyncInfoSet(ChangeSet set) {
        SyncInfoTree sis = (SyncInfoTree)activeSets.get(set);
        if (sis == null) {
            sis = new SyncInfoTree();
            set.getSyncInfoSet().addSyncSetChangedListener(this);
            activeSets.put(set, sis);
	        sis.addAll(select(set.getSyncInfoSet().getSyncInfos()));
        }
        return sis;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetReset(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void syncInfoSetReset(final SyncInfoSet set, IProgressMonitor monitor) {
        provider.performUpdate(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) {
		        ChangeSet changeSet = getChangeSet(set);
		        if (changeSet != null) {
			        SyncInfoSet targetSet = (SyncInfoSet)activeSets.get(changeSet);
			        if (targetSet != null) {
				        targetSet.clear();
				        targetSet.addAll(select(set.getSyncInfos()));
				        rootSet.removeAll(set.getResources());
			        }
		        }
            }
        }, true /* preserver expansion */);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoChanged(org.eclipse.team.core.synchronize.ISyncInfoSetChangeEvent, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void syncInfoChanged(final ISyncInfoSetChangeEvent event, IProgressMonitor monitor) {
        provider.performUpdate(new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) {
                ChangeSet changeSet = getChangeSet(event.getSet());
                if (changeSet != null) {
	                SyncInfoSet targetSet = (SyncInfoSet)activeSets.get(changeSet);
	                if (targetSet != null) {
		                targetSet.removeAll(event.getRemovedResources());
		                targetSet.addAll(select(event.getChangedResources()));
		                targetSet.addAll(select(event.getAddedResources()));
		                rootSet.removeAll(event.getSet().getResources());
	                }
                }
            }
        }, true /* preserver expansion */);
    }

    private ChangeSet getChangeSet(SyncInfoSet set) {
        for (Iterator iter = activeSets.keySet().iterator(); iter.hasNext();) {
            ChangeSet changeSet = (ChangeSet) iter.next();
            if (changeSet.getSyncInfoSet() == set) {
                return changeSet;
            }
        }
        return null;
    }
    
    private boolean select(SyncInfo info) {
        return getSeedSet().getSyncInfo(info.getLocal()) != null;
    }
    
    private SyncInfoSet select(SyncInfo[] syncInfos) {
        SyncInfoSet result = new SyncInfoSet();
        for (int i = 0; i < syncInfos.length; i++) {
            SyncInfo info = syncInfos[i];
            if (getSeedSet().getSyncInfo(info.getLocal()) != null) {
                result.add(info);
            }
        }
        return result;
    }
    
    private SyncInfoSet getSeedSet() {
        return provider.getSyncInfoSet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener#syncInfoSetErrors(org.eclipse.team.core.synchronize.SyncInfoSet, org.eclipse.team.core.ITeamStatus[], org.eclipse.core.runtime.IProgressMonitor)
     */
    public void syncInfoSetErrors(SyncInfoSet set, ITeamStatus[] errors, IProgressMonitor monitor) {
        // Errors are not injected into the active change sets
    }

    public void dispose() {
        getActiveChangeSetManager().removeListener(activeChangeSetListener);
    }
    
    /**
     * Set the change set listener for this collector. There is
     * only one for this type of collector.
     * @param listener change set change listener
     */
    public void setChangeSetChangeListener(IChangeSetChangeListener listener) {
        this.listener = listener;
        if (listener == null) {
            getActiveChangeSetManager().removeListener(activeChangeSetListener);
        } else {
            getActiveChangeSetManager().addListener(activeChangeSetListener);
        }
    }
}
