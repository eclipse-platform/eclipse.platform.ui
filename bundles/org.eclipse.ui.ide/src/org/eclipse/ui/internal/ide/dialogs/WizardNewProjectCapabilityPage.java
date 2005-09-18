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
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.Category;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.ProjectCapabilitySelectionGroup;
import org.eclipse.ui.internal.ide.registry.Capability;
import org.eclipse.ui.internal.ide.registry.CapabilityRegistry;

/**
 * Second page for the new project creation wizard. This page
 * collects the capabilities of the new project.
 * <p>
 * Example usage:
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

    private Category[] initialSelectedCategories;

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
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
                IIDEHelpContextIds.NEW_PROJECT_CAPABILITY_WIZARD_PAGE);
        CapabilityRegistry reg = IDEWorkbenchPlugin.getDefault()
                .getCapabilityRegistry();
        capabilityGroup = new ProjectCapabilitySelectionGroup(
                initialSelectedCategories, initialProjectCapabilities, reg);
        setControl(capabilityGroup.createContents(parent));

        capabilityGroup.setCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                getWizard().getContainer().updateButtons();
            }
        });

        if (!reg.hasCapabilities())
            setMessage(
                    IDEWorkbenchMessages.WizardNewProjectCapabilityPage_noCapabilities, WARNING);
    }

    /**
     * Returns the collection of capabilities selected
     * by the user. The collection is not in prerequisite
     * order.
     * 
     * @return array of selected capabilities
     */
    /* package */Capability[] getSelectedCapabilities() {
        return capabilityGroup.getSelectedCapabilities();
    }

    /**
     * Sets the initial categories to be selected.
     * 
     * @param categories initial categories to select
     */
    /* package */void setInitialSelectedCategories(Category[] categories) {
        initialSelectedCategories = categories;
    }

    /**
     * Sets the initial project capabilities to be selected.
     * 
     * @param capabilities initial project capabilities to select
     */
    /* package */void setInitialProjectCapabilities(Capability[] capabilities) {
        initialProjectCapabilities = capabilities;
    }
}
