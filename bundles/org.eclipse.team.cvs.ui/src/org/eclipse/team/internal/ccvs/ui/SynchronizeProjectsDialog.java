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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 */
public class SynchronizeProjectsDialog extends Dialog {
	
	private Button outgoingChangesButton;

	private static final String SYNC_OUTGOING_CHANGES = "SyncOutgoingChanges"; //$NON-NLS-1$

	IWorkingSet workingSet;
	boolean syncOutgoingChanges;
	
	// dialogs settings that are persistent between workbench sessions
	private IDialogSettings settings;
	private WorkingSetSelectionArea workingSetArea;

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
	public SynchronizeProjectsDialog(Shell parentShell) {
		super(parentShell);
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("SynchronizeProjectsDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("SynchronizeProjectsDialog");//$NON-NLS-1$
		}
	}

	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */	
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite)super.createDialogArea(parent);
	
		Font font = parent.getFont();
		composite.setFont(font);
		
		createLabel(composite, Policy.bind("SynchronizeProjectsDialog.description")); //$NON-NLS-1$
		
		// Create the checkbox to enable/disable working set use
		outgoingChangesButton = createCheckbox(composite, Policy.bind("SynchronizeProjectsDialog.syncOutgoingChanges")); //$NON-NLS-1$
		outgoingChangesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				syncOutgoingChanges = outgoingChangesButton.getSelection();
			}
		});
		if (settings != null) {
			syncOutgoingChanges = settings.getBoolean(SYNC_OUTGOING_CHANGES);
			outgoingChangesButton.setSelection(syncOutgoingChanges);
		}
			
		workingSetArea = new WorkingSetSelectionArea(this, settings);
		setWorkingSet(workingSet);
		workingSetArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				workingSet = (IWorkingSet)event.getNewValue();
			}
		});
		workingSetArea.createArea(composite);
		
		// F1 Help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.SYNCHRONIZE_PROJECTS_DIALOG);
		
		return composite;
	}

	/* (non-Javadoc)
	 * Method declared on Window.
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		WorkbenchHelp.setHelp(newShell, IHelpContextIds.PROJECT_SELECTION_DIALOG);
		newShell.setText(Policy.bind("SynchronizeProjectsDialog.title")); //$NON-NLS-1$
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
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		workingSet = workingSetArea.getWorkingSet();
		if (workingSet != null) {
			workingSetArea.useSelectedWorkingSet();
		}
		if (settings != null) {
			settings.put(SYNC_OUTGOING_CHANGES, outgoingChangesButton.getSelection());
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

		if (workingSetArea != null) {
			workingSetArea.setWorkingSet(workingSet);
		}
	}
	
	protected Button createCheckbox(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		button.setFont(parent.getFont());
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected Label createLabel(Composite composite, String text) {
		Label label = new Label(composite,SWT.NONE);
		if (text != null) {
			label.setText(text);
		} 
		label.setFont(composite.getFont());
		return label;
	}
	
	/**
	 * @return boolean
	 */
	public boolean isSyncOutgoingChanges() {
		return syncOutgoingChanges;
	}

}
