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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;

/**
 * This arranges its controls into 5 'slots' defined by its composite children
 * <ol>
 * <li>Top: spans the entire width and abuts the top of the container</li>
 * <li>Bottom: spans the entire width and abuts the bottom of the container</li>
 * <li>Left: spans the space between 'top' and 'bottom' and abuts the left of
 * the container</li>
 * <li>Right: spans the space between 'top' and 'bottom' and abuts the right of
 * the container</li>
 * <li>Center: fills the area remaining once the other controls have been
 * positioned</li>
 * </ol>
 * 
 * <strong>NOTE:</strong> <i>All</i> the child controls must exist. Also,
 * computeSize is not implemented because we expect this to be used in
 * situations (i.e. shells) where the outer bounds are always 'set', not
 * computed. Also, the interior structure of the center may contain overlapping
 * controls so it may not be capable of performing the calculation.
 * 
 * @author emoffatt
 * 
 */
public class TrimmedPartLayout extends Layout {
	public Composite top;
	public Composite bottom;
	public Composite left;
	public Composite right;
	public Composite clientArea;

	/**
	 * This layout is used to support parts that want trim for their containing
	 * composites.
	 * 
	 * @param trimOwner
	 */
	public TrimmedPartLayout(Composite parent) {
		clientArea = new Composite(parent, SWT.NONE);
		clientArea.setLayout(new FillLayout());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite
	 * , int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		// We can't actually compute a size so return a default
		return new Point(SWT.DEFAULT, SWT.DEFAULT);
	}

	protected void layout(Composite composite, boolean flushCache) {
		Rectangle ca = composite.getClientArea();
		Rectangle caRect = new Rectangle(0, 0, ca.width, ca.height);

		// Optimization
		final Shell shell = composite.getShell();
		shell.setLayoutDeferred(true);

		// 'Top' spans the entire area
		if (top != null) {
			Point topSize = top.computeSize(caRect.width, SWT.DEFAULT, true);
			caRect.y += topSize.y;
			caRect.height -= topSize.y;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(0, 0, caRect.width, topSize.y);
			if (!newBounds.equals(top.getBounds())) {
				top.setBounds(newBounds);
				shell.layout(new Control[] { top });
			}
		}

		// 'Bottom' spans the entire area
		if (bottom != null) {
			Point bottomSize = bottom.computeSize(caRect.width, SWT.DEFAULT,
					true);
			caRect.height -= bottomSize.y;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(0, caRect.y + caRect.height,
					caRect.width, bottomSize.y);
			if (!newBounds.equals(bottom.getBounds())) {
				bottom.setBounds(newBounds);
				shell.layout(new Control[] { bottom });
			}
		}

		// 'Left' spans between 'top' and 'bottom'
		if (left != null) {
			Point leftSize = left.computeSize(SWT.DEFAULT, caRect.height, true);
			caRect.x += leftSize.x;
			caRect.width -= leftSize.x;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(0, caRect.y, leftSize.x,
					caRect.height);
			if (!newBounds.equals(left.getBounds())) {
				left.setBounds(newBounds);
				shell.layout(new Control[] { left });
			}
		}

		// 'Right' spans between 'top' and 'bottom'
		if (right != null) {
			Point rightSize = right.computeSize(SWT.DEFAULT, caRect.height,
					true);
			caRect.width -= rightSize.x;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(caRect.x + caRect.width,
					caRect.y, rightSize.x, caRect.height);
			if (!newBounds.equals(right.getBounds())) {
				right.setBounds(caRect.x + caRect.width, caRect.y, rightSize.x,
						caRect.height);
				shell.layout(new Control[] { right });
			}
		}

		// Don't layout unless we've changed
		if (!caRect.equals(clientArea.getBounds())) {
			clientArea.setBounds(caRect);
			shell.layout(new Control[] { clientArea });
		}

		// Now we'll allow the layout to proceed
		shell.getDisplay().asyncExec(new Runnable() {
			public void run() {
				shell.setLayoutDeferred(false);
			}
		});
	}

}
