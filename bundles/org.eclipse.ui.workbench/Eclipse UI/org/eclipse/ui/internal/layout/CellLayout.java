/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.layout;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * <p>Instance of this class lay out the control children of a <code>Composite</code>
 * in a grid, using a simple set of rules and a simple API. This class is 
 * intended to be more predictable than <code>GridLayout</code> and easier to use than
 * <code>FormLayout</code>, while retaining most of the power of both.</p>
 * 
 * <p>The power of a <code>CellLayout</code> lies in the ability to control
 * the size and resizing properties of each row and column. Unlike other layout
 * classes, complex layouts can be created without attaching any layout data to
 * individual controls in the layout. </p>
 * 
 * <p>The various subclasses of <code>IColumnInfo</code> 
 * can be used to create columns with fixed width, columns whose width is computed
 * from child controls, or width that grows in proportion to the size of other
 * columns. Layouts can be given a default <code>IColumnInfo</code> that will
 * be used to set the size of any column whose properties have not been explicitly
 * set. This is useful for creating layouts where most or all columns have the
 * same properties. Similarly, the subclasses of <code>IRowInfo</code> can be used to
 * control the height of individual rows.</p>
 * 
 * <p>For a finer grain of control, <code>CellData</code> objects can be attached
 * to individual controls in the layout. These objects serve a similar function as
 * <code>GridData</code> objects serve for <code>GridLayout</code>. They allow
 * controls to span multiple rows or columns, set the justification of the control
 * within its cell, and allow the user to override the preferred size of the control.
 * </p> 
 * 
 * <p>In many cases, it is not necessary to attach any layout data to controls in
 * the layout, since the controls can be arranged based on the properties of rows
 * and columns. However, layout data may be attached to individual controls to
 * allow them to span multiple columns or to control their justification within
 * their cell. 
 * </p>
 * 
 * <p>All the <code>set</code> methods in this class return <code>this</code>, allowing
 * a layout to be created and initialized in a single line of code. For example: </p>
 * 
 * <code>
 * Composite myControl = new Composite(parent, SWT.NONE);
 * myControl.setLayout(new CellLayout(2).setMargins(10,10).setSpacing(5,5));
 * </code>
 * 
 * @since 3.0
 */
public class CellLayout extends Layout {

    /**
     * Object used to compute the height of rows whose properties have not been
     * explicitly set.
     */
    private Row defaultRowSettings = new Row(false);

    /**
     * Object used to compute the width of columns whose properties have not been
     * explicitly set.
     */
    private Row defaultColSettings = new Row(true);

    /**
     * horizontalSpacing specifies the number of pixels between the right
     * edge of one cell and the left edge of its neighbouring cell to
     * the right.
     *
     * The default value is 5.
     */
    int horizontalSpacing = 5;

    /**
     * verticalSpacing specifies the number of pixels between the bottom
     * edge of one cell and the top edge of its neighbouring cell underneath.
     *
     * The default value is 5.
     */
    int verticalSpacing = 5;

    /**
     * marginWidth specifies the number of pixels of horizontal margin
     * that will be placed along the left and right edges of the layout.
     *
     * The default value is 0.
     */
    public int marginWidth = 5;

    /**
     * marginHeight specifies the number of pixels of vertical margin
     * that will be placed along the top and bottom edges of the layout.
     *
     * The default value is 0.
     */
    public int marginHeight = 5;

    /**
     * Number of columns in this layout, or 0 indicating that the whole layout
     * should be on a single row.
     */
    private int numCols;

    /**
     * List of IColumnInfo. The nth object is used to compute the width of the
     * nth column, or null indicating that the default column should be used.
     */
    private List cols;

    /**
     * List of RowInfo. The nth object is used to compute the height of the
     * nth row, or null indicating that the default row should be used.
     */
    private List rows = new ArrayList(16);

    // Cached information 
    private GridInfo gridInfo = new GridInfo();

    private int[] cachedRowMin = null;

    private int[] cachedColMin = null;

    public static int cacheMisses;

    public static int cacheHits;

    private LayoutCache cache = new LayoutCache();

    // End of cached control sizes

