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

import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.TeamPlugin;
import org.osgi.service.prefs.Preferences;

/**
 * An active change set represents a set of local resource changes
 * that are grouped together as a single logical change.
 * @since 3.1
 */
public class ActiveChangeSet extends ChangeSet {
    
    private static final String CTX_TITLE = "title"; //$NON-NLS-1$
    private static final String CTX_COMMENT = "comment"; //$NON-NLS-1$
    private static final String CTX_RESOURCES = "resources"; //$NON-NLS-1$
    
    private String comment;
    private final SubscriberChangeSetCollector manager;
    
	/**
	 * Create a change set with the given title
	 * @param manager the manager that owns this set
     * @param title the title of the set
     */
    public ActiveChangeSet(SubscriberChangeSetCollector manager, String title) {
        super(title);
        this.manager = manager;
    }

    /**
     * Get the title of the change set. The title is used
     * as the comment when the set is checking in if no comment
     * has been explicitly set using <code>setComment</code>.
     * @return the title of the set
     */
    public String getTitle() {
        return getName();
    }
    
    /**
     * Set the title of the set. The title is used
     * as the comment when the set is committed if no comment
     * has been explicitly set using <code>setComment</code>.
     * @param title the title of the set
     */
    public void setTitle(String title) {
        setName(title);
        getManager().fireNameChangedEvent(this);
    }

    /**
     * Get the comment of this change set. If the comment
     * as never been set, the title is returned as the comment
     * @return the comment to be used when the set is committed
     */
    public String getComment() {
        if (comment == null) {
            return getTitle();
        }
        return comment;
    }
    
    /**
     * Set the comment to be used when the change set is committed.
     * If <code>null</code> is passed, the title of the set
     * will be used as the comment.
     * @param comment the comment for the set or <code>null</code>
     * if the title should be the comment
     */
    public void setComment(String comment) {
        if (comment != null && comment.equals(getTitle())) {
            this.comment = null;
        } else {
            this.comment = comment;
        }
    }

    /*
     * Override inherited method to only include outgoing changes
     */
    protected boolean isValidChange(SyncInfo info) {
        return getManager().isModified(info);
    }

    private void addResource(IResource resource) throws TeamException {
        Subscriber subscriber = getManager().getSubscriber();
        SyncInfo info = subscriber.getSyncInfo(resource);
        if (info != null) {
            add(info);
        }
    }

    private SubscriberChangeSetCollector getManager() {
        return manager;
    }

    /**
     * Return whether the set has a comment that differs from the title.
     * @return whether the set has a comment that differs from the title
     */
    public boolean hasComment() {
        return comment != null;
    }
    
    public void save(Preferences prefs) {
        prefs.put(CTX_TITLE, getTitle());
        if (comment != null) {
            prefs.put(CTX_COMMENT, comment);
        }
        if (!isEmpty()) {
	        StringBuffer buffer = new StringBuffer();
	        IResource[] resources = getResources();
	        for (int i = 0; i < resources.length; i++) {
                IResource resource = resources[i];
	            buffer.append(resource.getFullPath().toString());
	            buffer.append('\n');
	        }
	        prefs.put(CTX_RESOURCES, buffer.toString());
        }
    }

    public void init(Preferences prefs) {
        setName(prefs.get(CTX_TITLE, "")); //$NON-NLS-1$
        comment = prefs.get(CTX_COMMENT, null);
        String resourcePaths = prefs.get(CTX_RESOURCES, null);
        if (resourcePaths != null) {
            try {
                getSyncInfoSet().beginInput();
	            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
	            StringTokenizer tokenizer = new StringTokenizer(resourcePaths, "\n"); //$NON-NLS-1$
	            while (tokenizer.hasMoreTokens()) {
	                String next = tokenizer.nextToken();
	                if (next.trim().length() > 0) {
	                    IResource resource = root.findMember(next);
	                    if (resource != null) {
	                        try {
	                            addResource(resource);
	                        } catch (TeamException e) {
	                            TeamPlugin.log(e);
	                        }
	                    }
	                }
	            }
            } finally {
                getSyncInfoSet().endInput(null);
            }
        }
    }

    /**
     * Add the resources to the change set if they are outgoing changes.
     * @param resources the resouces to add.
     * @throws TeamException
     */
    public void add(IResource[] resources) throws TeamException {
        List toAdd = new ArrayList();
        for (int i = 0; i < resources.length; i++) {
            IResource resource = resources[i];
            SyncInfo info = manager.getSyncInfo(resource);
            if (info != null) {
                toAdd.add(info);
            }
        }
        if (!toAdd.isEmpty()) {
            add((SyncInfo[]) toAdd.toArray(new SyncInfo[toAdd.size()]));
        }
    }
}
