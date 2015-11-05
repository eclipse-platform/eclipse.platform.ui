/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dnd;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;

/**
 */
public abstract class AbstractDropTarget implements IDropTarget {
    @Override
	public abstract void drop();

    @Override
	public abstract Cursor getCursor();

    @Override
	public Rectangle getSnapRectangle() {
        return null;
    }
}
