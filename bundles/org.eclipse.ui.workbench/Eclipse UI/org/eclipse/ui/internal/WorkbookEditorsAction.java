/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;

/**
 * Implements an action to open a list of open editors in the
 * currently or last active workbook
 * 
 * @since 3.0
 */
public class WorkbookEditorsAction extends Action implements
        ActionFactory.IWorkbenchAction, IPageListener, IPartListener {

    /**
     * The workbench window; or <code>null</code> if this
     * action has been <code>dispose</code>d.
     */
    private IWorkbenchWindow workbenchWindow;

    /**
     * Constructor for NavigateWorkbenchAction.
     */
    public WorkbookEditorsAction(IWorkbenchWindow window) {
        super(WorkbenchMessages.getString("WorkbookEditorsAction.label")); //$NON-NLS-1$
        if (window == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchWindow = window;
        // Do we need help here ?
        //WorkbenchHelp.setHelp(this, IHelpContextIds.WORKBENCH_EDITORS_ACTION);
        setActionDefinitionId("org.eclipse.ui.window.openEditorDropDown"); //$NON-NLS-1$
        workbenchWindow.addPageListener(this);
        workbenchWindow.getPartService().addPartListener(this);
        updateState();
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        if (workbenchWindow == null) {
            // action has been disposed
            return;
        }
        IWorkbenchPage page = workbenchWindow.getActivePage();
        if (page != null) {
            WorkbenchPage wbp = (WorkbenchPage) page;
            EditorAreaHelper eah = wbp.getEditorPresentation();
            if (eah != null)
                eah.displayEditorList();
        }
    }

    /* (non-Javadoc)
     * Method declared on ActionFactory.IWorkbenchAction.
     */
    public void dispose() {
        workbenchWindow.removePageListener(this);
        workbenchWindow.getPartService().removePartListener(this);
        workbenchWindow = null;
    }

    /**
     * Updates the enabled state.
     */
    public void updateState() {
        IWorkbenchPage page = null;
        if (workbenchWindow != null)
            page = workbenchWindow.getActivePage();
        if (page == null) {
            setEnabled(false);
            return;
        }
        // enable iff there is at least one other editor to switch to
        setEnabled(page.getEditorReferences().length >= 1);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageActivated(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageActivated(IWorkbenchPage page) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPageListener#pageClosed(org.eclipse.ui.IWorkbenchPage)
     */
    public void pageClosed(IWorkbenchPage page) {
        updateState();
    }

    public void pageOpened(IWorkbenchPage page) {
        updateState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partActivated(IWorkbenchPart part) {
        //no-op
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
     */
    public void partBroughtToTop(IWorkbenchPart part) {
        //no-op
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
     */
    public void partClosed(IWorkbenchPart part) {
        updateState();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
     */
    public void partDeactivated(IWorkbenchPart part) {
        //no-op
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
     */
    public void partOpened(IWorkbenchPart part) {
        updateState();
    }
}