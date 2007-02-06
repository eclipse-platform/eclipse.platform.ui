/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - API refactoring and general maintenance
 *******************************************************************************/
package org.eclipse.jface.layout;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Scrollable;

/**
 * The AbstractColumnAdapter is a ControlAdapter used to set the size of a table
 * in a consistent way even during a resize unlike a {@link TableLayout} which
 * only sets initial sizes. You can only add the adapter to a container whose
 * <b>only</b> child is the table/tree control you want the layouts applied to.
 * 
 * @deprecated This class will be replaced during the 3.3 M6 development cycle -
 *             for details see bug 171824
 * @since 3.3 <strong>EXPERIMENTAL</strong> This class or interface has been
 *        added as part of a work in progress. This API may change at any given
 *        time. Please do not use this API without consulting with the
 *        Platform/UI team.
 */
public abstract class AbstractColumnAdapter extends ControlAdapter {
	
	private static int COLUMN_TRIM = "carbon".equals(SWT.getPlatform()) ? 24 : 3; //$NON-NLS-1$

	private Scrollable columnControl;

	/**
	 * Create a new instance of the receiver with the table to create specified.
	 * 
	 * @param control
	 */
	public AbstractColumnAdapter(Scrollable control) {
		columnControl = control;
	}

	/**
	 * The list of column layout data (element type:
	 * <code>ColumnLayoutData</code>).
	 */
	private ArrayList columns = new ArrayList();

	/**
	 * Adds a new column of data.
	 * 
	 * @param data
	 *            the column layout data
	 */
	public void addColumnData(ColumnLayoutData data) {
		columns.add(data);
	}

	/**
	 * Compute the size of the scrollable.
	 * @param scrollable
	 * @return {@link Point}
	 */
	private Point computeSize(Scrollable scrollable) {
		Point result = scrollable.computeSize(SWT.DEFAULT, SWT.DEFAULT);

		int width = 0;
		int size = columns.size();
		for (int i = 0; i < size; ++i) {
			ColumnLayoutData layoutData = (ColumnLayoutData) columns.get(i);
			if (layoutData instanceof ColumnPixelData) {
				ColumnPixelData col = (ColumnPixelData) layoutData;
				width += col.width;
				if (col.addTrim) {
					width += COLUMN_TRIM;
				}
			} else if (layoutData instanceof ColumnWeightData) {
				ColumnWeightData col = (ColumnWeightData) layoutData;
				width += col.minimumWidth;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}
		if (width > result.x)
			result.x = width;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {
		Assert
				.isTrue(columnControl.getParent().getChildren().length == 1,
						"The parent container can only hold the ColumnWidget as it's child"); //$NON-NLS-1$

		Rectangle area = columnControl.getParent().getClientArea();

		int width = area.width - 2 * columnControl.getBorderWidth();
		Point preferredSize = computeSize(columnControl);
		if (preferredSize.y > area.height) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = columnControl.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		// Layout is being called with an invalid value the first time
		// it is being called on Linux. This method resets the
		// Layout to null so we make sure we run it only when
		// the value is OK.
		if (width <= 1)
			return;

		int size = Math.min(columns.size(), getColumnCount());
		int[] widths = new int[size];
		int fixedWidth = 0;
		int numberOfWeightColumns = 0;
		int totalWeight = 0;

		ColumnPixelData pxData;
		int pixels;

		// First calc space occupied by fixed columns
		for (int i = 0; i < size; i++) {
			ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
			if (col instanceof ColumnPixelData) {
				pxData = (ColumnPixelData) col;
				pixels = pxData.width;
				widths[i] = pixels;
				fixedWidth += pixels;
				if (pxData.addTrim) {
					fixedWidth += COLUMN_TRIM;
				}
			} else if (col instanceof ColumnWeightData) {
				ColumnWeightData cw = (ColumnWeightData) col;
				numberOfWeightColumns++;
				// first time, use the weight specified by the column data,
				// otherwise use the actual width as the weight
				// int weight = firstTime ? cw.weight :
				int weight = cw.weight;
				totalWeight += weight;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}

		int weight;
		ColumnLayoutData col;
		ColumnWeightData cw;
		// Do we have columns that have a weight
		if (numberOfWeightColumns > 0) {
			// Now distribute the rest to the columns with weight.
			int rest = width - fixedWidth;
			int totalDistributed = 0;
			for (int i = 0; i < size; ++i) {
				col = (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					cw = (ColumnWeightData) col;
					// calculate weight as above
					// int weight = firstTime ? cw.weight :
					weight = cw.weight;
					pixels = totalWeight == 0 ? 0 : weight * rest / totalWeight;
					if (pixels < cw.minimumWidth)
						pixels = cw.minimumWidth;
					totalDistributed += pixels;
					widths[i] = pixels;
				}
			}

			// Distribute any remaining pixels to columns with weight.
			int diff = rest - totalDistributed;
			for (int i = 0; diff > 0; ++i) {
				if (i == size)
					i = 0;
				col = (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					++widths[i];
					--diff;
				}
			}
		}

		Point oldSize = columnControl.getSize();

		if (!(oldSize.x > area.width)) {
			columnControl.setSize(area.width, area.height);
		}

		columnControl.setRedraw(false);
		setColumnWidths(widths);
		columnControl.setRedraw(true);

		if (oldSize.x > area.width) {
			columnControl.setSize(area.width, area.height);
		}
	}

	/**
	 * Get the number of columns for the receiver.
	 * @return int
	 */
	abstract int getColumnCount();

	/**
	 * Set the widths of the columns.
	 * @param widths
	 */
	abstract void setColumnWidths(int[] widths);

	/**
	 * Return the control being wrapped by this adapter.
	 * @return Scrollable
	 */
	Scrollable getControl() {
		return columnControl;
	}
}
