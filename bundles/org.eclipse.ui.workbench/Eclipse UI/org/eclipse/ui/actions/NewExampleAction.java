/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.NewWizard;
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.ui.internal.registry.NewWizardsRegistryReader;

/**
 * Standard action for launching the create project selection
 * wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewExampleAction extends Action {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	/**
	 * The workbench window this action will run in
	 */
	private IWorkbenchWindow window;

	/**
	 * This default constructor allows the the action to be called from the welcome page.
	 */
	public NewExampleAction() {
		this(
			((Workbench) PlatformUI.getWorkbench()).getActiveWorkbenchWindow());
	}

	/**
	 * Creates a new action for launching the new project
	 * selection wizard.
	 *
	 * @param window the workbench window to query the current
	 * 		selection and shell for opening the wizard.
	 */
	public NewExampleAction(IWorkbenchWindow window) {
		super(WorkbenchMessages.getString("NewExampleAction.text")); //$NON-NLS-1$
		Assert.isNotNull(window);
		this.window = window;
		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		setHoverImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER));
		setDisabledImageDescriptor(
			images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
		setToolTipText(WorkbenchMessages.getString("NewExampleAction.toolTip")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_ACTION);
	}

	/* (non-Javadoc)
	 * Method declared on IAction.
	 */
	public void run() {
		// Create wizard selection wizard.
		IWorkbench workbench = PlatformUI.getWorkbench();
		NewWizard wizard = new NewWizard();
		wizard.setCategoryId(
			NewWizardsRegistryReader.FULL_EXAMPLES_WIZARD_CATEGORY);

		ISelection selection = window.getSelectionService().getSelection();
		IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
		if (selection instanceof IStructuredSelection)
			selectionToPass = (IStructuredSelection) selection;
		wizard.init(workbench, selectionToPass);
		IDialogSettings workbenchSettings =
			WorkbenchPlugin.getDefault().getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings.getSection("NewWizardAction"); //$NON-NLS-1$
		if (wizardSettings == null)
			wizardSettings = workbenchSettings.addNewSection("NewWizardAction"); //$NON-NLS-1$
		wizard.setDialogSettings(wizardSettings);
		wizard.setForcePreviousAndNextButtons(true);

		// Create wizard dialog.
		Shell parent = window.getShell();
		WizardDialog dialog = new WizardDialog(parent, wizard);
		dialog.create();
		wizard.setWindowTitle(WorkbenchMessages.getString("NewExample.title")); //$NON-NLS-1$
		dialog.getShell().setSize(
			Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x),
			SIZING_WIZARD_HEIGHT);
		WorkbenchHelp.setHelp(
			dialog.getShell(),
			IHelpContextIds.NEW_PROJECT_WIZARD);

		// Open wizard.
		dialog.open();
	}
}
