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
package org.eclipse.ui.internal.skins;

import org.eclipse.swt.graphics.Rectangle;

/**
 * This structure describes a drop event that will cause a dragged part
 * to be stacked in a position currently occupied by another part. 
 * 
 * @since 3.0
 */
public final class StackDropResult {
	
	private Rectangle snapRectangle;
	private int tabIndex;
	
	/**
	 * Creates a drag status object
	 * 
	 * @param snapRectangle region that should be highlighted by the tracking
	 * rectangle (display coordinates) 
	 * @param dragPart part being dragged over (or null if the dragged part
	 * can be added anywhere)
	 */
	public StackDropResult(Rectangle snapRectangle, int tabIndex) {
		this.snapRectangle = snapRectangle;
		this.tabIndex = tabIndex;
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
	 * Returns the part being dragged over. If the part being dragged is dropped
	 * here, it will replace this part.
	 * 
	 * @return the part that will be replaced by this drop operation (or null if
	 * the dragged part can be added anywhere in the stack)
	 */
	public int getDropIndex() {
		return tabIndex;
	}
}
