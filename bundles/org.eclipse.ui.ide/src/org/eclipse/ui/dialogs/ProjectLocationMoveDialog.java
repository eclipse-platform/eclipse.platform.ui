package org.eclipse.ui.dialogs;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *    IBM Corporation - initial API and implementation 
 *    Sebastian Davids <sdavids@gmx.de> - Fix for bug 19346 - Dialog
 *        font should be activated and used by other components.
******************************************************************************/ 
import org.eclipse.core.runtime.*;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.help.*;
import org.eclipse.ui.internal.*;
import java.io.File;
import java.util.ArrayList;

/**
 * The ProjectLocationMoveDialog is the dialog used to select the
 * location of a project for moving.
 */
public class ProjectLocationMoveDialog extends SelectionDialog {
	private IProject project;
	private IPath originalPath;
	
	// widgets
	private Text locationPathField;
	private Label locationLabel;
	private Label statusMessageLabel;
	private Button browseButton;

	private static String LOCATION_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.locationLabel"); //$NON-NLS-1$
	private static String BROWSE_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.browseLabel"); //$NON-NLS-1$
	private static String DIRECTORY_DIALOG_LABEL = WorkbenchMessages.getString("ProjectLocationSelectionDialog.directoryLabel"); //$NON-NLS-1$
	private static String INVALID_LOCATION_MESSAGE = WorkbenchMessages.getString("ProjectLocationSelectionDialog.locationError"); //$NON-NLS-1$
	private static String PROJECT_LOCATION_SELECTION_TITLE = WorkbenchMessages.getString("ProjectLocationSelectionDialog.selectionTitle"); //$NON-NLS-1$

