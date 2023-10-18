/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *        font should be activated and used by other components.
 *    Oakland Software Incorporated (Francis Upton) <francisu@ieee.org>
 *		  Bug 224997 [Workbench] Impossible to copy project
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

/**
 * The ProjectLocationMoveDialog is the dialog used to select the location of a
 * project for moving.
 */
public class ProjectLocationMoveDialog extends SelectionDialog {
	private IProject project;

	private Label statusMessageLabel;

	private static String PROJECT_LOCATION_SELECTION_TITLE = IDEWorkbenchMessages.ProjectLocationSelectionDialog_selectionTitle;

	private ProjectContentsLocationArea locationArea;

	/**
	 * Create a ProjectLocationMoveDialog on the supplied project parented by the
	 * parentShell.
	 *
	 * @param parentShell     dialogs parent shell
	 * @param existingProject project to be moved
	 */
	public ProjectLocationMoveDialog(Shell parentShell, IProject existingProject) {
		super(parentShell);
		setTitle(PROJECT_LOCATION_SELECTION_TITLE);
		this.project = existingProject;
	}

	@Override
	public void setMessage(String message) {
		super.setMessage(message);
		if (statusMessageLabel != null) {
			if (message == null) {
				statusMessageLabel.setText("");//$NON-NLS-1$
				statusMessageLabel.setToolTipText("");//$NON-NLS-1$
				getOkButton().setEnabled(true);
			} else {
				statusMessageLabel.setForeground(JFaceColors.getErrorText(statusMessageLabel.getDisplay()));
				statusMessageLabel.setText(message);
				statusMessageLabel.setToolTipText(message);
				getOkButton().setEnabled(false);
			}
		}
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IIDEHelpContextIds.PROJECT_LOCATION_SELECTION_DIALOG);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control content = super.createContents(parent);
		getOkButton().setEnabled(false);
		return content;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite, this.project);

		// Scale the button based on the rest of the dialog
		setButtonLayoutData(locationArea.getBrowseButton());

		// Add in a label for status messages if required
		statusMessageLabel = new Label(composite, SWT.WRAP);
		statusMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
		statusMessageLabel.setFont(parent.getFont());
		// Make it two lines.
		statusMessageLabel.setText(" \n "); //$NON-NLS-1$

		applyDialogFont(composite);
		return composite;
	}

	/**
	 * Get an error reporter for the receiver.
	 *
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter() {
		return (errorMessage, notError) -> setMessage(errorMessage);
	}

	/**
	 * Get the project being manipulated.
	 */
	private IProject getProject() {
		return this.project;
	}

	/**
	 * The <code>ProjectLocationMoveDialog</code> implementation of this
	 * <code>Dialog</code> method builds a two element list - the first element is
	 * the project name and the second one is the location.
	 */
	@Override
	protected void okPressed() {

		List<String> list = new ArrayList<>();
		list.add(getProject().getName());
		list.add(locationArea.getProjectLocation());
		setResult(list);
		super.okPressed();
	}

}
