package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import org.eclipse.swt.SWT;

/**
 * This implementation of the layout algorithm
 * attempts to position controls in the composite
 * using a two-pass autolayout HTML table altorithm
 * recommeded by HTML 4.01 W3C specification.
 * The main differences with GridLayout is that
 * it has two passes and that width and height
 * are not calculated in the same pass.
 * <p>
 * The advantage of the algorithm over GridLayout
 * is that it is capable of flowing text controls
 * capable of line wrap. These controls do not
 * have natural 'preferred size'. Instead, 
 * they are capable of providing the required
 * height if the width is set. Consequently,
 * this algorithm first calculates the widths
 * that will be assigned to columns, and then
 * passes those widths to the controls to
 * calculate the height. When a composite
 * with this layout is a child of the scrolling
 * composite, they should interact in such
 * a way that reduction in the scrolling composite
 * width results in the reflow and increase of
 * the overall height.
 * <p>
 * If none of the columns contain expandable
 * and wrappable controls, the end-result
 * will be similar to the one provided
 * by GridLayout. The difference will
 * show up for layouts that contain
 * controls whose minimum and maximum
 * widths are not the same.
 */

public class HTMLTableLayout extends Layout {
	public int numColumns = 1;
	public int leftMargin = 5;
	public int rightMargin = 5;
	public int topMargin = 5;
	public int bottomMargin = 5;
	public int horizontalSpacing = 5;
	public int verticalSpacing = 5;
	public boolean makeColumnsEqualWidth = false;
	
	private boolean initialLayout=true;
	private Vector grid = null;
	private int [] minColumnWidths, maxColumnWidths;
	private int widestColumnWidth;
	private int [] growingColumns;
	
