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

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IHelpContextIds;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;

/**
 * Standard action for creating a folder resource within the currently
 * selected folder or project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @deprecated should use NewWizardMenu to populate a New submenu instead (see Navigator view)
 */
public class CreateFolderAction extends SelectionListenerAction {

    /**
     * The id of this action.
     */
    public static final String ID = PlatformUI.PLUGIN_ID
            + ".CreateFolderAction";//$NON-NLS-1$

    /**
     * The shell in which to show any dialogs.
     */
    private Shell shell;

    /**
     * Creates a new action for creating a folder resource.
     *
     * @param shell the shell for any dialogs
     * 
     * @deprecated see deprecated tag on class
     */
    public CreateFolderAction(Shell shell) {
        super(IDEWorkbenchMessages.getString("CreateFolderAction.text")); //$NON-NLS-1$
        if (shell == null) {
            throw new IllegalArgumentException();
        }
        this.shell = shell;
        setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
        setToolTipText(IDEWorkbenchMessages
                .getString("CreateFolderAction.toolTip")); //$NON-NLS-1$
        setId(ID);
        WorkbenchHelp.setHelp(this, IHelpContextIds.CREATE_FOLDER_ACTION);
    }

    /**
     * The <code>CreateFolderAction</code> implementation of this
     * <code>IAction</code> method opens a <code>BasicNewFolderResourceWizard</code>
     * in a wizard dialog under the shell passed to the constructor.
     */
    public void run() {
        BasicNewFolderResourceWizard wizard = new BasicNewFolderResourceWizard();
        wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
        wizard.setNeedsProgressMonitor(true);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        dialog.getShell().setText(
                IDEWorkbenchMessages.getString("CreateFolderAction.title")); //$NON-NLS-1$
        WorkbenchHelp.setHelp(dialog.getShell(),
                IHelpContextIds.NEW_FOLDER_WIZARD);
        dialog.open();

    }

    /**
     * The <code>CreateFolderAction</code> implementation of this
     * <code>SelectionListenerAction</code> method enables the action only
     * if the selection contains folders and open projects.
     */
    protected boolean updateSelection(IStructuredSelection s) {
        if (!super.updateSelection(s)) {
            return false;
        }
        Iterator resources = getSelectedResources().iterator();
        while (resources.hasNext()) {
            IResource resource = (IResource) resources.next();
            if (!resourceIsType(resource, IResource.PROJECT | IResource.FOLDER)
                    || !resource.isAccessible()) {
                return false;
            }
        }
        return true;
    }
}