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
public interface IFileRevision extends IFileState {

	/**
	 * Returns the <em>unique</em> identifier for this file revision 
	 * @return a String containing the id
	 */
	public abstract String getContentIndentifier();

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
}
