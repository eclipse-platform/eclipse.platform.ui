package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;
import java.util.Map;

/**
 * A project is a type of resource which groups resources
 * into buildable, reusable units.  
 * <p>
 * Features of projects include:
 * <ul>
 * <li>A project collects together a set of files and folders.</li>
 * <li>A project's location controls where the project's resources are 
 *		stored in the local file system.</li>
 * <li>A project's build spec controls how building is done on the project.</li>
 * <li>A project can carry session and persistent properties.</li>
 * <li>A project can be open or closed; a closed project is
 * 		passive and has a minimal in-memory footprint.</li>
 * <li>A project can carry references to other projects.</li>
 * <li>A project can have one or more project natures.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Projects implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 */
public interface IProject extends IContainer, IAdaptable {
/**
 * Invokes the <code>build</code> method of the specified builder 
 * for this project. Does nothing if this project is closed.
 * <p>
 * The builder name is declared in the extension that plugs in
 * to the standard <code>org.eclipse.core.resources.builders</code> 
 * extension point.  The arguments are builder specific.
 * </p>
 * <p>
 * This method may change resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param kind the kind of build being requested. Valid values are:
 *		<ul>
 *		<li> <code>IncrementalProjectBuilder.FULL_BUILD</code> - indicates a full build.</li>
 *		<li> <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> - indicates an incremental build.</li>
 *		</ul>
 * @param builderName the name of the builder
 * @param args a table of builder-specific arguments keyed by argument name
 *		(key type: <code>String</code>, value type: <code>String</code>);
 *		<code>null</code> is equivalent to an empty map
 * @param monitor a progress monitor, or <code>null</code> if progress
 *		reporting and cancellation are not desired
 * @exception CoreException if the build fails.
 *		The status contained in the exception may be a generic <code>BUILD_FAILED</code>
 *		code, but it could also be any other status code; it might
 *		also be a multi-status.
 * 
 * @see IProjectDescription
 * @see IncrementalProjectBuilder#build
 * @see IncrementalProjectBuilder#FULL_BUILD
 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
 */
public void build(int kind, String builderName, Map args, IProgressMonitor monitor) throws CoreException;
/** 
 * Builds this project. Does nothing if the project is closed.
 * <p>
 * Building a project involves executing the commands found
 * in this project's build spec.
 * </p>
 * <p>
 * This method may change resources; these changes will be reported
 * in a subsequent resource change event.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param kind the kind of build being requested. Valid values are:
 *		<ul>
 *		<li> <code>IncrementalProjectBuilder.FULL_BUILD</code> - indicates a full build.</li>
 *		<li> <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> - indicates an incremental build.</li>
 *		</ul>
 * @param monitor a progress monitor, or <code>null</code> if progress
 *		reporting and cancellation are not desired
 * @exception CoreException if the build fails.
 *		The status contained in the exception may be a generic <code>BUILD_FAILED</code>
 *		code, but it could also be any other status code; it might
 *		also be a multi-status.
 *
 * @see IProjectDescription
 * @see IncrementalProjectBuilder#FULL_BUILD
 * @see IncrementalProjectBuilder#INCREMENTAL_BUILD
 */
public void build(int kind, IProgressMonitor monitor) throws CoreException;
/**
 * Closes this project.  The project need not be open.  Closing
 * a closed project does nothing.
 * <p>
 * Closing a project involves ensuring that all important project-related
 * state is safely stored on disk, and then discarding the in-memory
 * representation of its resources and other volatile state, 
 * including session properties.
 * After this method, the project continues to exist in the workspace
 * but its member resources (and their members, etc.) do not.  
 * A closed project can later be re-opened.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that includes
 * an indication that this project has been closed and its members
 * have been removed.  
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param monitor a progress monitor, or <code>null</code> if progress
 *		reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> Resource changes are disallowed during resource change event
 *      notification.</li>
 * </ul>
 * @see #open
 * @see #isOpen
 */
public void close(IProgressMonitor monitor) throws CoreException;
/**
 * Makes a copy of this project using the given description.  The description 
 * specifies the name, location and attributes of the new project.  The description
 * of this project is ignored.  The project's descendents are copied as well. 
 * After successful completion, corresponding new resources will exist 
 * at the destination project path; their contents and properties will be copies of 
 * the originals. The original resources are not affected.
 * <p>
 * When a resource is copied, its persistent properties are 
 * copied with it. Session properties and markers are not copied.
 * </p>
 * <p>
 * The <code>force</code> parameter controls how this method deals with
 * cases where the workspace is not completely in sync with the local file system.
 * If <code>false</code> is specified, the method will only attempt
 * to copy resources that are in sync with the corresponding files and
 * directories in the local file system; it will fail if it
 * encounters a resource that is out of sync with the file system.
 * However, if <code>true</code> is specified, the method
 * copies all corresponding files and directories from the local
 * file system, including ones that have been recently updated or created.
 * Note that in both settings of the <code>force</code> parameter,
 * the operation fails if the newly created resources in the 
 * workspace would be out of sync with the local file system; 
 * this ensures files in the file system cannot be accidentally
 * overwritten.
 * </p>
 * <p> 
 * This operation changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resource copy has been added to its new parent.
 * </p>
 * <p>
 * This operation is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param description the project description for the destination
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this resource could not be copied. Reasons include:
 * <ul>
 * <li> This resource is not accessible.</li>
 * <li> This resource or one of its descendents is not local.</li>
 * <li> This resource or one of its descendents is out of sync with the local file
 *      system and <code>force</code> is <code>false</code>.</li>
 * <li> The workspace and the local file system are out of sync
 *      at the destination resource or one of its descendents.</li>
 * <li> Resource changes are disallowed during resource change event notification.</li>
 * </ul>
 */
public void copy(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException;
/**
 * Creates a new project resource in the workspace
 * using the given project description.
 * Upon successful completion, the project will exist but be closed.
 * <p>
 * The given project description is used to initialize the new project's
 * location, natures, build spec, comment, and referenced projects.
 * The project has no session or persistent properties.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the project has been added to the workspace.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param description the project description
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project already exists in the workspace.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The project location is not valid (according to
 *      <code>IWorkspace.validateProjectLocation</code>).</li>
 * <li> Resource changes are disallowed during resource change event 
 *      notification.</li>
 * </ul>
 *
 * @see IWorkspace#validateProjectLocation
 */
public void create(IProjectDescription description, IProgressMonitor monitor) throws CoreException;
/**
 * Creates a new project resource in the workspace.
 * Upon successful completion, the project will exist but be closed.
 * <p>
 * The project is initialized as follows:
 * <ul>
 * <li> has no resources</li>
 * <li> has no references to other projects</li>
 * <li> has no natures</li>
 * <li> has no session or persistent properties</li>
 * <li> has an empty build spec</li>
 * <li> has an empty comment</li>
 * <li> has the default location</li>
 * </ul>
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this project has been added to the workspace.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project already exists in the workspace.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The project location is not valid (according to
 *      <code>IWorkspace.validateProjectLocation</code>).</li>
 * <li> Resource changes are disallowed during resource change event 
 *      notification.</li>
 * </ul>
 *
 * @see IWorkspace#validateProjectLocation
 */
public void create(IProgressMonitor monitor) throws CoreException;
/**
 * Deletes this project from the workspace.
 * No action is taken if this project does not exist.
 * <p>
 * This method can be expressed as a combination of calls to 
 * <code>IResource.delete</code>, <code>IProject.open</code>,
 * and <code>IProject.close</code>.  Deleting projects does not 
 * keep a local history of the deleted resources.
 * </p>
 * <p>
 * If <code>deleteContent</code> is <code>true</code>
 * the project will be opened if necessary and then deleted
 * using <code>IResource.delete(force, monitor)</code>.
 * This has the effect of deleting the project's resources
 * from the project's local content area.
 * If <code>force</code> is <code>true</code>, 
 * a "best effort" will be made to delete the project's content from
 * the local file system; even if this is not entirely successful, 
 * the project itself will be deleted.
 * If <code>force</code> is <code>false</code>, 
 * the project's content will be deleted in the normal careful
 * fashion; only if this is entirely successful will the project
 * itself be deleted.
 * </p>
 * <p>
 * If <code>deleteContent</code> is <code>false</code>
 * the project will be closed and then deleted.
 * This has the effect of retaining the project's resources
 * in the project's local content area.
 * </p>
 * <p>
 * Deleting a project which has sync information does not
 * convert the project to a phantom resource. The project and
 * any sync information that it may have are removed from
 * the workspace tree.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param deleteContent a flag controlling how whether content is
 *    aggressively deleted
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project could not be deleted.</li>
 * <li> This project's contents could not be deleted.</li>
 * <li> Resource changes are disallowed during resource change event 
 *      notification.</li>
 * </ul>
 * @see IResource#delete
 * @see #open
 * @see #close
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException;
/**
 * Returns the description for this project.
 * The returned value is a copy and cannot be used to modify 
 * this project.  The returned value is suitable for use in creating, 
 * copying and moving other projects.
 *
 * @return the description for this project
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project does not exist.</li>
 * <li> This project is not open.</li>
 * </ul>
 * @see #create
 * @see #copy
 * @see #move 
 */
public IProjectDescription getDescription() throws CoreException;
/**
 * Returns a handle to the file with the given name in this project.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * </p>
 *
 * @param name the string name of the member file
 * @return the (handle of the) member file
 * @see #getFolder
 */
public IFile getFile(String name);
/**
 * Returns a handle to the folder with the given name in this project.
 * <p> 
 * This is a resource handle operation; neither the container
 * nor the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * </p>
 *
 * @param name the string name of the member file
 * @return the (handle of the) member file
 * @see #getFile
 */
public IFolder getFolder(String name);
/** 
 * Returns the specified project nature for this project or <code>null</code> if
 * the project nature has not been added to this project.
 * Clients may downcast to a more concrete type for more nature-specific methods.
 * The documentation for a project nature specifies any such additional protocol.
 * <p>
 * This may cause the plug-in that provides the given nature to be activated.
 * </p>
 *
 * @param natureId the nature extension identifier
 * @return the project nature object
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project does not exist.</li>
 * <li> This project is not open.</li>
 * <li> The project nature extension could not be found.</li>
 * </ul>
 */
public IProjectNature getNature(String natureId) throws CoreException;
/**
 * Returns the location in the local file system of the project-specific
 * working data area for use by the given plug-in or <code>null</code>
 * if the project does not exist.
 * The content, structure, and management of this area is 
 * the responsibility of the plug-in.  This area is deleted when the
 * project is deleted.
 * This project needs to exist but does not need to be open.
 *
 * @param plugin the plug-in
 * @return a local file system path
 */
public IPath getPluginWorkingLocation(IPluginDescriptor plugin);
/**
 * Returns the projects referenced by this project.
 * The returned projects need not exist in the workspace.
 * The result will not contain duplicates. Returns an empty
 * array if there are no referenced projects.
 *
 * @return a list of projects
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project does not exist.</li>
 * <li> This project is not open.</li>
 * </ul>
 */
public IProject[] getReferencedProjects() throws CoreException;
/**
 * Returns the list of all open projects which reference
 * this project. This project may or may not exist. Returns
 * an empty array if there are no referencing projects.
 *
 * @return a list of open projects referencing this project
 */
public IProject[] getReferencingProjects();
/** 
 * Returns whether the project nature specified by the given
 * nature extension id has been added to this project. 
 *
 * @param natureId the nature extension identifier
 * @return <code>true</code> if the project has the given nature 
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project does not exist.</li>
 * <li> This project is not open.</li>
 * </ul>
 */
public boolean hasNature(String natureId) throws CoreException;
/**
 * Returns whether this project is open.
 * <p>
 * A project must be opened before it can be manipulated.
 * A closed project is passive and has a minimal memory
 * footprint; a closed project has no members.
 * </p>
 *
 * @return <code>true</code> if this project is open, <code>false</code> if
 *		this project is closed or does not exist
 * @see #open
 * @see #close
 */
public boolean isOpen();
/**
 * Renames this project so that it is located at the name in 
 * the given description.  After successful completion, the 
 * project and any direct or indirect members will 
 * no longer exist; but corresponding new resources 
 * will now exist at the location specified by the given description.
 * <p>
 * When a resource moves, its session and persistent properties move
 * with it. Likewise for all the other attributes of the resource including
 * markers.
 * </p>
 * <p>
 * If this project's location is the default location, the directories and files 
 * on disk are moved to be in the location specified by the given description.
 * If the given description specifies the default location for the project,
 * the directories and files are moved to the default location.  In all other
 * cases the directories and files on disk are left untouched.
 * </p>
 * <p>
 * If the name in the given description is the same as this project's name and
 * the location is different, then the project contents will be moved to the new
 * location. All other parts of the given description are ignored.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that will include 
 * an indication that the resource has been removed from its parent
 * and that a corresponding resource has been added to its new parent.
 * Additional information provided with resource delta shows that these
 * additions and removals are related.
 * </p>
 * <p>
 * The <code>force</code> parameter controls how this method deals with
 * cases where the workspace is not completely in sync with the local file system.
 * If <code>false</code> is specified, the method will only attempt
 * to move resources that are in sync with the corresponding files and
 * directories in the local file system; it will fail if it
 * encounters a resource that is out of sync with the file system.
 * However, if <code>true</code> is specified, the method
 * moves all corresponding files and directories from the local
 * file system, including ones that have been recently updated or created.
 * Note that in both settings of the <code>force</code> parameter,
 * the operation fails if the newly created resources in the 
 * workspace would be out of sync with the local file system; 
 * this ensures files in the file system cannot be accidentally
 * overwritten.
 * </p>
 * <p>
 * Moving projects does not keep a local history of moved resources.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param description the description for the destination project
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this resource could not be moved. Reasons include:
 * <ul>
 * <li> This resource is not accessible.</li>
 * <li> This resource or one of its descendents is not local.</li>
 * <li> This resource or one of its descendents is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> The workspace and the local file system are out of sync
 *      at the destination resource or one of its descendents.</li>
 * <li> Resource changes are disallowed during resource change event notification.</li>
 * </ul>
 * @see IResourceDelta#getFlags
 */
public void move(IProjectDescription description, boolean force, IProgressMonitor monitor) throws CoreException;
/**
 * Opens this project.  No action is taken if the project is already open.
 * <p>
 * Opening a project constructs an in-memory representation 
 * of its resources from information stored on disk.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event that includes
 * an indication that the project has been opened and its resources
 * have been added to the tree.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor.
 * </p>
 *
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> Resource changes are disallowed during resource change event 
 *    notification.</li>
 * </ul>
 * @see #close
 */
public void open(IProgressMonitor monitor) throws CoreException;
/**
 * Changes this project resource to match the given project
 * description. This project should exist and be open.
 * <p>
 * The given project description is used to change the project's
 * natures, build spec, comment, and referenced projects.
 * The name and location of a project cannot be changed; these settings
 * in the project description are ignored.
 * The project's session and persistent properties are not affected.
 * </p>
 * <p>
 * If the new description includes nature ids of natures that the project
 * did not have before, these natures will be configured in automatically, which involves
 * instantiating the project nature and calling <code>IProjectNature.configure</code>
 * on it. An internal reference to the nature object is retained, and will be
 * returned on subsequent calls to <code>getNature</code> for the specified
 * nature id. Similarly, any natures the project had which are no longer required 
 * will be automatically deconfigured by calling <code>IProjectNature.deconfigure</code>
 * on the nature object and letting go of the internal reference to it.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the project's content has changed.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 *
 * @param description the project description
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This project does not exist in the workspace.</li>
 * <li> This project is not open.</li>
 * <li> The project nature extension could not be found.</li>
 * <li> Resource changes are disallowed during resource change event 
 *      notification.</li>
 * </ul>
 *
 * @see #getDescription
 * @see IProjectNature#configure
 * @see IProjectNature#deconfigure
 */
public void setDescription(IProjectDescription description, IProgressMonitor monitor) throws CoreException;
}
