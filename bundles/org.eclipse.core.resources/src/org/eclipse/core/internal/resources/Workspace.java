/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.resources;

import java.util.*;

import org.eclipse.core.internal.events.BuildManager;
import org.eclipse.core.internal.events.NotificationManager;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.properties.PropertyManager;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.resources.team.IMoveDeleteHook;


public class Workspace extends PlatformObject implements IWorkspace, ICoreConstants {
	protected WorkspaceDescription description;
	protected LocalMetaArea localMetaArea;
	protected boolean openFlag = false;
	protected ElementTree tree;
	protected ElementTree operationTree;		// tree at the start of the current operation
	protected SaveManager saveManager;
	protected BuildManager buildManager;
	protected NatureManager natureManager;
	protected NotificationManager notificationManager;
	protected FileSystemResourceManager fileSystemManager;
	protected PropertyManager propertyManager;
	protected MarkerManager markerManager;
	protected long nextNodeId = 0;
	protected long nextModificationStamp = 0;
	protected long nextMarkerId = 0;
	protected Synchronizer synchronizer;
	protected WorkManager workManager;
	protected IProject[] buildOrder = null;
	protected IWorkspaceRoot defaultRoot = new WorkspaceRoot(Path.ROOT, this);

	protected static final String REFRESH_ON_STARTUP = "-refresh";
	
	/**
	 * File modification validation.  If it is true and validator is null, we try/initialize 
	 * validator first time through.  If false, there is no validator.
	 */
	protected static boolean shouldValidate = true;
	/**
	 * The currently installed file modification validator.
	 */
	protected static IFileModificationValidator validator = null;
	
	/**
	 * The currently installed Move/Delete hook.
	 */
	protected static IMoveDeleteHook moveDeleteHook = null;

	// whether the resources plugin is in debug mode.
	public static boolean DEBUG = false;

	/**
		This field is used to control the access to the workspace tree
	    inside operations. It is useful when calling alien code. Since
	    we usually are in the same thread as the alien code we are calling,
	    our concurrency model would allow the alien code to run operations
	    that could change the tree. If this field is set to true, a
	    beginOperation(true) fails, so the alien code would fail and be logged
	    in our SafeRunnable wrappers, not affecting the normal workspace operation.
	 */
	protected boolean treeLocked;

