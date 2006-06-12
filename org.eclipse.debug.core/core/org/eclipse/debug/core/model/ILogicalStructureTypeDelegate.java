/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;

import org.eclipse.core.runtime.CoreException;

/**
 * A delegate that provides a value representing the logical structure of a raw
 * implementation value from a debug model. Associated with a logical structure
 * type extension.
 * <p>
 * Clients contributing logical structure types should implement this
 * interface.
 * </p>
 * @since 3.0
 * @see org.eclipse.debug.core.ILogicalStructureType
 */
public interface ILogicalStructureTypeDelegate {
	
	/**
	 * Returns whether this structure type can provide a logical structure for 
	 * the given value.
	 * 
	 * @param value value for which a logical structure is being requested
	 * @return whether this structure type can provide a logical structure for 
	 * the given value
	 */
	public boolean providesLogicalStructure(IValue value);
	
	/**
	 * Returns a value representing a logical structure of the given value.
	 * 
	 * @param value value for which a logical structure is being requested
	 * @return value representing logical structure
	 * @throws CoreException if an exception occurs generating a logical
	 *  structure
	 */
	public IValue getLogicalStructure(IValue value) throws CoreException;

}
