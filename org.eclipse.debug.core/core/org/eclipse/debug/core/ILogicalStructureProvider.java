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
 * Provides logical structure types applicable to a raw implementation value from
 * a debug model. Associated with a logical structure provider extension.
 * <p>
 * The following is an example of a logical structure provider extension:
 * <pre>
 *  <extension point="org.eclipse.debug.core.logicalStructureProviders">
 *   <logicalStructureProvider
 *    class="com.example.ExampleLogicalStructureProvider"
 *    modelIdentifier="com.example.debug.model">
 *   </logicalStructureProvider>
 * </extension>
 * </pre>
 * </p>
 * In the example above, the specified logical structure provider will be consulted for
 * alternative logical structures for values from the <code>com.example.debug.model</code>
 * debug model as they are displayed in the variables view.
 * </p>
 * <p>
 * Clients contributing logical structure providers must implement this
 * interface.
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.core.ILogicalStructureType
 */
public interface ILogicalStructureProvider {
	
	/**
	 * Returns the logical structure types which are applicable to the given value.
	 * 
	 * @param value value for which logical structure types are being requested
	 * @return the logical structure types which are applicable to the given value
	 */
	public ILogicalStructureType[] getLogicalStructureTypes(IValue value);

}
