package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;

/**
 * A root resource represents the top of the resource hierarchy in a workspace.
 * There is exactly one root in a workspace.  The root resource has the following
 * behavior: 
 * <ul>
 * <li>It cannot be moved or copied </li>
 * <li>It always exists.</li>
 * <li>Deleting the root deletes all of the children under the root but leaves the root itself</li>
 * <li>It is always local.</li>
 * <li>It is never a phantom.</li>
 * </ul>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Workspace roots implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 */
public interface IWorkspaceRoot extends IContainer, IAdaptable {

/**
 * Deletes everything in the workspace except the workspace root resource
 * itself.
 * <p>
 * This is a convenience method, fully equivalent to:
 * <pre>
 *   delete((deleteContent ? DELETE_PROJECT_CONTENT : 0) | (force ? FORCE : 0), monitor);
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
 * @param deleteContent a flag controlling how whether content is
 *    aggressively deleted
 * @param force a flag controlling whether resources that are not
 *    in sync with the local file system will be tolerated
 * @param monitor a progress monitor, or <code>null</code> if progress
 *    reporting and cancellation are not desired
 * @exception CoreException if this method fails. Reasons include:
 * <ul>
 * <li> A project could not be deleted.</li>
 * <li> A project's contents could not be deleted.</li>
 * <li> Resource changes are disallowed during certain types of resource change 
 *       event notification. See IResourceChangeEvent for more details.</li>
 * </ul>
 * @see IResource#delete(int,IProgressMonitor)
 */
public void delete(boolean deleteContent, boolean force, IProgressMonitor monitor) throws CoreException;
/**
 * Returns a handle to the  workspace root, project or folder 
 * which is mapped to the given path
 * in the local file system, or <code>null</code> if none.
 * If the path maps to the platform working location, the returned object
 * will be of type <code>ROOT</code>.  If the path maps to a 
 * project, the resulting object will be
 * of type <code>PROJECT</code>; otherwise the resulting object 
 * will be a folder (type <code>FOLDER</code>).
 * The path must be absolute; its segments need not be valid names;
 * a trailing separator is ignored.
 * The resulting resource need not exist in the workspace.
 *
 * @param path a path in the local file system
 * @return the corresponding project or folder in the workspace,
 *    or <code>null</code> if none
 */
public IContainer getContainerForLocation(IPath path);
/**
 * Returns a handle to the file which is mapped to the given path 
 * in the local file system, or <code>null</code> if none.
 * The path must be absolute; its segments need not be valid names.
 * The resulting file need not exist in the workspace.
 * <p>
 *
 * @param path a path in the local file system
 * @return the corresponding file in the workspace,
 *    or <code>null</code> if none
 */
public IFile getFileForLocation(IPath filesystemPath);
/**
 * Returns a handle to the project resource with the given name
 * which is a child of this root.
 * <p>
 * Note: This method deals exclusively with resource handles, 
 * independent of whether the resources exist in the workspace.
 * The validation check on the project name is not done
 * when the project handle is constructed; rather, it is done
 * automatically as the project is created.
 * </p>
 * 
 * @param name the name of the project 
 * @return a project resource handle
 * @see #getProjects
 */
public IProject getProject(String name);
/**
 * Returns the collection of projects which exist under this root.
 * The projects can be open or closed.
 * 
 * @return an array of projects
 * @see #getProject
 */
public IProject[] getProjects();
}
