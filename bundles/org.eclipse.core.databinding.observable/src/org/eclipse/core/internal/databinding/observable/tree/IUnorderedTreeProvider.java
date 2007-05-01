/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.internal.databinding.observable.tree;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;

/**
 * @since 1.0
 *
 */
public interface IUnorderedTreeProvider {
	/**
	 * @return the realm for the createChildSet method
	 */
	public Realm getRealm();
	
	/**
	 * Returns the children of the given element, or null if the element is a leaf node.
	 * The caller of this method is expected to dispose the result set when it is no
	 * longer needed.
	 * 
	 * @param element element to query
	 * @return the children of the given element, or null if the element is a leaf node
	 */
	IObservableSet createChildSet(Object element);
}
