/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This is the implementation for both NextEditorAction and PrevEditorAction.
 */
public class CycleEditorAction extends CyclePartAction {

    /**
     * Creates a CycleEditorAction.
     */
    public CycleEditorAction(IWorkbenchWindow window, boolean forward) {
        super(window, forward); //$NON-NLS-1$
        updateState();
    }

    protected void setText() {
        // TBD: Remove text and tooltip when this becomes an invisible action.
        if (forward) {
            setText(WorkbenchMessages.getString("CycleEditorAction.next.text")); //$NON-NLS-1$
            setToolTipText(WorkbenchMessages
                    .getString("CycleEditorAction.next.toolTip")); //$NON-NLS-1$
            // @issue missing action ids
            WorkbenchHelp.setHelp(this,
                    IHelpContextIds.CYCLE_EDITOR_FORWARD_ACTION);
            setActionDefinitionId("org.eclipse.ui.window.nextEditor"); //$NON-NLS-1$
        } else {
            setText(WorkbenchMessages.getString("CycleEditorAction.prev.text")); //$NON-NLS-1$
            setToolTipText(WorkbenchMessages
                    .getString("CycleEditorAction.prev.toolTip")); //$NON-NLS-1$
            // @issue missing action ids
            WorkbenchHelp.setHelp(this,
                    IHelpContextIds.CYCLE_EDITOR_BACKWARD_ACTION);
            setActionDefinitionId("org.eclipse.ui.window.previousEditor"); //$NON-NLS-1$
        }
    }

    /**
     * Updates the enabled state.
     */
    public void updateState() {
        WorkbenchPage page = (WorkbenchPage) getActivePage();
        if (page == null) {
            setEnabled(false);
            return;
        }
        // enable iff there is at least one other editor to switch to
        setEnabled(page.getSortedEditors().length >= 1);
    }

    /**
     * Add all views to the dialog in the activation order
     */
    protected void addItems(Table table, WorkbenchPage page) {
        IEditorReference refs[] = page.getSortedEditors();
        for (int i = refs.length - 1; i >= 0; i--) {
            TableItem item = null;
            item = new TableItem(table, SWT.NONE);
            if (refs[i].isDirty())
                item.setText("*" + refs[i].getTitle()); //$NON-NLS-1$
            else
                item.setText(refs[i].getTitle());
            item.setImage(refs[i].getTitleImage());
            item.setData(refs[i]);
        }
    }

    /**
     * Returns the string which will be shown in the table header.
     */
    protected String getTableHeader() {
        return WorkbenchMessages.getString("CycleEditorAction.header"); //$NON-NLS-1$
    }
}