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
package org.eclipse.ui.internal.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.dialogs.IWorkingSetSelectionDialog;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * A filter dialog presents a list of filter patterns and
 * most recently used working set.
 * Zero or more filters and an optional working set can be
 * selected by the user.
 * 
 * @since 2.0
 */
public class FilterDialog extends ListSelectionDialog {
	static final int SELECT_ID = IDialogConstants.CLIENT_ID + 1;

	private Button workingSetButton;
	private Combo mruList;
	private Button selectButton;
	private IWorkingSet workingSet;
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
	public FilterDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider, ILabelProvider labelProvider, String message) {
		super(parentShell, input, contentProvider, labelProvider, message);
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
		workingSetButton.setText(WorkbenchMessages.getString("FilterDialog.workingSet")); //$NON-NLS-1$
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
		selectButton = createButton(group, SELECT_ID, WorkbenchMessages.getString("FilterDialog.workingSetOther"), false); //$NON-NLS-1$

		initializeMru();
		initializeWorkingSet();
		return composite;
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
		IWorkingSetSelectionDialog dialog = WorkbenchPlugin.getDefault().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false);
		IWorkingSetManager workingSetManager = WorkbenchPlugin.getDefault().getWorkingSetManager();
		IWorkingSet workingSet = workingSetManager.getWorkingSet(mruList.getText());

		if (workingSet != null) {
			dialog.setSelection(new IWorkingSet[]{workingSet});
		}
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
		mruList.setEnabled(workingSetButton.getSelection());
		selectButton.setEnabled(workingSetButton.getSelection());
	}
	/**
	 * Populates the most recently used working set list with MRU items from
	 * the working set manager as well as adds an item to enable selection of
	 * a working set not in the MRU list.
	 */
	private void initializeMru() {
		IWorkingSet[] workingSets = WorkbenchPlugin.getDefault().getWorkingSetManager().getRecentWorkingSets();
		
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
	}
	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		if (workingSetButton.getSelection()) {
			workingSet = (IWorkingSet) mruList.getData(mruList.getText());
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