/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor.templates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.core.runtime.Assert;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;

/**
 * Layout for tables, adapted from <code>TableLayoutComposite</code>.
 * <p>
 * XXX: Should switch to use {@link org.eclipse.jface.layout.TableColumnLayout}.
 * </p>
 *
 * @since 3.2
 */
final class ColumnLayout extends Layout {

	private static final String RECALCULATE_LAYOUT= "recalculateKey"; //$NON-NLS-1$

	/**
	 * The number of extra pixels taken as horizontal trim by the table column.
	 * To ensure there are N pixels available for the content of the column,
	 * assign N+COLUMN_TRIM for the column width.
	 * <p>
	 * XXX: Should either switch to use
	 * {@link org.eclipse.jface.layout.TableColumnLayout} or get API from JFace
	 * or SWT, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=218483
	 * </p>
	 *
	 * @since 3.1
	 */
	private static int COLUMN_TRIM;
	static {
		String platform= SWT.getPlatform();
		if ("win32".equals(platform)) //$NON-NLS-1$
			COLUMN_TRIM= 4;
		else if ("carbon".equals(platform)) //$NON-NLS-1$
			COLUMN_TRIM= 24;
		else
			COLUMN_TRIM= 3;
	}

	private List columns= new ArrayList();

	/**
	 * Adds a new column of data to this table layout.
	 *
	 * @param data the column layout data
	 */
	public void addColumnData(ColumnLayoutData data) {
		columns.add(data);
	}

	private Point computeTableSize(Table table, int wHint, int hHint) {
		Point result= table.computeSize(wHint, hHint);

		int width= 0;
		int size= columns.size();
		for (int i= 0; i < size; ++i) {
			ColumnLayoutData layoutData= (ColumnLayoutData) columns.get(i);
			if (layoutData instanceof ColumnPixelData) {
				ColumnPixelData col= (ColumnPixelData) layoutData;
				width += col.width;
				if (col.addTrim) {
					width += COLUMN_TRIM;
				}
			} else if (layoutData instanceof ColumnWeightData) {
				ColumnWeightData col= (ColumnWeightData) layoutData;
				width += col.minimumWidth;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}
		if (width > result.x)
			result.x= width;
		return result;
	}

	private void layoutTable(final Table table, final int width, final Rectangle area, final boolean increase) {
		final TableColumn[] tableColumns= table.getColumns();
		final int size= Math.min(columns.size(), tableColumns.length);
		final int[] widths= new int[size];

		final int[] weightIteration= new int[size];
		int numberOfWeightColumns= 0;

		int fixedWidth= 0;
		int minWeightWidth= 0;
		int totalWeight= 0;

		// First calc space occupied by fixed columns
		for (int i= 0; i < size; i++) {
			ColumnLayoutData col= (ColumnLayoutData) columns.get(i);
			if (col instanceof ColumnPixelData) {
				ColumnPixelData cpd= (ColumnPixelData) col;
				int pixels= cpd.width;
				if (cpd.addTrim) {
					pixels += COLUMN_TRIM;
				}
				widths[i]= pixels;
				fixedWidth += pixels;
			} else if (col instanceof ColumnWeightData) {
				ColumnWeightData cw= (ColumnWeightData) col;
				weightIteration[numberOfWeightColumns]= i;
				numberOfWeightColumns++;
				totalWeight += cw.weight;
				minWeightWidth += cw.minimumWidth;
				widths[i]= cw.minimumWidth;
			} else {
				Assert.isTrue(false, "Unknown column layout data"); //$NON-NLS-1$
			}
		}


		// Do we have columns that have a weight?
		final int restIncludingMinWidths= width - fixedWidth;
		final int rest= restIncludingMinWidths - minWeightWidth;
		if (numberOfWeightColumns > 0 && rest > 0) {

			// Modify the weights to reflect what each column already
			// has due to its minimum. Otherwise, columns with low
			// minimums get discriminated.
			int totalWantedPixels= 0;
			final int[] wantedPixels= new int[numberOfWeightColumns];
			for (int i= 0; i < numberOfWeightColumns; i++) {
				ColumnWeightData cw= (ColumnWeightData) columns.get(weightIteration[i]);
				wantedPixels[i]= totalWeight == 0 ? 0 : cw.weight * restIncludingMinWidths / totalWeight;
				totalWantedPixels+= wantedPixels[i];
			}

			// Now distribute the rest to the columns with weight.
			int totalDistributed= 0;
			for (int i= 0; i < numberOfWeightColumns; ++i) {
				int pixels= totalWantedPixels == 0 ? 0 : wantedPixels[i] * rest / totalWantedPixels;
				totalDistributed += pixels;
				widths[weightIteration[i]] += pixels;
			}

			// Distribute any remaining pixels to columns with weight.
			int diff= rest - totalDistributed;
			for (int i= 0; diff > 0; i= ((i + 1) % numberOfWeightColumns)) {
				++widths[weightIteration[i]];
				--diff;
			}
		}

		if (increase) {
			table.setSize(area.width, area.height);
		}
		for (int i= 0; i < size; i++) {
			tableColumns[i].setWidth(widths[i]);
		}
		if (!increase) {
			table.setSize(area.width, area.height);
		}
	}

	/*
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		return computeTableSize(getTable(composite), wHint, hHint);
	}

	/*
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
        Rectangle area= composite.getClientArea();
        Table table= getTable(composite);
        int tableWidth= table.getSize().x;
        int trim= computeTrim(area, table, tableWidth);
        int width= Math.max(0, area.width - trim);

        if (width > 1)
        	layoutTable(table, width, area, tableWidth < area.width);

        if( composite.getData(RECALCULATE_LAYOUT) == null ) {
        	composite.setData(RECALCULATE_LAYOUT, Boolean.FALSE);
        	composite.layout();
        }
	}

	private int computeTrim(Rectangle area, Table table, int tableWidth) {
		Point preferredSize= computeTableSize(table, area.width, area.height);
        int trim;
        if (tableWidth > 1) {
        	trim= tableWidth - table.getClientArea().width;
        } else {
        	// initially, the table has no extend and no client area - use the border with
        	// plus some padding as educated guess
        	trim= 2 * table.getBorderWidth() + 1 ;
        }
        if (preferredSize.y > area.height) {
            // Subtract the scrollbar width from the total column width
            // if a vertical scrollbar will be required, but is not currently showing
        	// (in which case it is already subtracted above)
            ScrollBar vBar= table.getVerticalBar();
            if (!vBar.isVisible()) {
            	Point vBarSize= vBar.getSize();
            	trim += vBarSize.x;
            }
        }
		return trim;
	}

	private Table getTable(Composite composite) {
		return (Table) composite.getChildren()[0];
	}

}
