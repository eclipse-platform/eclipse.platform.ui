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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.client.Command;
import org.eclipse.team.internal.ccvs.core.client.Commit;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.operations.CommitOperation;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPart;

/**
 * A commit set is used to associate a comment with a set of outgoing
 * file modifications. If the comment of the set is <code>null</code>,
 * the title of the commit set will be used as the comment when committing
 */
public class CommitSet {
    
    private static final String CTX_REVISION = "revision"; //$NON-NLS-1$
    private static final String CTX_PATH = "path"; //$NON-NLS-1$
    private static final String CTX_FILES = "files"; //$NON-NLS-1$
    private static final String CTX_TITLE = "title"; //$NON-NLS-1$
    private static final String CTX_COMMENT = "comment"; //$NON-NLS-1$
    
    private String title;
    private String comment;
    private Map dirtyFiles; // Maps IFile->String(revision)
    

    /**
     * Restore a commit set from the given memento
     * @param memento the memento to which the set was saved
     * @return the restored set
     */
    public static CommitSet from(IMemento memento) {
        CommitSet set = new CommitSet();
        set.init(memento);
        return set;
    }
    
    private CommitSet() {
        dirtyFiles = new HashMap();
    }
    
    /**
     * Create a commit set with the given title.
     * @param title the title for the commit set
     */
    /* package */ CommitSet(String title) {
        this();
        this.title = title;
    }
    
    /**
     * Get the title of the commit set. The title is used
     * as the comment when the set is committed if no comment
     * has been explicitly set using <code>setComment</code>.
     * @return the title of the set
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title of the set. The title is used
     * as the comment when the set is committed if no comment
     * has been explicitly set using <code>setComment</code>.
     * @param title the title of the set
     */
    public void setTitle(String title) {
        this.title = title;
        CommitSetManager.getInstance().titleChanged(this);
    }
    
    /**
     * Get the comment of this commit set. If the comment
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
     * Set the comment to be used when the commit set is committed.
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
    
    /**
     * Add the dirty files in the given array to the commit set.
     * The list of files that were added is returned.
     * @param files the files to be added to the set
     * @return the files that were added because they were dirty
     * @throws CVSException if the dirty state or revision of one of the files could not be determined
     */
    public IFile[] addFiles(IResource[] files) throws CVSException {
        List addedFiles = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            IResource file = files[i];
            if (file.getType() == IResource.FILE && addFile((IFile)file)) {
                addedFiles.add(file);
            }
        }
        IFile[] fileArray = (IFile[]) addedFiles.toArray(new IFile[addedFiles.size()]);
        if (fileArray.length > 0) {
            CommitSetManager.getInstance().filesAdded(this, fileArray);
        }
        return fileArray;
    }

    /**
     * Remove the given files from this set.
     * @param files the files to be removed
     */
    public void removeFiles(IFile[] files) {
        List removed = new ArrayList();
        for (int i = 0; i < files.length; i++) {
            IFile file = files[i];
            if (dirtyFiles.remove(file) != null) {
                removed.add(file);
            }
        }
        if (!removed.isEmpty()) {
            CommitSetManager.getInstance().filesChanged(this, (IFile[]) removed.toArray(new IFile[removed.size()]));
        }
    }
    
    private boolean addFile(IFile file) throws CVSException {
        ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
        if (!cvsFile.isModified(null)) {
            return false;
        }
        byte[] syncBytes = cvsFile.getSyncBytes();
        String revision;
        if (syncBytes == null) {
            revision = ResourceSyncInfo.ADDED_REVISION;
        } else {
            revision = ResourceSyncInfo.getRevision(syncBytes);
        }
        addFile(file, revision);
        return true;
    }

    private boolean isModified(IResource resource) {
        try {
            ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
            return cvsResource.isModified(null);
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
            return true;
        }
    }
    
    private void addFile(IFile file, String revision) {
        dirtyFiles.put(file, revision);
    }
    
    private String getRevision(IFile file) {
        return (String)dirtyFiles.get(file);
    }
    
    public void save(IMemento memento) {
        memento.putString(CTX_TITLE, getTitle());
        if (comment != null) {
            memento.putString(CTX_COMMENT, comment);
        }
        for (Iterator iter = dirtyFiles.keySet().iterator(); iter.hasNext();) {
            IFile file = (IFile) iter.next();
	        IMemento child = memento.createChild(CTX_FILES);
	        child.putString(CTX_PATH, file.getFullPath().toString());
	        child.putString(CTX_REVISION, getRevision(file));
        }
    }

    public void init(IMemento memento) {
        title = memento.getString(CTX_TITLE);
        comment = memento.getString(CTX_COMMENT);
        IMemento[] children = memento.getChildren(CTX_FILES);
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for (int i = 0; i < children.length; i++) {
            IMemento child = children[i];
            String path = child.getString(CTX_PATH);
            String revision = child.getString(CTX_REVISION);
            IFile file = root.getFile(new Path(path));
            addFile(file, revision);
        }
    }

    /**
     * The given project is no longer under CVS control.
     * Remoev any files that may have been included in the
     * commit set.
     */
    /* package*/ void projectRemoved(IProject project) {
        List filesToRemove = new ArrayList();
        for (Iterator iter = dirtyFiles.keySet().iterator(); iter.hasNext();) {
            IFile file = (IFile) iter.next();
            if (file.getProject().equals(project)) {
                filesToRemove.add(file);
            }
        }
        removeFiles((IFile[]) filesToRemove.toArray(new IFile[filesToRemove.size()]));
    }

    /**
     * The sync state of the given resources has changed. If any of them are in this
     * set, adjust the state accordingly.
     * @param changedResources the resources whose sync state has changed
     */
    /* package*/ void resourceSyncInfoChanged(IResource[] changedResources) {
        List filesToRemove = new ArrayList();
        for (int i = 0; i < changedResources.length; i++) {
            IResource resource = changedResources[i];
            if (dirtyFiles.containsKey(resource) && !isModified(resource)) {
                filesToRemove.add(resource);
            }
        }
        removeFiles((IFile[]) filesToRemove.toArray(new IFile[filesToRemove.size()]));
    }

    /**
     * Commit the files in this commit set to the repository.
     * @param monitor a progress monitor
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    public void commit(IWorkbenchPart part, IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        IFile[] files = getFiles();
        new CommitOperation(part, files, new Command.LocalOption[] { Commit.makeArgumentOption(Command.MESSAGE_OPTION, getComment()) })
        	.run(monitor);
        // TODO: Handle set archival
    }

    /**
     * Return the dirty files contained in this set.
     * @return the dirty files contained in this set
     */
    public IFile[] getFiles() {
        return (IFile[]) dirtyFiles.keySet().toArray(new IFile[dirtyFiles.size()]);
    }

    /**
     * Return whether the set contains any files.
     * @return whether the set contains any files
     */
    public boolean isEmpty() {
        return dirtyFiles.isEmpty();
    }

    /**
     * Return true if the given file is included in this set.
     * @param local a ocal file
     * @return true if the given file is included in this set
     */
    public boolean contains(IResource local) {
        return dirtyFiles.containsKey(local);
    }

    /**
     * Return whether the set has a comment that differs from the title.
     * @return whether the set has a comment that differs from the title
     */
    public boolean hasComment() {
        return comment != null;
    }

}
