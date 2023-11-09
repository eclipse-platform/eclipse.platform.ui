/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.performance.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;

/**
 * A very efficient (but useless) layout with complicated preferred size behavior.
 * Its preferred size attempts to maintain a constant area. This can be used for
 * performance testing other layouts (by attaching this to child Composites in the
 * layout being tested). It will give a good estimate as to how the layout will
 * handle wrapping widgets.
 *
 * @since 3.1
 */
public class ConstantAreaLayout extends Layout {

	private final int area;
	private final int preferredWidth;

	public ConstantAreaLayout(int area, int preferredWidth) {
		this.area = area;
		this.preferredWidth = preferredWidth;
	}

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint,
			boolean flushCache) {

		if (wHint == 0 || hHint == 0) {
			return new Point(1,1);
		}

		if (wHint == SWT.DEFAULT) {
			if (hHint == SWT.DEFAULT) {
				wHint = preferredWidth;
			} else {
				wHint = area / hHint;
			}
		}

		if (hHint == SWT.DEFAULT) {
			hHint = area / wHint;
		}

		return new Point(wHint, hHint);
	}

	@Override
	protected void layout(Composite composite, boolean flushCache) {

	}

}
