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

import java.io.*;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.util.ResourceStateChangeListeners;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;

/**
 * Thsi class keeps the active commit sets up-to-date.
 */
public class CommitSetManager extends Object implements IResourceChangeListener, IResourceStateChangeListener {
    
    private static final String FILENAME = "commitSets.xml"; //$NON-NLS-1$
    private static final String CTX_COMMIT_SETS = "commitSets"; //$NON-NLS-1$
    private static final String CTX_COMMIT_SET = "commitSet"; //$NON-NLS-1$
    private static final String CTX_DEFAULT_SET = "defaultSet"; //$NON-NLS-1$
    
    private List activeSets;
    private static CommitSetManager instance;
    private static ListenerList listeners = new ListenerList();
    private CommitSet defaultSet;
    
    /*
     * Property change event for the default set
     */
    public static final String DEFAULT_SET = "DefaultSet"; //$NON-NLS-1$
    
    public synchronized static CommitSetManager getInstance() {
        if (instance == null) {
            instance = new CommitSetManager();
        }
        return instance;
    }
    
    private CommitSetManager() {
        try {
            load();
        } catch (CoreException e) {
            // The saved commit sets could not be restored
            CVSUIPlugin.log(e);
        }
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
        ResourceStateChangeListeners.getListener().addResourceStateChangeListener(this);
    }
    
