/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.presentations;

import org.eclipse.swt.graphics.Rectangle;

/**
 * This structure describes a drop event that will cause a dragged part
 * to be stacked in a position currently occupied by another part. 
 * 
 * @since 3.0
 */
public final class StackDropResult {
	
	private Rectangle snapRectangle;
	private IPresentablePart insertBefore;
	
	/**
	 * Creates a drag status object
	 * 
	 * @param snapRectangle region that should be highlighted by the tracking
	 * rectangle (display coordinates) 
	 * @param insertBefore indicates the position where the drop will occur.
	 * The dragged part will be inserted before the indicated part. Null if
	 * the dragged part should be inserted at the end.
	 */
	public StackDropResult(Rectangle snapRectangle, IPresentablePart insertBefore) {
		this.snapRectangle = snapRectangle;
		this.insertBefore = insertBefore;
	}
	
	/**
	 * Returns a rectangle (screen coordinates) describing the target location
	 * for this drop operation. While dragging, the tracking rectangle will
	 * snap to this position.
	 * 
	 * @return a snap rectangle (not null)
	 */
	public Rectangle getSnapRectangle() {
		return snapRectangle;
	}
	
	/**
	 * Returns the insertion position for this drop, or null if the drop will
	 * occur at the end. That is, the newly inserted part will be inserted
	 * before the returned part.
	 * 
	 * @return the insertion position, or null if at the end
	 */
	public IPresentablePart getInsertionPoint() {
		return insertBefore;
	}	
}
