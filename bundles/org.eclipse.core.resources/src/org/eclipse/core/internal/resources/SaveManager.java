package org.eclipse.core.internal.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */

import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.*;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import java.io.*;
import java.util.*;

public class SaveManager implements IElementInfoFlattener, ICoreConstants, IManager {
	protected Workspace workspace;
	protected Properties masterTable;
	protected ElementTree lastSnap;
	protected int operationCount = 0;
	protected boolean snapshotRequested;

	/** plugins that participate on a workspace save */
	protected HashMap saveParticipants;

	/** in-memory representation of plugins saved state */
	protected HashMap savedStates;

	class SavedState implements ISavedState {
		ElementTree oldTree;
		ElementTree newTree;
		SafeFileTable fileTable;
		String pluginId;

		SavedState(String pluginId, ElementTree oldTree, ElementTree newTree) throws CoreException {
			this.pluginId = pluginId;
			this.newTree = newTree;
			this.oldTree = oldTree;
			this.fileTable = restoreFileTable();
		}
		void forgetTrees() {
			newTree = null;
			oldTree = null;
		}
		public int getSaveNumber() {
			return SaveManager.this.getSaveNumber(pluginId);
		}
		protected SafeFileTable getFileTable() {
			return fileTable;
		}
		protected SafeFileTable restoreFileTable() throws CoreException {
			if (fileTable == null)
				fileTable = new SafeFileTable(pluginId);
			return fileTable;
		}
		public IPath lookup(IPath file) {
			return getFileTable().lookup(file);
		}
		public IPath[] getFiles() {
			return getFileTable().getFiles();
		}
		public void processResourceChangeEvents(IResourceChangeListener listener) {
			try {
				try {
					workspace.prepareOperation();
					if (oldTree == null || newTree == null)
						return;
					workspace.beginOperation(true);
					ResourceDelta delta = ResourceDeltaFactory.computeDelta(workspace, oldTree, newTree, Path.ROOT, false);
					forgetTrees(); // free trees to prevent memory leak
					workspace.getNotificationManager().broadcastChanges(listener, IResourceChangeEvent.POST_AUTO_BUILD, delta, false);
				} finally {
					workspace.endOperation(false, null);
				}
			} catch (CoreException e) {
				// this is unlikelly to happen, so, just log it
				ResourceStatus status = new ResourceStatus(IResourceStatus.WARNING, null, e.getMessage(), e);
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
		}
	}

