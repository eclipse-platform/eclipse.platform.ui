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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * The ViewerLabelProvider is an abstract implementation of a 
 * label provider for structured viewers.
 * @since 3.3
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as
 * part of a work in progress. This API may change at any given time. Please 
 * do not use this API without consulting with the Platform/UI team.
 */
public class ViewerLabelProvider extends LabelProvider implements
		ILabelProvider, IColorProvider, IFontProvider {

	private static ILabelProvider defaultLabelProvider = new LabelProvider() {

	};

	private ILabelProvider labelProvider = defaultLabelProvider;

	private IColorProvider colorProvider;

	private IFontProvider fontProvider;

	private int columnIndex;

	/**
	 * Create a new instance of the receiver.
	 */
	public ViewerLabelProvider() {
		super();
	}

	/**
	 * Create a new instance of the receiver based on labelProvider.
	 * 
	 * @param labelProvider
	 */
	public ViewerLabelProvider(IBaseLabelProvider labelProvider) {
		super();
		setProviders(labelProvider);
	}

	/**
	 * Create a ViewerLabelProvider for the column at index
	 * 
	 * @param columnIndex
	 * @param labelProvider
	 *            The labelProvider to convert
	 * @return ViewerLabelProvider
	 */
	static ViewerLabelProvider createViewerLabelProvider(
			IBaseLabelProvider labelProvider) {

		if (labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ITableColorProvider
				|| labelProvider instanceof ITableFontProvider)
			return new TableColumnViewerLabelProvider(labelProvider);
		return new ViewerLabelProvider(labelProvider);

	}

	private void updateLabel(ViewerLabel label, Object element) {
		label.setText(labelProvider.getText(element));
		label.setImage(labelProvider.getImage(element));

		if (colorProvider != null) {
			label.setBackground(colorProvider.getBackground(element));
			label.setForeground(colorProvider.getForeground(element));
		}

		if (fontProvider != null) {
			label.setFont(fontProvider.getFont(element));
		}
	}

	/**
	 * Updates the label for the given element and the given index
	 * 
	 * @param label
	 *            the label to update
	 * @param element
	 *            the element
	 * @param columnIndex
	 *            the column index
	 */
	public void updateLabel(ViewerLabel label, Object element, int columnIndex) {
		setColumnIndex(columnIndex);
		updateLabel(label, element);
	}

	/**
	 * @param columnIndex
	 *            the column-index
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}

	/**
	 * @return Returns the column index
	 */
	public int getColumnIndex() {
		return columnIndex;
	}

	/**
	 * Set the colorProvider to use for the receiver.
	 * 
	 * @param colorProvider
	 *            The colorProvider to set.
	 */
	public void setColorProvider(IColorProvider colorProvider) {
		this.colorProvider = colorProvider;
	}

	/**
	 * Set the fontProvider to fontProvider.
	 * 
	 * @param fontProvider
	 *            The fontProvider to set.
	 */
	public void setFontProvider(IFontProvider fontProvider) {
		this.fontProvider = fontProvider;
	}

	/**
	 * Set the labelProvider to be provider.
	 * 
	 * @param provider
	 *            ILabelProvider provider to set.
	 */
	public void setLabelProvider(ILabelProvider provider) {
		this.labelProvider = provider;
	}

	/**
	 * Set the any providers for the receiver that can be adapted from provider.
	 * 
	 * @param provider
	 */
	public void setProviders(Object provider) {
		if (provider instanceof ILabelProvider)
			setLabelProvider((ILabelProvider) provider);

		if (provider instanceof IColorProvider)
			colorProvider = (IColorProvider) provider;

		if (provider instanceof IFontProvider)
			fontProvider = (IFontProvider) provider;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
	 */
	public Font getFont(Object element) {
		if (fontProvider == null) {
			return null;
		}

		return fontProvider.getFont(element);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		if (colorProvider == null) {
			return null;
		}

		return colorProvider.getBackground(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (colorProvider == null) {
			return null;
		}

		return colorProvider.getForeground(element);
	}

	/**
	 * Get the IColorProvider for the receiver.
	 * 
	 * @return IColorProvider
	 */
	public IColorProvider getColorProvider() {
		return colorProvider;
	}

	/**
	 * Get the IFontProvider for the receiver.
	 * 
	 * @return IFontProvider
	 */
	public IFontProvider getFontProvider() {
		return fontProvider;
	}

	/**
	 * @return Returns the labelProvider.
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * The image displayed in the tooltip
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return the image displayed if null no image is displayed
	 */

	public Image getTooltipImage(Object object) {
		return null;
	}

	/**
	 * Returns the tooltip text for the given element and column index, or
	 * <code>null</code> if a custom tooltip should not be displayed.
	 * 
	 * @param element
	 *            the element for which the tooltip is shown
	 * @return the text to be displayed in the tooltip, or <code>null</code>
	 *         if a custom tooltip should not be displayed
	 */
	public String getTooltipText(Object element) {
		return null;
	}

	/**
	 * The background color used for the tooltip
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * 
	 * @return the color used or <code>null</code> if you want to use the
	 */
	public Color getTooltipBackgroundColor(Object object) {
		return null;
	}

	/**
	 * The foreground color used to display the the text in the tooltip
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return the color used or <code>null</code> if you want to use the
	 *         default color {@link SWT#COLOR_INFO_FOREGROUND}
	 */
	public Color getTooltipForegroundColor(Object object) {
		return null;
	}

	/**
	 * The font used to display the tooltip
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return the font if null default font is used
	 */
	public Font getTooltipFont(Object object) {
		return null;
	}

	/**
	 * This is the amount the used to control how much the tooltip is shifted
	 * from the current mouse position
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return shift of the tooltip
	 */
	public Point getTooltipShift(Object object) {
		return null;
	}

	/**
	 * If you want your tooltip to be using the native Tooltip you can force
	 * this by returning true from this method. If native tooltips are used only
	 * the text-value is used all other feature are of custom tooltips are not
	 * supported.
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return true if native tooltips should be used
	 */
	public boolean useNativeTooltip(Object object) {
		return false;
	}

	/**
	 * The time in milliseconds after the tooltip is hidden
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return milliseconds
	 */
	public int getTooltipTimeDisplayed(Object object) {
		return 0;
	}

	/**
	 * The time in milliseconds until the tooltip pops up
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return milliseconds
	 */
	public int getTooltipDisplayDelayTime(Object object) {
		return 0;
	}

	/**
	 * The style used to create the label
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return style mask
	 */
	public int getTooltipStyle(Object object) {
		return SWT.SHADOW_NONE;
	}

}
