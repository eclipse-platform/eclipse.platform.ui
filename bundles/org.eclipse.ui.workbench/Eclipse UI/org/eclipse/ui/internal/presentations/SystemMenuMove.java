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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuMove extends ContributionItem {

    private IPresentablePart presentablePart;

    private IStackPresentationSite stackPresentationSite;

    public SystemMenuMove(IPresentablePart presentablePart,
            IStackPresentationSite stackPresentationSite) {
        this.presentablePart = presentablePart;
        this.stackPresentationSite = stackPresentationSite;
    }

    public void dispose() {
        presentablePart = null;
        stackPresentationSite = null;
    }

    protected void fill(Menu menu, int index, String movePart) {
        MenuItem menuItem = new MenuItem(menu, SWT.CASCADE);
        menuItem.setText(WorkbenchMessages.getString("PartPane.move")); //$NON-NLS-1$
        Menu moveMenu = new Menu(menu);
        menuItem.setMenu(moveMenu);
        final Display display = moveMenu.getDisplay();
        MenuItem menuItem2 = new MenuItem(moveMenu, SWT.NONE);
        menuItem2.setText(WorkbenchMessages.getString(movePart)); //$NON-NLS-1$
        menuItem2.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                stackPresentationSite.dragStart(presentablePart, display
                        .getCursorLocation(), true);
            }
        });
        menuItem2.setEnabled(presentablePart != null
                && stackPresentationSite.isMoveable(presentablePart));
        MenuItem menuItem3 = new MenuItem(moveMenu, SWT.NONE);
        menuItem3.setText(WorkbenchMessages.getString("ViewPane.moveFolder")); //$NON-NLS-1$
        menuItem3.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                stackPresentationSite.dragStart(display.getCursorLocation(),
                        true);
            }
        });
        menuItem3.setEnabled(true);
    }

    public boolean isDynamic() {
        return true;
    }
}