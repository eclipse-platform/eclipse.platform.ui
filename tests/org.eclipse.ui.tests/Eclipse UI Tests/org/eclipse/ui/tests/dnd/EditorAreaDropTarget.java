/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;

/**
 * @since 3.0
 */
public class EditorAreaDropTarget extends WorkbenchWindowDropTarget {
	int side;

	/**
	 * @param window
	 * @param side
	 */
	public EditorAreaDropTarget(IWorkbenchWindowProvider provider, int side) {
		super(provider);
		this.side = side;
	}

	@Override
	public String toString() {
		return DragOperations.nameForConstant(side) + " of editor area";
	}

	@Override
	public Point getLocation() {
		return DragOperations.getPointInEditorArea(getPage(), side);
	}

}
