package org.eclipse.ui.internal.dialogs;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.*;
import org.eclipse.ui.internal.dialogs.*;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.misc.*;
import org.eclipse.jface.*;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import org.eclipse.ui.internal.misc.UIHackFinder;
/**
 *	New wizard selection tab that allows the user to either select a
 *	registered 'New' wizard to be launched, or to select a solution or
 *	projects to be retrieved from an available server.  This page
 *	contains two visual tabs that allow the user to perform these tasks.
 */
class NewWizardSelectionPage extends WorkbenchWizardSelectionPage {
	private WizardCollectionElement	wizardCategories;
	
	// widgets
	private NewWizardNewPage		newResourcePage;
/**
 *	Create an instance of this class
 */
public NewWizardSelectionPage(IWorkbench aWorkbench , IStructuredSelection currentSelection, WizardCollectionElement elements) {
	// override what superclass does with elements
	super("newWizardSelectionPage", aWorkbench, currentSelection, null);
	setDescription("Select a wizard");
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
	setControl(newResourcePage.createControl(parent));
}
/**
 * Since Finish was pressed, write widget values to the dialog store so that they
 *will persist into the next invocation of this wizard page
 */
protected void saveWidgetValues() {
	newResourcePage.saveWidgetValues();
}
}