	public int getMinimumWidth(Composite parent, boolean changed) {
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
		   changed = true;
		   initialLayout = false;
		}
		if (grid==null || changed) {
		   changed = true;
		   grid = new Vector();
		   createGrid(parent);
		}
		if (minColumnWidths==null)
		   minColumnWidths = new int [numColumns];
		for (int i=0; i<numColumns; i++) {
			minColumnWidths[i] = 0;
		}
		return internalGetMinimumWidth(parent, changed);
	}
	
	public int getMaximumWidth(Composite parent, boolean changed) {
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
		   changed = true;
		   initialLayout = false;
		}
		if (grid==null || changed) {
		   changed = true;
		   grid = new Vector();
		   createGrid(parent);
		}
		if (maxColumnWidths==null)
		   maxColumnWidths = new int[numColumns];
		for (int i=0; i<numColumns; i++) {
			maxColumnWidths[i] = 0;
		}
		return internalGetMaximumWidth(parent, changed);
	}
	
	/**
	 * @see Layout#layout(Composite, boolean)
	 */
	protected void layout(Composite parent, boolean changed) {
		Rectangle clientArea = parent.getClientArea();
		Control [] children = parent.getChildren();
		if (children.length ==0) return;
		int parentWidth = clientArea.width;
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
		   changed = true;
		   initialLayout = false;
		}
		if (grid==null || changed) {
		   changed = true;
		   grid = new Vector();
		   createGrid(parent);
		}
		resetColumnWidths();
		int minWidth = internalGetMinimumWidth(parent, changed);
		int maxWidth = internalGetMaximumWidth(parent, changed);
		
		int tableWidth = parentWidth;
		
		int [] columnWidths;
		
		if (parentWidth < minWidth) {
		   tableWidth = minWidth;
		   if (makeColumnsEqualWidth) {
		      columnWidths = new int[numColumns];
		      for (int i=0; i<numColumns; i++) {
		      	columnWidths[i] = widestColumnWidth;
		      }
		   }
		   else
		      columnWidths = minColumnWidths;
		}
		else if (parentWidth > maxWidth) {
			if (growingColumns.length==0) {
		   		tableWidth = maxWidth;
		   		columnWidths = maxColumnWidths;
			}
			else {
				columnWidths = new int [numColumns];
				int colSpace = tableWidth - leftMargin - rightMargin;
				colSpace -= (numColumns-1)*horizontalSpacing;
				int extra = parentWidth - maxWidth;
				int colExtra = extra / growingColumns.length;
				for (int i=0; i<numColumns; i++) {
					columnWidths[i] = maxColumnWidths[i];
				
					if (isGrowingColumn(i)) {
						columnWidths[i] += colExtra;
					}
				}
			}
		}
		else {
			columnWidths = new int [numColumns];
			if (makeColumnsEqualWidth) {
				int colSpace = tableWidth - leftMargin - rightMargin;
				colSpace -= (numColumns-1)*horizontalSpacing;
				int col = colSpace/numColumns;
				for (int i=0; i<numColumns; i++) {
					columnWidths[i] = col;
				}
			}
			else {
			   int [] extraSpace = calculateExtraSpace(tableWidth, maxWidth, minWidth);
			   for (int i=0; i<numColumns; i++) {
				  int minCWidth = minColumnWidths[i];
		   		  columnWidths[i] = minCWidth + extraSpace[i];
			   }
			}
		}
		int x = 0;
		int y = topMargin;
		// assign widths 
		for (int i=0; i<grid.size(); i++) {
			TableData [] row = (TableData [])grid.elementAt(i);
			// assign widths, calculate heights
			int rowHeight = 0;
			x = leftMargin;
			for (int j=0; j<numColumns; j++) {
				TableData td = row[j];
				if (td.isItemData==false) {
					continue;
				}
				Control child = children[td.childIndex];
				int span = td.colspan;
				int cwidth = 0;
				for (int k=j; k<j+span; k++) {
					if (k>j) cwidth += horizontalSpacing;
					cwidth += columnWidths[k];
				}
				Point size = computeSize(child, cwidth, changed);
				td.compWidth = cwidth;
				if (td.heightHint!=SWT.DEFAULT) {
					size = new Point(size.x, td.heightHint);
				}
				td.compSize = size;
				rowHeight = Math.max(rowHeight, size.y);
			}
			for (int j=0; j<numColumns; j++) {
				TableData td = row[j];
				if (td.isItemData==false) {
					continue;
				}
				Control child = children[td.childIndex];
				if (j>0) x+= horizontalSpacing;
				placeControl(child, td, x, y, rowHeight);
				x += td.compWidth;
			}
			y += rowHeight + verticalSpacing;
		}
	}
	
boolean isGrowingColumn(int col) {
	if (growingColumns==null) return false;
	for (int i=0; i<growingColumns.length; i++) {
		if (col == growingColumns[i]) return true;
	}
	return false;
}	
	
int [] calculateExtraSpace(int tableWidth, int maxWidth, int minWidth) {
	int fixedPart = leftMargin + rightMargin + (numColumns-1)*horizontalSpacing;
	int D = maxWidth - minWidth;
	
	int W = tableWidth -fixedPart - minWidth;
	
	int extraSpace [] = new int [numColumns];

	int rem = 0;
	for (int i=0; i<numColumns; i++) { 
		int cmin = minColumnWidths[i];
		int cmax = maxColumnWidths[i];
		int d = cmax - cmin;
		int extra = D!=0 ? (d * W)/D : 0;
		if (i<numColumns-1) {
			extraSpace [i] = extra;
			rem += extra;
		}
		else {
			extraSpace[i] = W - rem;
		}
	}
	return extraSpace;
}
	
Point computeSize(Control child, int width, boolean changed) {
	int widthArg = width;
	if (!isWrap(child))
	   widthArg = SWT.DEFAULT;
	Point size = child.computeSize(widthArg, SWT.DEFAULT, changed);
	return size;
}

