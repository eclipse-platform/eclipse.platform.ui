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
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Basic implementation of the IDragSource interface. Provides empty implementations
 * of all methods, allowing subclasses to selectively override methods of interest.
 */
public abstract class AbstractDragSource implements IDragSource {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragSource#getDraggedItem(org.eclipse.swt.graphics.Point)
	 */
	public abstract Object getDraggedItem(Point position);

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragSource#dragStarted(java.lang.Object)
	 */
	public void dragStarted(Object draggedItem) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragSource#dragFinished(java.lang.Object, boolean)
	 */
	public void dragFinished(Object draggedItem, boolean success) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.dnd.IDragSource#getDragRectangle(java.lang.Object)
	 */
	public Rectangle getDragRectangle(Object draggedItem) {
		return null;
	}

}
