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

/**
 * @since 3.3
 * 
 */
public abstract class FocusCellHighlighter {
	private ColumnViewer viewer;

	/**
	 * @param viewer
	 */
	public FocusCellHighlighter(ColumnViewer viewer) {
		this.viewer = viewer;
	}

	/**
	 * @return the focus cell
	 */
	public ViewerCell getFocusCell() {
		return viewer.getColumnViewerEditor().getFocusCell();
	}

	/**
	 * Called by the framework when the focus cell has changed. Subclasses may extend.
	 * 
	 * @param cell the new focus cell
	 */
	protected void focusCellChanged(ViewerCell cell) {
	}

	/**
	 * This method is called by the framework to initialize this cell
	 * highlighter object. Subclasses may extend.
	 */
	protected void init() {
	}
}
