package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.help.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.widgets.Shell;
import java.util.*;

/**
 * Action representing the initiation of an Export operation by the user.
 */
public class ExportResourcesAction extends Action {
	private static final int SIZING_WIZARD_WIDTH = 470;
	private static final int SIZING_WIZARD_HEIGHT = 550;
	private IWorkbench workbench;
/**
 *	Create a new instance of this class
 */
public ExportResourcesAction(IWorkbench aWorkbench) {
	super(WorkbenchMessages.getString("ExportResourcesAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ExportResourcesAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.EXPORT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.EXPORT_ACTION);
	this.workbench = aWorkbench;
}
/**
 * Invoke the Export wizards selection Wizard.
 *
 * @param browser Window
 */
public void run() {
	ExportWizard wizard = new ExportWizard();
	ISelection selection = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
	IStructuredSelection selectionToPass = null;
	if (selection instanceof IStructuredSelection)
		selectionToPass = (IStructuredSelection) selection;
	else
		selectionToPass = StructuredSelection.EMPTY;
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("ExportResourcesAction");//$NON-NLS-1$
	if(wizardSettings==null)
		wizardSettings = workbenchSettings.addNewSection("ExportResourcesAction");//$NON-NLS-1$
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);

	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.EXPORT_WIZARD);
	dialog.open();
}
}
