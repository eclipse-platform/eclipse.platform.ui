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

import org.eclipse.team.core.TeamException;

/**
 * 
 * Provides a complete set of IFileRevisions that make up this IFileHistory.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 *
 */
public interface IFileHistory {

	/**
	 * Returns the complete set of file revisions for this file (this includes all predecessors
	 * and all descendents)
	 * @return an array containing all of the file revisions for this particular file or
	 * an empty array if this file has no revisions
	 * @throws TeamException 
	 */
	public abstract IFileRevision[] getFileRevisions() throws TeamException;
	
	/**
	 * Returns the file revision that corresponds to the passed in content identifier.
	 * @param id the content identifier that uniquely identifies a file revision.
	 * @return the file revision corresponding to the passed in content id or null if no file revisions 
	 * match the given id
	 */
	public abstract IFileRevision getFileRevision(String id) throws TeamException;
	
	/**
	 * Returns the previous version of the passed in file revision.
	 * @param revision the current revision
	 * @return the previous version of the passed in revision or null if the revision had no 
	 * predecessor
	 */
	public abstract IFileRevision getPredecessor(IFileRevision revision) throws TeamException;
	
	/**
	 * Returns all of the direct descendents of the passed in file revision. 
	 * 
	 * @param revision the file revision that acts as the root
	 * @return an array containing all of the the descendents or an empty array if the revision has
	 * no direct descendents
	 */
	public abstract IFileRevision[] getDirectDescendents(IFileRevision revision) throws TeamException;
	
	//TODO: Methods for getting all direct children of file revision, all elements along a branch (predecessors, file revision and descendent)
}
