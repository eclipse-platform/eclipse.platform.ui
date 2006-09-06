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
 ******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.jface.util.Policy;
import org.eclipse.swt.widgets.Widget;

/**
 * The ViewerColumn is the implementation of the column parts.
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public final class ViewerColumn {

	private ViewerLabelProvider labelProvider;

	static String COLUMN_VIEWER_KEY = Policy.JFACE + ".columnViewer";//$NON-NLS-1$

	private EditingSupport editingSupport;

	/**
	 * Create a new instance of the receiver at columnIndex.
	 * 
	 * @param columnOwner
	 * @param provider
	 */
	public ViewerColumn(Widget columnOwner, ViewerLabelProvider provider) {
		labelProvider = provider;
		columnOwner.setData(ViewerColumn.COLUMN_VIEWER_KEY, this);
	}

	/**
	 * Return the label provider for the receiver.
	 * 
	 * @return ViewerLabelProvider
	 */
	public ViewerLabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Set the label provider for the column.
	 * @param labelProvider
	 *            the new {@link ViewerLabelProvider}
	 */
	public void setLabelProvider(ViewerLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * Return the editing support for the reciever.
	 * @return {@link EditingSupport}
	 */
	EditingSupport getEditingSupport() {
		return editingSupport;
	}

	/**
	 * Set the editing support.
	 * @param editingSupport
	 *            The {@link EditingSupport} to set.
	 */
	void setEditingSupport(EditingSupport editingSupport) {
		this.editingSupport = editingSupport;
	}

	/**
	 * Refresh the cell for the given columnIndex.
	 * <strong>NOTE:</strong>the {@link ViewerCell}
	 * provided to this method is no longer valid after
	 * this method is exited. Do not cache the cell for
	 * future use.
	 * 
	 * @param cell {@link ViewerCell}
	 */
	public void refresh(ViewerCell cell) {
		getLabelProvider().update(cell);
	}
}
