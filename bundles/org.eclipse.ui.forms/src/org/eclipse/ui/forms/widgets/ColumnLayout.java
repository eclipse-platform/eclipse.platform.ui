/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     dinko.ivanov@sap.com - patch #70790
 *     RasmussenJamie@comcast.net - patch for Bug 184345
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.forms.widgets.ColumnLayoutUtils;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
/**
 * This layout manager arranges children of the composite parent in vertical
 * columns. All the columns are identical size and children are stretched
 * horizontally to fill the column width. The goal is to give layout some
 * reasonable range of column numbers to allow it to handle various parent
 * widths. That way, column number will drop to the lowest number in the range
 * when width decreases, and grow up to the highest number in the range when
 * allowed by the parent width.
 * <p>
 * In addition, the layout attempts to 'fill the space' equally i.e. to avoid
 * large gaps at the and of the last column.
 * <p>
 * Child controls are layed out according to their 'natural' (preferred) size.
 * For 'stretchy' controls that do not have natural preferred size, it is
 * possible to set width and/or height hints using ColumnLayoutData objects.
 *
 * @see ColumnLayoutData
 * @since 3.0
 */
public final class ColumnLayout extends Layout implements ILayoutExtension {
	/**
	 * Minimum number of columns (default is 1).
	 */
	public int minNumColumns = 1;
	/**
	 * Maximum number of columns (default is 3).
	 */
	public int maxNumColumns = 3;
	/**
	 * Horizontal spacing between columns (default is 5).
	 */
	public int horizontalSpacing = 5;
	/**
	 * Vertical spacing between controls (default is 5).
	 */
	public int verticalSpacing = 5;
	/**
	 * Top margin (default is 5).
	 */
	public int topMargin = 5;
	/**
	 * Left margin (default is 5).
	 */
	public int leftMargin = 5;
	/**
	 * Bottom margin (default is 5).
	 */
	public int bottomMargin = 5;
	/**
	 * Right margin (default is 5).
	 */
	public int rightMargin = 5;

	private LayoutCache cache = new LayoutCache();

	private final static int MIN_SIZE = -2;

	/**
	 * Creates a new instance of the column layout.
	 */
	public ColumnLayout() {
	}

