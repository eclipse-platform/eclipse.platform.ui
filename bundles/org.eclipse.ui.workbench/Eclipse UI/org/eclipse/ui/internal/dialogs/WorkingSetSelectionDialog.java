/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
	Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font 
		should be activated and used by other components.
*************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.*;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.model.WorkbenchViewerSorter;

/**
 * A working set selection dialog displays a list of working
 * sets available in the workbench.
 * 
 * @see IWorkingSetSelectionDialog
 * @since 2.0
 */
public class WorkingSetSelectionDialog extends SelectionDialog implements IWorkingSetSelectionDialog {
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 200;
	private final static int SIZING_SELECTION_WIDGET_WIDTH = 50;

	private static class WorkingSetLabelProvider extends LabelProvider {
		private Map icons;

		public WorkingSetLabelProvider() {
			icons = new Hashtable();
		}
		public void dispose() {
			Iterator iterator = icons.values().iterator();
			
			while (iterator.hasNext()) {
				Image icon = (Image) iterator.next();
				icon.dispose();
			}
			super.dispose();
		}
		public Image getImage(Object object) {
			Assert.isTrue(object instanceof IWorkingSet);
			IWorkingSet workingSet = (IWorkingSet) object; 
			ImageDescriptor imageDescriptor = workingSet.getImage();

			if (imageDescriptor == null)
				return null;
				
			Image icon = (Image) icons.get(imageDescriptor);
			if (icon == null) {
				icon = imageDescriptor.createImage();
				icons.put(imageDescriptor, icon);
			}
			return icon;
		}
		public String getText(Object object) {
			Assert.isTrue(object instanceof IWorkingSet);
			IWorkingSet workingSet = (IWorkingSet) object; 
			return workingSet.getName();
		}
	}

	private ILabelProvider labelProvider;
	private IStructuredContentProvider contentProvider;
	private TableViewer listViewer;
	private Button newButton;
	private Button detailsButton;
	private Button removeButton;
	private IWorkingSet[] result;
	private boolean multiSelect;
	private List addedWorkingSets;	
	private List removedWorkingSets;
	private Map editedWorkingSets;

	/**
	 * Creates a working set selection dialog.
	 *
	 * @param parentShell the parent shell
	 * @param multi true=more than one working set can be chosen 
	 * 	in the dialog. false=only one working set can be chosen. Multiple
	 * 	working sets can still be selected and removed from the list but
	 * 	the dialog can only be closed when a single working set is selected.
	 */
	public WorkingSetSelectionDialog(Shell parentShell, boolean multi) {
		super(parentShell);
		contentProvider = new ListContentProvider();
		labelProvider = new WorkingSetLabelProvider();
		multiSelect = multi;		
		if (multiSelect) {
			setTitle(WorkbenchMessages.getString("WorkingSetSelectionDialog.title.multiSelect")); //$NON-NLS-1$;
			setMessage(WorkbenchMessages.getString("WorkingSetSelectionDialog.message.multiSelect")); //$NON-NLS-1$
		}
		else {
			setTitle(WorkbenchMessages.getString("WorkingSetSelectionDialog.title")); //$NON-NLS-1$;
			setMessage(WorkbenchMessages.getString("WorkingSetSelectionDialog.message")); //$NON-NLS-1$
		}
	}
	/**
	 * Adds the modify buttons to the dialog.
	 * 
	 * @param composite Composite to add the buttons to
	 */
	private void addModifyButtons(Composite composite) {
		Composite buttonComposite = new Composite(composite, SWT.RIGHT);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		buttonComposite.setLayout(layout);
		buttonComposite.setFont(composite.getFont());
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL);
		data.grabExcessHorizontalSpace = true;
		composite.setData(data);

