/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.core.target;

import java.net.URL;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * Interface for target resources that are not local. This is a handle to a
 * clients-side 'proxy' for the server resource. There are no guarantees that
 * the handle is not stale or invalid.
 * <p>
 * Use <code>exists()</code> to verify is the associated server resource
 * exists.</p>
 * <p>
 * Methods that take progress monitors are expected to be long running and
 * may contact the server. Progress and cancellation will be provided. Clients 
 * can assume that methods that don't take progress monitors are responsive 
 * and won't contact the server.
 * </p>
 * 
 * @see IRemoteResource
 */
public interface IRemoteTargetResource extends IRemoteResource {
	/**
	 * Returns the URL of this remote resource.
	 */
	public URL getURL();
	
	/**
	 * Returns the size of the resource. 
	 */
	public int getSize(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Returns the last modified time
	 */
	public String getLastModified(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Return a boolean value indicating whether or not this resource exists on the
	 * remote server.
	 */
	public boolean exists(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Creates the directory named by this URL, including any necessary but non-existant
	 * parent directories.
	 */
	public void mkdirs(IProgressMonitor monitor) throws TeamException;
	
	/**
 	 * Returns a handle to the remote file identified by the given path in this
 	 * folder.
	 * <p> 
	 * This is a remote resource handle operation; neither the resource nor
	 * the result need exist on the server.</p>
	 * <p>
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource and is appended
	 * to this container's full path to form the full path of the resultant resource.
	 * A trailing separator is ignored.
	 * </p>
	 *
	 * @param name the path of the remote member file
	 * @return the (handle of the) remote file
	 * @see #getFolder
	 */
	public IRemoteTargetResource getFile(String name);

	/**
 	 * Returns a handle to the remote folder identified by the given path in this
 	 * folder.
	 * <p> 
	 * This is a remote resource handle operation; neither the resource nor
	 * the result need exist on the server.</p>
	 * <p>
	 * The supplied path may be absolute or relative; in either case, it is
	 * interpreted as relative to this resource and is appended
	 * to this container's full path to form the full path of the resultant resource.
	 * A trailing separator is ignored.
	 * </p>
	 *
	 * @param path the path of the remote member file
	 * @return the (handle of the) remote file
	 * @see #getFolder
	 */
	public IRemoteTargetResource getFolder(String name);
	
	/**
	 * Return the site where this remote resource exists
	 */
	public Site getSite();
}
