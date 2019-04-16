/*******************************************************************************
 * Copyright (c) 2016, 2017 Stefan Xenos and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stefan Xenos - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.forms.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.ILayoutExtension;

/**
 * Factory for creating test controls which try to maintain a constant area when
 * their width changes.
 */
public final class ControlFactory {

	/**
	 * Implements a layout intended for use within unit tests. The layout simulates
	 * a wrapping control by attempting to maintain a constant area. When the layout
	 * is compressed or stretched, its preferred height will increase or decrease in
	 * order to preserve the layout's area.
	 * <p>
	 * This layout also records whether or not its cache was ever flushed, allowing
	 * unit tests to test that the flush flags propagate correctly.
	 * <p>
	 * This layout does not reposition its children. It is meant to be used with
	 * Composites that have no children.
	 */
	public static class TestLayout extends Layout {
		final int maxWidth;
		final int desiredArea;
		public boolean wasChanged = false;
		public Rectangle bounds = new Rectangle(0, 0, 0, 0);

		public TestLayout(int maxWidth, int desiredArea) {
			super();
			this.maxWidth = maxWidth;
			this.desiredArea = desiredArea;
		}

		protected void recordChanged(boolean changed) {
			if (changed) {
				this.wasChanged = changed;
			}
		}

		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			recordChanged(flushCache);

			if (wHint == SWT.DEFAULT) {
				wHint = maxWidth;
			}

			if (hHint == SWT.DEFAULT) {
				hHint = wHint <= 0 ? desiredArea : (desiredArea / wHint);
			}

			return new Point(wHint, hHint);
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			recordChanged(flushCache);

			bounds = composite.getBounds();
		}
	}

	private static class TestLayoutWithExtension extends TestLayout implements ILayoutExtension {

		final int minWidth;

		public TestLayoutWithExtension(int minWidth, int maxWidth, int desiredArea) {
			super(maxWidth, desiredArea);
			this.minWidth = minWidth;
		}

		@Override
		public int computeMinimumWidth(Composite parent, boolean changed) {
			recordChanged(changed);
			return minWidth;
		}

		@Override
		public int computeMaximumWidth(Composite parent, boolean changed) {
			recordChanged(changed);
			return maxWidth;
		}

	}

	/**
	 * Creates a wrapping layout that does not implement ILayoutExtension and
	 * attempts to maintain a constant area.
	 */
	public static TestLayout createLayout(int maxWidth, int heightAtMaxWidth) {
		int area = heightAtMaxWidth * maxWidth;
		return new TestLayout(maxWidth, area);
	}

	/**
	 * Creates a wrapping layout that implements ILayoutExtension and attempts to maintain a constant area.
	 */
	public static TestLayout createLayout(int minWidth, int maxWidth, int heightAtMaxWidth) {
		int area = heightAtMaxWidth * maxWidth;
		return new TestLayoutWithExtension(minWidth, maxWidth, area);
	}

	/**
	 * Creates a new composite that attempts to maintain a constant area. The
	 * composite's layout implements ILayoutExtension. It will not lay out its
	 * children.
	 */
	public static Composite create(Composite parent, int minWidth, int maxWidth, int heightAtMaxWidth) {
		Composite newControl = new Composite(parent, SWT.NONE);
		newControl.setLayout(createLayout(minWidth, maxWidth, heightAtMaxWidth));
		return newControl;
	}

	/**
	 * Creates a new composite that attempts to maintain a constant area. The
	 * composite's layout does not implement ILayoutExtension. It will not lay
	 * out its children.
	 */
	public static Composite create(Composite parent, int maxWidth, int heightAtMaxWidth) {
		Composite newControl = new Composite(parent, SWT.NONE);
		newControl.setLayout(createLayout(maxWidth, heightAtMaxWidth));
		return newControl;
	}
}
