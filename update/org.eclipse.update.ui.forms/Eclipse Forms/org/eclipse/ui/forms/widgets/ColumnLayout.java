/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ColumnLayout extends Layout implements ILayoutExtension {
	public int minNumColumns = 1;
	public int maxNumColumns = 3;
	public int horizontalSpacing = 5;
	public int verticalSpacing = 5;
	public int topMargin = 5;
	public int leftMargin = 5;
	public int bottomMargin = 5;
	public int rightMargin = 5;
	/**
	 *  
	 */
	public ColumnLayout() {
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite,
	 *      int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		if (wHint == 0)
			return computeSize(composite, wHint, hHint, minNumColumns);
		else if (wHint == SWT.DEFAULT)
			return computeSize(composite, wHint, hHint, maxNumColumns);
		else
			return computeSize(composite, wHint, hHint, -1);
	}
	private Point computeSize(Composite parent, int wHint, int hHint,
			int ncolumns) {
		Control[] children = parent.getChildren();
		int cwidth = 0;
		int cheight = 0;
		Point[] sizes = new Point[children.length];
		for (int i = 0; i < children.length; i++) {
			ColumnLayoutData cd = (ColumnLayoutData)children[i].getLayoutData();
			sizes[i] = computeControlSize(children[i]);
			cwidth = Math.max(cwidth, sizes[i].x);
			cheight += sizes[i].y;
		}
		if (ncolumns == -1) {
			// must compute
			ncolumns = (wHint - leftMargin - rightMargin - horizontalSpacing)
					/ (cwidth + horizontalSpacing);
			ncolumns = Math.max(ncolumns, minNumColumns);
			ncolumns = Math.min(ncolumns, maxNumColumns);
		}
		int perColHeight = cheight / ncolumns;
		if (cheight % ncolumns !=0) 
			perColHeight++;
		int colHeight = 0;
		int[] heights = new int[ncolumns];
		int ncol = 0;
		for (int i = 0; i < sizes.length; i++) {
			int childHeight = sizes[i].y;
			if (colHeight + childHeight > perColHeight) {
				ncol++;
				if (ncol==ncolumns) {
					// overflow - start filling in
					ncol = findShortestColumn(heights);
					colHeight = heights[ncol];
				}
				else
					colHeight = 0;
			}
			colHeight += childHeight;
			if (heights[ncol] > 0)
				heights[ncol] += verticalSpacing;
			heights[ncol] += childHeight;
		}
		Point size = new Point(0, 0);
		for (int i = 0; i < ncolumns; i++) {
			size.y = Math.max(size.y, heights[i]);
		}
		size.x = cwidth * ncolumns + (ncolumns - 1) * horizontalSpacing;
		size.x += leftMargin + rightMargin;
		size.y += topMargin + bottomMargin;
		return size;
	}
	
	private Point computeControlSize(Control c) {
		ColumnLayoutData cd = (ColumnLayoutData)c.getLayoutData();
		int widthHint=cd!=null?cd.widthHint:SWT.DEFAULT;
		int heightHint = cd!=null?cd.heightHint:SWT.DEFAULT;
		return c.computeSize(widthHint, heightHint);
	}
	private int findShortestColumn(int [] heights) {
		int result = 0;
		int height = Integer.MAX_VALUE;
		for (int i=0; i<heights.length; i++) {
			if (height > heights[i]) {
				height = heights[i];
				result = i;
			}
		}
		return result;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	protected void layout(Composite parent, boolean flushCache) {
		Control[] children = parent.getChildren();
		Rectangle carea = parent.getClientArea();
		int cwidth = 0;
		int cheight = 0;
		Point[] sizes = new Point[children.length];
		for (int i = 0; i < children.length; i++) {
			sizes[i] = computeControlSize(children[i]);
			cwidth = Math.max(cwidth, sizes[i].x);
			cheight += sizes[i].y;
		}
		int ncolumns = (carea.width - leftMargin - rightMargin - horizontalSpacing)
				/ (cwidth + horizontalSpacing);
		ncolumns = Math.max(ncolumns, minNumColumns);
		ncolumns = Math.min(ncolumns, maxNumColumns);
		int realWidth = (carea.width - leftMargin - rightMargin + horizontalSpacing)
				/ ncolumns - horizontalSpacing;
		int childrenPerColumn = children.length/ncolumns;
		if (children.length % ncolumns != 0)
			childrenPerColumn++;
		int colWidth = 0;
		int colHeight = 0;
		int ncol = 0;
		int x = leftMargin, y = topMargin;
		int ccolCount=0;
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			Point csize = sizes[i];
			ccolCount++;
			int childWidth = Math.max(cwidth, realWidth);
			if (y + csize.y + bottomMargin > carea.height || ccolCount>childrenPerColumn) {
				// wrap
				x += horizontalSpacing + childWidth;
				y = topMargin;
				ncol++;
				ccolCount=1;
			}

			if (ncol == ncolumns - 1) {
				childWidth = carea.width - x - rightMargin;
			}
			child.setBounds(x, y, childWidth, csize.y);
			y += csize.y + verticalSpacing;
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMaximumWidth(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	public int computeMaximumWidth(Composite parent, boolean changed) {
		return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite,
	 *      boolean)
	 */
	public int computeMinimumWidth(Composite parent, boolean changed) {
		return computeSize(parent, 0, SWT.DEFAULT, changed).x;
	}
}