    /**
     * Creates the layout
     * 
     * @param numCols the number of columns in this layout, 
     * or 0 indicating that the whole layout should be on one row.
     */
    public CellLayout(int numCols) {
        super();
        this.numCols = numCols;
        cols = new ArrayList(numCols == 0 ? 3 : numCols);
    }

    /**
     * Sets the amount empty space between cells
     * 
     * @param newSpacing a point (x,y) corresponding to the number of pixels of
     * empty space between adjacent columns and rows respectively
     */
    public CellLayout setSpacing(int horizontalSpacing, int verticalSpacing) {
        this.horizontalSpacing = horizontalSpacing;
        this.verticalSpacing = verticalSpacing;

        return this;
    }

    /**
     * Sets the amount empty space between cells
     * 
     * @param newSpacing a point (x,y) corresponding to the number of pixels of
     * empty space between adjacent columns and rows respectively
     */
    public CellLayout setSpacing(Point newSpacing) {
        horizontalSpacing = newSpacing.x;
        verticalSpacing = newSpacing.y;
        return this;
    }

    /**
     * Returns the amount of empty space between adjacent cells
     * 
     * @return a point (x,y) corresponding to the number of pixels of empty
     * space between adjacent columns and rows respectively
     */
    public Point getSpacing() {
        return new Point(horizontalSpacing, verticalSpacing);
    }

    /**
     * Sets the size of the margin around the outside of the layout. 
     * 
     * @param marginWidth the size of the margin around the top and 
     * bottom of the layout
     * @param marginHeight the size of the margin on the left and right
     * of the layout.
     */
    public CellLayout setMargins(int marginWidth, int marginHeight) {
        this.marginWidth = marginWidth;
        this.marginHeight = marginHeight;
        return this;
    }

    /**
     * Sets the size of the margin around the outside of the layout.
     * 
     * @param newMargins point indicating the size of the horizontal and vertical
     * margins, in pixels.
     */
    public CellLayout setMargins(Point newMargins) {
        marginWidth = newMargins.x;
        marginHeight = newMargins.y;
        return this;
    }

    /**
     * Returns the size of the margins around the outside of the layout.
     * 
     * @return the size of the outer margins, in pixels.
     */
    public Point getMargins() {
        return new Point(marginWidth, marginHeight);
    }

    /**
     * Sets the default column settings. All columns will use these settings unless
     * they have been explicitly assigned custom settings by setColumn.
     * 
     * @param info the properties of all default columns
     * @see setColumn
     */
    public CellLayout setDefaultColumn(Row info) {
        defaultColSettings = info;
        return this;
    }

    /**
     * Sets the column info for the given column number (the leftmost column is column 0).
     * This replaces any existing info for the column. Note that more than one column
     * are allowed to share the same IColumnInfo instance if they have identical properties.
     * 
     * @param colNum the column number to modify
     * @param info the properties of the column, or null if this column should use the
     * default properties 
     */
    public CellLayout setColumn(int colNum, Row info) {
        while (cols.size() <= colNum) {
            cols.add(null);
        }

        cols.set(colNum, info);

        return this;
    }

    /**
     * Sets the default row settings for this layout. Unless this is overridden
     * for an individual row, all rows will use the default settings.
     * 
     * @param info the row info object that should be used to set the size
     * of rows, by default.
     */
    public CellLayout setDefaultRow(Row info) {
        defaultRowSettings = info;

        return this;
    }

    /**
     * Sets the row info for the given rows. The topmost row is row 0. Multiple
     * rows are allowed to share the same RowInfo instance.
     * 
     * @param rowNum the row number to set
     * @param info the row info that will control the sizing of the given row,
     * or null if the row should use the default settings for this layout.
     */
    public CellLayout setRow(int rowNum, Row info) {
        while (rows.size() <= rowNum) {
            rows.add(null);
        }

        rows.set(rowNum, info);

        return this;
    }

