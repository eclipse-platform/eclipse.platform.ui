/************************************************************************
Copyright (c) 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui;

/**
 * This adapter interface provides a way to test element containment.
 * The workbench uses this interface in the Navigator and Tasks views
 * to test if a given resource is part of a working set.
 * 
 * @since 2.1
 */
public interface IContainmentAdapter {
	
/**
 * Returns whether the given element is considered contained 
 * in the specified containment context or if it is the context 
 * itself.
 *
 * @param containmentContext object that provides containment 
 * 	context for the element. This is typically a container object 
 * 	(e.g., IFolder) and may be the element object itself. 
 * @param element object that should be tested for containment
 * @param includeDescendents whether to consider descendents of the 
 * 	container elements. Direct children are always tested.
 * @param includeAncestors whether to consider ancestors of the 
 * 	container.
 */
boolean contains(Object containmentContext, Object element, boolean includeDescendents, boolean includeAncestors);
}
