/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog font
 *     should be activated and used by other components.
 *     Francis Upton <francisu@ieee.org> - Fix for Bug 164695
 *     		[Workbench] Project copy doesn't validate location and uses invalid location as default
 *     Oakland Software Incorporated (Francis Upton) <francisu@ieee.org>
 *		    Bug 224997 [Workbench] Impossible to copy project
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

/**
 * The ProjectLocationSelectionDialog is the dialog used to select the name and
 * location of a project for copying.
 */
public class ProjectLocationSelectionDialog extends SelectionStatusDialog {
	// widgets
	private Text projectNameField;

	private IProject project;

	private ProjectContentsLocationArea locationArea;

	private static String PROJECT_NAME_LABEL = IDEWorkbenchMessages.ProjectLocationSelectionDialog_nameLabel;

	private static String PROJECT_LOCATION_SELECTION_TITLE = IDEWorkbenchMessages.ProjectLocationSelectionDialog_selectionTitle;

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	/**
	 * Create a ProjectLocationSelectionDialog on the supplied project parented
	 * by the parentShell.
	 * 
	 * @param parentShell
	 * @param existingProject
	 */
	public ProjectLocationSelectionDialog(Shell parentShell,
			IProject existingProject) {
		super(parentShell);
		setTitle(PROJECT_LOCATION_SELECTION_TITLE);
		setStatusLineAboveButtons(true);
		project = existingProject;
	}

	/**
	 * Check the message. If it is null then continue otherwise inform the user
	 * via the status value and disable the OK.
	 * 
	 * @param errorMsg
	 *            the error message to show if it is not <code>null</code>
	 */
	private void applyValidationResult(String errorMsg, boolean infoOnly) {
		int code;
		boolean allowFinish = false;

		if (errorMsg == null) {
			code = IStatus.OK;
			errorMsg = ""; //$NON-NLS-1$
			allowFinish = true;
		} else if (infoOnly) {
			code = IStatus.OK;
		} else {
			code = IStatus.ERROR;
		}

		updateStatus(new Status(code, IDEWorkbenchPlugin.IDE_WORKBENCH, code,
				errorMsg, null));
		if (getOkButton() != null)
			getOkButton().setEnabled(allowFinish);
	}

	/**
	 * Check whether the entries are valid. If so return null. Otherwise return
	 * a string that indicates the problem.
	 */
	private String checkValid() {
		String valid = checkValidName();
		if (valid != null) {
			return valid;
		}
		return locationArea.checkValidLocation();
	}

	/**
	 * Check if the entries in the widget are valid. If they are return null
	 * otherwise return a string that indicates the problem.
	 */
	private String checkValidName() {

		String name = this.projectNameField.getText();
		IWorkspace workspace = getProject().getWorkspace();
		IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			return nameStatus.getMessage();
		}
		IProject newProject = workspace.getRoot().getProject(name);
		if (newProject.exists()) {
			return NLS.bind(
					IDEWorkbenchMessages.CopyProjectAction_alreadyExists, name);
		}

		return null;
	}

	/**
	 * The <code>ProjectLocationSelectionDialog</code> implementation of this
	 * <code>SelectionStatusDialog</code> method builds a two element list -
	 * the first element is the project name and the second one is the location.
	 */
	protected void computeResult() {

		ArrayList list = new ArrayList();
		list.add(this.projectNameField.getText());
		list.add(locationArea.getProjectLocation());
		setResult(list);
	}

	/*
	 * (non-Javadoc) Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell,
				IIDEHelpContextIds.PROJECT_LOCATION_SELECTION_DIALOG);
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectNameGroup(composite);
		locationArea = new ProjectContentsLocationArea(getErrorReporter(),
				composite);
		locationArea.updateProjectName(projectNameField.getText());
		return composite;
	}

	/**
	 * Create the listener that is used to validate the entries for the receiver
	 */
	private void createNameListener() {

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				setLocationForSelection();
				applyValidationResult(checkValid(), false);
			}
		};

		this.projectNameField.addListener(SWT.Modify, listener);
	}

	/**
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private void createProjectNameGroup(Composite parent) {
		Font font = parent.getFont();
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setFont(font);
		projectLabel.setText(PROJECT_NAME_LABEL);

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);
		projectNameField.setFont(font);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		projectNameField.setText(getCopyNameFor(getProject().getName()));
		projectNameField.selectAll();

		createNameListener();

	}

	/**
	 * Generates a new name for the project that does not have any collisions.
	 */
	private String getCopyNameFor(String projectName) {

		IWorkspace workspace = getProject().getWorkspace();
		if (!workspace.getRoot().getProject(projectName).exists()) {
			return projectName;
		}

		int counter = 1;
		while (true) {
			String nameSegment;
			if (counter > 1) {
				nameSegment = NLS.bind(
						IDEWorkbenchMessages.CopyProjectAction_copyNameTwoArgs,
						new Integer(counter), projectName);
			} else {
				nameSegment = NLS.bind(
						IDEWorkbenchMessages.CopyProjectAction_copyNameOneArg,
						projectName);
			}

			if (!workspace.getRoot().getProject(nameSegment).exists()) {
				return nameSegment;
			}

			counter++;
		}

	}

	/**
	 * Get the project being manipulated.
	 */
	private IProject getProject() {
		return this.project;
	}

	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	private void setLocationForSelection() {
		locationArea.updateProjectName(projectNameField.getText());
	}

	/**
	 * Get an error reporter for the receiver.
	 * 
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter() {
		return new IErrorMessageReporter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
			 */
			public void reportError(String errorMessage, boolean infoOnly) {
				setMessage(errorMessage);
				applyValidationResult(errorMessage, infoOnly);
			}
		};
	}
}
