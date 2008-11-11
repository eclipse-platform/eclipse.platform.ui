/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.workbench.ui.renderers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * This arranges its controls into 5 'slots' defined by its composite children
 * <ol>
 *   <li>Top: spans the entire width and abuts the top of the container</li>
 *   <li>Bottom: spans the entire width and abuts the bottom of the container</li>
 *   <li>Left: spans the space between 'top' and 'bottom' and abuts the left of the container</li>
 *   <li>Right: spans the space between 'top' and 'bottom' and abuts the right of the container</li>
 *   <li>Center: fills the area remaining once the other controls have been positioned</li>
 * </ol>
 *
 * <strong>NOTE:</strong> <i>All</i> the child controls must exist. Also, computeSize is not
 * implemented because we expect this to be used in situations (i.e. shells) where the outer
 * bounds are always 'set', not computed. Also, the interior structure of the center may contain
 * overlapping controls so it may not be capable of performing the calculation.
 * 
 * @author emoffatt
 *
 */
public class TrimmedLayout extends Layout {
	Point cachedSize = null;
	Point topSize = null;
	Point bottomSize = null;
	Point leftSize =  null;
	Point rightSize =  null;
	Point centerSize =  null;
	
	public Composite top;
	public Composite bottom;
	public Composite left;
	public Composite right;
	public Composite center;
	
	public TrimmedLayout(Composite container) {
		top = new Composite(container, SWT.NONE);
		RowLayout trl = new RowLayout(SWT.HORIZONTAL);
		trl.marginBottom = trl.marginTop = 1; 
		top.setLayout(trl);

		bottom = new Composite(container, SWT.NONE);
		RowLayout brl = new RowLayout(SWT.HORIZONTAL);
		brl.marginBottom = brl.marginTop = 1; 
		bottom.setLayout(brl);
		
		left = new Composite(container, SWT.NONE);
		RowLayout lrl = new RowLayout(SWT.VERTICAL);
		lrl.marginLeft = lrl.marginRight = 1; 
		left.setLayout(lrl);
		
		right = new Composite(container, SWT.NONE);
		RowLayout rrl = new RowLayout(SWT.VERTICAL);
		rrl.marginLeft = rrl.marginRight = 1; 
		right.setLayout(rrl);
		
		center = new Composite(container, SWT.NONE);
		center.setLayout(new FillLayout());
	}
	
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		return new Point(SWT.DEFAULT, SWT.DEFAULT);
	}

	protected void layout(Composite composite, boolean flushCache) {
		Rectangle ca = composite.getClientArea();
		Point caSize = new Point(ca.width, ca.height);

		Control[] kids = composite.getChildren();
		if (kids.length != 5)
			return;

		Point topSize = kids[0].computeSize(caSize.x, SWT.DEFAULT, true);
		Point bottomSize = kids[1].computeSize(caSize.x, SWT.DEFAULT, true);
		int leftOverY = caSize.y - (topSize.y + bottomSize.y);
		
		Point leftSize = kids[2].computeSize(SWT.DEFAULT, leftOverY, true);
		Point rightSize = kids[3].computeSize(SWT.DEFAULT, leftOverY, true);
		int leftOverX = caSize.x - (leftSize.x + rightSize.x);
		
		composite.getShell().setRedraw(false);
		kids[0].setBounds(0,0, caSize.x, topSize.y);
		kids[1].setBounds(0,caSize.y-bottomSize.y, caSize.x, bottomSize.y);
		kids[2].setBounds(0,topSize.y, leftSize.x, leftOverY);
		kids[3].setBounds(caSize.x - rightSize.x ,topSize.y, rightSize.x, leftOverY);
		kids[4].setBounds(leftSize.x, topSize.y, leftOverX, leftOverY);
		composite.getShell().setRedraw(true);
	}

}
