/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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


import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.views.IViewDescriptor;
import org.junit.Assert;

/**
 * @since 3.0
 */
public class ViewDragSource extends TestDragSource {

	String targetPart;

	boolean wholeFolder;

	boolean maximized = false;

	public ViewDragSource(String part, boolean dragWholeFolder) {
		this(part, dragWholeFolder, false);
	}

	public ViewDragSource(String part, boolean dragWholeFolder,
			boolean maximized) {
		this.maximized = maximized;
		this.targetPart = part;

		wholeFolder = dragWholeFolder;
	}

	public IViewPart getPart() {
		return getPage().findView(targetPart);
	}

	@Override
	public String toString() {
		IViewDescriptor desc = WorkbenchPlugin.getDefault().getViewRegistry()
				.find(targetPart);
		String title = desc.getLabel();

		if (wholeFolder) {
			title = title + " folder";
		}

		if (maximized) {
			title = "maximized " + title;
		}

		return title;
	}

	@Override
	public void drag(TestDropLocation target) {
		IViewPart part = getPart();

		WorkbenchPage page = getPage();
		if (maximized) {
			page.toggleZoom(page.getReference(part));
		}

//        DragUtil.forceDropLocation(target);
//        ViewStack parent = ((ViewStack) (pane.getContainer()));
//
//        PartPane presentablePart = wholeFolder ? null : pane;
//        parent.paneDragStart(presentablePart, Display.getDefault()
//                .getCursorLocation(), false);
		Assert.fail("DND needs updated");

//        DragUtil.forceDropLocation(null);
	}

}
