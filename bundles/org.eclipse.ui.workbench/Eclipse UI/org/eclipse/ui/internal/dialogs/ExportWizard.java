/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Alain Bernard <alain.bernard1224@gmail.com> - Bug 281490
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.internal.registry.WizardsRegistryReader;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.wizards.IWizardCategory;

/**
 * The export wizard allows the user to choose which nested export wizard to
 * run. The set of available wizards comes from the export wizard extension
 * point.
 */
public class ExportWizard extends Wizard {
	private IWorkbench theWorkbench;

	private IStructuredSelection selection;

	// the list selection page
	static class SelectionPage extends WorkbenchWizardListSelectionPage {
		SelectionPage(IWorkbench w, IStructuredSelection ss, AdaptableList e, String s) {
			super(w, ss, e, s, WorkbenchTriggerPoints.EXPORT_WIZARDS);
		}

		@Override
		public void createControl(Composite parent) {
			super.createControl(parent);
			workbench.getHelpSystem().setHelp(getControl(),
					IWorkbenchHelpContextIds.EXPORT_WIZARD_SELECTION_WIZARD_PAGE);
		}

		@Override
		protected IWizardNode createWizardNode(WorkbenchWizardElement element) {
			return new WorkbenchWizardNode(this, element) {
				@Override
				public IWorkbenchWizard createWizard() throws CoreException {
					return wizardElement.createWizard();
				}
			};
		}
	}

	/**
	 * Creates the wizard's pages lazily.
	 */
	@Override
	public void addPages() {
		addPage(new SelectionPage(this.theWorkbench, this.selection, getAvailableExportWizards(),
				WorkbenchMessages.ExportWizard_selectWizard));
	}

	/**
	 * Returns the export wizards that are available for invocation.
	 */
	protected AdaptableList getAvailableExportWizards() {
		// TODO: exports are still flat - we need to get at the flat list. All
		// wizards will be in the "other" category.
		IWizardCategory root = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
		WizardCollectionElement otherCategory = (WizardCollectionElement) root
				.findCategory(IPath.fromOSString(WizardsRegistryReader.UNCATEGORIZED_WIZARD_CATEGORY));
		if (otherCategory == null) {
			return new AdaptableList();
		}
		return otherCategory.getWizardAdaptableList();
	}

	/**
	 * Initializes the wizard.
	 *
	 * @param aWorkbench       the workbench
	 * @param currentSelection the current selectio
	 */
	public void init(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
		this.theWorkbench = aWorkbench;
		this.selection = currentSelection;

		setWindowTitle(WorkbenchMessages.ExportWizard_title);
		setDefaultPageImageDescriptor(
				WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
		setNeedsProgressMonitor(true);
	}

	/**
	 * Subclasses must implement this <code>IWizard</code> method to perform any
	 * special finish processing for their wizard.
	 */
	@Override
	public boolean performFinish() {
		((SelectionPage) getPages()[0]).saveWidgetValues();
		return true;
	}
}