	/** indicates if the workspace crashed in a previous session */
	protected boolean crashed = false;
public Workspace() {
	super();
	Policy.relocalize();
	localMetaArea = new LocalMetaArea();
	tree = new ElementTree();
	/* tree should only be modified during operations */
	tree.immutable();
	treeLocked = true;
	tree.setTreeData(newElement(IResource.ROOT));
}
/**
 * @see IWorkspace
 */
public void addResourceChangeListener(IResourceChangeListener listener) {
	notificationManager.addListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
}
/**
 * @see IWorkspace
 */
public void addResourceChangeListener(IResourceChangeListener listener, int eventMask) {
	notificationManager.addListener(listener, eventMask);
}
/** 
 * @see IWorkspace
 */
public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException {
	Assert.isNotNull(plugin, "Plugin must not be null");
	Assert.isNotNull(participant, "Participant must not be null");
	return saveManager.addParticipant(plugin, participant);
}
public void beginOperation(boolean createNewTree) throws CoreException {
	WorkManager workManager = getWorkManager();
	workManager.incrementNestedOperations();
	if (!workManager.isBalanced())
		Assert.isTrue(false, "Operation was not prepared.");
	if (treeLocked && createNewTree) {
		String message = Policy.bind("resources.cannotModify");
		throw new ResourceException(IResourceStatus.ERROR, null, message, null);
	}
	if (workManager.getPreparedOperationDepth() > 1) {
		if (createNewTree && tree.isImmutable())
			newWorkingTree();
		return;
	}
	if (createNewTree) {
		// stash the current tree as the basis for this operation.
		operationTree = tree;
		newWorkingTree();
	}
}
private void broadcastChanges(ElementTree currentTree, int type, boolean lockTree, boolean updateState, IProgressMonitor monitor) {
	if (operationTree == null)
		return;
	monitor.subTask(Policy.bind("resources.updating"));
	notificationManager.broadcastChanges(currentTree, type, lockTree, updateState);
}
public void build(int trigger, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(null, Policy.opWork);
		try {
			prepareOperation();
			beginOperation(true);
			getBuildManager().build(trigger, Policy.subMonitorFor(monitor, Policy.opWork));
		} finally {
			getWorkManager().avoidAutoBuild();
			endOperation(false, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/**
 * Notify the relevant infrastructure pieces that the given project is being
 * changed (e.g., setDescription, move, copy...) and various parts of 
 * the workspace need to be
 * notified so they can update cached structures etc.
 */
public void changing(IProject project) throws CoreException {
	buildManager.changing(project);
	natureManager.changing(project);
	notificationManager.changing(project);
	propertyManager.changing(project);
	markerManager.changing(project);
}
/**
 * @see IWorkspace#checkpoint
 */
public void checkpoint(boolean build) {
	boolean immutable = true;
	try {
		/* if it was not called by the current operation, just ignore */
		if (!getWorkManager().isCurrentOperation())
			return;
		immutable = tree.isImmutable();
		broadcastChanges(tree, IResourceChangeEvent.PRE_AUTO_BUILD, false, false, Policy.monitorFor(null));
		if (build && isAutoBuilding())
			getBuildManager().build(IncrementalProjectBuilder.AUTO_BUILD, Policy.monitorFor(null));
		broadcastChanges(tree, IResourceChangeEvent.POST_AUTO_BUILD, false, false, Policy.monitorFor(null));
		broadcastChanges(tree, IResourceChangeEvent.POST_CHANGE, true, true, Policy.monitorFor(null));
		getMarkerManager().resetMarkerDeltas();
	} catch (CoreException e) {
		// ignore any CoreException.  There shouldn't be any as the buildmanager and notification manager
		// should be catching and logging...
	} finally {
		if (!immutable)
			newWorkingTree();
	}
}
/**
 * Deletes all the files and directories from the given root down (inclusive).
 * Returns false if we could not delete some file or an exception occurred
 * at any point in the deletion.
 * Even if an exception occurs, a best effort is made to continue deleting.
 */
public static boolean clear(java.io.File root) {
	boolean result = true;
	if (root.isDirectory()) {
		String[] list = root.list();
		// for some unknown reason, list() can return null.  
		// Just skip the children If it does.
		if (list != null)
			for (int i = 0; i < list.length; i++)
				result &= clear(new java.io.File(root, list[i]));
	}
	try {
		if (root.exists())
			result &= root.delete();
	} catch (Exception e) {
		result = false;
	}
	return result;
}
/**
 * Closes this workspace; ignored if this workspace is not open.
 * The state of this workspace is not saved before the workspace
 * is shut down.
 * <p> 
 * If the workspace was saved immediately prior to closing,
 * it will have the same set of projects
 * (open or closed) when reopened for a subsequent session.
 * Otherwise, closing a workspace may lose some or all of the
 * changes made since the last save or snapshot.
 * </p>
 * <p>
 * Note that session properties are discarded when a workspace is closed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if the workspace could not be shutdown.
 */
public void close(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		String msg = Policy.bind("resources.closing.0");
		int rootCount = tree.getChildCount(Path.ROOT);
		monitor.beginTask(msg, rootCount + 2);
		monitor.subTask(msg);
		//this operation will never end because the world is going away
		String message = Policy.bind("resources.workspaceClose");
		MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
		try {
			prepareOperation();
			if (isOpen()) {
				beginOperation(true);
				IProject[] projects = getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					try {
						Project project = (Project) projects[i];
						project.close(false, Policy.subMonitorFor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
						// Closing the project may have caused it to be deleted
						ResourceInfo info = project.getResourceInfo(true, false);
						if (project.exists(project.getFlags(info), true))
							deleteResource(project);
					} catch (CoreException e) {
						result.add(e.getStatus());
					}
				}
				openFlag = false;
			}
			// endOperation not needed here
		} finally {
			// Shutdown needs to be executed anyway. Doesn't matter if the workspace was not open.
			shutdown(Policy.subMonitorFor(monitor, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
		}
		if (result.matches(IStatus.ERROR))
			throw new ResourceException(result);
	} finally {
		monitor.done();
	}
}
/**
 * Notify the relevant infrastructure pieces that the given project is being
 * closed.  
 */
protected void closing(IProject project) throws CoreException {
	buildManager.closing(project);
	natureManager.closing(project);
	notificationManager.closing(project);
	propertyManager.closing(project);
	markerManager.closing(project);
}
private static HashMap computeCounts(String[][] mappings) {
	HashMap counts = new HashMap(5);
	for (int i = 0; i < mappings.length; i++) {
		String from = mappings[i][0];
		Integer fromCount = (Integer) counts.get(from);
		String to = mappings[i][1];
		if (to == null)
			counts.put(from, new Integer(0));
		else {
			if (((Integer) counts.get(to)) == null)
				counts.put(to, new Integer(0));
			fromCount = fromCount == null ? new Integer(1) : new Integer(fromCount.intValue() + 1);
			counts.put(from, fromCount);
		}
	}
	return counts;
}
public static String[][] computeNodeOrder(String[][] specs) {
	HashMap counts = computeCounts(specs);
	List nodes = new ArrayList(counts.size());
	while (!counts.isEmpty()) {
		List roots = findRootNodes(counts);
		if (roots.isEmpty())
			break;
		for (Iterator i = roots.iterator(); i.hasNext();)
			counts.remove(i.next());
		nodes.addAll(roots);
		removeArcs(specs, roots, counts);
	}
	String[][] result = new String[2][];
	result[0] = (String[]) nodes.toArray(new String[nodes.size()]);
	result[1] = (String[]) counts.keySet().toArray(new String[counts.size()]);
	return result;
}
/**
 * Method declared on IWorkspace
 */
public IProject[][] computePrerequisiteOrder(IProject[] targets) {
	List prereqs = new ArrayList(6);
	IProject[] projects = getRoot().getProjects();
	for (int i = 0; i < projects.length; i++) {
		prereqs.add(new String[] { projects[i].getName(), null });
		try {
			IProject[] prereqList = projects[i].getReferencedProjects();
			for (int j = 0; j < prereqList.length; j++)
				prereqs.add(new String[] { projects[i].getName(), prereqList[j].getName()});
		} catch (CoreException e) {
			// ignore closed and non-existant projects
		}
	}
	String[][] prereqArray = (String[][]) prereqs.toArray(new String[prereqs.size()][]);
	String[][] ordered = computeNodeOrder(prereqArray);
	HashSet filter = new HashSet(Arrays.asList(targets));
	IProject[][] result = new IProject[2][];
	// build the result list for the ordered projects.  Trim off any projects which
	// were not requested.
	ArrayList list = new ArrayList(ordered[0].length);
	for (int i = 0; i < ordered[0].length; i++) {
		IProject project = getRoot().getProject(ordered[0][i]);
		if (filter.contains(project))
			list.add(project);
	}
	result[0] = (IProject[])list.toArray(new IProject[list.size()]);

	// build the result list for the ordered projects.  Trim off any projects which
	// were not requested.
	list = new ArrayList(ordered[0].length);
	for (int i = 0; i < ordered[1].length; i++) {
		IProject project = getRoot().getProject(ordered[1][i]);
		if (filter.contains(project))
			list.add(project);
	}
	result[1] = (IProject[])list.toArray(new IProject[list.size()]);
	return result;
}

/*
 * @see IWorkspace#copy
 */
public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int opWork = Math.max(resources.length, 1);
		int totalWork = Policy.totalWork * opWork / Policy.opWork;
		String message = Policy.bind("resources.copying.0");
		monitor.beginTask(message, totalWork);
		Assert.isLegal(resources != null);
		if (resources.length == 0)
			return ResourceStatus.OK_STATUS;
		// to avoid concurrent changes to this array
		resources = (IResource[]) resources.clone();
		IPath parentPath = null;
		message = Policy.bind("resources.copyProblem");
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
		try {
			prepareOperation();
			beginOperation(true);
			for (int i = 0; i < resources.length; i++) {
				Policy.checkCanceled(monitor);
				IResource resource = resources[i];
				if (resource == null || isDuplicate(resources, i)) {
					monitor.worked(1);
					continue;
				}
				// test siblings
				if (parentPath == null)
					parentPath = resource.getFullPath().removeLastSegments(1);
				if (parentPath.equals(resource.getFullPath().removeLastSegments(1))) {
					// test copy requirements
					try {
						IStatus requirements = ((Resource) resource).checkCopyRequirements(destination.append(resource.getName()), resource.getType());
						if (requirements.isOK()) {
							try {
								resource.copy(destination.append(resource.getName()), updateFlags, Policy.subMonitorFor(monitor, 1));
							} catch (CoreException e) {
								status.merge(e.getStatus());
							}
						} else {
							monitor.worked(1);
							status.merge(requirements);
						}
					} catch (CoreException e) {
						monitor.worked(1);
						status.merge(e.getStatus());
					}
				} else {
					monitor.worked(1);
					message = Policy.bind("resources.notChild", resources[i].getFullPath().toString(), parentPath.toString());
					status.merge(new ResourceStatus(IResourceStatus.OPERATION_FAILED, resources[i].getFullPath(), message));
				}
			}
		} catch (OperationCanceledException e) {
			getWorkManager().operationCanceled();
			throw e;
		} finally {
			endOperation(true, Policy.subMonitorFor(monitor, totalWork - opWork));
		}
		if (status.matches(IStatus.ERROR))
			throw new ResourceException(status);
		return status.isOK() ? ResourceStatus.OK_STATUS : (IStatus) status;
	} finally {
		monitor.done();
	}
}

/**
 * @see IWorkspace#copy
 */
public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	return copy(resources, destination, updateFlags , monitor);
}

protected void copyTree(IResource source, IPath destination, int depth, boolean phantom, boolean overwrite) throws CoreException {
	// FIXME: change this method signature to use the int updateFlags rather than the booleans.

	// retrieve the resource at the destination if there is one (phantoms included).
	// if there isn't one, then create a new handle based on the type that we are
	// trying to copy
	IResource destinationResource = getRoot().findMember(destination, true);
	if (destinationResource == null) {
		int destinationType;
		if (source.getType() == IResource.FILE)
			destinationType = IResource.FILE;
		else
			if (destination.segmentCount() == 1)
				destinationType = IResource.PROJECT;
			else
				destinationType = IResource.FOLDER;
		destinationResource = newResource(destination, destinationType);
	}

	// create the resource at the destination
	ResourceInfo sourceInfo = ((Resource) source).getResourceInfo(true, false);
	if (destinationResource.getType() != source.getType()) {
		sourceInfo = (ResourceInfo) sourceInfo.clone();
		sourceInfo.setType(destinationResource.getType());
	}
	ResourceInfo newInfo = createResource(destinationResource, sourceInfo, phantom, overwrite);
	// get/set the node id from the source's resource info so we can later put it in the
	// info for the destination resource. This will help us generate the proper deltas,
	// indicating a move rather than a add/delete
	long nodeid = ((Resource) source).getResourceInfo(true, false).getNodeId();
	newInfo.setNodeId(nodeid);

	// preserve local sync info
	ResourceInfo oldInfo = ((Resource) source).getResourceInfo(true, false);
	newInfo.setFlags(newInfo.getFlags() | (oldInfo.getFlags() & M_LOCAL_EXISTS));

	// do the recursion. if we have a file then it has no members so return. otherwise
	// recursively call this method on the container's members if the depth tells us to
	if (depth == IResource.DEPTH_ZERO || source.getType() == IResource.FILE)
		return;
	if (depth == IResource.DEPTH_ONE)
		depth = IResource.DEPTH_ZERO;
	int flags = phantom ? IContainer.INCLUDE_PHANTOMS : IResource.NONE;
	flags |= IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS;
	IResource[] children = ((IContainer) source).members(flags);
	for (int i = 0; i < children.length; i++) {
		IResource child = children[i];
		IPath childPath = destination.append(child.getName());
		copyTree(child, childPath, depth, phantom, overwrite);
	}
}
/**
 * Returns the number of resources in a subtree of the resource tree.
 * @param path The subtree to count resources for
 * @param depth The depth of the subtree to count
 * @param phantom If true, phantoms are included, otherwise they are ignored.
 */
public int countResources(IPath root, int depth, final boolean phantom) {
	if (!tree.includes(root))
		return 0;
	switch (depth) {
		case IResource.DEPTH_ZERO:
			return 1;
		case IResource.DEPTH_ONE:
			return 1 + tree.getChildCount(root);
		case IResource.DEPTH_INFINITE:
			final int[] count = new int[1];
			IElementContentVisitor visitor = new IElementContentVisitor() {
				public void visitElement(ElementTree tree, IPath elementPath, Object elementContents) {
					if (phantom || !((ResourceInfo)elementContents).isSet(M_PHANTOM))
						count[0]++;
				}
			};
			new ElementTreeIterator().iterate(tree, visitor, root);
			return count[0];
	}
	return 0;
}
/*
 * Creates the given resource in the tree and returns the new resource info object.  
 * If phantom is true, the created element is marked as a phantom.
 * If there is already be an element in the tree for the given resource
 * in the given state (i.e., phantom), a CoreException is thrown.  
 * If there is already a phantom in the tree and the phantom flag is false, 
 * the element is overwritten with the new element. (but the synchronization
 * information is preserved) If the specified resource info is null, then create
 * a new one.
 */
public ResourceInfo createResource(IResource resource, ResourceInfo info, boolean phantom, boolean overwrite) throws CoreException {
	info = info == null ? newElement(resource.getType()) : (ResourceInfo) info.clone();
	ResourceInfo original = getResourceInfo(resource.getFullPath(), true, false);
	if (phantom) {
		info.set(M_PHANTOM);
		info.setModificationStamp(IResource.NULL_STAMP);
	}
	// if nothing existed at the destination then just create the resource in the tree
	if (original == null) {
		// we got here from a copy/move. we don't want to copy over any sync info
		// from the source so clear it.
		info.setSyncInfo(null);
		tree.createElement(resource.getFullPath(), info);
	} else {
		// if overwrite==true then slam the new info into the tree even if one existed before
		if (overwrite || (!phantom && original.isSet(M_PHANTOM))) {
			// copy over the sync info and flags from the old resource info
			// since we are replacing a phantom with a real resource
			// DO NOT set the sync info dirty flag because we want to
			// preserve the old sync info so its not dirty
			// XXX: must copy over the generic sync info from the old info to the new
			// XXX: do we really need to clone the sync info here?
			info.setSyncInfo(original.getSyncInfo(true));
			// mark the markers bit as dirty so we snapshot an empty marker set for
			// the new resource
			info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
			tree.setElementData(resource.getFullPath(), info);
		} else {
			String message = Policy.bind("resources.mustNotExist", resource.getFullPath().toString());
			throw new ResourceException(IResourceStatus.RESOURCE_EXISTS, resource.getFullPath(), message, null);
		}
	}
	return info;
}
/*
 * Creates the given resource in the tree and returns the new resource info object.  
 * If phantom is true, the created element is marked as a phantom.
 * If there is already be an element in the tree for the given resource
 * in the given state (i.e., phantom), a CoreException is thrown.  
 * If there is already a phantom in the tree and the phantom flag is false, 
 * the element is overwritten with the new element. (but the synchronization
 * information is preserved)
 */
public ResourceInfo createResource(IResource resource, boolean phantom) throws CoreException {
	return createResource(resource, null, phantom, false);
}
public ResourceInfo createResource(IResource resource, boolean phantom, boolean overwrite) throws CoreException {
	return createResource(resource, null, phantom, overwrite);
}
public static WorkspaceDescription defaultWorkspaceDescription() {
	return new WorkspaceDescription("Workspace");
}

/*
 * @see IWorkspace#delete
 */
public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int opWork = Math.max(resources.length, 1);
		int totalWork = Policy.totalWork * opWork / Policy.opWork;
		String message = Policy.bind("resources.deleting.0");
		monitor.beginTask(message, totalWork);
		message = Policy.bind("resources.deleteProblem");
		MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
		if (resources.length == 0)
			return result;
		resources = (IResource[]) resources.clone(); // to avoid concurrent changes to this array
		try {
			prepareOperation();
			beginOperation(true);
			for (int i = 0; i < resources.length; i++) {
				Policy.checkCanceled(monitor);
				Resource resource = (Resource) resources[i];
				if (resource == null) {
					monitor.worked(1);
					continue;
				}
				try {
					resource.delete(updateFlags, Policy.subMonitorFor(monitor, 1));
				} catch (CoreException e) {
					// Don't really care about the exception unless the resource is still around.
					ResourceInfo info = resource.getResourceInfo(false, false);
					if (resource.exists(resource.getFlags(info), false)) {
						message = Policy.bind("resources.couldnotDelete", resource.getFullPath().toString());
						result.merge(new ResourceStatus(IResourceStatus.FAILED_DELETE_LOCAL, resource.getFullPath(), message));
						result.merge(e.getStatus());
					}
				}
			}
			if (result.matches(IStatus.ERROR))
				throw new ResourceException(result);
			return result;
		} catch (OperationCanceledException e) {
			getWorkManager().operationCanceled();
			throw e;
		} finally {
			endOperation(true, Policy.subMonitorFor(monitor, totalWork - opWork));
		}
	} finally {
		monitor.done();
	}
}

