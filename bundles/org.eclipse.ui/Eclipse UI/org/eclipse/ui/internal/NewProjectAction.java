package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IConfigurationElement;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.ui.actions.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Invoke the resource creation wizard selection Wizard.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class NewProjectAction extends Action {

	/**
	 * The wizard dialog width
	 */
	private static final int SIZING_WIZARD_WIDTH = 500;

	/**
	 * The wizard dialog height
	 */
	private static final int SIZING_WIZARD_HEIGHT = 500;

/**
 *	Create a new instance of this class
 */
public NewProjectAction() {
	super("&Project...");
	setImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ));
	setHoverImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_HOVER));
	setDisabledImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_CTOOL_NEW_WIZ_DISABLED));
	setToolTipText("Open the New Project wizard");	
	WorkbenchHelp.setHelp(this, new Object[] {IHelpContextIds.NEW_ACTION});
}
/* (non-Javadoc)
 * Method declared on IAction.
 */
public void run() {
	// Create wizard selection wizard.
	IWorkbench workbench = PlatformUI.getWorkbench();
	NewWizard wizard = new NewWizard();
	wizard.setProjectsOnly(true);
	ISelection selection = workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
	IStructuredSelection selectionToPass = null;
	if (selection instanceof IStructuredSelection)
		selectionToPass = (IStructuredSelection) selection;
	else
		selectionToPass = StructuredSelection.EMPTY;
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("NewWizardAction");
	if(wizardSettings==null)
		wizardSettings = workbenchSettings.addNewSection("NewWizardAction");
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);

	// Create wizard dialog.
	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize(SIZING_WIZARD_WIDTH,SIZING_WIZARD_HEIGHT);
	WorkbenchHelp.setHelp(dialog.getShell(), new Object[]{IHelpContextIds.NEW_WIZARD});

	// Open wizard.
	dialog.open();
}
}
