/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dnd;

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.TestDropLocation;

/**
 * @since 3.0
 */
public class DragOperations {

    /**
     * Drags the given editor to the given location
     * 
     * @param editor
     * @param target
     * @param wholeFolder
     */
    public static void drag(IEditorPart editor, TestDropLocation target,
            boolean wholeFolder) {
        DragUtil.forceDropLocation(target);

        PartPane pane = ((EditorSite) editor.getSite()).getPane();
        EditorStack parent = ((EditorStack) (pane.getContainer()));

        PartPane part = wholeFolder ? null : pane;
        parent.paneDragStart(part, Display.getDefault().getCursorLocation(), false);

        DragUtil.forceDropLocation(null);
    }

    /**
     * Returns the name of the given editor
     * 
     * @param editor
     * @return
     */
    public static String getName(IEditorPart editor) {
        PartPane pane = ((EditorSite) editor.getSite()).getPane();
        IWorkbenchPartReference ref = pane.getPartReference();
        return ref.getPartName();
    }

    public static PartPane getPane(IEditorPart editor) {
        return ((EditorSite) editor.getSite()).getPane();
    }

    public static PartPane getPane(IViewPart view) {
        return ((ViewSite) view.getSite()).getPane();
    }

    public static Rectangle getDisplayBounds(PartPane pane) {
        LayoutPart parent = ((LayoutPart) (pane.getContainer()));
        Rectangle bounds = DragUtil.getDisplayBounds(parent.getControl());

        return bounds;
    }

    public static Point getLocation(PartPane pane, int side) {

        return DragOperations.getPoint(getDisplayBounds(pane), side);
    }

    /**
     * @param page
     * @param i
     * @return
     */
    public static Point getPointInEditorArea(WorkbenchPage page, int side) {
        return DragOperations.getPoint(DragUtil.getDisplayBounds(page
                .getEditorPresentation().getLayoutPart().getControl()), side);
    }

    public static Point getPoint(Rectangle bounds, int side) {
        Point centerPoint = Geometry.centerPoint(bounds);

        switch (side) {
        case SWT.TOP:
            return new Point(centerPoint.x, bounds.y + 1);
        case SWT.BOTTOM:
            return new Point(centerPoint.x, bounds.y + bounds.height - 1);
        case SWT.LEFT:
            return new Point(bounds.x + 1, centerPoint.y);
        case SWT.RIGHT:
            return new Point(bounds.x + bounds.width - 1, centerPoint.y);
        }

        return centerPoint;
    }

    public static String nameForConstant(int swtSideConstant) {
        switch (swtSideConstant) {
        case SWT.TOP:
            return "top";
        case SWT.BOTTOM:
            return "bottom";
        case SWT.LEFT:
            return "left";
        case SWT.RIGHT:
            return "right";
        }

        return "center";
    }

    /**
     * @param targetPart
     * @return
     */
    public static String getName(IViewPart targetPart) {
        return targetPart.getTitle();
    }

    /**
     * 
     * 
     * @param page
     * @return
     */
    public static String getLayoutDescription(WorkbenchPage page) {
        StringBuffer buf = new StringBuffer();

        page.getActivePerspective().describeLayout(buf);

        // Test result -- this will be a value in the resulting map
        return buf.toString();
    }
}