/*
 * @see IWorkspace#delete
 */
public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= IResource.KEEP_HISTORY;
	return delete(resources, updateFlags, monitor);
}

/**
 * @see IWorkspace
 */
public void deleteMarkers(IMarker[] markers) throws CoreException {
	Assert.isNotNull(markers);
	if (markers.length == 0)
		return;
	// clone to avoid outside changes
	markers = (IMarker[]) markers.clone();
	try {
		prepareOperation();
		beginOperation(true);
		for (int i = 0; i < markers.length; ++i)
			if (markers[i] != null && markers[i].getResource() != null)
				markerManager.removeMarker(markers[i].getResource(), markers[i].getId());
	} finally {
		endOperation(false, null);
	}
}
/**
 * Delete the given resource from the current tree of the receiver.
 * This method simply removes the resource from the tree.  No cleanup or 
 * other management is done.  Use IResource.delete for proper deletion.
 * If the given resource is the root, all of its children (i.e., all projects) are
 * deleted but the root is left.
 */
void deleteResource(IResource resource) {
	IPath path = resource.getFullPath();
	if (path.equals(Path.ROOT)) {
		IProject[] children = getRoot().getProjects();
		for (int i = 0; i < children.length; i++)
			tree.deleteElement(children[i].getFullPath());
	} else
		tree.deleteElement(path);
}
/**
 * Notify the relevant infrastructure pieces that the given project is being
 * deleted. 
 */
