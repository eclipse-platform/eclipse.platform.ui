package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.internal.registry.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.*;

/**
 * The export wizard allows the user to choose which nested export wizard to run.
 * The set of available wizards comes from the export wizard extension point.
 */
public class ExportWizard extends Wizard {
	private IWorkbench workbench;
	private IStructuredSelection selection;
	

	//the list selection page
	class SelectionPage extends WorkbenchWizardListSelectionPage {
		SelectionPage(IWorkbench w, IStructuredSelection ss, AdaptableList e, String s) {
			super(w, ss, e, s);
		}
		protected IWizardNode createWizardNode(WorkbenchWizardElement element) {
			return new WorkbenchWizardNode(this, element) {
				public IWorkbenchWizard createWizard() throws CoreException {
					return (IWorkbenchWizard)wizardElement.createExecutableExtension();
				}
			};
		}
	}
/**
 * Creates the wizard's pages lazily.
 */
public void addPages() {
	addPage(
		new SelectionPage(
			this.workbench, 
			this.selection, 
			getAvailableExportWizards(), 
			"Select export destination:")); 
}
/**
 * Returns the export wizards that are available for invocation.
 */
protected AdaptableList getAvailableExportWizards() {
	return new WizardsRegistryReader(IWorkbenchConstants.PL_EXPORT).getWizards();
}
/**
 * Initializes the wizard.
 */
public void init(IWorkbench aWorkbench,IStructuredSelection currentSelection) {
	this.workbench = aWorkbench;
	this.selection = currentSelection;
	
	setWindowTitle("Export");
	setDefaultPageImageDescriptor(WorkbenchImages.getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
	setNeedsProgressMonitor(true);
}
/**
 * Subclasses must implement this <code>IWizard</code> method 
 * to perform any special finish processing for their wizard.
 */
public boolean performFinish() {
	((SelectionPage)getPages()[0]).saveWidgetValues();
	return true;
}
}
