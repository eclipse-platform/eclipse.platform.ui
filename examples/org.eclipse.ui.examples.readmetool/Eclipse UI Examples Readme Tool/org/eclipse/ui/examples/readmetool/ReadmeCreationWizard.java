package org.eclipse.ui.examples.readmetool;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.ui.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;

/**
 * This class implements the interface required by the desktop
 * for all 'New' wizards.  This wizard creates readme files.
 */
public class ReadmeCreationWizard extends Wizard implements INewWizard {
	private IStructuredSelection selection;
	private IWorkbench workbench;
	private ReadmeCreationPage mainPage;
/** (non-Javadoc)
 * Method declared on Wizard.
 */
public void addPages() {
	mainPage = new ReadmeCreationPage(workbench, selection);
	addPage(mainPage);
}
/** (non-Javadoc)
 * Method declared on INewWizard
 */
public void init(IWorkbench workbench,IStructuredSelection selection) {
	this.workbench = workbench;
	this.selection = selection;
	setWindowTitle("New Readme File");
	setDefaultPageImageDescriptor(ReadmeImages.README_WIZARD_BANNER);
}
/** (non-Javadoc)
 * Method declared on IWizard
 */
public boolean performFinish() {
	return mainPage.finish();
}
}