protected void deleting(IProject project) throws CoreException {
	buildManager.deleting(project);
	natureManager.deleting(project);
	notificationManager.deleting(project);
	propertyManager.deleting(project);
	markerManager.deleting(project);
}
/**
 * For debugging purposes only.  Dumps plugin stats to console
 */
public void dumpStats() {
	ResourceStats.dumpStats();
}
/**
 * End an operation (group of resource changes).
 * Notify interested parties that some resource changes have taken place.
 * This is used in the middle of batch executions to broadcast intermediate
 * results.  All registered resource change listeners are notified.  If autobuilding
 * is enabled, a build is run.
 */
public void endOperation(boolean build, IProgressMonitor monitor) throws CoreException {
	WorkManager workManager = getWorkManager();
	try {
		workManager.setBuild(build);
		// if we are not exiting a top level operation then just decrement the count and return
		if (workManager.getPreparedOperationDepth() > 1) 
			return;
			
		// do the following in a try/finally to ensure that the operation tree is null'd at the end
		// as we are completing a top level operation.
		try {
			// if the tree is locked we likely got here in some finally block after a failed begin.
			// Since the tree is locked, nothing could have been done so there is nothing to do.
			Assert.isTrue(!(treeLocked && workManager.shouldBuild()), "The tree should not be locked.");
			// check for a programming error on using beginOperation/endOperation
			Assert.isTrue(workManager.getPreparedOperationDepth() > 0, "Mismatched begin/endOperation");
	
			// At this time we need to rebalance the nested operations. It is necessary because
			// build() and snapshot() should not fail if they are called.
			workManager.rebalanceNestedOperations();
	
			// If autobuild is on, give each open project a chance to build.  We have to tell each one
			// because there is no way of knowing whether or not there is a relevant change
			// for the project without computing the delta for each builder in each project relative
			// to its last built state.  If we have guaranteed corelation between the notification delta
			// and the last time autobuild was done, then we could look at the notification delta and
			// see which projects had changed and only build them.  Currently there is no such
			// guarantee.   
			// Note that  building a project when there is actually nothing to do is not free but
			// is should not be too expensive.  The computed delta will be empty and so the builder itself
			// will not actually be run.  This does require however the delta computation.
			//
			// This is done in a try finally to ensure that we always decrement the operation count.
			// The operationCount cannot be decremented before this as the build must be done
			// inside an operation.  Note that we only ever get here if we are at a top level operation.
			// As such, the operationCount will always be 0 (zero) after this.
			OperationCanceledException cancel = null;
			CoreException signal = null;
			monitor = Policy.monitorFor(monitor);
			monitor.subTask(Policy.bind("resources.updating"));
			broadcastChanges(tree, IResourceChangeEvent.PRE_AUTO_BUILD, false, false, Policy.monitorFor(null));
			if (isAutoBuilding() && shouldBuild()) {
				try {
					getBuildManager().build(IncrementalProjectBuilder.AUTO_BUILD, monitor);
				} catch (OperationCanceledException e) {
					cancel = e;
				} catch (CoreException sig) {
					signal = sig;
				}
			}
			broadcastChanges(tree, IResourceChangeEvent.POST_AUTO_BUILD, false, false, Policy.monitorFor(null));
			broadcastChanges(tree, IResourceChangeEvent.POST_CHANGE, true, true, Policy.monitorFor(null));
			getMarkerManager().resetMarkerDeltas();
			// Perform a snapshot if we are sufficiently out of date.  Be sure to make the tree immutable first
			tree.immutable();
			saveManager.snapshotIfNeeded();
			//make sure the monitor subtask message is cleared.
			monitor.subTask("");
			if (cancel != null)
				throw cancel;
			if (signal != null)
				throw signal;
		} finally {
			// make sure that the tree is immutable.  Only do this if we are ending a top-level operation.
			tree.immutable();
			operationTree = null;
		}
	} finally {
		workManager.checkOut();
	}
}
private static List findRootNodes(HashMap counts) {
	List result = new ArrayList(5);
	for (Iterator i = counts.keySet().iterator(); i.hasNext();) {
		String node = (String) i.next();
		int count = ((Integer) counts.get(node)).intValue();
		if (count == 0)
			result.add(node);
	}
	return result;
}
/**
 * A file modification validator hasn't been initialized. Check the extension point and 
 * try to create a new validator if a user has one defined as an extension.
 */
protected static void initializeValidator() {
	shouldValidate = false;
	IConfigurationElement[] configs = Platform.getPluginRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILE_MODIFICATION_VALIDATOR);
	// no-one is plugged into the extension point so disable validation
	if (configs == null || configs.length == 0) {
		return;
	}
	// can only have one defined at a time. log a warning, disable validation, but continue with
	// the #setContents (e.g. don't throw an exception)
	if (configs.length > 1) {
		//XXX: shoud provide a meaningful status code
		IStatus status = new ResourceStatus(IResourceStatus.ERROR, 1, null, Policy.bind("resources.oneValidator"), null);
		ResourcesPlugin.getPlugin().getLog().log(status);
		return;
	}
	// otherwise we have exactly one validator extension. Try to create a new instance 
	// from the user-specified class.
	try {
		IConfigurationElement config = configs[0];
		validator = (IFileModificationValidator) config.createExecutableExtension("class");
		shouldValidate = true;
	} catch (CoreException e) {
		//XXX: shoud provide a meaningful status code
		IStatus status = new ResourceStatus(IResourceStatus.ERROR, 1, null, Policy.bind("resources.initValidator"), e);
		ResourcesPlugin.getPlugin().getLog().log(status);
	}
}
/**
 * A move/delete hook hasn't been initialized. Check the extension point and 
 * try to create a new hook if a user has one defined as an extension. Otherwise
 * use the Core's implementation as the default.
 */
