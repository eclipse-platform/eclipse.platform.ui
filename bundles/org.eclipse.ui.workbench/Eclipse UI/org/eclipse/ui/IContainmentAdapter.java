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
	 * Only the containment context and direct children should be
	 * considered when testing for element containment.
	 * (value is 1)
	 * 
	 * @see #contains
	 */
	public static final int CHECK_CONTEXT = 1;
	/**
	 * Check all descendents of the containment context when testing
	 * for element containment. Recurse into children.
	 * (value is 2)
	 * 
	 * @see #contains
	 */
	public static final int CHECK_DESCENDENTS = 2;
	/**
	 * Check all ancestors of the containment context when testing
	 * for element containment.
	 * (value is 4)
	 * 
	 * @see #contains
	 */
	public static final int CHECK_ANCESTORS = 4;
 	
/**
 * Returns whether the given element is considered contained 
 * in the specified containment context or if it is the context 
 * itself.
 *
 * @param containmentContext object that provides containment 
 * 	context for the element. This is typically a container object 
 * 	(e.g., IFolder) and may be the element object itself. 
 * @param element object that should be tested for containment
 * @param flags one or more of <code>CHECK_CONTEXT</code>, 
 * 	<code>CHECK_ANCESTORS</code>, <code>CHECK_DESCENDENTS</code>
 *	logically ORed together..
 *	<code>CHECK_CONTEXT</code> always has to be specified if the 
 *	context itself and its direct children should be checked,
 */
public boolean contains(Object containmentContext, Object element, int flags);
}
