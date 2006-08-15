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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * TableColumnViewerLabelProvider is the mapping from the table based providers
 * to the ViewerLabelProvider.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 * @since 3.3
 * @see ITableLabelProvider
 * @see ITableColorProvider
 * @see ITableFontProvider
 * 
 */
class TableColumnViewerLabelProvider extends ViewerLabelProvider {
	
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
			setLabelProvider(labelProviderFor((ITableLabelProvider) labelProvider));

		if (labelProvider instanceof ITableColorProvider)
			setColorProvider(colorProviderFor((ITableColorProvider) labelProvider));

		if (labelProvider instanceof ITableFontProvider)
			setFontProvider(fontProviderFor((ITableFontProvider) labelProvider));
	}


	/**
	 * Return the IFontProvider based on provider at columnIndex.
	 * 
	 * @param provider
	 * @param columnIndex
	 * @return IFontProvider
	 */
	private IFontProvider fontProviderFor(final ITableFontProvider provider) {
		return new IFontProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
			 */
			public Font getFont(Object element) {
				return provider.getFont(element, getColumnIndex());
			}
		};
	}

	/**
	 * Return the ILabelProvider based on provider at columnIndex.
	 * 
	 * @param provider
	 * @param columnIndex
	 * @return ILabelProvider
	 */
	private ILabelProvider labelProviderFor(final ITableLabelProvider provider) {
		return new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return provider.getColumnText(element, getColumnIndex());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
			 */
			public Image getImage(Object element) {
				return provider.getColumnImage(element, getColumnIndex());
			}
		};
	}

	/**
	 * Create an IColorProvider from the ITableColorProvider at columnIndex.
	 * 
	 * @param provider
	 * @param columnIndex
	 * @return IColorProvider
	 */
	private IColorProvider colorProviderFor(final ITableColorProvider provider) {
		return new IColorProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
			 */
			public Color getBackground(Object element) {
				return provider.getBackground(element, getColumnIndex());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
			 */
			public Color getForeground(Object element) {
				return provider.getForeground(element, getColumnIndex());
			}
		};
	}

}