protected static void initializeMoveDeleteHook() {
	try {
		IConfigurationElement[] configs = Platform.getPluginRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MOVE_DELETE_HOOK);
		// no-one is plugged into the extension point so disable validation
		if (configs == null || configs.length == 0) {
			return;
		}
		// can only have one defined at a time. log a warning
		if (configs.length > 1) {
			//XXX: shoud provide a meaningful status code
			IStatus status = new ResourceStatus(IResourceStatus.ERROR, 1, null, Policy.bind("resources.oneHook"), null);
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}
		// otherwise we have exactly one hook extension. Try to create a new instance 
		// from the user-specified class.
		try {
			IConfigurationElement config = configs[0];
			moveDeleteHook = (IMoveDeleteHook) config.createExecutableExtension("class");
		} catch (CoreException e) {
			//XXX: shoud provide a meaningful status code
			IStatus status = new ResourceStatus(IResourceStatus.ERROR, 1, null, Policy.bind("resources.initHook"), e);
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	} finally {
		// for now just use Core's implementation
		if (moveDeleteHook == null)
			moveDeleteHook = new MoveDeleteHook();
	}
}
protected IMoveDeleteHook getMoveDeleteHook() {
	if (moveDeleteHook == null)
		initializeMoveDeleteHook();
	return moveDeleteHook;
}
/**
 * Flush the build order cache for the workspace.  Only needed if the
 * description does not already have a build order.  That is, if this
 * is really a cache.
 */
protected void flushBuildOrder() {
	if (description.getBuildOrder(false) == null)
		buildOrder = null;
}
/** 
 * @see IWorkspace
 */
public void forgetSavedTree(String pluginId) {
	Assert.isNotNull(pluginId, "PluginId must not be null");
	saveManager.forgetSavedTree(pluginId);
}
/**
 * Returns this workspace's build manager
 */
public BuildManager getBuildManager() {
	return buildManager;
}
/**
 * Returns this workspace's build order
 */
public IProject[] getBuildOrder() {
	if (buildOrder != null)
		return buildOrder;
	String[] order = description.getBuildOrder(false);
	if (order == null)
		buildOrder = computePrerequisiteOrder(getRoot().getProjects())[0];
	else {
		buildOrder = new IProject[order.length];
		for (int i = 0; i < order.length; i++)
			buildOrder[i] = getRoot().getProject(order[i]);
	}
	return buildOrder;
}
/**
 * @see IWorkspace#getDanglingReferences
 */
public Map getDanglingReferences() {
	IProject[] projects = getRoot().getProjects();
	Map result = new HashMap(projects.length);
	for (int i = 0; i < projects.length; i++) {
		Project project = (Project) projects[i];
		if (!project.isAccessible())
			continue;
		IProject[] refs = project.internalGetDescription().getReferencedProjects(false);
		List dangling = new ArrayList(refs.length);
		for (int j = 0; j < refs.length; j++)
			if (!refs[i].exists())
				dangling.add(refs[i]);
		if (!dangling.isEmpty())
			result.put(projects[i], dangling.toArray(new IProject[dangling.size()]));
	}
	return result;
}
/**
 * @see IWorkspace
 */
public IWorkspaceDescription getDescription() {
	return (IWorkspaceDescription) description.clone();
}
/** 
 * Returns the current element tree for this workspace
 */
public ElementTree getElementTree() {
	return tree;
}
public FileSystemResourceManager getFileSystemManager() {
	return fileSystemManager;
}
/**
 * Returns the marker manager for this workspace
 */
public MarkerManager getMarkerManager() {
	return markerManager;
}
public LocalMetaArea getMetaArea() {
	return localMetaArea;
}
/**
 * @see IWorkspace#getNatureDescriptor(String)
 */
public IProjectNatureDescriptor getNatureDescriptor(String natureId) {
	return natureManager.getNatureDescriptor(natureId);
}
/**
 * @see IWorkspace#getNatureDescriptors()
 */
public IProjectNatureDescriptor[] getNatureDescriptors() {
	return natureManager.getNatureDescriptors();
}
/**
 * Returns the nature manager for this workspace.
 */
public NatureManager getNatureManager() {
	return natureManager;
}
public NotificationManager getNotificationManager() {
	return notificationManager;
}
public PropertyManager getPropertyManager() {
	return propertyManager;
}
/**
 * Returns the resource info for the identified resource.
 * null is returned if no such resource can be found.
 * If the phantom flag is true, phantom resources are considered.
 * If the mutable flag is true, the info is opened for change.
 *
 * This method DOES NOT throw an exception if the resource is not found.
 */
public ResourceInfo getResourceInfo(IPath path, boolean phantom, boolean mutable) {
	try {
		if (path.segmentCount() == 0) {
			ResourceInfo info = (ResourceInfo)tree.getTreeData();
			Assert.isNotNull(info, "Tree root info must never be null");
			return info;
		}
		ResourceInfo result = null;
		if (!tree.includes(path))
			return null;
		if (mutable)
			result = (ResourceInfo) tree.openElementData(path);
		else
			result = (ResourceInfo) tree.getElementData(path);
		if (result != null && (!phantom && result.isSet(M_PHANTOM)))
			return null;
		return result;
	} catch (IllegalArgumentException e) {
		return null;
	}
}
/**
 * @see IWorkspace#getRoot
 */
public IWorkspaceRoot getRoot() {
	return defaultRoot;
}
public SaveManager getSaveManager() {
	return saveManager;
}
/**
 * @see IWorkspace#getSynchronizer
 */
public ISynchronizer getSynchronizer() {
	return synchronizer;
}
/**
 * We should not have direct references to this field. All references should go through
 * this method.
 */
public WorkManager getWorkManager() throws CoreException {
	if (workManager == null) {
		String message = Policy.bind("resources.shutdown");
		throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message));
	}
	return workManager;
}
protected IWorkspace getWorkspace() {
	return this;
}

public WorkspaceDescription internalGetDescription() {
	return description;
}
/**
 * @see IWorkspace
 */
public boolean isAutoBuilding() {
	return description.isAutoBuilding();
}
/**
 * Returns true if the object at the specified position has any
 * other copy in the given array.
 */
private static boolean isDuplicate(Object[] array, int position) {
	if (array == null || position >= array.length)
		return false;
	for (int j = position - 1; j >= 0; j--)
		if (array[j].equals(array[position]))
			return true;
	return false;
}
/**
 * @see IWorkspace#isOpen
 */
public boolean isOpen() {
	return openFlag;
}
public boolean isTreeLocked() {
	return treeLocked;
}
/**
 * Link the given tree into the receiver's tree at the specified resource.
 */
