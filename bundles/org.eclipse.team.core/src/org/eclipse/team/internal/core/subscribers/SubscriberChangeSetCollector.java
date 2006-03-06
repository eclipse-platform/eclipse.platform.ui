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
package org.eclipse.team.internal.core.subscribers;

import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.core.*;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * This class manages the active change sets associated with a subscriber.
 */
public class SubscriberChangeSetCollector extends DiffTreeChangeSetCollector implements IDiffChangeListener {
    
    private static final String PREF_CHANGE_SETS = "changeSets"; //$NON-NLS-1$
    private static final String CTX_DEFAULT_SET = "defaultSet"; //$NON-NLS-1$
    
    private static final int RESOURCE_REMOVAL = 1;
    private static final int RESOURCE_CHANGE = 2;
    
    private ActiveChangeSet defaultSet;
    private EventHandler handler;
    private ResourceCollector collector;
    
    /*
     * Background event handler for serializing and batching change set changes
     */
    private class EventHandler extends BackgroundEventHandler {

        private List dispatchEvents = new ArrayList();
        
        protected EventHandler(String jobName, String errorTitle) {
            super(jobName, errorTitle);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#processEvent(org.eclipse.team.internal.core.BackgroundEventHandler.Event, org.eclipse.core.runtime.IProgressMonitor)
         */
        protected void processEvent(Event event, IProgressMonitor monitor) throws CoreException {
            // Handle everything in the dispatch
            if (isShutdown())
                throw new OperationCanceledException();
            dispatchEvents.add(event);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#doDispatchEvents(org.eclipse.core.runtime.IProgressMonitor)
         */
        protected boolean doDispatchEvents(IProgressMonitor monitor) throws TeamException {
            if (dispatchEvents.isEmpty()) {
                return false;
            }
            if (isShutdown())
                throw new OperationCanceledException();
            ResourceDiffTree[] locked = null;
            try {
                locked = beginDispath();
                for (Iterator iter = dispatchEvents.iterator(); iter.hasNext();) {
                    Event event = (Event) iter.next();
	                switch (event.getType()) {
	                case RESOURCE_REMOVAL:
	                    handleRemove(event.getResource());
	                    break;
	                case RESOURCE_CHANGE:
	                    handleChange(event.getResource(), ((ResourceEvent)event).getDepth());
	                    break;
	                default:
	                    break;
	                }
                    if (isShutdown())
                        throw new OperationCanceledException();
                }
            } catch (CoreException e) {
				throw TeamException.asTeamException(e);
			} finally {
                try {
                    endDispatch(locked, monitor);
                } finally {
                    dispatchEvents.clear();
                }
            }
            return true;
        }

        /*
         * Begin input on all the sets and return the sync sets that were 
         * locked. If this method throws an exception then the client
         * can assume that no sets were locked
         */
        private ResourceDiffTree[] beginDispath() {
            ChangeSet[] sets = getSets();
            List lockedSets = new ArrayList();
            try {
                for (int i = 0; i < sets.length; i++) {
                    ActiveChangeSet set = (ActiveChangeSet)sets[i];
                    ResourceDiffTree tree = set.internalGetDiffTree();
                    lockedSets.add(tree);
                    tree.beginInput();
                }
                return (ResourceDiffTree[]) lockedSets.toArray(new ResourceDiffTree[lockedSets.size()]);
            } catch (RuntimeException e) {
                try {
                    for (Iterator iter = lockedSets.iterator(); iter.hasNext();) {
                    	ResourceDiffTree tree = (ResourceDiffTree) iter.next();
                        try {
                            tree.endInput(null);
                        } catch (Throwable e1) {
                            // Ignore so that original exception is not masked
                        }
                    }
                } catch (Throwable e1) {
                    // Ignore so that original exception is not masked
                }
                throw e;
            }
        }

        private void endDispatch(ResourceDiffTree[] locked, IProgressMonitor monitor) {
            if (locked == null) {
                // The begin failed so there's nothing to unlock
                return;
            }
            monitor.beginTask(null, 100 * locked.length);
            for (int i = 0; i < locked.length; i++) {
            	ResourceDiffTree tree = locked[i];
                try {
                    tree.endInput(Policy.subMonitorFor(monitor, 100));
                } catch (RuntimeException e) {
                    // Don't worry about ending every set if an error occurs.
                    // Instead, log the error and suggest a restart.
                    TeamPlugin.log(IStatus.ERROR, Messages.SubscriberChangeSetCollector_0, e); 
                    throw e;
                }
            }
            monitor.done();
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.team.internal.core.BackgroundEventHandler#queueEvent(org.eclipse.team.internal.core.BackgroundEventHandler.Event, boolean)
         */
        protected synchronized void queueEvent(Event event, boolean front) {
            // Override to allow access from enclosing class
            super.queueEvent(event, front);
        }
        
        /*
         * Handle the removal
         */
        private void handleRemove(IResource resource) {
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                // This will remove any descendants from the set and callback to 
                // resourcesChanged which will batch changes
                if (!set.isEmpty()) {
	                set.rootRemoved(resource, IResource.DEPTH_INFINITE);
	                if (set.isEmpty()) {
	                    remove(set);
	                }
                }
            }
        }
        
        /*
         * Handle the change
         */
        private void handleChange(IResource resource, int depth) throws CoreException {
            IDiff diff = getDiff(resource);
            if (isModified(diff)) {
                ActiveChangeSet[] containingSets = getContainingSets(resource);
                if (containingSets.length == 0) {
	                // Consider for inclusion in the default set
	                // if the resource is not already a member of another set
                    if (defaultSet != null) {
                        defaultSet.add(diff);
                     }
                } else {
                    for (int i = 0; i < containingSets.length; i++) {
                        ActiveChangeSet set = containingSets[i];
                        // Update the sync info in the set
                        set.add(diff);
                    }
                }
            } else {
                removeFromAllSets(resource);
            }
            if (depth != IResource.DEPTH_ZERO) {
                IResource[] members = getSubscriber().members(resource);
                for (int i = 0; i < members.length; i++) {
                    IResource member = members[i];
                    handleChange(member, depth == IResource.DEPTH_ONE ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE);
                }
            }
        }
        
        private void removeFromAllSets(IResource resource) {
            List toRemove = new ArrayList();
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                if (set.contains(resource)) {
                    set.remove(resource);
	                if (set.isEmpty()) {
	                    toRemove.add(set);
	                }
                }
            }
            for (Iterator iter = toRemove.iterator(); iter.hasNext();) {
                ActiveChangeSet set = (ActiveChangeSet) iter.next();
                remove(set);
            }
        }

        private ActiveChangeSet[] getContainingSets(IResource resource) {
            Set result = new HashSet();
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet set = sets[i];
                if (set.contains(resource)) {
                    result.add(set);
                }
            }
            return (ActiveChangeSet[]) result.toArray(new ActiveChangeSet[result.size()]);
        }
    }
    
