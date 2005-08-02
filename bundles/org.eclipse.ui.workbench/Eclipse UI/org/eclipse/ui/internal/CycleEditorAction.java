/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

/**
 * This is the implementation for both NextEditorAction and PrevEditorAction.
 */
public class CycleEditorAction extends CyclePartAction {

    /**
     * Creates a CycleEditorAction.
     * @param window the window
     * @param forward whether the editors will be cycled forward
     */
    public CycleEditorAction(IWorkbenchWindow window, boolean forward) {
        super(window, forward);
        updateState();
    }

    protected void setText() {
        // TBD: Remove text and tooltip when this becomes an invisible action.
        if (forward) {
            setText(WorkbenchMessages.CycleEditorAction_next_text); 
            setToolTipText(WorkbenchMessages.CycleEditorAction_next_toolTip); 
            // @issue missing action ids
            getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(this,
					IWorkbenchHelpContextIds.CYCLE_EDITOR_FORWARD_ACTION);
            setActionDefinitionId("org.eclipse.ui.window.nextEditor"); //$NON-NLS-1$
        } else {
            setText(WorkbenchMessages.CycleEditorAction_prev_text);
            setToolTipText(WorkbenchMessages.CycleEditorAction_prev_toolTip); 
            // @issue missing action ids
            getWorkbenchWindow().getWorkbench().getHelpSystem().setHelp(this,
					IWorkbenchHelpContextIds.CYCLE_EDITOR_BACKWARD_ACTION);
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
        return WorkbenchMessages.CycleEditorAction_header;
    }
}
