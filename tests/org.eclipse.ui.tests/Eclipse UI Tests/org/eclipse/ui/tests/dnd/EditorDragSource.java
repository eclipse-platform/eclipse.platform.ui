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

import org.eclipse.ui.IEditorPart;

/**
 * @since 3.0
 */
public class EditorDragSource extends TestDragSource {

	int editorIdx;

	boolean wholeFolder;

	public EditorDragSource(int editorIdx, boolean wholeFolder) {
		super();
		this.editorIdx = editorIdx;
		this.wholeFolder = wholeFolder;
	}

	IEditorPart getPart() {
		return getPage().getEditors()[editorIdx];
	}

	@Override
	public String toString() {
		String title = "editor " + editorIdx;

		if (wholeFolder) {
			return title + " folder";
		}
		return title;
	}

	@Override
	public void drag(TestDropLocation target) {
		DragOperations.drag(getPart(), target, wholeFolder);
	}

}
