/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.*;
import java.util.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;

public class SaveManager implements IElementInfoFlattener, IManager, IStringPoolParticipant {
	protected static final String CLEAR_DELTA_PREFIX = "clearDelta_"; //$NON-NLS-1$
	protected static final String DELTA_EXPIRATION_PREFIX = "deltaExpiration_"; //$NON-NLS-1$
	protected static final int DONE_SAVING = 3;

	/**
	 * The minimum delay, in milliseconds, between workspace snapshots
	 */
	private static final long MIN_SNAPSHOT_DELAY = 1000 * 30L; //30 seconds

	/**
	 * The number of empty operations that are equivalent to a single non-
	 * trivial operation.
	 */
	protected static final int NO_OP_THRESHOLD = 20;

	/** constants */
	protected static final int PREPARE_TO_SAVE = 1;
	protected static final int ROLLBACK = 4;
	protected static final String SAVE_NUMBER_PREFIX = "saveNumber_"; //$NON-NLS-1$
	protected static final int SAVING = 2;
	protected ElementTree lastSnap;
	protected Properties masterTable;

	/**
	 * A flag indicating that a save operation is occurring.  This is a signal
	 * that snapshot should not be scheduled if a nested operation occurs during
	 * save.
	 */
	private boolean isSaving = false;

	/**
	 * The number of empty (non-changing) operations since the last snapshot.
	 */
	protected int noopCount = 0;
	/**
	 * The number of non-trivial operations since the last snapshot.
	 */
	protected int operationCount = 0;

	// Count up the time taken for all saves/snaps on markers and sync info
	protected long persistMarkers = 0l;
	protected long persistSyncInfo = 0l;

	/** 
	 * In-memory representation of plugins saved state. Maps String (plugin id)-> SavedState.
	 * This map is accessed from API that is not synchronized, so it requires
	 * independent synchronization. This is accomplished using a synchronized
	 * wrapper map.
	 */
	protected Map savedStates;

	/**
	 * Plugins that participate on a workspace save. Maps (Plugin -> ISaveParticipant).
	 * This map is accessed from API that is not synchronized, so it requires
	 * independent synchronization. This is accomplished using a synchronized
	 * wrapper map.
	 */
	protected Map saveParticipants;

	protected final DelayedSnapshotJob snapshotJob;

	protected boolean snapshotRequested;
	protected Workspace workspace;
	//declare debug messages as fields to get sharing
	private static final String DEBUG_START = " starting..."; //$NON-NLS-1$
	private static final String DEBUG_FULL_SAVE = "Full save on workspace: "; //$NON-NLS-1$
	private static final String DEBUG_PROJECT_SAVE = "Save on project "; //$NON-NLS-1$
	private static final String DEBUG_SNAPSHOT = "Snapshot: "; //$NON-NLS-1$
	private static final int TREE_BUFFER_SIZE = 1024 * 64;//64KB buffer

	public SaveManager(Workspace workspace) {
		this.workspace = workspace;
		this.snapshotJob = new DelayedSnapshotJob(this);
		snapshotRequested = false;
		saveParticipants = Collections.synchronizedMap(new HashMap(10));
	}

	public ISavedState addParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException {
		// If the plugin was already registered as a save participant we return null
		if (saveParticipants.put(plugin, participant) != null)
			return null;
		String id = plugin.getBundle().getSymbolicName();
		SavedState state = (SavedState) savedStates.get(id);
		if (state != null) {
			if (isDeltaCleared(id)) {
				// this plugin was marked not to receive deltas
				state.forgetTrees();
				removeClearDeltaMarks(id);
			} else {
				try {
					// thread safety: (we need to guarantee that the tree is immutable when computing deltas)
					// so, the tree inside the saved state needs to be immutable
					workspace.prepareOperation(null, null);
					workspace.beginOperation(true);
					state.newTree = workspace.getElementTree();
				} finally {
					workspace.endOperation(null, false, null);
				}
				return state;
			}
		}
		// if the plug-in has a previous save number, we return a state, otherwise we return null
		if (getSaveNumber(id) > 0)
			return new SavedState(workspace, id, null, null);
		return null;
	}

	protected void broadcastLifecycle(final int lifecycle, Map contexts, final MultiStatus warnings, IProgressMonitor monitor) {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", contexts.size()); //$NON-NLS-1$
			for (final Iterator it = contexts.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				Plugin plugin = (Plugin) entry.getKey();
				final ISaveParticipant participant = (ISaveParticipant) saveParticipants.get(plugin);
				//save participants can be removed concurrently
				if (participant == null) {
					monitor.worked(1);
					continue;
				}
				final SaveContext context = (SaveContext) entry.getValue();
				/* Be extra careful when calling lifecycle method on arbitrary plugin */
				ISafeRunnable code = new ISafeRunnable() {

					public void handleException(Throwable e) {
						String message = Messages.resources_saveProblem;
						IStatus status = new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e);
						warnings.add(status);

						/* Remove entry for defective plug-in from this save operation */
						it.remove();
					}

					public void run() throws Exception {
						executeLifecycle(lifecycle, participant, context);
					}
				};
				SafeRunner.run(code);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Remove the delta expiration timestamp from the master table, either
	 * because the saved state has been processed, or the delta has expired.
	 */
	protected void clearDeltaExpiration(String pluginId) {
		masterTable.remove(DELTA_EXPIRATION_PREFIX + pluginId);
	}

	protected void cleanMasterTable() {
		//remove tree file entries for everything except closed projects
		for (Iterator it = masterTable.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (!key.endsWith(LocalMetaArea.F_TREE))
				continue;
			String prefix = key.substring(0, key.length() - LocalMetaArea.F_TREE.length());
			//always save the root tree entry
			if (prefix.equals(Path.ROOT.toString()))
				continue;
			IProject project = workspace.getRoot().getProject(prefix);
			if (!project.exists() || project.isOpen())
				it.remove();
		}
		IPath location = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES);
		IPath backup = workspace.getMetaArea().getBackupLocationFor(location);
		try {
			saveMasterTable(backup);
		} catch (CoreException e) {
			Policy.log(e.getStatus());
			backup.toFile().delete();
			return;
		}
		if (location.toFile().exists() && !location.toFile().delete())
			return;
		try {
			saveMasterTable(location);
		} catch (CoreException e) {
			Policy.log(e.getStatus());
			location.toFile().delete();
			return;
		}
		backup.toFile().delete();
	}

