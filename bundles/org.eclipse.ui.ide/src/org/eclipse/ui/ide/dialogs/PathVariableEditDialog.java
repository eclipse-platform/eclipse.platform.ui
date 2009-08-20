/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *******************************************************************************/
package org.eclipse.ui.ide.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.PathVariablesGroup;

public class PathVariableEditDialog extends SelectionDialog {

	private PathVariablesGroup pathVariablesGroup;

	/**
	 * Creates a path variable selection dialog.
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param variableType
	 *            the type of variables that are displayed in this dialog.
	 *            <code>IResource.FILE</code> and/or
	 *            <code>IResource.FOLDER</code> logically ORed together.
	 */
	public PathVariableEditDialog(Shell parentShell) {
		super(parentShell);
		setTitle(IDEWorkbenchMessages.PathVariableSelectionDialog_title);
		pathVariablesGroup = new PathVariablesGroup(false, IResource.FOLDER,
				null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	public void setProject(IProject receivingProject) {
		pathVariablesGroup.setProject(receivingProject);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.PATH_VARIABLE_SELECTION_DIALOG);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		pathVariablesGroup.createContents(dialogArea);
		return dialogArea;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		pathVariablesGroup.dispose();
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// Sets the dialog result to the selected path variable name(s).
		try {
			if (pathVariablesGroup.performOk()) {
				PathVariablesGroup.PathVariableElement[] selection = pathVariablesGroup
						.getSelection();
				String[] variableNames = new String[selection.length];

				for (int i = 0; i < selection.length; i++) {
					variableNames[i] = selection[i].name;
				}
				setSelectionResult(variableNames);
			} else {
				setSelectionResult(null);
			}
			super.okPressed();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
