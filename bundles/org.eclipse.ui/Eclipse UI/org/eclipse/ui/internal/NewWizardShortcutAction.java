package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;


/**
 *	Instances can launch arbitrary resource creation wizards
 *	that have been marked as being available as workbench shortcut
 *	items
 */
public class NewWizardShortcutAction extends Action {
	private WorkbenchWizardElement wizardElement;
	private IWorkbench workbench;
/**
 *	Create an instance of this class.  Use this constructor if you do
 *	not wish to pre-specify the selection that should be provided to
 *	launched shortcut wizards.
 *
 *	@param element WorkbenchWizardElement
 */
public NewWizardShortcutAction(IWorkbench workbench, WorkbenchWizardElement element) {
	super(element.getLabel(element));
	setToolTipText(element.getDescription());
	setImageDescriptor(element.getImageDescriptor());
	setId(IWorkbenchActionConstants.NEW);
	wizardElement = element;
	this.workbench = workbench;
}
/**
 *	This action has been invoked by the user
 *
 *	@param context Window
 */
public void run() {
	// create instance of target wizard

	INewWizard wizard;
	try {
		wizard = (INewWizard)wizardElement.createExecutableExtension();
	} catch (CoreException e) {
		ErrorDialog.openError(
			workbench.getActiveWorkbenchWindow().getShell(),
			WorkbenchMessages.getString("NewWizardShortcutAction.errorTitle"), //$NON-NLS-1$
			WorkbenchMessages.getString("NewWizardShortcutAction.errorMessage"), //$NON-NLS-1$
			e.getStatus());
		return;
	}

	ISelection selection = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
	IStructuredSelection selectionToPass = null;
	if (selection instanceof IStructuredSelection)
		selectionToPass = (IStructuredSelection) selection;
	else
		selectionToPass = StructuredSelection.EMPTY;
		
	if (!wizardElement.canHandleSelection(selectionToPass))
		selectionToPass = StructuredSelection.EMPTY;
		
	wizard.init(workbench, selectionToPass);

	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.open();
}
}
