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
package org.eclipse.core.internal.dependencies;

import java.util.Set;


/**
 * A policy that determines which elements from an element set will be picked during
 * the selection stage.
 * <p> 
 * Clients may implement.
 * </p>
 */
public interface ISelectionPolicy {
	/**
	 * Returns a set containing the selected elements for the given element set.  
	 */
	public Set selectMultiple(IElementSet elementSet);
	/**
	 * Returns the selected element for the given element set.  
	 */	
	public IElement selectSingle(IElementSet elementSet);
}