    /**
     * Returns the row info that controls the size of the given row. Will return
     * the default row settings for this layout if no custom row info has been
     * assigned to the row.
     * 
     * @param rowNum
     * @return
     */
    private Row getRow(int rowNum, boolean isHorizontal) {
        if (isHorizontal) {
            if (rowNum >= rows.size()) {
                return defaultRowSettings;
            }

            Row result = (Row) rows.get(rowNum);

            if (result == null) {
                result = defaultRowSettings;
            }

            return result;
        } else {
            if (rowNum >= cols.size()) {
                return defaultColSettings;
            }

            Row result = (Row) cols.get(rowNum);

            if (result == null) {
                result = defaultColSettings;
            }

            return result;
        }
    }

    /**
     * Initializes the gridInfo object.
     * 
     * @param children controls that are being layed out
     */
    private void initGrid(Control[] children) {
        cache.setControls(children);
        gridInfo.initGrid(children, this);
        cachedRowMin = null;
        cachedColMin = null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
     */
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        Control[] children = composite.getChildren();
        initGrid(children);

        if (flushCache) {
            cache.flush();
        }

        // Determine the amount of whitespace (area that cannot be used by controls)
        Point emptySpace = totalEmptySpace();

        int[] heightConstraints = computeConstraints(true);

        int width;
        if (wHint == SWT.DEFAULT) {
            width = preferredSize(heightConstraints, false);
        } else {
            width = wHint - emptySpace.x;
        }

        int height = hHint;
        if (hHint == SWT.DEFAULT) {
            height = preferredSize(
                    computeSizes(heightConstraints, width, false), true);
        } else {
            height = hHint - emptySpace.y;
        }

        Point preferredSize = new Point(width + emptySpace.x, height
                + emptySpace.y);

        // At this point we know the layout's preferred size. Now adjust it
        // if we're smaller than the minimum possible size for the composite.

        // If exactly one dimension of our preferred size is smaller than
        // the minimum size of our composite, then set that dimension to
        // the minimum size and recompute the other dimension (for example,
        // increasing the width to match a shell's minimum width may reduce
        // the height allocated for a wrapping text widget). There is no
        // point in doing this if both dimensions are smaller than the
        // composite's minimum size, since we're already smaller than
        // we need to be.
        Point minimumSize = CellLayoutUtil.computeMinimumSize(composite);

        boolean wider = (preferredSize.x >= minimumSize.x);
        boolean taller = (preferredSize.y >= minimumSize.y);

        if (wider) {
            if (taller) {
                // If we're larger in both dimensions, don't adjust the minimum
                // size.
                return preferredSize;
            } else {
                // If our preferred height is smaller than the minimum height,
                // recompute the preferred width using the minimum height
                return computeSize(composite, wHint, minimumSize.y, false);
            }
        } else {
            if (taller) {
                // If our preferred width is smaller than the minimum width,
                // recompute the preferred height using the minimum width
                return computeSize(composite, minimumSize.x, hHint, false);
            } else {
                // If both dimensions are smaller than the minimum size,
                // use the minimum size as our preferred size.
                return minimumSize;
            }
        }
    }

    int[] computeSizes(int[] constraints, int availableSpace,
            boolean computingRows) {
        int[] result = computeMinSizes(constraints, computingRows);

        int totalFixed = sumOfSizes(result);
        int denominator = getResizeDenominator(computingRows);
        int numRows = gridInfo.getNumRows(computingRows);

        if (totalFixed < availableSpace) {
            int remaining = availableSpace - totalFixed;

            for (int idx = 0; idx < numRows && denominator > 0; idx++) {
                Row row = getRow(idx, computingRows);

                if (row.grows) {
                    int greed = row.size;
                    int amount = remaining * greed / denominator;

                    result[idx] += amount;
                    remaining -= amount;
                    denominator -= greed;
                }
            }
        }

        return result;
    }

    /**
     * Computes one dimension of the preferred size of the layout.
     * 
     * @param hint contains the result if already known, or SWT.DEFAULT if it needs to be computed
     * @param constraints contains constraints along the other dimension, or SWT.DEFAULT if none. For
     * example, if we are computing the preferred row sizes, this would be an array of known column sizes.
     * @param computingRows if true, this method returns the height (pixels). Otherwise, it returns the
     * width (pixels).
     */
    int preferredSize(int[] constraints, boolean computingRows) {
        int[] fixedSizes = computeMinSizes(constraints, computingRows);

        return sumOfSizes(fixedSizes)
                + getDynamicSize(constraints, fixedSizes, computingRows);
    }

