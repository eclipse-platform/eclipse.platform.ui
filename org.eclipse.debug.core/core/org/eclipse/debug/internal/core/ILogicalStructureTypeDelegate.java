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
package org.eclipse.debug.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IValue;

/**
 * Provides a value represengting the logical structure of a raw value provided
 * by a debug model. Logical structures are useful for navigating complex data
 * structures. Logical structures are contributed via extensions in plug-in XML.
 * 
 * TODO: example plug-in XML
 * 
 * @since 3.0
 */
public interface ILogicalStructureTypeDelegate {
	
	/**
	 * Returns whether this structure type can provide a logical structure for 
	 * the given value.
	 * 
	 * @param value value for which a logial structure is being requested
	 * @return whether this structure type can provide a logical structure for 
	 * the given value
	 */
	public boolean providesLogicalStructure(IValue value);
	
	/**
	 * Returns a value representing a logical view of the given value.
	 * 
	 * @param value value for which a logical structure is being requested
	 * @return value representing logical structure value
	 * @throws CoreException if an exception occurrs generating a logical
	 *  structure
	 */
	public IValue getLogicalStructure(IValue value) throws CoreException;

}
