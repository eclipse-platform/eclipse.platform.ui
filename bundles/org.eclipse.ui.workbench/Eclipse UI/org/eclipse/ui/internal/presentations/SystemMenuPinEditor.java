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

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.EditorSite;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;

public class SystemMenuPinEditor extends Action implements ISelfUpdatingAction {

    private EditorPane editorPane;

    public SystemMenuPinEditor(EditorPane pane) {
        setText(WorkbenchMessages.getString("EditorPane.pinEditor")); //$NON-NLS-1$
        setPane(pane);
    }

    public void dispose() {
        editorPane = null;
    }

    public void setPane(EditorPane pane) {
        editorPane = pane;
        update();
    }

    public void run() {
        IWorkbenchPart part = editorPane.getPartReference().getPart(true);

        ((EditorSite) part.getSite()).setReuseEditor(isChecked());
    }

    //    public void fill(Menu menu, int index) {
    //        boolean reuseEditor = WorkbenchPlugin.getDefault().getPreferenceStore()
    //                .getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
    //        if (!reuseEditor) return;
    //        IWorkbenchPart part = editorPane.getPartReference().getPart(false);
    //        if (part == null) return;
    //        final MenuItem item = new MenuItem(menu, SWT.CHECK);
    //        item.setText(WorkbenchMessages.getString("EditorPane.pinEditor")); //$NON-NLS-1$
    //        item.addSelectionListener(new SelectionAdapter() {
    //
    //            public void widgetSelected(SelectionEvent e) {
    //                IWorkbenchPart part = editorPane.getPartReference().getPart(
    //                        true);
    //                if (part == null) {
    //                    // this should never happen
    //                    item.setSelection(false);
    //                    item.setEnabled(false);
    //                } else {
    //                    ((EditorSite) part.getSite()).setReuseEditor(!item
    //                            .getSelection());
    //                }
    //            }
    //        });
    //        item.setEnabled(true);
    //        item.setSelection(!((EditorSite) part.getSite()).getReuseEditor());
    //    }

    public void update() {
        if (editorPane == null) {
            setEnabled(false);
            return;
        }

        IWorkbenchPart part = editorPane.getPartReference().getPart(false);
        if (part == null) {
            setEnabled(false);
            return;
        }
        setEnabled(true);
        setChecked(!((EditorSite) part.getSite()).getReuseEditor());
    }

    public boolean shouldBeVisible() {
        if (editorPane == null) {
            return false;
        }

        boolean reuseEditor = WorkbenchPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.REUSE_EDITORS_BOOLEAN);
        return reuseEditor;
    }

}