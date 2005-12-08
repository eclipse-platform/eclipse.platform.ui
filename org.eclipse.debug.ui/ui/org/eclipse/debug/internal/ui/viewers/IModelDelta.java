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

/**
 * Describes a change within a model. A delta is a hierarchical description of changes
 * within a model. It constists of a tree of nodes. Each node references an element
 * from a model, and desribes how that element changed, if at all. A model proxy
 * fires model deltas as its model changes in order to update views displaying that
 * model. 
 * <p>
 * Each node in a model delta describes the following:
 * <ul>
 * <li>the type of change - for example, whether an element was added, removed or changed</li>
 * <li>how it changed - for example, content versus state change</li>
 * <li>action to consider - for example, select or reveal the element</li>
 * </ul> 
 * </p>
 * <p>
 * Clients are not intended to implement this interface directly. Instead, clients
 * creating and firing model deltas should create instances of
 * {@link org.eclipse.debug.internal.ui.viewers.update.ModelDelta}.
 * </p>
 * @since 3.2
 * @see IModelDeltaNode
 * @see org.eclipse.debug.internal.ui.viewers.update.ModelDelta
 */
public interface IModelDelta {
	
	//change types
	
	/**
	 * Indicates an element has not changed, but has children that have
	 * changed in some way.
	 */
	public static int NOCHANGE = 0;
	
	/**
	 * Indicates an element has been added to the model, as described by
	 * its path.
	 */
	public static int ADDED = 1;
	
	/**
	 * Indicates an element has been removed from the model, as described by
	 * its path.
	 */
	public static int REMOVED = 1 << 1;
	
	/**
	 * Indicates an element in the model has changed, as described by its path.
	 * Flags indicate how the element changed (i.e. content and/or state) 
	 */
	public static int CHANGED = 1 << 2;
	
	/**
	 * Indicates an element has been replaced in the model, as described by
	 * its path. In this case a replacement element is also specified in the
	 * model delta node.
	 */	
	public static int REPLACED = 1 << 3;
	
	/**
	 * Indicates an element has been inserted into the model, as described
	 * by its path and index.
	 */
	public static int INSERTED = 1 << 4;
	
	// change flags - how something changed
	
	/**
	 * Indicates an elements content has changed (i.e. its children).
	 */
	public static int CONTENT = 1 << 10;
	
	/**
	 * Indicates an elements state has changed (i.e. label)
	 */
	public static int STATE = 1 << 11;
	
	// action to consider for node
	
	/**
	 * Suggests that the element should be expanded, as described by its path.
	 */
	public static int EXPAND = 1 << 20;
	
	/**
	 * Suggests that the element should be selected, as described by its path.
	 */
	public static int SELECT = 1 << 21;

	// TODO: should be part of the implementation rather than the interface (i.e.
	// interface should be read-only).
	public IModelDeltaNode addNode(Object element, int flags);
	
	/**
	 * Returns the root nodes in this model delta.
	 * 
	 * @return root nodes in this model delta
	 */
	public IModelDeltaNode[] getNodes();
}
