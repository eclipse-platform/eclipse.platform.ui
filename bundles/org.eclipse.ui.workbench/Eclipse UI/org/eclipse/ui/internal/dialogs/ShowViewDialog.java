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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.registry.*;

/**
 * The Show View dialog.
 */
public class ShowViewDialog extends org.eclipse.jface.dialogs.Dialog
	implements ISelectionChangedListener, IDoubleClickListener
{
	private TreeViewer tree;
	private IViewRegistry viewReg;
	private IViewDescriptor[] viewDescs = new IViewDescriptor[0];
	private Button okButton;
	private Button cancelButton;

	private static final int LIST_WIDTH = 250;
	private static final int LIST_HEIGHT = 300;

	private static final String DIALOG_SETTING_SECTION_NAME = "ShowViewDialog";//$NON-NLS-1$
	private static final String STORE_EXPANDED_CATEGORIES_ID =
		DIALOG_SETTING_SECTION_NAME + ".STORE_EXPANDED_CATEGORIES_ID";//$NON-NLS-1$

/**
 * Constructs a new ShowViewDialog.
 */
public ShowViewDialog(Shell parentShell, IViewRegistry viewReg) {
	super(parentShell);
	this.viewReg = viewReg;
}

/**
 * This method is called if a button has been pressed.
 */
protected void buttonPressed(int buttonId) {
	if (buttonId == IDialogConstants.OK_ID) 
		saveWidgetValues();
	super.buttonPressed(buttonId); 
}

/**
 * Notifies that the cancel button of this dialog has been pressed.
 */
protected void cancelPressed() {
	viewDescs = new IViewDescriptor[0];
	super.cancelPressed();
}

/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	shell.setText(WorkbenchMessages.getString("ShowView.shellTitle")); //$NON-NLS-1$
	WorkbenchHelp.setHelp(shell, IHelpContextIds.SHOW_VIEW_DIALOG);
}
/**
 * Adds buttons to this dialog's button bar.
 * <p>
 * The default implementation of this framework method adds 
 * standard ok and cancel buttons using the <code>createButton</code>
 * framework method. Subclasses may override.
 * </p>
 *
 * @param parent the button bar composite
 */
protected void createButtonsForButtonBar(Composite parent) {
	okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	cancelButton = createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
}
/**
 * Creates and returns the contents of the upper part 
 * of this dialog (above the button bar).
 *
 * @param the parent composite to contain the dialog area
 * @return the dialog area control
 */
protected Control createDialogArea(Composite parent) {
	// Run super.
	Composite composite = (Composite)super.createDialogArea(parent);

	// Add perspective list.
	tree = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
	tree.setLabelProvider(new ViewLabelProvider());
	tree.setContentProvider(new ViewContentProvider());
	tree.setSorter(new ViewSorter((ViewRegistry) viewReg));
	tree.setInput(viewReg);
	tree.addSelectionChangedListener(this);
	tree.addDoubleClickListener(this);
	tree.getTree().setFont(parent.getFont());

	// Set tree size.
	Control ctrl = tree.getControl();
	GridData spec = new GridData(GridData.FILL_BOTH);
	spec.widthHint = LIST_WIDTH;
	spec.heightHint = LIST_HEIGHT;
	ctrl.setLayoutData(spec);

	// Restore the last state
	restoreWidgetValues();
	
	// Return results.
	return composite;
}
/* (non-Javadoc)
 * Method declared on IDoubleClickListener
 */
public void doubleClick(DoubleClickEvent event) {
	IStructuredSelection s = (IStructuredSelection) event.getSelection();
	Object element = s.getFirstElement();
	if (tree.isExpandable(element)) {
		tree.setExpandedState(element, !tree.getExpandedState(element));
	}
	else if (viewDescs.length > 0) {
		saveWidgetValues();
		setReturnCode(OK);
		close();
	}
}
/**
 * Return the dialog store to cache values into
 */
protected IDialogSettings getDialogSettings() {
	IDialogSettings workbenchSettings = WorkbenchPlugin.getDefault().getDialogSettings();
	IDialogSettings section = workbenchSettings.getSection(DIALOG_SETTING_SECTION_NAME);
	if(section == null)
		section = workbenchSettings.addNewSection(DIALOG_SETTING_SECTION_NAME);
	return section;
}

/**
 * Returns the descriptors for the selected views.
 */
public IViewDescriptor[] getSelection() {
	return viewDescs;
}

/**
 * Use the dialog store to restore widget values to the values that they held
 * last time this dialog was used to completion.
 */
protected void restoreWidgetValues() {
	IDialogSettings settings = getDialogSettings();
	String[] expandedCategoryIds = settings.getArray(STORE_EXPANDED_CATEGORIES_ID);
	if (expandedCategoryIds == null)
		return;

	ViewRegistry reg = (ViewRegistry)viewReg;
	ArrayList categoriesToExpand = new ArrayList(expandedCategoryIds.length);
	for (int i = 0; i < expandedCategoryIds.length; i++) {
		ICategory category = reg.findCategory(expandedCategoryIds[i]);
		if (category != null)	// ie.- it still exists
			categoriesToExpand.add(category);
	}

	if (!categoriesToExpand.isEmpty())
		tree.setExpandedElements(categoriesToExpand.toArray());
}

/**
 * Since OK was pressed, write widget values to the dialog store so that they
 * will persist into the next invocation of this dialog
 */
protected void saveWidgetValues() {
	IDialogSettings settings = getDialogSettings();

	// Collect the ids of the all expanded categories	
	Object[] expandedElements = tree.getExpandedElements();
	String[] expandedCategoryIds = new String[expandedElements.length];
	for (int i = 0; i < expandedElements.length; ++i)
		expandedCategoryIds[i] = ((ICategory)expandedElements[i]).getId();

	// Save them for next time.
	settings.put(
		STORE_EXPANDED_CATEGORIES_ID,
		expandedCategoryIds);
}

/**
 * Notifies that the selection has changed.
 *
 * @param event event object describing the change
 */
public void selectionChanged(SelectionChangedEvent event) {
	updateSelection();
	updateButtons();
}

/**
 * Update the button enablement state.
 */
protected void updateButtons() {
	okButton.setEnabled(getSelection() != null);	
}

/**
 * Update the selection object.
 */
protected void updateSelection() {
	ArrayList descs = new ArrayList();
	IStructuredSelection sel = (IStructuredSelection) tree.getSelection();
	for (Iterator i = sel.iterator(); i.hasNext();) {
		Object o = i.next();
		if (o instanceof IViewDescriptor) {
			descs.add(o);
		}
	}
	viewDescs = new IViewDescriptor[descs.size()];
	descs.toArray(viewDescs);
}
}
