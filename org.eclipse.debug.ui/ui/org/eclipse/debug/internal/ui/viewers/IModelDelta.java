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
 * Describes a change within a model. A delta is a hierarchical description of a change
 * within a model. It constists of a path of nodes. Each node references an element
 * from a model, and desribes how that element changed.
 * <p>
 * An element can be added, removed, or changed. As well, its possible that an element
 * did not change, but that one of its children changed. When an element changes, an 
 * additional information describes the change. For example, and element's content may have
 * changed (its children), or its state may have changed.
 * </p>
 * <p>
 * Optionally, an update action is provided with a node. A node may be expanded
 * or selected.
 * </p>
 * @since 3.2
 */
public interface IModelDelta {
	
	//change type
	public static int NOCHANGE = 0;
	public static int ADDED = 1;
	public static int REMOVED = 1 << 1;
	public static int CHANGED = 1 << 2;
	public static int REPLACED = 1 << 3;
	public static int INSERTED = 1 << 4;
	
	//how it changed.
	public static int CONTENT = 1 << 10;
	public static int STATE = 1 << 11;
	
	// action
	public static int EXPAND = 1 << 20;
	public static int SELECT = 1 << 21;

	// TODO: should be part of the implementation rather than the interface (i.e.
	// interface should be read-only).
	public IModelDeltaNode addNode(Object element, int flags);
	public IModelDeltaNode[] getNodes();
}
