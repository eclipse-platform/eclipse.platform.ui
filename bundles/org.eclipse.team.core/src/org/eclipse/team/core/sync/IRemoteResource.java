/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.sync;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * 
 * Interface for resources that are not local. 
 * 
 * @since 2.0
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
	
	public String getComment() throws TeamException;
	
	public String getContentIdentifier() throws TeamException;
	
	public String getCreatorDisplayName() throws TeamException;
	
	/**
	 * Returns an IStorage that contains (or provides access to) the buffered 
	 * contents of the remote resource. Returns <code>null</code> if the remote
	 * resource does not have contents (i.e. is not a file).
	 * 
	 * @param monitor
	 * @return
	 * @throws TeamException
	 */
	public IStorage getBufferedStorage(IProgressMonitor monitor) throws TeamException;
}

