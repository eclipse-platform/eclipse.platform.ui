package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
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
	NewWizardsRegistryReader rdr = new NewWizardsRegistryReader(projectsOnly);
	WizardCollectionElement wizards = (WizardCollectionElement)rdr.getWizards();
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

	if (projectsOnly) 
		setWindowTitle(WorkbenchMessages.getString("NewProject.title")); //$NON-NLS-1$
	else 	
		setWindowTitle(WorkbenchMessages.getString("NewWizard.title")); //$NON-NLS-1$
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
