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
			sizes[i] = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
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
				colHeight = 0;
			}
			colHeight += childHeight;
			if (heights[ncol] == 0)
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
			sizes[i] = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
			cwidth = Math.max(cwidth, sizes[i].x);
			cheight += sizes[i].y;
		}
		int ncolumns = (carea.width - leftMargin - rightMargin - horizontalSpacing)
				/ (cwidth + horizontalSpacing);
		ncolumns = Math.max(ncolumns, minNumColumns);
		ncolumns = Math.min(ncolumns, maxNumColumns);
		int realWidth = (carea.width - leftMargin - rightMargin + horizontalSpacing)
				/ ncolumns - horizontalSpacing;
		System.out.println("ncolumns="+ncolumns);
		System.out.println("cwidth="+cwidth);
		System.out.println("childWidth="+realWidth);
		int colWidth = 0;
		int colHeight = 0;
		int ncol = 0;
		int x = leftMargin, y = topMargin;
		for (int i = 0; i < children.length; i++) {
			Control child = children[i];
			Point csize = sizes[i];
			if (y + csize.y + bottomMargin > carea.height) {
				// wrap
				x += horizontalSpacing + realWidth;
				y = topMargin;
				ncol++;
			}
			int childWidth = realWidth;
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