    private class ResourceCollector extends SubscriberResourceCollector {

        public ResourceCollector(Subscriber subscriber) {
            super(subscriber);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#remove(org.eclipse.core.resources.IResource)
         */
        protected void remove(IResource resource) {
            handler.queueEvent(new BackgroundEventHandler.ResourceEvent(resource, RESOURCE_REMOVAL, IResource.DEPTH_INFINITE), false);
        }

        /* (non-Javadoc)
         * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#change(org.eclipse.core.resources.IResource, int)
         */
        protected void change(IResource resource, int depth) {
            handler.queueEvent(new BackgroundEventHandler.ResourceEvent(resource, RESOURCE_CHANGE, depth), false);
        }
        
        protected boolean hasMembers(IResource resource) {
            return SubscriberChangeSetCollector.this.hasMembers(resource);
        }
    }
    
    public SubscriberChangeSetCollector(Subscriber subscriber) {
        collector = new ResourceCollector(subscriber);
        load();
        handler = new EventHandler(NLS.bind(Messages.SubscriberChangeSetCollector_1, new String[] { subscriber.getName() }), NLS.bind(Messages.SubscriberChangeSetCollector_2, new String[] { subscriber.getName() })); // 
    }
    
    public boolean hasMembers(IResource resource) {
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ActiveChangeSet set = (ActiveChangeSet)sets[i];
            if (set.getDiffTree().getChildren(resource.getFullPath()).length > 0)
            	return true;
        }
        if (defaultSet != null)
            return (defaultSet.getDiffTree().getChildren(resource.getFullPath()).length > 0);
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.subscribers.ChangeSetCollector#add(org.eclipse.team.internal.core.subscribers.ChangeSet)
     */
    public void add(ChangeSet set) {
        Assert.isTrue(set instanceof ActiveChangeSet);
        super.add(set);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.subscribers.DiffTreeChangeSetCollector#handleSetAdded(org.eclipse.team.internal.core.subscribers.ChangeSet)
     */
    protected void handleSetAdded(ChangeSet set) {
    	Assert.isTrue(set instanceof ActiveChangeSet);
    	super.handleSetAdded(set);
    	handleAddedResources(set, ((ActiveChangeSet)set).internalGetDiffTree().getDiffs());
    }
    
    /**
     * Return whether the manager allows a resource to
     * be in multiple sets. By default, a resource
     * may only be in one set.
     * @return whether the manager allows a resource to
     * be in multiple sets.
     */
    protected boolean isSingleSetPerResource() {
        return true;
    }
    
    /**
     * Create a commit set with the given title and files. The created
     * set is not added to the control of the commit set manager
     * so no events are fired. The set can be added using the
     * <code>add</code> method.
     * @param title the title of the commit set
     * @param diffs the files contained in the set
     * @return the created set
     */
    public ActiveChangeSet createSet(String title, IDiff[] diffs) {
        ActiveChangeSet commitSet = new ActiveChangeSet(this, title);
        if (diffs != null && diffs.length > 0) {
            commitSet.add(diffs);
        }
        return commitSet;
    }

    /**
     * Create a change set containing the given files if
     * they have been modified locally.
     * @param title the title of the commit set
     * @param files the files contained in the set
     * @return the created set
     * @throws CoreException
     */
    public ActiveChangeSet createSet(String title, IFile[] files) throws CoreException {
        List infos = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            IFile file = files[i];
            IDiff diff = getDiff(file);
            if (diff != null) {
                infos.add(diff);
            }
        }
        return createSet(title, (IDiff[]) infos.toArray(new IDiff[infos.size()]));
    }

