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
import org.eclipse.ui.internal.dialogs.ExportWizard;

/**
 * Action representing the initiation of an Export operation by the user.
 * <p>
 * This class may be instantiated. It is not intended to be subclassed.
 * </p>
 * @since 2.0
 */
public class ExportResourcesAction extends SelectionListenerAction {
	private static final int SIZING_WIZARD_WIDTH = 470;
	private static final int SIZING_WIZARD_HEIGHT = 550;
	private IWorkbenchWindow window;
	
	/**
	 * Create a new instance of this class
	 */
	public ExportResourcesAction(IWorkbenchWindow window) {
		this(window, WorkbenchMessages.getString("ExportResourcesAction.text")); //$NON-NLS-1$
	}

	/**
	 * Create a new instance of this class
	 */
	public ExportResourcesAction(IWorkbenchWindow window, String label) {
		super(label); //$NON-NLS-1$
		setToolTipText(WorkbenchMessages.getString("ExportResourcesAction.toolTip")); //$NON-NLS-1$
		setId(IWorkbenchActionConstants.EXPORT);
		WorkbenchHelp.setHelp(this, IHelpContextIds.EXPORT_ACTION);
		this.window = window;
	}

	/**
	 * Create a new instance of this class
	 * 
	 * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow)</code>
	 */
	public ExportResourcesAction(IWorkbench workbench) {
		this(workbench.getActiveWorkbenchWindow());
	}

	/**
	 * Create a new instance of this class
	 *
	 * @deprecated use the constructor <code>ExportResourcesAction(IWorkbenchWindow, String)</code>
	 */
	public ExportResourcesAction(IWorkbench workbench, String label) {
		this(workbench.getActiveWorkbenchWindow(), label);
	}

	/**
	 * Invoke the Export wizards selection Wizard.
	 *
	 * @param browser Window
	 */
	public void run() {
		ExportWizard wizard = new ExportWizard();
		IStructuredSelection selectionToPass;
		List selectedResources = getSelectedResources();

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
		IDialogSettings wizardSettings = workbenchSettings.getSection("ExportResourcesAction"); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection("ExportResourcesAction"); //$NON-NLS-1$
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		Shell parent = window.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		dialog.getShell().setSize(Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT);
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.EXPORT_WIZARD);
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
