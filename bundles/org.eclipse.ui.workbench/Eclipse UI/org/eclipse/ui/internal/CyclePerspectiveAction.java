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
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.PerspectiveLabelProvider;

/**
 * Implements a action to enable the user switch between perspectives
 * using keyboard.
 */
public class CyclePerspectiveAction extends CyclePartAction implements
        IPerspectiveListener {

    private PerspectiveLabelProvider labelProvider = new PerspectiveLabelProvider(
            false);;

    /**
     * Creates a CyclePerspectiveAction.
     */
    public CyclePerspectiveAction(IWorkbenchWindow window, boolean forward) {
        super(window, forward); //$NON-NLS-1$
        window.addPerspectiveListener(this);
        updateState();
    }

    protected void setText() {
        // TBD: Remove text and tooltip when this becomes an invisible action.
        if (forward) {
            setText(WorkbenchMessages
                    .getString("CyclePerspectiveAction.next.text")); //$NON-NLS-1$
            setToolTipText(WorkbenchMessages
                    .getString("CyclePerspectiveAction.next.toolTip")); //$NON-NLS-1$
            // @issue missing action ids
            WorkbenchHelp.setHelp(this,
                    IWorkbenchHelpContextIds.CYCLE_PERSPECTIVE_FORWARD_ACTION);
            setActionDefinitionId("org.eclipse.ui.window.nextPerspective"); //$NON-NLS-1$
        } else {
            setText(WorkbenchMessages
                    .getString("CyclePerspectiveAction.prev.text")); //$NON-NLS-1$
            setToolTipText(WorkbenchMessages
                    .getString("CyclePerspectiveAction.prev.toolTip")); //$NON-NLS-1$
            // @issue missing action ids
            WorkbenchHelp.setHelp(this,
                    IWorkbenchHelpContextIds.CYCLE_PERSPECTIVE_BACKWARD_ACTION);
            setActionDefinitionId("org.eclipse.ui.window.previousPerspective"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void dispose() {
        if (getWorkbenchWindow() == null) {
            // already disposed
            return;
        }
        getWorkbenchWindow().removePerspectiveListener(this);
        labelProvider.dispose();
        super.dispose();
    }

    /**
     * Activate the selected item.
     */
    public void activate(IWorkbenchPage page, Object selection) {
        if (selection != null) {
            IPerspectiveDescriptor persp = (IPerspectiveDescriptor) selection;
            page.setPerspective(persp);
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
        setEnabled(page.getSortedPerspectives().length >= 1);
    }

    /**
     * Add all views to the dialog in the activation order
     */
    protected void addItems(Table table, WorkbenchPage page) {
        IPerspectiveDescriptor perspectives[] = page.getSortedPerspectives();
        for (int i = perspectives.length - 1; i >= 0; i--) {
            TableItem item = new TableItem(table, SWT.NONE);
            IPerspectiveDescriptor desc = perspectives[i];
            String text = labelProvider.getText(desc);
            if(text == null)
            	text = "";//$NON-NLS-1$
            item.setText(text);
            item.setImage(labelProvider.getImage(desc));
            item.setData(desc);
        }
    }

    /**
     * Returns the string which will be shown in the table header.
     */
    protected String getTableHeader() {
        return WorkbenchMessages.getString("CyclePerspectiveAction.header"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * Method declared on IPerspectiveListener
     */
    public void perspectiveActivated(IWorkbenchPage page,
            IPerspectiveDescriptor perspective) {
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on IPerspectiveListener
     */
    public void perspectiveChanged(IWorkbenchPage page,
            IPerspectiveDescriptor perspective, String changeId) {
        // do nothing
    }

}