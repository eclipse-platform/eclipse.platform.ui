/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Shindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * The CellLabelProvider is an abstract implementation of a label provider for
 * structured viewers.
 * 
 * @since 3.3
 */
public abstract class CellLabelProvider extends BaseLabelProvider  {


	/**
	 * Create a new instance of the receiver.
	 */
	public CellLabelProvider() {
		super();
	}

	/**
	 * Create a ViewerLabelProvider for the column at index
	 * 
	 * @param labelProvider
	 *            The labelProvider to convert
	 * @return ViewerLabelProvider
	 */
	/* package */ static CellLabelProvider createViewerLabelProvider(
			IBaseLabelProvider labelProvider) {

		if (labelProvider instanceof ITableLabelProvider
				|| labelProvider instanceof ITableColorProvider
				|| labelProvider instanceof ITableFontProvider)
			return new TableColumnViewerLabelProvider(labelProvider);
		if (labelProvider instanceof CellLabelProvider)
			return (CellLabelProvider) labelProvider;
		return new WrappedViewerLabelProvider(labelProvider);

	}

	
	/**
	 * Get the image displayed in the tool tip for object.
	 * 
	 * @param object
	 *            the element for which the tool tip is shown
	 * @return {@link Image} or <code>null</code> if there is not image.
	 */

	public Image getToolTipImage(Object object) {
		return null;
	}

	/**
	 * Returns the tool tip text for the given element and column index, or
	 * <code>null</code> if a custom tool tip should not be displayed.
	 * 
	 * @param element
	 *            the element for which the tool tip is shown
	 * @return the {@link String} to be displayed in the tool tip, or
	 *         <code>null</code> if a custom tool tip should not be displayed
	 */
	public String getToolTipText(Object element) {
		return null;
	}

	/**
	 * Return the background color used for the tool tip
	 * 
	 * @param object
	 *            the {@link Object} for which the tool tip is shown
	 * 
	 * @return the {@link Color} used or <code>null</code> if you want to use
	 *         the
	 */
	public Color getToolTipBackgroundColor(Object object) {
		return null;
	}

	/**
	 * The foreground color used to display the the text in the tool tip
	 * 
	 * @param object
	 *            the {@link Object} for which the tool tip is shown
	 * @return the {@link Color} used or <code>null</code> if you want to use
	 *         the default color {@link SWT#COLOR_INFO_FOREGROUND}
	 * @see SWT#COLOR_INFO_FOREGROUND
	 */
	public Color getToolTipForegroundColor(Object object) {
		return null;
	}

	/**
	 * Get the {@link Font} used to display the tool tip
	 * 
	 * @param object
	 *            the element for which the tool tip is shown
	 * @return {@link Font} or <code>null</code> if the default font is to be
	 *         used.
	 */
	public Font getToolTipFont(Object object) {
		return null;
	}

	/**
	 * Return the amount of pixels in x and y direction you want the tool tip to
	 * pop up from the mouse pointer. The default shift is 10px right and 0px
	 * below your mouse cursor. Be aware of the fact that you should at least
	 * position the tool tip 1px right to your mouse cursor else click events
	 * may not get propagated properly.
	 * 
	 * @param object
	 *            the element for which the tool tip is shown
	 * @return {@link Point} shift of the tool tip or <code>null</code> if the
	 *         default shift should be used.
	 */
	public Point getToolTipShift(Object object) {
		return null;
	}

	/**
	 * Return whether or not to use the native tool tip. If native tool tips are
	 * used only the textvalue is used. All other feature of custom tool tips
	 * are not supported.
	 * 
	 * @param object
	 *            the {@link Object} for which the tool tip is shown
	 * @return <code>true</code> if native tool tips should be used
	 */
	public boolean useNativeToolTip(Object object) {
		return false;
	}

	/**
	 * The time in milliseconds the tool tip is shown for.
	 * 
	 * @param object
	 *            the {@link Object} for which the tool tip is shown
	 * @return <code>int<code>
	 */
	public int getToolTipTimeDisplayed(Object object) {
		return 0;
	}

	/**
	 * The time in milliseconds until the tool tip is displaued.
	 * 
	 * @param object
	 *            the {@link Object} for which the tool tip is shown
	 * @return int
	 */
	public int getToolTipDisplayDelayTime(Object object) {
		return 0;
	}

	/**
	 * The {@link SWT} style used to create the {@link CLabel} (see there for supported styles). 
	 * By default {@link SWT#SHADOW_NONE} is used.
	 * 
	 * @param object
	 *            the element for which the tool tip is shown
	 * @return int
	 * @see CLabel
	 */
	public int getToolTipStyle(Object object) {
		return SWT.SHADOW_NONE;
	}

	/**
	 * Update the label for cell.
	 * 
	 * @param cell {@link ViewerCell}
	 */
	public abstract void update(ViewerCell cell);

}
