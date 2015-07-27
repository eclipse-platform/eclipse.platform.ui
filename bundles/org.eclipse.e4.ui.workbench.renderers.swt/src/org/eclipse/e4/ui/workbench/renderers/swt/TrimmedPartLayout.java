/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;

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

	/**
	 * gutterBottom specifies the number of pixels of vertical margin that will
	 * be placed between the bottom trim component and the bottom edge of the
	 * client area. If there is no bottom trim component, the gutter serves as a
	 * margin.
	 *
	 * The default value is 0.
	 */
	public int gutterBottom = 0;

	/**
	 * gutterLeft specifies the number of pixels of horizontal margin that will
	 * be placed between the left trim component and the left edge of the client
	 * area. If there is no left trim component, the gutter serves as a margin.
	 *
	 * The default value is 0.
	 */
	public int gutterLeft = 0;

	/**
	 * gutterTop specifies the number of pixels of vertical margin that will be
	 * placed between the top trim component and the top edge of the client
	 * area. If there is no top trim component, the gutter serves as a margin.
	 *
	 * The default value is 0.
	 */
	public int gutterTop = 0;

	/**
	 * gutterRight specifies the number of pixels of horizontal margin that will
	 * be placed between the right trim component and the right edge of the
	 * client area. If there is no right trim component, the gutter serves as a
	 * margin.
	 *
	 * The default value is 0.
	 */
	public int gutterRight = 0;

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

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {
		// We can't actually compute a size so return a default
		return new Point(SWT.DEFAULT, SWT.DEFAULT);
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {
		Rectangle ca = composite.getClientArea();
		Rectangle caRect = new Rectangle(ca.x, ca.y, ca.width, ca.height);

		// 'Top' spans the entire area
		if (top != null && top.isVisible()) {
			Point topSize = top.computeSize(caRect.width, SWT.DEFAULT, true);
			caRect.y += topSize.y;
			caRect.height -= topSize.y;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(ca.x, ca.y, caRect.width,
					topSize.y);
			if (!newBounds.equals(top.getBounds())) {
				top.setBounds(newBounds);
			}
		}
		// Include the gutter whether there is a top area or not.
		caRect.y += gutterTop;
		caRect.height -= gutterTop;

		// 'Bottom' spans the entire area
		if (bottom != null && bottom.isVisible()) {
			Point bottomSize = bottom.computeSize(caRect.width, SWT.DEFAULT,
					true);
			caRect.height -= bottomSize.y;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(caRect.x, caRect.y
					+ caRect.height, caRect.width, bottomSize.y);
			if (!newBounds.equals(bottom.getBounds())) {
				bottom.setBounds(newBounds);
			}
		}
		caRect.height -= gutterBottom;

		// 'Left' spans between 'top' and 'bottom'
		if (left != null && left.isVisible()) {
			Point leftSize = left.computeSize(SWT.DEFAULT, caRect.height, true);
			caRect.x += leftSize.x;
			caRect.width -= leftSize.x;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(caRect.x - leftSize.x,
					caRect.y, leftSize.x, caRect.height);
			if (!newBounds.equals(left.getBounds())) {
				left.setBounds(newBounds);
			}
		}
		caRect.x += gutterLeft;
		caRect.width -= gutterLeft;

		// 'Right' spans between 'top' and 'bottom'
		if (right != null && right.isVisible()) {
			Point rightSize = right.computeSize(SWT.DEFAULT, caRect.height,
					true);
			caRect.width -= rightSize.x;

			// Don't layout unless we've changed
			Rectangle newBounds = new Rectangle(caRect.x + caRect.width,
					caRect.y, rightSize.x, caRect.height);
			if (!newBounds.equals(right.getBounds())) {
				right.setBounds(newBounds);
			}
		}
		caRect.width -= gutterRight;

		// Don't layout unless we've changed
		if (!caRect.equals(clientArea.getBounds())) {
			clientArea.setBounds(caRect);
		}
	}

	/**
	 * @param top2
	 * @param b
	 * @return
	 */
	public Composite getTrimComposite(Composite parent, int side) {
		if (side == SWT.TOP) {
			if (top == null) {
				top = new Composite(parent, SWT.NONE);
				top.setLayout(new TrimBarLayout(true));
				top.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						top = null;
					}
				});
			}
			return top;
		} else if (side == SWT.BOTTOM) {
			if (bottom == null) {
				bottom = new Composite(parent, SWT.NONE);
				bottom.setLayout(new TrimBarLayout(true));
				bottom.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						bottom = null;
					}
				});
			}
			return bottom;
		} else if (side == SWT.LEFT) {
			if (left == null) {
				left = new Composite(parent, SWT.NONE);
				left.setLayout(new TrimBarLayout(false));
				left.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						left = null;
					}
				});
			}
			return left;
		} else if (side == SWT.RIGHT) {
			if (right == null) {
				right = new Composite(parent, SWT.NONE);
				right.setLayout(new TrimBarLayout(false));
				right.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						right = null;
					}
				});
			}
			return right;
		}

		// Unknown location
		return null;
	}

	public Rectangle getTrimRect(int side) {
		Rectangle caBounds = clientArea.getBounds();
		caBounds = Display.getCurrent().map(clientArea.getParent(), null,
				caBounds);

		if (side == SWT.TOP) {
			if (top != null) {
				Rectangle b = top.getBounds();
				b = top.getDisplay().map(top.getParent(), null, b);
				return b;
			}

			// Fake one
			caBounds.height = 25;
			return caBounds;
		}
		if (side == SWT.BOTTOM) {
			if (bottom != null) {
				Rectangle b = bottom.getBounds();
				b = bottom.getDisplay().map(bottom.getParent(), null, b);
				return b;
			}

			// Fake one
			caBounds.y = (caBounds.y + caBounds.height) - 25;
			caBounds.height = 25;
			return caBounds;
		}
		if (side == SWT.LEFT) {
			if (left != null && left.getChildren().length > 0) {
				Rectangle b = left.getBounds();
				b = left.getDisplay().map(left.getParent(), null, b);
				return b;
			}

			// Fake one
			caBounds.width = 25;
			return caBounds;
		}
		if (side == SWT.RIGHT) {
			if (right != null && right.getChildren().length > 0) {
				Rectangle b = right.getBounds();
				b = right.getDisplay().map(right.getParent(), null, b);
				return b;
			}

			// Fake one
			caBounds.x = (caBounds.x + caBounds.width) - 25;
			caBounds.width = 25;
			return caBounds;
		}
		return null;
	}
}