protected void linkTrees(IPath path, ElementTree[] newTrees) throws CoreException {
	tree = tree.mergeDeltaChain(path, newTrees);
}

/*
 * @see IWorkspace#move
 */
public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		int opWork = Math.max(resources.length, 1);
		int totalWork = Policy.totalWork * opWork / Policy.opWork;
		String message = Policy.bind("resources.moving.0");
		monitor.beginTask(message, totalWork);
		Assert.isLegal(resources != null);
		if (resources.length == 0)
			return ResourceStatus.OK_STATUS;
		resources = (IResource[]) resources.clone(); // to avoid concurrent changes to this array
		IPath parentPath = null;
		message = Policy.bind("resources.moveProblem");
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
		try {
			prepareOperation();
			beginOperation(true);
			for (int i = 0; i < resources.length; i++) {
				Policy.checkCanceled(monitor);
				Resource resource = (Resource) resources[i];
				if (resource == null || isDuplicate(resources, i)) {
					monitor.worked(1);
					continue;
				}
				// test siblings
				if (parentPath == null)
					parentPath = resource.getFullPath().removeLastSegments(1);
				if (parentPath.equals(resource.getFullPath().removeLastSegments(1))) {
					// test move requirements
					try {
						IStatus requirements = resource.checkMoveRequirements(destination.append(resource.getName()), resource.getType());
						if (requirements.isOK()) {
							try {
								resource.move(destination.append(resource.getName()), updateFlags, Policy.subMonitorFor(monitor, 1));
							} catch (CoreException e) {
								status.merge(e.getStatus());
							}
						} else {
							monitor.worked(1);
							status.merge(requirements);
						}
					} catch (CoreException e) {
						monitor.worked(1);
						status.merge(e.getStatus());
					}
				} else {
					monitor.worked(1);
					message = Policy.bind("resources.notChild", resource.getFullPath().toString(), parentPath.toString());
					status.merge(new ResourceStatus(IResourceStatus.OPERATION_FAILED, resource.getFullPath(), message));
				}
			}
		} catch (OperationCanceledException e) {
			getWorkManager().operationCanceled();
			throw e;
		} finally {
			endOperation(true, Policy.subMonitorFor(monitor, totalWork - opWork));
		}
		if (status.matches(IStatus.ERROR))
			throw new ResourceException(status);
		return status.isOK() ? (IStatus) ResourceStatus.OK_STATUS : (IStatus) status;
	} finally {
		monitor.done();
	}
}

/**
 * @see IWorkspace#move
 */
public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
	int updateFlags = force ? IResource.FORCE : IResource.NONE;
	updateFlags |= IResource.KEEP_HISTORY;
	return move(resources, destination, updateFlags, monitor);
}

/**
 * Moves this resource's subtree to the destination. This operation should only be
 * used by move methods. Destination must be a valid destination for this resource.
 */

/* package */ void move(Resource source, IPath destination, int depth, boolean overwrite) throws CoreException {
	// overlay the tree at the destination path, preserving any important info
	// in any already existing resource infos
	copyTree(source, destination, depth, false, overwrite);
	source.fixupAfterMoveSource();
}
/**
 * Create and return a new tree element of the given type.
 */
protected ResourceInfo newElement(int type) {
	ResourceInfo result = null;
	switch (type) {
		case IResource.FILE :
		case IResource.FOLDER :
			result = new ResourceInfo();
			break;
		case IResource.PROJECT :
			result = new ProjectInfo();
			break;
		case IResource.ROOT :
			result = new RootInfo();
			break;
	}
	result.setNodeId(nextNodeId());
	result.setModificationStamp(nextModificationStamp());
	result.setType(type);
	return result;
}
/**
 * @see IWorkspace#newProjectDescription
 */
public IProjectDescription newProjectDescription(String projectName) {
	IProjectDescription result = new ProjectDescription();
	result.setName(projectName);
	return result;
}
public Resource newResource(IPath path, int type) {
	String message;
	switch (type) {
		case IResource.FOLDER :
			message = "Path must include project and resource name.";
			Assert.isLegal(path.segmentCount() >= ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH , message);
			return new Folder(path.makeAbsolute(), this);
		case IResource.FILE :
			message = "Path must include project and resource name.";
			Assert.isLegal(path.segmentCount() >= ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH, message);
			return new File(path.makeAbsolute(), this);
		case IResource.PROJECT :
			return (Resource) getRoot().getProject(path.lastSegment());
		case IResource.ROOT :
			return (Resource) getRoot();
	}
	Assert.isLegal(false);
	// will never get here because of assertion.
	return null;
}
/**
 * Opens a new mutable element tree layer, thus allowing 
 * modifications to the tree.
 */
public ElementTree newWorkingTree() {
	tree = tree.newEmptyDelta();
	return tree;
}
/**
 * Returns the next, previously unassigned, marker id.
 */
protected long nextMarkerId() {
	return nextMarkerId++;
}
public long nextModificationStamp() {
	return nextModificationStamp++;
}
public long nextNodeId() {
	return nextNodeId++;
}
/**
 * Opens this workspace using the data at its location in the local file system.
 * This workspace must not be open.
 * If the operation succeeds, the result will detail any serious
 * (but non-fatal) problems encountered while opening the workspace.
 * The status code will be <code>OK</code> if there were no problems.
 * An exception is thrown if there are fatal problems opening the workspace,
 * in which case the workspace is left closed.
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return status with code <code>OK</code> if no problems;
 *     otherwise status describing any serious but non-fatal problems.
 *     
 * @exception CoreException if the workspace could not be opened.
 * Reasons include:
 * <ul>
 * <li> There is no valid workspace structure at the given location
 *      in the local file system.</li>
 * <li> The workspace structure on disk appears to be hopelessly corrupt.</li>
 * </ul>
 * @see IWorkspace#getLocation
 * @see ResourcePlugin#containsWorkspace
 */
public IStatus open(IProgressMonitor monitor) throws CoreException {
	// This method is not inside an operation because it is the one responsible for
	// creating the WorkManager object (who takes care of operations).
	String message = Policy.bind("resources.workspaceOpen");
	Assert.isTrue(!isOpen(), message);
	description = getMetaArea().readWorkspace();
	if (description == null) {
		message = Policy.bind("resources.readWorkspaceMeta");
		throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, Platform.getLocation(), message, null);
	}

	// create root location
	localMetaArea.locationFor(getRoot()).toFile().mkdirs();
		
	// turn off autobuilding while we open the workspace.  This is in
	// case an operation is triggered.  We don't want to do an autobuild
	// just yet. Any changes will be reported the next time we build.
	boolean oldBuildFlag = description.isAutoBuilding();
	try {
		description.setAutoBuilding(false);
		IProgressMonitor nullMonitor = Policy.monitorFor(null);
		startup(nullMonitor);
		//restart the notification manager so it is initialized with the right tree
		notificationManager.startup(null);
		openFlag = true;
		if (crashed || refreshRequested()) {
			try {
				getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				//don't fail entire open if refresh failed, just report as minor warning
				return e.getStatus();
			}
		}
		return ResourceStatus.OK_STATUS;
	} finally {
		description.setAutoBuilding(oldBuildFlag);
	}
}
/**
 * Notify the relevant infrastructure pieces that the given project is being
 * opened. 
 */
