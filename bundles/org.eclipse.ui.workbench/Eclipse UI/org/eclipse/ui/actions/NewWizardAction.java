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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.NewWizard;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewWizardAction extends Action {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

	/**
	 * The id of the category to show or <code>null</code> to
	 * show all the categories.
	 */
	private String categoryId = null;
	
/**
 *	Create a new instance of this class
 */
public NewWizardAction() {
	super(WorkbenchMessages.getString("NewWizardAction.text")); //$NON-NLS-1$
	ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
	setImageDescriptor(
		images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
	setHoverImageDescriptor(
		images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_HOVER));
	setDisabledImageDescriptor(
		images.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
	setToolTipText(WorkbenchMessages.getString("NewWizardAction.toolTip"));	 //$NON-NLS-1$
	WorkbenchHelp.setHelp(this, IHelpContextIds.NEW_ACTION);
	setActionDefinitionId("org.eclipse.ui.newWizard"); //$NON-NLS-1$
}
/**
 * Returns the id of the category of wizards to show
 * or <code>null</code> to show all categories.
 */
public String getCategoryId() {
	return categoryId;
}
/**
 * Sets the id of the category of wizards to show
 * or <code>null</code> to show all categories.
 */
public void setCategoryId(String id) {
	categoryId = id;
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	IWorkbench workbench = PlatformUI.getWorkbench();
	NewWizard wizard = new NewWizard();
	wizard.setCategoryId(categoryId);

	ISelection selection = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
	IStructuredSelection selectionToPass = StructuredSelection.EMPTY;
	if (selection instanceof IStructuredSelection) {
		selectionToPass = (IStructuredSelection) selection;
	} else {
		// Build the selection from the IFile of the editor
		IWorkbenchPart part = workbench.getActiveWorkbenchWindow().getPartService().getActivePart();
		if (part instanceof IEditorPart) {
			IEditorInput input = ((IEditorPart)part).getEditorInput();
			if (input instanceof IFileEditorInput) {
				selectionToPass = new StructuredSelection(((IFileEditorInput)input).getFile());
			}	
		}
	}
	
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("NewWizardAction");//$NON-NLS-1$
	if (wizardSettings == null)
		wizardSettings = workbenchSettings.addNewSection("NewWizardAction");//$NON-NLS-1$
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);
	
	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.NEW_WIZARD);
	dialog.open();
}
}