	/**
	 * Marks the current participants to not receive deltas next time they are registered
	 * as save participants. This is done in order to maintain consistency if we crash
	 * after a snapshot. It would force plug-ins to rebuild their state.
	 */
	protected void clearSavedDelta() {
		synchronized (saveParticipants) {
			for (Iterator i = saveParticipants.keySet().iterator(); i.hasNext();) {
				String pluginId = ((Plugin) i.next()).getBundle().getSymbolicName();
				masterTable.setProperty(CLEAR_DELTA_PREFIX + pluginId, "true"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Collects the set of ElementTrees we are still interested in,
	 * and removes references to any other trees.
	 */
	protected void collapseTrees() throws CoreException {
		//collect trees we're interested in

		//trees for plugin saved states
		ArrayList trees = new ArrayList();
		synchronized (savedStates) {
			for (Iterator i = savedStates.values().iterator(); i.hasNext();) {
				SavedState state = (SavedState) i.next();
				if (state.oldTree != null) {
					trees.add(state.oldTree);
				}
			}
		}

		//trees for builders
		IProject[] projects = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (project.isOpen()) {
				ArrayList builderInfos = workspace.getBuildManager().createBuildersPersistentInfo(project);
				if (builderInfos != null) {
					for (Iterator it = builderInfos.iterator(); it.hasNext();) {
						BuilderPersistentInfo info = (BuilderPersistentInfo) it.next();
						trees.add(info.getLastBuiltTree());
					}
				}
			}
		}

		//no need to collapse if there are no trees at this point
		if (trees.isEmpty())
			return;

		//the complete tree
		trees.add(workspace.getElementTree());

		//collapse the trees
		//sort trees in topological order, and set the parent of each
		//tree to its parent in the topological ordering.
		ElementTree[] treeArray = new ElementTree[trees.size()];
		trees.toArray(treeArray);
		ElementTree[] sorted = sortTrees(treeArray);
		// if there was a problem sorting the tree, bail on trying to collapse.  
		// We will be able to GC the layers at a later time.
		if (sorted == null)
			return;
		for (int i = 1; i < sorted.length; i++)
			sorted[i].collapseTo(sorted[i - 1]);
	}

	protected void commit(Map contexts) throws CoreException {
		for (Iterator i = contexts.values().iterator(); i.hasNext();)
			((SaveContext) i.next()).commit();
	}

	/**
	 * Given a collection of save participants, compute the collection of
	 * <code>SaveContexts</code> to use during the save lifecycle.
	 * The keys are plugins and values are SaveContext objects.
	 */
	protected Map computeSaveContexts(Plugin[] plugins, int kind, IProject project) {
		HashMap result = new HashMap(plugins.length);
		for (int i = 0; i < plugins.length; i++) {
			Plugin plugin = plugins[i];
			try {
				SaveContext context = new SaveContext(plugin, kind, project);
				result.put(plugin, context);
			} catch (CoreException e) {
				// FIXME: should return a status to the user and not just log it
				Policy.log(e.getStatus());
			}
		}
		return result;
	}

	/**
	 * Returns a table mapping having the plug-in id as the key and the old tree
	 * as the value.
	 * This table is based on the union of the current savedStates</code> 
	 * and the given table of contexts.  The specified tree is used as the tree for
	 * any newly created saved states.  This method is used to compute the set of
	 * saved states to be written out.
	 */
	protected Map computeStatesToSave(Map contexts, ElementTree current) {
		HashMap result = new HashMap(savedStates.size() * 2);
		synchronized (savedStates) {
			for (Iterator i = savedStates.values().iterator(); i.hasNext();) {
				SavedState state = (SavedState) i.next();
				if (state.oldTree != null)
					result.put(state.pluginId, state.oldTree);
			}
		}
		for (Iterator i = contexts.values().iterator(); i.hasNext();) {
			SaveContext context = (SaveContext) i.next();
			if (!context.isDeltaNeeded())
				continue;
			String pluginId = context.getPlugin().getBundle().getSymbolicName();
			result.put(pluginId, current);
		}
		return result;
	}

	protected void executeLifecycle(int lifecycle, ISaveParticipant participant, SaveContext context) throws CoreException {
		switch (lifecycle) {
			case PREPARE_TO_SAVE :
				participant.prepareToSave(context);
				break;
			case SAVING :
				try {
					if (ResourceStats.TRACE_SAVE_PARTICIPANTS)
						ResourceStats.startSave(participant);
					participant.saving(context);
				} finally {
					if (ResourceStats.TRACE_SAVE_PARTICIPANTS)
						ResourceStats.endSave();
				}
				break;
			case DONE_SAVING :
				participant.doneSaving(context);
				break;
			case ROLLBACK :
				participant.rollback(context);
				break;
			default :
				Assert.isTrue(false, "Invalid save lifecycle code"); //$NON-NLS-1$
		}
	}

	public void forgetSavedTree(String pluginId) {
		if (pluginId == null) {
			synchronized (savedStates) {
				for (Iterator i = savedStates.values().iterator(); i.hasNext();)
					((SavedState) i.next()).forgetTrees();
			}
		} else {
			SavedState state = (SavedState) savedStates.get(pluginId);
			if (state != null)
				state.forgetTrees();
		}
	}

	/**
	 * Used in the policy for cleaning up tree's of plug-ins that are not often activated.
	 */
	protected long getDeltaExpiration(String pluginId) {
		String result = masterTable.getProperty(DELTA_EXPIRATION_PREFIX + pluginId);
		return (result == null) ? System.currentTimeMillis() : new Long(result).longValue();
	}

	protected Properties getMasterTable() {
		return masterTable;
	}

	public int getSaveNumber(String pluginId) {
		String value = masterTable.getProperty(SAVE_NUMBER_PREFIX + pluginId);
		return (value == null) ? 0 : new Integer(value).intValue();
	}

	protected Plugin[] getSaveParticipantPlugins() {
		synchronized (saveParticipants) {
			return (Plugin[]) saveParticipants.keySet().toArray(new Plugin[saveParticipants.size()]);
		}
	}

	/**
	 * Hooks the end of a save operation, for debugging and performance
	 * monitoring purposes.
	 */
	private void hookEndSave(int kind, IProject project, long start) {
		if (ResourceStats.TRACE_SNAPSHOT && kind == ISaveContext.SNAPSHOT)
			ResourceStats.endSnapshot();
		if (Policy.DEBUG_SAVE) {
			String endMessage = null;
			switch (kind) {
				case ISaveContext.FULL_SAVE :
					endMessage = DEBUG_FULL_SAVE;
					break;
				case ISaveContext.SNAPSHOT :
					endMessage = DEBUG_SNAPSHOT;
					break;
				case ISaveContext.PROJECT_SAVE :
					endMessage = DEBUG_PROJECT_SAVE + project.getFullPath() + ": "; //$NON-NLS-1$
					break;
			}
			if (endMessage != null)
				System.out.println(endMessage + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$
		}
	}

	/**
	 * Hooks the start of a save operation, for debugging and performance
	 * monitoring purposes.
	 */
	private void hookStartSave(int kind, Project project) {
		if (ResourceStats.TRACE_SNAPSHOT && kind == ISaveContext.SNAPSHOT)
			ResourceStats.startSnapshot();
		if (Policy.DEBUG_SAVE) {
			switch (kind) {
				case ISaveContext.FULL_SAVE :
					System.out.println(DEBUG_FULL_SAVE + DEBUG_START);
					break;
				case ISaveContext.SNAPSHOT :
					System.out.println(DEBUG_SNAPSHOT + DEBUG_START);
					break;
				case ISaveContext.PROJECT_SAVE :
					System.out.println(DEBUG_PROJECT_SAVE + project.getFullPath() + DEBUG_START);
					break;
			}
		}
	}

	/**
	 * Initializes the snapshot mechanism for this workspace.
	 */
	protected void initSnap(IProgressMonitor monitor) throws CoreException {
		//discard any pending snapshot request
		snapshotJob.cancel();
		//the "lastSnap" tree must be frozen as the exact tree obtained from startup,
		// otherwise ensuing snapshot deltas may be based on an incorrect tree (see bug 12575)
		lastSnap = workspace.getElementTree();
		lastSnap.immutable();
		workspace.newWorkingTree();
		operationCount = 0;
		// delete the snapshot file, if any
		IPath snapPath = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
		java.io.File file = snapPath.toFile();
		if (file.exists())
			file.delete();
		if (file.exists()) {
			String message = Messages.resources_snapInit;
			throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, null, message, null);
		}
	}

	protected boolean isDeltaCleared(String pluginId) {
		String clearDelta = masterTable.getProperty(CLEAR_DELTA_PREFIX + pluginId);
		return clearDelta != null && clearDelta.equals("true"); //$NON-NLS-1$
	}

	protected boolean isOldPluginTree(String pluginId) {
		// first, check if this plug-ins was marked not to receive a delta
		if (isDeltaCleared(pluginId))
			return false;
		//see if the plugin is still installed
		if (Platform.getBundle(pluginId) == null)
			return true;

		//finally see if the delta has past its expiry date
		long deltaAge = System.currentTimeMillis() - getDeltaExpiration(pluginId);
		return deltaAge > workspace.internalGetDescription().getDeltaExpiration();
	}

	/**
	 * @see IElementInfoFlattener#readElement(IPath, DataInput)
	 */
	public Object readElement(IPath path, DataInput input) throws IOException {
		Assert.isNotNull(path);
		Assert.isNotNull(input);
		// read the flags and pull out the type.  
		int flags = input.readInt();
		int type = (flags & ICoreConstants.M_TYPE) >> ICoreConstants.M_TYPE_START;
		ResourceInfo info = workspace.newElement(type);
		info.readFrom(flags, input);
		return info;
	}

	/**
	 * Remove marks from current save participants. This marks prevent them to receive their
	 * deltas when they register themselves as save participants.
	 */
	protected void removeClearDeltaMarks() {
		synchronized (saveParticipants) {
			for (Iterator i = saveParticipants.keySet().iterator(); i.hasNext();) {
				String pluginId = ((Plugin) i.next()).getBundle().getSymbolicName();
				removeClearDeltaMarks(pluginId);
			}
		}
	}

	protected void removeClearDeltaMarks(String pluginId) {
		masterTable.setProperty(CLEAR_DELTA_PREFIX + pluginId, "false"); //$NON-NLS-1$
	}

	protected void removeFiles(java.io.File root, String[] candidates, List exclude) {
		for (int i = 0; i < candidates.length; i++) {
			boolean delete = true;
			for (ListIterator it = exclude.listIterator(); it.hasNext();) {
				String s = (String) it.next();
				if (s.equals(candidates[i])) {
					it.remove();
					delete = false;
					break;
				}
			}
			if (delete)
				new java.io.File(root, candidates[i]).delete();
		}
	}

	private void removeGarbage(DataOutputStream output, IPath location, IPath tempLocation) throws IOException {
		if (output.size() == 0) {
			output.close();
			location.toFile().delete();
			tempLocation.toFile().delete();
		}
	}

	public void removeParticipant(Plugin plugin) {
		saveParticipants.remove(plugin);
	}

	protected void removeUnusedSafeTables() {
		List valuables = new ArrayList(10);
		IPath location = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES);
		valuables.add(location.lastSegment()); // add master table
		for (Enumeration e = masterTable.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			if (key.startsWith(SAVE_NUMBER_PREFIX)) {
				String pluginId = key.substring(SAVE_NUMBER_PREFIX.length());
				valuables.add(workspace.getMetaArea().getSafeTableLocationFor(pluginId).lastSegment());
			}
		}
		java.io.File target = location.toFile().getParentFile();
		String[] candidates = target.list();
		if (candidates == null)
			return;
		removeFiles(target, candidates, valuables);
	}

	protected void removeUnusedTreeFiles() {
		// root resource
		List valuables = new ArrayList(10);
		IPath location = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), false);
		valuables.add(location.lastSegment());
		java.io.File target = location.toFile().getParentFile();
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(java.io.File dir, String name) {
				return name.endsWith(LocalMetaArea.F_TREE);
			}
		};
		String[] candidates = target.list(filter);
		if (candidates != null)
			removeFiles(target, candidates, valuables);

