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

import org.eclipse.core.databinding.observable.IObservable;

/**
 * 
 * A tree whose changes can be tracked by tree change listeners. If the tree is
 * ordered ({@link #isOrdered()}), the order of children for a given tree path
 * matters, and tree change notifications will always specify indices. If the
 * tree is unordered, the children of a tree path are an unordered set and
 * indices in change notifications are not specified.
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
	 * Element that can be returned from synchronous getters if this observable
	 * tree is lazy.
	 */
	public final static Object UNKNOWN_ELEMENT = new Object();
	
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
	 * Returns whether this tree is optimized to fetch subsets of children
	 * lazily and possibly asynchronously. Implies {@link #isOrdered()}.
	 * 
	 * @return true if this tree 
	 */
	public boolean isLazy();

	/**
	 * @param parentPath
	 * @return the children at the given parent path
	 */
	public Object[] getChildren(TreePath parentPath);
	
	/**
	 * @param parentPath
	 * @param children
	 */
	public void setChildren(TreePath parentPath, Object[] children);
	
	/**
	 * @param parentPath
	 * @param childElement
	 */
	public void addChild(TreePath parentPath, Object childElement);
	
	/**
	 * @param parentPath
	 * @param childElement 
	 */
	public void removeChild(TreePath parentPath, Object childElement);
	
	/**
	 * @param parentPath
	 * @param index
	 * @param childElement
	 */
	public void insertChild(TreePath parentPath, int index, Object childElement);
	
	/**
	 * @param parentPath
	 * @param index
	 */
	public void removeChild(TreePath parentPath, int index);
	
	/**
	 * @param parentPath
	 * @return <code>true</code> if the element at the given path has children
	 */
	public boolean hasChildren(TreePath parentPath);
	
	/**
	 * @param parentPath
	 * @return the number of children of the element at the given path 
	 */
	public int getChildCount(TreePath parentPath);
	
	/**
	 * @param parentPath
	 * @param count
	 */
	public void setChildCount(TreePath parentPath, int count);
	
	/**
	 * Updates the number of children for the given parent elements in the
	 * specified request.
	 * 
	 * @param update specifies counts to update and stores result
	 */
	public void updateChildrenCount(IChildrenCountUpdate update);
	
	/**
	 * Updates children as requested by the update.
	 * 
	 * @param update specifies children to update and stores result
	 */	
	public void updateChildren(IChildrenUpdate update);
	
	/**
	 * Updates whether elements have children.
	 * 
	 * @param update specifies elements to update and stores result
	 */
	public void updateHasChildren(IHasChildrenUpdate update);

}
