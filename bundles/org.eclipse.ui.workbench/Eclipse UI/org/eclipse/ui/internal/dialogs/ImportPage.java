/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.ITriggerPoint;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.activities.ws.WorkbenchTriggerPoints;
import org.eclipse.ui.wizards.IWizardCategory;

/**
 * Wizard page class from which an import wizard is selected.
 *
 * @since 3.2
 */
public class ImportPage extends ImportExportPage {
	private static final String STORE_SELECTED_IMPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
			+ "STORE_SELECTED_IMPORT_WIZARD_ID"; //$NON-NLS-1$

	private static final String STORE_EXPANDED_IMPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
			+ "STORE_EXPANDED_IMPORT_CATEGORIES"; //$NON-NLS-1$

	protected CategorizedWizardSelectionTree importTree;

	/**
	 * Constructor for import wizard selection page.
	 */
	public ImportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
		super(aWorkbench, currentSelection);
	}

	@Override
	protected void initialize() {
		workbench.getHelpSystem().setHelp(getControl(), IWorkbenchHelpContextIds.IMPORT_WIZARD_SELECTION_WIZARD_PAGE);
	}

	@Override
	protected Composite createTreeViewer(Composite parent) {
		IWizardCategory root = WorkbenchPlugin.getDefault().getImportWizardRegistry().getRootCategory();
		importTree = new CategorizedWizardSelectionTree(root, WorkbenchMessages.ImportWizard_selectWizard);
		Composite importComp = importTree.createControl(parent);
		importTree.getViewer().addSelectionChangedListener(event -> listSelectionChanged(event.getSelection()));
		importTree.getViewer().addDoubleClickListener(this::treeDoubleClicked);
		setTreeViewer(importTree.getViewer());
		return importComp;
	}

	@Override
	public void saveWidgetValues() {
		storeExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importTree.getViewer());
		storeSelectedCategoryAndWizard(STORE_SELECTED_IMPORT_WIZARD_ID, importTree.getViewer());
		super.saveWidgetValues();
	}

	@Override
	protected void restoreWidgetValues() {
		IWizardCategory importRoot = WorkbenchPlugin.getDefault().getImportWizardRegistry().getRootCategory();
		expandPreviouslyExpandedCategories(STORE_EXPANDED_IMPORT_CATEGORIES, importRoot, importTree.getViewer());
		selectPreviouslySelected(STORE_SELECTED_IMPORT_WIZARD_ID, importRoot, importTree.getViewer());
		super.restoreWidgetValues();
	}

	@Override
	protected ITriggerPoint getTriggerPoint() {
		return getWorkbench().getActivitySupport().getTriggerPointManager()
				.getTriggerPoint(WorkbenchTriggerPoints.IMPORT_WIZARDS);
	}

	@Override
	protected void updateMessage() {
		setMessage(WorkbenchMessages.ImportExportPage_chooseImportWizard);
		super.updateMessage();
	}
}
