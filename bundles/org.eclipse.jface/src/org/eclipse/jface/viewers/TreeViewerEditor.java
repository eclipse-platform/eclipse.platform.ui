/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.viewers;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @since 3.3
 * 
 */
public class TreeViewerEditor extends ColumnViewerEditor {
	/**
	 * This viewer's tree editor.
	 */
	private TreeEditor treeEditor;

	private SWTFocusCellManager focusCellManager;

	/**
	 * @param viewer
	 * @param focusCellManager
	 * @param editorActivationStrategy
	 * @param feature
	 */
	TreeViewerEditor(TreeViewer viewer, SWTFocusCellManager focusCellManager,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		super(viewer, editorActivationStrategy, feature);
		treeEditor = new TreeEditor(viewer.getTree());
		this.focusCellManager = focusCellManager;
	}

	/**
	 * @param viewer
	 * @param focusCellManager
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(TreeViewer viewer,
			SWTFocusCellManager focusCellManager,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		TreeViewerEditor editor = new TreeViewerEditor(viewer,
				focusCellManager, editorActivationStrategy, feature);
		viewer.setColumnViewerEditor(editor);
	}

	/**
	 * @param viewer
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(TreeViewer viewer,
			ColumnViewerEditorActivationStrategy editorActivationStrategy,
			int feature) {
		create(viewer, null, editorActivationStrategy, feature);
	}

	protected void setEditor(Control w, Item item, int fColumnNumber) {
		treeEditor.setEditor(w, (TreeItem) item, fColumnNumber);
	}

	protected void setLayoutData(LayoutData layoutData) {
		treeEditor.grabHorizontal = layoutData.grabHorizontal;
		treeEditor.horizontalAlignment = layoutData.horizontalAlignment;
		treeEditor.minimumWidth = layoutData.minimumWidth;
	}

	public ViewerCell getFocusCell() {
		if (focusCellManager != null) {
			return focusCellManager.getFocusCell();
		}

		return super.getFocusCell();
	}

	protected void updateFocusCell(ViewerCell focusCell) {
		List l = getViewer().getSelectionFromWidget();

		if (focusCellManager != null) {
			focusCellManager.setFocusCell(focusCell);
		}

		if (!l.contains(focusCell.getElement())) {
			getViewer().setSelection(new StructuredSelection(focusCell.getElement()));
		}

	}
}
