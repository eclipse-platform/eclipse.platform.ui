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
package org.eclipse.team.core.sync;

import java.io.InputStream;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * Interface for resources that are not local. 
 */
public interface IRemoteResource extends IAdaptable {

	/**
	 * Answers a string that describes the name of the remote resource. The name may be
	 * displayed to the user.
	 * 
	 * @return name of the remote resource.
	 */
	public String getName();
	
	/**
	 * Answers and array of <code>IRemoteResource</code> elements that are immediate 
	 * children of this remote resource, in no particular order.
	 * 
 	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @return array of immediate children of this remote resource. 
	 */
	public IRemoteResource[] members(IProgressMonitor progress) throws TeamException;
	
	/**
	 * Returns a stream over the contents of this remote element.
	 * 
 	 * @param progress a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 */
	public InputStream getContents(IProgressMonitor progress) throws TeamException;
	
	/**
	 * Answers if the remote element may have children.
	 * 
	 * @return <code>true</code> if the remote element may have children and 
	 * <code>false</code> otherwise.
	 */
	public boolean isContainer();
}