	/** constants */
	protected static final int PREPARE_TO_SAVE = 1;
	protected static final int SAVING = 2;
	protected static final int DONE_SAVING = 3;
	protected static final int ROLLBACK = 4;
	protected static final String SAVE_NUMBER_PREFIX = "saveNumber_";
	protected static final String CLEAR_DELTA_PREFIX = "clearDelta_";
	protected static final String DELTA_EXPIRATION_PREFIX = "deltaExpiration_";
public SaveManager(IWorkspace workspace) {
	this.workspace = (Workspace) workspace;
	snapshotRequested = false;
	saveParticipants = new HashMap(10);
}
public ISavedState addParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException {
	synchronized (saveParticipants) {
		// If the plugin was already registered as a save participant we return null
		if (saveParticipants.put(plugin, participant) != null)
			return null;
	}
	String id = plugin.getDescriptor().getUniqueIdentifier();
	SavedState state = (SavedState) savedStates.get(id);
	if (state != null) {
		if (isDeltaCleared(id)) {
			// this plugin was marked not to receive deltas
			state.forgetTrees();
			removeClearDeltaMarks(id);
		} else {
			try {
				// thread safety: (we need to guarantee that the tree is imutable when computing deltas)
				// so, the tree inside the saved state needs to be immutable
				workspace.prepareOperation();
				workspace.beginOperation(true);
				state.newTree = workspace.getElementTree();
			} finally {
				workspace.endOperation(false, null);
			}
			return state;
		}
	}
	// if the plug-in has a previous save number, we return a state, otherwise we return null
	if (getSaveNumber(id) > 0)
		return new SavedState(id, null, null);
	return null;
}
protected void broadcastLifecycle(final int lifecycle, Map contexts, final MultiStatus warnings, IProgressMonitor monitor) {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", contexts.size());
		for (final Iterator it = contexts.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			Plugin plugin = (Plugin) entry.getKey();
			final ISaveParticipant participant = (ISaveParticipant) saveParticipants.get(plugin);
			final SaveContext context = (SaveContext) entry.getValue();
			/* Be extra careful when calling lifecycle method on arbitary plugin */
			ISafeRunnable code = new ISafeRunnable() {
				public void run() throws Exception {
					executeLifecycle(lifecycle, participant, context);
				}
				public void handleException(Throwable e) {
					String message = "Problems occurred during save";
					IStatus status = new Status(Status.WARNING, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e);
					warnings.add(status);

					/* Remove entry for defective plug-in from this save operation */
					it.remove();
				}
			};
			Platform.run(code);
			monitor.worked(1);
		}
	} finally {
		monitor.done();
	}
}
protected void cleanMasterTable() {
	String pluginId = ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
	IPath location = workspace.getMetaArea().getSafeTableLocationFor(pluginId);
	IPath backup = workspace.getMetaArea().getBackupLocationFor(location);
	try {
		saveMasterTable(backup);
	} catch (CoreException e) {
		ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		backup.toFile().delete();
		return;
	}
	if (location.toFile().exists() && !location.toFile().delete())
		return;
	try {
		saveMasterTable(location);
	} catch (CoreException e) {
		ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
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
	for (Iterator i = saveParticipants.keySet().iterator(); i.hasNext();) {
		String pluginId = ((Plugin) i.next()).getDescriptor().getUniqueIdentifier();
		masterTable.setProperty(CLEAR_DELTA_PREFIX + pluginId, "true");
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
	for (Iterator i = savedStates.values().iterator(); i.hasNext();) {
		SavedState state = (SavedState) i.next();
		if (state.oldTree != null) {
			trees.add(state.oldTree);
		}
	}

	//trees for builders
	IProject[] projects = workspace.getRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		IProject project = projects[i];
		if (project.isOpen()) {
			Hashtable namesToBuilderTrees = workspace.getBuildManager().createBuilderMap(project);
			if (namesToBuilderTrees != null) {
				Iterator builderTrees = namesToBuilderTrees.values().iterator();
				while (builderTrees.hasNext()) {
					Object tree = builderTrees.next();
					trees.add(tree);
				}
			}
		}
	}

	//no need to collapse if there's no trees at this point
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

	for (int i = 1; i < sorted.length; i++) {
		ElementTree oldTree = sorted[i].collapseTo(sorted[i - 1]);
	}
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
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
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
	HashMap result = new HashMap(savedStates.size());
	for (Iterator i = savedStates.values().iterator(); i.hasNext();) {
		SavedState state = (SavedState) i.next();
		if (state.oldTree != null)
			result.put(state.pluginId, state.oldTree);
	}
	for (Iterator i = contexts.values().iterator(); i.hasNext();) {
		SaveContext context = (SaveContext) i.next();
		if (!context.isDeltaNeeded())
			continue;
		String pluginId = context.getPlugin().getDescriptor().getUniqueIdentifier();
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
			participant.saving(context);
			break;
		case DONE_SAVING :
			participant.doneSaving(context);
			break;
		case ROLLBACK :
			participant.rollback(context);
			break;
		default :
			Assert.isTrue(false, "Invalid save lifecycle code");
	}
}
public void forgetSavedTree(String pluginId) {
	if (pluginId == null) {
		for (Iterator i = savedStates.values().iterator(); i.hasNext();)
			 ((SavedState) i.next()).forgetTrees();
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
 * Initializes the snapshot mechanism for this workspace.
 */
protected void initSnap(IProgressMonitor monitor) throws CoreException {
	lastSnap = workspace.getElementTree();
	operationCount = 0;
	// delete the snapshot file, if any
	IPath snapPath = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
	java.io.File file = snapPath.toFile();
	if (file.exists())
		file.delete();
	if (file.exists()) {
		String message = "Could not initialize snapshot file";
		throw new ResourceException(IResourceStatus.FAILED_DELETE_METADATA, null, message, null);
	}
}
/**
 * Reads the markers which were originally saved
 * for the tree rooted by the given resource.
 */
protected void internalRestoreMarkers(IResource resource, boolean generateDeltas, IProgressMonitor monitor) throws CoreException {
	IPath sourceLocation = workspace.getMetaArea().getMarkersLocationFor(resource);
	IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(sourceLocation);
	if (!sourceLocation.toFile().exists() && !tempLocation.toFile().exists())
		return; // Ignore if no markers saved.
	try {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new SafeFileInputStream(sourceLocation.toOSString(), tempLocation.toOSString()));
			workspace.getMarkerManager().read(input, generateDeltas);
		} finally {
			if (input != null)
				input.close();
		}
	} catch (IOException e) {
		String msg = Policy.bind("readMeta", new String[] { sourceLocation.toString()});
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, sourceLocation, msg, e);
	}
}
/**
 * Reads the sync info which was originally saved
 * for the tree rooted by the given resource.
 */
protected void internalRestoreSyncInfo(IResource resource, IProgressMonitor monitor) throws CoreException {
	IPath sourceLocation = workspace.getMetaArea().getSyncInfoLocationFor(resource);
	IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(sourceLocation);
	if (!sourceLocation.toFile().exists() && !tempLocation.toFile().exists())
		return; // Ignore if no sync info saved.
	try {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new SafeFileInputStream(sourceLocation.toOSString(), tempLocation.toOSString()));
			((Synchronizer) workspace.getSynchronizer()).readSyncInfo(input);
		} finally {
			if (input != null)
				input.close();
		}
	} catch (IOException e) {
		String msg = Policy.bind("readMeta", new String[] { sourceLocation.toString()});
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, sourceLocation, msg, e);
	}
}
protected boolean isDeltaCleared(String pluginId) {
	String clearDelta = masterTable.getProperty(CLEAR_DELTA_PREFIX + pluginId);
	return clearDelta != null && clearDelta.equals("true");
}
protected boolean isOldPluginTree(String pluginId) {
	// first, check if this plug-ins was marked not to receive a delta
	if (isDeltaCleared(pluginId))
		return false;
	long deltaAge = System.currentTimeMillis() - getDeltaExpiration(pluginId);
	return deltaAge > workspace.internalGetDescription().getDeltaExpiration();
}
/**
 * @see IElementInfoFlattener#readElement
 */
