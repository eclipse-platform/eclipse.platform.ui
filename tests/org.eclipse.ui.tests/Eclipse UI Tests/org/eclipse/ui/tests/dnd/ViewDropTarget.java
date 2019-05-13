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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * @since 3.0
 */
public class ViewDropTarget extends WorkbenchWindowDropTarget {

	String targetPart;

	int side;

	public ViewDropTarget(IWorkbenchWindowProvider provider, String part, int side) {
		super(provider);
		targetPart = part;
		this.side = side;
	}

	IViewPart getPart() {
		return getPage().findView(targetPart);
	}

	@Override
	public String toString() {
		IViewDescriptor desc = WorkbenchPlugin.getDefault().getViewRegistry()
				.find(targetPart);
		String title = desc.getLabel();

		return DragOperations.nameForConstant(side) + " of " + title;
	}

	@Override
	public Point getLocation() {
		return DragOperations.getLocation(DragOperations.getPane(getPart()),
				side);
	}

	@Override
	public Shell getShell() {
		return getPart().getSite().getShell();
	}
}
