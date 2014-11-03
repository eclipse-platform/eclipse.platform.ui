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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.dnd.TestDropLocation;

/**
 * @since 3.0
 */
public abstract class WorkbenchWindowDropTarget implements TestDropLocation {

    private IWorkbenchWindowProvider window;

    @Override
	public abstract String toString();

    @Override
	public abstract Point getLocation();

    public WorkbenchWindowDropTarget(IWorkbenchWindowProvider window) {
        this.window = window;
    }

    public IWorkbenchWindow getWindow() {
        return window.getWorkbenchWindow();
    }

    public Shell getShell() {
        return getWindow().getShell();
    }

    public WorkbenchPage getPage() {
        return (WorkbenchPage)getWindow().getActivePage();
    }

    @Override
	public Shell[] getShells() {
        return new Shell[] {getShell()};
    }

}