void placeControl(Control control, TableData td, int x, int y, int rowHeight) {
	int xloc = x + td.indent;
	int yloc = y;
	int width = td.compSize.x;
	int height = td.compSize.y;
	int colWidth = td.compWidth;
	
	// align horizontally
	if (td.align==TableData.CENTER) {
		xloc = x + colWidth/2 - width/2;
	}
	else if (td.align==TableData.RIGHT) {
		xloc = x + colWidth - width;
	}
	else if (td.align==TableData.FILL) {
		width = colWidth;
	}
	// align vertically
	if (td.valign == TableData.MIDDLE) {
		yloc = y + rowHeight/2 - height/2;
	}
	else if (td.valign==TableData.BOTTOM) {
		yloc = y + rowHeight - height;
	}
	else if (td.valign==TableData.FILL) {
		height = rowHeight;	
	}
	control.setBounds(xloc, yloc, width, height);
}

void createGrid(Composite composite) {
	int row, column, rowFill, columnFill;
	Vector rows;
	Control[] children;
	TableData spacerSpec;
	Vector growingCols = new Vector();

	// 
	children = composite.getChildren();
	if (children.length==0) return;

	// 
	grid.addElement(createEmptyRow());
	row = 0;
	column = 0;

	// Loop through the children and place their associated layout specs in the
	// grid.  Placement occurs left to right, top to bottom (i.e., by row).
	for (int i = 0; i < children.length; i++) {
		// Find the first available spot in the grid.
		Control child = children[i];
		TableData spec = (TableData) child.getLayoutData();
		while (((TableData[]) grid.elementAt(row))[column] != null) {
			column = column + 1;
			if (column >= numColumns) {
				row = row + 1;
				column = 0;
				if (row >= grid.size()) {
					grid.addElement(createEmptyRow());
				}
			}
		}
		// See if the place will support the widget's horizontal span.  If not, go to the
		// next row.
		if (column + spec.colspan - 1 >= numColumns) {
			grid.addElement(createEmptyRow());
			row = row + 1;
			column = 0;
		}

		// The vertical span for the item will be at least 1.  If it is > 1,
		// add other rows to the grid.
		for (int j = 2; j <= spec.rowspan; j++) {
			if (row + j > grid.size()) {
				grid.addElement(createEmptyRow());
			}
		}

		// Store the layout spec.  Also cache the childIndex.  NOTE: That we assume the children of a
		// composite are maintained in the order in which they are created and added to the composite.
		((TableData[]) grid.elementAt(row))[column] = spec;
		spec.childIndex = i;
		
		if (spec.grabHorizontal) {
			updateGrowingColumns(growingCols, spec, column);
		}

		// Put spacers in the grid to account for the item's vertical and horizontal
		// span.
		rowFill = spec.rowspan - 1;
		columnFill = spec.colspan - 1;
		for (int r = 1; r <= rowFill; r++) {
			for (int c = 0; c < spec.colspan; c++) {
				spacerSpec = new TableData();
				spacerSpec.isItemData = false;
				((TableData[]) grid.elementAt(row + r))[column + c] = spacerSpec;
			}
		}
		for (int c = 1; c <= columnFill; c++) {
			for (int r = 0; r < spec.rowspan; r++) {
				spacerSpec = new TableData();
				spacerSpec.isItemData = false;
				((TableData[]) grid.elementAt(row + r))[column + c] = spacerSpec;
			}
		}
		column = column + spec.colspan - 1;
	}

	// Fill out empty grid cells with spacers.
	for (int k = column + 1; k < numColumns; k++) {
		spacerSpec = new TableData();
		spacerSpec.isItemData = false;
		((TableData[]) grid.elementAt(row))[k] = spacerSpec;
	}
	for (int k = row + 1; k < grid.size(); k++) {
		spacerSpec = new TableData();
		spacerSpec.isItemData = false;
		((TableData[]) grid.elementAt(k))[column] = spacerSpec;
	}
	growingColumns = new int [growingCols.size()];
	for (int i=0; i<growingCols.size(); i++) {
		growingColumns[i] = ((Integer)growingCols.get(i)).intValue();
	}
}

