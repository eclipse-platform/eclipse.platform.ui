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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.AddDeleteMoveListener;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * This prompter is used when a shared folder is moved into a shared project that is
 * shared with the same repository as the folder. The user is prompted to either manage
 * the folder or purge the cvs information.
 */
public class ManageFolderDialog extends Dialog {

	private ICVSFolder folder;
	private String projectName;
	private Button purgeButton;
	private Button manageButton;
	private boolean manage = false;
	
	public static void register() {
		AddDeleteMoveListener.setManageFolderPrompter(new AddDeleteMoveListener.IManageFolderPrompter() {
			public boolean promptToManageFolder(ICVSFolder mFolder) {
				return ManageFolderDialog.promptToManageFolder(mFolder);
			}
		});
	}
	
	public static void deregister() {
		AddDeleteMoveListener.setManageFolderPrompter(null);
	}
	
	public static boolean promptToManageFolder(final ICVSFolder mFolder) {
		final boolean[] result = new boolean[] { false };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Display display = Display.getCurrent();
				Shell shell = new Shell(display);
				try {
					ManageFolderDialog dialog = new ManageFolderDialog(shell, mFolder);
					dialog.open();
					result[0] = dialog.manage;
				} catch (CVSException e) {
					CVSUIPlugin.log(e);
				}
			}
		});
		return result[0];
	}
	
	/**
	 * Constructor for ManageFolderPrompter.
	 * @param parentShell
	 */
	public ManageFolderDialog(Shell parentShell, ICVSFolder folder) throws CVSException {
		super(parentShell);
		this.folder = folder;
		this.projectName = folder.getIResource().getProject().getName();
	}

	/*
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		getShell().setText(Policy.bind("ManageFolderDialog.title")); //$NON-NLS-1$
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(Policy.bind("ManageFolderDialog.description", folder.getName(), projectName)); //$NON-NLS-1$
		
		Composite actionGroup = new Composite(composite, SWT.NONE);
		GridData data = new GridData();
		data.horizontalSpan = 1;
		actionGroup.setLayoutData(data);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		actionGroup.setLayout(layout);

		purgeButton = createRadioButton(actionGroup, Policy.bind("ManageFolderDialog.purge"), 1); //$NON-NLS-1$
		manageButton = createRadioButton(actionGroup, Policy.bind("ManageFolderDialog.manage"), 1); //$NON-NLS-1$
		
		manageButton.setSelection(false);
		purgeButton.setSelection(!manageButton.getSelection());

		label = new Label(composite, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText(Policy.bind("ManageFolderDialog.note", folder.getName(), projectName)); //$NON-NLS-1$
				
		// set F1 help
		WorkbenchHelp.setHelp(composite, IHelpContextIds.MANAGE_FOLDER_DIALOG);	
		
		return composite;
	}

	/**
	 * Adds buttons to this dialog's button bar.
	 * <p>
	 * The <code>Dialog</code> implementation of this framework method adds 
	 * standard ok and cancel buttons using the <code>createButton</code>
	 * framework method. These standard buttons will be accessible from 
	 * <code>getCancelButton</code>, and <code>getOKButton</code>.
	 * Subclasses may override.
	 * </p>
	 *
	 * @param parent the button bar composite
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK button only (this operation can't be canceled)
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}
	
	/**
	 * Utility method to create a radio button
	 * 
	 * @param parent  the parent of the radio button
	 * @param label  the label of the radio button
	 * @param span  the number of columns to span
	 * @return the created radio button
	 */
	protected Button createRadioButton(Composite parent, String label, int span) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(label);
		GridData data = new GridData();
		data.horizontalSpan = span;
		button.setLayoutData(data);
		return button;
	}
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		manage = manageButton.getSelection();
		super.okPressed();
	}

}
