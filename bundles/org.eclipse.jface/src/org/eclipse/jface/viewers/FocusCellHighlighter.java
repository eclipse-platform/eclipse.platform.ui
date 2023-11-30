/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     											   bugfix in: 182800
 ******************************************************************************/

package org.eclipse.jface.viewers;

/**
 * @since 3.3
 */
public abstract class FocusCellHighlighter {
	private ColumnViewer viewer;
	private SWTFocusCellManager mgr;

	/**
	 * @param viewer the attached viewer
	 */
	public FocusCellHighlighter(ColumnViewer viewer) {
		this.viewer = viewer;
	}

	void setMgr(SWTFocusCellManager mgr) {
		this.mgr = mgr;
	}

	/**
	 * @return the focus cell
	 */
	public ViewerCell getFocusCell() {
		// Mgr is normally not null because the highlighter is passed
		// to the SWTFocusCellManager instance
		if( mgr != null ) {
			// Use this method because it ensure that no
			// cell update (which might cause scrolling) happens
			return mgr._getFocusCell();
		}

		return viewer.getColumnViewerEditor().getFocusCell();
	}

	/**
	 * Called by the framework when the focus cell has changed. Subclasses may
	 * extend.
	 *
	 * @param cell
	 *            the new focus cell or <code>null</code> if no new cell
	 *            receives the focus
	 * @deprecated use {@link #focusCellChanged(ViewerCell, ViewerCell)} instead
	 */
	@Deprecated
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
