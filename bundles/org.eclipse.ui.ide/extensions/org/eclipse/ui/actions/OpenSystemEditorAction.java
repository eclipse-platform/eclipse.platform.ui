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
package org.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Standard action for opening a system editor on the currently selected file 
 * resource.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class OpenSystemEditorAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".OpenSystemEditorAction";//$NON-NLS-1$

    /**
     * The workbench page to open the editor in.
     */
    private IWorkbenchPage workbenchPage;

    /**
     * Creates a new action that will open system editors on the then-selected file 
     * resources.
     *
     * @param page the workbench page in which to open the editor
     */
    public OpenSystemEditorAction(IWorkbenchPage page) {
        super(IDEWorkbenchMessages.getString("OpenSystemEditorAction.text")); //$NON-NLS-1$
        setToolTipText(IDEWorkbenchMessages
                .getString("OpenSystemEditorAction.toolTip")); //$NON-NLS-1$
        setId(ID);
        WorkbenchHelp.setHelp(this, IIDEHelpContextIds.OPEN_SYSTEM_EDITOR_ACTION);
        if (page == null) {
            throw new IllegalArgumentException();
        }
        this.workbenchPage = page;
    }

    /**
     * Return the workbench page to open the editor in.
     *
     * @return the workbench page to open the editor in
     */
    /* package */final IWorkbenchPage getWorkbenchPage() {
        return workbenchPage;
    }

    /**
     * Opens a system editor on the given file resource.
     *
     * @param file the file resource
     */
    /* package */void openFile(IFile file) {
        try {
            getWorkbenchPage().openEditor(new FileEditorInput(file),
                    IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
        } catch (PartInitException e) {
            DialogUtil.openError(getWorkbenchPage().getWorkbenchWindow()
                    .getShell(), IDEWorkbenchMessages
                    .getString("OpenSystemEditorAction.dialogTitle"), //$NON-NLS-1$
                    e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * Method declared on IAction.
     */
    public void run() {
        Iterator itr = getSelectedResources().iterator();
        while (itr.hasNext()) {
            IResource resource = (IResource) itr.next();
            if (resource instanceof IFile)
                openFile((IFile) resource);
        }
    }

    /**
     * The <code>OpenSystemEditorAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables the action only
     * if the selection contains just file resources.
     */
    protected boolean updateSelection(IStructuredSelection selection) {
        return super.updateSelection(selection)
                && selectionIsOfType(IResource.FILE);
    }
}