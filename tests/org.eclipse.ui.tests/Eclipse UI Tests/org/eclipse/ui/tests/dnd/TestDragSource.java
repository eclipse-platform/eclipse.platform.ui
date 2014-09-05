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

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.dnd.TestDropLocation;

/**
 * @since 3.0
 */
public abstract class TestDragSource implements IWorkbenchWindowProvider {
    private WorkbenchPage page;

    @Override
	public abstract String toString();

    public abstract void drag(TestDropLocation target);

    public void setPage(WorkbenchPage page) {
        this.page = page;
    }

    @Override
	public IWorkbenchWindow getWorkbenchWindow() {
        return getPage().getWorkbenchWindow();
    }
    
    public WorkbenchPage getPage() {
        if (page == null) {
            page = (WorkbenchPage) ((WorkbenchWindow) PlatformUI
                    .getWorkbench().getActiveWorkbenchWindow()).getActivePage();
        }
        return page;
    }
    
    
}
