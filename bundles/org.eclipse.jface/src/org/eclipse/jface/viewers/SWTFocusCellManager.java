/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *                                               - bug fix for bug 187189, 182800, 215069
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.core.runtime.Assert;

/**
 * This class is responsible to provide cell management base features for the
 * SWT-Controls {@link org.eclipse.swt.widgets.Table} and
 * {@link org.eclipse.swt.widgets.Tree}.
 *
 * @since 3.3
 *
 */
abstract class SWTFocusCellManager {

	private CellNavigationStrategy navigationStrategy;

	private ColumnViewer viewer;

	private ViewerCell focusCell;

	private FocusCellHighlighter cellHighlighter;

	private DisposeListener itemDeletionListener = e -> setFocusCell(null);

	/**
	 * @param viewer
	 * @param focusDrawingDelegate
	 * @param navigationDelegate
	 */
	public SWTFocusCellManager(ColumnViewer viewer,
			FocusCellHighlighter focusDrawingDelegate,
			CellNavigationStrategy navigationDelegate) {
		this.viewer = viewer;
		this.cellHighlighter = focusDrawingDelegate;
		if( this.cellHighlighter != null ) {
			this.cellHighlighter.setMgr(this);
		}

		this.navigationStrategy = navigationDelegate;
		hookListener(viewer);
	}

	/**
	 * This method is called by the framework to initialize this cell manager.
	 */
	void init() {
		this.cellHighlighter.init();
		this.navigationStrategy.init();
	}

	private void handleMouseDown(Event event) {
		ViewerCell cell = viewer.getCell(new Point(event.x, event.y));
		if (cell != null) {

			if (!cell.equals(focusCell)) {
				setFocusCell(cell);
			}
		}
	}

	private void handleKeyDown(Event event) {
		ViewerCell tmp = null;

		if (navigationStrategy.isCollapseEvent(viewer, focusCell, event)) {
			navigationStrategy.collapse(viewer, focusCell, event);
		} else if (navigationStrategy.isExpandEvent(viewer, focusCell, event)) {
			navigationStrategy.expand(viewer, focusCell, event);
		} else if (navigationStrategy.isNavigationEvent(viewer, event)) {
			tmp = navigationStrategy.findSelectedCell(viewer, focusCell, event);

			if (tmp != null) {
				if (!tmp.equals(focusCell)) {
					setFocusCell(tmp);
				}
			}
		}

		if (navigationStrategy.shouldCancelEvent(viewer, event)) {
			event.doit = false;
		}
	}

	private void handleSelection(Event event) {
		if ((event.detail & SWT.CHECK) == 0 && focusCell != null && focusCell.getItem() != event.item
				&& event.item != null && ! event.item.isDisposed() ) {
			ViewerRow row = viewer.getViewerRowFromItem(event.item);
			Assert
					.isNotNull(row,
							"Internal Structure invalid. Row item has no row ViewerRow assigned"); //$NON-NLS-1$
			ViewerCell tmp = row.getCell(focusCell.getColumnIndex());
			if (!focusCell.equals(tmp)) {
				setFocusCell(tmp);
			}
		}
	}

	/**
	 * Handles the {@link SWT#FocusIn} event.
	 *
	 * @param event the event
	 */
	private void handleFocusIn(Event event) {
		if (focusCell == null) {
			setFocusCell(getInitialFocusCell());
		}
	}

	abstract ViewerCell getInitialFocusCell();

	private void hookListener(final ColumnViewer viewer) {
		Listener listener = event -> {
			switch (event.type) {
			case SWT.MouseDown:
				handleMouseDown(event);
				break;
			case SWT.KeyDown:
				handleKeyDown(event);
				break;
			case SWT.Selection:
				handleSelection(event);
				break;
			case SWT.FocusIn:
				handleFocusIn(event);
				break;
			}
		};

		viewer.getControl().addListener(SWT.MouseDown, listener);
		viewer.getControl().addListener(SWT.KeyDown, listener);
		viewer.getControl().addListener(SWT.Selection, listener);
		viewer.addSelectionChangedListener(event -> {
			if( event.selection.isEmpty() ) {
				setFocusCell(null);
			}
		});
		viewer.getControl().addListener(SWT.FocusIn, listener);
		viewer.getControl().getAccessible().addAccessibleListener(
				new AccessibleAdapter() {
					@Override
					public void getName(AccessibleEvent event) {
						ViewerCell cell = getFocusCell();
						if (cell == null)
							return;

						ViewerRow row = cell.getViewerRow();
						if (row == null)
							return;

						ViewerColumn viewPart = viewer.getViewerColumn(cell
								.getColumnIndex());

						if (viewPart == null)
							return;

						CellLabelProvider labelProvider = viewPart
								.getLabelProvider();

						if (labelProvider == null)
							return;
						labelProvider.update(cell);
						event.result = cell.getText();
					}
				});

	}

	/**
	 * @return the cell with the focus
	 *
	 */
	public ViewerCell getFocusCell() {
		return focusCell;
	}

	final ViewerCell _getFocusCell() {
		return focusCell;
	}

	void setFocusCell(ViewerCell focusCell) {
		ViewerCell oldCell = this.focusCell;

		if( this.focusCell != null && ! this.focusCell.getItem().isDisposed() ) {
			this.focusCell.getItem().removeDisposeListener(itemDeletionListener);
		}

		this.focusCell = focusCell;

		if( this.focusCell != null && ! this.focusCell.getItem().isDisposed() ) {
			this.focusCell.getItem().addDisposeListener(itemDeletionListener);
		}

		if( focusCell != null ) {
			focusCell.scrollIntoView();
		}

		this.cellHighlighter.focusCellChanged(focusCell,oldCell);

		getViewer().getControl().getAccessible().setFocus(ACC.CHILDID_SELF);
	}

	ColumnViewer getViewer() {
		return viewer;
	}
}
