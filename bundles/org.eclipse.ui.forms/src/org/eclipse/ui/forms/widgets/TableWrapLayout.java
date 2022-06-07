/*******************************************************************************
 *  Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.widgets;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.internal.forms.widgets.FormUtil;

/**
 * This implementation of the layout algorithm attempts to position controls in
 * the composite using a two-pass autolayout HTML table algorithm recommended by
 * HTML 4.01 W3C specification (see
 * http://www.w3.org/TR/html4/appendix/notes.html#h-B.5.2.2). The main
 * differences with GridLayout is that it has two passes and that width and
 * height are not calculated in the same pass.
 * <p>
 * The advantage of the algorithm over GridLayout is that it is capable of
 * flowing text controls capable of line wrap. These controls do not have
 * natural 'preferred size'. Instead, they are capable of providing the required
 * height if the width is set. Consequently, this algorithm first calculates the
 * widths that will be assigned to columns, and then passes those widths to the
 * controls to calculate the height. When a composite with this layout is a
 * child of the scrolling composite, they should interact in such a way that
 * reduction in the scrolling composite width results in the reflow and increase
 * of the overall height.
 * <p>
 * If none of the columns contain expandable and wrappable controls, the
 * end-result will be similar to the one provided by GridLayout. The difference
 * will show up for layouts that contain controls whose minimum and maximum
 * widths are not the same.
 *
 * @see TableWrapData
 * @since 3.0
 */
public final class TableWrapLayout extends Layout implements ILayoutExtension {
	/**
	 * Number of columns to use when positioning children (default is 1).
	 */
	public int numColumns = 1;

	/**
	 * Left margin variable (default is 5).
	 */
	public int leftMargin = 5;

	/**
	 * Right margin variable (default is 5).
	 */
	public int rightMargin = 5;

	/**
	 * Top margin variable (default is 5).
	 */
	public int topMargin = 5;

	/**
	 * Botom margin variable (default is 5).
	 */
	public int bottomMargin = 5;

	/**
	 * Horizontal spacing (default is 5).
	 */
	public int horizontalSpacing = 5;

	/**
	 * Vertical spacing (default is 5).
	 */
	public int verticalSpacing = 5;

	/**
	 * If set to <code>true</code>, all the columns will have the same width.
	 * Otherwise, column widths will be computed based on controls in them and
	 * their layout data (default is <code>false</code>).
	 */
	public boolean makeColumnsEqualWidth = false;

	private boolean initialLayout = true;

	private ArrayList<TableWrapData[]> grid;

	private Hashtable<Control, RowSpan> rowspans;

	private int[] minColumnWidths, maxColumnWidths;

	private int widestColumnWidth;

	private int[] growingColumns;

	private int[] growingRows;

	private LayoutCache cache = new LayoutCache();

	private class RowSpan {
		Control child;

		int row;

		int height;

		int totalHeight;

		public RowSpan(Control child, int column, int row) {
			this.child = child;
			this.row = row;
		}

		/*
		 * Updates this row span's height with the given one if it is within
		 * this span.
		 */
		public void update(int currentRow, int rowHeight) {
			TableWrapData td = (TableWrapData) child.getLayoutData();
			// is currentRow within this span?
			if (currentRow >= row && currentRow < row + td.rowspan) {
				totalHeight += rowHeight;
				if (currentRow > row)
					totalHeight += verticalSpacing;
			}
		}

		public int getRequiredHeightIncrease() {
			if (totalHeight < height)
				return height - totalHeight;
			return 0;
		}
	}

