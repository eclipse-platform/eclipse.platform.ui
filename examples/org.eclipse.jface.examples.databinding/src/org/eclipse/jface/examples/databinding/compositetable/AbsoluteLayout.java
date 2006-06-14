/*******************************************************************************
 * Copyright (c) 2006 Coconut Palm Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Coconut Palm Software, Inc. - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.examples.databinding.compositetable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * A layout manager that lays out child controls in absolute positions.  
 * Each control's bounds is specified by setting an SWT Rectangle
 * object specifying the control's bounds into the control's layout data.
 * <p>
 * Use this layout manager whenever you would have used a null layout
 * previously.
 * 
 * @since 3.2
 */
public class AbsoluteLayout extends Layout {

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
	 */
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flush) {
		Point result = new Point(0, 0);
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			Rectangle bounds = (Rectangle) children[i].getLayoutData();
			if (bounds == null) {
				continue;
			}
			int rightBounds = bounds.x + bounds.width;
			int bottomBounds = bounds.y + bounds.height;
			if (result.x < rightBounds) {
				result.x = rightBounds;
			}
			if (result.y < bottomBounds) {
				result.y = bottomBounds;
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
	 */
	protected void layout(Composite composite, boolean flush) {
		Control[] children = composite.getChildren();
		for (int i = 0; i < children.length; i++) {
			Rectangle bounds = (Rectangle) children[i].getLayoutData();
			if (bounds == null) {
				continue;
			}
			children[i].setBounds(bounds);
		}
	}
}

