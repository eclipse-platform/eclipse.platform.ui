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
package org.eclipse.ui.actions;


import java.util.List;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.ImportWizard;

/**
 * Action representing the initiation of an Import operation by the user.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class ImportResourcesAction extends SelectionListenerAction {
	private static final int SIZING_WIZARD_WIDTH = 470;
	private static final int SIZING_WIZARD_HEIGHT = 550;
	private IWorkbenchWindow window;
	
	/**
	 * Create a new instance of this class
	 */
	public ImportResourcesAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("ImportResourcesAction.text")); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ImportResourcesAction.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.IMPORT);
		WorkbenchHelp.setHelp(this, IHelpContextIds.IMPORT_ACTION);
		this.window = window;
	}

	/**
	 * Create a new instance of this class
	 * 
	 * @deprecated use the constructor <code>ImportResourcesAction(IWorkbenchWindow)</code>
	 */
	public ImportResourcesAction(IWorkbench workbench) {
		this(workbench.getActiveWorkbenchWindow());
	}

	/**
	 * Invoke the Import wizards selection Wizard.
	 *
	 * @param browser Window
	 */
	public void run() {
		ImportWizard wizard = new ImportWizard();
		List selectedResources = getSelectedResources();
		IStructuredSelection selectionToPass;

		if (selectedResources.isEmpty()) {
			// get the current workbench selection
			ISelection workbenchSelection = window.getSelectionService().getSelection();
			if (workbenchSelection instanceof IStructuredSelection)
				selectionToPass = (IStructuredSelection) workbenchSelection;
			else
				selectionToPass = StructuredSelection.EMPTY;
		} else
			selectionToPass = new StructuredSelection(selectedResources);

		wizard.init(window.getWorkbench(), selectionToPass);
		IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("ImportResourcesAction"); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection("ImportResourcesAction"); //$NON-NLS-1$
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		Shell parent = window.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.IMPORT_WIZARD);
		dialog.open();
	}

	/**
	 * Sets the current selection. 
	 * In for backwards compatability. Use selectionChanged() instead.
	 * @param selection the new selection
	 * @deprecated
	 */
	public void setSelection(IStructuredSelection selection) {
		selectionChanged(selection);
	}
}
