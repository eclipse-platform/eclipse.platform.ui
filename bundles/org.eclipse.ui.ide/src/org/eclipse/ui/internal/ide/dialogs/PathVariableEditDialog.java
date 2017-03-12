/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Serge Beauchamp (Freescale Semiconductor) - [229633] Project Path Variable Support
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * A dialog that allows a user to browse, edit, add, and remove path variables
 * for a given project.
 *
 * @since 3.6
 */
public class PathVariableEditDialog extends SelectionDialog {

	private PathVariablesGroup pathVariablesGroup;

	/**
	 * Creates a path variable selection dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	public PathVariableEditDialog(Shell parentShell) {
		super(parentShell);
		setTitle(IDEWorkbenchMessages.PathVariableSelectionDialog_title);
		pathVariablesGroup = new PathVariablesGroup(false, IResource.FOLDER,
				null);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
	}

	@Override
	public boolean close() {
		pathVariablesGroup.dispose();
		return super.close();
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.PATH_VARIABLE_SELECTION_DIALOG);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// create composite
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		pathVariablesGroup.createContents(dialogArea);
		return dialogArea;
	}

	@Override
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
			IDEWorkbenchPlugin.log(t.getMessage(), t);
		}
	}

	/**
	 * Sets the project for which variables are being edited
	 *
	 * @param resource
	 *            The project whose variables are being edited
	 */
	public void setResource(IResource resource) {
		pathVariablesGroup.setResource(resource);
	}
}
