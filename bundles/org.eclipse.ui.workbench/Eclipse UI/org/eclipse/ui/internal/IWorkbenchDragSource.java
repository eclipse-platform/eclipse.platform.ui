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

import org.eclipse.swt.graphics.Point;

/**
 * The IWorkbenchDragSource is the interface for drag sources.
 */
public interface IWorkbenchDragSource extends IWorkbenchDragDropPart {

	/**
	 * Get the type constant for this drag source.
	 * This can only have a single bit (i.e. 1,2,4,8).
	 * @return int
	 */
	public int getType();

	/**
	 *	Allow the layout part to determine if they are in
	 * an acceptable state to start a drag & drop operation.
	 * @param point
	 * @return boolean
	 */
	public boolean isDragAllowed(Point point);
	
}
