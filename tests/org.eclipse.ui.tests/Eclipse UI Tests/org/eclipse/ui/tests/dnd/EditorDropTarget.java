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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDropTarget extends WorkbenchWindowDropTarget {

	int editorIdx;

	int side;

	public EditorDropTarget(IWorkbenchWindowProvider provider, int editorIdx, int side) {
		super(provider);
		this.editorIdx = editorIdx;
		this.side = side;
	}

	IEditorPart getPart() {
		return getPage().getEditors()[editorIdx];
	}

	@Override
	public String toString() {
		return DragOperations.nameForConstant(side) + " of editor " + editorIdx;
	}

	@Override
	public Point getLocation() {
		return DragOperations.getLocation(side);
	}

	@Override
	public Shell getShell() {
		return getPart().getSite().getShell();
	}
}
