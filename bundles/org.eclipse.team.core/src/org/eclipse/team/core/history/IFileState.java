/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.history;

import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.variants.FileState;

/**
 * A previous, current or proposed future state of a file.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients that want to define there own a file state can 
 * subclass {@link FileState} instead.
 * 
 * <p> <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IFileRevision
 * @see FileState
 * 
 * @since 3.2
 */
public interface IFileState {

	/**
	 * Returns the storage for this file revision.
	 * If the returned storage is an instance of
	 * <code>IFile</code> clients can assume that this
	 * file state represents the current state of
	 * the returned <code>IFile</code>.
	 * @return IStorage containing file storage 
	 */
	public IStorage getStorage(IProgressMonitor monitor) throws CoreException;

	/**
	 * Returns the name of the file to which this state is associated
	 * @return String containing the name of the file
	 */
	public String getName();

	/**
	 * Returns the URI of the file to which this state is associated
	 * or <code>null</code> if the file does not have a URI.
	 * @return URI of the file to which this state is associated
	 */
	public URI getURI();

	/**
	 * Returns the time stamp of this revision as a long or <code>-1</code>
	 * if the timestamp is unknown.
	 * 
	 * @return a long that represents the time of this revision as the number of milliseconds
	 * since the base time
	 *
	 * @see java.lang.System#currentTimeMillis()
	 */
	public long getTimestamp();

	/**
	 * Returns whether the file represented by this state exists.
	 * @return whether the file represented by this state exists
	 */
	public boolean exists();

}