		// projects	
		IProject[] projects = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++) {
			location = workspace.getMetaArea().getTreeLocationFor(projects[i], false);
			valuables.add(location.lastSegment());
			target = location.toFile().getParentFile();
			candidates = target.list(filter);
			if (candidates != null)
				removeFiles(target, candidates, valuables);
		}
	}

	public void requestSnapshot() {
		snapshotRequested = true;
	}

	/**
	 * Reset the snapshot mechanism for the non-workspace files. This
	 * includes the markers and sync info. 
	 */
	protected void resetSnapshots(IResource resource) throws CoreException {
		Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
		String message;

		// delete the snapshot file, if any
		java.io.File file = workspace.getMetaArea().getMarkersSnapshotLocationFor(resource).toFile();
		if (file.exists())
			file.delete();
		if (file.exists()) {
			message = Messages.resources_resetMarkers;
			throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, resource.getFullPath(), message, null);
		}

		// delete the snapshot file, if any
		file = workspace.getMetaArea().getSyncInfoSnapshotLocationFor(resource).toFile();
		if (file.exists())
			file.delete();
		if (file.exists()) {
			message = Messages.resources_resetSync;
			throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, resource.getFullPath(), message, null);
		}

		// if we have the workspace root then recursive over the projects.
		// only do open projects since closed ones are saved elsewhere
		if (resource.getType() == IResource.PROJECT)
			return;
		IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++)
			resetSnapshots(projects[i]);
	}

	/**
	 * Restores the state of this workspace by opening the projects
	 * which were open when it was last saved.
	 */
	protected void restore(IProgressMonitor monitor) throws CoreException {
		if (Policy.DEBUG_RESTORE)
			System.out.println("Restore workspace: starting..."); //$NON-NLS-1$
		long start = System.currentTimeMillis();
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", 50); //$NON-NLS-1$
			// need to open the tree to restore, but since we're not 
			// inside an operation, be sure to close it afterwards
			workspace.newWorkingTree();
			try {
				String msg = Messages.resources_startupProblems;
				MultiStatus problems = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, msg, null);

				restoreMasterTable();
				// restore the saved tree and overlay the snapshots if any
				restoreTree(Policy.subMonitorFor(monitor, 10));
				restoreSnapshots(Policy.subMonitorFor(monitor, 10));

				// tolerate failure for non-critical information
				// if startup fails, the entire workspace is shot
				try {
					restoreMarkers(workspace.getRoot(), false, Policy.subMonitorFor(monitor, 10));
				} catch (CoreException e) {
					problems.merge(e.getStatus());
				}
				try {
					restoreSyncInfo(workspace.getRoot(), Policy.subMonitorFor(monitor, 10));
				} catch (CoreException e) {
					problems.merge(e.getStatus());
				}
				// restore meta info last because it might close a project if its description is not readable
				restoreMetaInfo(problems, Policy.subMonitorFor(monitor, 10));
				IProject[] roots = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
				for (int i = 0; i < roots.length; i++)
					((Project) roots[i]).startup();
				if (!problems.isOK())
					Policy.log(problems);
			} finally {
				workspace.getElementTree().immutable();
			}
		} finally {
			monitor.done();
		}
		if (Policy.DEBUG_RESTORE)
			System.out.println("Restore workspace: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Restores the contents of this project.  Throw
	 * an exception if the project could not be restored.
	 * @return <code><code>true</code> if the project data was restored successfully,
	 * and <code>false</code> if non-critical problems occurred while restoring. 
	 * @exception CoreException if the project could not be restored.
	 */
	protected boolean restore(Project project, IProgressMonitor monitor) throws CoreException {
		boolean status = true;
		if (Policy.DEBUG_RESTORE)
			System.out.println("Restore project " + project.getFullPath() + ": starting..."); //$NON-NLS-1$ //$NON-NLS-2$
		long start = System.currentTimeMillis();
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", 40); //$NON-NLS-1$
			if (project.isOpen()) {
				status = restoreTree(project, Policy.subMonitorFor(monitor, 10));
			} else {
				monitor.worked(10);
			}
			restoreMarkers(project, true, Policy.subMonitorFor(monitor, 10));
			restoreSyncInfo(project, Policy.subMonitorFor(monitor, 10));
			// restore meta info last because it might close a project if its description is not found
			restoreMetaInfo(project, Policy.subMonitorFor(monitor, 10));
		} finally {
			monitor.done();
		}
		if (Policy.DEBUG_RESTORE)
			System.out.println("Restore project " + project.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return status;
	}

	/**
	 * Reads the markers which were originally saved
	 * for the tree rooted by the given resource.
	 */
	protected void restoreMarkers(IResource resource, boolean generateDeltas, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
		long start = System.currentTimeMillis();
		MarkerManager markerManager = workspace.getMarkerManager();
		// when restoring a project, only load markers if it is open
		if (resource.isAccessible())
			markerManager.restore(resource, generateDeltas, monitor);

		// if we have the workspace root then restore markers for its projects
		if (resource.getType() == IResource.PROJECT) {
			if (Policy.DEBUG_RESTORE_MARKERS) {
				System.out.println("Restore Markers for " + resource.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return;
		}
		IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++)
			if (projects[i].isAccessible())
				markerManager.restore(projects[i], generateDeltas, monitor);
		if (Policy.DEBUG_RESTORE_MARKERS) {
			System.out.println("Restore Markers for workspace: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ 
		}
	}

	protected void restoreMasterTable() throws CoreException {
		long start = System.currentTimeMillis();
		masterTable = new Properties();
		IPath location = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES);
		java.io.File target = location.toFile();
		if (!target.exists()) {
			location = workspace.getMetaArea().getBackupLocationFor(location);
			target = location.toFile();
			if (!target.exists())
				return;
		}
		try {
			SafeChunkyInputStream input = new SafeChunkyInputStream(target);
			try {
				masterTable.load(input);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			String message = Messages.resources_exMasterTable;
			throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
		}
		if (Policy.DEBUG_RESTORE_MASTERTABLE)
			System.out.println("Restore master table for " + location + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Restores the state of this workspace by opening the projects
	 * which were open when it was last saved.
	 */
	protected void restoreMetaInfo(MultiStatus problems, IProgressMonitor monitor) {
		if (Policy.DEBUG_RESTORE_METAINFO)
			System.out.println("Restore workspace metainfo: starting..."); //$NON-NLS-1$
		long start = System.currentTimeMillis();
		IProject[] roots = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < roots.length; i++) {
			//fatal to throw exceptions during startup
			try {
				restoreMetaInfo((Project) roots[i], monitor);
			} catch (CoreException e) {
				String message = NLS.bind(Messages.resources_readMeta, roots[i].getName());
				problems.merge(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, roots[i].getFullPath(), message, e));
			}
		}
		if (Policy.DEBUG_RESTORE_METAINFO)
			System.out.println("Restore workspace metainfo: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Restores the contents of this project.  Throw an exception if the 
	 * project description could not be restored.
	 */
	protected void restoreMetaInfo(Project project, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		ProjectDescription description = null;
		CoreException failure = null;
		try {
			if (project.isOpen())
				description = workspace.getFileSystemManager().read(project, true);
			else
				//for closed projects, just try to read the legacy .prj file, 
				//because the project location is stored there.
				description = workspace.getMetaArea().readOldDescription(project);
		} catch (CoreException e) {
			failure = e;
		}
		// If we had an open project and there was an error reading the description
		// from disk, close the project and give it a default description. If the project
		// was already closed then just set a default description.
		if (description == null) {
			description = new ProjectDescription();
			description.setName(project.getName());
			//try to read private metadata and add to the description
			workspace.getMetaArea().readPrivateDescription(project, description);
		}
		project.internalSetDescription(description, false);
		if (failure != null) {
			//close the project
			project.internalClose();
			throw failure;
		}
		if (Policy.DEBUG_RESTORE_METAINFO)
			System.out.println("Restore metainfo for " + project.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Restores the workspace tree from snapshot files in the event
	 * of a crash.  The workspace tree must be open when this method
	 * is called, and will be open at the end of this method.  In the
	 * event of a crash recovery, the snapshot file is not deleted until
	 * the next successful save.
	 */
	protected void restoreSnapshots(IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
			IPath snapLocation = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
			java.io.File localFile = snapLocation.toFile();

			// If the snapshot file doesn't exist, there was no crash. 
			// Just initialize the snapshot file and return.
			if (!localFile.exists()) {
				initSnap(Policy.subMonitorFor(monitor, Policy.totalWork / 2));
				return;
			}
			// If we have a snapshot file, the workspace was shutdown without being saved or crashed.
			workspace.setCrashed(true);
			try {
				/* Read each of the snapshots and lay them on top of the current tree.*/
				ElementTree complete = workspace.getElementTree();
				complete.immutable();
				DataInputStream input = new DataInputStream(new SafeChunkyInputStream(localFile));
				try {
					WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt());
					complete = reader.readSnapshotTree(input, complete, monitor);
				} finally {
					FileUtil.safeClose(input);
					//reader returned an immutable tree, but since we're inside
					//an operation, we must return an open tree
					lastSnap = complete;
					complete = complete.newEmptyDelta();
					workspace.tree = complete;
				}
			} catch (Exception e) {
				// only log the exception, we should not fail restoring the snapshot
				message = Messages.resources_snapRead;
				Policy.log(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, null, message, e));
			}
		} finally {
			monitor.done();
		}
		if (Policy.DEBUG_RESTORE_SNAPSHOTS)
			System.out.println("Restore snapshots for workspace: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Reads the sync info which was originally saved
	 * for the tree rooted by the given resource.
	 */
	protected void restoreSyncInfo(IResource resource, IProgressMonitor monitor) throws CoreException {
		Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
		long start = System.currentTimeMillis();
		Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
		// when restoring a project, only load sync info if it is open
		if (resource.isAccessible())
			synchronizer.restore(resource, monitor);

		// restore sync info for all projects if we were given the workspace root.
		if (resource.getType() == IResource.PROJECT) {
			if (Policy.DEBUG_RESTORE_SYNCINFO) {
				System.out.println("Restore SyncInfo for " + resource.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
			return;
		}
		IProject[] projects = ((IWorkspaceRoot) resource).getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++)
			if (projects[i].isAccessible())
				synchronizer.restore(projects[i], monitor);
		if (Policy.DEBUG_RESTORE_SYNCINFO) {
			System.out.println("Restore SyncInfo for workspace: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Reads the contents of the tree rooted by the given resource from the 
	 * file system. This method is used when restoring a complete workspace 
	 * after workspace save/shutdown.
	 * @exception CoreException if the workspace could not be restored.
	 */
	protected void restoreTree(IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), false);
		IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
		if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists()) {
			savedStates = Collections.synchronizedMap(new HashMap(10));
			return;
		}
		try {
			DataInputStream input = new DataInputStream(new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString(), TREE_BUFFER_SIZE));
			try {
				WorkspaceTreeReader.getReader(workspace, input.readInt()).readTree(input, monitor);
			} finally {
				input.close();
			}
		} catch (IOException e) {
			String msg = NLS.bind(Messages.resources_readMeta, treeLocation.toOSString());
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, treeLocation, msg, e);
		}
		if (Policy.DEBUG_RESTORE_TREE) {
			System.out.println("Restore Tree for workspace: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Restores the trees for the builders of this project from the local disk.
	 * Does nothing if the tree file does not exist (this means the
	 * project has never been saved).  This method is
	 * used when restoring a saved/closed project.  restoreTree(Workspace) is
	 * used when restoring a complete workspace after workspace save/shutdown.
	 * @return <code>true</code> if the tree file exists, <code>false</code> otherwise.
	 * @exception CoreException if the project could not be restored.
	 */
	protected boolean restoreTree(Project project, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
			IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, false);
			IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
			if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists())
				return false;
			DataInputStream input = new DataInputStream(new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString()));
			try {
				WorkspaceTreeReader reader = WorkspaceTreeReader.getReader(workspace, input.readInt());
				reader.readTree(project, input, Policy.subMonitorFor(monitor, Policy.totalWork));
			} finally {
				input.close();
			}
		} catch (IOException e) {
			message = NLS.bind(Messages.resources_readMeta, project.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, project.getFullPath(), message, e);
		} finally {
			monitor.done();
		}
		if (Policy.DEBUG_RESTORE_TREE) {
			System.out.println("Restore Tree for " + project.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return true;
	}

	public IStatus save(int kind, Project project, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			isSaving = true;
			String message = Messages.resources_saving_0;
			monitor.beginTask(message, 7);
			message = Messages.resources_saveWarnings;
			MultiStatus warnings = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IStatus.WARNING, message, null);
			ISchedulingRule rule = project != null ? (IResource) project : workspace.getRoot();
			try {
				workspace.prepareOperation(rule, monitor);
				workspace.beginOperation(false);
				hookStartSave(kind, project);
				long start = System.currentTimeMillis();
				Map contexts = computeSaveContexts(getSaveParticipantPlugins(), kind, project);
				broadcastLifecycle(PREPARE_TO_SAVE, contexts, warnings, Policy.subMonitorFor(monitor, 1));
				try {
					broadcastLifecycle(SAVING, contexts, warnings, Policy.subMonitorFor(monitor, 1));
					switch (kind) {
						case ISaveContext.FULL_SAVE :
							// save the complete tree and remember all of the required saved states
							saveTree(contexts, Policy.subMonitorFor(monitor, 1));
							// reset the snapshot state.
							initSnap(null);
							//save master table right after saving tree to ensure correct tree number is saved
							cleanMasterTable();
							// save all of the markers and all sync info in the workspace
							persistMarkers = 0l;
							persistSyncInfo = 0l;
							visitAndSave(workspace.getRoot());
							monitor.worked(1);
							if (Policy.DEBUG_SAVE) {
								Policy.debug("Total Save Markers: " + persistMarkers + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
								Policy.debug("Total Save Sync Info: " + persistSyncInfo + "ms"); //$NON-NLS-1$	 //$NON-NLS-2$
							}
							// reset the snap shot files
							resetSnapshots(workspace.getRoot());
							//remove unused files
							removeUnusedSafeTables();
							removeUnusedTreeFiles();
							workspace.getFileSystemManager().getHistoryStore().clean(Policy.subMonitorFor(monitor, 1));
							// write out all metainfo (e.g., workspace/project descriptions) 
							saveMetaInfo(warnings, Policy.subMonitorFor(monitor, 1));
							break;
						case ISaveContext.SNAPSHOT :
							snapTree(workspace.getElementTree(), Policy.subMonitorFor(monitor, 1));
							// snapshot the markers and sync info for the workspace
							persistMarkers = 0l;
							persistSyncInfo = 0l;
							visitAndSnap(workspace.getRoot());
							monitor.worked(1);
							if (Policy.DEBUG_SAVE) {
								Policy.debug("Total Snap Markers: " + persistMarkers + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
								Policy.debug("Total Snap Sync Info: " + persistSyncInfo + "ms"); //$NON-NLS-1$	 //$NON-NLS-2$
							}
							collapseTrees();
							clearSavedDelta();
							// write out all metainfo (e.g., workspace/project descriptions) 
							saveMetaInfo(warnings, Policy.subMonitorFor(monitor, 1));
							break;
						case ISaveContext.PROJECT_SAVE :
							writeTree(project, IResource.DEPTH_INFINITE);
							monitor.worked(1);
							// save markers and sync info 
							visitAndSave(project);
							monitor.worked(1);
							// reset the snapshot file
							resetSnapshots(project);
							IStatus result = saveMetaInfo(project, null);
							if (!result.isOK())
								warnings.merge(result);
							monitor.worked(1);
							break;
					}
					// save contexts
					commit(contexts);
					if (kind == ISaveContext.FULL_SAVE)
						removeClearDeltaMarks();
					//this must be done after committing save contexts to update participant save numbers
					saveMasterTable();
					broadcastLifecycle(DONE_SAVING, contexts, warnings, Policy.subMonitorFor(monitor, 1));
					hookEndSave(kind, project, start);
					return warnings;
				} catch (CoreException e) {
					broadcastLifecycle(ROLLBACK, contexts, warnings, Policy.subMonitorFor(monitor, 1));
					// rollback ResourcesPlugin master table
					restoreMasterTable();
					throw e; // re-throw
				}
			} catch (OperationCanceledException e) {
				workspace.getWorkManager().operationCanceled();
				throw e;
			} finally {
				workspace.endOperation(rule, false, Policy.monitorFor(null));
			}
		} finally {
			isSaving = false;
			monitor.done();
		}
	}

	protected void saveMasterTable() throws CoreException {
		saveMasterTable(workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES));
	}

	protected void saveMasterTable(IPath location) throws CoreException {
		long start = System.currentTimeMillis();
		java.io.File target = location.toFile();
		try {
			SafeChunkyOutputStream output = new SafeChunkyOutputStream(target);
			try {
				masterTable.store(output, "master table"); //$NON-NLS-1$
				output.succeed();
			} finally {
				output.close();
			}
		} catch (IOException e) {
			String message = Messages.resources_exSaveMaster;
			throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
		}
		if (Policy.DEBUG_SAVE_MASTERTABLE)
			System.out.println("Save master table for " + location + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * Writes the metainfo (e.g. descriptions) of the given workspace and
	 * all projects to the local disk.
	 */
	protected void saveMetaInfo(MultiStatus problems, IProgressMonitor monitor) throws CoreException {
		if (Policy.DEBUG_SAVE_METAINFO)
			System.out.println("Save workspace metainfo: starting..."); //$NON-NLS-1$
		long start = System.currentTimeMillis();
		// save preferences (workspace description, path variables, etc)
		ResourcesPlugin.getPlugin().savePluginPreferences();
		// save projects' meta info
		IProject[] roots = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < roots.length; i++)
			if (roots[i].isAccessible()) {
				IStatus result = saveMetaInfo((Project) roots[i], null);
				if (!result.isOK())
					problems.merge(result);
			}
		if (Policy.DEBUG_SAVE_METAINFO)
			System.out.println("Save workspace metainfo: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Ensures that the project meta-info is saved.  The project meta-info
	 * is usually saved as soon as it changes, so this is just a sanity check
	 * to make sure there is something on disk before we shutdown.
	 * 
	 * @return Status object containing non-critical warnings, or an OK status.
	 */
	protected IStatus saveMetaInfo(Project project, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		//if there is nothing on disk, write the description
		if (!workspace.getFileSystemManager().hasSavedDescription(project)) {
			workspace.getFileSystemManager().writeSilently(project);
			String msg = NLS.bind(Messages.resources_missingProjectMetaRepaired, project.getName());
			return new ResourceStatus(IResourceStatus.MISSING_DESCRIPTION_REPAIRED, project.getFullPath(), msg);
		}
		if (Policy.DEBUG_SAVE_METAINFO)
			System.out.println("Save metainfo for " + project.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return Status.OK_STATUS;
	}

	/**
	 * Writes the current state of the entire workspace tree to disk.
	 * This is used during workspace save.  saveTree(Project)
	 * is used to save the state of an individual project.
	 * @exception CoreException if there is a problem writing the tree to disk.
	 */
	protected void saveTree(Map contexts, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), true);
		try {
			IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
			DataOutputStream output = new DataOutputStream(new SafeFileOutputStream(treeLocation.toOSString(), tempLocation.toOSString()));
			try {
				output.writeInt(ICoreConstants.WORKSPACE_TREE_VERSION_2);
				writeTree(computeStatesToSave(contexts, workspace.getElementTree()), output, monitor);
			} finally {
				output.close();
			}
		} catch (Exception e) {
			String msg = NLS.bind(Messages.resources_writeWorkspaceMeta, treeLocation);
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, Path.ROOT, msg, e);
		}
		if (Policy.DEBUG_SAVE_TREE)
			System.out.println("Save Workspace Tree: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Should only be used for read purposes.
	 */
	void setPluginsSavedState(HashMap savedStates) {
		this.savedStates = Collections.synchronizedMap(savedStates);
	}

	protected void setSaveNumber(String pluginId, int number) {
		masterTable.setProperty(SAVE_NUMBER_PREFIX + pluginId, new Integer(number).toString());
	}

	/* (non-Javadoc)
	 * Method declared on IStringPoolParticipant
	 */
	public void shareStrings(StringPool pool) {
		lastSnap.shareStrings(pool);
	}

	public void shutdown(final IProgressMonitor monitor) {
		// do a last snapshot if it was scheduled
		// we force it in the same thread because it would not  
		// help if the job runs after we close the workspace
		int state = snapshotJob.getState();
		if (state == Job.WAITING || state == Job.SLEEPING)
			// we cannot pass null to Job#run
			snapshotJob.run(Policy.monitorFor(monitor));
		// cancel the snapshot job
		snapshotJob.cancel();
	}

	/**
	 * Performs a snapshot if one is deemed necessary.
	 * Encapsulates rules for determining when a snapshot is needed.
	 * This should be called at the end of every top level operation.
	 */
	public void snapshotIfNeeded(boolean hasTreeChanges) {
		// never schedule a snapshot while save is occurring.
		if (isSaving)
			return;
		if (snapshotRequested || operationCount >= workspace.internalGetDescription().getOperationsPerSnapshot()) {
			if (snapshotJob.getState() == Job.NONE)
				snapshotJob.schedule();
			else
				snapshotJob.wakeUp();
		} else {
			if (hasTreeChanges) {
				operationCount++;
				if (snapshotJob.getState() == Job.NONE) {
					if (Policy.DEBUG_SAVE)
						System.out.println("Scheduling workspace snapshot"); //$NON-NLS-1$
					long interval = workspace.internalGetDescription().getSnapshotInterval();
					snapshotJob.schedule(Math.max(interval, MIN_SNAPSHOT_DELAY));
				}
			} else {
				//only increment the operation count if we've had a sufficient number of no-ops
				if (++noopCount > NO_OP_THRESHOLD) {
					operationCount++;
					noopCount = 0;
				}
			}
		}
	}

	/**
	 * Performs a snapshot of the workspace tree.
	 */
	protected void snapTree(ElementTree tree, IProgressMonitor monitor) throws CoreException {
		long start = System.currentTimeMillis();
		monitor = Policy.monitorFor(monitor);
		String message;
		try {
			monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
			//the tree must be immutable
			tree.immutable();
			// don't need to snapshot if there are no changes 
			if (tree == lastSnap)
				return;
			operationCount = 0;
			IPath snapPath = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
			ElementTreeWriter writer = new ElementTreeWriter(this);
			java.io.File localFile = snapPath.toFile();
			try {
				SafeChunkyOutputStream safeStream = new SafeChunkyOutputStream(localFile);
				DataOutputStream out = new DataOutputStream(safeStream);
				try {
					out.writeInt(ICoreConstants.WORKSPACE_TREE_VERSION_2);
					writeWorkspaceFields(out, monitor);
					writer.writeDelta(tree, lastSnap, Path.ROOT, ElementTreeWriter.D_INFINITE, out, ResourceComparator.getSaveComparator());
					safeStream.succeed();
				} finally {
					out.close();
				}
			} catch (IOException e) {
				message = NLS.bind(Messages.resources_writeWorkspaceMeta, localFile.getAbsolutePath());
				throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, Path.ROOT, message, e);
			}
			lastSnap = tree;
		} finally {
			monitor.done();
		}
		if (Policy.DEBUG_SAVE_TREE)
			System.out.println("Snapshot Workspace Tree: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Sorts the given array of trees so that the following rules are true:
	 * 	 - The first tree has no parent
	 * 	 - No tree has an ancestor with a greater index in the array.
	 * If there are no missing parents in the given trees array, this means
	 * that in the resulting array, the i'th tree's parent will be tree i-1.
	 * The input tree array may contain duplicate trees.
	 */
	protected ElementTree[] sortTrees(ElementTree[] trees) {
		/* the sorted list */
		int numTrees = trees.length;
		ElementTree[] sorted = new ElementTree[numTrees];

		/* first build a table of ElementTree -> List of Integers(indices in trees array) */
		Map table = new HashMap(numTrees * 2 + 1);
		for (int i = 0; i < trees.length; i++) {
			List indices = (List) table.get(trees[i]);
			if (indices == null) {
				indices = new ArrayList(10);
				table.put(trees[i], indices);
			}
			indices.add(new Integer(i));
		}

		/* find the oldest tree (a descendent of all other trees) */
		ElementTree oldest = trees[ElementTree.findOldest(trees)];

		/**
		 * Walk through the chain of trees from oldest to newest,
		 * adding them to the sorted list as we go.
		 */
		int i = numTrees - 1;
		while (i >= 0) {
			/* add all instances of the current oldest tree to the sorted list */
			List indices = (List) table.remove(oldest);
			for (Enumeration e = Collections.enumeration(indices); e.hasMoreElements();) {
				e.nextElement();
				sorted[i] = oldest;
				i--;
			}
			if (i >= 0) {
				/* find the next tree in the list */
				ElementTree parent = oldest.getParent();
				while (parent != null && table.get(parent) == null) {
					parent = parent.getParent();
				}
				if (parent == null) {
					IStatus status = new Status(IStatus.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, "null parent found while collapsing trees", null); //$NON-NLS-1$
					Policy.log(status);
					return null;
				}
				oldest = parent;
			}
		}
		return sorted;
	}

	public void startup(IProgressMonitor monitor) throws CoreException {
		restore(monitor);
		java.io.File table = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.PI_RESOURCES).toFile();
		if (!table.exists())
			table.getParentFile().mkdirs();
	}

	/**
	 * Update the expiration time for the given plug-in's tree.  If the tree was never
	 * loaded, use the current value in the master table. If the tree has been loaded,
	 * use the provided new timestamp.
	 * 
	 * The timestamp is used in the policy for cleaning up tree's of plug-ins that are 
	 * not often activated.
	 */
	protected void updateDeltaExpiration(String pluginId) {
		String key = DELTA_EXPIRATION_PREFIX + pluginId;
		if (!masterTable.containsKey(key))
			masterTable.setProperty(key, Long.toString(System.currentTimeMillis()));
	}

	/**
	 * Visit the given resource (to depth infinite) and write out extra information
	 * like markers and sync info. To be called during a full save and project save.
	 * 
	 * FIXME: This method is ugly. Fix it up and look at merging with #visitAndSnap
	 */
	public void visitAndSave(final IResource root) throws CoreException {
		// Ensure we have either a project or the workspace root
		Assert.isLegal(root.getType() == IResource.ROOT || root.getType() == IResource.PROJECT);
		// only write out info for accessible resources
		if (!root.isAccessible())
			return;

		// Setup variables
		final Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
		final MarkerManager markerManager = workspace.getMarkerManager();
		IPath markersLocation = workspace.getMetaArea().getMarkersLocationFor(root);
		IPath markersTempLocation = workspace.getMetaArea().getBackupLocationFor(markersLocation);
		IPath syncInfoLocation = workspace.getMetaArea().getSyncInfoLocationFor(root);
		IPath syncInfoTempLocation = workspace.getMetaArea().getBackupLocationFor(syncInfoLocation);
		final List writtenTypes = new ArrayList(5);
		final List writtenPartners = new ArrayList(synchronizer.registry.size());
		DataOutputStream o1 = null;
		DataOutputStream o2 = null;
		String message;

		// Create the output streams
		try {
			o1 = new DataOutputStream(new SafeFileOutputStream(markersLocation.toOSString(), markersTempLocation.toOSString()));
			// we don't store the sync info for the workspace root so don't create
			// an empty file
			if (root.getType() != IResource.ROOT)
				o2 = new DataOutputStream(new SafeFileOutputStream(syncInfoLocation.toOSString(), syncInfoTempLocation.toOSString()));
		} catch (IOException e) {
			if (o1 != null)
				try {
					o1.close();
				} catch (IOException e2) {
					// ignore
				}
			message = NLS.bind(Messages.resources_writeMeta, root.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), message, e);
		}

		final DataOutputStream markersOutput = o1;
		final DataOutputStream syncInfoOutput = o2;
		// The following 2 piece array will hold a running total of the times
		// taken to save markers and syncInfo respectively.  This will cut down
		// on the number of statements printed out as we would get 2 statements
		// for each resource otherwise.
		final long[] saveTimes = new long[2];

		// Create the visitor 
		IElementContentVisitor visitor = new IElementContentVisitor() {
			public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object elementContents) {
				ResourceInfo info = (ResourceInfo) elementContents;
				if (info != null) {
					try {
						// save the markers
						long start = System.currentTimeMillis();
						markerManager.save(info, requestor, markersOutput, writtenTypes);
						long markerSaveTime = System.currentTimeMillis() - start;
						saveTimes[0] += markerSaveTime;
						persistMarkers += markerSaveTime;
						// save the sync info - if we have the workspace root then the output stream will be null
						if (syncInfoOutput != null) {
							start = System.currentTimeMillis();
							synchronizer.saveSyncInfo(info, requestor, syncInfoOutput, writtenPartners);
							long syncInfoSaveTime = System.currentTimeMillis() - start;
							saveTimes[1] += syncInfoSaveTime;
							persistSyncInfo += syncInfoSaveTime;
						}
					} catch (IOException e) {
						throw new WrappedRuntimeException(e);
					}
				}
				// don't continue if the current resource is the workspace root, only continue for projects
				return root.getType() != IResource.ROOT;
			}
		};

		// Call the visitor
		try {
			try {
				new ElementTreeIterator(workspace.getElementTree(), root.getFullPath()).iterate(visitor);
			} catch (WrappedRuntimeException e) {
				throw (IOException) e.getTargetException();
			}
			if (Policy.DEBUG_SAVE_MARKERS)
				System.out.println("Save Markers for " + root.getFullPath() + ": " + saveTimes[0] + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (Policy.DEBUG_SAVE_SYNCINFO)
				System.out.println("Save SyncInfo for " + root.getFullPath() + ": " + saveTimes[1] + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			removeGarbage(markersOutput, markersLocation, markersTempLocation);
			// if we have the workspace root the output stream will be null and we
			// don't have to perform cleanup code
			if (syncInfoOutput != null)
				removeGarbage(syncInfoOutput, syncInfoLocation, syncInfoTempLocation);
		} catch (IOException e) {
			message = NLS.bind(Messages.resources_writeMeta, root.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), message, e);
		} finally {
			FileUtil.safeClose(markersOutput);
			FileUtil.safeClose(syncInfoOutput);
		}

		// recurse over the projects in the workspace if we were given the workspace root
		if (root.getType() == IResource.PROJECT)
			return;
		IProject[] projects = ((IWorkspaceRoot) root).getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++)
			visitAndSave(projects[i]);
	}

	/**
	 * Visit the given resource (to depth infinite) and write out extra information
	 * like markers and sync info. To be called during a snapshot
	 * 
	 * FIXME: This method is ugly. Fix it up and look at merging with #visitAndSnap
	 */
	public void visitAndSnap(final IResource root) throws CoreException {
		// Ensure we have either a project or the workspace root
		Assert.isLegal(root.getType() == IResource.ROOT || root.getType() == IResource.PROJECT);
		// only write out info for accessible resources
		if (!root.isAccessible())
			return;

		// Setup variables
		final Synchronizer synchronizer = (Synchronizer) workspace.getSynchronizer();
		final MarkerManager markerManager = workspace.getMarkerManager();
		IPath markersLocation = workspace.getMetaArea().getMarkersSnapshotLocationFor(root);
		IPath syncInfoLocation = workspace.getMetaArea().getSyncInfoSnapshotLocationFor(root);
		SafeChunkyOutputStream safeMarkerStream = null;
		SafeChunkyOutputStream safeSyncInfoStream = null;
		DataOutputStream o1 = null;
		DataOutputStream o2 = null;
		String message;

		// Create the output streams
		try {
			safeMarkerStream = new SafeChunkyOutputStream(markersLocation.toFile());
			o1 = new DataOutputStream(safeMarkerStream);
			// we don't store the sync info for the workspace root so don't create
			// an empty file
			if (root.getType() != IResource.ROOT) {
				safeSyncInfoStream = new SafeChunkyOutputStream(syncInfoLocation.toFile());
				o2 = new DataOutputStream(safeSyncInfoStream);
			}
		} catch (IOException e) {
			FileUtil.safeClose(o1);
			message = NLS.bind(Messages.resources_writeMeta, root.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), message, e);
		}

		final DataOutputStream markersOutput = o1;
		final DataOutputStream syncInfoOutput = o2;
		int markerFileSize = markersOutput.size();
		int syncInfoFileSize = safeSyncInfoStream == null ? -1 : syncInfoOutput.size();
		// The following 2 piece array will hold a running total of the times
		// taken to save markers and syncInfo respectively.  This will cut down
		// on the number of statements printed out as we would get 2 statements
		// for each resource otherwise.
		final long[] snapTimes = new long[2];

		IElementContentVisitor visitor = new IElementContentVisitor() {
			public boolean visitElement(ElementTree tree, IPathRequestor requestor, Object elementContents) {
				ResourceInfo info = (ResourceInfo) elementContents;
				if (info != null) {
					try {
						// save the markers
						long start = System.currentTimeMillis();
						markerManager.snap(info, requestor, markersOutput);
						long markerSnapTime = System.currentTimeMillis() - start;
						snapTimes[0] += markerSnapTime;
						persistMarkers += markerSnapTime;
						// save the sync info - if we have the workspace root then the output stream will be null
						if (syncInfoOutput != null) {
							start = System.currentTimeMillis();
							synchronizer.snapSyncInfo(info, requestor, syncInfoOutput);
							long syncInfoSnapTime = System.currentTimeMillis() - start;
							snapTimes[1] += syncInfoSnapTime;
							persistSyncInfo += syncInfoSnapTime;
						}
					} catch (IOException e) {
						throw new WrappedRuntimeException(e);
					}
				}
				// don't continue if the current resource is the workspace root, only continue for projects
				return root.getType() != IResource.ROOT;
			}
		};

		try {
			// Call the visitor
			try {
				new ElementTreeIterator(workspace.getElementTree(), root.getFullPath()).iterate(visitor);
			} catch (WrappedRuntimeException e) {
				throw (IOException) e.getTargetException();
			}
			if (Policy.DEBUG_SAVE_MARKERS)
				System.out.println("Snap Markers for " + root.getFullPath() + ": " + snapTimes[0] + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (Policy.DEBUG_SAVE_SYNCINFO)
				System.out.println("Snap SyncInfo for " + root.getFullPath() + ": " + snapTimes[1] + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			if (markerFileSize != markersOutput.size())
				safeMarkerStream.succeed();
			if (safeSyncInfoStream != null && syncInfoFileSize != syncInfoOutput.size())
				safeSyncInfoStream.succeed();
		} catch (IOException e) {
			message = NLS.bind(Messages.resources_writeMeta, root.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), message, e);
		} finally {
			FileUtil.safeClose(markersOutput);
			FileUtil.safeClose(syncInfoOutput);
		}

		// recurse over the projects in the workspace if we were given the workspace root
		if (root.getType() == IResource.PROJECT)
			return;
		IProject[] projects = ((IWorkspaceRoot) root).getProjects(IContainer.INCLUDE_HIDDEN);
		for (int i = 0; i < projects.length; i++)
			visitAndSnap(projects[i]);
	}

	/**
	 * Writes out persistent information about all builders for which a last built
	 * tree is available. File format is:
	 * int - number of builders
	 * for each builder:
	 *    UTF - project name
	 *    UTF - fully qualified builder extension name
	 *    int - number of interesting projects for builder
	 *    For each interesting project:
	 *       UTF - interesting project name
	 */
	protected void writeBuilderPersistentInfo(DataOutputStream output, List builders, List trees, IProgressMonitor monitor) throws IOException {
		monitor = Policy.monitorFor(monitor);
		try {
			// write the number of builders we are saving
			int numBuilders = builders.size();
			output.writeInt(numBuilders);
			for (int i = 0; i < numBuilders; i++) {
				BuilderPersistentInfo info = (BuilderPersistentInfo) builders.get(i);
				output.writeUTF(info.getProjectName());
				output.writeUTF(info.getBuilderName());
				// write interesting projects
				IProject[] interestingProjects = info.getInterestingProjects();
				output.writeInt(interestingProjects.length);
				for (int j = 0; j < interestingProjects.length; j++)
					output.writeUTF(interestingProjects[j].getName());
				ElementTree last = info.getLastBuiltTree();
				//it is not unusual for a builder to have no last built tree (for example after a clean)
				if (last == null)
					last = workspace.getElementTree();
				trees.add(last);
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * @see IElementInfoFlattener#writeElement(IPath, Object, DataOutput)
	 */
	public void writeElement(IPath path, Object element, DataOutput output) throws IOException {
		Assert.isNotNull(path);
		Assert.isNotNull(element);
		Assert.isNotNull(output);
		ResourceInfo info = (ResourceInfo) element;
		output.writeInt(info.getFlags());
		info.writeTo(output);
	}

	protected void writeTree(Map statesToSave, DataOutputStream output, IProgressMonitor monitor) throws IOException, CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
			boolean wasImmutable = false;
			try {
				// Create an array of trees to save. Ensure that the current one is in the list
				ElementTree current = workspace.getElementTree();
				wasImmutable = current.isImmutable();
				current.immutable();
				ArrayList trees = new ArrayList(statesToSave.size() * 2); // pick a number
				monitor.worked(Policy.totalWork * 10 / 100);

				// write out the workspace fields
				writeWorkspaceFields(output, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

				// save plugin info
				output.writeInt(statesToSave.size()); // write the number of plugins we are saving
				for (Iterator i = statesToSave.entrySet().iterator(); i.hasNext();) {
					Map.Entry entry = (Map.Entry) i.next();
					String pluginId = (String) entry.getKey();
					output.writeUTF(pluginId);
					trees.add(entry.getValue()); // tree
					updateDeltaExpiration(pluginId);
				}
				monitor.worked(Policy.totalWork * 10 / 100);

				// add builders' trees
				IProject[] projects = workspace.getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
				List builders = new ArrayList(projects.length * 2);
				for (int i = 0; i < projects.length; i++) {
					IProject project = projects[i];
					if (project.isOpen()) {
						ArrayList infos = workspace.getBuildManager().createBuildersPersistentInfo(project);
						if (infos != null)
							builders.addAll(infos);
					}
				}
				writeBuilderPersistentInfo(output, builders, trees, Policy.subMonitorFor(monitor, Policy.totalWork * 10 / 100));

				// add the current tree in the list as the last element
				trees.add(current);

				/* save the forest! */
				ElementTreeWriter writer = new ElementTreeWriter(this);
				ElementTree[] treesToSave = (ElementTree[]) trees.toArray(new ElementTree[trees.size()]);
				writer.writeDeltaChain(treesToSave, Path.ROOT, ElementTreeWriter.D_INFINITE, output, ResourceComparator.getSaveComparator());
				monitor.worked(Policy.totalWork * 50 / 100);
			} finally {
				if (!wasImmutable)
					workspace.newWorkingTree();
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Attempts to save all the trees for this project (the current tree
	 * plus a tree for each builder with a previously built state).  Throws
	 * an IOException if anything went wrong during save.  Attempts to close
	 * the provided stream at all costs.
	 */
	protected void writeTree(Project project, DataOutputStream output, IProgressMonitor monitor) throws IOException, CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", 10); //$NON-NLS-1$
			boolean wasImmutable = false;
			try {
				/**
				 * Obtain a list of BuilderPersistentInfo.
				 * This list includes builders that have never been instantiated
				 * but already had a last built state.
				 */
				ArrayList builderInfos = workspace.getBuildManager().createBuildersPersistentInfo(project);
				if (builderInfos == null)
					builderInfos = new ArrayList(5);
				List trees = new ArrayList(builderInfos.size() + 1);
				monitor.worked(1);

				/* Make sure the most recent tree is in the array */
				ElementTree current = workspace.getElementTree();
				wasImmutable = current.isImmutable();
				current.immutable();

				/* add the tree for each builder to the array */
				writeBuilderPersistentInfo(output, builderInfos, trees, Policy.subMonitorFor(monitor, 1));
				trees.add(current);

				/* save the forest! */
				ElementTreeWriter writer = new ElementTreeWriter(this);
				ElementTree[] treesToSave = (ElementTree[]) trees.toArray(new ElementTree[trees.size()]);
				writer.writeDeltaChain(treesToSave, project.getFullPath(), ElementTreeWriter.D_INFINITE, output, ResourceComparator.getSaveComparator());
				monitor.worked(8);
			} finally {
				if (output != null)
					output.close();
				if (!wasImmutable)
					workspace.newWorkingTree();
			}
		} finally {
			monitor.done();
		}
	}

	protected void writeTree(Project project, int depth) throws CoreException {
		long start = System.currentTimeMillis();
		IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, true);
		IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
		try {
			SafeFileOutputStream safe = new SafeFileOutputStream(treeLocation.toOSString(), tempLocation.toOSString());
			try {
				DataOutputStream output = new DataOutputStream(safe);
				output.writeInt(ICoreConstants.WORKSPACE_TREE_VERSION_2);
				writeTree(project, output, null);
			} finally {
				safe.close();
			}
		} catch (IOException e) {
			String msg = NLS.bind(Messages.resources_writeMeta, project.getFullPath());
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, treeLocation, msg, e);
		}
		if (Policy.DEBUG_SAVE_TREE)
			System.out.println("Save tree for " + project.getFullPath() + ": " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	protected void writeWorkspaceFields(DataOutputStream output, IProgressMonitor monitor) throws IOException {
		monitor = Policy.monitorFor(monitor);
		try {
			// save the next node id 
			output.writeLong(workspace.nextNodeId);
			// save the modification stamp (no longer used)
			output.writeLong(0L);
			// save the marker id counter
			output.writeLong(workspace.nextMarkerId);
			// save the registered sync partners in the synchronizer
			((Synchronizer) workspace.getSynchronizer()).savePartners(output);
		} finally {
			monitor.done();
		}
	}
}