public Object readElement(IPath path, DataInput input) throws IOException {
	Assert.isNotNull(path);
	Assert.isNotNull(input);
	// read the flags and pull out the type.  
	int flags = input.readInt();
	int type = (flags & M_TYPE) >> M_TYPE_START;
	ResourceInfo info = (ResourceInfo) workspace.newElement(type);
	info.readFrom(flags, input);
	return info;
}
protected int readVersionNumber(DataInputStream input) throws IOException {
	return input.readInt();
}
protected void readWorkspaceFields(DataInputStream input, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		// read the node id 
		workspace.nextNodeId = input.readLong();
		// read the modification stamp
		workspace.nextModificationStamp = input.readLong();
		// read the next marker id
		workspace.nextMarkerId = input.readLong();
		// read the synchronizer's registered sync partners
		 ((Synchronizer) workspace.getSynchronizer()).readPartners(input);
	} finally {
		monitor.done();
	}
}
protected void readWorkspaceTree(DataInputStream input, IProgressMonitor monitor) throws CoreException, IOException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", Policy.totalWork);

		// read in the fields for the workspace
		readWorkspaceFields(input, Policy.subMonitorFor(monitor, Policy.opWork * 20 / 100));

		/* read the plugins saved states */
		int stateCount = input.readInt();
		savedStates = new HashMap(stateCount);
		List pluginsToBeLinked = new ArrayList(stateCount);
		for (int i = 0; i < stateCount; i++) {
			String pluginId = input.readUTF();
			SavedState state = new SavedState(pluginId, null, null);
			savedStates.put(pluginId, state);
			pluginsToBeLinked.add(state);
		}
		monitor.worked(Policy.totalWork * 10 / 100);

		/* read builder infos */
		int builderCount = input.readInt();
		List buildersToBeLinked = new ArrayList(builderCount);
		for (int i = 0; i < builderCount; i++) {
			String[] params = new String[2];
			params[0] = input.readUTF(); // project name
			params[1] = input.readUTF(); // builder name
			buildersToBeLinked.add(params);
		}
		monitor.worked(Policy.totalWork * 10 / 100);

		/* read the trees and attach the restored delta chain to the workspace */
		ElementTreeReader treeReader = new ElementTreeReader(this);
		ElementTree[] trees = treeReader.readDeltaChain(input);
		monitor.worked(Policy.totalWork * 30 / 100);
		workspace.linkTrees(Path.ROOT, trees);
		monitor.worked(Policy.totalWork * 10 / 100);

		/* link up the plugins saved states to the trees */
		for (int i = 0; i < pluginsToBeLinked.size(); i++) {
			SavedState state = (SavedState) pluginsToBeLinked.get(i);
			// If the tree is too old (depends on the policy), the plug-in should not
			// get it back as a delta. It is expensive to maintain this information too long.
			if (!isOldPluginTree(state.pluginId))
				state.oldTree = trees[i];
		}
		monitor.worked(Policy.totalWork * 10 / 100);

		/* link up the builders to the trees */
		String project = null;
		Hashtable builders = null;
		for (int i = 0; i < buildersToBeLinked.size(); i++) {
			String[] params = (String[]) buildersToBeLinked.get(i);
			if (!params[0].equals(project)) {
				if (builders != null)
					workspace.getBuildManager().setBuilderMap(workspace.getRoot().getProject(project), builders);
				builders = new Hashtable(5);
				project = params[0];
			}
			builders.put(params[1], trees[pluginsToBeLinked.size() + i]);
		}
		if (builders != null)
			workspace.getBuildManager().setBuilderMap(workspace.getRoot().getProject(project), builders);
		monitor.worked(Policy.totalWork * 10 / 100);
	} finally {
		monitor.done();
	}
}
/**
 * Remove marks from current save participants. This marks prevent them to receive their
 * deltas when they register themselves as save participants.
 */
