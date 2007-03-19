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

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationListener;
import org.eclipse.jface.viewers.ColumnViewerEditorDeactivationEvent;
import org.eclipse.jface.viewers.FocusCellHighlighter;
import org.eclipse.jface.viewers.ViewerCell;



/**
 * @since 3.3
 * 
 */
public class CursorCellHighlighter extends FocusCellHighlighter {
	private ColumnViewer viewer;

	private AbstractCellCursor cursor;

	/**
	 * @param viewer
	 * @param cursor
	 */
	public CursorCellHighlighter(ColumnViewer viewer,
			AbstractCellCursor cursor) {
		super(viewer);
		this.viewer = viewer;
		this.cursor = cursor;
	}

	protected void focusCellChanged(ViewerCell cell) {
		super.focusCellChanged(cell);
		if( ! viewer.isCellEditorActive() ) {
			System.err.println("SHOW EDITOR"); //$NON-NLS-1$
			cursor.setSelection(cell, 0); //TODO THE TIME
			cursor.setVisible(true);
		}
	}
	
	protected void init() {
		hookListener();
	}

	private void hookListener() {
		ColumnViewerEditorActivationListener listener = new ColumnViewerEditorActivationListener() {

			public void afterEditorActivated(
					ColumnViewerEditorActivationEvent event) {
				
			}

			public void afterEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
				cursor.setVisible(true);
				cursor.setSelection(getFocusCell(), 0); //TODO THE TIME
			}

			public void beforeEditorActivated(
					ColumnViewerEditorActivationEvent event) {
				cursor.setVisible(false);
			}

			public void beforeEditorDeactivated(
					ColumnViewerEditorDeactivationEvent event) {
				
			}
		};
		
		viewer.getColumnViewerEditor().addEditorActivationListener(listener);
	}
}
