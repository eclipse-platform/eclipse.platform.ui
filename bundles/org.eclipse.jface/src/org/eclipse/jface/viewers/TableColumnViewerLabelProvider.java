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


/**
 * TableColumnViewerLabelProvider is the mapping from the table based providers
 * to the ViewerLabelProvider.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * @see ITableLabelProvider
 * @see ITableColorProvider
 * @see ITableFontProvider
 * 
 */
class TableColumnViewerLabelProvider extends ViewerLabelProvider {

	private ITableLabelProvider tableLabelProvider;

	private ITableColorProvider tableColorProvider;

	private ITableFontProvider tableFontProvider;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param labelProvider
	 *            instance of a table based label provider
	 * @param columnIndex
	 *            the index into the table for this column
	 * @see ITableLabelProvider
	 * @see ITableColorProvider
	 * @see ITableFontProvider
	 */
	public TableColumnViewerLabelProvider(IBaseLabelProvider labelProvider) {
		super(labelProvider);

		if (labelProvider instanceof ITableLabelProvider)
			tableLabelProvider = (ITableLabelProvider) labelProvider;

		if (labelProvider instanceof ITableColorProvider)
			tableColorProvider = (ITableColorProvider) labelProvider;

		if (labelProvider instanceof ITableFontProvider)
			tableFontProvider = (ITableFontProvider) labelProvider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerLabelProvider#updateLabel(org.eclipse.jface.viewers.ViewerLabel,
	 *      org.eclipse.jface.viewers.ViewerCell)
	 */
	public void updateLabel(ViewerLabel label, ViewerCell cell) {

		Object element = cell.getElement();
		int index = cell.getColumnIndex();

		if (tableLabelProvider == null) {
			label.setText(getLabelProvider().getText(element));
			label.setImage(getLabelProvider().getImage(element));
		} else {
			label.setText(tableLabelProvider.getColumnText(element, index));
			label.setImage(tableLabelProvider.getColumnImage(element, index));
		}

		if (tableColorProvider == null) {
			if (getColorProvider() != null) {
				label.setBackground(getColorProvider().getBackground(element));
				label.setForeground(getColorProvider().getForeground(element));
			}

		} else {
			label.setBackground(tableColorProvider
					.getBackground(element, index));
			label.setForeground(tableColorProvider
					.getForeground(element, index));

		}

		if (tableFontProvider == null) {
			if (getFontProvider() != null)
				label.setFont(getFontProvider().getFont(element));
		} else
			label.setFont(tableFontProvider.getFont(element, index));

	}


}