protected void removeClearDeltaMarks() {
	for (Iterator i = saveParticipants.keySet().iterator(); i.hasNext();) {
		String pluginId = ((Plugin) i.next()).getDescriptor().getUniqueIdentifier();
		removeClearDeltaMarks(pluginId);
	}
}
protected void removeClearDeltaMarks(String pluginId) {
	masterTable.setProperty(CLEAR_DELTA_PREFIX + pluginId, "false");
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
	synchronized (saveParticipants) {
		saveParticipants.remove(plugin);
	}
}
protected void removeUnusedSafeTables() {
	List valuables = new ArrayList(10);
	IPath location = workspace.getMetaArea().getSafeTableLocationFor(ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier());
	valuables.add(location.lastSegment()); // add master table
	for (Enumeration enum = masterTable.keys(); enum.hasMoreElements();) {
		String key = (String) enum.nextElement();
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
	IProject[] projects = workspace.getRoot().getProjects();
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
protected DataInputStream resetStream(DataInputStream input, IPath location, IPath tempLocation) throws IOException {
	input.close();
	return new DataInputStream(new SafeFileInputStream(location.toOSString(), tempLocation.toOSString()));
}
/**
 * Restores the contents of this project.  Throw
 * an exception if the project could not be restored.
 */
protected void restore(Project project, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 40);
		if (project.isOpen()) {
			restoreTree(project, Policy.subMonitorFor(monitor, 10));
		} else {
			monitor.worked(10);
		}
		restoreMarkers(project, true, Policy.subMonitorFor(monitor, 10));
		restoreSyncInfo(project, Policy.subMonitorFor(monitor, 10));
		// restore meta info last because it might delete a project if its description is not found
		restoreMetaInfo(project, Policy.subMonitorFor(monitor, 10));
	} finally {
		monitor.done();
	}
}
/**
 * Restores the state of this workspace by opening the projects
 * which were open when it was last saved.
 */
protected void restore(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 50);
		// need to open the tree to restore, but since we're not 
		// inside an operation, be sure to close it afterwards
		workspace.newWorkingTree();
		try {
			restoreMasterTable();
			// restore the saved tree and overlay the snapshots if any
			restoreTree(workspace, Policy.subMonitorFor(monitor, 10));
			restoreSnapshots(Policy.subMonitorFor(monitor, 10));
			restoreMarkers(workspace.getRoot(), false, Policy.subMonitorFor(monitor, 10));
			restoreSyncInfo(workspace.getRoot(), Policy.subMonitorFor(monitor, 10));
			// restore meta info last because it might delete a project if its description is not found
			restoreMetaInfo(workspace, Policy.subMonitorFor(monitor, 10));
			IProject[] roots = workspace.getRoot().getProjects();
			for (int i = 0; i < roots.length; i++)
				 ((Project) roots[i]).startup();
		} finally {
			workspace.getElementTree().immutable();
		}
	} finally {
		monitor.done();
	}
}
/**
 * Reads the markers which were originally saved
 * for the tree rooted by the given resource.
 */
protected void restoreMarkers(IResource resource, boolean generateDeltas, IProgressMonitor monitor) throws CoreException {
	Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
	// when restoring a project, only load markers if it is open
	if (resource.getType() == IResource.PROJECT) {
		if (resource.isAccessible())
			internalRestoreMarkers(resource, generateDeltas, monitor);
		return;
	}
	internalRestoreMarkers(resource, generateDeltas, monitor);
	IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
	for (int i = 0; i < projects.length; i++)
		if (projects[i].isAccessible())
			internalRestoreMarkers(projects[i], generateDeltas, monitor);
}
protected void restoreMasterTable() throws CoreException {
	masterTable = new Properties();
	String pluginId = ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
	IPath location = workspace.getMetaArea().getSafeTableLocationFor(pluginId);
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
		String message = "Could not read master table.";
		throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
	}
}
/**
 * Restores the contents of this project.  Throw
 * an exception if the project could not be restored.
 */
protected void restoreMetaInfo(Project project, IProgressMonitor monitor) throws CoreException {
	// load the description even if this project is not open.
	ProjectDescription description = workspace.getMetaArea().read(project);
	if (description == null && project.internalGetDescription() == null) {
		// Somebody has probably deleted the .prj file from disk 
		// delete the project from the workspace but leave its contents
		project.basicDelete(new MultiStatus(ResourcesPlugin.PI_RESOURCES, 0, "ignored", null));
		return;
	}
	if (description != null) {
		// FIXME: decide what it means to validate
		// validate(desc);
		project.internalSetDescription(description, false);
	}
}
/**
 * Restores the state of this workspace by opening the projects
 * which were open when it was last saved.
 */
protected void restoreMetaInfo(Workspace workspace, IProgressMonitor monitor) throws CoreException {
	// FIXME: read the meta info for the workspace?
	IProject[] roots = workspace.getRoot().getProjects();
	for (int i = 0; i < roots.length; i++)
		restoreMetaInfo((Project) roots[i], monitor);
}
/**
 * Restores the workspace tree from snapshot files in the event
 * of a crash.  The workspace tree must be open when this method
 * is called, and will be open at the end of this method.  In the
 * event of a crash recovery, the snapshot file is not deleted until
 * the next successful save.
 */
