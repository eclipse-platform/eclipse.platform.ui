/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.model.AdaptableList;
import org.eclipse.ui.model.*;

/**
 * Abstract implementation of a wizard selection page which simply displays
 * a list of specified wizard elements and allows the user to select one to
 * be launched.  Subclasses just need to provide a method which creates an
 * appropriate wizard node based upon a user selection.
 */
public abstract class WorkbenchWizardListSelectionPage extends WorkbenchWizardSelectionPage 
	implements ISelectionChangedListener, IDoubleClickListener {
	private String message;

	// id constants
	private static final String STORE_SELECTED_WIZARD_ID = "WizardListSelectionPage.STORE_SELECTED_WIZARD_ID";//$NON-NLS-1$
	private final static int SIZING_LISTS_HEIGHT = 200;
/**
 * Creates a <code>WorkbenchWizardListSelectionPage</code>.
 *
 * @param aWorkbench the current workbench
 * @param currentSelection the workbench's current resource selection
 * @param wizardElements the collection of <code>WorkbenchWizardElements</code> to display for selection
 * @param message the message to display above the selection list
 */
protected WorkbenchWizardListSelectionPage(IWorkbench aWorkbench, IStructuredSelection currentSelection, AdaptableList wizardElements, String message) {
	super("singleWizardSelectionPage", aWorkbench, currentSelection, wizardElements);//$NON-NLS-1$
	setDescription(WorkbenchMessages.getString("WizardList.description")); //$NON-NLS-1$
	this.message = message;
}
/** (non-Javadoc)
 * Method declared on IDialogPage.
 */
public void createControl(Composite parent) {
	
	Font font = parent.getFont();

	// create composite for page.
	Composite outerContainer = new Composite(parent, SWT.NONE);
	outerContainer.setLayout(new GridLayout());
	outerContainer.setLayoutData(new GridData(
		GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
	outerContainer.setFont(font);

	Label messageLabel = new Label(outerContainer,SWT.NONE);
	messageLabel.setText(message);
	messageLabel.setFont(font);

	//Create a table for the list
	Table table = new Table(outerContainer, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_BOTH);
	
	int availableRows = DialogUtil.availableRows(parent);
	
	//Only give a height hint if the dialog is going to be too small
	if(availableRows > 50){
		data.heightHint = SIZING_LISTS_HEIGHT;
	}
	else{
		data.heightHint = availableRows * 3;
	}
		
	table.setLayoutData(data);
	table.setFont(font);

	// the list viewer		
	wizardSelectionViewer = new TableViewer(table);
	wizardSelectionViewer.setContentProvider(new WorkbenchContentProvider());
	wizardSelectionViewer.setLabelProvider(new WorkbenchLabelProvider());
	wizardSelectionViewer.setSorter(new WorkbenchViewerSorter());
	wizardSelectionViewer.addSelectionChangedListener(this);
	wizardSelectionViewer.addDoubleClickListener(this);

	wizardSelectionViewer.setInput(wizardElements);
	restoreWidgetValues();
		
	setControl(outerContainer);
}
/**
 * Returns an <code>IWizardNode</code> representing the specified workbench wizard
 * which has been selected by the user.  <b>Subclasses</b> must override this
 * abstract implementation.
 *
 * @param element the wizard element that an <code>IWizardNode</code> is needed for
 * @return org.eclipse.jface.wizards.IWizardNode
 */
protected abstract IWizardNode createWizardNode(WorkbenchWizardElement element);
/**
 * An item in a viewer has been double-clicked.
 */
public void doubleClick(DoubleClickEvent event) {
	selectionChanged(
		new SelectionChangedEvent(
			wizardSelectionViewer,
			wizardSelectionViewer.getSelection()));
	getContainer().showPage(getNextPage());
}
/**
 * Uses the dialog store to restore widget values to the values that they held
 * last time this wizard was used to completion.
 */
private void restoreWidgetValues() {
	// reselect previous wizard
	String wizardId = getDialogSettings().get(STORE_SELECTED_WIZARD_ID);
	WorkbenchWizardElement wizard = findWizard(wizardId);
	if (wizard == null)
		return;				// wizard no longer exists, or has moved

	StructuredSelection selection = new StructuredSelection(wizard);
	wizardSelectionViewer.setSelection(selection);
	selectionChanged(new SelectionChangedEvent(wizardSelectionViewer,selection));
}
/**
 * Since Finish was pressed, write widget values to the dialog store so that they
 * will persist into the next invocation of this wizard page
 */
public void saveWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	
	// since the user is able to leave this page then exactly one wizard
	// must be currently selected
	IStructuredSelection sel = (IStructuredSelection)wizardSelectionViewer.getSelection();
	// We are losing the selection going back
	if (sel.size() > 0) {
		WorkbenchWizardElement selectedWizard = (WorkbenchWizardElement) sel.getFirstElement();
		settings.put(
			STORE_SELECTED_WIZARD_ID,
			selectedWizard.getID());
	}
}
/**
 * Notes the newly-selected wizard element and updates the page accordingly.
 *
 * @param event the selection changed event
 */
public void selectionChanged(SelectionChangedEvent event) {
	setErrorMessage(null);
	IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	WorkbenchWizardElement currentWizardSelection = (WorkbenchWizardElement) selection.getFirstElement();
	if (currentWizardSelection == null) {
		setMessage(null);
		setSelectedNode(null);
		return;
	}

	setSelectedNode(createWizardNode(currentWizardSelection));
	setMessage((String) currentWizardSelection.getDescription());
}
}
