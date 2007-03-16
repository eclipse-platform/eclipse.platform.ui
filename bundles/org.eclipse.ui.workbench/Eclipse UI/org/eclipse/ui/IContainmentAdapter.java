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
package org.eclipse.ui;

/**
 * This adapter interface provides a way to test element containment 
 * in a model-independent way.
 * The workbench uses this interface in certain views
 * to test if a given resource is part of a working set.
 * 
 * @since 2.1
 */
public interface IContainmentAdapter {
    /**
     * Checks whether the given element corresponds to the containment context.
     */
    public static final int CHECK_CONTEXT = 1;

    /**
     * Checks whether the given element corresponds to a direct child of the 
     * containment context.  Does not include the containment context itself.
     */
    public static final int CHECK_IF_CHILD = 2;

    /**
     * Checks whether the given element corresponds to an ancestor of the
     * containment context.  Does not include the containment context itself.
     */
    public static final int CHECK_IF_ANCESTOR = 4;

    /**
     * Checks whether the given element corresponds to a descendant of the 
     * containment context.  Does not include the containment context itself.
     */
    public static final int CHECK_IF_DESCENDANT = 8;

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
     *    <code>CHECK_IF_CHILD</code>, <code>CHECK_IF_ANCESTOR</code>, 
     *    <code>CHECK_IF_DESCENDENT</code> logically ORed together.
     */
    public boolean contains(Object containmentContext, Object element, int flags);
}
