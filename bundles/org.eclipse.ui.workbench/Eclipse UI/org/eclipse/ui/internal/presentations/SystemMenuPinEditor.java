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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class SystemMenuPinEditor extends ContributionItem {

    private EditorPane editorPane;

    public SystemMenuPinEditor(EditorPane editorPane) {
        this.editorPane = editorPane;
    }

    public void dispose() {
        editorPane = null;
    }
    
    public void fill(Menu menu, int index) {
        boolean reuseEditor = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
        if (!reuseEditor) return;
        IWorkbenchPart part = editorPane.getPartReference().getPart(false);
        if (part == null) return;
        final MenuItem item = new MenuItem(menu, SWT.CHECK);
        item.setText(WorkbenchMessages.getString("EditorPane.pinEditor")); //$NON-NLS-1$
        item.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IWorkbenchPart part = editorPane.getPartReference().getPart(
                        true);
                if (part == null) {
                    // this should never happen
                    item.setSelection(false);
                    item.setEnabled(false);
                } else {
                    ((EditorSite) part.getSite()).setReuseEditor(!item
                            .getSelection());
                }
            }
        });
        item.setEnabled(true);
        item.setSelection(!((EditorSite) part.getSite()).getReuseEditor());
    }
    
    public boolean isDynamic() {
        return true;
    }
}