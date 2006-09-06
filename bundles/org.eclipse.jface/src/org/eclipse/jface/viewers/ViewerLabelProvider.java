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
 * The ViewerLabelProvider is an abstract implementation of a label provider for
 * structured viewers.
 * 
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 */
public class ViewerLabelProvider extends LabelProvider implements
		ILabelProvider, IColorProvider, IFontProvider {

	private static ILabelProvider defaultLabelProvider = new LabelProvider();

	private ILabelProvider labelProvider = defaultLabelProvider;

	private IColorProvider colorProvider;

	private IFontProvider fontProvider;

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
		if (labelProvider instanceof ViewerLabelProvider)
			return (ViewerLabelProvider) labelProvider;
		return new ViewerLabelProvider(labelProvider);

	}

	/**
	 * Updates the label for the given cell.
	 * 
	 * @param label
	 *            the {@link ViewerLabel} to update
	 * @param cell
	 *            the {@link ViewerCell} to update
	 */
	public void updateLabel(ViewerLabel label, ViewerCell cell) {

		Object element = cell.getElement();

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
	 * Set the colorProvider to use for the receiver.
	 * 
	 * @param colorProvider
	 *            {@link IColorProvider} The colorProvider to set. This value
	 *            may be <code>null</code>.
	 */
	public void setColorProvider(IColorProvider colorProvider) {
		this.colorProvider = colorProvider;
	}

	/**
	 * Set the font provider.
	 * 
	 * @param fontProvider
	 *            {@link IFontProvider} The fontProvider to set. This value may
	 *            be <code>null</code>.
	 */
	public void setFontProvider(IFontProvider fontProvider) {
		this.fontProvider = fontProvider;
	}

	/**
	 * Set the labelProvider to be provider.
	 * 
	 * @param provider
	 *            {@link ILabelProvider} provider to set. This value may be
	 *            <code>null</code>.
	 */
	public void setLabelProvider(ILabelProvider provider) {
		this.labelProvider = provider;
	}

	/**
	 * Set the any providers for the receiver that can be adapted from provider.
	 * 
	 * @param provider
	 *            {@link Object}
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
	 * @return {@link IColorProvider} or <code>null</code>
	 */
	public IColorProvider getColorProvider() {
		return colorProvider;
	}

	/**
	 * Get the IFontProvider for the receiver.
	 * 
	 * @return {@link IFontProvider} or <code>null</code>
	 */
	public IFontProvider getFontProvider() {
		return fontProvider;
	}

	/**
	 * Return the label provider for the receiver.
	 * 
	 * @return {@link ILabelProvider}
	 */
	public ILabelProvider getLabelProvider() {
		return labelProvider;
	}

	/**
	 * Get the image displayed in the tooltip for object.
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return {@link Image} or <code>null</code> if there is not image.
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
	 * @return the {@link String} to be displayed in the tooltip, or
	 *         <code>null</code> if a custom tooltip should not be displayed
	 */
	public String getTooltipText(Object element) {
		return null;
	}

	/**
	 * Return the background color used for the tooltip
	 * 
	 * @param object
	 *            the {@link Object} for which the tooltip is shown
	 * 
	 * @return the {@link Color} used or <code>null</code> if you want to use
	 *         the
	 */
	public Color getTooltipBackgroundColor(Object object) {
		return null;
	}

	/**
	 * The foreground color used to display the the text in the tooltip
	 * 
	 * @param object
	 *            the {@link Object} for which the tooltip is shown
	 * @return the {@link Color} used or <code>null</code> if you want to use
	 *         the default color {
	 * @link SWT#COLOR_INFO_FOREGROUND}
	 * @see SWT#COLOR_INFO_FOREGROUND
	 */
	public Color getTooltipForegroundColor(Object object) {
		return null;
	}

	/**
	 * Get the {@link Font} used to display the tooltip
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return {@link Font} or <code>null</code> if the default font is to be
	 *         used.
	 */
	public Font getTooltipFont(Object object) {
		return null;
	}

	/**
	 * Return the amount the used to control how much the tooltip is shifted
	 * from the current mouse position
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return {@link Point} shift of the tooltip or <code>null</code> if
	 *         there is to be no shift.
	 */
	public Point getTooltipShift(Object object) {
		return null;
	}

	/**
	 * Return whether or not to use the native Tooltip. If native tooltips are
	 * used only the textvalue is used. All other feature are of custom tooltips
	 * are not supported.
	 * 
	 * @param object
	 *            the {@link Object} for which the tooltip is shown
	 * @return <code>true</code> if native tooltips should be used
	 */
	public boolean useNativeTooltip(Object object) {
		return false;
	}

	/**
	 * The time in milliseconds the tooltip is shown for.
	 * 
	 * @param object
	 *            the {@link Object} for which the tooltip is shown
	 * @return <code>int<code>
	 */
	public int getTooltipTimeDisplayed(Object object) {
		return 0;
	}

	/**
	 * The time in milliseconds until the tooltip is displaued.
	 * 
	 * @param object
	 *            the {@link Object} for which the tooltip is shown
	 * @return int
	 */
	public int getTooltipDisplayDelayTime(Object object) {
		return 0;
	}

	/**
	 * The {@link SWT} style used to create the label
	 * 
	 * @param object
	 *            the element for which the tooltip is shown
	 * @return int
	 * @see SWT#SHADOW_NONE
	 */
	public int getTooltipStyle(Object object) {
		return SWT.SHADOW_NONE;
	}

	/**
	 * Update the label for cell.
	 * 
	 * @param cell {@link ViewerCell}
	 */
	public void update(ViewerCell cell) {
		ViewerLabel label = new ViewerLabel(cell.getText(), cell.getImage());
		updateLabel(label, cell);

		cell.setBackground(label.getBackground());
		cell.setForeground(label.getForeground());
		cell.setFont(label.getFont());

		if (label.hasNewText())
			cell.setText(label.getText());

		if (label.hasNewImage())
			cell.setImage(label.getImage());

	}

}
