/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 * 												  fix for bug 163317
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class represent a column of a {@link ColumnViewer}. Label
 * providers and editing support can be configured for each column separately.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public abstract class ViewerColumn {

	private CellLabelProvider labelProvider;

	static String COLUMN_VIEWER_KEY = Policy.JFACE + ".columnViewer";//$NON-NLS-1$

	private EditingSupport editingSupport;

	private ILabelProviderListener listener;

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
		columnOwner.setData(ViewerColumn.COLUMN_VIEWER_KEY, this);
		this.listener = new ILabelProviderListener() {

			public void labelProviderChanged(LabelProviderChangedEvent event) {
				viewer.handleLabelProviderChanged(event);
			}

		};
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
	 * Set the label provider for the column.
	 * 
	 * @param labelProvider
	 *            the new {@link CellLabelProvider}
	 */
	public void setLabelProvider(CellLabelProvider labelProvider) {
		if (this.labelProvider != null) {
			this.labelProvider.removeListener(listener);
		}

		this.labelProvider = labelProvider;
		this.labelProvider.addListener(listener);
	}

	/**
	 * Return the editing support for the reciever.
	 * 
	 * @return {@link EditingSupport}
	 */
	/* package */ EditingSupport getEditingSupport() {
		return editingSupport;
	}

	/**
	 * Set the editing support.
	 * 
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
	/* package */ void refresh(ViewerCell cell) {
		getLabelProvider().update(cell);
	}
}
