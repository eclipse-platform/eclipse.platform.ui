/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;

/**
 * Select the target location that will be the parent of the selected projects.
 * The behavior of the dialog differs between 1 project and multiple projects.
 * For one project, the location specified is the location of the project and
 * the project name can be modified. For multiple projects, it is the parent
 * location which is specified.
 */
public class TagetLocationSelectionDialog extends SelectionDialog {

	// widgets
	private Text projectNameField;
	private Text locationPathField;
	private Label locationLabel;
	private Label statusMessageLabel;
	private Button browseButton;
	
	// state
	private boolean useDefaults = true;
	private IProject[] targetProjects;
	private String newProjectName;
	private String targetLocation;
	
	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	
	/**
	 * Constructor.
	 * @param parentShell
	 */
	public TagetLocationSelectionDialog(Shell parentShell, String title, IProject targetProject) {
		this(parentShell, title, new IProject[] { targetProject });
	}
	
	/**
	 * Constructor.
	 * @param parentShell
	 */
	public TagetLocationSelectionDialog(Shell parentShell, String title, IProject[] targetProjects) {
		super(parentShell);
		setTitle(title);
		this.targetProjects = targetProjects;
		if (targetProjects.length == 1) newProjectName = targetProjects[0].getName();
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		// page group
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		if (isSingleCheckout())
			createProjectNameGroup(composite);
		createProjectLocationGroup(composite);

		//Add in a label for status messages if required
		statusMessageLabel = new Label(composite, SWT.NONE);
		statusMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));

		Dialog.applyDialogFont(parent);
		return composite;
	}
	
	/**
	 * Creates the project name specification controls.
	 *
	 * @param parent the parent composite
	 */
	private void createProjectNameGroup(Composite parent) {
		// project specification group
		Composite projectGroup = new Composite(parent,SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup,SWT.NONE);
		projectLabel.setText(Policy.bind("TargetLocationSelectionDialog.projectNameLabel")); //$NON-NLS-1$

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(data);
	
		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		projectNameField.setText(getNewProjectName());
		projectNameField.selectAll();
	
		createNameListener();
	
	}
	
	/**
	 * Create the listener that is used to validate the entries for the receiver
	 */
	private void createNameListener() {

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				newProjectName = projectNameField.getText();
				setLocationForSelection(false);
				applyValidationResult(checkValid());
			}
		};

		this.projectNameField.addListener(SWT.Modify, listener);
	}
	
	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	private void setLocationForSelection(boolean changed) {
		if (useDefaults) {
			IPath defaultPath = null;
			if (isSingleCheckout()) {
				try {
					defaultPath = getSingleProject().getDescription().getLocation();
				} catch (CoreException e) {
					// ignore
				}
				if (defaultPath == null) {
					defaultPath = Platform.getLocation().append(getSingleProject().getName());
				}
			} else {
				defaultPath = Platform.getLocation();
			}
			locationPathField.setText(defaultPath.toOSString());
			targetLocation = null;
		} else if (changed) {
			IPath location = null;
			try {
				location = this.targetProjects[0].getDescription().getLocation();
			} catch (CoreException e) {
				// ignore the exception
			}
			if (location == null) {
				targetLocation = null;
				locationPathField.setText(""); //$NON-NLS-1$
			} else {
				if (isSingleCheckout()) {
					targetLocation = location.toOSString();
				} else {
					targetLocation = location.removeLastSegments(1).toOSString();
				}
				locationPathField.setText(targetLocation);
			}
		}
	}
	
	/**
	 * Creates the project location specification controls.
	 *
	 * @param parent the parent composite
	 */
	private final void createProjectLocationGroup(Composite parent) {
	
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Button useDefaultsButton =
			new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
		useDefaultsButton.setText(Policy.bind("TargetLocationSelectionDialog.useDefaultLabel")); //$NON-NLS-1$
		useDefaultsButton.setSelection(this.useDefaults);
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		useDefaultsButton.setLayoutData(buttonData);

		createUserSpecifiedProjectLocationGroup(projectGroup, !this.useDefaults);

		SelectionListener listener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				useDefaults = useDefaultsButton.getSelection();
				browseButton.setEnabled(!useDefaults);
				locationPathField.setEnabled(!useDefaults);
				locationLabel.setEnabled(!useDefaults);
				setLocationForSelection(true);
			}
		};
		useDefaultsButton.addSelectionListener(listener);
	}
	
	/**
	 * Creates the project location specification controls.
	 *
	 * @return the parent of the widgets created
	 * @param projectGroup the parent composite
	 * @param enabled - sets the initial enabled state of the widgets
	 */
	private Composite createUserSpecifiedProjectLocationGroup(Composite projectGroup, boolean enabled) {
	
		// location label
		locationLabel = new Label(projectGroup, SWT.NONE);
		if (isSingleCheckout()) {
			locationLabel.setText(Policy.bind("TargetLocationSelectionDialog.locationLabel")); //$NON-NLS-1$
		} else {
			locationLabel.setText(Policy.bind("TargetLocationSelectionDialog.parentDirectoryLabel")); //$NON-NLS-1$
		}
		locationLabel.setEnabled(enabled);

		// project location entry field
		locationPathField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		locationPathField.setLayoutData(data);
		locationPathField.setEnabled(enabled);

		// browse button
		this.browseButton = new Button(projectGroup, SWT.PUSH);
		this.browseButton.setText(Policy.bind("TargetLocationSelectionDialog.browseLabel")); //$NON-NLS-1$
		this.browseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				handleLocationBrowseButtonPressed();
			}
		});
		this.browseButton.setEnabled(enabled);
		setButtonLayoutData(this.browseButton);

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		setLocationForSelection(true);
		createLocationListener();
		return projectGroup;

	}
	
	/**
	 *	Open an appropriate directory browser
	 */
	private void handleLocationBrowseButtonPressed() {
		DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
		if (isSingleCheckout()) {
			dialog.setMessage(Policy.bind("TargetLocationSelectionDialog.messageForSingle", newProjectName)); //$NON-NLS-1$
		} else {
			dialog.setMessage(Policy.bind("TargetLocationSelectionDialog.messageForMulti", new Integer(targetProjects.length).toString())); //$NON-NLS-1$
		}
	
		String dirName = locationPathField.getText();
		if (!dirName.equals("")) {//$NON-NLS-1$
			File path = new File(dirName);
			if (path.exists())
				dialog.setFilterPath(dirName);
		}

		String selectedDirectory = dialog.open();
		if (selectedDirectory != null) {
			if (targetProjects.length == 1) {
				locationPathField.setText(new Path(selectedDirectory).append(newProjectName).toOSString());
			} else {
				locationPathField.setText(new Path(selectedDirectory).toOSString());
			}
		}
		targetLocation = locationPathField.getText();
	}
	
	/**
	 * Method isSingleCheckout.
	 * @return boolean
	 */
	private boolean isSingleCheckout() {
		return targetProjects.length == 1;
	}

	private IProject getSingleProject() {
		if (newProjectName == null || newProjectName.length() == 0 || targetProjects[0].getName().equals(newProjectName))
			return targetProjects[0];
		else
			return ResourcesPlugin.getWorkspace().getRoot().getProject(newProjectName);
	}
	
	/**
	 * Create the listener that is used to validate the location entered by the iser
	 */
	private void createLocationListener() {

		Listener listener = new Listener() {
			public void handleEvent(Event event) {
				applyValidationResult(checkValid());
			}
		};

		this.locationPathField.addListener(SWT.Modify, listener);
	}
	
	/**
	 * Check the message. If it is null then continue otherwise inform the user via the
	 * status value and disable the OK.
	 * @param message - the error message to show if it is not null.
	 */
	private void applyValidationResult(String errorMsg) {

		if (errorMsg == null) {
			statusMessageLabel.setText("");//$NON-NLS-1$
			getOkButton().setEnabled(true);
		} else {
			statusMessageLabel.setForeground(
				JFaceColors.getErrorText(
					statusMessageLabel.getDisplay()));
			statusMessageLabel.setText(errorMsg);
			getOkButton().setEnabled(false);
		}
	}
	/**
	 * Check whether the entries are valid. If so return null. Otherwise
	 * return a string that indicates the problem.
	 */
	private String checkValid() {
		if (isSingleCheckout()) {
			String valid = checkValidName();
			if (valid != null)
				return valid;
		}
		return checkValidLocation();
	}
	/**
	 * Check if the entry in the widget location is valid. If it is valid return null. Otherwise
	 * return a string that indicates the problem.
	 */
	private String checkValidLocation() {

		if (useDefaults) {
			targetLocation = null;
			return null;
		} else {
			targetLocation = locationPathField.getText();
			if (targetLocation.equals("")) {//$NON-NLS-1$
				return(Policy.bind("TagetLocationSelectionDialog.locationEmpty")); //$NON-NLS-1$
			}
			else{
				IPath path = new Path("");//$NON-NLS-1$
				if (!path.isValidPath(targetLocation)) {
					return Policy.bind("TagetLocationSelectionDialog.invalidLocation"); //$NON-NLS-1$
				}
			}

			if (isSingleCheckout()) {
				IStatus locationStatus =
					ResourcesPlugin.getWorkspace().validateProjectLocation(
						getSingleProject(),
						new Path(targetLocation));
	
				if (!locationStatus.isOK())
					return locationStatus.getMessage();
			} else {
				for (int i = 0; i < targetProjects.length; i++) {
					ResourcesPlugin.getWorkspace().validateProjectLocation(
						targetProjects[i],
						new Path(targetLocation).append(targetProjects[i].getName()));
				}
			}

			return null;
		}
	}
	/**
	 * Check if the entries in the widget are valid. If they are return null otherwise
	 * return a string that indicates the problem.
	 */
	private String checkValidName() {

		newProjectName = this.projectNameField.getText();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IStatus nameStatus = workspace.validateName(newProjectName, IResource.PROJECT);
		if (!nameStatus.isOK())
			return nameStatus.getMessage();
//		IProject newProject = workspace.getRoot().getProject(newProjectName);
//		if (newProject.exists()) {
//			return Policy.bind("TagetLocationSelectionDialog.alreadyExists", newProjectName); //$NON-NLS-1$
//		}

		return null;
	}
	
	/**
	 * @return String
	 */
	public String getNewProjectName() {
		return newProjectName;
	}

	/**
	 * @return String
	 */
	public String getTargetLocation() {
		return targetLocation;
	}

}
