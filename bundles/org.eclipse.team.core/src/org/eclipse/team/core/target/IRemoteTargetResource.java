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
package org.eclipse.team.core.target;

import java.net.URL;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.sync.IRemoteResource;

/**
 * Interface for target resources that are not local. This is a handle to a
 * clients-side 'proxy' for the server resource. There are no guarantees that
 * the handle is not stale or invalid.
 * <p>
 * Use <code>exists()</code> to verify is the associated server resource
 * exists.
 * </p>
 * 
 * @see IRemoteResource
 */
public interface IRemoteTargetResource extends IRemoteResource {
	/**
	 * Returns the URL of this remote resource.
	 */
	public URL getURL() throws TeamException;
	
	/**
	 * Returns the size of the resource. 
	 */
	public int getSize() throws TeamException;
	
	/**
	 * Returns the last modified time
	 */
	public String getLastModified() throws TeamException;
	
	/**
	 * Return a boolean value indicating whether or not this resource exists on the
	 * remote server.
	 */
	public boolean exists() throws TeamException;
}
