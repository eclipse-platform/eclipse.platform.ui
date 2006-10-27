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

package org.eclipse.jface.databinding.observable.tree;

import org.eclipse.jface.databinding.observable.IObservable;

/**
 * 
 * An ordered tree whose changes can be tracked by tree change listeners.
 * 
 * <p>
 * This interface is not intended to be implemented by clients. Clients should
 * instead subclass one of the framework classes that implement this interface.
 * Note that direct implementers of this interface outside of the framework will
 * be broken in future releases when methods are added to this interface.
 * </p>
 * 
 * @since 1.1
 */
public interface IObservableTree extends IObservable {
	/**
	 * @param listener
	 */
	public void addTreeChangeListener(ITreeChangeListener listener);

	/**
	 * @param listener
	 */
	public void removeTreeChangeListener(ITreeChangeListener listener);

	/**
	 * Returns whether the order of children for a given parent is important. If
	 * this tree is ordered, tree change notifications will always specify
	 * indices.
	 * 
	 * @return true if the order of children for a given parent is important
	 */
	public boolean isOrdered();

	/**
	 * @param parentPath
	 * @return
	 */
	public Object[] getChildren(TreePath parentPath);
}
