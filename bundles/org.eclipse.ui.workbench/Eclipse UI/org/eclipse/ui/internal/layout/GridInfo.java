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

import org.eclipse.swt.widgets.Control;

/**
 * Computes and the positions of controls in a CellLayout, based on their span.
 */
class GridInfo {
    private int cols = 0;

    private int rows = 0;

    private int[] gridInfo;

    int[] controlRow;

    int[] controlCol;

    private CellData[] cellData;

    Control[] controls;

    /**
     * Initialize the grid
     * 
     * @param newControls
     * @param cols
     */
    public void initGrid(Control[] newControls, CellLayout layout) {
        cols = layout.getColumns();
        controls = newControls;

        int area = 0;
        int totalWidth = 0;

        controlRow = new int[controls.length];
        controlCol = new int[controls.length];

        // Get the CellData objects for each control, and compute
        // the total number of cells spanned by controls in this layout.
        cellData = new CellData[controls.length];
        for (int idx = 0; idx < controls.length; idx++) {
            if (controls[idx] == null) {
                continue;
            }

            CellData next = CellLayoutUtil.getData(controls[idx]);
            cellData[idx] = next;
            area += next.horizontalSpan * next.verticalSpan;
            totalWidth += next.horizontalSpan;
        }

        // Compute the number of columns
        if (cols == 0) {
            cols = totalWidth;
        }

        // Compute the number of rows
        rows = area / cols;
        if (area % cols > 0) {
            // Ensure we count any partial rows
            rows++;
        }

        area = rows * cols;

        // Allocate the gridInfo array
        gridInfo = new int[area];
        for (int idx = 0; idx < area; idx++) {
            gridInfo[idx] = -1;
        }

        int infoIdx = 0;
        // Compute the positions of each control
        for (int idx = 0; idx < controls.length; idx++) {
            CellData data = cellData[idx];

            // Find an empty position to insert the control
            while (gridInfo[infoIdx] >= 0) {
                infoIdx++;
            }

            controlRow[idx] = infoIdx / cols;
            controlCol[idx] = infoIdx % cols;

            // Insert the new control here
            for (int rowIdx = 0; rowIdx < data.verticalSpan; rowIdx++) {
                for (int colIdx = 0; colIdx < data.horizontalSpan; colIdx++) {
                    gridInfo[infoIdx + rowIdx * cols + colIdx] = idx;
                }
            }

            infoIdx += data.horizontalSpan;
        }
    }

    public int getRows() {
        return rows;
    }

    public int getStartPos(int control, boolean row) {
        if (row) {
            return controlRow[control];
        } else {
            return controlCol[control];
        }
    }

    /**
     * Returns the number of rows or columns
     * 
     * @param isRow if true, returns the number of rows. If false, returns the number of columns
     * @return
     */
    public int getNumRows(boolean isRow) {
        if (isRow) {
            return rows;
        } else {
			return cols;
		}
    }

    public void getRow(int[] result, int rowId, boolean horizontal) {
        if (horizontal) {
            int prev = -1;
            for (int colIdx = 0; colIdx < cols; colIdx++) {
                int next = gridInfo[cols * rowId + colIdx];

                if (prev == next) {
                    result[colIdx] = -1;
                } else {
					result[colIdx] = next;
				}

                prev = next;
            }
        } else {
            int prev = -1;
            for (int rowIdx = 0; rowIdx < rows; rowIdx++) {
                int next = gridInfo[cols * rowIdx + rowId];

                if (prev == next) {
                    result[rowIdx] = -1;
                } else {
					result[rowIdx] = next;
				}

                prev = next;
            }
        }
    }

    public CellData getCellData(int controlId) {
        return cellData[controlId];
    }

    public int getCols() {
        return cols;
    }
}
