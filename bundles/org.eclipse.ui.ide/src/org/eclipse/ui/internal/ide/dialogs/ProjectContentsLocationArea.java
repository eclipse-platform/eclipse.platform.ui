/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.ide.dialogs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * ProjectContentsLocationArea is a convenience class for area that handle entry
 * of locations using URIs.
 * 
 * @since 3.2
 * 
 */
public class ProjectContentsLocationArea {
	/**
	 * IErrorMessageReporter is an interface for type that allow message
	 * reporting.
	 * 
	 */
	public interface IErrorMessageReporter {
		/**
		 * Report the error message
		 * 
		 * @param errorMessage
		 *            String or <code>null</code>. If the errorMessage is
		 *            null then clear any error state.
		 */
		public void reportError(String errorMessage);
	}

	private static String BROWSE_LABEL = IDEWorkbenchMessages.ProjectLocationSelectionDialog_browseLabel;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	private Label locationLabel;

	private Text locationPathField;

	private Button browseButton;

	private IErrorMessageReporter errorReporter;

	private String projectName = IDEResourceInfoUtils.EMPTY_STRING;

	private String userPath = IDEResourceInfoUtils.EMPTY_STRING;

	private Button useDefaultsButton;

	private IProject existingProject;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param reporter
	 * @param composite
	 * @param startProject
	 */
	public ProjectContentsLocationArea(IErrorMessageReporter reporter,
			Composite composite, IProject startProject) {

		errorReporter = reporter;
		projectName = startProject.getName();
		existingProject = startProject;

		boolean defaultEnabled = true;
		try {
			defaultEnabled = startProject.getDescription().getLocationURI() == null;
		} catch (CoreException e1) {
			// If we get a CoreException assume the default.
		}
		createContents(composite, defaultEnabled);
	}

	/**
	 * Create a new instance of a ProjectContentsLocationArea.
	 * 
	 * @param reporter
	 * @param composite
	 */
	public ProjectContentsLocationArea(IErrorMessageReporter reporter,
			Composite composite) {
		errorReporter = reporter;

		// If it is a new project always start enabled
		createContents(composite, true);
	}

