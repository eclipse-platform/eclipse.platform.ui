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

package org.eclipse.team.core.filehistory;

import java.net.URI;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Represents an individual revision of a file
 * <p> <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IFileRevision {

	/**
	 * Returns the storage for this file revision.
	 * @return IStorage containing file storage 
	 */
	public abstract IStorage getStorage(IProgressMonitor monitor);

	/**
	 * Returns the name of this file revision
	 * @return String containing the name of the file revision
	 */
	public abstract String getName();
	
	/**
	 * Returns the <em>unique</em> identifier for this file revision 
	 * @return a String containing the id
	 */
	public abstract String getContentIndentifier();

	/**
	 * Returns the URI for this file revision
	 * @return URI
	 */
	public abstract URI getURI();

	/**
	 * Returns the time stamp of this revision as a long.
	 * 
	 * @return a long that represents the time of this revision as the number of milliseconds
	 * since the base time
	 *
	 * @see java.lang.System#currentTimeMillis()
	 */
	public abstract long getTimestamp();

	/**
	 *  Returns a String containing the author of this revision
	 *  
	 *  @return String representing the author of this revision or an empty string if no author is associated with this string
	 */
	public abstract String getAuthor();

	/**
	 * Returns the comment for this file revision
	 * 
	 * @return String containing the comment or a an empty string if no comment exists
	 */
	public abstract String getComment();

	/**
	 * Returns the set of ITags available for this file revision.
	 * 
	 * @return an array of ITag's if ITags exist for this revision or an empty ITag array
	 * if no tags exist
	 */
	public abstract ITag[] getTags();
	
	/**
	 * Returns whether this particular revision represents a deletion of the file
	 * @return true if this revision is a deletion of the file; false otherwise
	 */
	public abstract boolean isDeletion();
}
