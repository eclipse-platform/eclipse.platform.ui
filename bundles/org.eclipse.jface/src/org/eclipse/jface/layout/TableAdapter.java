/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   IBM Corporation - initial API and implementation
 *     Tom Schindl <tom.schindl@bestsolution.at> - fixed scrollbar flashing
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * The TableAdapter is a ControlAdapter used to set the size of a table in
 * a consistent way even during a resize unlike a {@link TableLayout} which only
 * sets initial sizes.
 * 
 * @since 3.3
 */
public class TableAdapter extends ControlAdapter {
	private Table table;

	/**
	 * Create a new instance of the receiver with the table to create specified.
	 * 
	 * @param control
	 */
	public TableAdapter(Table control) {
		table = control;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
	 */
	public void controlResized(ControlEvent e) {
		Rectangle area = table.getParent().getClientArea();

		int width = area.width - 2 * table.getBorderWidth();
		Point preferredSize = table.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (preferredSize.y > area.height) {
			// Subtract the scrollbar width from the total column width
			// if a vertical scrollbar will be required
			Point vBarSize = table.getVerticalBar().getSize();
			width -= vBarSize.x;
		}

		// Layout is being called with an invalid value the first time
		// it is being called on Linux. This method resets the
		// Layout to null so we make sure we run it only when
		// the value is OK.
		if (width <= 1)
			return;

		TableColumn[] tableColumns = table.getColumns();
		int size = Math.min(columns.size(), tableColumns.length);
		int[] widths = new int[size];
		int fixedWidth = 0;
		int numberOfWeightColumns = 0;
		int totalWeight = 0;

		// First calc space occupied by fixed columns
		for (int i = 0; i < size; i++) {
			ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
			if (col instanceof ColumnPixelData) {
				int pixels = ((ColumnPixelData) col).width;
				widths[i] = pixels;
				fixedWidth += pixels;
			} else if (col instanceof ColumnWeightData) {
				ColumnWeightData cw = (ColumnWeightData) col;
				numberOfWeightColumns++;
				// first time, use the weight specified by the column data,
				// otherwise use the actual width as the weight
				// int weight = firstTime ? cw.weight :
				tableColumns[i].getWidth();
				int weight = cw.weight;
				totalWeight += weight;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}

		// Do we have columns that have a weight
		if (numberOfWeightColumns > 0) {
			// Now distribute the rest to the columns with weight.
			int rest = width - fixedWidth;
			int totalDistributed = 0;
			for (int i = 0; i < size; ++i) {
				ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					ColumnWeightData cw = (ColumnWeightData) col;
					// calculate weight as above
					// int weight = firstTime ? cw.weight :
					tableColumns[i].getWidth();
					int weight = cw.weight;
					int pixels = totalWeight == 0 ? 0 : weight * rest
							/ totalWeight;
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
				ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnWeightData) {
					++widths[i];
					--diff;
				}
			}
		}

		Point oldSize = table.getSize();

		if (!(oldSize.x > area.width)) {
			table.setSize(area.width, area.height);
		}

		table.setRedraw(false);

		for (int i = 0; i < size; i++) {
			tableColumns[i].setWidth(widths[i]);
		}

		table.setRedraw(true);

		if (oldSize.x > area.width) {
			table.setSize(area.width, area.height);
		}
	}

}
