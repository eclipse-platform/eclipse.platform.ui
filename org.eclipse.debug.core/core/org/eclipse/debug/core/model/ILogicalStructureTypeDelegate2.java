/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.core.model;


/**
 * Optional extension to <code>ILogicalStructureTypeDelegate</code> that allows
 * a logical structure type delegate to provide a description for a value.
 * This allows a logical structure type to provide logical structures and
 * descriptions for more than one type of value.
 * <p>
 * If a logical structure type delegate implements this interface, it will
 * be consulted for a description rather than using the description attribute
 * provided in plug-in XML.
 * </p>
 * <p>
 * Clients contributing logical structure types can implement this
 * interface.
 * </p>
 * @since 3.1
 * @see org.eclipse.debug.core.ILogicalStructureType
 */
public interface ILogicalStructureTypeDelegate2 {
	
	/**
	 * Returns a simple description of the logical structure provided by this
	 * structure type delegate, for the given value.
	 * Cannot return <code>null</code>. This method is only called if this
	 * logical structure type delegate returns <code>true</code> for
	 * <code>providesLogicalStructure(IValue)</code>.
	 * 
	 * @param value a value a description is requested for
	 * @return a simple description of the logical structure provided by this
	 * structure type delegate, for the given value
	 */
	public String getDescription(IValue value);

}
