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
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.activities.ws.ActivityMessages;
import org.eclipse.ui.model.AdaptableList;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * Abstract implementation of a wizard selection page which simply displays a
 * list of specified wizard elements and allows the user to select one to be
 * launched. Subclasses just need to provide a method which creates an
 * appropriate wizard node based upon a user selection.
 */
public abstract class WorkbenchWizardListSelectionPage
	extends WorkbenchWizardSelectionPage
	implements ISelectionChangedListener, IDoubleClickListener {

	// id constants
	private static final String DIALOG_SETTING_SECTION_NAME = "WizardListSelectionPage."; //$NON-NLS-1$

	private final static int SIZING_LISTS_HEIGHT = 200;
	private static final String STORE_SELECTED_WIZARD_ID = DIALOG_SETTING_SECTION_NAME + "STORE_SELECTED_WIZARD_ID"; //$NON-NLS-1$
	private static final String SHOW_ALL_ENABLED = DIALOG_SETTING_SECTION_NAME + ".SHOW_ALL_ENABLED"; //$NON-NLS-1$
	private TableViewer filteredViewer, unfilteredViewer;
	private String message;

	private StackLayout stackLayout;
	private Composite stackComposite;
	private Button showAllCheck;
	/**
	 * Creates a <code>WorkbenchWizardListSelectionPage</code>.
	 * 
	 * @param aWorkbench the current workbench
	 * @param currentSelection the workbench's current resource selection
	 * @param wizardElements the collection of <code>WorkbenchWizardElements</code>
	 *            to display for selection
	 * @param message the message to display above the selection list
	 */
	protected WorkbenchWizardListSelectionPage(
		IWorkbench aWorkbench,
		IStructuredSelection currentSelection,
		AdaptableList wizardElements,
		String message) {
		super("singleWizardSelectionPage", aWorkbench, currentSelection, wizardElements); //$NON-NLS-1$
		setDescription(WorkbenchMessages.getString("WizardList.description")); //$NON-NLS-1$
		this.message = message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Font font = parent.getFont();

		// create composite for page.
		Composite outerContainer = new Composite(parent, SWT.NONE);
		outerContainer.setLayout(new GridLayout());
		outerContainer.setLayoutData(
			new GridData(
				GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
		outerContainer.setFont(font);

		Label messageLabel = new Label(outerContainer, SWT.NONE);
		messageLabel.setText(message);
		messageLabel.setFont(font);

		stackLayout = new StackLayout();			
		stackComposite = new Composite(outerContainer, SWT.NONE);
		stackComposite.setLayout(stackLayout);
		stackComposite.setFont(parent.getFont());
		layoutTopControl(stackComposite);		
		filteredViewer = createViewer(stackComposite, true);
		
		if (WorkbenchActivityHelper.showAll()) {
			unfilteredViewer = createViewer(stackComposite, false);

			showAllCheck = new Button(outerContainer, SWT.CHECK);
			showAllCheck.setText(ActivityMessages.getString("ActivityFiltering.showAll")); //$NON-NLS-1$
			
			// flipping tabs updates the selected node
			showAllCheck.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (!showAllCheck.getSelection()) {
					    filteredViewer.setSelection(unfilteredViewer.getSelection());
						stackLayout.topControl = filteredViewer.getControl();	
						stackComposite.layout();					    
						selectionChanged(
							new SelectionChangedEvent(
								filteredViewer,
								filteredViewer.getSelection()));
					} else {
					    unfilteredViewer.setSelection(filteredViewer.getSelection());
						stackLayout.topControl = unfilteredViewer.getControl();	
						stackComposite.layout();					    					    
						selectionChanged(
							new SelectionChangedEvent(
								unfilteredViewer,
								unfilteredViewer.getSelection()));
					}
				}
			});

		}
		
		stackLayout.topControl = filteredViewer.getControl();

		restoreWidgetValues();

		setControl(outerContainer);
	}
	/**
	 * Create a new viewer in the parent.
	 * 
	 * @param parent the parent <code>Composite</code>.
	 * @param filtering whether the viewer should be filtering based on
	 *            activities.
	 * @return <code>TableViewer</code>
	 */
	private TableViewer createViewer(Composite parent, boolean filtering) {
		//Create a table for the list
		Table table = new Table(parent, SWT.BORDER);
		table.setFont(parent.getFont());

		// the list viewer
		TableViewer viewer = new TableViewer(table);
		viewer.setContentProvider(new WizardContentProvider(filtering));
		viewer.setLabelProvider(new WorkbenchLabelProvider());
		viewer.setSorter(new WorkbenchViewerSorter());
		viewer.addSelectionChangedListener(this);
		viewer.addDoubleClickListener(this);
		viewer.setInput(wizardElements);

		return viewer;
	}

	/**
	 * Returns an <code>IWizardNode</code> representing the specified
	 * workbench wizard which has been selected by the user. <b>Subclasses
	 * </b> must override this abstract implementation.
	 * 
	 * @param element the wizard element that an <code>IWizardNode</code> is
	 *            needed for
	 * @return org.eclipse.jface.wizards.IWizardNode
	 */
	protected abstract IWizardNode createWizardNode(WorkbenchWizardElement element);

	/**
	 * An item in a viewer has been double-clicked.
	 */
	public void doubleClick(DoubleClickEvent event) {
		selectionChanged(
			new SelectionChangedEvent(
				event.getViewer(),
				event.getViewer().getSelection()));
		getContainer().showPage(getNextPage());
	}

	/**
	 * Layout the top control.
	 * 
	 * @param control the control.
	 * @since 3.0
	 */
	private void layoutTopControl(Control control) {
		GridData data = new GridData(GridData.FILL_BOTH);

		int availableRows = DialogUtil.availableRows(control.getParent());

		//Only give a height hint if the dialog is going to be too small
		if (availableRows > 50) {
			data.heightHint = SIZING_LISTS_HEIGHT;
		} else {
			data.heightHint = availableRows * 3;
		}

		control.setLayoutData(data);

	}

	/**
	 * Uses the dialog store to restore widget values to the values that they
	 * held last time this wizard was used to completion.
	 */
	private void restoreWidgetValues() {
	    
	    updateViewerSelection(
				filteredViewer,
				STORE_SELECTED_WIZARD_ID);
	    
		if (unfilteredViewer != null) {
		    updateViewerSelection(
					unfilteredViewer,
					STORE_SELECTED_WIZARD_ID);
		    
			boolean unfilteredSelected =
				getDialogSettings().getBoolean(SHOW_ALL_ENABLED);
			
			showAllCheck.setSelection(unfilteredSelected);

			if (unfilteredSelected) {
				stackLayout.topControl = unfilteredViewer.getControl();
				stackComposite.layout();
			}			
		}		
	}

	/**
	 * @param viewer the <code>TableViewer</code> to save.
	 * @param key the preference key to use.
	 * @since 3.0
	 */
	private void saveViewerSelection(TableViewer viewer, String key) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		if (sel.size() > 0) {
			WorkbenchWizardElement selectedWizard =
				(WorkbenchWizardElement) sel.getFirstElement();
			getDialogSettings().put(key, selectedWizard.getID());
		}
	}

	/**
	 * Since Finish was pressed, write widget values to the dialog store so
	 * that they will persist into the next invocation of this wizard page
	 */
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();

		if (showAllCheck != null && showAllCheck.getSelection()) {
		    saveViewerSelection(
					unfilteredViewer,
					STORE_SELECTED_WIZARD_ID);
		}
		else {
		    saveViewerSelection(
					filteredViewer,
					STORE_SELECTED_WIZARD_ID);
		}	    
		
		if (showAllCheck != null) {
			settings.put(
				SHOW_ALL_ENABLED,
				showAllCheck.getSelection());
		}
		
	}

	/**
	 * Notes the newly-selected wizard element and updates the page
	 * accordingly.
	 * 
	 * @param event the selection changed event
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		setErrorMessage(null);
		IStructuredSelection selection =
			(IStructuredSelection) event.getSelection();
		WorkbenchWizardElement currentWizardSelection =
			(WorkbenchWizardElement) selection.getFirstElement();
		if (currentWizardSelection == null) {
			setMessage(null);
			setSelectedNode(null);
			return;
		}

		setSelectedNode(createWizardNode(currentWizardSelection));
		setMessage(currentWizardSelection.getDescription());
	}

	/**
	 * @param viewer the <code>TableViewer</code> to update.
	 * @param key the preference key to use.
	 * @since 3.0
	 */
	private void updateViewerSelection(TableViewer viewer, String key) {
		String wizardId = getDialogSettings().get(key);
		WorkbenchWizardElement wizard = findWizard(wizardId);
		if (wizard != null) {
			StructuredSelection selection = new StructuredSelection(wizard);
			viewer.setSelection(selection);
		}
	}
}
