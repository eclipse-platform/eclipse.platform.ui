/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.util.*;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.dialogs.DialogArea;

/**
 * Reusable area that provides a list to select from and a select all and
 * deselect all button.
 */
public class ListSelectionArea extends DialogArea {
	private Object inputElement;
	private IStructuredContentProvider contentProvider;
	private ILabelProvider labelProvider;
	private String message;
	private List initialSelections;

	// the visual selection widget group
	private CheckboxTableViewer listViewer;
	
	private Object[] previousCheckedElements;

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	
	public static final String LIST_SELECTION = "ListSelection"; //$NON-NLS-1$
	
	/**
	 * Constructor for ListSelectionArea.
	 * @param parentDialog
	 * @param settings
	 */
	public ListSelectionArea(
			Dialog parentDialog, 
			Object input,
			IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider,
			String message) {
		super(parentDialog, null);
		this.inputElement = input;
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		this.message = message;
		this.initialSelections = new ArrayList();
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.DialogArea#createArea(org.eclipse.swt.widgets.Composite)
	 */
	public void createArea(Composite parent) {
        Dialog.applyDialogFont(parent);
		Composite composite = createComposite(parent, 1);
		initializeDialogUnits(composite);

		if (message != null)
			createWrappingLabel(composite, message, 1);

		listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		
		listViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object[] checkedElements = getViewer().getCheckedElements();
				firePropertyChangeChange(LIST_SELECTION, previousCheckedElements, checkedElements);
				previousCheckedElements = checkedElements;
			}
		});

		addSelectionButtons(composite);

		initializeViewer();

		// initialize page
		if (!getInitialElementSelections().isEmpty())
			checkInitialSelections();
	}
	
	/**
	 * Initializes this dialog's viewer after it has been laid out.
	 */
	private void initializeViewer() {
		listViewer.setInput(inputElement);
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
	
	/**
	 * Add the selection and deselection buttons to the dialog.
	 * @param composite org.eclipse.swt.widgets.Composite
	 */
	private void addSelectionButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		Button selectButton = createButton(buttonComposite, Policy.bind("ListSelectionArea.selectAll"), GridData.HORIZONTAL_ALIGN_FILL); //$NON-NLS-1$

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(true);
			}
		};
		selectButton.addSelectionListener(listener);


		Button deselectButton = createButton(buttonComposite, Policy.bind("ListSelectionArea.deselectAll"), GridData.HORIZONTAL_ALIGN_FILL); //$NON-NLS-1$

		listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				listViewer.setAllChecked(false);

			}
		};
		deselectButton.addSelectionListener(listener);
	}
	
	/**
	 * Returns the list of initial element selections.
	 * @return List
	 */
	protected List getInitialElementSelections(){
		return initialSelections;
	}
	
	/**
	 * Sets the initial selection in this selection dialog to the given elements.
	 *
	 * @param selectedElements the array of elements to select
	 */
	public void setInitialSelections(Object[] selectedElements) {
		initialSelections = new ArrayList(selectedElements.length);
		for (int i = 0; i < selectedElements.length; i++)
			initialSelections.add(selectedElements[i]);
	}

	/**
	 * Sets the initial selection in this selection dialog to the given elements.
	 *
	 * @param selectedElements the List of elements to select
	 */
	public void setInitialElementSelections(List selectedElements) {
		initialSelections = selectedElements;
	}
	/**
	 * Returns the listViewer.
	 * @return CheckboxTableViewer
	 */
	public CheckboxTableViewer getViewer() {
		return listViewer;
	}

}
