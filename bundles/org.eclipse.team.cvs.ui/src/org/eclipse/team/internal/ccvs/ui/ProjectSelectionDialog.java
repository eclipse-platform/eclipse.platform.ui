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
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * 
 */
public class ProjectSelectionDialog extends ListSelectionDialog {

	IWorkingSet workingSet;
	
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
	public ProjectSelectionDialog(Shell parentShell, Object input, IStructuredContentProvider contentProvider, ILabelProvider labelProvider, String message) {
		super(parentShell, input, contentProvider, labelProvider, message);
		
		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("ProjectSelectionDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("ProjectSelectionDialog");//$NON-NLS-1$
		}
	}

	/**
	 * Overrides method in Dialog
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(Composite)
	 */	
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		workingSetArea = new WorkingSetSelectionArea(this, settings);
		setWorkingSet(workingSet);
		workingSetArea.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				workingSet = (IWorkingSet)event.getNewValue();
				handleWorkingSetChange();
			}
		});
		workingSetArea.createArea(composite);
		return composite;
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
		workingSet = workingSetArea.getWorkingSet();
		if (workingSet != null) {
			workingSetArea.useSelectedWorkingSet();
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
}
