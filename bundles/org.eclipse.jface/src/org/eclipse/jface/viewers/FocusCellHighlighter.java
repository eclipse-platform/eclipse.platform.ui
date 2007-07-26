/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											   bugfix in: 182800
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
	 * Called by the framework when the focus cell has changed. Subclasses may
	 * extend.
	 *
	 * @param cell
	 *            the new focus cell
	 * @deprecated use {@link #focusCellChanged(ViewerCell, ViewerCell)} instead
	 */
	protected void focusCellChanged(ViewerCell cell) {
	}

	/**
	 * Called by the framework when the focus cell has changed. Subclasses may
	 * extend.
	 * <p>
	 * <b>The default implementation for this method calls
	 * focusCellChanged(ViewerCell). Subclasses should override this method
	 * rather than {@link #focusCellChanged(ViewerCell)} .</b>
	 *
	 * @param newCell
	 *            the new focus cell or <code>null</code> if no new cell
	 *            receives the focus
	 * @param oldCell
	 *            the old focus cell or <code>null</code> if no cell has been
	 *            focused before
	 * @since 3.4
	 */
	protected void focusCellChanged(ViewerCell newCell, ViewerCell oldCell) {
		focusCellChanged(newCell);
	}

	/**
	 * This method is called by the framework to initialize this cell
	 * highlighter object. Subclasses may extend.
	 */
	protected void init() {
	}
}