    /**
     * Computes the sum of all integers in the given array. If any of the entries are SWT.DEFAULT,
     * the result is SWT.DEFAULT. 
     */
    static int sumOfSizes(int[] input) {
        return sumOfSizes(input, 0, input.length);
    }

    static int sumOfSizes(int[] input, int start, int length) {
        int sum = 0;
        for (int idx = start; idx < start + length; idx++) {
            int next = input[idx];

            if (next == SWT.DEFAULT) {
                return SWT.DEFAULT;
            }

            sum += next;
        }

        return sum;
    }

    /**
     * Returns the preferred dynamic width of the layout 
     * 
     * @param constraints
     * @param fixedSizes
     * @param computingRows
     * @return
     */
    int getDynamicSize(int[] constraints, int[] fixedSizes,
            boolean computingRows) {
        int result = 0;
        int numerator = getResizeDenominator(computingRows);

        // If no resizable columns, return
        if (numerator == 0) {
            return 0;
        }

        int rowSpacing = computingRows ? verticalSpacing : horizontalSpacing;
        int colSpacing = computingRows ? horizontalSpacing : verticalSpacing;

        int numControls = gridInfo.controls.length;
        for (int idx = 0; idx < numControls; idx++) {
            int controlRowStart = gridInfo.getStartPos(idx, computingRows);
            int controlRowSpan = getSpan(idx, computingRows);
            int controlColStart = gridInfo.getStartPos(idx, !computingRows);
            int controlColSpan = getSpan(idx, !computingRows);

            int denominator = getGrowthRatio(controlRowStart, controlRowSpan,
                    computingRows);

            if (denominator > 0) {

                int widthHint = sumOfSizes(constraints, controlColStart,
                        controlColSpan);
                if (widthHint != SWT.DEFAULT) {
                    widthHint += colSpacing * (controlColSpan - 1);
                }

                // Compute the total control size
                int controlSize = computeControlSize(idx, widthHint,
                        computingRows);

                // Subtract the amount that overlaps fixed-size columns
                controlSize -= sumOfSizes(fixedSizes, controlRowStart,
                        controlRowSpan);

                // Subtract the amount that overlaps spacing between cells
                controlSize -= (rowSpacing * (controlRowSpan - 1));

                result = Math
                        .max(result, controlSize * numerator / denominator);
            }
        }

        return result;
    }

    /**
     * Computes one dimension of a control's size
     * 
     * @param control the index of the control being computed
     * @param constraint the other dimension of the control's size, or SWT.DEFAULT if unknown
     * @param computingHeight if true, this method returns a height. Else it returns a width
     * @return the preferred height or width of the control, in pixels
     */
    int computeControlSize(int control, int constraint, boolean computingHeight) {
        CellData data = gridInfo.getCellData(control);

        // If we're looking for the preferred size of the control (without hints)
        if (constraint == SWT.DEFAULT) {
            Point result = data.computeSize(cache.getCache(control),
                    SWT.DEFAULT, SWT.DEFAULT);

            // Return result
            if (computingHeight) {
                return result.y;
            }
            return result.x;
        }

        // Compute a height
        if (computingHeight) {
            return data.computeSize(cache.getCache(control), constraint,
                    SWT.DEFAULT).y;
        }

        return data.computeSize(cache.getCache(control), SWT.DEFAULT,
                constraint).x;
    }

    /**
     * Returns the relative amount that a control starting on the given row and spanning
     * the given length will contribute 
     * 
     * @param start
     * @param length
     * @param computingRows
     * @return
     */
    int getGrowthRatio(int start, int length, boolean computingRows) {
        boolean willGrow = false;
        int sum = 0;

        int end = start + length;
        for (int idx = start; idx < end; idx++) {
            Row row = getRow(idx, computingRows);

            if (row.largerThanChildren && row.grows) {
                willGrow = true;
            }

            sum += row.size;
        }

        if (!willGrow) {
            return 0;
        }

        return sum;
    }