	private void createContents(Composite composite, boolean defaultEnabled) {
		// project specification group
		Composite projectGroup = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		useDefaultsButton = new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton
				.setText(IDEWorkbenchMessages.ProjectLocationSelectionDialog_useDefaultLabel);
		useDefaultsButton.setSelection(defaultEnabled);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserEntryArea(projectGroup, defaultEnabled);

		useDefaultsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				boolean useDefaults = useDefaultsButton.getSelection();

				if (useDefaults) {
					userPath = locationPathField.getText();
					locationPathField.setText(getDefaultPathDisplayString());
				} else
					locationPathField.setText(userPath);
				setUserAreaEnabled(!useDefaults);
			}
		});
		setUserAreaEnabled(!defaultEnabled);
	}

	/**
	 * Return whether or not we are currently showing the default location for
	 * the project.
	 * 
	 * @return boolean
	 */
	public boolean isDefault() {
		return useDefaultsButton.getSelection();
	}

	/**
	 * Create the area for user entry.
	 * 
	 * @param composite
	 * @param defaultEnabled
	 */
	private void createUserEntryArea(Composite composite, boolean defaultEnabled) {
		// location label
		locationLabel = new Label(composite, SWT.NONE);
		locationLabel
				.setText(IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationLabel);

		// project location entry field
		locationPathField = new Text(composite, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);

		// browse button
		browseButton = new Button(composite, SWT.PUSH);
		browseButton.setText(BROWSE_LABEL);
		browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});

		if (defaultEnabled)// Will be null if it is not set
			locationPathField.setText(getDefaultPathDisplayString());
		else {
			if (existingProject == null)
				locationPathField.setText(IDEResourceInfoUtils.EMPTY_STRING);
			else {
				locationPathField.setText(existingProject.getLocation()
						.toString());
			}
		}

		locationPathField.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				errorReporter.reportError(checkValidLocation());
			}
		});
	}

	/**
	 * Return the path we are going to display. If it is a file URI then remove
	 * the file prefix.
	 * 
	 * @return String
	 */
	private String getDefaultPathDisplayString() {

		URI defaultURI = null;
		if (existingProject != null)
			defaultURI = existingProject.getLocationURI();

		// Handle files specially. Assume a file if there is no project to query
		if (defaultURI == null || defaultURI.getScheme().equals(FILE_SCHEME)) {
			return Platform.getLocation().append(projectName).toString();
		}
		return defaultURI.toString();

	}

	/**
	 * Set the enablement state of the receiver.
	 * 
	 * @param enabled
	 */
	private void setUserAreaEnabled(boolean enabled) {

		locationLabel.setEnabled(enabled);
		locationPathField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	/**
	 * Return the browse button. Usually referenced in order to set the layout
	 * data for a dialog.
	 * 
	 * @return Button
	 */
	public Button getBrowseButton() {
		return browseButton;
	}

	/**
	 * Open an appropriate directory browser
	 */
	private void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField
				.getShell());
		dialog
				.setMessage(IDEWorkbenchMessages.ProjectLocationSelectionDialog_directoryLabel);

		String dirName = getPathFromLocationField();
		if (!dirName.equals(IDEResourceInfoUtils.EMPTY_STRING)) {
			IFileInfo info = IDEResourceInfoUtils.getFileInfo(dirName);
			if (info.exists())
				dialog.setFilterPath(dirName);
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null)
			updateLocationField(selectedDirectory);
	}

	/**
	 * Update the location field based on the selected path.
	 * 
	 * @param selectedPath
	 */
	private void updateLocationField(String selectedPath) {
		locationPathField.setText(selectedPath);
	}

	/**
	 * Return the path on the location field.
	 * 
	 * @return String
	 */
	private String getPathFromLocationField() {
		URI fieldURI;
		try {
			fieldURI = new URI(locationPathField.getText());
		} catch (URISyntaxException e) {
			return locationPathField.getText();
		}
		return fieldURI.getPath();
	}

	/**
	 * Check if the entry in the widget location is valid. If it is valid return
	 * null. Otherwise return a string that indicates the problem.
	 * 
	 * @return String
	 */
	public String checkValidLocation() {
		
		if(isDefault())
			return null;

		String locationFieldContents = locationPathField.getText();
		if (locationFieldContents.length() == 0) {
			return (IDEWorkbenchMessages.WizardNewProjectCreationPage_projectLocationEmpty);
		}

		URI newPath = getLocationFieldURI();
		if (newPath == null) {
			return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
		}

		if (existingProject == null) {
			IPath projectPath = new Path(locationFieldContents);
			if (Platform.getLocation().isPrefixOf(projectPath)) {
				return IDEWorkbenchMessages.WizardNewProjectCreationPage_defaultLocationError;
			}

		} else {
			IStatus locationStatus = existingProject.getWorkspace()
					.validateProjectLocationURI(existingProject, newPath);

			if (!locationStatus.isOK())
				return locationStatus.getMessage();

			URI projectPath = existingProject.getLocationURI();
			if (projectPath != null && projectPath.equals(newPath)) {
				return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
			}
		}

		return null;
	}

	/**
	 * Get the URI for the location field if possible.
	 * 
	 * @return URI or <code>null</code> if it is not valid.
	 */
	private URI getLocationFieldURI() {

		String locationContents = locationPathField.getText();
		try {
			return new URI(locationContents);
		} catch (URISyntaxException e) {

			// See if it was acceptable as a file
			return new File(locationContents).toURI();
		}

	}

	/**
	 * Set the text to the default or clear it if not using the defaults.
	 * 
	 * @param useDefaults
	 * @param newName
	 *            the name of the project to use. If <code>null</code> use the
	 *            existing project name.
	 */
	public void updateProjectName(String newName) {
		projectName = newName;
		if (isDefault())
			locationPathField.setText(getDefaultPathDisplayString());

	}

	/**
	 * Return the location for the project. If we are using defaults then return
	 * the workspace root so that core creates it with default values.
	 * 
	 * @return String
	 */
	public String getProjectLocation() {
		if (isDefault())
			return Platform.getLocation().toString();
		return locationPathField.getText();
	}
}
