package org.eclipse.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.*;

/**
 * Interface for resources which may contain
 * other resources (termed its <em>members</em>). While the 
 * workspace itself is not considered a container in this sense, the
 * workspace root resource is a container.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * <p>
 * Containers implement the <code>IAdaptable</code> interface;
 * extensions are managed by the platform's adapter manager.
 * </p>
 *
 * @see Platform#getAdapterManager
 * @see IProject
 * @see IFolder
 * @see IWorkspaceRoot
 */
public interface IContainer extends IResource, IAdaptable {
/**
 * Returns whether a resource of some type with the given path 
 * exists relative to this resource.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.  Trailing separators are ignored.
 * If the path is empty this container is checked for existence.
 *
 * @param path the path of the resource
 * @return <code>true</code> if a resource of some type with the given path 
 *     exists relative to this resource, and <code>false</code> otherwise
 * @see IResource#exists
 */
public boolean exists(IPath path);
/**
 * Finds and returns the member resource (project, folder, or file)
 * with the given name in this container, or <code>null</code> if no such
 * resource exists.
 * 
 * <p> N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * resource existing at the calculated path in the workspace.
 * </p>
 *
 * @param name the string name of the member resource
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 */
public IResource findMember(String name);
/**
 * Finds and returns the member resource (project, folder, or file)
 * with the given name in this container, or <code>null</code> if 
 * there is no such resource.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only a member resource with the given name that exists will be returned.
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the method also returns a phantom member resource with 
 * the given name that the workspace is keeping track of.
 * </p>
 * <p>
 * N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * existing resource (or phantom) in the workspace.
 * </p>
 *
 * @param name the string name of the member resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 * @see #members(boolean)
 * @see IResource#isPhantom
 */
public IResource findMember(String name, boolean includePhantoms);
/**
 * Finds and returns the member resource identified by the given path in
 * this container, or <code>null</code> if no such resource exists.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.   Trailing separators are ignored.
 * If the path is empty this container is returned.
 * <p> N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * resource existing at the calculated path in the workspace.
 * </p>
 *
 * @param path the path of the desired resource
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 */
public IResource findMember(IPath path);
/**
 * Finds and returns the member resource identified by the given path in
 * this container, or <code>null</code> if there is no such resource.
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource.  Trailing separators are ignored.
 * If the path is empty this container is returned.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only a resource that exists at the given path will be returned.
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the method also returns a phantom member resource at the given path
 * that the workspace is keeping track of.
 * </p>
 * <p>
 * N.B. Unlike the methods which traffic strictly in resource
 * handles, this method infers the resulting resource's type from the
 * existing resource (or phantom) at the calculated path in the workspace.
 * </p>
 *
 * @param path the path of the desired resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest
 * @return the member resource, or <code>null</code> if no such
 * 		resource exists
 * @see #members(boolean)
 * @see IResource#isPhantom
 */
public IResource findMember(IPath path, boolean includePhantoms);
/**
 * Returns a handle to the file identified by the given path in this
 * container.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * <p>
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource and is appended
 * to this container's full path to form the full path of the resultant resource.
 * A trailing separator is ignored. The path resulting resource will 
 * have at least 3 segments.
 * </p>
 *
 * @param path the path of the member file
 * @return the (handle of the) member file
 * @see #getFolder
 */
public IFile getFile(IPath path);
/**
 * Returns a handle to the folder identified by the given path in this
 * container.
 * <p> 
 * This is a resource handle operation; neither the resource nor
 * the result need exist in the workspace.
 * The validation check on the resource name/path is not done
 * when the resource handle is constructed; rather, it is done
 * automatically as the resource is created.
 * <p>
 * The supplied path may be absolute or relative; in either case, it is
 * interpreted as relative to this resource and is appended
 * to this container's full path to form the full path of the resultant resource.
 * A trailing separator is ignored. The path of the resulting resource will 
 * have at least 2 segments.
 * </p>
 *
 * @param path the path of the member folder
 * @return the (handle of the) member folder
 * @see #getFile
 */
public IFolder getFolder(IPath path);
/**
 * Returns a list of existing member resources (projects, folders and files)
 * in this resource, in no particular order.
 * <p>
 * Note that the members of a project or folder are the files and folders
 * immediately contained within it.  The members of the workspace root
 * are the projects in the workspace.
 * </p>
 *
 * @return an array of members of this resource
 * @exception CoreException if this request fails. Reasons include:
 * <ul>
 * <li> This resource does not exist.</li>
 * <li> This resource is a project that is not open.</li>
 * </ul>
 * @see #findMember
 * @see IResource#isAccessible
 */
public IResource[] members() throws CoreException;
/**
 * Returns a list of all member resources (projects, folders and files)
 * in this resource, in no particular order.
 * <p>
 * If the <code>includePhantoms</code> argument is <code>false</code>, 
 * only member resources that exist will be returned.
 * If the <code>includePhantoms</code> argument is <code>true</code>,
 * the result will also includes any phantom member resources the
 * workspace is keeping track of.
 * </p>
 *
 * @return an array of members of this resource
 * @param includePhantoms <code>true</code> if phantom resources are
 *   of interest; <code>false</code> if phantom resources are not of
 *   interest.
 * @exception CoreException if this request fails. Reasons include:
 * <ul>
 * <li> <code>includePhantoms</code> is <code>false</code> and
 *     this resource does not exist.</li>
 * <li> <code>includePhantoms</code> is <code>false</code> and
 *     this resource is a project that is not open.</li>
 * </ul>
 * @see #members(boolean)
 * @see IResource#exists
 * @see IResource#isPhantom
 */
public IResource[] members(boolean includePhantoms) throws CoreException;
}