	private void updateCache(Composite composite, boolean flushCache) {
		Control[] children = composite.getChildren();
		if (flushCache) {
			cache.flush();
		}
		cache.setControls(children);
	}

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		updateCache(composite, flushCache);
		return computeSize(composite, wHint, hHint);
	}

	/**
	 * Given a desired number of columns, this returns a clamped result that falls
	 * within the range specified by the minimum and maximum number of columns.
	 */
	private int clampNumColumns(Composite parent, int desiredNumColumns) {
		int ncolumns = desiredNumColumns;
		ncolumns = Math.min(ncolumns, parent.getChildren().length);
		ncolumns = Math.min(ncolumns, maxNumColumns);
		ncolumns = Math.max(ncolumns, minNumColumns);
		ncolumns = Math.max(ncolumns, 1);
		return ncolumns;
	}

	private int computeOptimalNumColumnsForWidth(Composite parent, int width) {
		if (minNumColumns >= maxNumColumns || parent.getChildren().length <= minNumColumns) {
			return clampNumColumns(parent, minNumColumns);
		}

		Control[] children = parent.getChildren();
		int minColWidth = 0;

		for (int i = 0; i < children.length; i++) {
			// To maximize the number of columns:
			int nextWidth = computeMinimumWidth(i);

			// To minimize the number of columns:
			// int nextWidth = computeControlSize(i, SWT.DEFAULT).x;

			minColWidth = Math.max(minColWidth, nextWidth);
		}

		return clampNumColumns(parent,
				(width - leftMargin - rightMargin + horizontalSpacing) / (minColWidth + horizontalSpacing));
	}

	private int computeColumnWidthForNumColumns(int layoutWidth, int numColumns) {
		return ((layoutWidth - leftMargin - rightMargin) - (numColumns - 1) * horizontalSpacing) / numColumns;
	}

	private Point computeSize(Composite parent, int wHint, int hHint) {
		Control[] children = parent.getChildren();
		int cheight = 0;
		Point[] sizes = new Point[children.length];

		int columnWidth = 0;
		int nColumns;
		if (wHint == SWT.DEFAULT) {
			nColumns = clampNumColumns(parent, maxNumColumns);

			for (int i = 0; i < children.length; i++) {
				columnWidth = Math.max(columnWidth, computeControlSize(i, SWT.DEFAULT).x);
			}
		} else if (wHint == MIN_SIZE) {
			nColumns = clampNumColumns(parent, 0);

			for (int i = 0; i < children.length; i++) {
				columnWidth = Math.max(columnWidth, computeMinimumWidth(i));
			}
		} else {
			nColumns = computeOptimalNumColumnsForWidth(parent, wHint);
			columnWidth = computeColumnWidthForNumColumns(wHint, nColumns);
		}

		for (int i = 0; i < children.length; i++) {
			sizes[i] = computeControlSize(i, columnWidth);
			cheight += sizes[i].y;
		}

		int perColHeight = ColumnLayoutUtils.computeColumnHeight(nColumns, sizes, cheight, verticalSpacing);
		int colHeight = 0;
		int[] heights = new int[nColumns];
		int ncol = 0;

		boolean fillIn = false;

		for (int i = 0; i < sizes.length; i++) {
			int childHeight = sizes[i].y;
			if (i>0 && colHeight + childHeight > perColHeight) {
				heights[ncol] = colHeight;
				ncol++;
				if (ncol == nColumns || fillIn) {
					// overflow - start filling in
					fillIn = true;
					ncol = findShortestColumn(heights);
				}
				colHeight = heights[ncol];
			}
			if (colHeight > 0)
				colHeight += verticalSpacing;
			colHeight += childHeight;
		}
		heights[ncol] = Math.max(heights[ncol],colHeight);

		Point size = new Point(0, 0);
		for (int i = 0; i < nColumns; i++) {
			size.y = Math.max(size.y, heights[i]);
		}
		size.x = columnWidth * nColumns + (nColumns - 1) * horizontalSpacing;
		size.x += leftMargin + rightMargin;
		size.y += topMargin + bottomMargin;
		if (hHint != SWT.DEFAULT) {
			size.y = hHint;
		}
		return size;
	}

	private int computeMinimumWidth(int i) {
		SizeCache sc = cache.getCache(i);
		return sc.computeMinimumWidth();
	}

	private Point computeControlSize(int controlIndex, int wHint) {
		SizeCache sizeCache = cache.getCache(controlIndex);
		Control c = sizeCache.getControl();
		ColumnLayoutData cd = (ColumnLayoutData) c.getLayoutData();

		if (cd != null) {
			return FormUtil.computeControlSize(sizeCache, wHint, cd.widthHint, cd.heightHint,
					cd.horizontalAlignment == ColumnLayoutData.FILL);
		}
		return FormUtil.computeControlSize(sizeCache, wHint, SWT.DEFAULT, SWT.DEFAULT, true);
	}

	private int findShortestColumn(int[] heights) {
		int result = 0;
		int height = Integer.MAX_VALUE;
		for (int i = 0; i < heights.length; i++) {
			if (height > heights[i]) {
				height = heights[i];
				result = i;
			}
		}
		return result;
	}

	@Override
	protected void layout(Composite parent, boolean flushCache) {
		updateCache(parent, flushCache);
		Control[] children = parent.getChildren();
		Rectangle carea = parent.getClientArea();
		int nColumns = computeOptimalNumColumnsForWidth(parent, carea.width);
		int columnWidth = computeColumnWidthForNumColumns(carea.width, nColumns);

		int cheight = 0;
		Point[] sizes = new Point[children.length];
		for (int i = 0; i < children.length; i++) {
			sizes[i] = computeControlSize(i, columnWidth);
			cheight += sizes[i].y;
		}

		int perColHeight = ColumnLayoutUtils.computeColumnHeight(nColumns, sizes, cheight, verticalSpacing);

		int colHeight = 0;
		int[] heights = new int[nColumns];
		int ncol = 0;
		int x = leftMargin;
		boolean fillIn = false;

		for (int i = 0; i < sizes.length; i++) {
			Control child = children[i];
			Point csize = sizes[i];
			ColumnLayoutData cd = (ColumnLayoutData) child.getLayoutData();
			int align = cd != null ? cd.horizontalAlignment : ColumnLayoutData.FILL;
			int childWidth = csize.x;

			if (i>0 && colHeight + csize.y > perColHeight) {
				heights[ncol] = colHeight;
				if (fillIn || ncol == nColumns - 1) {
					// overflow - start filling in
					fillIn = true;
					ncol = findShortestColumn(heights);

					x = leftMargin + ncol * (columnWidth + horizontalSpacing);

				}
				else {
					ncol++;
					x += columnWidth + horizontalSpacing;
				}
				colHeight = heights[ncol];
			}
			if (colHeight > 0)
				colHeight += verticalSpacing;


			switch (align) {
				case ColumnLayoutData.LEFT :
				case ColumnLayoutData.FILL :
					child.setBounds(x, topMargin+colHeight, childWidth, csize.y);
					break;
				case ColumnLayoutData.RIGHT :
				child.setBounds(x + columnWidth - childWidth, topMargin + colHeight, childWidth, csize.y);
					break;
				case ColumnLayoutData.CENTER :
				child.setBounds(x + columnWidth / 2 - childWidth / 2, topMargin + colHeight, childWidth, csize.y);
					break;
			}

			colHeight += csize.y;
		}
	}

	@Override
	public int computeMaximumWidth(Composite parent, boolean changed) {
		return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
	}

	@Override
	public int computeMinimumWidth(Composite parent, boolean changed) {
		updateCache(parent, changed);
		return computeSize(parent, MIN_SIZE, SWT.DEFAULT).x;
	}
}
