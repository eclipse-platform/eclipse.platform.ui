/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.ViewPane;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuFastView extends ContributionItem {

    private IStackPresentationSite stackPresentationSite;

    private ViewPane viewPane;

    public SystemMenuFastView(IStackPresentationSite stackPresentationSite,
            ViewPane viewPane) {
        this.stackPresentationSite = stackPresentationSite;
        this.viewPane = viewPane;
    }

    public void dispose() {
        stackPresentationSite = null;
        viewPane = null;
    }

    public void fill(Menu menu, int index) {
        WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane.getPage()
                .getWorkbenchWindow();
        if (workbenchWindow.getFastViewBar() != null
                && stackPresentationSite.isMoveable(viewPane
                        .getPresentablePart())) {
            MenuItem menuItem = new MenuItem(menu, SWT.NONE);
            menuItem.setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
            menuItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    viewPane.doMakeFast();
                }
            });
        }
    }

    public boolean isDynamic() {
        return true;
    }
}