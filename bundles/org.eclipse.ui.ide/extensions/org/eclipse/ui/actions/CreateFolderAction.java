/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.wizards.newresource.BasicNewFolderResourceWizard;


/**
 * Standard action for creating a folder resource within the currently
 * selected folder or project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @deprecated should use NewWizardMenu to populate a New submenu instead (see Navigator view)
 * @noextend This class is not intended to be subclassed by clients.
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
    protected IShellProvider shellProvider;
    
    /**
     * Creates a new action for creating a folder resource.
     *
     * @param shell the shell for any dialogs
     * 
     * @deprecated {@link #CreateFolderAction(IShellProvider)}
     */
    public CreateFolderAction(final Shell shell) {
        super(IDEWorkbenchMessages.CreateFolderAction_text);
        Assert.isNotNull(shell);
        shellProvider = new IShellProvider(){
        	public Shell getShell(){
        		return shell;
        	}
        };
        initAction();
    }

    /**
     * Creates a new action for creating a folder resource.
     * 
     * @param provider the shell for any dialogs
     * 
     * @deprecated see deprecated tag on class
     * @since 3.4
     */
    public CreateFolderAction(IShellProvider provider){
    	super(IDEWorkbenchMessages.CreateFolderAction_text);
    	Assert.isNotNull(provider);
    	shellProvider = provider;
    	initAction();
    }
    
    /**
     * Initializes for the constructor.
     */
    private void initAction(){
    	setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));
        setToolTipText(IDEWorkbenchMessages.CreateFolderAction_toolTip);
        setId(ID);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this,
				IIDEHelpContextIds.CREATE_FOLDER_ACTION);
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
        WizardDialog dialog = new WizardDialog(shellProvider.getShell(), wizard);
        dialog.create();
        dialog.getShell().setText(
                IDEWorkbenchMessages.CreateFolderAction_title);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
                IIDEHelpContextIds.NEW_FOLDER_WIZARD);
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