protected void restoreSnapshots(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", Policy.totalWork);
		IPath snapLocation = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
		java.io.File localFile = snapLocation.toFile();

		// If the snapshot file doesn't exist, there was no crash.  
		// Just initialize the snapshot file and return
		if (!localFile.exists()) {
			initSnap(Policy.subMonitorFor(monitor, Policy.totalWork / 2));
			return;
		}
		// If we have a snapshot file, the workspace was shutdown without being saved
		// or crashed.
		workspace.setCrashed(true);
		try {
			/* Read each of the snapshots and lay them on top of the current tree.*/
			ElementTree complete = workspace.getElementTree();
			complete.immutable();
			SafeChunkyInputStream safeStream = new SafeChunkyInputStream(localFile);
			DataInputStream in = new DataInputStream(safeStream);
			try {
				ElementTreeReader reader = new ElementTreeReader(this);
				while (in.available() > 0) {
					readWorkspaceFields(in, Policy.subMonitorFor(monitor, Policy.totalWork / 2));
					complete = reader.readDelta(complete, in);
				}
			} finally {
				in.close();
				//reader returned an immutable tree, but since we're inside
				//an operation, we must return an open tree
				complete = complete.newEmptyDelta();
				workspace.tree = complete;
				lastSnap = complete;
			}
		} catch (Exception e) {
			// only log the exception, we should not fail restoring the snapshot
			String message = "Could not read snapshot file";
			ResourcesPlugin.getPlugin().getLog().log(new ResourceStatus(IResourceStatus.FAILED_READ_METADATA, null, message, e));
		}
	} finally {
		monitor.done();
	}
}
/**
 * Reads the sync info which was originally saved
 * for the tree rooted by the given resource.
 */
protected void restoreSyncInfo(IResource resource, IProgressMonitor monitor) throws CoreException {
	Assert.isLegal(resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT);
	// when restoring a project, only load sync info if it is open
	if (resource.getType() == IResource.PROJECT) {
		if (resource.isAccessible())
			internalRestoreSyncInfo(resource, monitor);
		return;
	}
	internalRestoreSyncInfo(resource, monitor);
	IProject[] projects = ((IWorkspaceRoot) resource).getProjects();
	for (int i = 0; i < projects.length; i++)
		if (projects[i].isAccessible())
			internalRestoreSyncInfo(projects[i], monitor);
}
/**
 * Restores the trees for the builders of this project from the local disk.
 * Does nothing if the tree file does not exist (this means the
 * project has never been saved).  This method is
 * used when restoring a saved/closed project.  restoreTree(Workspace) is
 * used when restoring a complete workspace after workspace save/shutdown.
 * @exception if the project could not be restored.
 */
protected void restoreTree(Project project, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", 10);
		IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, false);
		IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
		if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists())
			return;
		try {
			DataInputStream input = null;
			try {
				input = new DataInputStream(new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString()));
				// FIXME: implement version reader code. 
				// check the version number to ensure we can read the file.
				// if the file doesn't have a version number yet, then reset it
				// and continue.
				int version = readVersionNumber(input);
				if (version != ICoreConstants.WORKSPACE_VERSION)
					input = resetStream(input, treeLocation, tempLocation);

				/* read the number of builders */
				int numBuilders = input.readInt();

				/* read in the list of builder names */
				String[] builderNames = new String[numBuilders];
				for (int i = 0; i < numBuilders; i++) {
					String builderName = input.readUTF();
					builderNames[i] = builderName;
				}
				monitor.worked(1);

				/* read the trees */
				ElementTreeReader treeReader = new ElementTreeReader(this);
				ElementTree[] trees = treeReader.readDeltaChain(input);
				monitor.worked(6);

				/* attach the restored delta chain to the project's tree */
				workspace.linkTrees(project.getFullPath(), trees);

				monitor.worked(2);

				/* map builder names to trees */
				if (numBuilders > 0) {
					Hashtable builderMap = new Hashtable(trees.length * 2 + 1);
					for (int i = 0; i < numBuilders; i++) {
						builderMap.put(builderNames[i], trees[i]);
					}

					workspace.getBuildManager().setBuilderMap(project, builderMap);
				}
				monitor.worked(1);
			} finally {
				if (input != null)
					input.close();
			}
		} catch (IOException e) {
			String msg = Policy.bind("readMeta", new String[] { project.getFullPath().toString()});
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, project.getFullPath(), msg, e);
		}
	} finally {
		monitor.done();
	}
}
/**
 * Reads the contents of the tree rooted by the given resource from the 
 * file system. This method is used when restoring a complete workspace 
 * after workspace save/shutdown.
 * @exception if the workspace could not be restored.
 */
