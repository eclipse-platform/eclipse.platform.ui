package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;

/**
 * The new wizard is responsible for allowing the user to choose which
 * new (nested) wizard to run. The set of available new wizards comes
 * from the new extension point.
 */
public class NewWizard extends Wizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	private NewWizardSelectionPage mainPage;
	private boolean projectsOnly = false;
/**
 * Create the wizard pages
 */
public void addPages() {
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader();
	WizardCollectionElement wizards = null;
	if (projectsOnly)
		wizards = (WizardCollectionElement)rdr.getProjectWizards();
	else
		wizards = (WizardCollectionElement)rdr.getWizards();
	mainPage =
		new NewWizardSelectionPage(
			this.workbench,
			this.selection,
			wizards);
	addPage(mainPage);
}
/**
 *	Lazily create the wizards pages
 */
public void init(IWorkbench aWorkbench, IStructuredSelection currentSelection) {
	this.workbench = aWorkbench;
	this.selection = currentSelection;
	
	setWindowTitle("New");
	setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_NEW_WIZ));
	setNeedsProgressMonitor(true);
}
/**
 *	The user has pressed Finish.  Instruct self's pages to finish, and
 *	answer a boolean indicating success.
 *
 *	@return boolean
 */
public boolean performFinish() {
	//save our selection state
	mainPage.saveWidgetValues();
	return true;
}
/**
 * Sets the projects only flag.  If <code>true</code> only projects will
 * be shown in this wizard.
 */
public void setProjectsOnly(boolean b) {
	projectsOnly = b;
}
}
