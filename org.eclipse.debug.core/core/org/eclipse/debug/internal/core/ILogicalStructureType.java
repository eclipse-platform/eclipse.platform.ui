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


/**
 * Provides a value represengting the logical structure of a raw value provided
 * by a debug model. Logical structures are useful for navigating complex data
 * structures. Logical structures delegates are contributed via extensions in
 * plug-in XML.
 * 
 * @since 3.0
 */
public interface ILogicalStructureType extends ILogicalStructureTypeDelegate{
	
	/**
	 * Returns a simple description of the logical structure provided by this
	 * structure type.
	 * 
	 * @return a simple description of the logical structure provided by this
	 * structure type
	 */
	public String getDescription();

}