protected void restoreTree(Workspace workspace, IProgressMonitor monitor) throws CoreException {
	IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), false);
	IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
	if (!treeLocation.toFile().exists() && !tempLocation.toFile().exists()) {
		savedStates = new HashMap(10);
		return;
	}
	try {
		DataInputStream input = null;
		try {
			input = new DataInputStream(new SafeFileInputStream(treeLocation.toOSString(), tempLocation.toOSString()));
			// FIXME: implement version reader code. 
			// check the version number to ensure we can read the file.
			// if the file doesn't have a version number yet, then reset it
			// and continue.
			int version = readVersionNumber(input);
			if (version != ICoreConstants.WORKSPACE_VERSION)
				input = resetStream(input, treeLocation, tempLocation);

			readWorkspaceTree(input, monitor);
		} finally {
			if (input != null)
				input.close();
		}
	} catch (IOException e) {
		String msg = Policy.bind("readMeta", new String[] { treeLocation.toOSString()});
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, treeLocation, msg, e);
	}
}
public IStatus save(int kind, Project project, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String taskName = Policy.bind("saving.1", null);
		monitor.beginTask(taskName, 6);
		MultiStatus warnings = new MultiStatus(ResourcesPlugin.PI_RESOURCES, Status.WARNING, "Save operation warnings", null);
		try {
			workspace.prepareOperation();
			workspace.beginOperation(false);
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
						break;
					case ISaveContext.SNAPSHOT :
						snapTree(workspace.getElementTree(), Policy.subMonitorFor(monitor, 1));
						collapseTrees();
						clearSavedDelta();
						break;
					case ISaveContext.PROJECT_SAVE :
						writeTree(project, IResource.DEPTH_INFINITE);
						// save markers and sync info
						visitAndWrite(project);
						saveMetaInfo(project, null);
						monitor.worked(1);
						break;
				}
				if (kind == ISaveContext.FULL_SAVE || kind == ISaveContext.SNAPSHOT) {
					// write out all metainfo (e.g., workspace/project descriptions) 
					saveMetaInfo(workspace, Policy.subMonitorFor(monitor, 1));
					// save all of the markers and all sync info in the workspace
					visitAndWrite(workspace.getRoot());
					monitor.worked(1);
				} else {
					monitor.worked(2);
				}
				// save contexts
				commit(contexts);
				if (kind == ISaveContext.FULL_SAVE)
					removeClearDeltaMarks();
				// commit ResourcesPlugin master table
				saveMasterTable();
				broadcastLifecycle(DONE_SAVING, contexts, warnings, Policy.subMonitorFor(monitor, 1));
				// as this save operation was successful, we may need to update its participants' save numbers
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
			if (kind == ISaveContext.FULL_SAVE) {
				removeUnusedSafeTables();
				removeUnusedTreeFiles();
				cleanMasterTable();
				workspace.getFileSystemManager().getHistoryStore().clean();
			}
			workspace.endOperation(false, null);
		}
	} finally {
		monitor.done();
	}
}
protected void saveMasterTable() throws CoreException {
	String pluginId = ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
	saveMasterTable(workspace.getMetaArea().getSafeTableLocationFor(pluginId));
}
protected void saveMasterTable(IPath location) throws CoreException {
	java.io.File target = location.toFile();
	try {
		SafeChunkyOutputStream output = new SafeChunkyOutputStream(target);
		try {
			masterTable.store(output, "master table");
			output.succeed();
		} finally {
			output.close();
		}
	} catch (IOException e) {
		String message = "Could not save master table.";
		throw new ResourceException(IResourceStatus.INTERNAL_ERROR, null, message, e);
	}
}
protected void saveMetaInfo(Project project, IProgressMonitor monitor) throws CoreException {
	ProjectDescription description = (ProjectDescription) project.internalGetDescription();
	if (description.isDirty()) {
		workspace.getFileSystemManager().write(project, null);
		description.clean();
	}
}
/**
 * Writes the metainfo (e.g. descriptions) of the given workspace and
 * all projects to the local disk.
 */
protected void saveMetaInfo(Workspace workspace, IProgressMonitor monitor) throws CoreException {
	WorkspaceDescription description = workspace.internalGetDescription();
	if (description.isDirty())
		workspace.getMetaArea().writeWorkspace(description);
	IProject[] roots = workspace.getRoot().getProjects();
	for (int i = 0; i < roots.length; i++)
		saveMetaInfo((Project) roots[i], null);
}
/**
 * Writes the current state of the entire workspace tree to disk.
 * This is used during workspace save.  saveTree(Project)
 * is used to save the state of an individual project.
 * @exception CoreException if there is a problem writing the tree to disk.
 */
protected void saveTree(Map contexts, IProgressMonitor monitor) throws CoreException {
	try {
		IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(workspace.getRoot(), true);
		IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
		DataOutputStream output = new DataOutputStream(new SafeFileOutputStream(treeLocation.toOSString(), tempLocation.toOSString()));
		try {
			writeVersionNumber(output);
			writeTree(computeStatesToSave(contexts, workspace.getElementTree()), output, monitor);
		} finally {
			output.close();
		}
	} catch (Exception e) {
		String msg = Policy.bind("writeMeta", new String[] { Path.ROOT.toString()});
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, Path.ROOT, msg, e);
	}
}
/**
 * Used in the policy for cleaning up tree's of plug-ins that are not often activated.
 */
protected void setDeltaExpiration(String pluginId, long timestamp) {
	masterTable.setProperty(DELTA_EXPIRATION_PREFIX + pluginId, new Long(timestamp).toString());
}
protected void setSaveNumber(String pluginId, int number) {
	masterTable.setProperty(SAVE_NUMBER_PREFIX + pluginId, new Integer(number).toString());
}
public void shutdown(IProgressMonitor monitor) {
}
/**
 * Performs a snapshot if one is deemed necessary.
 * Encapsulates rules for determining when a snapshot is needed.
 * This should be called at the end of every top level operation.
 */
public void snapshotIfNeeded() throws CoreException {
	if (!workspace.internalGetDescription().isSnapshotEnabled() && !snapshotRequested)
		return;
	if (snapshotRequested || operationCount >= workspace.internalGetDescription().getOperationsPerSnapshot()) {
		try {
			ResourceStats.startSnapshot();
			long begin = System.currentTimeMillis();
			save(ISaveContext.SNAPSHOT, null, Policy.monitorFor(null));
			if (ResourcesPlugin.getPlugin().isDebugging()) {
				long end = System.currentTimeMillis();
				System.out.println("Snapshot took: " + (end - begin) + " milliseconds.");
			}
		} finally {
			operationCount = 0;
			snapshotRequested = false;
			ResourceStats.endSnapshot();
		}
	} else {
		operationCount++;
	}
}
/**
 * Performs a snapshot of the workspace tree.
 */
