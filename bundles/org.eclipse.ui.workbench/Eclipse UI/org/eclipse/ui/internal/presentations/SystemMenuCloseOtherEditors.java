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
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.WorkbenchMessages;

public class SystemMenuCloseOtherEditors extends ContributionItem {

    private EditorPane editorPane;

    public SystemMenuCloseOtherEditors(EditorPane editorPane) {
        this.editorPane = editorPane;
    }

    public void dispose() {
        editorPane = null;
    }
    
    public void fill(Menu menu, int index) {
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.setText(WorkbenchMessages.getString("PartPane.closeOthers")); //$NON-NLS-1$
        menuItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                editorPane.doHideOthers();
            }
        });

        menuItem.setEnabled(editorPane.getPage().getEditors().length > 1);
    }
    
    public boolean isDynamic() {
        return true;
    }
}