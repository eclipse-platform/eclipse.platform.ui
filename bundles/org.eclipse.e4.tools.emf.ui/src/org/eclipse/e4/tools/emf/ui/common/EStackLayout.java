/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.tools.emf.ui.common;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public class EStackLayout extends StackLayout {
	private boolean onlyVisible;

	public EStackLayout() {
		this(true);
	}

	public EStackLayout(boolean onlyVisible) {
		this.onlyVisible = onlyVisible;
	}

	@Override
	protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
		if (topControl != null && onlyVisible) {
			int maxWidth = 0;
			int maxHeight = 0;
			Point size = topControl.computeSize(wHint, hHint, flushCache);
			maxWidth = Math.max(size.x, maxWidth);
			maxHeight = Math.max(size.y, maxHeight);
			int width = maxWidth + 2 * marginWidth;
			int height = maxHeight + 2 * marginHeight;
			if (wHint != SWT.DEFAULT)
				width = wHint;
			if (hHint != SWT.DEFAULT)
				height = hHint;
			return new Point(width, height);
		}
		return super.computeSize(composite, wHint, hHint, flushCache);
	}
}
