/*
 * Created on Jan 20, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.ui.forms.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;

/**
 * @author dejan
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ColumnLayout extends Layout implements ILayoutExtension {
	public int minNumColumns=1;
	public int maxNumColumns=3;
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
	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {

		if (wHint==0)
			return computeSize(composite, wHint, minNumColumns);
		else if (wHint==SWT.DEFAULT)
			return computeSize(composite, wHint, maxNumColumns);
		else
			return computeSize(composite, wHint, -1);
	}

	private Point computeSize(Composite parent, int width, int ncolumns) {
		Control [] children = parent.getChildren();
		int cwidth = 0;
		int cheight = 0;
		
		for (int i=0; i<children.length; i++) {
			Point csize = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT);
			cwidth = Math.max(cwidth, csize.x);
			cheight += csize.y;
		}
		if (ncolumns  == -1) {
			// must compute
		}
		int colHeight = cheight/ncolumns;
		for (int i=0; i<children.length; i++) {
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flushCache) {
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMaximumWidth(org.eclipse.swt.widgets.Composite, boolean)
	 */
	public int computeMaximumWidth(Composite parent, boolean changed) {
		return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.forms.widgets.ILayoutExtension#computeMinimumWidth(org.eclipse.swt.widgets.Composite, boolean)
	 */
	public int computeMinimumWidth(Composite parent, boolean changed) {
		return computeSize(parent, 0, SWT.DEFAULT, changed).x;
	}
}