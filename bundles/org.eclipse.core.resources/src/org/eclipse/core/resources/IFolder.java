package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;

/**
 * Folders may be leaf or non-leaf resources and may contain files and/or other folders.
 * A folder resource is stored as a directory in the local file system.
 * <p>
 * Folders, like other resource types, may exist in the workspace but
 * not be local; non-local folder resources serve as placeholders for
 * folders whose properties have not yet been fetched from a repository.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Folders implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 */
public interface IFolder extends IContainer, IAdaptable {
/**
 * Creates a new folder resource as a member of this handle's parent resource.
 * <p>
 * The <code>force</code> parameter controls how this method deals with
 * cases where the workspace is not completely in sync with the local file system.
 * If <code>false</code> is specified, this method will only attempt
 * to create a directory in the local file system if there isn't one already. 
 * This option ensures there is no unintended data loss; it is the recommended setting.
 * However, if <code>true</code> is specified, this method will 
 * be deemed a success if if there already is a corresponding directory.
 * </p>
 * <p>
 * This method synchronizes this resource with the local file system.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that the folder has been added to its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param force a flag controlling how to deal with resources that
 *    are not in sync with the local file system
 * @param local a flag controlling whether or not the folder will be local
 *    after the creation
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> This resource already exists in the workspace.</li>
 * <li> The workspace contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The parent of this resource does not exist.</li>
 * <li> The parent of this resource is a project that is not open.</li>
 * <li> The parent contains a resource of a different type 
 *      at the same path as this resource.</li>
 * <li> The name of this resource is not valid (according to 
 *    <code>IWorkspace.validateName</code>).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a file (as opposed to a directory).</li>
 * <li> The corresponding location in the local file system is occupied
 *    by a folder and <code>force </code> is <code>false</code>.</li>
 * <li> Resource changes are disallowed during resource change event 
 *    notification.</li>
 * </ul>
 */
public void create(boolean force, boolean local, IProgressMonitor monitor) throws CoreException;
/**
 * Deletes this resource from the workspace.
 * This method has the same behavior of <code>IResource.delete</code>
 * plus the <code>keepHistory</code> parameter indicating whether or not 
 * files under this folder should have their current contents stored 
 * in the workspace's local history.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this folder has been removed from its parent.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
  * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 *
 * @see IResource#delete
 */
public void delete(boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;
/**
 * Returns a handle to the file with the given name in the
 * receiver.
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
 * Returns a handle to the folder with the given name in the
 * receiver.
 * <p> 
 * This is a resource handle operation; neither the container
 * nor the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * </p>
 *
 * @param name the string name of the member folder
 * @return the (handle of the) member folder
 * @see #getFile
 */
public IFolder getFolder(String name);
/**
 * Moves this resource so that it is located at the given path.  
 * This method has the same behavior as <code>IResource.move</code>
 * plus the <code>keepHistory</code> parameter indicating whether or not 
 * the to-be-moved files under this folder should have their current contents
 * stored in the workspace's local history.
 * </p>
 * <p>
 * This method changes resources; these changes will be reported
 * in a subsequent resource change event, including an indication 
 * that this folder has been removed from its parent and a new folder
 * has been added to the parent of the destination.
 * </p>
 * <p>
 * This method is long-running; progress and cancellation are provided
 * by the given progress monitor. 
 * </p>
 * 
 * @param destination the destination path 
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param keepHistory a flag controlling whether files under this folder
 *    should be stored in the workspace's local history
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this resource could not be moved. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource or one of its descendents is not local.</li>
 * <li> The resource corresponding to the parent destination path does not exist.</li>
 * <li> The resource corresponding to the parent destination path is a closed 
 *      project.</li>
 * <li> A resource at destination path does exist.</li>
 * <li> A resource of a different type exists at the destination path.</li>
 * <li> This resource or one of its descendents is out of sync with the local file system
 *      and <code>force</code> is <code>false</code>.</li>
 * <li> The workspace and the local file system are out of sync
 *      at the destination resource or one of its descendents.</li>
 * <li> Resource changes are disallowed during resource change event notification.</li>
 * </ul>
 *
 * @see IResource#move
 */
public void move(IPath destination, boolean force, boolean keepHistory, IProgressMonitor monitor) throws CoreException;
}