    /**
     * Make the given set the default set into which all new modifications
     * that are not already in another set go.
     * @param set the set which is to become the default set
     */
    public void makeDefault(ActiveChangeSet set) {
        // The default set must be an active set
        if (!contains(set)) {
            add(set);
        }
        ActiveChangeSet oldSet = defaultSet;
        defaultSet = set;
        fireDefaultChangedEvent(oldSet, defaultSet);
    }

    /**
     * Return the set which is currently the default or
     * <code>null</code> if there is no default set.
     * @return the default change set
     */
    public ActiveChangeSet getDefaultSet() {
        return defaultSet;
    }
    /**
     * Return whether the given set is the default set into which all
     * new modifications will be placed.
     * @param set the set to test
     * @return whether the set is the default set
     */
    public boolean isDefault(ActiveChangeSet set) {
        return set == defaultSet;
    }
    
    /**
     * Return the sync info for the given resource obtained
     * from the subscriber.
     * @param resource the resource
     * @return the sync info for the resource
     * @throws CoreException
     */
    protected IDiff getDiff(IResource resource) throws CoreException {
        Subscriber subscriber = getSubscriber();
        return subscriber.getDiff(resource);
    }
    
    /**
     * Return the subscriber associated with this collector.
     * @return the subscriber associated with this collector
     */
    public Subscriber getSubscriber() {
        return collector.getSubscriber();
    }

