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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;

/**
 * The export wizard allows the user to choose which nested export wizard to run.
 * The set of available wizards comes from the export wizard extension point.
 */
public class ExportWizard extends Wizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	

	//the list selection page
	class SelectionPage extends WorkbenchWizardListSelectionPage {
		SelectionPage(IWorkbench w, IStructuredSelection ss, AdaptableList e, String s) {
			super(w, ss, e, s);
		}
		public void createControl(Composite parent) {
			super.createControl(parent);
			WorkbenchHelp.setHelp(getControl(), IHelpContextIds.EXPORT_WIZARD_SELECTION_WIZARD_PAGE);
		}
		protected IWizardNode createWizardNode(WorkbenchWizardElement element) {
			return new WorkbenchWizardNode(this, element) {
				public IWorkbenchWizard createWizard() throws CoreException {
					return (IWorkbenchWizard)wizardElement.createExecutableExtension();
				}
			};
		}
	}
/**
 * Creates the wizard's pages lazily.
 */
public void addPages() {
	addPage(
		new SelectionPage(
			this.workbench, 
			this.selection, 
			getAvailableExportWizards(), 
			WorkbenchMessages.getString("ExportWizard.selectDestination")));  //$NON-NLS-1$
}
/**
 * Returns the export wizards that are available for invocation.
 */
protected AdaptableList getAvailableExportWizards() {
	return new WizardsRegistryReader(IWorkbenchConstants.PL_EXPORT).getWizards();
}
/**
 * Initializes the wizard.
 */
public void init(IWorkbench aWorkbench,IStructuredSelection currentSelection) {
	this.workbench = aWorkbench;
	this.selection = currentSelection;
	
	setWindowTitle(WorkbenchMessages.getString("ExportWizard.title")); //$NON-NLS-1$
	setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
	setNeedsProgressMonitor(true);
}
/**
 * Subclasses must implement this <code>IWizard</code> method 
 * to perform any special finish processing for their wizard.
 */
public boolean performFinish() {
	((SelectionPage)getPages()[0]).saveWidgetValues();
	return true;
}
}