	// constants
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private boolean useDefaults = true;

/**
 * Create a ProjectLocationMoveDialog on the supplied project parented by the parentShell.
 * @param parentShell
 * @param existingProject
 */
public ProjectLocationMoveDialog(Shell parentShell, IProject existingProject) {
	super(parentShell);
	setTitle(PROJECT_LOCATION_SELECTION_TITLE);
	this.project = existingProject;
	try {
		this.originalPath = this.getProject().getDescription().getLocation();
		this.useDefaults = this.originalPath == null;
	} catch (CoreException exception) {
		// Leave it as the default.
	}
}
/**
 * Check the message. If it is null then continue otherwise inform the user via the
 * status value and disable the OK.
 * @param message - the error message to show if it is not null.
 */
private void applyValidationResult(String errorMsg) {

	if (errorMsg == null) {
		statusMessageLabel.setText("");//$NON-NLS-1$
		statusMessageLabel.setToolTipText("");//$NON-NLS-1$
		getOkButton().setEnabled(true);
	} else {
		statusMessageLabel.setForeground(
			JFaceColors.getErrorText(
				statusMessageLabel.getDisplay()));
		statusMessageLabel.setText(errorMsg);
		statusMessageLabel.setToolTipText(errorMsg);
		getOkButton().setEnabled(false);
	}
}
/**
 * Check whether the entries are valid. If so return null. Otherwise
 * return a string that indicates the problem.
 */
private String checkValid() {
	return checkValidLocation();
}
/**
 * Check if the entry in the widget location is valid. If it is valid return null.
 * Otherwise return a string that indicates the problem.
 */
private String checkValidLocation() {

	if (useDefaults) {
		if (this.originalPath == null)
			return INVALID_LOCATION_MESSAGE;
		return null;
	}
	else {
		String locationFieldContents = locationPathField.getText();
		if (locationFieldContents.equals("")) {//$NON-NLS-1$
			return(WorkbenchMessages.getString("WizardNewProjectCreationPage.projectLocationEmpty")); //$NON-NLS-1$
		}
		else{
			IPath path = new Path("");//$NON-NLS-1$
			if (!path.isValidPath(locationFieldContents)) {
				return INVALID_LOCATION_MESSAGE;
			}
		}

		Path newPath = new Path(locationFieldContents);
		IStatus locationStatus =
			this.project.getWorkspace().validateProjectLocation(
				this.project,
				newPath);

		if (!locationStatus.isOK())
			return locationStatus.getMessage();

		if (originalPath != null && originalPath.equals(newPath)) {
			return INVALID_LOCATION_MESSAGE;
		}
		
		return null;
	}
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.PROJECT_LOCATION_SELECTION_DIALOG);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createContents(Composite parent) {
	Control content = super.createContents(parent);
	getOkButton().setEnabled(false);
	return content;
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// page group
	Composite composite = (Composite) super.createDialogArea(parent);

	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	createProjectLocationGroup(composite);

	//Add in a label for status messages if required
	statusMessageLabel = new Label(composite, SWT.WRAP);
	statusMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
	statusMessageLabel.setFont(parent.getFont());
	//Make it two lines.
	statusMessageLabel.setText(" \n "); //$NON-NLS-1$

	return composite;
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
 * Creates the project location specification controls.
 *
 * @param parent the parent composite
 */
private final void createProjectLocationGroup(Composite parent) {
	Font font = parent.getFont();
	// project specification group
	Composite projectGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	projectGroup.setLayout(layout);
	projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	projectGroup.setFont(font);
	
	final Button useDefaultsButton =
		new Button(projectGroup, SWT.CHECK | SWT.RIGHT);
	useDefaultsButton.setFont(font);
	useDefaultsButton.setText(WorkbenchMessages.getString("ProjectLocationSelectionDialog.useDefaultLabel")); //$NON-NLS-1$
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
			setLocationForSelection();
			if (!useDefaults) {
				if (originalPath != null)
					locationPathField.setText(originalPath.toOSString());
				else
					locationPathField.setText(""); //$NON-NLS-1$
			}
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
	Font font = projectGroup.getFont();
	// location label
	locationLabel = new Label(projectGroup, SWT.NONE);
	locationLabel.setFont(font);
	locationLabel.setText(LOCATION_LABEL);
	locationLabel.setEnabled(enabled);

	// project location entry field
	locationPathField = new Text(projectGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	locationPathField.setLayoutData(data);
	locationPathField.setFont(font);
	locationPathField.setEnabled(enabled);

	// browse button
	this.browseButton = new Button(projectGroup, SWT.PUSH);
	this.browseButton.setFont(font);
	this.browseButton.setText(BROWSE_LABEL);
	this.browseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleLocationBrowseButtonPressed();
		}
	});
	this.browseButton.setEnabled(enabled);
	setButtonLayoutData(this.browseButton);

	// Set the initial value first before listener
	// to avoid handling an event during the creation.
	if (originalPath == null)
		setLocationForSelection();
	else
		locationPathField.setText(originalPath.toOSString());

	createLocationListener();
	return projectGroup;

}
/**
 * Get the project being manipulated.
 */
private IProject getProject() {
	return this.project;
}
/**
 *	Open an appropriate directory browser
 */
private void handleLocationBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
	dialog.setMessage(DIRECTORY_DIALOG_LABEL);
	
	String dirName = locationPathField.getText();
	if (!dirName.equals("")) {//$NON-NLS-1$
		File path = new File(dirName);
		if (path.exists())
			dialog.setFilterPath(dirName);
	}

	String selectedDirectory = dialog.open();
	if (selectedDirectory != null)
		locationPathField.setText(selectedDirectory);
}
/**
 * The <code>ProjectLocationMoveDialog</code> implementation of this 
 * <code>Dialog</code> method builds a two element list - the first element
 * is the project name and the second one is the location.
 */
protected void okPressed() {
	
	ArrayList list = new ArrayList();
	list.add(getProject().getName());
	if(useDefaults)
		list.add(Platform.getLocation().toString());
	else
		list.add(this.locationPathField.getText());
	setResult(list);
	super.okPressed();
}
/**
 * Set the location to the default location if we are set to useDefaults.
 */
private void setLocationForSelection() {
	if (useDefaults) {
		IPath defaultPath = Platform.getLocation().append(getProject().getName());
		locationPathField.setText(defaultPath.toOSString());
	}
}
}
