/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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
package org.eclipse.ui.tests.dnd;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Note: this drop location is hardcoded to assume a presentation that has
 * a tab drop location at the upper left corner, 8 pixels away from the
 * edge in both dimensions. This drop location should be omitted from the
 * tests in situations where this does not apply (this is not a problem
 * right now since the current tests only use the tabs-on-top drop
 * location).
 *
 * @since 3.0
 */
public class ViewTitleDropTarget extends WorkbenchWindowDropTarget {

	String targetPart;

	public ViewTitleDropTarget(IWorkbenchWindowProvider provider, String part) {
		super(provider);
		targetPart = part;
	}

	IViewPart getPart() {
		return getPage().findView(targetPart);
	}

	@Override
	public String toString() {
		IViewDescriptor desc = WorkbenchPlugin.getDefault().getViewRegistry()
				.find(targetPart);
		String title = desc.getLabel();

		return title + " view title area";
	}

	@Override
	public Point getLocation() {
		Rectangle bounds = DragOperations.getDisplayBounds();

		return new Point( (bounds.x + bounds.width) - 8, bounds.y + 8);
	}

	@Override
	public Shell getShell() {
		return getPart().getSite().getShell();
	}
}