protected void snapTree(ElementTree tree, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask("", Policy.totalWork);
		// don't need to snapshot if there are no changes 
		if (tree == lastSnap)
			return;
		Assert.isTrue(tree.isImmutable(), "The tree must be immutable in a snapshot."); // sanity check
		operationCount = 0;
		IPath snapPath = workspace.getMetaArea().getSnapshotLocationFor(workspace.getRoot());
		ElementTreeWriter writer = new ElementTreeWriter(this);
		java.io.File localFile = snapPath.toFile();
		try {
			SafeChunkyOutputStream safeStream = new SafeChunkyOutputStream(localFile);
			DataOutputStream out = new DataOutputStream(safeStream);
			try {
				writeWorkspaceFields(out, monitor);
				writer.writeDelta(tree, lastSnap, Path.ROOT, writer.D_INFINITE, out, ResourceComparator.getComparator());
				safeStream.succeed();
			} finally {
				out.close();
			}
		} catch (IOException e) {
			String message = Policy.bind("writeWorkspaceMeta", new String[] { localFile.getAbsolutePath()});
			throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, Path.ROOT, message, e);
		}
		lastSnap = tree;
	} finally {
		monitor.done();
	}
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
			Integer next = (Integer) e.nextElement();
			sorted[i] = oldest;
			i--;
		}
		if (i >= 0) {
			/* find the next tree in the list */
			ElementTree parent = oldest.getParent();
			while (table.get(parent) == null) {
				parent = parent.getParent();
			}
			oldest = parent;
		}
	}
	return sorted;
}
public void startup(IProgressMonitor monitor) throws CoreException {
	restore(monitor);
	String pluginId = ResourcesPlugin.getPlugin().getDescriptor().getUniqueIdentifier();
	java.io.File masterTable = workspace.getMetaArea().getSafeTableLocationFor(pluginId).toFile();
	if (!masterTable.exists())
		masterTable.getParentFile().mkdirs();
}
/**
 * Visit the given resource (to depth infinite) and write out extra information
 * like markers and sync info.
 */
