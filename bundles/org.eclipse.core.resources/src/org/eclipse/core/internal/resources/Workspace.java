/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.IOException;
import java.util.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.CoreFileSystemLibrary;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.properties.PropertyManager;
import org.eclipse.core.internal.refresh.RefreshManager;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.TeamHook;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class Workspace extends PlatformObject implements IWorkspace, ICoreConstants {
	// whether the resources plugin is in debug mode.
	public static boolean DEBUG = false;
	protected static final String REFRESH_ON_STARTUP = "-refresh"; //$NON-NLS-1$
	protected WorkspacePreferences description;
	protected LocalMetaArea localMetaArea;
	protected boolean openFlag = false;
	protected ElementTree tree;
	protected ElementTree operationTree; // tree at the start of the current operation
	protected SaveManager saveManager;
	protected BuildManager buildManager;
	protected NatureManager natureManager;
	protected NotificationManager notificationManager;
	protected FileSystemResourceManager fileSystemManager;
	protected PathVariableManager pathVariableManager;
	protected PropertyManager propertyManager;
	protected MarkerManager markerManager;
	protected ContentDescriptionManager contentDescriptionManager;
	/**
	 * Work manager should never be accessed directly because accessor
	 * asserts that workspace is still open.
	 */
	protected WorkManager _workManager;
	protected AliasManager aliasManager;
	protected CharsetManager charsetManager;
	protected RefreshManager refreshManager;

	protected long nextNodeId = 1;
	protected long nextModificationStamp = 0;
	protected long nextMarkerId = 0;
	protected Synchronizer synchronizer;
	protected IProject[] buildOrder = null;
	protected IWorkspaceRoot defaultRoot = new WorkspaceRoot(Path.ROOT, this);

	protected final HashSet lifecycleListeners = new HashSet(10);

	/**
	 * Scheduling rule factory
	 */
	private IResourceRuleFactory ruleFactory;

	/**
	 * File modification validation.  If it is true and validator is null, we try/initialize 
	 * validator first time through.  If false, there is no validator.
	 */
	protected boolean shouldValidate = true;
	/**
	 * The currently installed file modification validator.
	 */
	protected IFileModificationValidator validator = null;

	/**
	 * The currently installed Move/Delete hook.
	 */
	protected IMoveDeleteHook moveDeleteHook = null;

	/**
	 * The currently installed team hook.
	 */
	protected TeamHook teamHook = null;

	/**
	 * This field is used to control access to the workspace tree during
	 * resource change notifications. It tracks which thread, if any, is
	 * in the middle of a resource change notification.  This is used to cause
	 * attempts to modify the workspace during notifications to fail.
	 */
	protected Thread treeLocked = null;

	/** indicates if the workspace crashed in a previous session */
	protected boolean crashed = false;

	public Workspace() {
		super();
		localMetaArea = new LocalMetaArea();
		tree = new ElementTree();
		/* tree should only be modified during operations */
		tree.immutable();
		treeLocked = Thread.currentThread();
		tree.setTreeData(newElement(IResource.ROOT));
	}

	/**
	 * Indicates that a build is about to occur. Broadcasts the necessary
	 * deltas before the build starts. Note that this will cause POST_BUILD
	 * to be automatically done at the end of the operation in which
	 * the build occurs.
	 */
	protected void aboutToBuild() throws CoreException {
		//fire a POST_CHANGE first to ensure everyone is up to date
		//before firing PRE_BUILD
		broadcastChanges(IResourceChangeEvent.POST_CHANGE, true);
		broadcastChanges(IResourceChangeEvent.PRE_BUILD, false);
	}

	/**
	 * Adds a listener for internal workspace lifecycle events.  There is no way to
	 * remove lifecycle listeners.
	 */
	public void addLifecycleListener(ILifecycleListener listener) {
		lifecycleListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#addResourceChangeListener(IResourceChangeListener)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener) {
		notificationManager.addListener(listener, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#addResourceChangeListener(IResourceChangeListener, int)
	 */
	public void addResourceChangeListener(IResourceChangeListener listener, int eventMask) {
		notificationManager.addListener(listener, eventMask);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#addSaveParticipant(Plugin, ISaveParticipant)
	 */
	public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException {
		Assert.isNotNull(plugin, "Plugin must not be null"); //$NON-NLS-1$
		Assert.isNotNull(participant, "Participant must not be null"); //$NON-NLS-1$
		return saveManager.addParticipant(plugin, participant);
	}

	public void beginOperation(boolean createNewTree) throws CoreException {
		WorkManager workManager = getWorkManager();
		workManager.incrementNestedOperations();
		if (!workManager.isBalanced())
			Assert.isTrue(false, "Operation was not prepared."); //$NON-NLS-1$
		if (workManager.getPreparedOperationDepth() > 1) {
			if (createNewTree && tree.isImmutable())
				newWorkingTree();
			return;
		}
		// stash the current tree as the basis for this operation.
		operationTree = tree;
		if (createNewTree && tree.isImmutable())
			newWorkingTree();
	}

	protected void broadcastChanges(int type, boolean lockTree) throws CoreException {
		notificationManager.broadcastChanges(tree, type, lockTree);
	}

	/**
	 * Broadcasts an internal workspace lifecycle event to interested
	 * internal listeners.
	 */
	protected void broadcastEvent(LifecycleEvent event) throws CoreException {
		for (Iterator it = lifecycleListeners.iterator(); it.hasNext();) {
			ILifecycleListener listener = (ILifecycleListener) it.next();
			listener.handleEvent(event);
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#build(int, IProgressMonitor)
	 */
	public void build(int trigger, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final ISchedulingRule rule = getRuleFactory().buildRule();
		try {
			monitor.beginTask(null, Policy.opWork);
			try {
				prepareOperation(rule, monitor);
				beginOperation(true);
				aboutToBuild();
				getBuildManager().build(trigger, Policy.subMonitorFor(monitor, Policy.opWork));
				broadcastChanges(IResourceChangeEvent.POST_BUILD, false);
			} finally {
				//building may close the tree, but we are still inside an operation so open it
				if (tree.isImmutable())
					newWorkingTree();
				endOperation(rule, false, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#checkpoint(boolean)
	 */
	public void checkpoint(boolean build) {
		try {
			final ISchedulingRule rule = getWorkManager().getNotifyRule();
			try {
				prepareOperation(rule, null);
				beginOperation(true);
				broadcastChanges(IResourceChangeEvent.POST_CHANGE, true);
			} finally {
				endOperation(rule, build, null);
			}
		} catch (CoreException e) {
			ResourcesPlugin.getPlugin().getLog().log(e.getStatus());
		}
	}

	/**
	 * Deletes all the files and directories from the given root down (inclusive).
	 * Returns false if we could not delete some file or an exception occurred
	 * at any point in the deletion.
	 * Even if an exception occurs, a best effort is made to continue deleting.
	 */
	public static boolean clear(java.io.File root) {
		boolean result = clearChildren(root);
		try {
			if (root.exists())
				result &= root.delete();
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * Deletes all the files and directories from the given root down, except for 
	 * the root itself.
	 * Returns false if we could not delete some file or an exception occurred
	 * at any point in the deletion.
	 * Even if an exception occurs, a best effort is made to continue deleting.
	 */
	public static boolean clearChildren(java.io.File root) {
		boolean result = true;
		if (root.isDirectory()) {
			String[] list = root.list();
			// for some unknown reason, list() can return null.  
			// Just skip the children If it does.
			if (list != null)
				for (int i = 0; i < list.length; i++)
					result &= clear(new java.io.File(root, list[i]));
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
		//nothing to do if the workspace failed to open
		if (!isOpen())
			return;
		monitor = Policy.monitorFor(monitor);
		try {
			String msg = Policy.bind("resources.closing.0"); //$NON-NLS-1$
			int rootCount = tree.getChildCount(Path.ROOT);
			monitor.beginTask(msg, rootCount + 2);
			monitor.subTask(msg);
			//this operation will never end because the world is going away
			try {
				//shutdown save manager now so a last snapshot can be taken before we close
				//note: you can't call #save() from within a nested operation
				saveManager.shutdown(null);
				prepareOperation(getRoot(), monitor);
				//shutdown notification first to avoid calling third parties during shutdown
				notificationManager.shutdown(null);
				beginOperation(true);
				IProject[] projects = getRoot().getProjects();
				for (int i = 0; i < projects.length; i++) {
					//notify managers of closing so they can cleanup
					broadcastEvent(LifecycleEvent.newEvent(LifecycleEvent.PRE_PROJECT_CLOSE, projects[i]));
					monitor.worked(1);
				}
				//empty the workspace tree so we leave in a clean state
				deleteResource(getRoot());
				openFlag = false;
				// endOperation not needed here
			} finally {
				// Shutdown needs to be executed regardless of failures
				shutdown(Policy.subMonitorFor(monitor, 2, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * Implementation of API method declared on IWorkspace.
	 * 
	 * @see IWorkspace#computePrerequisiteOrder(IProject[])
	 * @deprecated Replaced by <code>IWorkspace.computeProjectOrder</code>, which
	 * produces a more usable result when there are cycles in project reference
	 * graph.
	 */
	public IProject[][] computePrerequisiteOrder(IProject[] targets) {
		return computePrerequisiteOrder1(targets);
	}

	/*
	 * Compatible reimplementation of 
	 * <code>IWorkspace.computePrerequisiteOrder</code> using 
	 * <code>IWorkspace.computeProjectOrder</code>.
	 * 
	 * @since 2.1
	 */
	private IProject[][] computePrerequisiteOrder1(IProject[] projects) {
		IWorkspace.ProjectOrder r = computeProjectOrder(projects);
		if (!r.hasCycles) {
			return new IProject[][] {r.projects, new IProject[0]};
		}
		// when there are cycles, we need to remove all knotted projects from
		// r.projects to form result[0] and merge all knots to form result[1]
		// Set<IProject> bad
		Set bad = new HashSet();
		// Set<IProject> bad
		Set keepers = new HashSet(Arrays.asList(r.projects));
		for (int i = 0; i < r.knots.length; i++) {
			IProject[] knot = r.knots[i];
			for (int j = 0; j < knot.length; j++) {
				IProject project = knot[j];
				// keep only selected projects in knot
				if (keepers.contains(project)) {
					bad.add(project);
				}
			}
		}
		IProject[] result2 = new IProject[bad.size()];
		bad.toArray(result2);
		// List<IProject> p
		List p = new LinkedList();
		p.addAll(Arrays.asList(r.projects));
		for (Iterator it = p.listIterator(); it.hasNext();) {
			IProject project = (IProject) it.next();
			if (bad.contains(project)) {
				// remove knotted projects from the main answer
				it.remove();
			}
		}
		IProject[] result1 = new IProject[p.size()];
		p.toArray(result1);
		return new IProject[][] {result1, result2};
	}

	/* (non-Javadoc)
	 * @see IWorkspace#computeProjectOrder(IProject[])
	 * @since 2.1
	 */
	public ProjectOrder computeProjectOrder(IProject[] projects) {

		// compute the full project order for all accessible projects
		ProjectOrder fullProjectOrder = computeFullProjectOrder();

		// "fullProjectOrder.projects" contains no inaccessible projects
		// but might contain accessible projects omitted from "projects"
		// optimize common case where "projects" includes everything
		int accessibleCount = 0;
		for (int i = 0; i < projects.length; i++) {
			if (projects[i].isAccessible()) {
				accessibleCount++;
			}
		}
		// no filtering required if the subset accounts for the full list
		if (accessibleCount == fullProjectOrder.projects.length) {
			return fullProjectOrder;
		}

		// otherwise we need to eliminate mention of other projects...
		// ... from "fullProjectOrder.projects"...		
		// Set<IProject> keepers
		Set keepers = new HashSet(Arrays.asList(projects));
		// List<IProject> p
		List reducedProjects = new ArrayList(fullProjectOrder.projects.length);
		for (int i = 0; i < fullProjectOrder.projects.length; i++) {
			IProject project = fullProjectOrder.projects[i];
			if (keepers.contains(project)) {
				// remove projects not in the initial subset
				reducedProjects.add(project);
			}
		}
		IProject[] p1 = new IProject[reducedProjects.size()];
		reducedProjects.toArray(p1);

		// ... and from "fullProjectOrder.knots"		
		// List<IProject[]> k
		List reducedKnots = new ArrayList(fullProjectOrder.knots.length);
		for (int i = 0; i < fullProjectOrder.knots.length; i++) {
			IProject[] knot = fullProjectOrder.knots[i];
			List x = new ArrayList(knot.length);
			for (int j = 0; j < knot.length; j++) {
				IProject project = knot[j];
				if (keepers.contains(project)) {
					x.add(project);
				}
			}
			// keep knots containing 2 or more projects in the specified subset
			if (x.size() > 1) {
				reducedKnots.add(x.toArray(new IProject[x.size()]));
			}
		}
		IProject[][] k1 = new IProject[reducedKnots.size()][];
		// okay to use toArray here because reducedKnots elements are IProject[]
		reducedKnots.toArray(k1);
		return new ProjectOrder(p1, (k1.length > 0), k1);
	}

	/**
	 * Computes the global total ordering of all open projects in the
	 * workspace based on project references. If an existing and open project P
	 * references another existing and open project Q also included in the list,
	 * then Q should come before P in the resulting ordering. Closed and non-
	 * existent projects are ignored, and will not appear in the result. References
	 * to non-existent or closed projects are also ignored, as are any self-
	 * references.
	 * <p>
	 * When there are choices, the choice is made in a reasonably stable way. For
	 * example, given an arbitrary choice between two projects, the one with the
	 * lower collating project name is usually selected.
	 * </p>
	 * <p>
	 * When the project reference graph contains cyclic references, it is
	 * impossible to honor all of the relationships. In this case, the result
	 * ignores as few relationships as possible.  For example, if P2 references P1,
	 * P4 references P3, and P2 and P3 reference each other, then exactly one of the
	 * relationships between P2 and P3 will have to be ignored. The outcome will be
	 * either [P1, P2, P3, P4] or [P1, P3, P2, P4]. The result also contains
	 * complete details of any cycles present.
	 * </p>
	 *
	 * @return result describing the global project order
	 * @since 2.1
	 */
	private ProjectOrder computeFullProjectOrder() {

		// determine the full set of accessible projects in the workspace
		// order the set in descending alphabetical order of project name
		SortedSet allAccessibleProjects = new TreeSet(new Comparator() {
			public int compare(Object x, Object y) {
				IProject px = (IProject) x;
				IProject py = (IProject) y;
				return py.getName().compareTo(px.getName());
			}
		});
		IProject[] allProjects = getRoot().getProjects();
		// List<IProject[]> edges
		List edges = new ArrayList(allProjects.length);
		for (int i = 0; i < allProjects.length; i++) {
			Project project = (Project) allProjects[i];
			// ignore projects that are not accessible
			if (!project.isAccessible())
				continue;
			ProjectDescription description = project.internalGetDescription();
			if (description == null)
				continue;
			//obtain both static and dynamic project references
			IProject[] refs = description.getAllReferences(false);
			allAccessibleProjects.add(project);
			for (int j = 0; j < refs.length; j++) {
				IProject ref = refs[j];
				// ignore self references and references to projects that are not accessible
				if (ref.isAccessible() && !ref.equals(project))
					edges.add(new IProject[] {project, ref});
			}
		}

		ProjectOrder fullProjectOrder = ComputeProjectOrder.computeProjectOrder(allAccessibleProjects, edges);
		return fullProjectOrder;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#copy(IResource[], IPath, int, IProgressMonitor)
	 */
	public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Policy.bind("resources.copying.0"); //$NON-NLS-1$
			monitor.beginTask(message, totalWork);
			Assert.isLegal(resources != null);
			if (resources.length == 0)
				return Status.OK_STATUS;
			// to avoid concurrent changes to this array
			resources = (IResource[]) resources.clone();
			IPath parentPath = null;
			message = Policy.bind("resources.copyProblem"); //$NON-NLS-1$
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
			try {
				prepareOperation(getRoot(), monitor);
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
							IPath destinationPath = destination.append(resource.getName());
							IStatus requirements = ((Resource) resource).checkCopyRequirements(destinationPath, resource.getType(), updateFlags);
							if (requirements.isOK()) {
								try {
									resource.copy(destinationPath, updateFlags, Policy.subMonitorFor(monitor, 1));
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
						message = Policy.bind("resources.notChild", resources[i].getFullPath().toString(), parentPath.toString()); //$NON-NLS-1$
						status.merge(new ResourceStatus(IResourceStatus.OPERATION_FAILED, resources[i].getFullPath(), message));
					}
				}
			} catch (OperationCanceledException e) {
				getWorkManager().operationCanceled();
				throw e;
			} finally {
				endOperation(getRoot(), true, Policy.subMonitorFor(monitor, totalWork - opWork));
			}
			if (status.matches(IStatus.ERROR))
				throw new ResourceException(status);
			return status.isOK() ? Status.OK_STATUS : (IStatus) status;
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#copy(IResource[], IPath, boolean, IProgressMonitor)
	 */
	public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		return copy(resources, destination, updateFlags, monitor);
	}

	protected void copyTree(IResource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo) throws CoreException {
		// retrieve the resource at the destination if there is one (phantoms included).
		// if there isn't one, then create a new handle based on the type that we are
		// trying to copy
		IResource destinationResource = getRoot().findMember(destination, true);
		if (destinationResource == null) {
			int destinationType;
			if (source.getType() == IResource.FILE)
				destinationType = IResource.FILE;
			else if (destination.segmentCount() == 1)
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
		ResourceInfo newInfo = createResource(destinationResource, sourceInfo, false, false, keepSyncInfo);
		// get/set the node id from the source's resource info so we can later put it in the
		// info for the destination resource. This will help us generate the proper deltas,
		// indicating a move rather than a add/delete
		long nodeid = ((Resource) source).getResourceInfo(true, false).getNodeId();
		newInfo.setNodeId(nodeid);

		// preserve local sync info
		ResourceInfo oldInfo = ((Resource) source).getResourceInfo(true, false);
		newInfo.setFlags(newInfo.getFlags() | (oldInfo.getFlags() & M_LOCAL_EXISTS));

		// forget content-related caching flags
		newInfo.clear(M_CONTENT_CACHE);

		// update link locations in project descriptions
		if (source.isLinked()) {
			LinkDescription linkDescription;
			if ((updateFlags & IResource.SHALLOW) != 0) {
				//for shallow move the destination is also a linked resource
				newInfo.set(ICoreConstants.M_LINK);
				linkDescription = new LinkDescription(destinationResource, source.getRawLocation());
			} else {
				//for deep move the destination is not a linked resource
				newInfo.clear(ICoreConstants.M_LINK);
				linkDescription = null;
			}
			Project project = (Project) destinationResource.getProject();
			project.internalGetDescription().setLinkLocation(destinationResource.getName(), linkDescription);
			project.writeDescription(updateFlags);
		}

		// do the recursion. if we have a file then it has no members so return. otherwise
		// recursively call this method on the container's members if the depth tells us to
		if (depth == IResource.DEPTH_ZERO || source.getType() == IResource.FILE)
			return;
		if (depth == IResource.DEPTH_ONE)
			depth = IResource.DEPTH_ZERO;
		IResource[] children = ((IContainer) source).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		for (int i = 0; i < children.length; i++) {
			IResource child = children[i];
			IPath childPath = destination.append(child.getName());
			copyTree(child, childPath, depth, updateFlags, keepSyncInfo);
		}
	}

	/**
	 * Returns the number of resources in a subtree of the resource tree.
	 * 
	 * @param root The subtree to count resources for
	 * @param depth The depth of the subtree to count
	 * @param phantom If true, phantoms are included, otherwise they are ignored.
	 */
	public int countResources(IPath root, int depth, final boolean phantom) {
		if (!tree.includes(root))
			return 0;
		switch (depth) {
			case IResource.DEPTH_ZERO :
				return 1;
			case IResource.DEPTH_ONE :
				return 1 + tree.getChildCount(root);
			case IResource.DEPTH_INFINITE :
				final int[] count = new int[1];
				IElementContentVisitor visitor = new IElementContentVisitor() {
					public boolean visitElement(ElementTree aTree, IPathRequestor requestor, Object elementContents) {
						if (phantom || !((ResourceInfo) elementContents).isSet(M_PHANTOM))
							count[0]++;
						return true;
					}
				};
				new ElementTreeIterator(tree, root).iterate(visitor);
				return count[0];
		}
		return 0;
	}

	public ResourceInfo createResource(IResource resource, ResourceInfo info, boolean phantom, boolean overwrite) throws CoreException {
		return createResource(resource, info, phantom, overwrite, false);
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
	 * 
	 * If keepSyncInfo is set to be true, the sync info in the given ResourceInfo is NOT
	 * cleared before being created and thus any sync info already existing at that namespace
	 * (as indicated by an already existing phantom resource) will be lost.
	 */
	public ResourceInfo createResource(IResource resource, ResourceInfo info, boolean phantom, boolean overwrite, boolean keepSyncInfo) throws CoreException {
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
			if (!keepSyncInfo)
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
				if (!keepSyncInfo)
					info.setSyncInfo(original.getSyncInfo(true));
				// mark the markers bit as dirty so we snapshot an empty marker set for
				// the new resource
				info.set(ICoreConstants.M_MARKERS_SNAP_DIRTY);
				tree.setElementData(resource.getFullPath(), info);
			} else {
				String message = Policy.bind("resources.mustNotExist", resource.getFullPath().toString()); //$NON-NLS-1$
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
		return new WorkspaceDescription("Workspace"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see IWorkspace#delete(IResource[], int, IProgressMonitor)
	 */
	public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Policy.bind("resources.deleting.0"); //$NON-NLS-1$
			monitor.beginTask(message, totalWork);
			message = Policy.bind("resources.deleteProblem"); //$NON-NLS-1$
			MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
			if (resources.length == 0)
				return result;
			resources = (IResource[]) resources.clone(); // to avoid concurrent changes to this array
			try {
				prepareOperation(getRoot(), monitor);
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
							message = Policy.bind("resources.couldnotDelete", resource.getFullPath().toString()); //$NON-NLS-1$
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
				endOperation(getRoot(), true, Policy.subMonitorFor(monitor, totalWork - opWork));
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#delete(IResource[], boolean, IProgressMonitor)
	 */
	public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= IResource.KEEP_HISTORY;
		return delete(resources, updateFlags, monitor);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#deleteMarkers(IMarker[])
	 */
	public void deleteMarkers(IMarker[] markers) throws CoreException {
		Assert.isNotNull(markers);
		if (markers.length == 0)
			return;
		// clone to avoid outside changes
		markers = (IMarker[]) markers.clone();
		try {
			prepareOperation(null, null);
			beginOperation(true);
			for (int i = 0; i < markers.length; ++i)
				if (markers[i] != null && markers[i].getResource() != null)
					markerManager.removeMarker(markers[i].getResource(), markers[i].getId());
		} finally {
			endOperation(null, false, null);
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
	 * For debugging purposes only.  Dumps plugin stats to console
	 */
	public void dumpStats() {
		EventStats.dumpStats();
	}

	/**
	 * End an operation (group of resource changes).
	 * Notify interested parties that resource changes have taken place.  All
	 * registered resource change listeners are notified.  If autobuilding is
	 * enabled, a build is run.
	 */
	public void endOperation(ISchedulingRule rule, boolean build, IProgressMonitor monitor) throws CoreException {
		WorkManager workManager = getWorkManager();
		//don't do any end operation work if we failed to check in
		if (workManager.checkInFailed(rule))
			return;
		// This is done in a try finally to ensure that we always decrement the operation count
		// and release the workspace lock.  This must be done at the end because snapshot
		// and "hasChanges" comparison have to happen without interference from other threads.
		boolean hasTreeChanges = false;
		boolean depthOne = false;
		try {
			workManager.setBuild(build);
			// if we are not exiting a top level operation then just decrement the count and return
			depthOne = workManager.getPreparedOperationDepth() == 1;
			if (!(notificationManager.shouldNotify() || depthOne)) {
				notificationManager.requestNotify();
				return;
			}
			// do the following in a try/finally to ensure that the operation tree is nulled at the end
			// as we are completing a top level operation.
			try {
				notificationManager.beginNotify();
				// check for a programming error on using beginOperation/endOperation
				Assert.isTrue(workManager.getPreparedOperationDepth() > 0, "Mismatched begin/endOperation"); //$NON-NLS-1$

				// At this time we need to rebalance the nested operations. It is necessary because
				// build() and snapshot() should not fail if they are called.
				workManager.rebalanceNestedOperations();

				//find out if any operation has potentially modified the tree
				hasTreeChanges = workManager.shouldBuild();
				//double check if the tree has actually changed
				if (hasTreeChanges)
					hasTreeChanges = operationTree != null && ElementTree.hasChanges(tree, operationTree, ResourceComparator.getComparator(false), true);
				broadcastChanges(IResourceChangeEvent.POST_CHANGE, true);
				// Request a snapshot if we are sufficiently out of date.
				saveManager.snapshotIfNeeded(hasTreeChanges);
			} finally {
				// make sure the tree is immutable if we are ending a top-level operation.
				if (depthOne) {
					tree.immutable();
					operationTree = null;
				} else
					newWorkingTree();
			}
		} finally {
			workManager.checkOut(rule);
		}
		if (depthOne)
			buildManager.endTopLevel(hasTreeChanges);
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

	/* (non-Javadoc)
	 * @see IWorkspace#forgetSavedTree(String)
	 */
	public void forgetSavedTree(String pluginId) {
		Assert.isNotNull(pluginId, "PluginId must not be null"); //$NON-NLS-1$
		saveManager.forgetSavedTree(pluginId);
	}

	public AliasManager getAliasManager() {
		return aliasManager;
	}

	/**
	 * Returns this workspace's build manager
	 */
	public BuildManager getBuildManager() {
		return buildManager;
	}

	/**
	 * Returns the order in which open projects in this workspace will be built.
	 * <p>
	 * The project build order is based on information specified in the workspace
	 * description. The projects are built in the order specified by
	 * <code>IWorkspaceDescription.getBuildOrder</code>; closed or non-existent
	 * projects are ignored and not included in the result. If
	 * <code>IWorkspaceDescription.getBuildOrder</code> is non-null, the default
	 * build order is used; again, only open projects are included in the result.
	 * </p>
	 * <p>
	 * The returned value is cached in the <code>buildOrder</code> field.
	 * </p>
	 * 
	 * @return the list of currently open projects in the workspace in the order in
	 * which they would be built by <code>IWorkspace.build</code>.
	 * @see IWorkspace#build(int, IProgressMonitor)
	 * @see IWorkspaceDescription#getBuildOrder()
	 * @since 2.1
	 */
	public IProject[] getBuildOrder() {
		if (buildOrder != null) {
			// return previously-computed and cached project build order
			return buildOrder;
		}
		// see if a particular build order is specified
		String[] order = description.getBuildOrder(false);
		if (order != null) {
			// convert from project names to project handles
			// and eliminate non-existent and closed projects
			List projectList = new ArrayList(order.length);
			for (int i = 0; i < order.length; i++) {
				IProject project = getRoot().getProject(order[i]);
				if (project.isAccessible()) {
					projectList.add(project);
				}
			}
			buildOrder = new IProject[projectList.size()];
			projectList.toArray(buildOrder);
		} else {
			// use default project build order
			// computed for all accessible projects in workspace
			buildOrder = computeFullProjectOrder().projects;
		}
		return buildOrder;
	}

	public CharsetManager getCharsetManager() {
		return charsetManager;
	}

	public ContentDescriptionManager getContentDescriptionManager() {
		return contentDescriptionManager;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getDanglingReferences()
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

	/* (non-Javadoc)
	 * @see IWorkspace#getDescription()
	 */
	public IWorkspaceDescription getDescription() {
		WorkspaceDescription workingCopy = defaultWorkspaceDescription();
		description.copyTo(workingCopy);
		return workingCopy;
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

	protected IMoveDeleteHook getMoveDeleteHook() {
		if (moveDeleteHook == null)
			initializeMoveDeleteHook();
		return moveDeleteHook;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getNatureDescriptor(String)
	 */
	public IProjectNatureDescriptor getNatureDescriptor(String natureId) {
		return natureManager.getNatureDescriptor(natureId);
	}

	/* (non-Javadoc)
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

	/* (non-Javadoc)
	 * @see IWorkspace#getPathVariableManager()
	 */
	public IPathVariableManager getPathVariableManager() {
		return pathVariableManager;
	}

	public PropertyManager getPropertyManager() {
		return propertyManager;
	}

	/**
	 * Returns the refresh manager for this workspace
	 */
	public RefreshManager getRefreshManager() {
		return refreshManager;
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
				ResourceInfo info = (ResourceInfo) tree.getTreeData();
				Assert.isNotNull(info, "Tree root info must never be null"); //$NON-NLS-1$
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

	/* (non-Javadoc)
	 * @see IWorkspace#getRoot()
	 */
	public IWorkspaceRoot getRoot() {
		return defaultRoot;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getRuleFactory()
	 */
	public IResourceRuleFactory getRuleFactory() {
		//note that the rule factory is created lazily because it
		//requires loading the teamHook extension
		if (ruleFactory == null)
			ruleFactory = new Rules(this);
		return ruleFactory;
	}

	public SaveManager getSaveManager() {
		return saveManager;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#getSynchronizer()
	 */
	public ISynchronizer getSynchronizer() {
		return synchronizer;
	}

	/**
	 * Returns the installed team hook.  Never returns null.
	 */
	protected TeamHook getTeamHook() {
		if (teamHook == null)
			initializeTeamHook();
		return teamHook;
	}

	/**
	 * We should not have direct references to this field. All references should go through
	 * this method.
	 */
	public WorkManager getWorkManager() throws CoreException {
		if (_workManager == null) {
			String message = Policy.bind("resources.shutdown"); //$NON-NLS-1$
			throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message));
		}
		return _workManager;
	}

	/**
	 * A file modification validator hasn't been initialized. Check the extension point and 
	 * try to create a new validator if a user has one defined as an extension.
	 */
	protected void initializeValidator() {
		shouldValidate = false;
		IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILE_MODIFICATION_VALIDATOR);
		// no-one is plugged into the extension point so disable validation
		if (configs == null || configs.length == 0) {
			return;
		}
		// can only have one defined at a time. log a warning, disable validation, but continue with
		// the #setContents (e.g. don't throw an exception)
		if (configs.length > 1) {
			//XXX: shoud provide a meaningful status code
			IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.oneValidator"), null); //$NON-NLS-1$
			ResourcesPlugin.getPlugin().getLog().log(status);
			return;
		}
		// otherwise we have exactly one validator extension. Try to create a new instance 
		// from the user-specified class.
		try {
			IConfigurationElement config = configs[0];
			validator = (IFileModificationValidator) config.createExecutableExtension("class"); //$NON-NLS-1$
			shouldValidate = true;
		} catch (CoreException e) {
			//XXX: shoud provide a meaningful status code
			IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.initValidator"), e); //$NON-NLS-1$
			ResourcesPlugin.getPlugin().getLog().log(status);
		}
	}

	/**
	 * A move/delete hook hasn't been initialized. Check the extension point and 
	 * try to create a new hook if a user has one defined as an extension. Otherwise
	 * use the Core's implementation as the default.
	 */
	protected void initializeMoveDeleteHook() {
		try {
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MOVE_DELETE_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: shoud provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.oneHook"), null); //$NON-NLS-1$
				ResourcesPlugin.getPlugin().getLog().log(status);
				return;
			}
			// otherwise we have exactly one hook extension. Try to create a new instance 
			// from the user-specified class.
			try {
				IConfigurationElement config = configs[0];
				moveDeleteHook = (IMoveDeleteHook) config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				//XXX: shoud provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.initHook"), e); //$NON-NLS-1$
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
		} finally {
			// for now just use Core's implementation
			if (moveDeleteHook == null)
				moveDeleteHook = new MoveDeleteHook();
		}
	}

	/**
	 * A team hook hasn't been initialized. Check the extension point and 
	 * try to create a new hook if a user has one defined as an extension. 
	 * Otherwise use the Core's implementation as the default.
	 */
	protected void initializeTeamHook() {
		try {
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_TEAM_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: shoud provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.oneTeamHook"), null); //$NON-NLS-1$
				ResourcesPlugin.getPlugin().getLog().log(status);
				return;
			}
			// otherwise we have exactly one hook extension. Try to create a new instance 
			// from the user-specified class.
			try {
				IConfigurationElement config = configs[0];
				teamHook = (TeamHook) config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				//XXX: shoud provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Policy.bind("resources.initTeamHook"), e); //$NON-NLS-1$
				ResourcesPlugin.getPlugin().getLog().log(status);
			}
		} finally {
			// default to use Core's implementation
			//create anonymous subclass because TeamHook is abstract
			if (teamHook == null)
				teamHook = new TeamHook() {
					// empty
				};
		}
	}

	public WorkspaceDescription internalGetDescription() {
		return description;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#isAutoBuilding()
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

	public boolean isOpen() {
		return openFlag;
	}

	/**
	 * Returns true if the given file system locations overlap. If "bothDirections" is true,
	 * this means they are the same, or one is a proper prefix of the other.  If "bothDirections"
	 * is false, this method only returns true if the locations are the same, or the first location
	 * is a prefix of the second.  Returns false if the locations do not overlap
	 * Does the right thing with respect to case insensitive platforms.
	 */
	protected boolean isOverlapping(IPath location1, IPath location2, boolean bothDirections) {
		IPath one = location1;
		IPath two = location2;
		// If we are on a case-insensitive file system then convert to all lowercase.
		if (!CoreFileSystemLibrary.isCaseSensitive()) {
			one = new Path(location1.toOSString().toLowerCase());
			two = new Path(location2.toOSString().toLowerCase());
		}
		return one.isPrefixOf(two) || (bothDirections && two.isPrefixOf(one));
	}

	/* (non-Javadoc)
	 * @see IWorkspace#isTreeLocked()
	 */
	public boolean isTreeLocked() {
		return treeLocked == Thread.currentThread();
	}

	/**
	 * Link the given tree into the receiver's tree at the specified resource.
	 */
	protected void linkTrees(IPath path, ElementTree[] newTrees) {
		tree = tree.mergeDeltaChain(path, newTrees);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#loadProjectDescription(IPath)
	 * @since 2.0
	 */
	public IProjectDescription loadProjectDescription(IPath path) throws CoreException {
		IProjectDescription result = null;
		IOException e = null;
		try {
			result = new ProjectDescriptionReader().read(path);
			if (result != null) {
				// check to see if we are using in the default area or not. use java.io.File for
				// testing equality because it knows better w.r.t. drives and case sensitivity
				IPath user = path.removeLastSegments(1);
				IPath platform = Platform.getLocation().append(result.getName());
				if (!user.toFile().equals(platform.toFile()))
					result.setLocation(user);
			}
		} catch (IOException ex) {
			e = ex;
		}
		if (result == null || e != null) {
			String message = Policy.bind("resources.errorReadProject", path.toOSString()); //$NON-NLS1 //$NON-NLS-1$
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message, e);
			throw new ResourceException(status);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#move(IResource[], IPath, int, IProgressMonitor)
	 */
	public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Policy.bind("resources.moving.0"); //$NON-NLS-1$
			monitor.beginTask(message, totalWork);
			Assert.isLegal(resources != null);
			if (resources.length == 0)
				return Status.OK_STATUS;
			resources = (IResource[]) resources.clone(); // to avoid concurrent changes to this array
			IPath parentPath = null;
			message = Policy.bind("resources.moveProblem"); //$NON-NLS-1$
			MultiStatus status = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, null);
			try {
				prepareOperation(getRoot(), monitor);
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
							IStatus requirements = resource.checkMoveRequirements(destination.append(resource.getName()), resource.getType(), updateFlags);
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
						message = Policy.bind("resources.notChild", resource.getFullPath().toString(), parentPath.toString()); //$NON-NLS-1$
						status.merge(new ResourceStatus(IResourceStatus.OPERATION_FAILED, resource.getFullPath(), message));
					}
				}
			} catch (OperationCanceledException e) {
				getWorkManager().operationCanceled();
				throw e;
			} finally {
				endOperation(getRoot(), true, Policy.subMonitorFor(monitor, totalWork - opWork));
			}
			if (status.matches(IStatus.ERROR))
				throw new ResourceException(status);
			return status.isOK() ? (IStatus) Status.OK_STATUS : (IStatus) status;
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#move(IResource[], IPath, boolean, IProgressMonitor)
	 */
	public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= IResource.KEEP_HISTORY;
		return move(resources, destination, updateFlags, monitor);
	}

	/**
	 * Moves this resource's subtree to the destination. This operation should only be
	 * used by move methods. Destination must be a valid destination for this resource.
	 * The keepSyncInfo boolean is used to indicated whether or not the sync info should
	 * be moved from the source to the destination.
	 */

	/* package */
	void move(Resource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo) throws CoreException {
		// overlay the tree at the destination path, preserving any important info
		// in any already existing resource infos
		copyTree(source, destination, depth, updateFlags, keepSyncInfo);
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

	/* (non-Javadoc)
	 * @see IWorkspace#newProjectDescription(String)
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
				if (path.segmentCount() < ICoreConstants.MINIMUM_FOLDER_SEGMENT_LENGTH) {
					message = "Path must include project and resource name: " + path.toString(); //$NON-NLS-1$
					Assert.isLegal(false, message);
				}
				return new Folder(path.makeAbsolute(), this);
			case IResource.FILE :
				if (path.segmentCount() < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
					message = "Path must include project and resource name: " + path.toString(); //$NON-NLS-1$
					Assert.isLegal(false, message);
				}
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
	 * @see ResourcesPlugin#getWorkspace()
	 */
	public IStatus open(IProgressMonitor monitor) throws CoreException {
		// This method is not inside an operation because it is the one responsible for
		// creating the WorkManager object (who takes care of operations).
		String message = Policy.bind("resources.workspaceOpen"); //$NON-NLS-1$
		Assert.isTrue(!isOpen(), message);
		if (!getMetaArea().hasSavedWorkspace()) {
			message = Policy.bind("resources.readWorkspaceMeta"); //$NON-NLS-1$
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, Platform.getLocation(), message, null);
		}
		description = new WorkspacePreferences();
		description.setDefaults(Workspace.defaultWorkspaceDescription());

		// if we have an old description file, read it (getting rid of it)
		WorkspaceDescription oldDescription = getMetaArea().readOldWorkspace();
		if (oldDescription != null) {
			description.copyFrom(oldDescription);
			ResourcesPlugin.getPlugin().savePluginPreferences();
		}

		// create root location
		localMetaArea.locationFor(getRoot()).toFile().mkdirs();

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
		return Status.OK_STATUS;
	}

	/**
	 * Called before checking the pre-conditions of an operation.  Optionally supply
	 * a scheduling rule to determine when the operation is safe to run.  If a scheduling 
	 * rule is supplied, this method will block until it is safe to run.
	 * 
	 * @param rule the scheduling rule that describes what this operation intends to modify.
	 */
	public void prepareOperation(ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
		//make sure autobuild is not running
		if (rule != null)
			buildManager.interrupt();
		getWorkManager().checkIn(rule, monitor);
		if (!isOpen()) {
			String message = Policy.bind("resources.workspaceClosed"); //$NON-NLS-1$
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

	/* (non-Javadoc)
	 * @see IWorkspace#removeResourceChangeListener(IResourceChangeListener)
	 */
	public void removeResourceChangeListener(IResourceChangeListener listener) {
		notificationManager.removeListener(listener);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#removeSaveParticipant(Plugin)
	 */
	public void removeSaveParticipant(Plugin plugin) {
		Assert.isNotNull(plugin, "Plugin must not be null"); //$NON-NLS-1$
		saveManager.removeParticipant(plugin);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor)
	 */
	public void run(IWorkspaceRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask(null, Policy.totalWork);
			int depth = -1;
			boolean avoidNotification = (options & IWorkspace.AVOID_UPDATE) != 0;
			try {
				prepareOperation(rule, monitor);
				beginOperation(true);
				if (avoidNotification)
					avoidNotification = notificationManager.beginAvoidNotify();
				depth = getWorkManager().beginUnprotected();
				action.run(Policy.subMonitorFor(monitor, Policy.opWork, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK));
			} catch (OperationCanceledException e) {
				getWorkManager().operationCanceled();
				throw e;
			} finally {
				if (avoidNotification)
					notificationManager.endAvoidNotify();
				if (depth >= 0)
					getWorkManager().endUnprotected(depth);
				endOperation(rule, false, Policy.subMonitorFor(monitor, Policy.endOpWork));
			}
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#run(IWorkspaceRunnable, IProgressMonitor)
	 */
	public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
		run(action, defaultRoot, IWorkspace.AVOID_UPDATE, monitor);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#save(boolean, IProgressMonitor)
	 */
	public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException {
		String message;
		if (full) {
			//according to spec it is illegal to start a full save inside another operation
			if (getWorkManager().isLockAlreadyAcquired()) {
				message = Policy.bind("resources.saveOp"); //$NON-NLS-1$
				throw new ResourceException(IResourceStatus.OPERATION_FAILED, null, message, new IllegalStateException());
			}
			return saveManager.save(ISaveContext.FULL_SAVE, null, monitor);
		}
		// A snapshot was requested.  Start an operation (if not already started) and 
		// signal that a snapshot should be done at the end.
		try {
			prepareOperation(getRoot(), monitor);
			beginOperation(false);
			saveManager.requestSnapshot();
			message = Policy.bind("resources.snapRequest"); //$NON-NLS-1$
			return new ResourceStatus(IStatus.OK, message);
		} finally {
			endOperation(getRoot(), false, null);
		}
	}

	public void setCrashed(boolean value) {
		crashed = value;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 */
	public void setDescription(IWorkspaceDescription value) throws CoreException {
		// if both the old and new description's build orders are null, leave the
		// workspace's build order slot because it is caching the computed order.
		// Otherwise, set the slot to null to force recomputation or building from the description.
		WorkspaceDescription newDescription = (WorkspaceDescription) value;
		String[] newOrder = newDescription.getBuildOrder(false);
		if (description.getBuildOrder(false) != null || newOrder != null)
			buildOrder = null;
		description.copyFrom(newDescription);
		ResourcesPlugin.getPlugin().savePluginPreferences();
	}

	public void setTreeLocked(boolean locked) {
		treeLocked = locked ? Thread.currentThread() : null;
	}

	/**
	 * @deprecated
	 */
	public void setWorkspaceLock(WorkspaceLock lock) {
		// do nothing
	}

	protected void shutdown(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			IManager[] managers = {buildManager, propertyManager, pathVariableManager, charsetManager, fileSystemManager, markerManager, _workManager, aliasManager, refreshManager, contentDescriptionManager};
			monitor.beginTask(null, managers.length);
			String message = Policy.bind("resources.shutdownProblems"); //$NON-NLS-1$
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
						message = Policy.bind("resources.shutdownProblems"); //$NON-NLS-1$
						status.add(new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.INTERNAL_ERROR, message, e));
					}
				}
			}
			buildManager = null;
			notificationManager = null;
			propertyManager = null;
			pathVariableManager = null;
			fileSystemManager = null;
			markerManager = null;
			synchronizer = null;
			saveManager = null;
			_workManager = null;
			aliasManager = null;
			refreshManager = null;
			charsetManager = null;
			contentDescriptionManager = null;
			if (!status.isOK())
				throw new CoreException(status);
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#sortNatureSet(String[])
	 */
	public String[] sortNatureSet(String[] natureIds) {
		return natureManager.sortNatureSet(natureIds);
	}

	protected void startup(IProgressMonitor monitor) throws CoreException {
		// ensure the tree is locked during the startup notification
		try {
			_workManager = new WorkManager(this);
			_workManager.startup(null);
			fileSystemManager = new FileSystemResourceManager(this);
			fileSystemManager.startup(monitor);
			propertyManager = new PropertyManager(this);
			propertyManager.startup(monitor);
			pathVariableManager = new PathVariableManager();
			pathVariableManager.startup(null);
			natureManager = new NatureManager();
			natureManager.startup(null);
			buildManager = new BuildManager(this, getWorkManager().getLock());
			buildManager.startup(null);
			notificationManager = new NotificationManager(this);
			notificationManager.startup(null);
			markerManager = new MarkerManager(this);
			markerManager.startup(null);
			synchronizer = new Synchronizer(this);
			saveManager = new SaveManager(this);
			saveManager.startup(null);
			//must start after save manager, because (read) access to tree is needed
			aliasManager = new AliasManager(this);
			aliasManager.startup(null);
			refreshManager = new RefreshManager(this);
			refreshManager.startup(null);
			charsetManager = new CharsetManager(this);
			charsetManager.startup(null);
			contentDescriptionManager = new ContentDescriptionManager();
			contentDescriptionManager.startup(null);
		} finally {
			//unlock tree even in case of failure, otherwise shutdown will also fail
			treeLocked = null;
		}
	}

	/** 
	 * Returns a string representation of this working state's
	 * structure suitable for debug purposes.
	 */
	public String toDebugString() {
		final StringBuffer buffer = new StringBuffer("\nDump of " + toString() + ":\n"); //$NON-NLS-1$ //$NON-NLS-2$
		buffer.append("  parent: " + tree.getParent()); //$NON-NLS-1$
		IElementContentVisitor visitor = new IElementContentVisitor() {
			public boolean visitElement(ElementTree aTree, IPathRequestor requestor, Object elementContents) {
				buffer.append("\n  " + requestor.requestPath() + ": " + elementContents); //$NON-NLS-1$ //$NON-NLS-2$
				return true;
			}
		};
		new ElementTreeIterator(tree, Path.ROOT).iterate(visitor);
		return buffer.toString();
	}

	public void updateModificationStamp(ResourceInfo info) {
		info.setModificationStamp(nextModificationStamp());
	}

	/* (non-javadoc)
	 * @see IWorkspace#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(final IFile[] files, final Object context) {
		// if validation is turned off then just return
		if (!shouldValidate) {
			String message = Policy.bind("resources.readOnly2"); //$NON-NLS-1$
			MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.READ_ONLY_LOCAL, message, null);
			for (int i = 0; i < files.length; i++) {
				if (files[i].isReadOnly()) {
					IPath filePath = files[i].getFullPath();
					message = Policy.bind("resources.readOnly", filePath.toString()); //$NON-NLS-1$
					result.add(new ResourceStatus(IResourceStatus.READ_ONLY_LOCAL, filePath, message));
				}
			}
			return result.getChildren().length == 0 ? Status.OK_STATUS : (IStatus) result;
		}
		// first time through the validator hasn't been initialized so try and create it
		if (validator == null)
			initializeValidator();
		// we were unable to initialize the validator. Validation has been turned off and 
		// a warning has already been logged so just return.
		if (validator == null)
			return Status.OK_STATUS;
		// otherwise call the API and throw an exception if appropriate
		final IStatus[] status = new IStatus[1];
		ISafeRunnable body = new ISafeRunnable() {
			public void run() throws Exception {
				status[0] = validator.validateEdit(files, context);
			}

			public void handleException(Throwable exception) {
				status[0] = new ResourceStatus(IStatus.ERROR, null, Policy.bind("resources.errorValidator"), exception); //$NON-NLS-1$
			}
		};
		Platform.run(body);
		return status[0];
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateLinkLocation(IResource, IPath)
	 */
	public IStatus validateLinkLocation(IResource resource, IPath unresolvedLocation) {
		String message;
		//check if resource linking is disabled
		if (ResourcesPlugin.getPlugin().getPluginPreferences().getBoolean(ResourcesPlugin.PREF_DISABLE_LINKING)) {
			message = Policy.bind("links.workspaceVeto", resource.getName()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//check that the resource has a project as its parent
		IContainer parent = resource.getParent();
		if (parent == null || parent.getType() != IResource.PROJECT) {
			message = Policy.bind("links.parentNotProject", resource.getName()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		if (!parent.isAccessible()) {
			message = Policy.bind("links.parentNotAccessible", resource.getFullPath().toString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		IPath location = getPathVariableManager().resolvePath(unresolvedLocation);
		//check nature veto
		String[] natureIds = ((Project) parent).internalGetDescription().getNatureIds();

		IStatus result = getNatureManager().validateLinkCreation(natureIds);
		if (!result.isOK())
			return result;
		//check team provider veto
		if (resource.getType() == IResource.FILE)
			result = getTeamHook().validateCreateLink((IFile) resource, IResource.NONE, location);
		else
			result = getTeamHook().validateCreateLink((IFolder) resource, IResource.NONE, location);
		if (!result.isOK())
			return result;
		if (location.isEmpty()) {
			message = Policy.bind("links.noPath"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//check the standard path name restrictions
		int segmentCount = location.segmentCount();
		for (int i = 0; i < segmentCount; i++) {
			result = validateName(location.segment(i), resource.getType());
			if (!result.isOK())
				return result;
		}
		//if the location doesn't have a device, see if the OS will assign one
		if (location.isAbsolute() && location.getDevice() == null)
			location = new Path(location.toFile().getAbsolutePath());
		// test if the given location overlaps the platform metadata location
		IPath testLocation = getMetaArea().getLocation();
		if (isOverlapping(location, testLocation, true)) {
			message = Policy.bind("links.invalidLocation", location.toOSString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//test if the given path overlaps the location of the given project
		testLocation = resource.getProject().getLocation();
		if (testLocation != null && isOverlapping(location, testLocation, false)) {
			message = Policy.bind("links.locationOverlapsProject", location.toOSString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, resource.getFullPath(), message);
		}
		//warnings (all errors must be checked before all warnings)
		//check that the location is absolute
		if (!location.isAbsolute()) {
			//we know there is at least one segment, because of previous isEmpty check
			message = Policy.bind("pathvar.undefined", location.toOSString(), location.segment(0)); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.VARIABLE_NOT_DEFINED_WARNING, resource.getFullPath(), message);
		}
		// Iterate over each known project and ensure that the location does not
		// conflict with any project locations or linked resource locations
		IProject[] projects = getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			// since we are iterating over the project in the workspace, we
			// know that they have been created before and must have a description
			IProjectDescription desc = ((Project) project).internalGetDescription();
			testLocation = desc.getLocation();
			if (testLocation != null && isOverlapping(location, testLocation, true)) {
				message = Policy.bind("links.overlappingResource", location.toOSString()); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, resource.getFullPath(), message);
			}
			//iterate over linked resources and check for overlap
			if (!project.isOpen())
				continue;
			IResource[] children = null;
			try {
				children = project.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children == null)
				continue;
			for (int j = 0; j < children.length; j++) {
				if (children[j].isLinked()) {
					testLocation = children[j].getLocation();
					if (testLocation != null && isOverlapping(location, testLocation, true)) {
						message = Policy.bind("links.overlappingResource", location.toOSString()); //$NON-NLS-1$
						return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, resource.getFullPath(), message);
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateName(String, int)
	 */
	public IStatus validateName(String segment, int type) {
		String message;

		/* segment must not be null */
		if (segment == null) {
			message = Policy.bind("resources.nameNull"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		// cannot be an empty string
		if (segment.length() == 0) {
			message = Policy.bind("resources.nameEmpty"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* segment must not begin or end with a whitespace */
		if (Character.isWhitespace(segment.charAt(0)) || Character.isWhitespace(segment.charAt(segment.length() - 1))) {
			message = Policy.bind("resources.invalidWhitespace", segment); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* segment must not end with a dot */
		if (segment.endsWith(".")) { //$NON-NLS-1$
			message = Policy.bind("resources.invalidDot", segment); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* test invalid characters */
		char[] chars = OS.INVALID_RESOURCE_CHARACTERS;
		for (int i = 0; i < chars.length; i++)
			if (segment.indexOf(chars[i]) != -1) {
				message = Policy.bind("resources.invalidCharInName", String.valueOf(chars[i]), segment); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}

		/* test invalid OS names */
		if (!OS.isNameValid(segment)) {
			message = Policy.bind("resources.invalidName", segment); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		return Status.OK_STATUS;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateNatureSet(String[])
	 */
	public IStatus validateNatureSet(String[] natureIds) {
		return natureManager.validateNatureSet(natureIds);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validatePath(String, int)
	 */
	public IStatus validatePath(String path, int type) {
		/* path must not be null */
		if (path == null) {
			String message = Policy.bind("resources.pathNull"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		return validatePath(new Path(path), type, false);
	}

	/**
	 * Validates that the given workspace path is valid for the given type.  If 
	 * <code>lastSegmentOnly</code> is true, it is assumed that all segments except
	 * the last one have previously been validated.  This is an optimization for validating
	 * a leaf resource when it is known that the parent exists (and thus its parent path
	 * must already be valid).
	 */
	public IStatus validatePath(IPath path, int type, boolean lastSegmentOnly) {
		String message;

		/* path must not be null */
		if (path == null) {
			message = Policy.bind("resources.pathNull"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must not have a device separator */
		if (path.getDevice() != null) {
			message = Policy.bind("resources.invalidCharInPath", String.valueOf(IPath.DEVICE_SEPARATOR), path.toString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must not be the root path */
		if (path.isRoot()) {
			message = Policy.bind("resources.invalidRoot"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* path must be absolute */
		if (!path.isAbsolute()) {
			message = Policy.bind("resources.mustBeAbsolute", path.toString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}

		/* validate segments */
		int numberOfSegments = path.segmentCount();
		if ((type & IResource.PROJECT) != 0) {
			if (numberOfSegments == ICoreConstants.PROJECT_SEGMENT_LENGTH) {
				return validateName(path.segment(0), IResource.PROJECT);
			} else if (type == IResource.PROJECT) {
				message = Policy.bind("resources.projectPath", path.toString()); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		if ((type & (IResource.FILE | IResource.FOLDER)) != 0) {
			if (numberOfSegments < ICoreConstants.MINIMUM_FILE_SEGMENT_LENGTH) {
				message = Policy.bind("resources.resourcePath", path.toString()); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
			int fileFolderType = type &= ~IResource.PROJECT;
			int segmentCount = path.segmentCount();
			if (lastSegmentOnly)
				return validateName(path.segment(segmentCount - 1), fileFolderType);
			IStatus status = validateName(path.segment(0), IResource.PROJECT);
			if (!status.isOK())
				return status;
			// ignore first segment (the project)
			for (int i = 1; i < segmentCount; i++) {
				status = validateName(path.segment(i), fileFolderType);
				if (!status.isOK())
					return status;
			}
			return Status.OK_STATUS;
		}
		message = Policy.bind("resources.invalidPath", path.toString()); //$NON-NLS-1$
		return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateProjectLocation(IProject, IPath)
	 */
	public IStatus validateProjectLocation(IProject context, IPath unresolvedLocation) {
		String message;
		// the default default is ok for all projects
		if (unresolvedLocation == null) {
			return Status.OK_STATUS;
		}
		//check the standard path name restrictions
		IPath location = getPathVariableManager().resolvePath(unresolvedLocation);
		int segmentCount = location.segmentCount();
		for (int i = 0; i < segmentCount; i++) {
			IStatus result = validateName(location.segment(i), IResource.PROJECT);
			if (!result.isOK())
				return result;
		}
		//check that the location is absolute
		if (!location.isAbsolute()) {
			if (location.segmentCount() > 0)
				message = Policy.bind("pathvar.undefined", location.toOSString(), location.segment(0)); //$NON-NLS-1$
			else
				message = Policy.bind("links.noPath"); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.VARIABLE_NOT_DEFINED, null, message);
		}
		//if the location doesn't have a device, see if the OS will assign one
		if (location.getDevice() == null)
			location = new Path(location.toFile().getAbsolutePath());
		// test if the given location overlaps the default default location
		IPath defaultDefaultLocation = Platform.getLocation();
		if (isOverlapping(location, defaultDefaultLocation, true)) {
			message = Policy.bind("resources.overlapLocal", location.toString(), defaultDefaultLocation.toString()); //$NON-NLS-1$
			return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
		}
		// Iterate over each known project and ensure that the location does not
		// conflict with any of their already defined locations.
		IProject[] projects = getRoot().getProjects();
		for (int j = 0; j < projects.length; j++) {
			IProject project = projects[j];
			// since we are iterating over the project in the workspace, we
			// know that they have been created before and must have a description
			IProjectDescription desc = ((Project) project).internalGetDescription();
			IPath definedLocalLocation = desc.getLocation();
			// if the project uses the default location then continue
			if (definedLocalLocation == null)
				continue;
			//tolerate locations being the same if this is the project being tested
			if (project.equals(context) && definedLocalLocation.equals(location))
				continue;
			if (isOverlapping(location, definedLocalLocation, true)) {
				message = Policy.bind("resources.overlapLocal", location.toString(), definedLocalLocation.toString()); //$NON-NLS-1$
				return new ResourceStatus(IResourceStatus.INVALID_VALUE, null, message);
			}
		}
		//if this project exists and has linked resources, the project location cannot overlap
		//the locations of any linked resources in that project
		if (context.exists() && context.isOpen()) {
			IResource[] children = null;
			try {
				children = context.members();
			} catch (CoreException e) {
				//ignore projects that cannot be accessed
			}
			if (children != null) {
				for (int i = 0; i < children.length; i++) {
					if (children[i].isLinked()) {
						IPath testLocation = children[i].getLocation();
						if (testLocation != null && isOverlapping(testLocation, location, false)) {
							message = Policy.bind("links.locationOverlapsLink", location.toOSString()); //$NON-NLS-1$
							return new ResourceStatus(IResourceStatus.OVERLAPPING_LOCATION, context.getFullPath(), message);
						}
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Internal method. To be called only from the following methods:
	 * <ul>
	 * <li><code>IFile#appendContents</code></li>
	 * <li><code>IFile#setContents(InputStream, boolean, boolean, IProgressMonitor)</code></li>
	 * <li><code>IFile#setContents(IFileState, boolean, boolean, IProgressMonitor)</code></li>
	 * </ul>
	 * 
	 * @see IFileModificationValidator#validateSave(IFile)
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
				status[0] = new ResourceStatus(IStatus.ERROR, null, Policy.bind("resources.errorValidator"), exception); //$NON-NLS-1$
			}
		};
		Platform.run(body);
		if (!status[0].isOK())
			throw new ResourceException(status[0]);
	}
}