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
package org.eclipse.jface.text;



/**
 * Describes a region of an indexed text store such as document or string.
 * The region consists of offset, length, and type. The type is defines as 
 * a string. A typed region can, e.g., be used to described document partitions.
 * Clients may implement this interface or use the standard impementation
 * <code>TypedRegion</code>.
 */
public interface ITypedRegion extends IRegion {
	
	/**
	 * Returns the content type of the region.
	 *
	 * @return the content type of the region
	 */
	String getType();
}
