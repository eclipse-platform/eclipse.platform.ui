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

public class SystemMenuFastView extends ContributionItem {

    private ViewPane viewPane;

    public SystemMenuFastView(ViewPane viewPane) {
        this.viewPane = viewPane;
    }

    public void dispose() {
        viewPane = null;
    }

    public void fill(Menu menu, int index) {
        WorkbenchWindow workbenchWindow = (WorkbenchWindow) viewPane.getPage()
                .getWorkbenchWindow();
        if (workbenchWindow.getFastViewBar() != null
                && !viewPane.getPage().getActivePerspective().isFixedView(viewPane.getViewReference())) {
            final MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
            menuItem.setSelection(viewPane.getPage().getActivePerspective().isFastView(viewPane.getViewReference()));
            menuItem.setText(WorkbenchMessages.getString("ViewPane.fastView")); //$NON-NLS-1$
            menuItem.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                	if (menuItem.getSelection()) {
                		viewPane.doMakeFast();
                	} else {
                		viewPane.doRemoveFast();
                	}
                }
            });
        }
    }

    public boolean isDynamic() {
        return true;
    }
}