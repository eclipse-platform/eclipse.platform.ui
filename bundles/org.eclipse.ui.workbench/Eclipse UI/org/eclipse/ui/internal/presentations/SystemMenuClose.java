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
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

public class SystemMenuClose extends ContributionItem {

    private IPresentablePart presentablePart;

    private IStackPresentationSite stackPresentationSite;

    public SystemMenuClose(IPresentablePart presentablePart,
            IStackPresentationSite stackPresentationSite) {
        this.presentablePart = presentablePart;
        this.stackPresentationSite = stackPresentationSite;
    }

    public void dispose() {
        presentablePart = null;
        stackPresentationSite = null;
    }
    
    public void fill(Menu menu, int index) {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.setText(WorkbenchMessages.getString("PartPane.close")); //$NON-NLS-1$
        menuItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                stackPresentationSite.close(presentablePart);
            }
        });

        menuItem.setEnabled(presentablePart != null
                && stackPresentationSite.isCloseable(presentablePart));
    }
    
    public boolean isDynamic() {
        return true;
    }
}