		int id = IDialogConstants.CLIENT_ID + 1;
		newButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.newButton.label"), false); //$NON-NLS-1$
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				createWorkingSet();
			}
		});

		detailsButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.detailsButton.label"), false); //$NON-NLS-1$
		detailsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editSelectedWorkingSet();
			}
		});

		removeButton = createButton(buttonComposite, id++, WorkbenchMessages.getString("WorkingSetSelectionDialog.removeButton.label"), false); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				removeSelectedWorkingSets();
			}
		});
	}
	/**
	 * Overrides method from Dialog.
	 * 
	 * @see Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		restoreAddedWorkingSets();
		restoreChangedWorkingSets();
		restoreRemovedWorkingSets();
		super.cancelPressed();
	}
	/** 
	 * Overrides method from Window.
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		WorkbenchHelp.setHelp(shell, IHelpContextIds.WORKING_SET_SELECTION_DIALOG);
	}
	/**
	 * Overrides method from Dialog.
	 * Create the dialog widgets.
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		createMessageArea(composite);
		listViewer = new TableViewer(composite, SWT.BORDER | SWT.MULTI);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.heightHint = SIZING_SELECTION_WIDGET_HEIGHT;
		data.widthHint = SIZING_SELECTION_WIDGET_WIDTH;
		listViewer.getTable().setLayoutData(data);
		listViewer.getTable().setFont(parent.getFont());
		
		listViewer.setLabelProvider(labelProvider);
		listViewer.setContentProvider(contentProvider);
		listViewer.setSorter(new WorkbenchViewerSorter());
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				handleSelectionChanged();
			}
		});
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				okPressed();
			}
		});
		addModifyButtons(composite);
		listViewer.setInput(Arrays.asList(WorkbenchPlugin.getDefault().getWorkingSetManager().getWorkingSets()));

		return composite;
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
		//don't allow ok dismissal until a change has been made. 
		//Fixes bug 22735.
		getOkButton().setEnabled(false);
		return control;
	}
	/**
	 * Opens a working set wizard for creating a new working set.
	 */
	private void createWorkingSet() {
		WorkingSetNewWizard wizard = new WorkingSetNewWizard();
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
	
		dialog.create();		
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.WORKING_SET_NEW_WIZARD);
		if (dialog.open() == Window.OK) {
			IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();			
			IWorkingSet workingSet = wizard.getSelection();

			listViewer.add(workingSet);
			listViewer.setSelection(new StructuredSelection(workingSet), true);
			manager.addWorkingSet(workingSet);
			addedWorkingSets.add(workingSet);
		}
	}
	/**
	 * Opens a working set wizard for editing the currently selected 
	 * working set.
	 * 
	 * @see org.eclipse.ui.IWorkingSetPage
	 */
	private void editSelectedWorkingSet() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();			
		IWorkingSet editWorkingSet = (IWorkingSet) getSelectedWorkingSets().get(0);		
		IWorkingSetEditWizard wizard = manager.createWorkingSetEditWizard(editWorkingSet);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets.get(editWorkingSet);
		boolean firstEdit = originalWorkingSet == null;
		
		// save the original working set values for restoration when selection 
		// dialog is cancelled.
		if (firstEdit) {
			originalWorkingSet = new WorkingSet(editWorkingSet.getName(), editWorkingSet.getElements());
		}
		else {
			editedWorkingSets.remove(editWorkingSet);
		}
		dialog.create();
		WorkbenchHelp.setHelp(dialog.getShell(), IHelpContextIds.WORKING_SET_EDIT_WIZARD);
		if (dialog.open() == Window.OK) {		
			editWorkingSet = (IWorkingSet) wizard.getSelection();
			listViewer.update(editWorkingSet, null);
		}
		editedWorkingSets.put(editWorkingSet, originalWorkingSet);
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#getSelection()
	 */
	public IWorkingSet[] getSelection() {
		return result;
	}
	/**
	 * Returns the selected working sets.
	 * 
	 * @return the selected working sets
	 */
	private List getSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();
		if (selection instanceof IStructuredSelection)
			return ((IStructuredSelection) selection).toList();
		return null;
	}
	/**
	 * Called when the selection has changed.
	 */
	private void handleSelectionChanged() {
		updateButtonAvailability();
	}
	/**
	 * Sets the selected working sets as the dialog result.
	 * Overrides method from Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		List newResult = getSelectedWorkingSets();

		result = (IWorkingSet[]) newResult.toArray(new IWorkingSet[newResult.size()]);
		setResult(newResult);
		super.okPressed();
	}
	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#open()
	 */
	public int open() {
		addedWorkingSets = new ArrayList();
		removedWorkingSets = new ArrayList();
		editedWorkingSets = new HashMap();
		return super.open();
	}
	/**
	 * Removes the selected working sets from the workbench.
	 */
	private void removeSelectedWorkingSets() {
		ISelection selection = listViewer.getSelection();

		if (selection instanceof IStructuredSelection) {
			IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
			Iterator iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				IWorkingSet workingSet = (IWorkingSet) iter.next();
				manager.removeWorkingSet(workingSet);
				if (addedWorkingSets.contains(workingSet)) {
					addedWorkingSets.remove(workingSet);
				}
				else {
					removedWorkingSets.add(workingSet);
				}
			}
			listViewer.remove(((IStructuredSelection) selection).toArray());
		}			
	}
	/**
	 * Removes newly created working sets from the working set manager.
	 */
	private void restoreAddedWorkingSets() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		Iterator iterator = addedWorkingSets.iterator();
		
		while (iterator.hasNext()) {
			manager.removeWorkingSet(((IWorkingSet) iterator.next()));
		}		
	}
	/**
	 * Rolls back changes to working sets.
	 */
	private void restoreChangedWorkingSets() {
		Iterator iterator = editedWorkingSets.keySet().iterator();
		
		while (iterator.hasNext()) {
			IWorkingSet editedWorkingSet = (IWorkingSet) iterator.next();
			IWorkingSet originalWorkingSet = (IWorkingSet) editedWorkingSets.get(editedWorkingSet);
						
			if (editedWorkingSet.getName().equals(originalWorkingSet.getName()) == false) {
				editedWorkingSet.setName(originalWorkingSet.getName());
			}
			if (editedWorkingSet.getElements().equals(originalWorkingSet.getElements()) == false) {
				editedWorkingSet.setElements(originalWorkingSet.getElements());
			}
		}		
	}

	/**
	 * Adds back removed working sets to the working set manager.
	 */
	private void restoreRemovedWorkingSets() {
		IWorkingSetManager manager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		Iterator iterator = removedWorkingSets.iterator();
		
		while (iterator.hasNext()) {
			manager.addWorkingSet(((IWorkingSet) iterator.next()));
		}		
	}
	/**
	 * Implements IWorkingSetSelectionDialog.
	 *
	 * @see org.eclipse.ui.dialogs.IWorkingSetSelectionDialog#setSelection(IWorkingSet[])
	 */
	public void setSelection(IWorkingSet[] workingSets) {
		result = workingSets;
		setInitialSelections(workingSets);
	}
	/**
	 * Updates the modify buttons' enabled state based on the 
	 * current seleciton.
	 */
	private void updateButtonAvailability() {
		ISelection selection = listViewer.getSelection();
		boolean hasSelection = selection != null && !selection.isEmpty();
		boolean hasSingleSelection = hasSelection;

		removeButton.setEnabled(hasSelection);
		if (hasSelection && selection instanceof IStructuredSelection) {
			hasSingleSelection = ((IStructuredSelection) selection).size() == 1;
		}
		detailsButton.setEnabled(hasSingleSelection);
		if (multiSelect == false) {
			getOkButton().setEnabled(hasSelection == false || hasSingleSelection);
		}
		else {
			getOkButton().setEnabled(true);
		}
	}
}