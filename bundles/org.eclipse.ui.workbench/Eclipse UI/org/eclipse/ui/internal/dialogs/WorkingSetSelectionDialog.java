/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation 
 * 		Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *   	should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.internal.AggregateWorkingSet;
import org.eclipse.ui.internal.IWorkbenchHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.model.WorkbenchViewerComparator;

/**
 * A working set selection dialog displays a list of working
 * sets available in the workbench.
 * 
 * @see IWorkingSetSelectionDialog
 * @since 2.0
 */
public class WorkingSetSelectionDialog extends AbstractWorkingSetDialog {
    private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;

    private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

    private ILabelProvider labelProvider;

    private IStructuredContentProvider contentProvider;

    private CheckboxTableViewer listViewer;

    private boolean multiSelect;
    
    private IWorkbenchWindow workbenchWindow;

	private Button buttonWindowSet;

	private Button buttonNoSet;

	private Button buttonSelectedSets;

    /**
     * Creates a working set selection dialog.
     *
     * @param parentShell the parent shell
     * @param multi true=more than one working set can be chosen 
     * 	in the dialog. false=only one working set can be chosen. Multiple
     * 	working sets can still be selected and removed from the list but
     * 	the dialog can only be closed when a single working set is selected.
     * @param workingSetIds a list of working set ids which are valid workings sets
     *  to be selected, created, removed or edited, or <code>null</code> if all currently
     *  available working set types are valid 
     */
    public WorkingSetSelectionDialog(Shell parentShell, boolean multi, String[] workingSetIds) {
        super(parentShell, workingSetIds, true);
        initWorkbenchWindow();
        
        contentProvider = new ArrayContentProvider();
        labelProvider = new WorkingSetLabelProvider();
        multiSelect = multi;
        if (multiSelect) {
            setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title_multiSelect); 
            setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message_multiSelect);
        } else {
            setTitle(WorkbenchMessages.WorkingSetSelectionDialog_title); 
            setMessage(WorkbenchMessages.WorkingSetSelectionDialog_message);
        }
            			
    }

    /**
	 * Determine what window this dialog is being opened on. This impacts the
	 * returned working set in the case where the user chooses the window set.
	 * 
	 * @since 3.2
	 */
    private void initWorkbenchWindow() {
		Shell shellToCheck = getShell();

		workbenchWindow = Util.getWorkbenchWindowForShell(shellToCheck);
	}

    /**
     * Overrides method from Dialog.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    protected void cancelPressed() {
        restoreAddedWorkingSets();
        restoreChangedWorkingSets();
        restoreRemovedWorkingSets();
        setSelection(null);
        super.cancelPressed();
    }

    /** 
     * Overrides method from Window.
     * 
     * @see org.eclipse.jface.window.Window#configureShell(Shell)
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IWorkbenchHelpContextIds.WORKING_SET_SELECTION_DIALOG);
    }

    /**
     * Overrides method from Dialog.
     * Create the dialog widgets.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
     */
    protected Control createDialogArea(Composite parent) {
    	initializeDialogUnits(parent);
    	
        Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateButtonAvailability();
			}
		};
		
		buttonWindowSet = new Button(composite, SWT.RADIO);
		buttonWindowSet.setText(WorkbenchMessages.WindowWorkingSets);
		buttonWindowSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonWindowSet.addSelectionListener(listener);

		buttonNoSet = new Button(composite, SWT.RADIO);
		buttonNoSet.setText(WorkbenchMessages.NoWorkingSet);
		buttonNoSet.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttonNoSet.addSelectionListener(listener);

		buttonSelectedSets = new Button(composite, SWT.RADIO);
		buttonSelectedSets.setText(WorkbenchMessages.SelectedWorkingSets);
		buttonSelectedSets.addSelectionListener(listener);

		switch (getInitialRadioSelection()) {
		case 0:
			buttonWindowSet.setSelection(true);
			break;
		case 1:
			buttonNoSet.setSelection(true);
			break;
		case 2:
			buttonSelectedSets.setSelection(true);
			break;
		}
		buttonSelectedSets
				.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
		Composite viewerComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		viewerComposite.setLayout(layout);
		
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH + 300;  // fudge?  I like fudge.
		viewerComposite.setLayoutData(data);
		
		listViewer = CheckboxTableViewer.newCheckList(viewerComposite,
				SWT.BORDER | SWT.MULTI);
		data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);

        listViewer.setLabelProvider(labelProvider);
        listViewer.setContentProvider(contentProvider);
        listViewer.setComparator(new WorkbenchViewerComparator());
        
        listViewer.addFilter(new WorkingSetFilter(getSupportedWorkingSetIds()));
        
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                handleSelectionChanged();
            }
        });
        listViewer.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent event) {
            	Object obj = ((IStructuredSelection) listViewer.getSelection())
						.getFirstElement();
				listViewer.setCheckedElements(new Object[] {obj});
				buttonWindowSet.setSelection(false);
				buttonNoSet.setSelection(false);
				buttonSelectedSets.setSelection(true);            	
            	okPressed();
            }
        });
        listViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				// implicitly select the third radio button
				buttonWindowSet.setSelection(false);
				buttonNoSet.setSelection(false);
				buttonSelectedSets.setSelection(true);
			}
		});

        addModifyButtons(viewerComposite);
        
        addSelectionButtons(composite);
        

		listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault()
				.getWorkingSetManager().getWorkingSets()));
        List initialElementSelections = getInitialElementSelections();
		if (multiSelect) {
			listViewer.setCheckedElements(initialElementSelections.toArray());
		} else if (!initialElementSelections.isEmpty()) {
			IWorkingSet set = (IWorkingSet) initialElementSelections.get(0);
			if (set instanceof AggregateWorkingSet) {
				AggregateWorkingSet aggregate = (AggregateWorkingSet) set;
				listViewer.setCheckedElements(aggregate.getComponents());
			}
			else {
				listViewer.setCheckedElements(initialElementSelections.toArray());
			}
		}
		
		availableWorkingSetsChanged();
		Dialog.applyDialogFont(composite);
		
		return composite;
    }

    private int getInitialRadioSelection() {
    		IWorkingSet windowSet = workbenchWindow.getActivePage().getAggregateWorkingSet();
    		
    		int selectionIndex;
    		if (getSelection() != null && getSelection().length > 0) {
    			if (windowSet.equals(getSelection()[0])) {
    				selectionIndex = 0;
    			}
    			else {
    				selectionIndex = 2;
    			}
    		}
    		else {
    			selectionIndex = 1;
    		}
    		
		return selectionIndex;
	}

	/**
     * Overrides method from Dialog.
     * Sets the initial selection, if any.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        List selections = getInitialElementSelections();
        if (!selections.isEmpty()) {
            listViewer.setSelection(new StructuredSelection(selections), true);
        }
        updateButtonAvailability();
        return control;
    }

    /**
     * Returns the selected working sets.
     * 
     * @return the selected working sets
     */
    protected List getSelectedWorkingSets() {
        ISelection selection = listViewer.getSelection();
        if (selection instanceof IStructuredSelection) {
			return ((IStructuredSelection) selection).toList();
		}
        return null;
    }

    /**
     * Called when the selection has changed.
     */
    void handleSelectionChanged() {
        updateButtonAvailability();
    }

    /**
     * Sets the selected working sets as the dialog result.
     * Overrides method from Dialog
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
    		if (buttonWindowSet.getSelection()) {
    			IWorkingSet [] windowSet = new IWorkingSet[] {workbenchWindow.getActivePage().getAggregateWorkingSet()};
    			setSelection(windowSet);
    			setResult(Arrays.asList(getSelection()));
    		}
    		else if (buttonNoSet.getSelection()) {
			setSelection(new IWorkingSet[0]);
			setResult(Arrays.asList(getSelection()));
    		}
    		else if (buttonSelectedSets.getSelection()) {
			Object[] untypedResult = listViewer.getCheckedElements();
			IWorkingSet[] typedResult = new IWorkingSet[untypedResult.length];
			System.arraycopy(untypedResult, 0, typedResult, 0,
					untypedResult.length);
			// if multiselect is allowed or there was only one selected then dont create 
			// an aggregate
			if (multiSelect || typedResult.length <= 1) {
				setSelection(typedResult);
				setResult(Arrays.asList(typedResult));
			}
			else {
				String setId = getAggregateIdForSets(typedResult);
				IWorkingSetManager workingSetManager = workbenchWindow
						.getWorkbench().getWorkingSetManager();
				IWorkingSet aggregate = workingSetManager
						.getWorkingSet(setId);
				if (aggregate == null) {
					aggregate = workingSetManager
							.createAggregateWorkingSet(
									setId,
									WorkbenchMessages.WorkbenchPage_workingSet_multi_label,
									typedResult);
					workingSetManager.addWorkingSet(aggregate);
				}
				setSelection(new IWorkingSet[] {aggregate});
				setResult(Collections.singletonList(aggregate));
			}
    		}
        
        super.okPressed();
    }

    /**
	 * Create a string that represents the name of the aggregate set composed of
	 * the supplied working sets. It's very long and not printworthy.
	 * 
	 * @param typedResult the sets 
	 * @return the name
	 */
    private String getAggregateIdForSets(IWorkingSet[] typedResult) {
    		StringBuffer buffer = new StringBuffer();
    		buffer.append("Aggregate:"); //$NON-NLS-1$
    		for (int i = 0; i < typedResult.length; i++) {
			buffer.append(typedResult[i].getName()).append(':');
		}
		return buffer.toString();
	}

	/**
     * Removes newly created working sets from the working set manager.
     */
    private void restoreAddedWorkingSets() {
        IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                .getWorkingSetManager();
        Iterator iterator = getAddedWorkingSets().iterator();

        while (iterator.hasNext()) {
            manager.removeWorkingSet(((IWorkingSet) iterator.next()));
        }
    }

    /**
     * Rolls back changes to working sets.
     */
    private void restoreChangedWorkingSets() {
        Iterator iterator = getEditedWorkingSets().keySet().iterator();

        while (iterator.hasNext()) {
            IWorkingSet editedWorkingSet = (IWorkingSet) iterator.next();
            IWorkingSet originalWorkingSet = (IWorkingSet) getEditedWorkingSets()
                    .get(editedWorkingSet);

            if (editedWorkingSet.getName().equals(originalWorkingSet.getName()) == false) {
                editedWorkingSet.setName(originalWorkingSet.getName());
            }
            if (editedWorkingSet.getElements().equals(
                    originalWorkingSet.getElements()) == false) {
                editedWorkingSet.setElements(originalWorkingSet.getElements());
            }
        }
    }

    /**
     * Adds back removed working sets to the working set manager.
     */
    private void restoreRemovedWorkingSets() {
        IWorkingSetManager manager = WorkbenchPlugin.getDefault()
                .getWorkingSetManager();
        Iterator iterator = getRemovedWorkingSets().iterator();

        while (iterator.hasNext()) {
            manager.addWorkingSet(((IWorkingSet) iterator.next()));
        }
        iterator = getRemovedMRUWorkingSets().iterator();
        while (iterator.hasNext()) {
            manager.addRecentWorkingSet(((IWorkingSet) iterator.next()));
        }
    }

    /**
     * Implements IWorkingSetSelectionDialog.
     *
     * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
     */
    public void setSelection(IWorkingSet[] workingSets) {
        super.setSelection(workingSets);
        setInitialSelections(workingSets == null ? new Object[0] : workingSets);
    }

	protected void availableWorkingSetsChanged() {
		listViewer.setInput(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets());
		super.availableWorkingSetsChanged();
	}

	protected void selectAllSets() {
		listViewer.setCheckedElements(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSets());
		// implicitly select the third radio button
		buttonWindowSet.setSelection(false);
		buttonNoSet.setSelection(false);
		buttonSelectedSets.setSelection(true);
		updateButtonAvailability();
	}

	protected void deselectAllSets() {
		listViewer.setCheckedElements(new Object[0]);
		// implicitly select the third radio button
		buttonWindowSet.setSelection(false);
		buttonNoSet.setSelection(false);
		buttonSelectedSets.setSelection(true);
		updateButtonAvailability();
	}
}
