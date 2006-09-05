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
 * The ViewerColumn is abstract implementation of the column parts.
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
	 * @param labelProvider
	 *            the new label-provider
	 */
	public void setLabelProvider(ViewerLabelProvider labelProvider) {
		this.labelProvider = labelProvider;
	}

	/**
	 * @return Returns the editingSupport.
	 */
	EditingSupport getEditingSupport() {
		return editingSupport;
	}

	/**
	 * @param editingSupport
	 *            The editingSupport to set.
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
	 * @param cell
	 */
	public void refresh(ViewerCell cell) {
		getLabelProvider().update(cell);
	}

	/**
	 * Refresh the TreeItem for element.
	 * 
	 * @param item
	 * @param element
	 * @param columnIndex
	 */
	/*
	 * public void refresh(TreeItem item, Object element, int columnIndex) {
	 * 
	 * ViewerLabel label = new ViewerLabel(item.getText(columnIndex), item
	 * .getImage(columnIndex)); getLabelProvider().updateLabel(label, element,
	 * columnIndex); // We can not make a null check because then we could not //
	 * set the items back to default state item.setBackground(columnIndex,
	 * label.getBackground()); item.setForeground(columnIndex,
	 * label.getForeground()); item.setFont(columnIndex, label.getFont());
	 * 
	 * if (label.hasNewText()) item.setText(columnIndex, label.getText());
	 * 
	 * if (label.hasNewImage()) item.setImage(columnIndex, label.getImage()); }
	 */
	/**
	 * Refresh the TableItem for element.
	 * 
	 * @param item
	 * @param element
	 * @param columnIndex
	 */
	/*
	 * public void refresh(TableItem item, Object element, int columnIndex) {
	 * 
	 * ViewerLabel label = new ViewerLabel(item.getText(columnIndex), item
	 * .getImage(columnIndex)); getLabelProvider().updateLabel(label, element,
	 * columnIndex);
	 * 
	 * if (label.hasNewBackground()) item.setBackground(columnIndex,
	 * label.getBackground());
	 * 
	 * if (label.hasNewForeground()) item.setForeground(columnIndex,
	 * label.getForeground());
	 * 
	 * if (label.hasNewFont()) item.setFont(columnIndex, label.getFont());
	 * 
	 * if (label.hasNewText()) item.setText(columnIndex, label.getText());
	 * 
	 * if (label.hasNewImage()) item.setImage(columnIndex, label.getImage()); }
	 */
}
