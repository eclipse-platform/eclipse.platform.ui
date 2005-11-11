/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.team.core.synchronize.ISyncInfoSetChangeListener;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.core.ListenerList;

/**
 * This class defines the common aspects of collecting a set of change
 * sets, including event notification.
 * 
 * @since 3.1
 */
public abstract class ChangeSetCollector {

    private ListenerList listeners = new ListenerList();
    private Set sets = new HashSet();
    
    private Object[] getListeners() {
        return listeners.getListeners();
    }
    
    /**
     * Method that can be invoked by subclasses when the name of
     * a managed change set changes.
     * @param set the set whose title has changed
     */
    protected void fireNameChangedEvent(final ChangeSet set) {
        if (contains(set)) {
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final IChangeSetChangeListener listener = (IChangeSetChangeListener)listeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        // Exceptions are logged by the platform
                    }
                    public void run() throws Exception {
                        listener.nameChanged(set);
                    }
                });
            }
        }
    }
    
    /**
     * Method which allows subclasses to notify listeners that the default
     * set has changed.
     * @param oldSet the previous default
     * @param defaultSet the new default
     */
    protected void fireDefaultChangedEvent(final ChangeSet oldSet, final ChangeSet defaultSet) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IChangeSetChangeListener listener = (IChangeSetChangeListener)listeners[i];
            Platform.run(new ISafeRunnable() {
                public void handleException(Throwable exception) {
                    // Exceptions are logged by the platform
                }
                public void run() throws Exception {
                    listener.defaultSetChanged(oldSet, defaultSet);
                }
            });
        }
    }
    
    /**
     * Add the set to the list of active sets.
     * @param set the set to be added
     */
    public void add(final ChangeSet set) {
        if (!contains(set)) {
            sets.add(set);
            set.getSyncInfoSet().addSyncSetChangedListener(getChangeSetChangeListener());
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final IChangeSetChangeListener listener = (IChangeSetChangeListener)listeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        // Exceptions are logged by the platform
                    }
                    public void run() throws Exception {
                        listener.setAdded(set);
                    }
                });
            }
        }
    }

    /**
     * Remove the set from the list of active sets.
     * @param set the set to be removed
     */
    public void remove(final ChangeSet set) {
        if (contains(set)) {
            set.getSyncInfoSet().removeSyncSetChangedListener(getChangeSetChangeListener());
            sets.remove(set);
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final IChangeSetChangeListener listener = (IChangeSetChangeListener)listeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        // Exceptions are logged by the platform
                    }
                    public void run() throws Exception {
                        listener.setRemoved(set);
                    }
                });
            }
        }
    }
    
    /**
     * Return the change listener that will be registered with each 
     * <code>SyncInfoSet</code> associated with the <code>ChangeSets</code>
     * added to this collector.
     * @return the change listener that will be registered with each 
     * <code>SyncInfoSet</code> associated with the <code>ChangeSets</code>
     * added to this collector
     */
    protected abstract ISyncInfoSetChangeListener getChangeSetChangeListener();

    /**
     * Return whether the manager contains the given commit set
     * @param set the commit set being tested
     * @return whether the set is contained in the manager's list of active sets
     */
    public boolean contains(ChangeSet set) {
        return sets.contains(set);
    }

    /**
     * Add the listener to the set of registered listeners.
     * @param listener the listener to be added
     */
    public void addListener(IChangeSetChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the listener from the set of registered listeners.
     * @param listener the listener to remove
     */
    public void removeListener(IChangeSetChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Return the list of active commit sets.
     * @return the list of active commit sets
     */
    public ChangeSet[] getSets() {
        return (ChangeSet[]) sets.toArray(new ChangeSet[sets.size()]);
    }
    
    /**
     * Dispose of any resources maintained by the manager
     */
    public void dispose() {
        // Nothing to do
    }

    /**
     * @param changeSet
     * @param allAffectedResources
     */
    protected void fireResourcesChangedEvent(final ChangeSet changeSet, final IResource[] allAffectedResources) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final IChangeSetChangeListener listener = (IChangeSetChangeListener)listeners[i];
            Platform.run(new ISafeRunnable() {
                public void handleException(Throwable exception) {
                    // Exceptions are logged by the platform
                }
                public void run() throws Exception {
                    listener.resourcesChanged(changeSet, allAffectedResources);
                }
            });
        }
    }
    
    /**
     * Return the Change Set whose sync info set is the
     * one given.
     * @param set a sync info set
     * @return the change set for the given sync info set
     */
    protected ChangeSet getChangeSet(SyncInfoSet set) {
        for (Iterator iter = sets.iterator(); iter.hasNext();) {
            ChangeSet changeSet = (ChangeSet) iter.next();
            if (changeSet.getSyncInfoSet() == set) {
                return changeSet;
            }
        }
        return null;
    }
}
