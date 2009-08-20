/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.wizards.newresource;

import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewGroupMainPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;


/**
 * Standard workbench wizard that create a new group resource in the workspace.
 * <p>
 * This class may be instantiated and used without further configuration;
 * this class is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * IWorkbenchWizard wizard = new BasicNewGroupResourceWizard();
 * wizard.init(workbench, selection);
 * WizardDialog dialog = new WizardDialog(shell, wizard);
 * dialog.open();
 * </pre>
 * During the call to <code>open</code>, the wizard dialog is presented to the
 * user. When the user hits Finish, a group resource at the user-specified
 * workspace path is created, the dialog closes, and the call to
 * <code>open</code> returns.
 * </p>
 * @noextend This class is not intended to be subclassed by clients.
 */

public class BasicNewGroupResourceWizard extends BasicNewResourceWizard {
    private WizardNewGroupMainPage mainPage;

	/**
	 * The wizard id for creating new groups in the workspace.
	 * @since 3.4
	 */
	public static final String WIZARD_ID = "org.eclipse.ui.wizards.new.group"; //$NON-NLS-1$

	/**
     * Creates a wizard for creating a new folder resource in the workspace.
     */
    public BasicNewGroupResourceWizard() {
        super();
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewGroupMainPage(ResourceMessages.NewGroup_text, getSelection()); 
        addPage(mainPage);
    }

    /* (non-Javadoc)
     * Method declared on IWorkbenchWizard.
     */
    public void init(IWorkbench workbench, IStructuredSelection currentSelection) {
        super.init(workbench, currentSelection);
        setWindowTitle(ResourceMessages.NewGroup_title);
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * Method declared on BasicNewResourceWizard.
     */
    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = IDEWorkbenchPlugin.getIDEImageDescriptor("wizban/newgroup_wiz.png");//$NON-NLS-1$
        setDefaultPageImageDescriptor(desc);
    }

    /* (non-Javadoc)
     * Method declared on IWizard.
     */
    public boolean performFinish() {
        IFolder folder = mainPage.createNewGroup();
        if (folder == null) {
			return false;
		}

        selectAndReveal(folder);

        return true;
    }
}