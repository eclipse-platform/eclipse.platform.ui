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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;

/**
 * This class is implemented by IStackContainer to describe the regions in which
 * dropping a part would cause it to be stacked in a position currently occupied
 * by an existing part.
 * <p>
 * For example, this provides support for rearranging tabs in a stack. 
 * </p>
 * 
 * @since 3.0
 */
public abstract class StackDragListener {
	/**
	 * Given the control being dragged over and the current position of the mouse
	 * cursor, this method returns information about the tab that would be stacked
	 * on if a part were to be dropped here. Note that this method should return
	 * null unless the skin specifically wants to  
	 * 
	 * @param currentControl
	 * @param position
	 * @return a StackDropEvent describing the part being dragged over, or null if
	 * the skin does not provide any special stacking behavior when dragging over
	 * the given position.
	 */
	public abstract StackDropResult dragEvent(Control currentControl, Point position);
}
