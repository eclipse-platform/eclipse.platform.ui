/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Red Hat Incorporated - loadProjectDescription(InputStream)
 *******************************************************************************/
package org.eclipse.core.internal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import org.eclipse.core.internal.events.*;
import org.eclipse.core.internal.localstore.FileSystemResourceManager;
import org.eclipse.core.internal.properties.IPropertyManager;
import org.eclipse.core.internal.refresh.RefreshManager;
import org.eclipse.core.internal.utils.*;
import org.eclipse.core.internal.watson.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.team.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.xml.sax.InputSource;

/**
 * The workspace class is the monolithic nerve center of the resources plugin.
 * All interesting functionality stems from this class.
 * </p>
 * <p>
 * The lifecycle of the resources plugin is encapsulated by the {@link #open(IProgressMonitor)}
 * and {@link #close(IProgressMonitor)} methods.  A closed workspace is completely
 * unusable - any attempt to access or modify interesting workspace state on a closed
 * workspace will fail.
 * </p>
 * <p>
 * All modifications to the workspace occur within the context of a workspace operation.
 * A workspace operation is implemented using the following sequence:
 * <pre>
 * 	try {
 *		prepareOperation(...);
 *		//check preconditions
 *		beginOperation(...);
 *		//perform changes
 *	} finally {
 *		endOperation(...);
 *	}
 * </pre>
 * Workspace operations can be nested arbitrarily. A "top level" workspace operation
 * is an operation that is not nested within another workspace operation in the current
 * thread.
 * See the javadoc of {@link #prepareOperation(ISchedulingRule, IProgressMonitor)},
 * {@link #beginOperation(boolean)}, and {@link #endOperation(ISchedulingRule, boolean, IProgressMonitor)}
 * for more details.
 * </p>
 * <p>
 * Major areas of functionality are farmed off to various manager classes.  Open a
 * type hierarchy on {@link IManager} to see all the different managers. Each
 * manager is typically referenced three times in this class: Once in {@link #startup(IProgressMonitor)}
 * when it is instantiated, once in {@link #shutdown(IProgressMonitor)} when it
 * is destroyed, and once in a manager accessor method.
 * </p>
 */
public class Workspace extends PlatformObject implements IWorkspace, ICoreConstants {
	public static final boolean caseSensitive = Platform.OS_MACOSX.equals(Platform.getOS()) ? false : new java.io.File("a").compareTo(new java.io.File("A")) != 0; //$NON-NLS-1$ //$NON-NLS-2$
	// whether the resources plugin is in debug mode.
	public static boolean DEBUG = false;

	/**
	 * Work manager should never be accessed directly because accessor
	 * asserts that workspace is still open.
	 */
	protected WorkManager _workManager;
	protected AliasManager aliasManager;
	protected BuildManager buildManager;
	protected IProject[] buildOrder = null;
	protected CharsetManager charsetManager;
	protected ContentDescriptionManager contentDescriptionManager;
	/** indicates if the workspace crashed in a previous session */
	protected boolean crashed = false;
	protected final IWorkspaceRoot defaultRoot = new WorkspaceRoot(Path.ROOT, this);
	protected WorkspacePreferences description;
	protected FileSystemResourceManager fileSystemManager;
	protected final HashSet lifecycleListeners = new HashSet(10);
	protected LocalMetaArea localMetaArea;
	/**
	 * Helper class for performing validation of resource names and locations.
	 */
	protected final LocationValidator locationValidator = new LocationValidator(this);
	protected MarkerManager markerManager;
	/**
	 * The currently installed Move/Delete hook.
	 */
	protected IMoveDeleteHook moveDeleteHook = null;
	protected NatureManager natureManager;
	protected long nextMarkerId = 0;
	protected long nextNodeId = 1;

	protected NotificationManager notificationManager;
	protected boolean openFlag = false;
	protected ElementTree operationTree; // tree at the start of the current operation
	protected PathVariableManager pathVariableManager;
	protected IPropertyManager propertyManager;

	protected RefreshManager refreshManager;

	/**
	 * Scheduling rule factory. This field is null if the factory has not been used
	 * yet.  The accessor method should be used rather than accessing this field
	 * directly.
	 */
	private IResourceRuleFactory ruleFactory;

