/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.ui.internal.ide.dialogs;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
/**
 * Dialog that asks the user to confirm a clean operation, and to configure
 * settings in relation to the clean. Clicking ok in the dialog will perform the
 * clean operation.
 * 
 * @since 3.0
 */
public class CleanDialog extends MessageDialog {
	private static final String QUESTION_TEXT = IDEWorkbenchMessages.getString("CleanDialog.buildCleanMessage"); //$NON-NLS-1$
	private Button allButton, selectedButton;
	private IProject[] selection;
	/**
	 * Creates a new clean dialog.
	 * 
	 * @param shell the parent shell for this dialog
	 * @param selection the currently selected projects (may be empty)
	 */
	public CleanDialog(Shell shell, IProject[] selection) {
		super(shell, IDEWorkbenchMessages.getString("CleanDialog.title"), null, QUESTION_TEXT, QUESTION, new String[]{ //$NON-NLS-1$
				IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 0);
		this.selection = selection;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			try {
				//batching changes ensures that autobuild runs after cleaning
				ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						doClean();
					}
				}, null);
			} catch (CoreException e) {
				ErrorDialog.openError(getShell(), null, null, e.getStatus());
			}
		}
		super.buttonPressed(buttonId);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createCustomArea(Composite parent) {
		Composite radioGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		radioGroup.setLayout(layout);
		radioGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		allButton = new Button(radioGroup, SWT.RADIO);
		allButton.setText(IDEWorkbenchMessages.getString("CleanDialog.cleanallbutton")); //$NON-NLS-1$
		allButton.setSelection(true);
		selectedButton = new Button(radioGroup, SWT.RADIO);
		selectedButton.setText(IDEWorkbenchMessages.getString("CleanDialog.cleanselectedbutton")); //$NON-NLS-1$
		if (selection.length == 0)
			selectedButton.setEnabled(false);
		return radioGroup;
	}
	/**
	 * Performs the actual clean operation
	 */
	protected void doClean() throws CoreException {
		if (allButton.getSelection())
			ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, null);
		else
			for (int i = 0; i < selection.length; i++)
				selection[i].build(IncrementalProjectBuilder.CLEAN_BUILD, null);
	}
}
