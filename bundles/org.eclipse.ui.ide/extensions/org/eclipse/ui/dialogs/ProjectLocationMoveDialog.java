/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation 
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *        font should be activated and used by other components.
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import java.net.URI;
import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.dialogs.FileStoreLocationArea;

/**
 * The ProjectLocationMoveDialog is the dialog used to select the location of a
 * project for moving.
 */
public class ProjectLocationMoveDialog extends SelectionDialog {
	private IProject project;

	private Label statusMessageLabel;

	private static String PROJECT_LOCATION_SELECTION_TITLE = IDEWorkbenchMessages.ProjectLocationSelectionDialog_selectionTitle;

	private boolean useDefaults = true;

	private FileStoreLocationArea locationArea;

	/**
	 * Create a ProjectLocationMoveDialog on the supplied project parented by
	 * the parentShell.
	 * 
	 * @param parentShell
	 * @param existingProject
	 */
	public ProjectLocationMoveDialog(Shell parentShell, IProject existingProject) {
		super(parentShell);
		setTitle(PROJECT_LOCATION_SELECTION_TITLE);
		this.project = existingProject;
		try {
			URI originalPath = this.getProject().getDescription()
					.getLocationURI();
			this.useDefaults = originalPath == null;
		} catch (CoreException exception) {
			// Leave it as the default.
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.SelectionDialog#setMessage(java.lang.String)
	 */
	public void setMessage(String message) {
		super.setMessage(message);
		if (statusMessageLabel != null) {
			if (message == null) {
				statusMessageLabel.setText("");//$NON-NLS-1$
				statusMessageLabel.setToolTipText("");//$NON-NLS-1$
				getOkButton().setEnabled(true);
			} else {
				statusMessageLabel.setForeground(JFaceColors
						.getErrorText(statusMessageLabel.getDisplay()));
				statusMessageLabel.setText(message);
				statusMessageLabel.setToolTipText(message);
				getOkButton().setEnabled(false);
			}
		}
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
	protected Control createContents(Composite parent) {
		Control content = super.createContents(parent);
		getOkButton().setEnabled(false);
		return content;
	}

	/*
	 * (non-Javadoc) Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectLocationGroup(composite);

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
	 * Creates the project location specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button useDefaultsButton = new Button(projectGroup, SWT.CHECK
				| SWT.RIGHT);
		useDefaultsButton
				.setText(IDEWorkbenchMessages.ProjectLocationSelectionDialog_useDefaultLabel);
		useDefaultsButton.setSelection(this.useDefaults);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserSpecifiedProjectLocationGroup(projectGroup, !this.useDefaults);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useDefaults = useDefaultsButton.getSelection();
				locationArea.setToDefault(useDefaults);
			}
		};
		useDefaultsButton.addSelectionListener(listener);
	}

	/**
	 * Creates the project location specification controls.
	 * 
	 * @return the parent of the widgets created
	 * @param projectGroup
	 *            the parent composite
	 * @param enabled -
	 *            sets the initial enabled state of the widgets
	 */
	private Composite createUserSpecifiedProjectLocationGroup(
			Composite projectGroup, boolean enabled) {

		locationArea = new FileStoreLocationArea(this, projectGroup,
				this.project);
		locationArea.setEnabled(enabled);

		// Scale the button based on the rest of the dialog
		setButtonLayoutData(locationArea.getBrowseButton());

		return projectGroup;

	}

	/**
	 * Get the project being manipulated.
	 */
	private IProject getProject() {
		return this.project;
	}

	/**
	 * The <code>ProjectLocationMoveDialog</code> implementation of this
	 * <code>Dialog</code> method builds a two element list - the first
	 * element is the project name and the second one is the location.
	 */
	protected void okPressed() {

		ArrayList list = new ArrayList();
		list.add(getProject().getName());
		if (useDefaults)
			list.add(Platform.getLocation().toString());
		else
			list.add(locationArea.getLocationValue());
		setResult(list);
		super.okPressed();
	}

}