	protected SaveManager saveManager;
	/**
	 * File modification validation.  If it is true and validator is null, we try/initialize 
	 * validator first time through.  If false, there is no validator.
	 */
	protected boolean shouldValidate = true;

	/**
	 * Job that performs periodic string pool canonicalization.
	 */
	private StringPoolJob stringPoolJob;

	/**
	 * The synchronizer
	 */
	protected Synchronizer synchronizer;

	/**
	 * The currently installed team hook.
	 */
	protected TeamHook teamHook = null;

	/**
	 * The workspace tree.  The tree is an in-memory representation
	 * of the resources that make up the workspace.  The tree caches
	 * the structure and state of files and directories on disk (their existence
	 * and last modified times).  When external parties make changes to
	 * the files on disk, this representation becomes out of sync. A local refresh
	 * reconciles the state of the files on disk with this tree (@link {@link IResource#refreshLocal(int, IProgressMonitor)}).
	 * The tree is also used to store metadata associated with resources in
	 * the workspace (markers, properties, etc).
	 * 
	 * While the ElementTree data structure can hand both concurrent
	 * reads and concurrent writes, write access to the tree is governed
	 * by {@link WorkManager}.
	 */
	protected ElementTree tree;

	/**
	 * This field is used to control access to the workspace tree during
	 * resource change notifications. It tracks which thread, if any, is
	 * in the middle of a resource change notification.  This is used to cause
	 * attempts to modify the workspace during notifications to fail.
	 */
	protected Thread treeLocked = null;

	/**
	 * The currently installed file modification validator.
	 */
	protected IFileModificationValidator validator = null;

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