protected void opening(IProject project) throws CoreException {
	buildManager.opening(project);
	natureManager.opening(project);
	notificationManager.opening(project);
	propertyManager.opening(project);
	markerManager.opening(project);
}
/**
 * Called before checking the pre-conditions of an operation.
 */
public void prepareOperation() throws CoreException {
	getWorkManager().checkIn();
	if (!isOpen()) {
		String message = Policy.bind("resources.workspaceClosed");
		throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, null);
	}
}

protected boolean refreshRequested() {
	String[] args = Platform.getCommandLineArgs();
	for (int i = 0; i < args.length; i++) 
		if (args[i].equalsIgnoreCase(REFRESH_ON_STARTUP))
			return true;
	return false;
}
private static void removeArcs(String[][] mappings, List roots, HashMap counts) {
	for (Iterator j = roots.iterator(); j.hasNext();) {
		String root = (String) j.next();
		for (int i = 0; i < mappings.length; i++) {
			if (root.equals(mappings[i][1])) {
				String input = mappings[i][0];
				Integer count = (Integer) counts.get(input);
				if (count != null)
					counts.put(input, new Integer(count.intValue() - 1));
			}
		}
	}
}
/**
 * @see IWorkspace
 */
public void removeResourceChangeListener(IResourceChangeListener listener) {
	notificationManager.removeListener(listener);
}
/**
 * @see IWorkspace
 */
public void removeSaveParticipant(Plugin plugin) {
	Assert.isNotNull(plugin, "Plugin must not be null");
	saveManager.removeParticipant(plugin);
}
/**
 * @see IWorkspace#run
 */
public void run(IWorkspaceRunnable job, IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		monitor.beginTask(null, Policy.totalWork);
		try {
			prepareOperation();
			beginOperation(true);
			job.run(Policy.subMonitorFor(monitor, Policy.opWork, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
		} catch (OperationCanceledException e) {
			getWorkManager().operationCanceled();
			throw e;
		} finally {
			endOperation(false, Policy.subMonitorFor(monitor, Policy.buildWork));
		}
	} finally {
		monitor.done();
	}
}
/** 
 * @see IWorkspace
 */
public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException {
	String message;
	// if a full save was requested, and this is a top-level op, try the save.  Otherwise
	// fail the operation.
	if (full) {
		if (getWorkManager().getPreparedOperationDepth() > 0) {
			message = Policy.bind("resources.saveOp");
			throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, null);
		} 
		return saveManager.save(ISaveContext.FULL_SAVE, null, monitor);
	}
	// A snapshot was requested.  Start an operation (if not altready started) and 
	// signal that a snapshot should be done at the end.
	try {
		prepareOperation();
		beginOperation(false);
		saveManager.requestSnapshot();
		message = Policy.bind("resources.snapRequest");
		return new ResourceStatus(IResourceStatus.OK, message);
	} finally {
		endOperation(false, null);
	}
}
public void setCrashed(boolean value) {
	crashed = value;
}
/**
 * @see IWorkspace
 */
public void setDescription(IWorkspaceDescription value) throws CoreException {
	// if both the old and new description's build orders are null, leave the
	// workspace's build order slot because it is caching the computed order.
	// Otherwise, set the slot to null to force recomputation or building from the description.
	String[] newOrder = ((WorkspaceDescription) value).getBuildOrder(false);
	if (description.getBuildOrder(false) != null || newOrder != null)
		buildOrder = null;
	description = (WorkspaceDescription) ((WorkspaceDescription) value).clone();
	Policy.setupAutoBuildProgress(description.isAutoBuilding());
	localMetaArea.write(description);
}
public void setTreeLocked(boolean locked) {
	treeLocked = locked;
}
public void setWorkspaceLock(WorkspaceLock lock) {
	workManager.setWorkspaceLock(lock);
}

private boolean shouldBuild() throws CoreException {
	return getWorkManager().shouldBuild() && ElementTree.hasChanges(tree, operationTree, true);
}
protected void shutdown(IProgressMonitor monitor) throws CoreException {
	monitor = Policy.monitorFor(monitor);
	try {
		IManager[] managers = { buildManager, notificationManager, propertyManager, fileSystemManager, markerManager, saveManager, workManager };
		monitor.beginTask(null, managers.length);
		String message = Policy.bind("resources.shutdownProblems");
		MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
		// best effort to shutdown every object and free resources
		for (int i = 0; i < managers.length; i++) {
			IManager manager = managers[i];
			if (manager == null)
				monitor.worked(1);
			else {
				try {
					manager.shutdown(Policy.subMonitorFor(monitor, 1));
				} catch (Exception e) {
					message = Policy.bind("resources.shutdownProblems");
					status.add(new Status(Status.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e));
				}
			}
		}
		buildManager = null;
		notificationManager = null;
		propertyManager = null;
		fileSystemManager = null;
		markerManager = null;
		synchronizer = null;
		saveManager = null;
		workManager = null;
		if (!status.isOK())
			throw new CoreException(status);
	} finally {
		monitor.done();
	}
}
/**
 * @see IWorkspace#sortNatureSet(String[])
 */
public String[] sortNatureSet(String[] natureIds) {
	return natureManager.sortNatureSet(natureIds);
}
protected void startup(IProgressMonitor monitor) throws CoreException {
	// ensure the tree is locked during the startup notification
	workManager = new WorkManager(this);
	workManager.startup(null);
	fileSystemManager = new FileSystemResourceManager(this);
	fileSystemManager.startup(monitor);
	propertyManager = new PropertyManager(this);
	propertyManager.startup(monitor);
	natureManager = new NatureManager();
	buildManager = new BuildManager(this);
	buildManager.startup(null);
	notificationManager = new NotificationManager(this);
	notificationManager.startup(null);
	markerManager = new MarkerManager(this);
	markerManager.startup(null);
	synchronizer = new Synchronizer(this);
	saveManager = new SaveManager(this);
	saveManager.startup(null);
	treeLocked = false; // unlock the tree.
}
/** 
 * Returns a string representation of this working state's
 * structure suitable for debug purposes.
 */
public String toDebugString() {
	final StringBuffer buffer = new StringBuffer("\nDump of " + toString() + ":\n");
	buffer.append("  parent: " + tree.getParent());
	ElementTreeIterator iterator = new ElementTreeIterator();
	IElementContentVisitor visitor = new IElementContentVisitor() {
		public void visitElement(ElementTree tree, IPath path, Object elementContents) {
			buffer.append("\n  " + path + ": " + elementContents);
		}
	};
	iterator.iterate(tree, visitor);
	return buffer.toString();
}
public void updateModificationStamp(ResourceInfo info) {
	info.setModificationStamp(nextModificationStamp());
}
/* (non-javadoc)
 * Method declared on IWorkspace.
 */
