/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class ViewTabDropTarget extends WorkbenchWindowDropTarget {

    String targetPart;

    public ViewTabDropTarget(IWorkbenchWindowProvider provider, String part) {
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

        return title + " view tab area";
    }

    @Override
	public Point getLocation() {
        Rectangle bounds = DragOperations.getDisplayBounds(DragOperations
                .getPane(getPart()));

        return new Point(bounds.x + 8, bounds.y + 8);
    }

    @Override
	public Shell getShell() {
    	return getPart().getSite().getShell();
    }
}
