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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetEditWizard;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A working set edit wizard allows the user to edit a 
 * working set using a plugin specified working set page.
 * 
 * @since 2.0
 * @see org.eclipse.ui.dialog.IWorkingSetPage
 */
public class WorkingSetEditWizard extends Wizard implements
        IWorkingSetEditWizard {
    private IWorkingSetPage workingSetEditPage;

    private IWorkingSet workingSet;

    /**
     * Creates a new instance of the receiver.
     * 
     * @param editPage the working set page that is going to 
     * 	be used for editing a working set.
     */
    public WorkingSetEditWizard(IWorkingSetPage editPage) {
        super();
        workingSetEditPage = editPage;
        workingSetEditPage.setWizard(this);
        setWindowTitle(WorkbenchMessages.WorkingSetEditWizard_title);
    }

    /**
     * Overrides Wizard.
     * 
     * @see org.eclipse.jface.wizard.Wizard#addPages
     */
    public void addPages() {
        super.addPages();
        addPage(workingSetEditPage);
    }

    /**
     * Overrides Wizard.
     * 
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    public boolean canFinish() {
        return workingSetEditPage.isPageComplete();
    }

    /**
     * Returns the working set that is being edited.
     * 
     * @return the working set that is being edited.
     */
    public IWorkingSet getSelection() {
        return workingSet;
    }

    /**
     * Overrides Wizard.
     * Notifies the IWorkingSetPage that the wizard is being closed.
     * 
     * @see org.eclipse.jface.wizard.Wizard#performFinish
     */
    public boolean performFinish() {
        workingSetEditPage.finish();
        return true;
    }

    /**
     * Sets the working set that should be edited.
     * 
     * @param workingSet the working set that should be edited.
     */
    public void setSelection(IWorkingSet workingSet) {
        this.workingSet = workingSet;
        workingSetEditPage.setSelection(workingSet);
    }
}
