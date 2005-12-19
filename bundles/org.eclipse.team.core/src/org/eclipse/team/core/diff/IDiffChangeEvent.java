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
package org.eclipse.team.core.diff;

import org.eclipse.core.runtime.IPath;

/**
 * A change event that describes changes that have occurred
 * in an {@link IDiffTree}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @since 3.2
 */
public interface IDiffChangeEvent {
	
	/**
	 * Returns the tree that has been changed.
	 * @return the tree that has been changed.
	 */
	public IDiffTree getTree();
	
	/**
	 * Returns the delta nodes that have been added to the delta tree.
	 * @return the delta nodes that have been added to the delta tree
	 */
	public IDiffNode[] getAdditions();
	
	/**
	 * Return the paths of the delta nodes that have been removed from the delta tree.
	 * @return the paths of the delta nodes that have been removed from the delta tree
	 */
	public IPath[] getRemovals();
	
	/**
	 * Return the delta nodes contained in the delta tree that have changed in some way.
	 * @return the delta nodes contained in the delta tree that have changed
	 */
	public IDiffNode[] getChanges();
}
