/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.viewers;

import org.eclipse.debug.internal.ui.viewers.update.ModelDeltaNode;

/**
 * A node in a model delta describing changes to an element in a model.
 * <p>
 * Clients are not intended to implement this interface directly. Instead
 * clients should create instances of
 * {@link org.eclipse.debug.internal.ui.viewers.update.ModelDeltaNode}.
 * </p>
 * @see IModelDelta
 * @since 3.2
 */
public interface IModelDeltaNode {
	/**
	 * Returns the parent of this node, or <code>null</code> if this is
	 * a root node.
	 * 
	 * @return parent node or <code>null</code> if this is a root node
	 */
	public IModelDeltaNode getParent();
	
	/**
	 * Returns the model element this node describes.
	 * 
	 * @return associated model element
	 */
	public Object getElement();
	
	/**
	 * Returns flags describing how this element changed. A bit mask of the
	 * change type constants described in {@link IModelDelta}.
	 * 
	 * @return change flags
	 */
	public int getFlags();
	
	/**
	 * Returns nodes describing changed children, possibly an empty collection.
	 *  
	 * @return changed children, possibly empty
	 */
	public ModelDeltaNode[] getNodes();
	
	/**
	 * When a node indicates the <code>IModelDelta.REPLACED</code> flag, this method
	 * returns the replacement element, otherwise <code>null</code>.
	 *  
	 * @return replacement element or <code>null</code>
	 */
	public Object getNewElement();
	
	/**
	 * When a node indicates the <code>IModelDelta.INSERTED</code> flag, this method
	 * returns the index that the new element should be inserted at relative to its
	 * parents children, otherwise -1.
	 * 
	 * @return insertion index or -1
	 */
	public int getIndex();
	
	// TODO: should be part of the implementation rather than the interface (i.e.
	// interface should bre read-only).
	public IModelDeltaNode addNode(Object object, int flags);
	public IModelDeltaNode addNode(Object element, Object newElement, int flags);
	public IModelDeltaNode addNode(Object element, int index, int flags);
}
