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
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * A standard dialog which solicits a list of selections from the user.
 * This class is configured with an arbitrary data model represented by content
 * and label provider objects. The <code>getResult</code> method returns the
 * selected elements.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example:
 * <pre>
 * ListSelectionDialog dlg =
 *   new ListSelectionDialog(
 *       getShell(),
 *       input,
 *       new WorkbenchContentProvider(),
 *		 new WorkbenchLabelProvider(),
 *		 "Select the resources to save.");
 *	dlg.setInitialSelections(dirtyEditors);
 *	dlg.setTitle("Save Resources");
 *	dlg.open();
 * </pre>
 * </p>
 */
public class ListSelectionDialog extends SelectionDialog  {
	// the root element to populate the viewer with
	private Object inputElement;

	// providers for populating this dialog
	private ILabelProvider labelProvider;
	private IStructuredContentProvider contentProvider;

	// the visual selection widget group
	private CheckboxTableViewer listViewer;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
/**
 * Creates a list selection dialog.
 *
 * @param parentShell the parent shell
 * @param input	the root element to populate this dialog with
 * @param contentProvider the content provider for navigating the model
 * @param labelProvider the label provider for displaying model elements
 * @param message the message to be displayed at the top of this dialog, or
 *    <code>null</code> to display a default message
 */
public ListSelectionDialog(
		Shell parentShell,
		Object input,
		IStructuredContentProvider contentProvider,
		ILabelProvider labelProvider,
		String message) {
	super(parentShell);
	setTitle(WorkbenchMessages.getString("ListSelection.title")); //$NON-NLS-1$
	inputElement = input;
	this.contentProvider = contentProvider;
	this.labelProvider = labelProvider;
	if (message != null)
		setMessage(message);
	else
		setMessage(WorkbenchMessages.getString("ListSelection.message")); //$NON-NLS-1$
}
/**
 * Add the selection and deselection buttons to the dialog.
 * @param composite org.eclipse.swt.widgets.Composite
 */
private void addSelectionButtons(Composite composite) {

	Composite buttonComposite = new Composite(composite, SWT.RIGHT);
	buttonComposite.setFont(composite.getFont());	
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	buttonComposite.setLayout(layout);
	GridData data =
		new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
	data.grabExcessHorizontalSpace = true;
	composite.setData(data);

	Button selectButton = createButton(buttonComposite, IDialogConstants.SELECT_ALL_ID, SELECT_ALL_TITLE, false);

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			listViewer.setAllChecked(true);
		}
	};
	selectButton.addSelectionListener(listener);


	Button deselectButton = createButton(buttonComposite, IDialogConstants.DESELECT_ALL_ID, DESELECT_ALL_TITLE, false);

	listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			listViewer.setAllChecked(false);

		}
	};
	deselectButton.addSelectionListener(listener);
	

}
/**
 * Visually checks the previously-specified elements in this dialog's list 
 * viewer.
 */
private void checkInitialSelections() {
	Iterator itemsToCheck = getInitialElementSelections().iterator();
	
	while (itemsToCheck.hasNext())
		listViewer.setChecked(itemsToCheck.next(),true);
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.LIST_SELECTION_DIALOG);
}

/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// page group
	Composite composite = (Composite)super.createDialogArea(parent);
	
	Font font = parent.getFont();
	composite.setFont(font);

	createMessageArea(composite);

	listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_BOTH);
	data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
	data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
	listViewer.getTable().setLayoutData(data);

	listViewer.setLabelProvider(labelProvider);
	listViewer.setContentProvider(contentProvider);
	listViewer.getControl().setFont(font);

	addSelectionButtons(composite);

	initializeViewer();

	// initialize page
	if (!getInitialElementSelections().isEmpty())
		checkInitialSelections();

	return composite;
}

/**
 * Returns the viewer used to show the list.
 * 
 * @return the viewer, or <code>null</code> if not yet created
 */
protected CheckboxTableViewer getViewer() {
	return listViewer;
}

/**
 * Initializes this dialog's viewer after it has been laid out.
 */
private void initializeViewer() {
	listViewer.setInput(inputElement);
}
/**
 * The <code>ListSelectionDialog</code> implementation of this 
 * <code>Dialog</code> method builds a list of the selected elements for later
 * retrieval by the client and closes this dialog.
 */
protected void okPressed() {

	// Get the input children.
	Object[] children = contentProvider.getElements(inputElement);

	// Build a list of selected children.
	if (children != null) {
		ArrayList list = new ArrayList();
		for (int i = 0; i < children.length; ++i) {
			Object element = children[i];
			if (listViewer.getChecked(element))
				list.add(element);
		}
		setResult(list);
	}

	super.okPressed();
}
}
