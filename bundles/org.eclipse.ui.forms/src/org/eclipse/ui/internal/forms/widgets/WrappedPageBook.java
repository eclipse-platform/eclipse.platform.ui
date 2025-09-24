/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.forms.widgets;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.ILayoutExtension;

/**
 * A pagebook is a composite control where only a single control is visible at
 * a time. It is similar to a notebook, but without tabs.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class WrappedPageBook extends Composite {
	class PageBookLayout extends Layout implements ILayoutExtension {
		@Override
		protected Point computeSize(Composite composite, int wHint, int hHint,
				boolean flushCache) {
			if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
			Point result = null;
			if (currentPage != null) {
				result = currentPage.computeSize(wHint, hHint, flushCache);
			} else {
				result = new Point(0, 0);
			}
			return result;
		}

		@Override
		protected void layout(Composite composite, boolean flushCache) {
			if (currentPage != null) {
				currentPage.setBounds(composite.getClientArea());
			}
		}

		@Override
		public int computeMaximumWidth(Composite parent, boolean changed) {
			return computeSize(parent, SWT.DEFAULT, SWT.DEFAULT, changed).x;
		}

		@Override
		public int computeMinimumWidth(Composite parent, boolean changed) {
			return computeSize(parent, 0, SWT.DEFAULT, changed).x;
		}
	}

	/**
	 * The current control; <code>null</code> if none.
	 */
	private Control currentPage;

	/**
	 * Creates a new empty pagebook.
	 *
	 * @param parent
	 *            the parent composite
	 * @param style
	 *            the SWT style bits
	 */
	public WrappedPageBook(Composite parent, int style) {
		super(parent, style);
		setLayout(new PageBookLayout());
	}

	/**
	 * Shows the given page. This method has no effect if the given page is not
	 * contained in this pagebook.
	 *
	 * @param page
	 *            the page to show
	 */
	public void showPage(Control page) {
		if (page == currentPage) {
			return;
		}
		if (page.getParent() != this) {
			return;
		}
		Control oldPage = currentPage;
		currentPage = page;
		// show new page
		if (!page.isDisposed()) {
			// page.setVisible(true);
			layout(true);
			page.setVisible(true);
		}
		// hide old *after* new page has been made visible in order to avoid
		// flashing
		if (oldPage != null && !oldPage.isDisposed()) {
			oldPage.setVisible(false);
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return ((PageBookLayout) getLayout()).computeSize(this, wHint, hHint,
				changed);
	}
}
