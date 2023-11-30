/*******************************************************************************
 * Copyright (c) 2006, 2023 IBM Corporation and others.
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
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;


/**
 * TableColumnViewerLabelProvider is the mapping from the table based providers
 * to the ViewerLabelProvider.
 *
 * @since 3.33
 * @see ITableLabelProvider
 * @see ITableColorProvider
 * @see ITableFontProvider
 */
public class TableColumnViewerLabelProvider extends WrappedViewerLabelProvider {

	private ITableLabelProvider tableLabelProvider;

	private ITableColorProvider tableColorProvider;

	private ITableFontProvider tableFontProvider;

	/**
	 * Create a new instance of the receiver.
	 *
	 * @param labelProvider
	 *            instance of a table based label provider
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

	@Override
	public void update(ViewerCell cell) {

		Object element = cell.getElement();
		int index = cell.getColumnIndex();

		if (tableLabelProvider == null) {
			cell.setText(getLabelProvider().getText(element));
			cell.setImage(getLabelProvider().getImage(element));
		} else {
			cell.setText(tableLabelProvider.getColumnText(element, index));
			cell.setImage(tableLabelProvider.getColumnImage(element, index));
		}

		if (tableColorProvider == null) {
			if (getColorProvider() != null) {
				cell.setBackground(getColorProvider().getBackground(element));
				cell.setForeground(getColorProvider().getForeground(element));
			}

		} else {
			cell.setBackground(tableColorProvider
					.getBackground(element, index));
			cell.setForeground(tableColorProvider
					.getForeground(element, index));

		}

		if (tableFontProvider == null) {
			if (getFontProvider() != null)
				cell.setFont(getFontProvider().getFont(element));
		} else
			cell.setFont(tableFontProvider.getFont(element, index));

	}


}
