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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;

/**
 * FileStoreLocationArea is a convenience class for area that handle entry of
 * locations using URIs.
 * 
 * @since 3.2
 * 
 */
public class FileStoreLocationArea {

	private static String BROWSE_LABEL = IDEWorkbenchMessages.ProjectLocationSelectionDialog_browseLabel;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	private Label locationLabel;

	private Text locationPathField;

	private Button browseButton;

	private IProject project;

	private SelectionDialog selectionDialog;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param dialog
	 * @param composite
	 * @param startProject
	 */
	public FileStoreLocationArea(SelectionDialog dialog, Composite composite,
			IProject startProject) {

		selectionDialog = dialog;
		project = startProject;

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

		locationPathField.setText(project.getLocation().toString());

		locationPathField.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				selectionDialog.setMessage(checkValidLocation());
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

		URI defaultURI = project.getLocationURI();
		
		//Handle files specially
		if (defaultURI.getScheme().equals(FILE_SCHEME)) {
			return Platform.getLocation().append(project.getFullPath())
					.toString();
		}
		return defaultURI.toString();

	}

	/**
	 * Set the enablement state of the receiver.
	 * 
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {

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
	 */
	private String checkValidLocation() {

		String locationFieldContents = locationPathField.getText();
		if (locationFieldContents.length() == 0) {
			return (IDEWorkbenchMessages.WizardNewProjectCreationPage_projectLocationEmpty);
		}

		URI newPath = getLocationFieldURI();
		if (newPath == null) {
			return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
		}

		IStatus locationStatus = this.project.getWorkspace()
				.validateProjectLocationURI(this.project, newPath);

		if (!locationStatus.isOK())
			return locationStatus.getMessage();

		URI projectPath = project.getLocationURI();
		if (projectPath != null && projectPath.equals(newPath)) {
			return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
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
	 */
	public void setToDefault(boolean useDefaults) {
		if (useDefaults)
			locationPathField.setText(getDefaultPathDisplayString());
		else
			locationPathField.setText(project.getLocation().toString());
		setEnabled(!useDefaults);
	}

	/**
	 * Return the value of the location string.
	 * 
	 * @return String
	 */
	public String getLocationValue() {
		return locationPathField.getText();
	}
}