	/**
	 * Implements ILayoutExtension. Should not be called directly.
	 *
	 * @see ILayoutExtension
	 */
	@Override
	public int computeMinimumWidth(Composite parent, boolean changed) {

		Control[] children = parent.getChildren();
		if (changed) {
			cache.flush();
		}

		cache.setControls(children);

		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
			changed = true;
			initialLayout = false;
		}
		if (grid == null || changed) {
			changed = true;
			grid = new ArrayList<>();
			createGrid(parent);
		}
		if (minColumnWidths == null)
			minColumnWidths = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			minColumnWidths[i] = 0;
		}
		return internalGetMinimumWidth(parent, changed);
	}

	/**
	 * Implements ILayoutExtension. Should not be called directly.
	 *
	 * @see ILayoutExtension
	 */
	@Override
	public int computeMaximumWidth(Composite parent, boolean changed) {
		Control[] children = parent.getChildren();
		if (changed) {
			cache.flush();
		}

		cache.setControls(children);

		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
			changed = true;
			initialLayout = false;
		}
		if (grid == null || changed) {
			changed = true;
			grid = new ArrayList<>();
			createGrid(parent);
		}
		if (maxColumnWidths == null)
			maxColumnWidths = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			maxColumnWidths[i] = 0;
		}
		return internalGetMaximumWidth(parent, changed);
	}

	/**
	 * @see Layout#layout(Composite, boolean)
	 */
	@Override
	protected void layout(Composite parent, boolean changed) {

		Rectangle clientArea = parent.getClientArea();
		Control[] children = parent.getChildren();
		if (changed) {
			cache.flush();
		}

		if (children.length == 0)
			return;

		cache.setControls(children);

		int parentWidth = clientArea.width;
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
			changed = true;
			initialLayout = false;
		}
		if (grid == null || changed) {
			changed = true;
			grid = new ArrayList<>();
			createGrid(parent);
		}
		resetColumnWidths();
		int minWidth = internalGetMinimumWidth(parent, changed);
		int maxWidth = internalGetMaximumWidth(parent, changed);
		int tableWidth = parentWidth;
		int[] columnWidths;
		if (parentWidth <= minWidth) {
			tableWidth = minWidth;
			if (makeColumnsEqualWidth) {
				columnWidths = new int[numColumns];
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = widestColumnWidth;
				}
			} else
				columnWidths = minColumnWidths;
		} else if (parentWidth > maxWidth) {
			if (growingColumns.length == 0) {
				tableWidth = maxWidth;
				columnWidths = maxColumnWidths;
			} else {
				columnWidths = new int[numColumns];
				int extra = parentWidth - maxWidth;
				int colExtra = extra / growingColumns.length;
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = maxColumnWidths[i];
					if (isGrowingColumn(i)) {
						columnWidths[i] += colExtra;
					}
				}
			}
		} else {
			columnWidths = new int[numColumns];
			if (makeColumnsEqualWidth) {
				int colSpace = tableWidth - leftMargin - rightMargin;
				colSpace -= (numColumns - 1) * horizontalSpacing;
				int col = colSpace / numColumns;
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = col;
				}
			} else {
				columnWidths = assignExtraSpace(tableWidth, maxWidth, minWidth);
			}
		}
		int y = topMargin+clientArea.y;
		int[] rowHeights = computeRowHeights(children, columnWidths, changed);
		for (int i = 0; i < grid.size(); i++) {
			int rowHeight = rowHeights[i];
			int x = leftMargin+clientArea.x;
			TableWrapData[] row = grid.get(i);
			for (int j = 0; j < numColumns; j++) {
				TableWrapData td = row[j];
				if (td.isItemData) {
					Control child = children[td.childIndex];
					placeControl(child, td, x, y, rowHeights, i);
				}
				x += columnWidths[j];
				if (j < numColumns - 1)
					x += horizontalSpacing;
			}
			y += rowHeight + verticalSpacing;
		}
	}

	int[] computeRowHeights(Control[] children, int[] columnWidths,
			boolean changed) {
		int[] rowHeights = new int[grid.size()];
		for (int i = 0; i < grid.size(); i++) {
			TableWrapData[] row = grid.get(i);
			rowHeights[i] = 0;
			for (int j = 0; j < numColumns; j++) {
				TableWrapData td = row[j];
				if (td.isItemData == false) {
					continue;
				}
				Control child = children[td.childIndex];
				int span = td.colspan;
				int cwidth = 0;
				for (int k = j; k < j + span; k++) {
					cwidth += columnWidths[k];
					if (k < j + span - 1)
						cwidth += horizontalSpacing;
				}
				Point size = FormUtil.computeControlSize(cache.getCache(td.childIndex), cwidth - td.indent, td.maxWidth,
						td.maxHeight, isFillAligned(child));
				size.x += td.indent;
				td.compWidth = cwidth;
				if (td.heightHint != SWT.DEFAULT) {
					size = new Point(size.x, td.heightHint);
				}
				td.compSize = size;
				RowSpan rowspan = rowspans.get(child);
				if (rowspan == null) {
					rowHeights[i] = Math.max(rowHeights[i], size.y);
				} else
					rowspan.height = size.y;
			}
			updateRowSpans(i, rowHeights[i]);
		}
		for (Enumeration<RowSpan> enm = rowspans.elements(); enm.hasMoreElements();) {
			RowSpan rowspan = enm.nextElement();
			int increase = rowspan.getRequiredHeightIncrease();
			if (increase == 0)
				continue;
			TableWrapData td = (TableWrapData) rowspan.child.getLayoutData();
			int ngrowing = 0;
			int[] affectedRows = new int[grid.size()];
			for (int growingRow : growingRows) {
				if (growingRow >= rowspan.row
						&& growingRow < rowspan.row + td.rowspan) {
					affectedRows[ngrowing++] = growingRow;
				}
			}
			if (ngrowing == 0) {
				ngrowing = 1;
				affectedRows[0] = rowspan.row + td.rowspan - 1;
			}
			increase += increase % ngrowing;
			int perRowIncrease = increase / ngrowing;
			for (int i = 0; i < ngrowing; i++) {
				int growingRow = affectedRows[i];
				rowHeights[growingRow] += perRowIncrease;
			}
		}
		return rowHeights;
	}

	boolean isGrowingColumn(int col) {
		if (growingColumns == null)
			return false;
		for (int growingColumn : growingColumns) {
			if (col == growingColumn)
				return true;
		}
		return false;
	}

	int[] assignExtraSpace(int tableWidth, int maxWidth, int minWidth) {
		int fixedPart = leftMargin + rightMargin + (numColumns - 1)
				* horizontalSpacing;
		int D = maxWidth - minWidth;
		int W = tableWidth - minWidth;
		int widths[] = new int[numColumns];
		int rem = 0;
		for (int i = 0; i < numColumns; i++) {
			int cmin = minColumnWidths[i];
			int cmax = maxColumnWidths[i];
			int d = cmax - cmin;
			int extra = D != 0 ? (d * W) / D : 0;
			if (i < numColumns - 1) {
				widths[i] = cmin + extra;
				rem += widths[i];
			} else {
				widths[i] = tableWidth - fixedPart - rem;
			}
		}
		return widths;
	}

	void placeControl(Control control, TableWrapData td, int x, int y,
			int[] rowHeights, int row) {
		int xloc = x + td.indent;
		int yloc = y;
		int height = td.compSize.y;
		int colWidth = td.compWidth - td.indent;
		int width = td.compSize.x-td.indent;
		width = Math.min(width, colWidth);
		int slotHeight = rowHeights[row];
		RowSpan rowspan = rowspans.get(control);
		if (rowspan != null) {
			slotHeight = 0;
			for (int i = row; i < row + td.rowspan; i++) {
				if (i > row)
					slotHeight += verticalSpacing;
				slotHeight += rowHeights[i];
			}
		}
		// align horizontally
		if (isFillAligned(control)) {
			width = colWidth;
		} else if (td.align == TableWrapData.CENTER) {
			xloc = x + (colWidth - width) / 2;
		} else if (td.align == TableWrapData.RIGHT) {
			xloc = x + colWidth - width;
		}
		// align vertically
		switch (td.valign) {
		case TableWrapData.MIDDLE:
			yloc = y + (slotHeight - height) / 2;
			break;
		case TableWrapData.BOTTOM:
			yloc = y + slotHeight - height;
			break;
		case TableWrapData.FILL:
			height = slotHeight;
			break;
		default:
			break;
		}
		control.setBounds(xloc, yloc, width, height);
	}

	void createGrid(Composite composite) {
		int row, column, rowFill, columnFill;
		Control[] children;
		TableWrapData spacerSpec;
		ArrayList<Integer> growingCols = new ArrayList<>();
		ArrayList<Integer> growingRows = new ArrayList<>();
		rowspans = new Hashtable<>();
		//
		children = composite.getChildren();
		if (children.length == 0)
			return;
		//
		grid.add(createEmptyRow());
		row = 0;
		column = 0;
		// Loop through the children and place their associated layout specs in
		// the
		// grid. Placement occurs left to right, top to bottom (i.e., by row).
		for (int i = 0; i < children.length; i++) {
			// Find the first available spot in the grid.
			Control child = children[i];
			TableWrapData spec = (TableWrapData) child.getLayoutData();
			while (grid.get(row)[column] != null) {
				column = column + 1;
				if (column >= numColumns) {
					row = row + 1;
					column = 0;
					if (row >= grid.size()) {
						grid.add(createEmptyRow());
					}
				}
			}
			// See if the place will support the widget's horizontal span. If
			// not, go to the
			// next row.
			if (column + spec.colspan - 1 >= numColumns) {
				grid.add(createEmptyRow());
				row = row + 1;
				column = 0;
			}
			// The vertical span for the item will be at least 1. If it is > 1,
			// add other rows to the grid.
			if (spec.rowspan > 1) {
				rowspans.put(child, new RowSpan(child, column, row));
			}
			for (int j = 2; j <= spec.rowspan; j++) {
				if (row + j > grid.size()) {
					grid.add(createEmptyRow());
				}
			}
			// Store the layout spec. Also cache the childIndex. NOTE: That we
			// assume the children of a
			// composite are maintained in the order in which they are created
			// and added to the composite.
			grid.get(row)[column] = spec;
			spec.childIndex = i;
			if (spec.grabHorizontal) {
				updateGrowingColumns(growingCols, spec, column);
			}
			if (spec.grabVertical) {
				updateGrowingRows(growingRows, spec, row);
			}
			// Put spacers in the grid to account for the item's vertical and
			// horizontal
			// span.
			rowFill = spec.rowspan - 1;
			columnFill = spec.colspan - 1;
			for (int r = 1; r <= rowFill; r++) {
				for (int c = 0; c < spec.colspan; c++) {
					spacerSpec = new TableWrapData();
					spacerSpec.isItemData = false;
					grid.get(row + r)[column + c] = spacerSpec;
				}
			}
			for (int c = 1; c <= columnFill; c++) {
				for (int r = 0; r < spec.rowspan; r++) {
					spacerSpec = new TableWrapData();
					spacerSpec.isItemData = false;
					grid.get(row + r)[column + c] = spacerSpec;
				}
			}
			column = column + spec.colspan - 1;
		}
		// Fill out empty grid cells with spacers.
		for (int k = column + 1; k < numColumns; k++) {
			spacerSpec = new TableWrapData();
			spacerSpec.isItemData = false;
			grid.get(row)[k] = spacerSpec;
		}
		for (int k = row + 1; k < grid.size(); k++) {
			spacerSpec = new TableWrapData();
			spacerSpec.isItemData = false;
			grid.get(k)[column] = spacerSpec;
		}
		growingColumns = new int[growingCols.size()];
		for (int i = 0; i < growingCols.size(); i++) {
			growingColumns[i] = growingCols.get(i).intValue();
		}
		this.growingRows = new int[growingRows.size()];
		for (int i = 0; i < growingRows.size(); i++) {
			this.growingRows[i] = growingRows.get(i).intValue();
		}
	}

	private void updateGrowingColumns(ArrayList<Integer> growingColumns,
			TableWrapData spec, int column) {
		int affectedColumn = column + spec.colspan - 1;
		for (Integer col : growingColumns) {
			if (col.intValue() == affectedColumn)
				return;
		}
		growingColumns.add(Integer.valueOf(affectedColumn));
	}

	private void updateGrowingRows(ArrayList<Integer> growingRows, TableWrapData spec,
			int row) {
		int affectedRow = row + spec.rowspan - 1;
		for (Integer irow : growingRows) {
			if (irow.intValue() == affectedRow)
				return;
		}
		growingRows.add(Integer.valueOf(affectedRow));
	}

	private TableWrapData[] createEmptyRow() {
		TableWrapData[] row = new TableWrapData[numColumns];
		for (int i = 0; i < numColumns; i++)
			row[i] = null;
		return row;
	}

	/**
	 * @see Layout#computeSize(Composite, int, int, boolean)
	 */
	@Override
	protected Point computeSize(Composite parent, int wHint, int hHint,
			boolean changed) {
		Control[] children = parent.getChildren();
		if (changed) {
			cache.flush();
		}
		if (children.length == 0) {
			return new Point(0, 0);
		}
		cache.setControls(children);

		int parentWidth = wHint;
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
			changed = true;
			initialLayout = false;
		}
		if (grid == null || changed) {
			changed = true;
			grid = new ArrayList<>();
			createGrid(parent);
		}
		resetColumnWidths();
		int minWidth = internalGetMinimumWidth(parent, changed);
		int maxWidth = internalGetMaximumWidth(parent, changed);

		if (wHint == SWT.DEFAULT)
			parentWidth = maxWidth;

		int tableWidth = parentWidth;
		int[] columnWidths;
		if (parentWidth <= minWidth) {
			tableWidth = minWidth;
			if (makeColumnsEqualWidth) {
				columnWidths = new int[numColumns];
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = widestColumnWidth;
				}
			} else
				columnWidths = minColumnWidths;
		} else if (parentWidth >= maxWidth) {
			if (makeColumnsEqualWidth) {
				columnWidths = new int[numColumns];
				int colSpace = parentWidth - leftMargin - rightMargin;
				colSpace -= (numColumns - 1) * horizontalSpacing;
				int col = colSpace / numColumns;
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = col;
				}
			} else {
				tableWidth = maxWidth;
				columnWidths = maxColumnWidths;
			}
		} else {
			columnWidths = new int[numColumns];
			if (makeColumnsEqualWidth) {
				int colSpace = tableWidth - leftMargin - rightMargin;
				colSpace -= (numColumns - 1) * horizontalSpacing;
				int col = colSpace / numColumns;
				for (int i = 0; i < numColumns; i++) {
					columnWidths[i] = col;
				}
			} else {
				columnWidths = assignExtraSpace(tableWidth, maxWidth, minWidth);
			}
		}
		int innerHeight = 0;
		// compute widths
		for (int i = 0; i < grid.size(); i++) {
			TableWrapData[] row = grid.get(i);
			// assign widths, calculate heights
			int rowHeight = 0;
			for (int j = 0; j < numColumns; j++) {
				TableWrapData td = row[j];
				if (td.isItemData == false) {
					continue;
				}
				Control child = children[td.childIndex];
				int span = td.colspan;
				int cwidth = 0;
				for (int k = j; k < j + span; k++) {
					if (k > j)
						cwidth += horizontalSpacing;
					cwidth += columnWidths[k];
				}
				int cy = td.heightHint;
				if (cy == SWT.DEFAULT) {
					SizeCache controlCache = cache.getCache(td.childIndex);
					Point size = FormUtil.computeControlSize(controlCache, cwidth - td.indent, td.maxWidth,
							td.maxHeight, isFillAligned(child));
					cy = size.y;
				}
				RowSpan rowspan = rowspans.get(child);
				if (rowspan != null) {
					// don't take the height of this child into acount
					// because it spans multiple rows
					rowspan.height = cy;
				} else {
					rowHeight = Math.max(rowHeight, cy);
				}
			}
			updateRowSpans(i, rowHeight);
			if (i > 0)
				innerHeight += verticalSpacing;
			innerHeight += rowHeight;
		}
		if (!rowspans.isEmpty())
			innerHeight = compensateForRowSpans(innerHeight);
		int totalHeight = topMargin + innerHeight + bottomMargin;
		return new Point(tableWidth, totalHeight);
	}

	private void updateRowSpans(int row, int rowHeight) {
		if (rowspans == null || rowspans.isEmpty())
			return;
		for (Enumeration<RowSpan> enm = rowspans.elements(); enm.hasMoreElements();) {
			RowSpan rowspan = enm.nextElement();
			rowspan.update(row, rowHeight);
		}
	}

	private int compensateForRowSpans(int totalHeight) {
		for (Enumeration<RowSpan> enm = rowspans.elements(); enm.hasMoreElements();) {
			RowSpan rowspan = enm.nextElement();
			totalHeight += rowspan.getRequiredHeightIncrease();
		}
		return totalHeight;
	}

	int internalGetMinimumWidth(Composite parent, boolean changed) {
		if (changed)
			//calculateMinimumColumnWidths(parent, true);
			calculateColumnWidths(parent, minColumnWidths, false, true, makeColumnsEqualWidth);
		int minimumWidth = 0;
		widestColumnWidth = 0;
		if (makeColumnsEqualWidth) {
			for (int i = 0; i < numColumns; i++) {
				widestColumnWidth = Math.max(widestColumnWidth,
						minColumnWidths[i]);
			}
		}
		for (int i = 0; i < numColumns; i++) {
			if (i > 0)
				minimumWidth += horizontalSpacing;
			if (makeColumnsEqualWidth)
				minimumWidth += widestColumnWidth;
			else
				minimumWidth += minColumnWidths[i];
		}
		// add margins
		minimumWidth += leftMargin + rightMargin;
		return minimumWidth;
	}

	int internalGetMaximumWidth(Composite parent, boolean changed) {
		if (changed)
			//calculateMaximumColumnWidths(parent, true);
			calculateColumnWidths(parent, maxColumnWidths, true, true, makeColumnsEqualWidth);
		int maximumWidth = 0;
		for (int i = 0; i < numColumns; i++) {
			if (i > 0)
				maximumWidth += horizontalSpacing;
			maximumWidth += maxColumnWidths[i];
		}
		// add margins
		maximumWidth += leftMargin + rightMargin;
		return maximumWidth;
	}

	void resetColumnWidths() {
		if (minColumnWidths == null)
			minColumnWidths = new int[numColumns];
		if (maxColumnWidths == null)
			maxColumnWidths = new int[numColumns];
		for (int i = 0; i < numColumns; i++) {
			minColumnWidths[i] = 0;
		}
		for (int i = 0; i < numColumns; i++) {
			maxColumnWidths[i] = 0;
		}
	}

	void calculateColumnWidths(Composite parent, int [] columnWidths, boolean max, boolean changed, boolean makeColumnsEqualWidth2) {
		boolean secondPassNeeded=false;
		int widestColumnWidth = 0;
		for (TableWrapData[] row : grid) {
			for (int j = 0; j < numColumns; j++) {
				TableWrapData td = row[j];
				if (td.isItemData == false)
					continue;

				if (td.colspan>1) {
					// we will not do controls with multiple column span
					// here - increment and continue
					secondPassNeeded=true;
					j+=td.colspan-1;
					continue;
				}

				SizeCache childCache = cache.getCache(td.childIndex);
				// !!
				int width = max?childCache.computeMaximumWidth():childCache.computeMinimumWidth();
				if (td.maxWidth!=SWT.DEFAULT)
					width = Math.min(width, td.maxWidth);

				width += td.indent;
				columnWidths[j] = Math.max(columnWidths[j], width);
				widestColumnWidth = Math.max(widestColumnWidth, columnWidths[j]);
			}
		}
		if (makeColumnsEqualWidth) {
			for (int i = 0; i < numColumns; i++) {
				columnWidths[i] = widestColumnWidth;
			}
		}
		if (!secondPassNeeded) return;

		// Second pass for controls with multi-column horizontal span
		for (TableWrapData[] row : grid) {
			for (int j = 0; j < numColumns; j++) {
				TableWrapData td = row[j];
				if (td.isItemData == false || td.colspan==1)
					continue;

				SizeCache childCache = cache.getCache(td.childIndex);
				int width = max?childCache.computeMaximumWidth():childCache.computeMinimumWidth();
				if (td.maxWidth!=SWT.DEFAULT)
					width = Math.min(width, td.maxWidth);

				width += td.indent;
				// check if the current width is enough to
				// support the control; if not, add the delta to
				// the last column or to all the growing columns, if present
				int current = 0;
				for (int k = j; k < j + td.colspan; k++) {
					if (k > j)
						current += horizontalSpacing;
					current += columnWidths[k];
				}
				if (width <= current) {
					// we are ok - nothing to do here
				} else {
					int ndiv = 0;
					if (growingColumns != null) {
						for (int k = j; k < j + td.colspan; k++) {
							if (isGrowingColumn(k)) {
								ndiv++;
							}
						}
					}
					if (ndiv == 0) {
						// add the delta to the last column
						columnWidths[j + td.colspan - 1] += width
								- current;
					} else {
						// distribute the delta to the growing
						// columns
						int percolumn = (width - current) / ndiv;
						if ((width - current) % ndiv > 0)
							percolumn++;
						for (int k = j; k < j + td.colspan; k++) {
							if (isGrowingColumn(k))
								columnWidths[k] += percolumn;
						}
					}
				}
			}
		}
	}

	boolean isFillAligned(Control control) {
		Object layoutData = control.getLayoutData();

		if (layoutData instanceof TableWrapData) {
			TableWrapData tableWrapData = (TableWrapData) layoutData;

			if (tableWrapData.align == TableWrapData.FILL) {
				return true;
			}
		}
		if (control instanceof Composite
				&& ((Composite) control).getLayout() instanceof ILayoutExtension)
			return true;
		return (control.getStyle() & SWT.WRAP) != 0;
	}

	private void initializeIfNeeded(Composite parent, boolean changed) {
		if (changed)
			initialLayout = true;
		if (initialLayout) {
			initializeLayoutData(parent);
			initialLayout = false;
		}
	}

	void initializeLayoutData(Composite composite) {
		Control[] children = composite.getChildren();
		for (Control child : children) {
			if (child.getLayoutData() == null) {
				child.setLayoutData(new TableWrapData());
			}
		}
	}
}
