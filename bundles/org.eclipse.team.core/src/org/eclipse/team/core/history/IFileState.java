/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.history;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A previous, current or proposed future state of a file.
 * 
 * <p> <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IFileRevision
 * 
 * @deprecated use {@link IFileRevision} instead. W2ill be removed before 3.2 M5
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

}