    int[] computeMinSizes(int[] constraints, boolean computingRows) {
        // We cache the result of this function since it might be called more than once
        // for a single size computation
        int[] result = computingRows ? cachedRowMin : cachedColMin;

        if (result == null) {
            int columnSpacing;
            int rowSpacing;

            if (computingRows) {
                columnSpacing = horizontalSpacing;
                rowSpacing = verticalSpacing;
            } else {
                columnSpacing = verticalSpacing;
                rowSpacing = horizontalSpacing;
            }

            int rowCount = gridInfo.getNumRows(computingRows);
            result = new int[rowCount];
            int colCount = gridInfo.getNumRows(!computingRows);
            int[] rowControls = new int[colCount];

            int lastGrowingRow = -1;

            for (int idx = 0; idx < rowCount; idx++) {
                Row row = getRow(idx, computingRows);

                if (row.grows) {
                    // There is no minimum size for growing rows
                    lastGrowingRow = idx;
                    result[idx] = 0;
                } else {
                    result[idx] = row.size;

                    if (row.largerThanChildren) {
                        // Determine which controls are in this row
                        gridInfo.getRow(rowControls, idx, computingRows);

                        for (int colIdx = 0; colIdx < rowControls.length; colIdx++) {
                            int control = rowControls[colIdx];

                            // The getRow method will insert -1 into empty cells... skip these.
                            if (control != -1) {
                                int controlStart = gridInfo.getStartPos(
                                        control, computingRows);
                                int controlSpan = getSpan(control,
                                        computingRows);

                                // If the control ends on this row and does not span any growing rows
                                if (controlStart + controlSpan - 1 == idx
                                        && controlStart > lastGrowingRow) {
                                    int controlColStart = gridInfo.getStartPos(
                                            control, !computingRows);
                                    int controlColSpan = getSpan(control,
                                            !computingRows);
                                    int controlRowSpan = getSpan(control,
                                            computingRows);

                                    // Compute the width constraint for this control
                                    int spannedWidth = sumOfSizes(constraints,
                                            controlColStart, controlColSpan);
                                    if (spannedWidth != SWT.DEFAULT) {
                                        spannedWidth += (columnSpacing * (controlSpan - 1));
                                    }

                                    int controlHeight = computeControlSize(
                                            control, spannedWidth,
                                            computingRows);

                                    // Determine how much of the control spans already allocated columns
                                    int allocatedHeight = sumOfSizes(result,
                                            controlColStart, controlRowSpan - 1)
                                            + (rowSpacing * (controlRowSpan - 1));

                                    result[idx] = Math.max(result[idx],
                                            controlHeight - allocatedHeight);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Cache this result
        if (computingRows) {
            cachedRowMin = result;
        } else {
            cachedColMin = result;
        }

        return result;
    }

    /**
     * Returns the height constraints that should be used when computing column widths. Requires initGrid
     * to have been called first.
     * 
     * @param result Will contain the height constraint for row i in the ith position of the array, 
     * or SWT.DEFAULT if there is no constraint on that row.
     */
    private int[] computeConstraints(boolean horizontal) {
        // Initialize the height constraints for each row (basically, these will always be SWT.DEFAULT,
        // except for rows of type FixedRow, which have a constant height).
        int numRows = gridInfo.getNumRows(horizontal);
        int[] result = new int[numRows];

        for (int idx = 0; idx < numRows; idx++) {
            Row row = getRow(idx, horizontal);

            if (!(row.grows || row.largerThanChildren)) {
                result[idx] = row.size;
            } else {
                result[idx] = SWT.DEFAULT;
            }
        }

        return result;
    }

    /**
     * Computes the total greediness of all rows
     * 
     * @return the total greediness of all rows
     */
    private int getResizeDenominator(boolean horizontal) {
        int result = 0;
        int numRows = gridInfo.getNumRows(horizontal);

        for (int idx = 0; idx < numRows; idx++) {
            Row row = getRow(idx, horizontal);

            if (row.grows) {
                result += row.size;
            }
        }

        return result;
    }

    //	/**
    //	 * Computes the total fixed height of all rows
    //	 * 
    //	 * @param widthConstraints array where the nth entry indicates the known width of the
    //	 * nth column, or SWT.DEFAULT if the width is still unknown
    //	 * 
    //	 * @return the total fixed height for all rows
    //	 */
    //	private int getMinimumSize(int[] constraints, boolean horizontal) {
    //		Control[] controls = new Control[gridInfo.getRows()];
    //		int result = 0;
    //		int numRows = gridInfo.getRows();
    //		
    //		for (int idx = 0; idx < numRows; idx++) {
    //			result += getRow(idx).getFixedHeight(gridInfo, widthConstraints, idx);
    //		}		
    //		
    //		return result;
    //	}

    protected int getSpan(int controlId, boolean isRow) {
        CellData data = gridInfo.getCellData(controlId);

        if (isRow) {
            return data.verticalSpan;
        }
        return data.horizontalSpan;
    }

    /**
     * Returns the total space that will be required for margins and spacing between and
     * around cells. initGrid(...) must have been called first.
     * 
     * @return
     */
    private Point totalEmptySpace() {
        int numRows = gridInfo.getRows();

        return new Point((2 * marginWidth)
                + ((gridInfo.getCols() - 1) * horizontalSpacing),
                (2 * marginHeight) + ((numRows - 1) * verticalSpacing));
    }

    /**
     * Returns the absolute positions of each row, given the start position, row sizes, 
     * and row spacing 
     * 
     * @param startPos position of the initial row
     * @param sizes array of row sizes (pixels)
     * @param spacing space between each row (pixels)
     * @return array of row positions. The result size is sizes.length + 1. The last entry is
     * the position of the end of the layout.
     */
    private static int[] computeRowPositions(int startPos, int[] sizes,
            int spacing) {
        int[] result = new int[sizes.length + 1];

        result[0] = startPos;
        for (int idx = 0; idx < sizes.length; idx++) {
            result[idx + 1] = result[idx] + sizes[idx] + spacing;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
     */
    protected void layout(Composite composite, boolean flushCache) {
        Control[] children = composite.getChildren();
        
        // If there are no children then this is a NO-OP
        if (children.length == 0)
        	return;
        	
        initGrid(children);

        if (flushCache) {
            cache.flush();
        }

        Point emptySpace = totalEmptySpace();

        // Compute the area actually available for controls (once the margins and spacing is removed)
        int availableWidth = composite.getClientArea().width - emptySpace.x;
        int availableHeight = composite.getClientArea().height - emptySpace.y;

        int[] heights = computeConstraints(true);
        int[] widths = new int[gridInfo.getCols()];

        // Compute the actual column widths
        widths = computeSizes(heights, availableWidth, false);

        // Compute the actual row heights (based on the actual column widths)
        heights = computeSizes(widths, availableHeight, true);

        Rectangle currentCell = new Rectangle(0, 0, 0, 0);

        int[] starty = computeRowPositions(composite.getClientArea().y
                + marginHeight, heights, verticalSpacing);
        int[] startx = computeRowPositions(composite.getClientArea().x
                + marginWidth, widths, horizontalSpacing);

        int numChildren = gridInfo.controls.length;
        for (int controlId = 0; controlId < numChildren; controlId++) {
            CellData data = gridInfo.getCellData(controlId);

            int row = gridInfo.controlRow[controlId];
            int col = gridInfo.controlCol[controlId];

            currentCell.x = startx[col];
            currentCell.width = startx[col + data.horizontalSpan]
                    - currentCell.x - horizontalSpacing;

            currentCell.y = starty[row];
            currentCell.height = starty[row + data.verticalSpan]
                    - currentCell.y - verticalSpacing;

            data.positionControl(cache.getCache(controlId), currentCell);
        }
    }

    /**
     * @return
     */
    public int getColumns() {
        return numCols;
    }

    public boolean canGrow(Composite composite, boolean horizontally) {
        initGrid(composite.getChildren());

        int numRows = gridInfo.getNumRows(horizontally);

        for (int idx = 0; idx < numRows; idx++) {
            Row row = getRow(idx, horizontally);

            if (row.grows) {
                return true;
            }
        }

        return false;

    }
}
