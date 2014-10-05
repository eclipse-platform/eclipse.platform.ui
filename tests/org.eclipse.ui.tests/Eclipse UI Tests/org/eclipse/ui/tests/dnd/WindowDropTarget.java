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

import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * @since 3.0
 */
public class WindowDropTarget extends WorkbenchWindowDropTarget {

    private int side;

    public WindowDropTarget(IWorkbenchWindowProvider provider, int side) {
        super(provider);
        this.side = side;
    }

    @Override
	public String toString() {
        return DragOperations.nameForConstant(side) + " of window";
    }

    @Override
	public Point getLocation() {
        Shell shell = getShell();
        Rectangle clientArea = shell.getClientArea();

        return DragOperations.getPoint(Geometry.toDisplay(shell, clientArea),
                side);
    }
}