public void visitAndWrite(IResource root) throws CoreException {
	// Ensure we have either a project or the workspace root
	Assert.isLegal(root.getType() == IResource.ROOT || root.getType() == IResource.PROJECT);
	// only write out info for accessible resources
	if (!root.isAccessible())
		return;

	// Setup vars
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
			}
		String msg = Policy.bind("writeMeta", new String[] { root.getFullPath().toString()});
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), msg, e);
	}

	final DataOutputStream markersOutput = o1;
	final DataOutputStream syncInfoOutput = o2;

	// Create the visitor 
	IResourceVisitor visitor = new IResourceVisitor() {
		public boolean visit(IResource resource) throws CoreException {
			try {
				markerManager.write(resource, markersOutput, writtenTypes);
				// if we have the workspace root then the output stream will be null
				if (syncInfoOutput != null)
					synchronizer.writeSyncInfo(resource, syncInfoOutput, writtenPartners);
			} catch (IOException e) {
				throw new ResourceException(IResourceStatus.FAILED_WRITE_LOCAL, resource.getFullPath(), "Failed to write meta info for resource.", e);
			}
			return true;
		}
	};

	// Call the visitor
	try {
		int depth = root.getType() == IResource.ROOT ? IResource.DEPTH_ZERO : IResource.DEPTH_INFINITE;
		root.accept(visitor, depth, true);
		removeGarbage(markersOutput, markersLocation, markersTempLocation);
		// if we have the workspace root the output stream will be null and we
		// don't have to perform cleanup code
		if (syncInfoOutput != null)
			removeGarbage(syncInfoOutput, syncInfoLocation, syncInfoTempLocation);
	} catch (IOException e) {
		String msg = Policy.bind("writeMeta", new String[] { root.getFullPath().toString()});
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), msg, e);
	} catch (CoreException e) {
		String msg = Policy.bind("writeMeta", new String[] { root.getFullPath().toString()});
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, root.getFullPath(), msg, e);
	} finally {
		if (markersOutput != null)
			try {
				markersOutput.close();
			} catch (IOException e) {
			}
		if (syncInfoOutput != null)
			try {
				syncInfoOutput.close();
			} catch (IOException e) {
			}
	}

	// recurse over the projects in the workspace if we were given the workspace root
	if (root.getType() == IResource.PROJECT)
		return;
	IProject[] projects = ((IWorkspaceRoot) root).getProjects();
	for (int i = 0; i < projects.length; i++)
		visitAndWrite(projects[i]);
}
/**
 * @see IElementInfoFlattener#writeElement
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
		monitor.beginTask("", Policy.totalWork);
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
			long lastTreeTimestamp = System.currentTimeMillis();
			output.writeInt(statesToSave.size()); // write the number of plugins we are saving
			for (Iterator i = statesToSave.entrySet().iterator(); i.hasNext();) {
				Map.Entry entry = (Map.Entry) i.next();
				String pluginId = (String) entry.getKey();
				output.writeUTF(pluginId);
				trees.add((ElementTree) entry.getValue()); // tree
				setDeltaExpiration(pluginId, lastTreeTimestamp);
			}
			monitor.worked(Policy.totalWork * 10 / 100);

			// add builders' trees
			IProject[] projects = workspace.getRoot().getProjects();
			Map builders = new HashMap(projects.length);
			int nBuilders = 0;
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				if (project.isOpen()) {
					Hashtable namesToBuilderTrees = workspace.getBuildManager().createBuilderMap(project);
					if (namesToBuilderTrees != null) {
						builders.put(project, namesToBuilderTrees);
						nBuilders += namesToBuilderTrees.size();
					}
				}
			}
			output.writeInt(nBuilders); // write the number of builders we are saving
			for (Iterator i = builders.keySet().iterator(); i.hasNext();) {
				IProject project = (IProject) i.next();
				Map namesToBuilderTrees = (Map) builders.get(project);
				for (Iterator j = namesToBuilderTrees.keySet().iterator(); j.hasNext();) {
					String builderName = (String) j.next();
					output.writeUTF(project.getName());
					output.writeUTF(builderName);
					trees.add(namesToBuilderTrees.get(builderName));
				}
			}
			monitor.worked(Policy.totalWork * 10 / 100);

			// add the current tree in the list as the last element
			trees.add(current);

			/* save the forest! */
			ElementTreeWriter writer = new ElementTreeWriter(this);
			ElementTree[] treesToSave = (ElementTree[]) trees.toArray(new ElementTree[trees.size()]);
			writer.writeDeltaChain(treesToSave, Path.ROOT, writer.D_INFINITE, output, ResourceComparator.getComparator());
			monitor.worked(Policy.totalWork * 50 / 100);
		} finally {
			if (!wasImmutable)
				workspace.newWorkingTree();
		}
	} finally {
		monitor.done();
	}
}
protected void writeTree(Project project, int depth) throws CoreException {
	IPath treeLocation = workspace.getMetaArea().getTreeLocationFor(project, true);
	IPath tempLocation = workspace.getMetaArea().getBackupLocationFor(treeLocation);
	try {
		SafeFileOutputStream safe = new SafeFileOutputStream(treeLocation.toOSString(), tempLocation.toOSString());
		try {
			ElementTreeWriter writer = new ElementTreeWriter(this);
			DataOutputStream output = new DataOutputStream(safe);
			writeVersionNumber(output);
			writeTree(project, output, null);
		} finally {
			safe.close();
		}
	} catch (IOException e) {
		String msg = Policy.bind("writeMeta", new String[] { project.getFullPath().toString()});
		throw new ResourceException(IResourceStatus.FAILED_WRITE_METADATA, treeLocation, msg, e);
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
		monitor.beginTask("", 10);
		boolean wasImmutable = false;
		try {
			/**
			 * Obtain a table of String(builder name) -> ElementTree
			 * This includes builders that have never been instantiated
			 * but already had a last built state.
			 */
			Hashtable namesToBuilderTrees = workspace.getBuildManager().createBuilderMap(project);
			final int numBuilders = namesToBuilderTrees == null ? 0 : namesToBuilderTrees.size();
			monitor.worked(1);

			/* write number of builders */
			output.writeInt(numBuilders);
			ElementTree[] trees = new ElementTree[numBuilders + 1];

			/* Make sure the most recent tree is in the array */
			ElementTree current = workspace.getElementTree();
			wasImmutable = current.isImmutable();
			current.immutable();
			trees[numBuilders] = current;

			/* add the tree for each builder to the array */
			if (numBuilders > 0) {
				int nextTree = 0;
				for (Enumeration e = namesToBuilderTrees.keys(); e.hasMoreElements(); nextTree++) {
					String builderName = (String) e.nextElement();
					trees[nextTree] = (ElementTree) namesToBuilderTrees.get(builderName);

					/* write builder name */
					output.writeUTF(builderName);
				}
			}
			monitor.worked(1);

			/* save the forest! */
			ElementTreeWriter writer = new ElementTreeWriter(this);
			writer.writeDeltaChain(trees, project.getFullPath(), writer.D_INFINITE, output, ResourceComparator.getComparator());
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
protected void writeVersionNumber(DataOutputStream output) throws IOException {
	output.writeInt(ICoreConstants.WORKSPACE_VERSION);
}
protected void writeWorkspaceFields(DataOutputStream output, IProgressMonitor monitor) throws IOException, CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		// save the next node id 
		output.writeLong(workspace.nextNodeId);
		// save the modification stamp
		output.writeLong(workspace.nextModificationStamp);
		// save the marker id counter
		output.writeLong(workspace.nextMarkerId);
		// save the registered sync partners in the synchronizer
		 ((Synchronizer) workspace.getSynchronizer()).writePartners(output);
	} finally {
		monitor.done();
	}
}
}
