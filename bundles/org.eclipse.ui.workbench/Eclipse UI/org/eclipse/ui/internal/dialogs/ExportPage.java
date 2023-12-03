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
 * Wizard page class from which an export wizard is selected.
 *
 * @since 3.2
 */
public class ExportPage extends ImportExportPage {
	private static final String STORE_SELECTED_EXPORT_WIZARD_ID = DIALOG_SETTING_SECTION_NAME
			+ "STORE_SELECTED_EXPORT_WIZARD_ID"; //$NON-NLS-1$

	private static final String STORE_EXPANDED_EXPORT_CATEGORIES = DIALOG_SETTING_SECTION_NAME
			+ "STORE_EXPANDED_EXPORT_CATEGORIES"; //$NON-NLS-1$

	CategorizedWizardSelectionTree exportTree;

	/**
	 * Constructor for export wizard selection page.
	 */
	public ExportPage(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
		super(aWorkbench, currentSelection);
	}

	@Override
	protected void initialize() {
		workbench.getHelpSystem().setHelp(getControl(), IWorkbenchHelpContextIds.EXPORT_WIZARD_SELECTION_WIZARD_PAGE);
	}

	@Override
	protected Composite createTreeViewer(Composite parent) {
		IWizardCategory root = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
		exportTree = new CategorizedWizardSelectionTree(root, WorkbenchMessages.ExportWizard_selectWizard);
		Composite exportComp = exportTree.createControl(parent);
		exportTree.getViewer().addSelectionChangedListener(event -> listSelectionChanged(event.getSelection()));
		exportTree.getViewer().addDoubleClickListener(this::treeDoubleClicked);
		setTreeViewer(exportTree.getViewer());
		return exportComp;
	}

	@Override
	public void saveWidgetValues() {
		storeExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportTree.getViewer());
		storeSelectedCategoryAndWizard(STORE_SELECTED_EXPORT_WIZARD_ID, exportTree.getViewer());
		super.saveWidgetValues();
	}

	@Override
	protected void restoreWidgetValues() {
		IWizardCategory exportRoot = WorkbenchPlugin.getDefault().getExportWizardRegistry().getRootCategory();
		expandPreviouslyExpandedCategories(STORE_EXPANDED_EXPORT_CATEGORIES, exportRoot, exportTree.getViewer());
		selectPreviouslySelected(STORE_SELECTED_EXPORT_WIZARD_ID, exportRoot, exportTree.getViewer());
		super.restoreWidgetValues();
	}

	@Override
	protected ITriggerPoint getTriggerPoint() {
		return getWorkbench().getActivitySupport().getTriggerPointManager()
				.getTriggerPoint(WorkbenchTriggerPoints.EXPORT_WIZARDS);
	}

	@Override
	protected void updateMessage() {
		setMessage(WorkbenchMessages.ImportExportPage_chooseExportWizard);
		super.updateMessage();
	}
}