    protected boolean isModified(IDiff diff) {
        if (diff != null) {
        	if (diff instanceof IThreeWayDiff) {
				IThreeWayDiff twd = (IThreeWayDiff) diff;
				int dir = twd.getDirection();
				return dir == IThreeWayDiff.OUTGOING || dir == IThreeWayDiff.CONFLICTING;
			} else {
				return diff.getKind() != IDiff.NO_CHANGE;
			}
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#dispose()
     */
    public void dispose() {
        handler.shutdown();
        collector.dispose();
        super.dispose();
        save();
    }
    
    private void save() {
		Preferences prefs = getPreferences();
        // Clear the persisted state before saving the new state
        try {
            String[] oldSetNames = prefs.childrenNames();
            for (int i = 0; i < oldSetNames.length; i++) {
                String string = oldSetNames[i];
                prefs.node(string).removeNode();
            }
        } catch (BackingStoreException e) {
            TeamPlugin.log(IStatus.ERROR, NLS.bind("An error occurred purging the commit set state for {0}", new String[] { getSubscriber().getName() }), e); //$NON-NLS-1$
        }
        ChangeSet[] sets = getSets();
        for (int i = 0; i < sets.length; i++) {
            ChangeSet set = sets[i];
			if (set instanceof ActiveChangeSet && !set.isEmpty()) {
			    Preferences child = prefs.node(((ActiveChangeSet)set).getTitle());
			    ((ActiveChangeSet)set).save(child);
			}
		}
		if (defaultSet != null) {
		    prefs.put(CTX_DEFAULT_SET, defaultSet.getTitle());
		}
		try {
            prefs.flush();
        } catch (BackingStoreException e) {
            TeamPlugin.log(IStatus.ERROR, NLS.bind(Messages.SubscriberChangeSetCollector_3, new String[] { getSubscriber().getName() }), e); 
        }
    }
    
    private void load() {
        Preferences prefs = getPreferences();
		String defaultSetTitle = prefs.get(CTX_DEFAULT_SET, null);
		try {
            String[] childNames = prefs.childrenNames();
            for (int i = 0; i < childNames.length; i++) {
                String string = childNames[i];
                Preferences childPrefs = prefs.node(string);
                ActiveChangeSet set = createSet(string, childPrefs);
                if (!set.isEmpty()) {
	            	if (defaultSet == null && defaultSetTitle != null && set.getTitle().equals(defaultSetTitle)) {
	            	    defaultSet = set;
	            	}
	            	add(set);
                }
            }
        } catch (BackingStoreException e) {
            TeamPlugin.log(IStatus.ERROR, NLS.bind(Messages.SubscriberChangeSetCollector_4, new String[] { getSubscriber().getName() }), e); 
        }
    }

    /**
     * Create a change set from the given preferences that were 
     * previously saved.
     * @param childPrefs the previously saved preferences
     * @return the created change set
     */
    protected ActiveChangeSet createSet(String title, Preferences childPrefs) {
        ActiveChangeSet changeSet = new ActiveChangeSet(this, title);
        changeSet.init(childPrefs);
        return changeSet;
    }

    private Preferences getPreferences() {
        return getParentPreferences().node(getSubscriberIdentifier());
    }
    
	private static Preferences getParentPreferences() {
		return getTeamPreferences().node(PREF_CHANGE_SETS);
	}
	
	private static Preferences getTeamPreferences() {
		return new InstanceScope().getNode(TeamPlugin.getPlugin().getBundle().getSymbolicName());
	}
	
    /**
     * Return the id that will uniquely identify the subscriber across
     * restarts.
     * @return the id that will uniquely identify the subscriber across
     */
    protected String getSubscriberIdentifier() {
        return getSubscriber().getName();
    }

    private IPath[] getAllResources(IDiffChangeEvent event) {
    	Set allResources = new HashSet();
		IDiff[] addedResources = event.getAdditions();
		for (int i = 0; i < addedResources.length; i++) {
			IDiff diff = addedResources[i];
			allResources.add(diff.getPath());
		}
		IDiff[] changedResources = event.getChanges();
		for (int i = 0; i < changedResources.length; i++) {
			IDiff diff = changedResources[i];
			allResources.add(diff.getPath());
		}
		IPath[] removals = event.getRemovals();
		for (int i = 0; i < removals.length; i++) {
			IPath path = removals[i];
			allResources.add(path);
		}
        return (IPath[]) allResources.toArray(new IPath[allResources.size()]);
    }

    private void handleAddedResources(ChangeSet set, IDiff[] diffs) {
        if (isSingleSetPerResource()) {
            IResource[] resources = new IResource[diffs.length];
            for (int i = 0; i < resources.length; i++) {
				resources[i] = ((DiffChangeSet)set).getDiffTree().getResource(diffs[i]);
			}
	        // Remove the added files from any other set that contains them
            ChangeSet[] sets = getSets();
            for (int i = 0; i < sets.length; i++) {
                ChangeSet otherSet = sets[i];
	            if (otherSet != set) {
	                otherSet.remove(resources);
	            }
	        }
        }
    }
    
    private void handleSyncSetChange(IResourceDiffTree tree, IDiff[] addedDiffs, IPath[] allAffectedResources) {
        ChangeSet changeSet = getChangeSet(tree);
        if (tree.isEmpty() && changeSet != null) {
            remove(changeSet);
        }
        fireResourcesChangedEvent(changeSet, allAffectedResources);
        handleAddedResources(changeSet, addedDiffs);
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.core.subscribers.ChangeSetCollector#getChangeSetSyncSetChangeListener()
     */
    protected IDiffChangeListener getDiffTreeListener() {
        return this;
    }

    /**
     * Wait until the collector is done processing any events.
     * This method is for testing purposes only.
     * @param monitor 
     */
    public void waitUntilDone(IProgressMonitor monitor) {
		monitor.worked(1);
		// wait for the event handler to process changes.
		while(handler.getEventHandlerJob().getState() != Job.NONE) {
			monitor.worked(1);
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
			Policy.checkCanceled(monitor);
		}
		monitor.worked(1);
    }

	public void diffsChanged(IDiffChangeEvent event, IProgressMonitor monitor) {
        IResourceDiffTree tree = (IResourceDiffTree)event.getTree();
        handleSyncSetChange(tree, event.getAdditions(), getAllResources(event));
	}

	public void propertyChanged(IDiffTree tree, int property, IPath[] paths) {
		// ignore
	}
}
