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

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.ViewStack;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.TestDropLocation;
import org.eclipse.ui.views.IViewDescriptor;

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

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDragSource#getName()
     */
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

    /* (non-Javadoc)
     * @see org.eclipse.ui.tests.dnd.TestDragSource#drag(org.eclipse.swt.graphics.Point)
     */
    public void drag(TestDropLocation target) {
        IViewPart part = getPart();

        WorkbenchPage page = getPage();
        PartPane pane = ((ViewSite) part.getSite()).getPane();
        if (maximized) {
            page.toggleZoom(pane.getPartReference());
        }
        
        DragUtil.forceDropLocation(target);
        ViewStack parent = ((ViewStack) (pane.getContainer()));
        
        PartPane presentablePart = wholeFolder ? null : pane;
        parent.paneDragStart(presentablePart, Display.getDefault()
                .getCursorLocation(), false);

        DragUtil.forceDropLocation(null);
    }

}
