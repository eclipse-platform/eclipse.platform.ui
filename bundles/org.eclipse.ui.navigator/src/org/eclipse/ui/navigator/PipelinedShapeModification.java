/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.navigator;

import java.util.Set;

/**
 * 
 * Indicates how a shape modification should be transformed when applied to the
 * tree. Clients use {@link PipelinedShapeModification} as the input and return
 * type from intercept methods on {@link IPipelinedTreeContentProvider}.
 * 
 * <p>
 * Overriding extensions should use these to map attempts to directly modify the
 * tree down to the overridden model. A shape modification can either be an
 * <i>add</i> or <i>remove</i> shape modification, and the type is determined by
 * the context of its use. If supplied to an <code>interceptRemove</code>
 * method, then it is a remove shape modification, otherwise if supplied to an
 * <code>interceptAdd</code> method, then it is an add shape modification.
 * </p>
 * 
 * 
 * @since 3.2
 * 
 */
public final class PipelinedShapeModification {

	private Object parent;

	private final Set children;

	/**
	 * Create a shape modification. The given parent and children will be set as
	 * the initial values for the shape modification.
	 * 
	 * @param aParent
	 *            The parent for the add or remove call to the tree.
	 * @param theChildren
	 *            The children that should be added or removed from the tree.
	 */
	public PipelinedShapeModification(Object aParent, Set theChildren) {
		parent = aParent;
		children = theChildren;
	}

	/**
	 * 
	 * @return The parent to use for the shape modification.
	 */
	public final Object getParent() {
		return parent;
	}

	/**
	 * 
	 * @param aParent
	 *            The parent to use for the shape modification.
	 */
	public final void setParent(Object aParent) {
		parent = aParent;
	}

	/**
	 * 
	 * @return The current set of children. Clients may add or remove elements
	 *         directly to this set.
	 */
	public final Set getChildren() {
		return children;
	}

}
