package org.eclipse.ui.actions;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.ArrayList;
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
import org.eclipse.ui.internal.dialogs.ImportWizard;
import org.eclipse.ui.views.navigator.ResourceSelectionUtil;

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
	private IWorkbench workbench;
	private IStructuredSelection selection;
/**
 *	Create a new instance of this class
 */
public ImportResourcesAction(IWorkbench aWorkbench) {
	super(WorkbenchMessages.getString("ImportResourcesAction.text")); //$NON-NLS-1$
	setToolTipText(WorkbenchMessages.getString("ImportResourcesAction.toolTip")); //$NON-NLS-1$
	setId(IWorkbenchActionConstants.IMPORT);
	WorkbenchHelp.setHelp(this, IHelpContextIds.IMPORT_ACTION);
	this.workbench = aWorkbench;
}
/*
 * @see SelectionListenerAction.updateSelection()
 */
protected boolean updateSelection(IStructuredSelection selection) {
	
	if(!super.updateSelection(selection))
		return false;
	
	if (selection.size() != 1) {
		return selection.size() == 0;
	} 
	
	Iterator iterator = getSelectedResources().iterator();
	boolean hasFile = false;
	List projects = new ArrayList();
	
	while(iterator.hasNext()){
		IResource resource = (IResource) iterator.next();
		if(resource.getType() == IResource.PROJECT){
			projects.add(resource);
			break;
		}
		
		if(resource.getType() == IResource.FOLDER)
			break;
		hasFile = true;
	}
	
	if(hasFile)
		return false;
	 
	if (projects.isEmpty()) {
		return false;
	}

	IProject project = (IProject)projects.get(0);
	return(project.isOpen());
}


/**
 * Invoke the Import wizards selection Wizard.
 *
 * @param browser Window
 */
public void run() {
	ImportWizard wizard = new ImportWizard();
	IStructuredSelection selectionToPass = selection;
	if (selectionToPass == null) {
		// get the current workbench selection
		ISelection workbenchSelection = 
			workbench.getActiveWorkbenchWindow().getSelectionService().getSelection();
		if (workbenchSelection instanceof IStructuredSelection)
			selectionToPass = (IStructuredSelection)workbenchSelection;
		else
			selectionToPass = StructuredSelection.EMPTY;
	}
	wizard.init(workbench, selectionToPass);
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings wizardSettings = workbenchSettings.getSection("ImportResourcesAction");//$NON-NLS-1$
	if(wizardSettings==null)
		wizardSettings = workbenchSettings.addNewSection("ImportResourcesAction");//$NON-NLS-1$
	wizard.setDialogSettings(wizardSettings);
	wizard.setForcePreviousAndNextButtons(true);
	
	Shell parent = workbench.getActiveWorkbenchWindow().getShell();
	WizardDialog dialog = new WizardDialog(parent, wizard);
	dialog.create();
	dialog.getShell().setSize( Math.max(SIZING_WIZARD_WIDTH, dialog.getShell().getSize().x), SIZING_WIZARD_HEIGHT );
	WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.IMPORT_WIZARD);
	dialog.open();
}
}
