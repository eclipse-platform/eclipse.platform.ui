/************************************************************************
Copyright (c) 2002, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.IWorkingSetPage;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.WorkingSetDescriptor;
import org.eclipse.ui.internal.registry.WorkingSetRegistry;

/**
 * A new working set wizard allows the user to create a 
 * new working set using a plugin specified working set page.
 * 
 * @since 2.0
 * @see org.eclipse.ui.dialog.IWorkingSetPage
 */
public class WorkingSetNewWizard extends Wizard {
	private WorkingSetTypePage workingSetTypePage;
	private IWorkingSetPage workingSetEditPage;
	private String editPageId;
	private IWorkingSet workingSet;

	/**
	 * Creates a new instance of the receiver.
	 */
	public WorkingSetNewWizard() {
		super();
		setWindowTitle(WorkbenchMessages.getString("WorkingSetNewWizard.title"));	//$NON-NLS-1$
	}
	/**
	 * Overrides method in Wizard.
	 * Adds a page listing the available kinds of working sets.
	 * The second wizard page will depend on the selected working set 
	 * type.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */	
	public void addPages() {
		super.addPages();

		IWizardPage page;
		WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
		WorkingSetDescriptor[] descriptors = registry.getWorkingSetDescriptors();	
		if (descriptors.length > 1) {
			page = workingSetTypePage = new WorkingSetTypePage();
		}
		else {
			editPageId = descriptors[0].getId();
			page = workingSetEditPage = registry.getWorkingSetPage(editPageId);
		}
		page.setWizard(this);
		addPage(page);
		setForcePreviousAndNextButtons(descriptors.length > 1);
	}
	/**
	 * Overrides method in Wizard.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		return (workingSetEditPage != null && workingSetEditPage.isPageComplete());
	}
	/**
	 * Overrides method in Wizard.
	 * Returns a working set page for creating the new working set.
	 * This second page is loaded from the plugin that defined the
	 * selected working set type.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(IWizardPage)
	 */	
	public IWizardPage getNextPage(IWizardPage page) {
		if (workingSetTypePage != null && page == workingSetTypePage) {
			String pageId = workingSetTypePage.getSelection();
			if (pageId != null) {			
				if (workingSetEditPage == null || pageId != editPageId) {
					WorkingSetRegistry registry = WorkbenchPlugin.getDefault().getWorkingSetRegistry();
					workingSetEditPage = registry.getWorkingSetPage(pageId);
					workingSetEditPage.setWizard(this);
					editPageId = pageId;
				}			
				return workingSetEditPage;
			}
		}
		return null;
	}
	/**
	 * Returns the new working set. Returns null if the wizard has 
	 * been cancelled.
	 * 
	 * @return the new working set or null if the wizard has been 
	 * 	cancelled.
	 */
	public IWorkingSet getSelection() {
		return workingSet;
	}
	/**
	 * Overrides method in Wizard.
	 * Stores the newly created working set and the id of the page
	 * used to create it.
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */	
	public boolean performFinish() {
		workingSetEditPage.finish();
		workingSet = workingSetEditPage.getSelection();
		workingSet.setId(editPageId);
		return true;
	}
}