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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoTree;

/**
 * A change set represents a set of changes that are logically 
 * grouped together as a single change. The changes are 
 * represented usign a set of <code>SyncInfo</code>.
 * 
 * @since 3.1
 */
public abstract class ChangeSet {

    private String name;
    
    private final SyncInfoTree set = new SyncInfoTree();

    /**
     * Create a change set with no name. Subclasses
     * that create a change set in this manner should
     * provide a name before the set is used by other clients.
     */
    protected ChangeSet() {
        super();
    }
    
    /**
     * Create a change set with the given name.
     */
    public ChangeSet(String name) {
        this.name = name;
    }
    
    /**
     * Return the SyncInfoSet that contains the resources that belong to this change set.
     * @return  the SyncInfoSet that contains the resources that belong to this change set
     */
    public SyncInfoTree getSyncInfoSet() {
        return set;
    }

    /**
     * Return the resources that are contained in this set.
     * @return the resources that are contained in this set
     */
    public IResource[] getResources() {
        return set.getResources();
    }
    
    /**
     * Return whether the set contains any files.
     * @return whether the set contains any files
     */
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * Return true if the given file is included in this set.
     * @param local a ocal file
     * @return true if the given file is included in this set
     */
    public boolean contains(IResource local) {
        return set.getSyncInfo(local) != null;
    }
    
    /**
     * Add the resource to this set if it is modified
     * w.r.t. the subscriber.
     * @param resource
     * @throws TeamException
     */
    public void add(SyncInfo info) {
        if (isValidChange(info)) {
            set.add(info);
        }
    }
    
    /**
     * Return whether the given sync info is a valid change
     * and can be included in this set. This method is used
     * by the <code>add</code> method to filter set additions.
     * @param info a sync info
     * @return whether the sync info is a valid member of this set
     */
    protected boolean isValidChange(SyncInfo info) {
        return (info != null);
    }

    /**
     * Add the resources to this set if they are modified
     * w.r.t. the subscriber.
     * @param resources the resources to be added.
     * @throws TeamException
     */
    public void add(SyncInfo[] infos) {
       try {
           set.beginInput();
           for (int i = 0; i < infos.length; i++) {
              SyncInfo info = infos[i];
              add(info);
           }
       } finally {
           set.endInput(null);
       }
    }
    
    /**
     * Remove the resource from the set.
     * @param resource the resource to be removed
     */
    public void remove(IResource resource) {
        if (contains(resource)) {
            set.remove(resource);
        }
    }
    
    /**
     * Remove the resources from the set.
     * @param resources the resources to be removed
     */
    public void remove(IResource[] resources) {
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            remove(resource);
        }
    }
    
    /**
     * Remove the resource and it's descendants to the given depth.
     * @param resource the resource to be removed
     * @param depth the depth of the removal (one of
     * <code>IResource.DEPTH_ZERO, IResource.DEPTH_ONE, IResource.DEPTH_INFINITE)</code>
     */
    public void rootRemoved(IResource resource, int depth) {
        SyncInfo[] infos = set.getSyncInfos(resource, depth);
        if (infos.length > 0) {
            IResource[] resources = new IResource[infos.length];
            for (int i = 0; i < resources.length; i++) {
                resources[i] = infos[i].getLocal();
            }
            set.removeAll(resources);
        }
    }
    
    /**
     * Return a comment describing the change.
     * @return a comment describing the change
     */
    public abstract String getComment();

    /**
     * Return the name assigned to this set. The name should be
     * unique.
     * @return the name assigned to this set
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the change set. The name of a change
     * set can be changed but it is up to subclass to notify
     * any interested partied of the name change.
     * @param name the new name for the set
     */
    protected void setName(String name) {
        this.name = name;
    }
}
