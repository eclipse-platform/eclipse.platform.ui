/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *     		- Bug 44162 [Wizards]  Define constants for wizard ids of new.file, new.folder, and new.project
 *******************************************************************************/
package org.eclipse.ui.wizards.newresource;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFolderMainPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;

/**
 * Standard workbench wizard that create a new folder resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewFolderResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a folder resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BasicNewFolderResourceWizard extends BasicNewResourceWizard {

	/**
	 * The wizard id for creating new folders in the workspace.
	 * @since 3.4
	 */
	public static final String WIZARD_ID = "org.eclipse.ui.wizards.new.folder"; //$NON-NLS-1$
	
    private WizardNewFolderMainPage mainPage;

    /**
     * Creates a wizard for creating a new folder resource in the workspace.
     */
    public BasicNewFolderResourceWizard() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewFolderMainPage(ResourceMessages.NewFolder_text, getSelection()); 
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle(ResourceMessages.NewFolder_title);
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * Method declared on BasicNewResourceWizard.
     */
    protected void initializeDefaultPageImageDescriptor() {
      ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newfolder_wiz.png");//$NON-NLS-1$
      setDefaultPageImageDescriptor(desc);
       
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
        IFolder folder = mainPage.createNewFolder();
        if (folder == null) {
			return false;
		}

        selectAndReveal(folder);

        return true;
    }
}
