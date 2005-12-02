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
 * 
 *
 * @since 3.2
 */
public interface IModelDeltaNode {
	public IModelDeltaNode getParent();
	public Object getElement();
	public int getFlags();
	public ModelDeltaNode[] getNodes();
	public Object getNewElement();
	public int getIndex();
	
	// TODO: should be part of the implementation rather than the interface (i.e.
	// interface should bre read-only).
	public IModelDeltaNode addNode(Object object, int flags);
	public IModelDeltaNode addNode(Object element, Object newElement, int flags);
	public IModelDeltaNode addNode(Object element, int index, int flags);
}
