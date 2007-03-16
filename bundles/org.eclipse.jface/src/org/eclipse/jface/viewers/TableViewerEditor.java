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
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableItem;

/**
 * @since 3.3
 *
 */
public final class TableViewerEditor extends ColumnViewerEditor {
	/**
	 * This viewer's table editor.
	 */
	private TableEditor tableEditor;
	
	private SWTFocusCellManager focusCellManager;
	
	TableViewerEditor(TableViewer viewer, SWTFocusCellManager focusCellManager, ColumnViewerEditorActivationStrategy editorActivationStrategy, int feature) {
		super(viewer,editorActivationStrategy,feature);
		tableEditor = new TableEditor(viewer.getTable());
		this.focusCellManager = focusCellManager; 
	}
	
	/**
	 * @param viewer
	 * @param focusCellManager
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(TableViewer viewer, SWTFocusCellManager focusCellManager, ColumnViewerEditorActivationStrategy editorActivationStrategy, int feature) {
		TableViewerEditor editor = new TableViewerEditor(viewer,focusCellManager,editorActivationStrategy,feature);
		viewer.setColumnViewerEditor(editor);
	}
	
	/**
	 * @param viewer
	 * @param editorActivationStrategy
	 * @param feature
	 */
	public static void create(TableViewer viewer, ColumnViewerEditorActivationStrategy editorActivationStrategy, int feature) {
		create(viewer, null, editorActivationStrategy, feature);
	}
	
	protected void setEditor(Control w, Item item, int columnNumber) {
		tableEditor.setEditor(w, (TableItem) item, columnNumber);
	}

	protected void setLayoutData(LayoutData layoutData) {
		tableEditor.grabHorizontal = layoutData.grabHorizontal;
		tableEditor.horizontalAlignment = layoutData.horizontalAlignment;
		tableEditor.minimumWidth = layoutData.minimumWidth;
	}

	public ViewerCell getFocusCell() {
		if( focusCellManager != null ) {
			return focusCellManager.getFocusCell();
		}
		
		return super.getFocusCell();
	}

	protected void updateFocusCell(ViewerCell focusCell, ColumnViewerEditorActivationEvent event) {
		// Update the focus cell when we activated the editor with these 2 events
		if( event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC || event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL ) {

			List l = getViewer().getSelectionFromWidget();

			if( focusCellManager != null ) {
				focusCellManager.setFocusCell(focusCell);
			}

			if (!l.contains(focusCell.getElement())) {
				getViewer().setSelection(new StructuredSelection(focusCell.getElement()));
			}
		}
	}
}