private void updateGrowingColumns(Vector growingColumns, TableData spec, int column) {
	int affectedColumn = column + spec.colspan -1;
	for (int i=0; i<growingColumns.size(); i++) {
		Integer col = (Integer)growingColumns.get(i);
		if (col.intValue()==affectedColumn)
		   return;
	}
	growingColumns.add(new Integer(affectedColumn));
}

private TableData [] createEmptyRow() {
	TableData [] row = new TableData[numColumns];
	for (int i=0; i<numColumns; i++) 
	   row[i] = null;
	return row;
}

	/**
	 * @see Layout#computeSize(Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite parent, int wHint, int hHint, boolean changed) {
		Control [] children = parent.getChildren();
		if (children.length == 0) {
			return new Point(0, 0);
		}
		int parentWidth = wHint;
		changed = true;
		initializeIfNeeded(parent, changed);
		if (initialLayout) {
		   changed = true;
		   initialLayout = false;
		}
		if (grid==null || changed) {
		   changed = true;
		   grid = new Vector();
		   createGrid(parent);
		}
		resetColumnWidths();
		int minWidth = internalGetMinimumWidth(parent, changed);
		int maxWidth = internalGetMaximumWidth(parent, changed);
		
		int tableWidth = parentWidth;
		
		int [] columnWidths;
		
		if (parentWidth < minWidth) {
		   tableWidth = minWidth;
		   if (makeColumnsEqualWidth) {
		      columnWidths = new int[numColumns];
		      for (int i=0; i<numColumns; i++) {
		      	columnWidths[i] = widestColumnWidth;
		      }
		   }
		   else
		      columnWidths = minColumnWidths;
		}
		else if (parentWidth > maxWidth) {
		   tableWidth = maxWidth;
		   columnWidths = maxColumnWidths;
		}
		else {
			columnWidths = new int [numColumns];
			if (makeColumnsEqualWidth) {
				int colSpace = tableWidth - leftMargin - rightMargin;
				colSpace -= (numColumns-1)*horizontalSpacing;
				int col = colSpace/numColumns;
				for (int i=0; i<numColumns; i++) {
					columnWidths[i] = col;
				}
			}
			else {
			   int [] extraSpace = calculateExtraSpace(tableWidth, maxWidth, minWidth);
			   for (int i=0; i<numColumns; i++) {
				  int minCWidth = minColumnWidths[i];
		   		  columnWidths[i] = minCWidth + extraSpace[i];
			   }
			}
		}
		int totalWidth=0;
		int totalHeight=0;
		int x = leftMargin;
		int y = topMargin;
		// compute widths 
		for (int i=0; i<grid.size(); i++) {
			TableData [] row = (TableData [])grid.elementAt(i);
			// assign widths, calculate heights
			int rowHeight = 0;
			for (int j=0; j<numColumns; j++) {
				TableData td = row[j];
				if (td.isItemData==false) {
					continue;
				}
				Control child = children[td.childIndex];
				int span = td.colspan;
				int cwidth = 0;
				for (int k=j; k<j+span; k++) {
					if (k>j) cwidth += horizontalSpacing;
					cwidth += columnWidths[k];
				}
				int cy = td.heightHint;
				if (cy == SWT.DEFAULT) {
				   Point size = computeSize(child, cwidth, changed);
				   cy = size.y;
				}
				rowHeight = Math.max(rowHeight, cy);
			}
			y += rowHeight + verticalSpacing;
		}
		totalHeight = y + bottomMargin;
		return new Point(tableWidth, totalHeight);
	}

	int internalGetMinimumWidth(Composite parent, boolean changed) {
		if (changed)
		   calculateMinimumColumnWidths(parent, true);
		int minimumWidth = 0;
		
		widestColumnWidth = 0;
		
		if (makeColumnsEqualWidth) {
			for (int i=0; i<numColumns; i++) {
				widestColumnWidth = Math.max(widestColumnWidth, minColumnWidths[i]);
			}
		}
		for (int i=0; i<numColumns; i++) {
			if (i>0) minimumWidth += horizontalSpacing;
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
		   calculateMaximumColumnWidths(parent, true);
		int maximumWidth = 0;
		for (int i=0; i<numColumns; i++) {
			if (i>0) maximumWidth += horizontalSpacing;
			maximumWidth += maxColumnWidths[i];
		}
		// add margins
		maximumWidth += leftMargin + rightMargin;
		return maximumWidth;
	}
	
	void resetColumnWidths() {
		if (minColumnWidths == null)
		   minColumnWidths = new int [numColumns];
		if (maxColumnWidths == null)
		   maxColumnWidths = new int [numColumns];
		for (int i=0; i<numColumns; i++) {
			minColumnWidths [i] = 0;
		}
		for (int i=0; i<numColumns; i++) {
			maxColumnWidths [i] = 0;
		}
	}

	void calculateMinimumColumnWidths(Composite parent, boolean changed) {
		Control [] children = parent.getChildren();
		
		for (int i=0; i<grid.size(); i++) {
			TableData [] row = (TableData [])grid.elementAt(i);
			for (int j=0; j<numColumns; j++) {
				TableData td = row[j];
				if (td.isItemData==false) continue;
				Control child = children[td.childIndex];
				int minWidth = -1;
				if (child instanceof Composite) {
					Composite cc = (Composite)child;
					Layout l = cc.getLayout();
					if (l instanceof HTMLTableLayout) {
						minWidth = ((HTMLTableLayout)l).getMinimumWidth(cc, changed);
					}
				}
				if (minWidth == -1) {
					if (isWrap(child)) {
						// Should be the width of the
						// longest word, we'll pick
						// some small number instead
						minWidth = 30;
					}
					else {
					   Point size = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
					   minWidth = size.x;
					}
				}
				minWidth += td.indent;
				minColumnWidths[j] = Math.max(minColumnWidths[j], minWidth);
			}
		}
	}
	
	boolean isWrap(Control control) {
		if (control instanceof Composite &&
		   ((Composite)control).getLayout() instanceof HTMLTableLayout)
		   return true;
		return (control.getStyle() & SWT.WRAP)!=0;
	}
	
	void calculateMaximumColumnWidths(Composite parent, boolean changed) {
		Control [] children = parent.getChildren();
		
		for (int i=0; i<numColumns; i++) {
			maxColumnWidths [i] = 0;
		}
		for (int i=0; i<grid.size(); i++) {
			TableData [] row = (TableData [])grid.elementAt(i);
			for (int j=0; j<numColumns; j++) {
				TableData td = row[j];
				if (td.isItemData==false) continue;
				Control child = children[td.childIndex];
				int maxWidth = -1;
				if (child instanceof Composite) {
					Composite cc = (Composite)child;
					Layout l = cc.getLayout();
					if (l instanceof HTMLTableLayout) {
						maxWidth = ((HTMLTableLayout)l).getMaximumWidth(cc, changed);
					}
				}
				if (maxWidth == -1) {
					Point size = child.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
					maxWidth = size.x;
				}
				maxWidth += td.indent;
				if (td.colspan==1)
				   maxColumnWidths[j] = Math.max(maxColumnWidths[j], maxWidth);
				else {
					// grow the last column
					int last = j + td.colspan -1;
					int rem = 0;
					for (int k=j; k<j+td.colspan-1; k++) {
						rem += maxColumnWidths[k];
					}
					int reduced = maxWidth - rem;
					maxColumnWidths[last] = Math.max(maxColumnWidths[last], reduced);
				}
			}
		}
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
	for (int i = 0; i < children.length; i++) {
		Control child = children[i];
		if (child.getLayoutData() == null) {
			child.setLayoutData(new TableData());
		}
	}
}
}