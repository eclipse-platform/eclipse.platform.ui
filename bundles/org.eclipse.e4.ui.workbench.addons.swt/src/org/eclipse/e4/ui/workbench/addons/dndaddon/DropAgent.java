/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
abstract class DropAgent {
	public abstract boolean canDrop(MUIElement dragElement, CursorInfo info);

	public abstract boolean drop(MUIElement dragElement, CursorInfo info);

	public Cursor getCursor(Display display, MUIElement dragElement,
			CursorInfo info) {
		return display.getSystemCursor(SWT.CURSOR_HAND);
	}

	public Rectangle getRectangle(MUIElement dragElement, CursorInfo info) {
		return null;
	}

	public void dragEnter() {
	}

	public void dragLeave() {

	}

	public void track() {

	}
}
