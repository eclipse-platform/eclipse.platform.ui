/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.tree;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;

/**
 * Objects that implement this interface are capable of describing a tree by
 * returning the list of children of any given element in the tree.
 * 
 * @since 3.3
 */
public interface IOrderedTreeProvider {
	/**
	 * Returns the children of the given element, or null if the element is a
	 * leaf node. The caller of this method is expected to dispose the result
	 * list when it is no longer needed.
	 * 
	 * @param element
	 *            the tree path of the element to query
	 * @return the children of the given element, or null if the element is a
	 *         leaf node
	 */
	IObservableList createChildList(TreePath element);

	/**
	 * @return the realm shared by all child lists
	 */
	Realm getRealm();
}
