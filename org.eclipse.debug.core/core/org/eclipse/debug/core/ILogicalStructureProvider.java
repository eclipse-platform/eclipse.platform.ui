/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core;

import org.eclipse.debug.core.model.IValue;

/**
 * A type that provides the logical structure types applicable to a raw
 * implementation value from a debug model. Associated with a logical structure
 * provider extension.
 * <p>
 * Clients contributing logical structure providers should implement this
 * interface.
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.core.ILogicalStructureType
 */
public interface ILogicalStructureProvider {
	
	/**
	 * Returns the logical structure types which are applicable to the given type.
	 * 
	 * @param value value for which logical structure types are being requested
	 * @return the logical structure types appli
	 */
	public ILogicalStructureType[] getLogicalStructureTypes(IValue value);

}
