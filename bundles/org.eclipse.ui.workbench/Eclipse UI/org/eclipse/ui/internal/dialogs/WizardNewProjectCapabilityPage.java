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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.ProjectCapabilitySelectionGroup;
import org.eclipse.ui.internal.registry.*;

/**
 * Second page for the new project creation wizard. This page
 * collects the capabilities of the new project.
 * <p>
 * Example useage:
 * <pre>
 * mainPage = new WizardNewProjectCapabilityPage("wizardNewProjectCapabilityPage");
 * mainPage.setTitle("Project");
 * mainPage.setDescription("Choose project's capabilities.");
 * </pre>
 * </p>
 */
public class WizardNewProjectCapabilityPage extends WizardPage {
	// initial value stores
	private Capability[] initialProjectCapabilities;
	private ICategory[] initialSelectedCategories;

	// widgets
	private ProjectCapabilitySelectionGroup capabilityGroup;
	
	/**
	 * Creates a new project capabilities wizard page.
	 *
	 * @param pageName the name of this page
	 */
	public WizardNewProjectCapabilityPage(String pageName) {
		super(pageName);
	}

	/* (non-Javadoc)
	 * Method declared on IWizardPage
	 */
	public boolean canFlipToNextPage() {
		// Already know there is a next page...
		return isPageComplete();
	}

	/* (non-Javadoc)
	 * Method declared on IDialogPage.
	 */
	public void createControl(Composite parent) {
		WorkbenchHelp.setHelp(parent, IHelpContextIds.NEW_PROJECT_CAPABILITY_WIZARD_PAGE);
		CapabilityRegistry reg = WorkbenchPlugin.getDefault().getCapabilityRegistry();
		capabilityGroup = new ProjectCapabilitySelectionGroup(initialSelectedCategories, initialProjectCapabilities, reg);
		setControl(capabilityGroup.createContents(parent));
		
		capabilityGroup.setCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				getWizard().getContainer().updateButtons();
			}
		});
		
		if (!reg.hasCapabilities())
			setMessage(WorkbenchMessages.getString("WizardNewProjectCapabilityPage.noCapabilities"), WARNING); //$NON-NLS-1$
	}

	/**
	 * Returns the collection of capabilities selected
	 * by the user. The collection is not in prerequisite
	 * order.
	 * 
	 * @return array of selected capabilities
	 */
	/* package */ Capability[] getSelectedCapabilities() {
		return capabilityGroup.getSelectedCapabilities();
	}
	
	/**
	 * Sets the initial categories to be selected.
	 * 
	 * @param categories initial categories to select
	 */
	/* package */ void setInitialSelectedCategories(ICategory[] categories) {
		initialSelectedCategories = categories;
	}
	
	/**
	 * Sets the initial project capabilities to be selected.
	 * 
	 * @param capabilities initial project capabilities to select
	 */
	/* package */ void setInitialProjectCapabilities(Capability[] capabilities) {
		initialProjectCapabilities = capabilities;
	}
}