	public static WorkspaceDescription defaultWorkspaceDescription() {
		return new WorkspaceDescription("Workspace"); //$NON-NLS-1$
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
	protected void aboutToBuild(Object source, int trigger) {
		//fire a POST_CHANGE first to ensure everyone is up to date before firing PRE_BUILD
		broadcastPostChange();
		broadcastBuildEvent(source, IResourceChangeEvent.PRE_BUILD, trigger);
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

	public void broadcastBuildEvent(Object source, int type, int buildTrigger) {
		ResourceChangeEvent event = new ResourceChangeEvent(source, type, buildTrigger, null);
		notificationManager.broadcastChanges(tree, event, false);
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

	public void broadcastPostChange() {
		ResourceChangeEvent event = new ResourceChangeEvent(this, IResourceChangeEvent.POST_CHANGE, 0, null);
		notificationManager.broadcastChanges(tree, event, true);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#build(int, IProgressMonitor)
	 */
	public void build(int trigger, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		final ISchedulingRule rule = getRuleFactory().buildRule();
		try {
			monitor.beginTask("", Policy.opWork); //$NON-NLS-1$
			try {
				prepareOperation(rule, monitor);
				beginOperation(true);
				aboutToBuild(this, trigger);
				IStatus result;
				try {
					result = getBuildManager().build(trigger, Policy.subMonitorFor(monitor, Policy.opWork));
				} finally {
					//must fire POST_BUILD if PRE_BUILD has occurred
					broadcastBuildEvent(this, IResourceChangeEvent.POST_BUILD, trigger);
				}
				if (!result.isOK())
					throw new ResourceException(result);
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

	/**
	 * Returns whether creating executable extensions is acceptable
	 * at this point in time.  In particular, returns <code>false</code>
	 * when the system bundle is shutting down, which only occurs
	 * when the entire framework is exiting.
	 */
	private boolean canCreateExtensions() {
		return Platform.getBundle("org.eclipse.osgi").getState() != Bundle.STOPPING; //$NON-NLS-1$
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
				broadcastPostChange();
			} finally {
				endOperation(rule, build, null);
			}
		} catch (CoreException e) {
			Policy.log(e.getStatus());
		}
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
			String msg = Messages.resources_closing_0;
			int rootCount = tree.getChildCount(Path.ROOT);
			monitor.beginTask(msg, rootCount + 2);
			monitor.subTask(msg);
			//this operation will never end because the world is going away
			try {
				stringPoolJob.cancel();
				//shutdown save manager now so a last snapshot can be taken before we close
				//note: you can't call #save() from within a nested operation
				saveManager.shutdown(null);
				prepareOperation(getRoot(), monitor);
				//shutdown notification first to avoid calling third parties during shutdown
				notificationManager.shutdown(null);
				beginOperation(true);
				IProject[] projects = getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
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
			//release the scheduling rule to be a good job citizen
			Job.getJobManager().endRule(getRoot());
			monitor.done();
		}
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
		IProject[] allProjects = getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
		// List<IProject[]> edges
		List edges = new ArrayList(allProjects.length);
		for (int i = 0; i < allProjects.length; i++) {
			Project project = (Project) allProjects[i];
			// ignore projects that are not accessible
			if (!project.isAccessible())
				continue;
			ProjectDescription desc = project.internalGetDescription();
			if (desc == null)
				continue;
			//obtain both static and dynamic project references
			IProject[] refs = desc.getAllReferences(false);
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
		// List<IProject> projects
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
		// List<IProject[]> knots
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

	/* (non-Javadoc)
	 * @see IWorkspace#copy(IResource[], IPath, boolean, IProgressMonitor)
	 */
	public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		return copy(resources, destination, updateFlags, monitor);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#copy(IResource[], IPath, int, IProgressMonitor)
	 */
	public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			Assert.isLegal(resources != null);
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Messages.resources_copying_0;
			monitor.beginTask(message, totalWork);
			if (resources.length == 0)
				return Status.OK_STATUS;
			// to avoid concurrent changes to this array
			resources = (IResource[]) resources.clone();
			IPath parentPath = null;
			message = Messages.resources_copyProblem;
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
						message = NLS.bind(Messages.resources_notChild, resources[i].getFullPath(), parentPath);
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

	protected void copyTree(IResource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo) throws CoreException {
		// retrieve the resource at the destination if there is one (phantoms included).
		// if there isn't one, then create a new handle based on the type that we are
		// trying to copy
		IResource destinationResource = getRoot().findMember(destination, true);
		int destinationType;
		if (destinationResource == null) {
			if (source.getType() == IResource.FILE)
				destinationType = IResource.FILE;
			else if (destination.segmentCount() == 1)
				destinationType = IResource.PROJECT;
			else
				destinationType = IResource.FOLDER;
			destinationResource = newResource(destination, destinationType);
		} else
			destinationType = destinationResource.getType();

		// create the resource at the destination
		ResourceInfo sourceInfo = ((Resource) source).getResourceInfo(true, false);
		if (destinationType != source.getType()) {
			sourceInfo = (ResourceInfo) sourceInfo.clone();
			sourceInfo.setType(destinationType);
		}
		ResourceInfo newInfo = createResource(destinationResource, sourceInfo, false, false, keepSyncInfo);
		// get/set the node id from the source's resource info so we can later put it in the
		// info for the destination resource. This will help us generate the proper deltas,
		// indicating a move rather than a add/delete
		newInfo.setNodeId(sourceInfo.getNodeId());

		// preserve local sync info but not location info
		newInfo.setFlags(newInfo.getFlags() | (sourceInfo.getFlags() & M_LOCAL_EXISTS));
		newInfo.setFileStoreRoot(null);

		// forget content-related caching flags
		newInfo.clear(M_CONTENT_CACHE);

		// update link locations in project descriptions
		if (source.isLinked()) {
			LinkDescription linkDescription;
			if ((updateFlags & IResource.SHALLOW) != 0) {
				//for shallow move the destination is a linked resource with the same location
				newInfo.set(ICoreConstants.M_LINK);
				linkDescription = new LinkDescription(destinationResource, source.getLocationURI());
			} else {
				//for deep move the destination is not a linked resource
				newInfo.clear(ICoreConstants.M_LINK);
				linkDescription = null;
			}
			Project project = (Project) destinationResource.getProject();
			project.internalGetDescription().setLinkLocation(destinationResource.getProjectRelativePath(), linkDescription);
			project.writeDescription(updateFlags);
		}

		// do the recursion. if we have a file then it has no members so return. otherwise
		// recursively call this method on the container's members if the depth tells us to
		if (depth == IResource.DEPTH_ZERO || source.getType() == IResource.FILE)
			return;
		if (depth == IResource.DEPTH_ONE)
			depth = IResource.DEPTH_ZERO;
		//copy .project file first if project is being copied, otherwise links won't be able to update description
		boolean projectCopy = source.getType() == IResource.PROJECT && destinationType == IResource.PROJECT;
		if (projectCopy) {
			IResource dotProject = ((Project) source).findMember(IProjectDescription.DESCRIPTION_FILE_NAME);
			if (dotProject != null)
				copyTree(dotProject, destination.append(dotProject.getName()), depth, updateFlags, keepSyncInfo);
		}
		IResource[] children = ((IContainer) source).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS | IContainer.INCLUDE_HIDDEN);
		for (int i = 0, imax = children.length; i < imax; i++) {
			String childName = children[i].getName();
			if (!projectCopy || !childName.equals(IProjectDescription.DESCRIPTION_FILE_NAME)) {
				IPath childPath = destination.append(childName);
				copyTree(children[i], childPath, depth, updateFlags, keepSyncInfo);
			}
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
		return createResource(resource, null, phantom, false, false);
	}

	/**
	 * Creates a resource, honoring update flags requesting that the resource
	 * be immediately made derived, hidden and/or team private
	 */
	public ResourceInfo createResource(IResource resource, int updateFlags) throws CoreException {
		ResourceInfo info = createResource(resource, null, false, false, false);
		if ((updateFlags & IResource.DERIVED) != 0)
			info.set(M_DERIVED);
		if ((updateFlags & IResource.TEAM_PRIVATE) != 0)
			info.set(M_TEAM_PRIVATE_MEMBER);
		if ((updateFlags & IResource.HIDDEN) != 0)
			info.set(M_HIDDEN);
		return info;
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
			info.clearModificationStamp();
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
				String message = NLS.bind(Messages.resources_mustNotExist, resource.getFullPath());
				throw new ResourceException(IResourceStatus.RESOURCE_EXISTS, resource.getFullPath(), message, null);
			}
		}
		return info;
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
	 * @see IWorkspace#delete(IResource[], int, IProgressMonitor)
	 */
	public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Messages.resources_deleting_0;
			monitor.beginTask(message, totalWork);
			message = Messages.resources_deleteProblem;
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
							message = NLS.bind(Messages.resources_couldnotDelete, resource.getFullPath());
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
			IProject[] children = getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
			for (int i = 0; i < children.length; i++)
				tree.deleteElement(children[i].getFullPath());
		} else
			tree.deleteElement(path);
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

				// At this time we need to re-balance the nested operations. It is necessary because
				// build() and snapshot() should not fail if they are called.
				workManager.rebalanceNestedOperations();

				//find out if any operation has potentially modified the tree
				hasTreeChanges = workManager.shouldBuild();
				//double check if the tree has actually changed
				if (hasTreeChanges)
					hasTreeChanges = operationTree != null && ElementTree.hasChanges(tree, operationTree, ResourceComparator.getBuildComparator(), true);
				broadcastPostChange();
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
		IProject[] projects = getRoot().getProjects(IContainer.INCLUDE_HIDDEN);
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

	public IPropertyManager getPropertyManager() {
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
			String message = Messages.resources_shutdown;
			throw new ResourceException(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, null, message));
		}
		return _workManager;
	}

	/**
	 * A move/delete hook hasn't been initialized. Check the extension point and 
	 * try to create a new hook if a user has one defined as an extension. Otherwise
	 * use the Core's implementation as the default.
	 */
	protected void initializeMoveDeleteHook() {
		try {
			if (!canCreateExtensions())
				return;
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_MOVE_DELETE_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: should provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneHook, null);
				Policy.log(status);
				return;
			}
			// otherwise we have exactly one hook extension. Try to create a new instance 
			// from the user-specified class.
			try {
				IConfigurationElement config = configs[0];
				moveDeleteHook = (IMoveDeleteHook) config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				//ignore the failure if we are shutting down (expected since extension
				//provider plugin has probably already shut down
				if (canCreateExtensions()) {
					IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initHook, e);
					Policy.log(status);
				}
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
			if (!canCreateExtensions())
				return;
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_TEAM_HOOK);
			// no-one is plugged into the extension point so disable validation
			if (configs == null || configs.length == 0) {
				return;
			}
			// can only have one defined at a time. log a warning
			if (configs.length > 1) {
				//XXX: should provide a meaningful status code
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneTeamHook, null);
				Policy.log(status);
				return;
			}
			// otherwise we have exactly one hook extension. Try to create a new instance 
			// from the user-specified class.
			try {
				IConfigurationElement config = configs[0];
				teamHook = (TeamHook) config.createExecutableExtension("class"); //$NON-NLS-1$
			} catch (CoreException e) {
				//ignore the failure if we are shutting down (expected since extension
				//provider plugin has probably already shut down
				if (canCreateExtensions()) {
					IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initTeamHook, e);
					Policy.log(status);
				}
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

	/**
	 * A file modification validator hasn't been initialized. Check the extension point and 
	 * try to create a new validator if a user has one defined as an extension.
	 */
	protected void initializeValidator() {
		shouldValidate = false;
		if (!canCreateExtensions())
			return;
		IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(ResourcesPlugin.PI_RESOURCES, ResourcesPlugin.PT_FILE_MODIFICATION_VALIDATOR);
		// no-one is plugged into the extension point so disable validation
		if (configs == null || configs.length == 0) {
			return;
		}
		// can only have one defined at a time. log a warning, disable validation, but continue with
		// the #setContents (e.g. don't throw an exception)
		if (configs.length > 1) {
			//XXX: should provide a meaningful status code
			IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_oneValidator, null);
			Policy.log(status);
			return;
		}
		// otherwise we have exactly one validator extension. Try to create a new instance 
		// from the user-specified class.
		try {
			IConfigurationElement config = configs[0];
			validator = (IFileModificationValidator) config.createExecutableExtension("class"); //$NON-NLS-1$
			shouldValidate = true;
		} catch (CoreException e) {
			//ignore the failure if we are shutting down (expected since extension
			//provider plugin has probably already shut down
			if (canCreateExtensions()) {
				IStatus status = new ResourceStatus(IStatus.ERROR, 1, null, Messages.resources_initValidator, e);
				Policy.log(status);
			}
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

	public boolean isOpen() {
		return openFlag;
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
	 * @see IWorkspace#loadProjectDescription(InputStream)
	 * @since 3.1
	 */
	public IProjectDescription loadProjectDescription(InputStream stream) throws CoreException {
		IProjectDescription result = null;
		result = new ProjectDescriptionReader().read(new InputSource(stream));
		if (result == null) {
			String message = NLS.bind(Messages.resources_errorReadProject, stream.toString());
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message, null);
			throw new ResourceException(status);
		}
		return result;
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
				IPath platform = getRoot().getLocation().append(result.getName());
				if (!user.toFile().equals(platform.toFile()))
					result.setLocation(user);
			}
		} catch (IOException ex) {
			e = ex;
		}
		if (result == null || e != null) {
			String message = NLS.bind(Messages.resources_errorReadProject, path.toOSString());
			IStatus status = new Status(IStatus.ERROR, ResourcesPlugin.PI_RESOURCES, IResourceStatus.FAILED_READ_METADATA, message, e);
			throw new ResourceException(status);
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see IWorkspace#move(IResource[], IPath, boolean, IProgressMonitor)
	 */
	public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException {
		int updateFlags = force ? IResource.FORCE : IResource.NONE;
		updateFlags |= IResource.KEEP_HISTORY;
		return move(resources, destination, updateFlags, monitor);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#move(IResource[], IPath, int, IProgressMonitor)
	 */
	public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			Assert.isLegal(resources != null);
			int opWork = Math.max(resources.length, 1);
			int totalWork = Policy.totalWork * opWork / Policy.opWork;
			String message = Messages.resources_moving_0;
			monitor.beginTask(message, totalWork);
			if (resources.length == 0)
				return Status.OK_STATUS;
			resources = (IResource[]) resources.clone(); // to avoid concurrent changes to this array
			IPath parentPath = null;
			message = Messages.resources_moveProblem;
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
						message = NLS.bind(Messages.resources_notChild, resource.getFullPath(), parentPath);
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

	/**
	 * Moves this resource's subtree to the destination. This operation should only be
	 * used by move methods. Destination must be a valid destination for this resource.
	 * The keepSyncInfo boolean is used to indicated whether or not the sync info should
	 * be moved from the source to the destination.
	 */

	/* package */
	void move(Resource source, IPath destination, int depth, int updateFlags, boolean keepSyncInfo) throws CoreException {
		// overlay the tree at the destination path, preserving any important info
		// in any already existing resource information
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
		updateModificationStamp(result);
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

	protected long nextNodeId() {
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
		String message = Messages.resources_workspaceOpen;
		Assert.isTrue(!isOpen(), message);
		if (!getMetaArea().hasSavedWorkspace()) {
			message = Messages.resources_readWorkspaceMeta;
			throw new ResourceException(IResourceStatus.FAILED_READ_METADATA, Platform.getLocation(), message, null);
		}
		description = new WorkspacePreferences();

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
				refreshManager.refresh(getRoot());
			} catch (RuntimeException e) {
				//don't fail entire open if refresh failed, just report as warning
				return new ResourceStatus(IResourceStatus.INTERNAL_ERROR, Path.ROOT, Messages.resources_errorMultiRefresh, e);
			}
		}
		//finally register a string pool participant
		stringPoolJob = new StringPoolJob();
		stringPoolJob.addStringPoolParticipant(saveManager, getRoot());
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
		try {
			//make sure autobuild is not running if it conflicts with this operation
			if (rule != null && rule.isConflicting(getRuleFactory().buildRule()))
				buildManager.interrupt();
		} finally {
			getWorkManager().checkIn(rule, monitor);
		}
		if (!isOpen()) {
			String message = Messages.resources_workspaceClosed;
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
	 * @see IWorkspace#run(IWorkspaceRunnable, IProgressMonitor)
	 */
	public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
		run(action, defaultRoot, IWorkspace.AVOID_UPDATE, monitor);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#run(IWorkspaceRunnable, ISchedulingRule, int, IProgressMonitor)
	 */
	public void run(IWorkspaceRunnable action, ISchedulingRule rule, int options, IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			monitor.beginTask("", Policy.totalWork); //$NON-NLS-1$
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
	 * @see IWorkspace#save(boolean, IProgressMonitor)
	 */
	public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException {
		String message;
		if (full) {
			//according to spec it is illegal to start a full save inside another operation
			if (getWorkManager().isLockAlreadyAcquired()) {
				message = Messages.resources_saveOp;
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
			message = Messages.resources_snapRequest;
			return new ResourceStatus(IStatus.OK, message);
		} finally {
			endOperation(getRoot(), false, null);
		}
	}

	public void setCrashed(boolean value) {
		crashed = value;
		if (crashed) {
			String msg = "The workspace exited with unsaved changes in the previous session; refreshing workspace to recover changes."; //$NON-NLS-1$
			Policy.log(new ResourceStatus(ICoreConstants.CRASH_DETECTED, msg));
			if (Policy.DEBUG)
				System.out.println(msg);
		}
	}

	/* (non-Javadoc)
	 * @see IWorkspace#setDescription(IWorkspaceDescription)
	 */
	public void setDescription(IWorkspaceDescription value) {
		// if both the old and new description's build orders are null, leave the
		// workspace's build order slot because it is caching the computed order.
		// Otherwise, set the slot to null to force recomputing or building from the description.
		WorkspaceDescription newDescription = (WorkspaceDescription) value;
		String[] newOrder = newDescription.getBuildOrder(false);
		if (description.getBuildOrder(false) != null || newOrder != null)
			buildOrder = null;
		description.copyFrom(newDescription);
		ResourcesPlugin.getPlugin().savePluginPreferences();
	}

	public void setTreeLocked(boolean locked) {
		Assert.isTrue(!locked || treeLocked == null, "The workspace tree is already locked"); //$NON-NLS-1$
		treeLocked = locked ? Thread.currentThread() : null;
	}

	/**
	 * @deprecated
	 */
	public void setWorkspaceLock(WorkspaceLock lock) {
		// do nothing
	}

	/**
	 * Shuts down the workspace managers.
	 */
	protected void shutdown(IProgressMonitor monitor) throws CoreException {
		monitor = Policy.monitorFor(monitor);
		try {
			IManager[] managers = {buildManager, propertyManager, pathVariableManager, charsetManager, fileSystemManager, markerManager, _workManager, aliasManager, refreshManager, contentDescriptionManager};
			monitor.beginTask("", managers.length); //$NON-NLS-1$
			String message = Messages.resources_shutdownProblems;
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
						message = Messages.resources_shutdownProblems;
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

	/**
	 * Starts all the workspace manager classes.
	 */
	protected void startup(IProgressMonitor monitor) throws CoreException {
		// ensure the tree is locked during the startup notification
		try {
			_workManager = new WorkManager(this);
			_workManager.startup(null);
			fileSystemManager = new FileSystemResourceManager(this);
			fileSystemManager.startup(monitor);
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
			refreshManager = new RefreshManager(this);
			saveManager = new SaveManager(this);
			saveManager.startup(null);
			//must start after save manager, because (read) access to tree is needed
			refreshManager.startup(null);
			aliasManager = new AliasManager(this);
			aliasManager.startup(null);
			propertyManager = ResourcesCompatibilityHelper.createPropertyManager();
			propertyManager.startup(monitor);
			charsetManager = new CharsetManager(this);
			charsetManager.startup(null);
			contentDescriptionManager = new ContentDescriptionManager();
			contentDescriptionManager.startup(null);
		} finally {	
			//unlock tree even in case of failure, otherwise shutdown will also fail
			treeLocked = null;	
			_workManager.postWorkspaceStartup();
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
		info.incrementModificationStamp();
	}

	/* (non-javadoc)
	 * @see IWorkspace#validateEdit(IFile[], Object)
	 */
	public IStatus validateEdit(final IFile[] files, final Object context) {
		// if validation is turned off then just return
		if (!shouldValidate) {
			String message = Messages.resources_readOnly2;
			MultiStatus result = new MultiStatus(ResourcesPlugin.PI_RESOURCES, IResourceStatus.READ_ONLY_LOCAL, message, null);
			for (int i = 0; i < files.length; i++) {
				if (files[i].isReadOnly()) {
					IPath filePath = files[i].getFullPath();
					message = NLS.bind(Messages.resources_readOnly, filePath);
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
			public void handleException(Throwable exception) {
				status[0] = new ResourceStatus(IStatus.ERROR, null, Messages.resources_errorValidator, exception);
			}

			public void run() throws Exception {
				Object c = context;
				//must null any reference to FileModificationValidationContext for backwards compatibility
				if (!(validator instanceof FileModificationValidator))
					if (c instanceof FileModificationValidationContext)
						c = null;
				status[0] = validator.validateEdit(files, c);
			}
		};
		SafeRunner.run(body);
		return status[0];
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateLinkLocation(IResource, IPath)
	 */
	public IStatus validateLinkLocation(IResource resource, IPath unresolvedLocation) {
		return locationValidator.validateLinkLocation(resource, unresolvedLocation);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateLinkLocation(IResource, IPath)
	 */
	public IStatus validateLinkLocationURI(IResource resource, URI unresolvedLocation) {
		return locationValidator.validateLinkLocationURI(resource, unresolvedLocation);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateName(String, int)
	 */
	public IStatus validateName(String segment, int type) {
		return locationValidator.validateName(segment, type);
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
		return locationValidator.validatePath(path, type);
	}

	/* (non-Javadoc)
	 * @see IWorkspace#validateProjectLocation(IProject, IPath)
	 */
	public IStatus validateProjectLocation(IProject context, IPath location) {
		return locationValidator.validateProjectLocation(context, location);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IWorkspace#validateProjectLocation(org.eclipse.core.resources.IProject, java.net.URI)
	 */
	public IStatus validateProjectLocationURI(IProject project, URI location) {
		return locationValidator.validateProjectLocationURI(project, location);
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
			public void handleException(Throwable exception) {
				status[0] = new ResourceStatus(IStatus.ERROR, null, Messages.resources_errorValidator, exception);
			}

			public void run() throws Exception {
				status[0] = validator.validateSave(file);
			}
		};
		SafeRunner.run(body);
		if (!status[0].isOK())
			throw new ResourceException(status[0]);
	}

}
