package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */

import java.util.Map;

import org.eclipse.core.runtime.*;

/**
 * Workspaces are the basis for Eclipse Platform resource management.  There 
 * is only one workspace per running platform.  All resources exist in the context
 * of this workspace.
 * <p>
 * A workspace corresponds closely to discreet areas in the local file system.
 * Each project in a workspace maps onto a specific area of the file system.
 * The folders and files within a project map directly onto the corresponding
 * directories and files in the file system.  
 * One subdirectory, the workspace metadata area, contains internal 
 * information about the workspace and its resources. This metadata
 * area should be accessed only by the Platform or via Platform API calls.
 * </p>
 * <p>
 * Workspaces add value over using the file system directly in that they 
 * allow for comprehensive change tracking (through <code>IResourceDelta</code>s), 
 * various forms of resource metadata (e.g., markers and properties) as well as support
 * for managing application/tool state (e.g., saving and restoring).
 * </p>
 * <p>
 * The workspace as a whole is thread safe and allows one 
 * writer concurrent with multiple readers.  It also supports mechanisms for
 * saving and snapshoting the current resource state.
 * </p>
 * <p>
 * The workspace is provided by the Resources plug-in and is automatically created
 * when that plug-in is activated.  The default workspace data area (i.e., where
 * its resources are stored) overlap exactly with the platform's data area.  That is,
 * by default, the workspace's projects are found directly in the platform's data area.
 * Individual project locations can be specified explicitly.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Workspaces implement the <code>IAdaptable</code> interface;
 * 	extensions are managed by the platform's adapter manager.
 * </p>
 */
