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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.AdaptableList;

/**
 * The import wizard allows the user to choose which nested import wizard to
 * run. The set of available wizards comes from the import wizard extension
 * point.
 */
public class ImportWizard extends Wizard {

    //the list selection page
    class SelectionPage extends WorkbenchWizardListSelectionPage {
        SelectionPage(IWorkbench w, IStructuredSelection ss, AdaptableList e,
                String s) {
            super(w, ss, e, s);
        }

        public void createControl(Composite parent) {
            super.createControl(parent);
            WorkbenchHelp.setHelp(getControl(),
                    IHelpContextIds.IMPORT_WIZARD_SELECTION_WIZARD_PAGE);
        }

        public IWizardNode createWizardNode(WorkbenchWizardElement element) {
            return new WorkbenchWizardNode(this, element) {
                public IWorkbenchWizard createWizard() throws CoreException {
                    return (IWorkbenchWizard) wizardElement
                            .createExecutableExtension();
                }
            };
        }
    }

    private IStructuredSelection selection;

    private IWorkbench workbench;

    /**
     * Creates the wizard's pages lazily.
     */
    public void addPages() {
        addPage(new SelectionPage(this.workbench, this.selection,
                getAvailableImportWizards(), WorkbenchMessages
                        .getString("ImportWizard.selectSource"))); //$NON-NLS-1$
    }

    /**
     * Returns the import wizards that are available for invocation.
     */
    protected AdaptableList getAvailableImportWizards() {
        return new WizardsRegistryReader(IWorkbenchConstants.PL_IMPORT)
                .getWizards();
    }

    /**
     * Initializes the wizard.
     */
    public void init(IWorkbench aWorkbench,
            IStructuredSelection currentSelection) {
        this.workbench = aWorkbench;
        this.selection = currentSelection;

        setWindowTitle(WorkbenchMessages.getString("ImportWizard.title")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
        setNeedsProgressMonitor(true);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish() {
        ((SelectionPage) getPages()[0]).saveWidgetValues();
        return true;
    }
}