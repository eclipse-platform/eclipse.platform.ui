/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.Collection;

/**
 * IWorkbenchDropTarget is the interface for drop targets.
 */
public interface IWorkbenchDropTarget extends IWorkbenchDragDropPart{
	
	public LayoutPart targetPartFor(IWorkbenchDragSource dragSource);
	
	/**
	 * Get the type constant for this drop target. This may
	 * be a combination of bits as a drop target may accept
	 * multiple drag source types.
	 * @return int
	 */
	public int getType();
	
	/**
	 * Get all of the drop targets contained in the receiver.
	 * If this is a composite object then return the children
	 * otherwise return this.
	 * Recurse into composite children.
	 * @param result
	 */
	public void addDropTargets(Collection result);

}