public interface IWorkspace extends IAdaptable {
/** 
 * Adds the given listener for resource change events to this workspace.
 * Has no effect if an identical listener is already registered.
 * <p>
 * This method is equivalent to:
 * <pre>
 *     addResourceChangeListener(listener, 
 *         IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.POST_CHANGE);
 * </pre>
 * </p>
 * 
 * @param listener the listener
 * @see IResourceChangeListener
 * @see IResourceChangeEvent
 * @see #addResourceChangeListener(IResourceChangeListener, int)
 * @see #removeResourceChangeListener
 */
public void addResourceChangeListener(IResourceChangeListener listener);
/** 
 * Adds the given listener for the specified resource change events to this workspace.
 * Has no effect if an identical listener is already registered for these events.
 * After completion of this method, the given listener will be registered for exactly the
 * the specified events.  If they were previously registered for other events, they
 * will be deregistered.  
 * <p>
 * Once registered, a listener starts receiving notification of changes to
 * resources in the workspace. The resource deltas in the resource change 
 * event are rooted at the workspace root.  Most resource change notifications
 * occur well after the fact; the exception is pre-notification of impending 
 * project closures and deletions. The listener continues to receive 
 * notifications until it is replaced or removed. 
 * </p>
 * <p>
 * Listeners can listen for several types of event as defined in <code>IResourceChangeEvent</code>.
 * Clients are free to register for any number of event types however if they register
 * for more than one, it is their responsibility to ensure they correctly handle the
 * case where the same resource change shows up in multiple notifications.  
 * Clients are guaranteed to receive only the events for which they are registered.
 * </p>
 * 
 * @param listener the listener
 * @param eventMask the bit-wise OR of all event types of interest to the listener
 * @see IResourceChangeListener
 * @see IResourceChangeEvent
 * @see #removeResourceChangeListener
 */
public void addResourceChangeListener(IResourceChangeListener listener, int eventMask);
/** 
 * Registers the given plug-in's workspace save participant, and
 * returns an object describing the workspace state at the time
 * of the last save in which the plug-in participated.
 * <p>
 * Once registered, the workspace save participant will actively
 * participate in the saving of this workspace.
 * </p>
 * 
 * @param plugin the plug-in 
 * @param participant the participant
 * @return the last saved state in which the plug-in participated,
 *   or <code>null</code> if the plug-in has not participated before
 * @exception CoreException if the method fails to add the participant.
 * Reasons include:
 * <ul>
 * <li> The previous state could not be recovered.</li>
 * </ul>
 * @see ISaveParticipant
 * @see #removeSaveParticipant
 */
public ISavedState addSaveParticipant(Plugin plugin, ISaveParticipant participant) throws CoreException;
/** 
 * Builds all projects in this workspace.  Projects are built in the order specified
 * in this workspace's description.  Projects not mentioned in the order or for which
 * the order cannot be determined are built in an undefined order after all other 
 * projects have been built.  If no order is specified, the workspace computes
 * an order determined by project references.  
 * <p>
 * This method may change resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param kind the kind of build being requested. Valid values are
 * <ul>
 * <li> <code>FULL_BUILD</code> - indicates a full build.</li>
 * <li> <code>INCREMENTAL_BUILD</code> - indicates a incremental build.</li>
 * </ul>
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if some of the builds fail.
 *    The status contained in the exception is a multi-status with
 *    entries for the project builds that failed.
 *
 * @see IProject#build
 * @see #computePrerequisiteOrder
 * @see IncrementalProjectBuilder#FULL_BUILD
 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
 */
public void build(int kind, IProgressMonitor monitor) throws CoreException;
/**
 * Checkpoints the operation currently in progress.
 * This method is used in the middle of a group of operations 
 * to force a build (if the build argument is true) and send an 
 * interim notification of resource change events.
 * <p>
 * When invoked in the dynamic scope of a
 * call to the <code>IWorkspace.run</code> method,
 * this method reports a single resource change event 
 * describing the net effect of all changes done to resources
 * since the last round of notifications.
 * When the outermost <code>run</code> method eventually
 * completes, it will do another auto-build (if enabled) and report
 * the resource changes made after this call.
 * </p>
 * <p>
 * This method has no effect if invoked outside the dynamic scope
 * of a call to the <code>IWorkspace.run</code> method.
 * </p>
 * <p>
 * This method should be used under controlled circumstance
 * (e.g., to break up extremely long-running operations).
 * </p>
 *
 * @param build whether or not to run a build
 * @see IWorkspace#run
 */
public void checkpoint(boolean build);
/**
 * Returns the prerequisite ordering of the given projects.  The computation
 * is done by interpreting project references as dependency relationships.
 * For example if A references B and C, and C references B, this method,
 * given the list A, B, C will return the order B, C, A.  That is, projects with
 * no dependencies are listed first.
 * <p>
 * The return value is a two element array of project arrays.  The first project
 * array is the list of projects which could be sorted (as outlined above).  The
 * second element of the return value is an array of the projects which are
 * ambiguously ordered (e.g., they are part of a cycle).
 * </p>
 * <p>
 * Cycles and ambiguities are handled by elimination.  Projects involved
 * in cycles are simply cut out of the ordered list and returned in an undefined
 * order.  Closed and non-existent projects are ignored and do not appear
 * in the returned value at all.
 * </p>
 *
 * @param projects the projects to order
 * @return the projects in sorted order and a list of projects which could 
 *		not be ordered
 */
public IProject[][] computePrerequisiteOrder(IProject[] projects);

/**
 * Copies the given sibling resources so that they are located 
 * as members of the resource at the given path; the names of
 * the copies are the same as the corresponding originals.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   copy(resources, destination, (force ? IResource.FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resources have been added to the new parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to copy
 * @param destination the destination container path
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return a status object with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages
 * @exception CoreException if the method fails to copy some resources.
 * The status contained in the exception may be a multi-status indicating 
 * where the individual failures occurred.
 * @see #copy(IResource[],IPath,int,IProgressMonitor)
 */
public IStatus copy(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;

/**
 * Copies the given sibling resources so that they are located 
 * as members of the resource at the given path; the names of
 * the copies are the same as the corresponding originals.
 * <p>
 * This method can be expressed as a series of calls to 
 * <code>IResource.copy(IPath,int,IProgressMonitor</code>, with "best effort"
 * semantics:
 * <ul>
 * <li> Resources are copied in the order specified, using the given update
 *    flags.</li>
 * <li> Duplicate resources are only copied once.</li>
 * <li> The method fails if the resources are not all siblings.</li>
 * <li> The failure of an individual copy does not necessarily prevent
 *    the method from attempting to copy other resources.</li>
 * <li> The method fails if there are projects among the resources.</li>
 * <li> The method fails if the path of the resources
 *    is a prefix of the destination path.</li>
 * <li> This method also fails if one or more of the
 *    individual resource copy steps fails.</li>
 * </ul>
 * </p>
 * <p>
 * After successful completion, corresponding new resources 
 * will now exist as members of the resource at the given path.
 * </p>
 * <p>
 * The supplied path may be absolute or relative.  Absolute paths
 * fully specify the new location for the resource, including its
 * project.  Relative paths are considered to be relative to the
 * container of the resources being copied. A trailing separator is ignored.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resources have been added to the new parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to copy
 * @param destination the destination container path
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return a status object with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages
 * @exception CoreException if the method fails to copy some resources.
 * The status contained in the exception may be a multi-status indicating 
 * where the individual failures occurred. Reasons include:
 * <ul>
 * <li> One of the resources does not exist.</li>
 * <li> The resources are not siblings.</li>
 * <li> One of the resources, or one of its descendents, is not local.</li>
 * <li> The resource corresponding to the destination path does not exist.</li>
 * <li> The resource corresponding to the parent destination path is a closed project.</li>
 * <li> A corresponding target resource does exist.</li>
 * <li> A resource of a different type exists at the target path.</li>
 * <li> One of the resources is a project.</li>
 * <li> The path of one of the resources is a prefix of the destination path.</li>
 * <li> One of the resources, or one of its descendents, is out of sync with the
 *      local file system and <code>FORCE</code> is not specified.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IResource#copy(IPath,int,IProgressMonitor)
 * @since 2.0
 */
public IStatus copy(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Deletes the given resources.  
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   delete(resources, IResource.KEEP_HISTORY | (force ? IResource.FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to delete
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return status with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages
 * @exception CoreException if the method fails to delete some resource.
 *    The status contained in the exception is a multi-status indicating 
 *    where the individual failures occurred.
 * @see #delete(IResource[],int,IProgressMonitor)
 */
public IStatus delete(IResource[] resources, boolean force, IProgressMonitor monitor) throws CoreException;

/**
 * Deletes the given resources.  
 * <p>
 * This method can be expressed as a series of calls to 
 * <code>IResource.delete(int,IProgressMonitor)</code>.
 * </p>
 * <p>
 * The semantics of multiple deletion are:
 * <ul>
 * <li> Resources are deleted in the order presented, using the given update
 *    flags.</li>
 * <li> Resources that do not exist are ignored.</li>
 * <li> An individual deletion fails if the resource still exists afterwards.</li>
 * <li> The failure of an individual deletion does not prevent
 *    the method from attempting to delete other resources.</li>
 * <li> This method fails if one or more of the
 *    individual resource deletions fails; that is, if at least one
 *    of the resources in the list still exists at the end of this 
 *    method.</li>
 * </ul>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to delete
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return status with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages
 * @exception CoreException if the method fails to delete some resource.
 *    The status contained in the exception is a multi-status indicating 
 *    where the individual failures occurred.
 * @see IResource#delete(int,IProgressMonitor)
 * @since 2.0
 */
public IStatus delete(IResource[] resources, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Removes the given markers from the resources with which they are associated.
 * Markers that do not exist are ignored.
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 *
 * @param markers the markers to remove
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 */
public void deleteMarkers(IMarker[] markers) throws CoreException;
/**
 * Forgets any resource tree being saved for the plug-in
 * with the given name. If the plug-in id is <code>null</code>,
 * all trees are forgotten.
 * <p>
 * Clients should not call this method unless they have a reason
 * to do so.
 * A plug-in which uses <code>ISaveContext.needDelta</code> in the
 * process of a save indicates that it would like to be fed the
 * a resource delta the next time it is reactivated. If a plug-in
 * never gets reactivated (or if it fails to successfully register
 * to participate in workspace saves), the workspace nevertheless
 * retains the necessary information to generate the resource delta
 * if asked. This method allows such a long term leak to be plugged.
 * </p>
 *
 * @param pluginId the unique identifier of the plug-in
 * @see ISaveContext#needDelta
 */
public void forgetSavedTree(String pluginId);
/**
 * Returns all nature descriptors known to this workspace.
 * Returns an empty array if there are no installed natures.
 *
 * @return the nature descriptors known to this workspace
 * @since 2.0
 */
public IProjectNatureDescriptor[] getNatureDescriptors(); 
/**
 * Returns the nature descriptor with the given unique identifier,
 * or <code>null</code> if there is no such nature.
 *
 * @param natureId the nature extension identifer
 *		(e.g. <code>"com.example.coolNature"</code>).
 * @return the nature descriptor, or <code>null</code>
 * @since 2.0
 */
public IProjectNatureDescriptor getNatureDescriptor(String natureId); 
/**
 * Finds all dangling project references in this workspace. 
 * Projects which are not open are ignored.
 * Returns a map with one entry for each open project in the workspace
 * that has at least one dangling project reference; the value
 * of the entry is an array of projects which are referenced by that
 * project but do not exist in the workspace.  Returns an empty Map
 * if there are no projects in the workspace.
 *
 * @return a map (key type: <code>IProject</code>, value type: <code>IProject[]</code>)
 *    from project to dangling project references
 */
public Map getDanglingReferences();
/**
 * Returns the workspace description. This object is responsible for defining
 * workspace preferences. The returned value is a modifiable copy but changes
 * are not automatically applied to the workspace. In order to changes
 * take effect, <code>IWorkspace.setDescription</code> needs to be called.
 *
 * @return the workspace description
 * @see #setDescription
 */
public IWorkspaceDescription getDescription();
/**
 * Returns the root resource of this workspace.
 *
 * @return the workspace root
 */
public IWorkspaceRoot getRoot();
/**
 * Returns the synchronizer for this workspace.
 *
 * @return the synchronizer
 * @see ISynchronizer
 */
public ISynchronizer getSynchronizer();
/**
 * Returns whether this workspace performs auto-builds.
 *
 * @return <code>true</code> if auto-building is on, <code>false</code> otherwise
 */
public boolean isAutoBuilding();

/**
 * Reads the project description file (".project") from the given location
 * in the local file system. This object is useful for discovering the 
 * correct name for a project before importing it into the workspace.
 * <p>
 * The returned value is writeable.
 * </p>
 *
 * @param projectDescriptionFile the path in the local file system of an
 *   existing project description file
 * @return a new project description
 * @exception CoreException if the operation failed, either because the
 *   project description file does not exist, or cannot be opened, or
 *   cannot be parsed as a legal project description file
 * @see #newProjectDescription
 * @see IProject#getDescription
 * @since 2.0
 */
public IProjectDescription loadProjectDescription(IPath projectDescriptionFile) throws CoreException; 
/**
 * Moves the given sibling resources so that they are located 
 * as members of the resource at the given path; the names of
 * the new members are the same.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   move(resources, destination, IResource.KEEP_HISTORY | (force ? IResource.FORCE : IResource.NONE), monitor);
 * </pre>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resources have been removed from their parent
 * and that corresponding resources have been added to the new parent.
 * Additional information provided with resource delta shows that these
 * additions and removals are pairwise related.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to move
 * @param destination the destination container path
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return status with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages.
 * @exception CoreException if the method fails to move some resources.
 * The status contained in the exception may be a multi-status indicating 
 * where the individual failures occurred.
 * @see #move(IResource[],IPath,int,IProgressMonitor)
 */
public IStatus move(IResource[] resources, IPath destination, boolean force, IProgressMonitor monitor) throws CoreException;

/**
 * Moves the given sibling resources so that they are located 
 * as members of the resource at the given path; the names of
 * the new members are the same.
 * <p>
 * This method can be expressed as a series of calls to 
 * <code>IResource.move</code>, with "best effort" semantics:
 * <ul>
 * <li> Resources are moved in the order specified.</li>
 * <li> Duplicate resources are only moved once.</li>
 * <li> The <code>force</code> flag has the same meaning as it does
 *      on the corresponding single-resource method.</li>
 * <li> The method fails if the resources are not all siblings.</li>
 * <li> The method fails the path of any of the resources
 *    is a prefix of the destination path.</li>
 * <li> The failure of an individual move does not necessarily prevent
 *    the method from attempting to move other resources.</li>
 * <li> This method also fails if one or more of the
 *    individual resource moves fails; that is, if at least one
 *    of the resources in the list still exists at the end of this 
 *    method.</li>
 * <li> History is kept for moved files. When projects are moved, no history
 *    is kept</li>
 * </ul>
 * </p>
 * <p>
 * After successful completion, the resources and descendents
 * will no longer exist; but corresponding new resources 
 * will now exist as members of the resource at the given path.
 * </p>
 * <p>
 * The supplied path may be absolute or relative.  Absolute paths
 * fully specify the new location for the resource, including its
 * project.  Relative paths are considered to be relative to the
 * container of the resources being moved. A trailing separator is ignored.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resources have been removed from their parent
 * and that corresponding resources have been added to the new parent.
 * Additional information provided with resource delta shows that these
 * additions and removals are pairwise related.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param resources the resources to move
 * @param destination the destination container path
 * @param updateFlags bit-wise or of update flag constants
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return status with code <code>OK</code> if there were no problems;
 *     otherwise a description (possibly a multi-status) consisting of
 *     low-severity warnings or informational messages.
 * @exception CoreException if the method fails to move some resources.
 * The status contained in the exception may be a multi-status indicating 
 * where the individual failures occurred. Reasons include:
 * <ul>
 * <li> One of the resources does not exist.</li>
 * <li> The resources are not siblings.</li>
 * <li> One of the resources, or one of its descendents, is not local.</li>
 * <li> The resource corresponding to the destination path does not exist.</li>
 * <li> The resource corresponding to the parent destination path is a 
 *      closed project.</li>
 * <li> A corresponding target resource does exist.</li>
 * <li> A resource of a different type exists at the target path.</li>
 * <li> The path of one of the resources is a prefix of the destination path.</li>
 * <li> One of the resources, or one of its descendents, is out of sync with the
 *      local file system and <code>FORCE</code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IResource#move(IPath,int,IProgressMonitor)
 * @since 2.0
 */
public IStatus move(IResource[] resources, IPath destination, int updateFlags, IProgressMonitor monitor) throws CoreException;

/**
 * Creates and returns a new project description for a project
 * with the given name.   This object is useful when creating,
 * moving or copying projects.
 * <p>
 * The project description is initialized to:
 * <ul>
 * <li> the given project name</li>
 * <li> no references to other projects</li>
 * <li> an empty build spec</li>
 * <li> an empty comment</li>
 * </ul>
 * </p>
 * <p>
 * The returned value is writeable.
 * </p>
 *
 * @param projectName the name of the project
 * @return a new project description
 * @see IProject#getDescription
 * @see IProject#create
 * @see IProject#copy
 * @see IProject#move
 */
public IProjectDescription newProjectDescription(String projectName);
/** 
 * Removes the given resource change listener from this workspace.
 * Has no effect if an identical listener is not registered.
 *
 * @param listener the listener
 * @see IResourceChangeListener
 * @see #addResourceChangeListener
 */
public void removeResourceChangeListener(IResourceChangeListener listener);
/** 
 * Removes the workspace save participant for the given plug-in
 * from this workspace. If no such participant is registered, 
 * no action is taken.
 * <p>
 * Once removed, the workspace save participant no longer actively
 * participates in any future saves of this workspace.
 * </p>
 * 
 * @param plugin the plug-in
 * @see ISaveParticipant
 * @see #addSaveParticipant
 */
public void removeSaveParticipant(Plugin plugin);
/**
 * Runs the given action as an atomic workspace operation.
 * <p>
 * After running a method that modifies resources in the workspace,
 * registered listeners receive after-the-fact notification of
 * what just transpired, in the form of a resource change event.
 * This method allows clients to call a number of
 * methods that modify resources and only have resource
 * change event notifications reported at the end of the entire
 * batch.
 * </p>
 * <p>
 * If this method is called outside the dynamic scope of another such
 * call, this method runs the action and then reports a single
 * resource change event describing the net effect of all changes
 * done to resources by the action.
 * </p>
 * <p>
 * If this method is called in the dynamic scope of another such
 * call, this method simply runs the action.
 * </p>
 *
 * @param action the action to perform
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if the operation failed.
 */
public void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException;
/**
 * Saves this workspace's valuable state on disk.
 * Consults with all registered plug-ins so that they can
 * coordinate the saving of their persistent state as well.
 * <p>
 * The <code>full</code> parameter indicates whether a full save or a snapshot
 * is being requested. Snapshots save the workspace information that is
 * considered hard to be recomputed in the unlikely event of a crash. It includes
 * parts of the workspace tree, workspace and projects descriptions, markers and
 * sync infos. Full saves are heavy weight operations which save the complete
 * workspace state.
 * </p>
 * <p>
 * To ensure that all outstanding changes to the workspace have been 
 * reported to interested parties prior to saving, a full save cannot
 * be used within the dynamic scope of an <code>IWorkspace.run</code> 
 * invocation. Snapshots can be called anytime and are interpreted by the
 * workspace as a hint that a snapshot is required. The workspace will perform
 * the snapshot when possible. Even as a hint, snapshots should only be called
 * when necessary as they impact system performance.
 * Although saving does not change the workspace per se, its execution
 * is serialized like methods that write the workspace.
 * </p>
 * <p>
 * The workspace is comprised of several different kinds of data with varying
 * degrees of importance. The most important data, the resources themselves
 * and their persistent properties,
 * are written to disk immediately; other data are kept in volatile memory 
 * and only written to disk periodically; and other data are maintained in memory 
 * and never written out. The following table summarizes what gets saved when:
 * <ul>
 * <li>creating or deleting resource - immediately</li>
 * <li>setting contents of file - immediately</li>
 * <li>changes to project description - immediately</li>
 * <li>session properties - never</li>
 * <li>changes to persistent properties - immediately</li>
 * <li>markers - <code>save</code></li>
 * <li>synchronizer info - <code>save</code></li>
 * <li>shape of the workspace resource tree - <code>save</code></li>
 * <li>list of active plug-ins - never</li>
 * </ul>
 * Resource-based plug-in also have data with varying degrees of importance.
 * Each plug-in gets to decide the policy for protecting its data, either
 * immediately, never, or at <code>save</code> time. For the latter,
 * the plug-in coordinates its actions with the workspace
 * (see <code>ISaveParticipant</code> for details).
 * </p>
 * <p>
 * If the platform is shutdown (or crashes) after saving the workspace,
 * any information written to disk by the last successful workspace
 * <code>save</code> will be restored the next time the workspace is 
 * reopened for the next session. 
 * Naturally, information that is written to disk immediately
 * will be as of the last time it was changed. 
 * </p>
 * <p>
 * The workspace provides a general mechanism for keeping concerned parties
 * apprised of any and all changes to resources in the workspace
 * (<code>IResourceChangeListener</code>).
 * It is even possible for a plug-in to find out about changes to resources
 * that happen between workspace sessions
 * (see <code>IWorkspace.addSaveParticipant</code>).
 * </p>
 * <p>
 * At certain points during this method, the entire workspace resource tree
 * must be locked to prevent resources from being changed (read access to
 * resources is permitted).
 * </p>
 * <p>
 * Implementation note: The execution sequence is as follows.
 * <ul>
 * <li>A long-term lock on the workspace is taken out to
 *   prevent further changes to workspace until the save is done.</li>
 * <li>The list of saveable resource tree snapshots is initially empty.</li>
 * <li>A different <code>ISaveContext</code> object is created for each
 *     registered workspace save participant plug-in, reflecting the
 *     kind of save (<code>ISaveContext.getKind</code>), the previous save number
 *     in which this plug-in actively participated, and the new save number
 *     (= previous save number plus 1).
 * </li>
 * <li>Each registered workspace save participant is sent
 *     <code>prepareToSave(context)</code>, passing in its own context object.
 *   <ul>
 *   <li>Plug-in suspends all activities until further notice.
 *   </li>
 *   </ul>
 *   If <code>prepareToSave</code> fails (throws an exception),
 *   the problem is logged and the participant is marked as unstable.
 * </li>
 * <li>In dependent-before-prerequisite order, each registered workspace
 *     save participant is sent
 *     <code>saving(context)</code>, passing in its own context object.
 *   <ul>
 *   <li>Plug-in decides whether it wants to actively participate in this
 *       save. The plug-in only needs to actively participate if some of 
 *       its important state has changed since the last time it actively
 *       participated. If it does decide to actively participate, 
 *       it writes its important state to a brand new file in its
 *       plug-in state area under a generated file name based on 
 *       <code>context.getStateNumber()</code> and calls
 *       <code>context.needStateNumber()</code> to indicate that
 *       it has actively participated. If upon reactivation
 *       the plug-in will want a resource delta covering all changes
 *       between now and then, the plug-in should invoke 
 *       <code>context.needDelta()</code> to request this now; otherwise,
 *       a resource delta for the intervening period will not be available
 *       on reactivation.
 *   </li>
 *   </ul>
 *   If <code>saving</code> fails (throws an exception),
 *   the problem is logged and the participant is marked as unstable.
 * </li>
 * <li>The plug-in save table contains an entry for each plug-in that
 *     has registered to participate in workspace saves at some time in
 *     the past (the list of plug-ins increases monotonically). Each
 *     entry records the save number of the last successful save in which 
 *     that plug-in actively participated, and, optionally, a saved resource tree
 *     (conceptually, this is a complete tree; in practice, it is compressed into
 *     a special delta tree representation).
 *     A copy of the plug-in save table is made. Entries are created
 *     or modified for each registered plug-in to record the appropriate
 *     save number (either the previous save number, or the previous save
 *     number plus 1, depending on whether the participant was active and
 *     asked for a new number).
 * </li>
 * <li>The workspace tree, the modified copy of the plug-in save table,
 *     all markers, etc. and all saveable resource tree 
 *	   snapshots are written to disk as <b>one atomic operation</b>.
 * </li>
 * <li>The long-term lock on the workspace is released.</li>
 * <li>If the atomic save succeeded: 
 *   <ul>
 *   <li>The modified copy of the plug-in save table becomes the new
 *       plug-in save table.
 *   </li>
 *   <li>In prerequisite-before-dependent order, each registered workspace
 *      save participant is sent
 *      <code>doneSaving(context)</code>, passing in its own context object.
 *     <ul>
 *     <li>Plug-in may perform clean up by deleting obsolete state files
 *        in its plug-in state area.
 *     </li>
 *     <li>Plug-in resumes its normal activities.
 *     </li>
 *     </ul>
 *     If <code>doneSaving</code> fails (throws an exception),
 *     the problem is logged and the participant is marked as unstable.
 *     (The state number in the save table is not rolled back just because of
 *      this instability.)
 *   </li>
 *   <li>The workspace save operation returns.</li>
 *   </ul>
 * <li>If it failed:
 *   <ul>
 *   <li>The workspace previous state is restored.
 *   </li>
 *   <li>In prerequisite-before-dependent order, each registered workspace 
 *     save participant is sent
 *     <code>rollback(context)</code>, passing in its own context object.
 *     <ul>
 *     <li>Plug-in may perform clean up by deleting newly-created 
 *       but obsolete state file in its plug-in state area.
 *     </li>
 *     <li>Plug-in resumes its normal activities.
 *     </li>
 *     </ul>
 *     If <code>rollback</code> fails (throws an exception),
 *     the problem is logged and the participant is marked as unstable.
 *     (The state number in the save table is rolled back anyway.)
 *   </li>
 *   <li>The workspace save operation fails.</li>
 *   </ul>
 * </li>
 * </ul>
 * </p>
 * <p>
 * After a full save, the platform can be shutdown. This will cause
 * the Resources plug-in and all the other plug-ins to shutdown,
 * without disturbing the saved workspace on disk.
 * </p>
 * <p>
 * When the platform is later restarted, activating the Resources plug-in
 * opens the saved workspace. This reads into memory the workspace's
 * resource tree, plug-in save table, and saved resource tree snapshots 
 * (everything that was written to disk in the atomic operation above).
 * Later, when a plug-in gets
 * reactivated and registers to participate in workspace saves, 
 * it is handed back the info from its entry in the plug-in save table,
 * if it has one. It gets back the number of the last save in which it
 * actively participated and, possibly, a resource delta.
 * </p>
 * <p>
 * The only source of long term garbage would come from a plug-in that 
 * never gets reactivated, or one that gets reactivated but fails to
 * register for workspace saves. (There is no such problem with a plug-in
 * that gets uninstalled; its easy enough to scrub its state areas
 * and delete its entry in the plug-in save table.)
 * </p>
 *
 * @param full <code>true</code> if this is a full save, and 
 *   <code>false</code> if this is only a snapshot
 *   for protecting against crashes
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @return a status that may contain warnings, such as the failure
 *   of an individual participant
 * @exception CoreException if this method fails to save
 *   the state of this workspace. Reasons include:
 * <ul>
 *   <li>The operation cannot be batched with others.</li>
 * </ul>
 * @see #addSaveParticipant
 */
public IStatus save(boolean full, IProgressMonitor monitor) throws CoreException;
/**
 * Sets the workspace description. 
 *
 * @param description the new workspace description.
 * @see #getDescription
 * @exception CoreException if the method fails.
 * Reasons include:
 * <ul>
 * <li> There was a problem writing the description to disk.</li>
 * </ul>
 */
public void setDescription(IWorkspaceDescription description) throws CoreException;

/**
 * Sets the lock to use for controlling write access to this workspace. 
 * The lock must only be set once.
 * <p>
 * This method is for internal use by the platform-related plug-ins.  
 * Clients should not call this method.
 * </p>
 * @param lock the lock to install on this workspace.
 */
public void setWorkspaceLock(WorkspaceLock lock);
/**
 * Returns a copy of the given set of natures sorted in prerequisite order.  
 * For each nature, it is guaranteed that all of its prerequisites will 
 * preceed it in the resulting array.  
 * 
 * <p>Natures that are missing from the install or are involved in a 
 * prerequisite cycle are sorted arbitrarily.  Duplicate nature IDs are 
 * removed, so the returned array may be smaller than the original.
 * </p>
 * 
 * @param natureIds a valid set of nature extension identifiers
 * @return the set of nature Ids sorted in prerequisite order
 * @see #validateNatureSet
 * @since 2.0
 */
public String[] sortNatureSet(String[] natureIds); 
/**
 * <b>Note:</b> This method is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * Advises that the caller intends to modify the contents of the given files in the near future and
 * asks whether modifying all these files would be reasonable. The files must all exist.
 * This method is used to give the VCM component an opportunity to check out (or otherwise
 * prepare) the files if required. (It is provided in this component rather than in the UI so that
 * "core" (i.e., head-less) clients can use it. Similarly, it is located outside the VCM component 
 * for the convenience of clients that must also operate in configurations without VCM.)
 * </p>
 * <p>
 * A client (such as an editor) should perform a <code>validateEdit</code> on a file 
 * whenever it finds itself in the following position: (a) the file is marked read-only, 
 * and (b) the client believes it likely (not necessarily certain) that it will modify the 
 * file's contents at some point. A case in point is an editor that has a buffer opened 
 * on a file. When the user starts to dirty the buffer, the editor should check to see whether 
 * the file is read-only. If it is, it should call <code>validateEdit</code>, and can 
 * reasonably expect this call, when successful, to cause the file to become read-write. 
 * An editor should also be sensitive to a file becoming read-only again even after a successful 
 * <code>validateEdit</code> (e.g., due to the user checking in the file in a different view); 
 * the editor should again call <code>validateEdit</code> if the file is read-only before 
 * attempting to save the contents of the file.
 * </p>
 * <p>
 * By passing a UI context, the caller indicates that the VCM component may
 * contact the user to help decide how best to proceed. If no UI context is provided, the
 * VCM component will make its decision without additional interaction with the user.
 * If OK is returned, the caller can safely assume that all of the given files haven been prepared
 * for modification and that there is good reason to believe that <code>IFile.setContents</code>
 * (or <code>appendContents</code>) would be successful on any of them. If the result
 * is not OK, modifying the given files might not succeed for the reason(s) indicated.
 * </p>
 * <p>
 * If a shell is passed in as the context, the VCM component
 * may bring up a dialogs to query the user or report difficulties; the shell should be used to
 * parent any such dialogs; the caller may safely assume that the reasons for failure will have
 * been made clear to the user. If <code>null</code> is passed, the user should not be contacted;
 * any failures should be reported via the result; the caller may chose to present these to the
 * user however they see fit. The ideal implementation of this method is transactional; no files
 * would be affected unless the go-ahead could be given. (In practice, there may be no feasible
 * way to ensure such changes get done atomically.)
 * </p>
 * <p>
 * The method calls <code>IFileModificationValidator.validateEdit</code> for
 * the file modification validator (if provided by the VCM plug-in). When there is no file modification
 * validator, this method immediately return an OK status.
 * </p>
 * 
 * @param files the files that are to be modified; these files must all exist in the workspace
 * @param context the <code>org.eclipse.swt.widgets.Shell</code> that is to be used to
 *    parent any dialogs with the user, or <code>null</code> if there is no UI context (declared
 *   as an <code>Object</code> to avoid any direct references on the SWT component)
 * @return a status object that is OK if things are fine, otherwise a status describing
 *    reasons why modifying the given files is not a reasonable
 * @since 2.0
 */
public IStatus validateEdit(IFile[] files, Object context);
/**
 * Validates the given string as the name of a resource
 * valid for one of the given types.
 * <p>
 * In addition to the basic restrictions on paths in general 
 * (see <code>IPath.isValidSegment</code>),
 * a resource name must also obey the following rules:
 * <ul>
 * <li> it must not be empty
 * <li> it must not be a single period character (".")
 * <li> it must not contain two or more consecutive period characters
 * <li> it must not end in a period character
 * <li> it must not contain any characters or substrings that are not valid 
 *		   on the filesystem on which workspace root is located.
 * </ul>
 * </p>
 * <p>
 * This validation check is done automatically as a 
 * resource is created (but not when the resource handle is constructed);
 * this means that any resource that exists can be safely assumed to have
 * a valid name and path.  Note that the name of the workspace root resource
 * is inherently invalid.
 * </p>
 * 
 * @param segment the name segment to be checked
 * @param typeMask bitwise-or of the resource type constants (
 *		<code>FILE</code>, <code>FOLDER</code>,
 *		<code>PROJECT</code> or <code>ROOT</code>) indicating 
 *		expected resource type(s)
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given string is valid as a resource name, otherwise a status 
 *		object indicating what is wrong with the string
 * @see IResource#PROJECT
 * @see IResource#FOLDER
 * @see IResource#FILE
 * @see IStatus#OK
 */
public IStatus validateName(String segment, int typeMask);
/**
 * Validates that each of the given natures exists, and that all nature
 * constraints are satisfied within the given set.
 * <p>
 * The following conditions apply to validation of a set of natures:
 * <ul>
 * <li> all natures in the set exist in the plug-in registry
 * <li> all prerequisites of each nature are present in the set
 * <li> there are no cycles in the prerequisite graph of the set
 * <li> there are no two natures in the set that specify one-of-nature
 * 	inclusion in the same group.
 * <li> there are no two natures in the set with the same id
 * </ul>
 * </p>
 * <p>
 * An empty nature set is always valid.
 * </p>
 * @param natureIds an array of nature extension identifiers
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given set of natures is valid, otherwise a status 
 *		object indicating what is wrong with the set
 * @since 2.0
 */
public IStatus validateNatureSet(String[] natureIds); 
/**
 * Validates the given string as a path for a resource of the given type(s).
 * <p>
 * In addition to the restrictions for
 * paths in general (see <code>IPath.isValidPath</code>),
 * a resource path should also obey the following rules:
 * <ul>
 * <li> a resource path should be an absolute path with no device id
 * <li> its segments should be valid names according to <code>validateName</code>
 * <li> a path for the workspace root must be the canonical root path
 * <li> a path for a project should have exactly 1 segment
 * <li> a path for a file or folder should have more than 1 segment
 * <li> the first segment should be a valid project name
 * <li> the second through penultimate segments should be valid folder names
 * <li> the last segment should be a valid name of the given type
 * </ul>
 * </p>
 * <p>
 * Note: this method does not consider whether a resource at the
 * specified path exists.
 * </p>
 * <p>
 * This validation check is done automatically as a 
 * resource is created (but not when the resource handle is constructed);
 * this means that any resource that exists can be safely assumed to have
 * a valid name and path.
 * </p>
 *
 * @param path the path string to be checked
 * @param typeMask bitwise-or of the resource type constants (
 *		<code>FILE</code>, <code>FOLDER</code>, <code>PROJECT</code>,
 *		or <code>ROOT</code>) indicating expected resource type(s)
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given path is valid as a resource path, otherwise a status 
 *		object indicating what is wrong with the string
 * @see IResource#PROJECT
 * @see IResource#FOLDER
 * @see IResource#FILE
 * @see IStatus#OK
 * @see IResourceStatus#getPath
 */
public IStatus validatePath(String path, int typeMask);
/**
 * Validates the given path as the location of the given project on disk.
 * In addition to the restrictions for
 * paths in general (see <code>IPath.isValidPath</code>),
 * a location path should also obey the following rules:
 * <ul>
 * <li> must not overlap with another open or closed project
 * <li> must not overlap with the platform's working directory
 * </ul>
 * </p>
 * <p>
 * Note: this method does not consider whether files or directories exist in 
 * the filesystem at the specified path.
 * 
 * @param project the project to validate the location for
 * @param location the location of the project contents on disk
 * @return a status object with code <code>IStatus.OK</code> if
 *		the given location is valid as the project content location, otherwise a status 
 *		object indicating what is wrong with the string
 * @see IProjectDescription#getLocation
 * @see IProjectDescription#setLocation
 * @see IStatus#OK
 */
public IStatus validateProjectLocation(IProject project, IPath location);
}
