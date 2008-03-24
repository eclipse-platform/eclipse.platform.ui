/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.databinding.viewers;

/**
 * Instances of this class can be used to improve accuracy and performance of an
 * {@link ObservableListTreeContentProvider} or an
 * {@link ObservableSetTreeContentProvider}.
 * 
 * @since 3.4
 * 
 */
public abstract class TreeStructureAdvisor {

	/**
	 * Returns the parent for the given element, or <code>null</code>
	 * indicating that the parent can't be computed. In this case the
	 * tree-structured viewer can't expand a given node correctly if requested.
	 * 
	 * @param element
	 *            the element
	 * @return the parent element, or <code>null</code> if it has none or if
	 *         the parent cannot be computed
	 */
	public abstract Object getParent(Object element);

	/**
	 * Returns whether the given element has children.
	 * <p>
	 * Intended as an optimization for when the viewer does not need the actual
	 * children. Clients may be able to implement this more efficiently than
	 * <code>getChildren</code>.
	 * </p>
	 * 
	 * @param element
	 *            the element
	 * @return <code>true</code> if the given element has children, and
	 *         <code>false</code> if it has no children
	 */
	public abstract boolean hasChildren(Object element);

}
