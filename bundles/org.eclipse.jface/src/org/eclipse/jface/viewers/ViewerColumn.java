/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 * 												  fix for bug 163317, 201905
 *     Ralf Ebert - bug 294738
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 475689
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class represent a column of a {@link ColumnViewer}. Label
 * providers and editing support can be configured for each column separately.
 * Concrete subclasses of {@link ColumnViewer} should implement a matching
 * concrete subclass of {@link ViewerColumn}.
 *
 * @since 3.3
 *
 */
public abstract class ViewerColumn {

	private CellLabelProvider labelProvider;

	static String COLUMN_VIEWER_KEY = Policy.JFACE + ".columnViewer";//$NON-NLS-1$

	private EditingSupport editingSupport;

	private ILabelProviderListener listener;

	private boolean listenerRegistered = false;

	private ColumnViewer viewer;

	/**
	 * Create a new instance of the receiver at columnIndex.
	 *
	 * @param viewer
	 *            the viewer the column is part of
	 * @param columnOwner
	 *            the widget owning the viewer in case the widget has no columns
	 *            this could be the widget itself
	 */
	protected ViewerColumn(final ColumnViewer viewer, Widget columnOwner) {
		this.viewer = viewer;
		columnOwner.setData(ViewerColumn.COLUMN_VIEWER_KEY, this);
		this.listener = viewer::handleLabelProviderChanged;
		columnOwner.addDisposeListener(e -> handleDispose(viewer));
	}

	/**
	 * Return the label provider for the receiver.
	 *
	 * @return ViewerLabelProvider
	 */
	/* package */CellLabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Set the label provider for the column. Subclasses may extend but must
	 * call the super implementation.
	 *
	 * @param labelProvider
	 *            the new {@link CellLabelProvider}
	 */
	public void setLabelProvider(CellLabelProvider labelProvider) {
		setLabelProvider(labelProvider, true);
	}

	/**
	 * @param labelProvider
	 * @param registerListener
	 */
	/* package */void setLabelProvider(CellLabelProvider labelProvider,
			boolean registerListener) {
		if (listenerRegistered && this.labelProvider != null) {
			this.labelProvider.removeListener(listener);
			listenerRegistered = false;
			if (registerListener) {
				this.labelProvider.dispose(viewer, this);
			}
		}

		this.labelProvider = labelProvider;

		if (registerListener) {
			this.labelProvider.initialize(viewer, this);
			this.labelProvider.addListener(listener);
			listenerRegistered = true;
		}
	}

	/**
	 * Return the editing support for the receiver.
	 *
	 * @return {@link EditingSupport}
	 */
	/* package */EditingSupport getEditingSupport() {
		return editingSupport;
	}

	/**
	 * Set the editing support. Subclasses may extend but must call the super
	 * implementation.
	 * <p>
	 * Users setting up an editable {@link TreeViewer} or {@link TableViewer} with more than 1 column <b>have</b>
	 * to pass the SWT.FULL_SELECTION style bit when creating the viewer
	 * </p>
	 * @param editingSupport
	 *            The {@link EditingSupport} to set.
	 */
	public void setEditingSupport(EditingSupport editingSupport) {
		this.editingSupport = editingSupport;
	}

	/**
	 * Refresh the cell for the given columnIndex. <strong>NOTE:</strong>the
	 * {@link ViewerCell} provided to this method is no longer valid after this
	 * method returns. Do not cache the cell for future use.
	 *
	 * @param cell
	 *            {@link ViewerCell}
	 */
	/* package */void refresh(ViewerCell cell) {
		CellLabelProvider labelProvider = getLabelProvider();
		if (labelProvider == null) {
			Assert.isTrue(false, "Column " + cell.getColumnIndex() + //$NON-NLS-1$
			" has no label provider."); //$NON-NLS-1$
		}
		labelProvider.update(cell);
	}

	/**
	 * Disposes of the label provider (if set), unregisters the listener and
	 * nulls the references to the label provider and editing support. This
	 * method is called when the underlying widget is disposed. Subclasses may
	 * extend but must call the super implementation.
	 */
	protected void handleDispose() {
		boolean disposeLabelProvider = listenerRegistered;
		CellLabelProvider cellLabelProvider = labelProvider;
		setLabelProvider(null, false);
		if (disposeLabelProvider) {
			cellLabelProvider.dispose(viewer, this);
		}
		editingSupport = null;
		listener = null;
		viewer = null;
	}

	private void handleDispose(ColumnViewer viewer) {
		handleDispose();
		viewer.clearLegacyEditingSetup();
	}

	/**
	 * Returns the viewer of this viewer column.
	 *
	 * @return Returns the viewer.
	 *
	 * @since 3.4
	 */
	public ColumnViewer getViewer() {
		return viewer;
	}
}
