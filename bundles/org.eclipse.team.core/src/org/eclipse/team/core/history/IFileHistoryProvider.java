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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.provider.FileHistoryProvider;

/**
 * This is API to access individual file histories.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients can
 * instead subclass {@link FileHistoryProvider}
 * 
 * <p><strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 *
 */
public interface IFileHistoryProvider {

	/**
	 * Returns the file history for the passed in resource
	 * 
	 * @param resource
	 * @param monitor 
	 * @return the history of the file
	 */
	public abstract IFileHistory getFileHistoryFor(IResource resource, IProgressMonitor monitor);
	

	/**
	 * Returns the file revision of the passed in resourrce or null if that file revision cannot be
	 * determined
	 * 
	 * @param resource
	 * @return the file revision belonging to the passed in resource or null
	 */
	public abstract IFileRevision getWorkspaceFileRevision(IResource resource);

}
