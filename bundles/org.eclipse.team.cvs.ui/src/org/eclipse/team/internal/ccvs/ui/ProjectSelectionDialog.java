/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 */
public class ProjectSelectionDialog extends ListSelectionDialog {
	static final int SELECT_ID = IDialogConstants.CLIENT_ID + 1;

	private Button workingSetButton;
	private Combo mruList;
	private Button selectButton;
	private IWorkingSet workingSet;
	
	// dialogs settings that are persistent between workbench sessions
	private IDialogSettings settings;
	private static final String USE_WORKING_SET = "UseWorkingSet"; //$NON-NLS-1$
	private static final String SELECTED_WORKING_SET = "SelectedWorkingSet"; //$NON-NLS-1$
	
	/*
	 * Used to update the mru list box when working sets are 
	 * renamed in the working set selection dialog.
	 */
	private IPropertyChangeListener workingSetChangeListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			String property = event.getProperty();
			Object newValue = event.getNewValue();
			
			if (IWorkingSetManager.CHANGE_WORKING_SET_NAME_CHANGE.equals(property) && 
				newValue instanceof IWorkingSet) {
				String newName = ((IWorkingSet) newValue).getName();
				int count = mruList.getItemCount();
				for (int i = 0; i < count; i++) {
					String item = mruList.getItem(i);
					IWorkingSet workingSet = (IWorkingSet) mruList.getData(item);
					if (workingSet == newValue) {
						boolean isTopItem = (mruList.getData(mruList.getText()) == workingSet);
						mruList.remove(i);
						mruList.add(newName, i);
						mruList.setData(newName, workingSet);
						if (isTopItem) {
							mruList.setText(newName);
						}
						break;
					}
				}
			}	
		}
	};
	
	/**
	 * Creates a filter selection dialog.
	 *
	 * @param parentShell the parent shell
	 * @param input the root element to populate this dialog with
	 * @param contentProvider the content provider for navigating the model
	 * @param labelProvider the label provider for displaying model elements
	 * @param message the message to be displayed at the top of this dialog, or
	 *    <code>null</code> to display a default message
	 */
	public ProjectSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider, ILabelProvider labelProvider, String message) {
		super(parentShell, input, contentProvider, labelProvider, message);
		
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("ProjectSelectionDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("ProjectSelectionDialog");//$NON-NLS-1$
		}
		if (settings.getBoolean(USE_WORKING_SET)) {
			setWorkingSet(PlatformUI.getWorkbench().getWorkingSetManager().getWorkingSet(settings.get(SELECTED_WORKING_SET)));
		}
		
	}
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void buttonPressed(int buttonId) {
		if (SELECT_ID == buttonId) {
			handleWorkingSetSelection();
		}
		else {
			super.buttonPressed(buttonId);
		}
	}
	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite group = new Composite(composite, SWT.NONE);
		GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		group.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;		
		group.setLayout(layout);

		workingSetButton = new Button(group, SWT.CHECK);	
		workingSetButton.setText(Policy.bind("ResourceSelectionDialog.workingSet")); //$NON-NLS-1$
		workingSetButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleWorkingSetButtonSelection();
			}
		});
		data = new GridData();
		data.horizontalSpan = 2;
		workingSetButton.setLayoutData(data);

		group = new Composite(group, SWT.NONE);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		group.setLayoutData(data);
		layout = new GridLayout();
		layout.marginHeight = 0;
		group.setLayout(layout);

		mruList = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_CENTER);
		mruList.setLayoutData(data);
		mruList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleMruSelection();
			}
		});
		selectButton = createButton(group, SELECT_ID, Policy.bind("ResourceSelectionDialog.workingSetOther"), false); //$NON-NLS-1$

		initializeMru();
		initializeWorkingSet();
		return composite;
	}

	/**
	 * Method handleMruSelection.
	 */
	private void handleMruSelection() {
		workingSet = (IWorkingSet) mruList.getData(mruList.getText());
		handleWorkingSetChange();
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.PROJECT_SELECTION_DIALOG);
	}
	/**
	 * Returns the selected working set or null if none is selected.
	 * 
	 * @return the selected working set or null if none is selected.
	 */
	public IWorkingSet getWorkingSet() {
		return workingSet;
	}
	/**
	 * Opens the working set selection dialog if the "Other..." item
	 * is selected in the most recently used working set list.
	 */
	private void handleWorkingSetSelection() {
		IWorkingSetSelectionDialog dialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false);
		IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.getWorkingSet(mruList.getText());

		if (workingSet != null) {
			dialog.setSelection(new IWorkingSet[]{workingSet});
		}
		// add a change listener to detect a working set name change
		workingSetManager.addPropertyChangeListener(workingSetChangeListener);		
		if (dialog.open() == Window.OK) {
			IWorkingSet[] result = dialog.getSelection();
			if (result != null && result.length > 0) {
				workingSet = result[0];
				String workingSetName = workingSet.getName();
				if (mruList.indexOf(workingSetName) != -1) {
					mruList.remove(workingSetName);
				}					
				mruList.add(workingSetName, 0);
				mruList.setText(workingSetName);
				mruList.setData(workingSetName, workingSet);
				handleMruSelection();
			}
			else {
				workingSet = null;
			}				
			// remove deleted working sets from the mru list box				
			String[] mruNames = mruList.getItems();
			for (int i = 0; i < mruNames.length; i++) {
				if (workingSetManager.getWorkingSet(mruNames[i]) == null) {
					mruList.remove(mruNames[i]);
				}
			}
		}
		workingSetManager.removePropertyChangeListener(workingSetChangeListener);
	}
	/**
	 * Sets the enabled state of the most recently used working set list
	 * based on the checked state of the working set check box.
	 */
	private void handleWorkingSetButtonSelection() {
		boolean useWorkingSet = workingSetButton.getSelection();
		mruList.setEnabled(useWorkingSet);
		selectButton.setEnabled(useWorkingSet);
		if (useWorkingSet && mruList.getSelectionIndex() >= 0) {
			handleMruSelection();
		}
	}
	
	/**
	 * Populates the most recently used working set list with MRU items from
	 * the working set manager as well as adds an item to enable selection of
	 * a working set not in the MRU list.
	 */
	private void initializeMru() {
		IWorkingSet[] workingSets = PlatformUI.getWorkbench().getWorkingSetManager().getRecentWorkingSets();
		
		for (int i = 0; i < workingSets.length; i++) {
			String workingSetName = workingSets[i].getName();
			mruList.add(workingSetName);
			mruList.setData(workingSetName, workingSets[i]);
		}
		if (workingSets.length > 0) {
			mruList.setText(workingSets[0].getName());
		}
	}
	
	/**
	 * Initializes the state of the working set part of the dialog.
	 */
	private void initializeWorkingSet() {
		workingSetButton.setSelection(workingSet != null);
		handleWorkingSetButtonSelection();
		if (workingSet != null && mruList.indexOf(workingSet.getName()) != -1) {
			mruList.setText(workingSet.getName());
		}
		handleWorkingSetChange();
	}
	
	private void handleWorkingSetChange() {
		if (workingSet != null) {
			// check any projects in the working set
			getViewer().setAllChecked(false);
			IAdaptable[] adaptables = workingSet.getElements();
			for (int i = 0; i < adaptables.length; i++) {
				IAdaptable adaptable = adaptables[i];
				Object adapted = adaptable.getAdapter(IResource.class);
				if (adapted != null) {
					// Can this code be generalized?
					IProject project = ((IResource)adapted).getProject();
					getViewer().setChecked(project, true);
				}
			}
		}
	}
	
	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		boolean useWorkingSet = workingSetButton.getSelection();
		settings.put(USE_WORKING_SET, useWorkingSet);
		if (useWorkingSet) {
			String selectedWorkingSet = mruList.getText();
			workingSet = (IWorkingSet) mruList.getData(selectedWorkingSet);
			// Add the selected working set to the MRU list
			if (workingSet != null) {
				PlatformUI.getWorkbench().getWorkingSetManager().addRecentWorkingSet(workingSet);
			}
			settings.put(SELECTED_WORKING_SET, selectedWorkingSet);
		}
		else {
			workingSet = null;
		}
		super.okPressed();
	}
	/**
	 * Sets the working set that should be selected in the most recently 
	 * used working set list.
	 * 
	 * @param workingSet the working set that should be selected.
	 * 	has to exist in the list returned by 
	 * 	org.eclipse.ui.IWorkingSetManager#getRecentWorkingSets().
	 * 	Must not be null.
	 */
	public void setWorkingSet(IWorkingSet workingSet) {
		this.workingSet = workingSet;

		if (workingSetButton != null && mruList != null) {
			initializeWorkingSet();
		}
	}
}
