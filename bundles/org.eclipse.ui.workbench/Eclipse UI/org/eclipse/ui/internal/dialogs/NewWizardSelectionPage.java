package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IDecisionPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
/**
 *	New wizard selection tab that allows the user to either select a
 *	registered 'New' wizard to be launched, or to select a solution or
 *	projects to be retrieved from an available server.  This page
 *	contains two visual tabs that allow the user to perform these tasks.
 */
class NewWizardSelectionPage extends WorkbenchWizardSelectionPage implements IDecisionPage{
	private WizardCollectionElement	wizardCategories;
	
	// widgets
	private NewWizardNewPage		newResourcePage;
/**
 *	Create an instance of this class
 */
public NewWizardSelectionPage(IWorkbench aWorkbench , IStructuredSelection currentSelection, WizardCollectionElement elements) {
	// override what superclass does with elements
	super("newWizardSelectionPage", aWorkbench, currentSelection, null);//$NON-NLS-1$
	setDescription(WorkbenchMessages.getString("NewWizardSelectionPage.description")); //$NON-NLS-1$
	wizardCategories = elements;	
}
/**
 * Makes the next page visible.
 */
public void advanceToNextPage() {
	getContainer().showPage(getNextPage());
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	IDialogSettings settings = getDialogSettings();
	newResourcePage = new NewWizardNewPage(this, this.workbench, wizardCategories);
	newResourcePage.setDialogSettings(settings);

	Control control = newResourcePage.createControl(parent);
	WorkbenchHelp.setHelp(control, IHelpContextIds.NEW_WIZARD_SELECTION_WIZARD_PAGE);
	setControl(control);
}
/**
 * Since Finish was pressed, write widget values to the dialog store so that they
 *will persist into the next invocation of this wizard page
 */
protected void saveWidgetValues() {
	newResourcePage.saveWidgetValues();
}
}