    public void shutdown() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
        ResourceStateChangeListeners.getListener().removeResourceStateChangeListener(this);
        save();
    }
    
    private void load() throws CoreException {
        activeSets = new ArrayList();
		File file = getStateFile();
		Reader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			return;
		}
		IMemento memento = XMLMemento.createReadRoot(reader);
		String defaultSetTitle = memento.getString(CTX_DEFAULT_SET);
		IMemento[] sets = memento.getChildren(CTX_COMMIT_SET);
		for (int i = 0; i < sets.length; i++) {
			IMemento child = sets[i];
			CommitSet set = CommitSet.from(child);
			if (defaultSet == null && defaultSetTitle != null && set.getTitle().equals(defaultSetTitle)) {
			    defaultSet = set;
			}
			activeSets.add(set);
		}
    }
	
    private void save() {
		XMLMemento xmlMemento = XMLMemento.createWriteRoot(CTX_COMMIT_SETS);
		for (Iterator it = activeSets.iterator(); it.hasNext(); ) {
		    CommitSet set = (CommitSet) it.next();
			if (!set.isEmpty()) {
			    IMemento child = xmlMemento.createChild(CTX_COMMIT_SET);
			    set.save(child);
			}
		}
		if (defaultSet != null) {
		    xmlMemento.putString(CTX_DEFAULT_SET, defaultSet.getTitle());
		}
		try {
			Writer writer = new BufferedWriter(new FileWriter(getStateFile()));
			try {
				xmlMemento.save(writer);
			} finally {
				writer.close();
			}
		} catch (IOException e) {
			TeamUIPlugin.log(new Status(IStatus.ERROR, TeamUIPlugin.ID, 1, "An error occurred saving the commit sets to disk.", e)); //$NON-NLS-1$
		} 
    }
    
	private File getStateFile() {
		IPath pluginStateLocation = CVSUIPlugin.getPlugin().getStateLocation();
		return pluginStateLocation.append(FILENAME).toFile(); //$NON-NLS-1$	
	}

    /* (non-Javadoc)
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        // File modifications should be handled by the resourceModified method.
        // Therefore, we are only concerned with project deletions and close
        processDelta(event.getDelta());
        
    }

	private void processDelta(IResourceDelta delta) {
		IResource resource = delta.getResource();
		int kind = delta.getKind();

		if (resource.getType() == IResource.ROOT) {
			IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.REMOVED | IResourceDelta.ADDED);
			for (int i = 0; i < affectedChildren.length; i++) {
				processDelta(affectedChildren[i]);
			}
			return;
		}
		
		if (resource.getType() == IResource.PROJECT) {
			// Handle a deleted project
			if (((kind & IResourceDelta.REMOVED) != 0)) {
				projectDeconfigured((IProject)resource);
				return;
			}
			// Handle a closed project
			if ((delta.getFlags() & IResourceDelta.OPEN) != 0 && !((IProject) resource).isOpen()) {
			    projectDeconfigured((IProject)resource);
				return;
			}
		}
	}
		
    private Object[] getListeners() {
        return listeners.getListeners();
    }
    
    /**
     * The title of the given set has changed. Notify any listeners.
     */
    /* package */ void titleChanged(final CommitSet set) {
        if (activeSets.contains(set)) {
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final ICommitSetChangeListener listener = (ICommitSetChangeListener)listeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        // Exceptions are logged by the platform
                    }
                    public void run() throws Exception {
                        listener.titleChanged(set);
                    }
                });
            }
        }
    }

    /**
     * The state of files in the set have changed.
     */
    /* package */void filesAdded(final CommitSet set, final IFile[] files) {
        filesChanged(set, files);
        // Remove the added files from any other set that contains them
        for (Iterator iter = activeSets.iterator(); iter.hasNext();) {
            CommitSet otherSet = (CommitSet) iter.next();
            if (otherSet != set) {
                otherSet.removeFiles(files);
            }
        }
    }
    
    /**
     * The state of files in the set have changed.
     */
    /* package */void filesChanged(final CommitSet set, final IFile[] files) {
        if (activeSets.contains(set)) {
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final ICommitSetChangeListener listener = (ICommitSetChangeListener) listeners[i];
                Platform.run(new ISafeRunnable() {
                    public void handleException(Throwable exception) {
                        // Exceptions are logged by the platform
                    }
                    public void run() throws Exception {
                        listener.filesChanged(set, files);
                    }
                });
            }
        }
    }

    private void firePropertyChange(String property, CommitSet oldDefault, CommitSet newDefault) {
        final PropertyChangeEvent event = new PropertyChangeEvent(this, property, oldDefault, newDefault);
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            final ICommitSetChangeListener listener = (ICommitSetChangeListener) listeners[i];
            Platform.run(new ISafeRunnable() {
                public void handleException(Throwable exception) {
                    // Exceptions are logged by the platform
                }
                public void run() throws Exception {
                    listener.propertyChange(event);
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceSyncInfoChanged(org.eclipse.core.resources.IResource[])
     */
    public void resourceSyncInfoChanged(IResource[] changedResources) {
        for (Iterator iter = activeSets.iterator(); iter.hasNext();) {
            CommitSet set = (CommitSet) iter.next();
            if (!set.isEmpty()) {
                // Don't forward the event if the set is empty
	            set.resourceSyncInfoChanged(changedResources);
	            if (set.isEmpty()) {
	                remove(set);
	            }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#externalSyncInfoChange(org.eclipse.core.resources.IResource[])
     */
    public void externalSyncInfoChange(IResource[] changedResources) {
        resourceSyncInfoChanged(changedResources);
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#resourceModified(org.eclipse.core.resources.IResource[])
     */
    public void resourceModified(IResource[] changedResources) {
        // First, remove any clean files from active commit sets
        resourceSyncInfoChanged(changedResources);
        // Next, add any dirty files that are not in an active set to the default set
        if (defaultSet != null) {
            considerForDefaultSet(changedResources);
        }
    }

    private void considerForDefaultSet(IResource[] changedResources) {
        List filesToAdd = new ArrayList();
        for (int i = 0; i < changedResources.length; i++) {
            IResource resource = changedResources[i];
            if (isDirtyFile(resource) && !isInActiveSet(resource)) {
                filesToAdd.add(resource);
            }
        }
        if (!filesToAdd.isEmpty()) {
            try {
                defaultSet.addFiles((IFile[]) filesToAdd.toArray(new IFile[filesToAdd.size()]));
            } catch (CVSException e) {
                CVSUIPlugin.log(e);
            }
        }
    }

    private boolean isInActiveSet(IResource resource) {
        for (Iterator iter = activeSets.iterator(); iter.hasNext();) {
            CommitSet set = (CommitSet) iter.next();
            if (set.contains(resource)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDirtyFile(IResource resource) {
        try {
            if (resource.getType() != IResource.FILE) return false;
            ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor((IFile)resource);
            return cvsFile.isModified(null);
        } catch (CVSException e) {
            CVSUIPlugin.log(e);
            return true;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectConfigured(org.eclipse.core.resources.IProject)
     */
    public void projectConfigured(IProject project) {
        if (defaultSet != null) {
            // Need to scan for outgoing changes and add them to the default set
            CVSWorkspaceSubscriber subscriber = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
            // TODO: Should be done in a background job
            SyncInfoSet syncSet = new SyncInfoSet();
            subscriber.collectOutOfSync(new IResource[] { project }, IResource.DEPTH_INFINITE, syncSet, new NullProgressMonitor());
            syncSet.selectNodes(ChangeLogModelProvider.OUTGOING_FILE_FILTER);
            considerForDefaultSet(syncSet.getResources());
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.team.internal.ccvs.core.IResourceStateChangeListener#projectDeconfigured(org.eclipse.core.resources.IProject)
     */
    public void projectDeconfigured(IProject project) {
        for (Iterator iter = activeSets.iterator(); iter.hasNext();) {
            CommitSet set = (CommitSet) iter.next();
            set.projectRemoved(project);
        }
    }

    /**
     * Create a commit set with the given title and files. The created
     * set is not added to the control of the commit set manager
     * so no events are fired. The set can be added using the
     * <code>add</code> method.
     * @param title the title of the commit set
     * @param files the files contained in the set
     * @return the created set
     * @throws CVSException
     */
    public CommitSet createCommitSet(String title, IResource[] files) throws CVSException {
        CommitSet commitSet = new CommitSet(title);
        if (files != null && files.length > 0)
            commitSet.addFiles(files);
        return commitSet;
    }

    /**
     * Add the set to the list of active sets.
     * @param set the set to be added
     */
    public void add(final CommitSet set) {
        if (!contains(set)) {
            activeSets.add(set);
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final ICommitSetChangeListener listener = (ICommitSetChangeListener)listeners[i];
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
    public void remove(final CommitSet set) {
        if (contains(set)) {
            activeSets.remove(set);
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                final ICommitSetChangeListener listener = (ICommitSetChangeListener)listeners[i];
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
     * Return whether the manager contains the given commit set
     * @param set the commit set being tested
     * @return whether the set is contained in the manager's list of active sets
     */
    public boolean contains(CommitSet set) {
        return activeSets.contains(set);
    }

    /**
     * Add the listener to the set of registered listeners.
     * @param listener the listener to be added
     */
    public void addListener(ICommitSetChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the listener from the set of registered listeners.
     * @param listener the listener to remove
     */
    public void removeListener(ICommitSetChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Return the list of active commit sets.
     * @return the list of active commit sets
     */
    public CommitSet[] getSets() {
        return (CommitSet[]) activeSets.toArray(new CommitSet[activeSets.size()]);
    }

    /**
     * Make the given set the default set into which all new modifications
     * that ae not already in another set go.
     * @param set the set which is to become the default set
     */
    public void makeDefault(CommitSet set) {
        CommitSet oldDefault = defaultSet;
        defaultSet = set;
        firePropertyChange(DEFAULT_SET, oldDefault, defaultSet);
    }

    /**
     * Retrn the set which is currently the default or
     * <code>null</code> if there is no default set.
     * @return
     */
    public CommitSet getDefaultCommitSet() {
        return defaultSet;
    }
    /**
     * Return whether the given set is the default set into which all
     * new modifications will be placed.
     * @param set the set to test
     * @return whether the set is the default set
     */
    public boolean isDefault(CommitSet set) {
        return set == defaultSet;
    }

}