public IStatus validateEdit(final IFile[] files, final Object context) {
	// if validation is turned off then just return
	if (!shouldValidate)
		return ResourceStatus.OK_STATUS;
	// first time through the validator hasn't been initialized so try and create it
	if (validator == null) 
		initializeValidator();
	// we were unable to initialize the validator. Validation has been turned off and 
	// a warning has already been logged so just return.
	if (validator == null)
		return ResourceStatus.OK_STATUS;
	// otherwise call the API and throw an exception if appropriate
	final IStatus[] status = new IStatus[1];
	ISafeRunnable body = new ISafeRunnable() {
		public void run() throws Exception {
			status[0] = validator.validateEdit(files, context);
		}
		public void handleException(Throwable exception) {
			status[0]  = new ResourceStatus(IResourceStatus.ERROR, null, Policy.bind("resources.errorValidator"), exception);
		}
	};
	Platform.run(body);
	return status[0];
}
/**
 * @see IWorkspace#validateName
 */
public IStatus validateName(String segment, int type) {
	String message;

	/* segment must not be null */
	if (segment == null) {
		message = Policy.bind("resources.nameNull");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	// cannot be an empty string
	if (segment.length() == 0) {
		message = Policy.bind("resources.nameEmpty");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* segment must not begin or end with a whitespace */
	if (Character.isWhitespace(segment.charAt(0)) || Character.isWhitespace(segment.charAt(segment.length() - 1))) {
		message = Policy.bind("resources.invalidWhitespace");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* segment must not end with a dot */
	if (segment.endsWith(".")) {
		message = Policy.bind("resources.invalidDot", segment);
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* test invalid characters */
	char[] chars = OS.INVALID_RESOURCE_CHARACTERS;
	for (int i = 0; i < chars.length; i++)
		if (segment.indexOf(chars[i]) != -1) {
			message = Policy.bind("resources.invalidChar", String.valueOf(chars[i]));
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

	/* test invalid OS names */
	if (!OS.isNameValid(segment)) {
		message = Policy.bind("resources.invalidName", segment);
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}
	return ResourceStatus.OK_STATUS;
}
/**
 * @see IWorkspace#validateNatureSet(String[])
 */
public IStatus validateNatureSet(String[] natureIds) {
	return natureManager.validateNatureSet(natureIds);
}
/**
 * @see IWorkspace#validatePath
 */
public IStatus validatePath(String path, int type) {
	String message;

	/* path must not be null */
	if (path == null) {
		message = Policy.bind("resources.pathNull");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* path must not have a device separator */
	if (path.indexOf(IPath.DEVICE_SEPARATOR) != -1) {
		message = Policy.bind("resources.invalidChar", String.valueOf(IPath.DEVICE_SEPARATOR));
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* path must not be the root path */
	IPath testPath = new Path(path);
	if (testPath.isRoot()) {
		message = Policy.bind("resources.invalidRoot");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* path must be absolute */
	if (!testPath.isAbsolute()) {
		message = Policy.bind("resources.mustBeAbsolute");
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* validate segments */
	int numberOfSegments = testPath.segmentCount();
	if ((type & IResource.PROJECT) != 0) {
		if (numberOfSegments == ICoreConstants.PROJECT_SEGMENT_LENGTH) {
			return validateName(testPath.segment(0), IResource.PROJECT);
		} else
			if (type == IResource.PROJECT) {
				message = Policy.bind("resources.projectPath");
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
	}
	if ((type & (IResource.FILE | IResource.FOLDER)) != 0)
		if (numberOfSegments >= ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
			IStatus status = validateName(testPath.segment(0), IResource.PROJECT);
			if (!status.isOK())
				return status;
			int fileFolderType = type &= ~IResource.PROJECT;
			int segmentCount = testPath.segmentCount();
			// ignore first segment (the project)
			for (int i = 1; i < segmentCount; i++) {
				status = validateName(testPath.segment(i), fileFolderType);
				if (!status.isOK())
					return status;
			}
			return ResourceStatus.OK_STATUS;
		} else {
			message = Policy.bind("resources.resourcePath");
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
	message = Policy.bind("resources.invalidPath", path);
	return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
}
/**
 * @see IWorkspace#validateProjectLocation
 */
public IStatus validateProjectLocation(IProject context, IPath location) {
	String message;
	// the default default is ok for all projects
	if (location == null) {
		message = Policy.bind("resources.validLocation");
		return new ResourceStatus(IResourceStatus.OK, message);
	}
	//check the standard path name restrictions
	int segmentCount = location.segmentCount();
	for (int i = 0; i < segmentCount; i++) {
		IStatus result = validateName(location.segment(i), IResource.PROJECT);
		if (!result.isOK())
			return result;
	}
	//if the location doesn't have a device, see if the OS will assign one
	if (location.getDevice() == null)
		location = new Path(location.toFile().getAbsolutePath());
	// test if the given location overlaps the default default location
	IPath defaultDefaultLocation = Platform.getLocation();
	if (defaultDefaultLocation.isPrefixOf(location) || location.isPrefixOf(defaultDefaultLocation)) {
		message = Policy.bind("resources.overlapLocal", location.toString(), defaultDefaultLocation.toString());
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}
	// Iterate over each known project and ensure that the location does not
	// conflict with any of their already defined locations.
	IProject[] projects = getRoot().getProjects();
	for (int j = 0; j < projects.length; j++) {
		IProject project = (IProject) projects[j];
		// skip over the project that we are validating for 
		if (project.equals(context))
			continue;
		// since we are iterating over the project in the workspace, we
		// know that they have been created before and must have a description
		IProjectDescription desc  = ((Project) project).internalGetDescription();
		IPath definedLocalLocation = desc.getLocation();
		// if the project uses the default location then continue
		if (definedLocalLocation == null)
			continue;
		if (definedLocalLocation != null && location != null)
			if (location.isPrefixOf(definedLocalLocation) || definedLocalLocation.isPrefixOf(location)) {
				message = Policy.bind("resources.overlapLocal", location.toString(), definedLocalLocation.toString());
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
	}
	message = Policy.bind("resources.validLocation");
	return new ResourceStatus(IResourceStatus.OK, message);
}
/**
 * Internal method. To be called only from the following methods:
 * <ul>
 * <li><code>IFile#appendContents</code></li>
 * <li><code>IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)</code></li>
 * <li><code>IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)</code></li>
 * </ul>
 * 
 * @see IFileModificationValidator#validateSave
 */
protected void validateSave(final IFile file) throws CoreException {
	// if validation is turned off then just return
	if (!shouldValidate)
		return;
	// first time through the validator hasn't been initialized so try and create it
	if (validator == null) 
		initializeValidator();
	// we were unable to initialize the validator. Validation has been turned off and 
	// a warning has already been logged so just return.
	if (validator == null)
		return;
	// otherwise call the API and throw an exception if appropriate
	final IStatus[] status = new IStatus[1];
	ISafeRunnable body = new ISafeRunnable() {
		public void run() throws Exception {
			status[0] = validator.validateSave(file);
		}
		public void handleException(Throwable exception) {
			status[0]  = new ResourceStatus(IResourceStatus.ERROR, null, Policy.bind("resources.errorValidator"), exception);
		}
	};
	Platform.run(body);
	if (!status[0].isOK())
		throw new ResourceException(status[0]);
}

}
