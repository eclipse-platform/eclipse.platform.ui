package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchActionConstants;
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
	private IWorkbench workbench;
/**
 *	Create a new instance of this class
 */
public ExportResourcesAction(IWorkbench aWorkbench) {
	this(aWorkbench,WorkbenchMessages.getString("ExportResourcesAction.text")); //$NON-NLS-1$
}

/**
 *	Create a new instance of this class
 */
public ExportResourcesAction(IWorkbench aWorkbench, String label) {
	super(label); //$NON-NLS-1$
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
	IStructuredSelection selectionToPass; 
	List selectedResources = getSelectedResources();
	
	if (selectedResources.isEmpty()) {
		// get the current workbench selection
		ISelection workbenchSelection = 
			workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (workbenchSelection instanceof IStructuredSelection)
			selectionToPass = (IStructuredSelection)workbenchSelection;
		else
			selectionToPass = StructuredSelection.EMPTY;
	}
	else
		selectionToPass = new StructuredSelection(selectedResources);
		
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
