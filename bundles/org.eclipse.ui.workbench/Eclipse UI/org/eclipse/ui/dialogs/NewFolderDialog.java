/*
 * Copyright (c) 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
package org.eclipse.ui.dialogs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.*;
import org.eclipse.ui.internal.dialogs.PathVariableSelectionDialog;

/**
 * The NewFolderDialog is used to create a new folder.
 * The folder can optionally be linked to a file system folder.
 * <p>
 * NOTE: 
 * A linked folder can only be created at the project 
 * level. The widgets used to specify a link target are disabled 
 * if the supplied container is not a project.
 * </p>
 */
public class NewFolderDialog extends SelectionDialog {
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	// widgets
	private Text folderNameField;
	private Text linkTargetField;
	private Label statusMessageLabel;
	private Button browseButton;
	private Button variablesButton;

	private boolean createLink = false;

	IContainer container;
	
/**
 * Creates a NewFolderDialog
 * 
 * @param parentShell parent of the new dialog
 * @param container parent of the new folder
 */
public NewFolderDialog(Shell parentShell, IContainer container) {
	super(parentShell);
	this.container = container;
	setTitle(WorkbenchMessages.getString("NewFolderDialog.title")); //$NON-NLS-1$
}
/**
 * Checks the message and informs the user if it is not null. 
 *
 * @param message the error message to show. may be null.
 */
private void applyValidationResult(String message) {
	if (message == null) {
		statusMessageLabel.setText("");//$NON-NLS-1$
		getOkButton().setEnabled(true);
	} else {
		statusMessageLabel.setForeground(
			JFaceColors.getErrorText(
				statusMessageLabel.getDisplay()));
		statusMessageLabel.setText(message);
		getOkButton().setEnabled(false);
	}
}
/**
 * Checks whether the folder name and link location are valid.
 * 
 * @return null if the folder name and link location are valid.
 * 	a message that indicates the problem otherwise.
 */
private String checkValid() {
	String valid = checkValidName();
	if (valid != null)
		return valid;
	return checkValidLocation();
}
/**
 * Checks if the link location is valid. 
 * 
 * @return null if the link location is valid.
 * 	a message that indicates the problem otherwise.
 */
private String checkValidLocation() {
	if (createLink == false)
		return null;
	else {
		String linkTargetName = linkTargetField.getText();
		if (linkTargetName.equals("")) {//$NON-NLS-1$
			return(WorkbenchMessages.getString("NewFolderDialog.linkTargetEmpty")); //$NON-NLS-1$
		}
		else {
			IPath path = new Path("");//$NON-NLS-1$
			if (!path.isValidPath(linkTargetName)) {
				return WorkbenchMessages.getString("NewFolderDialog.linkTargetInvalid"); //$NON-NLS-1$
			}
		}
		File linkTargetFile = new Path(linkTargetName).toFile();
		if (linkTargetFile.exists() == false) {
			return WorkbenchMessages.getString("NewFolderDialog.linkTargetNonExistent"); //$NON-NLS-1$
		}
		IStatus locationStatus =
			container.getWorkspace().validateLinkLocation(
				container,
				new Path(linkTargetName));

		if (!locationStatus.isOK())
			return WorkbenchMessages.getString("NewFolderDialog.linkTargetLocationInvalid"); //$NON-NLS-1$

		return null;
	}
}
/**
 * Checks if the folder name is valid. 
 * 
 * @return null if the new folder name is valid.
 * 	a message that indicates the problem otherwise.
 */
private String checkValidName() {
	String name = folderNameField.getText();
	IWorkspace workspace = container.getWorkspace();
	IStatus nameStatus = workspace.validateName(name, IResource.FOLDER);
	if (!nameStatus.isOK())
		return nameStatus.getMessage();
	IFolder newFolder = container.getFolder(new Path(name));
	if (newFolder.exists()) {
		return WorkbenchMessages.format("NewFolderDialog.alreadyExists", new Object[] { name }); //$NON-NLS-1$
	}

	return null;
}
/* (non-Javadoc)
 * Method declared in Window.
 */
protected void configureShell(Shell shell) {
	super.configureShell(shell);
	WorkbenchHelp.setHelp(shell, IHelpContextIds.NEW_FOLDER_DIALOG);
}
/**
 * @see org.eclipse.jface.window.Window#create()
 */
public void create() {
	super.create();
	// initially disable the ok button since we don't preset the
	// folder name field
	getButton(IDialogConstants.OK_ID).setEnabled(false);
}
/* (non-Javadoc)
 * Method declared on Dialog.
 */
protected Control createDialogArea(Composite parent) {
	// page group
	Composite composite = (Composite) super.createDialogArea(parent);
	composite.setLayout(new GridLayout());
	composite.setLayoutData(new GridData(GridData.FILL_BOTH));

	createFolderNameGroup(composite);
	createLinkLocationGroup(composite);
	createValidationListener();
	
	// add in a label for status messages if required
	statusMessageLabel = new Label(composite, SWT.WRAP);
	statusMessageLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
	statusMessageLabel.setFont(parent.getFont());
	
	return composite;
}
/**
 * Creates the linked folder option button 
 *
 * @param parent the parent composite
 */
private void createLinkLocationGroup(Composite parent) {
	Composite folderGroup = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 3;
	folderGroup.setLayout(layout);
	folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	final Button createLinkButton =
		new Button(folderGroup, SWT.CHECK | SWT.RIGHT);
	createLinkButton.setText(WorkbenchMessages.getString("NewFolderDialog.createLinkButton")); //$NON-NLS-1$
	createLinkButton.setSelection(createLink);
	GridData data = new GridData();
	data.horizontalSpan = 3;
	createLinkButton.setLayoutData(data);
	createLinkButton.setEnabled(container instanceof IProject);
	
	createUserSpecifiedLinkLocationGroup(folderGroup, createLink);

	SelectionListener listener = new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			createLink = createLinkButton.getSelection();
			browseButton.setEnabled(createLink);
			variablesButton.setEnabled(createLink);
			linkTargetField.setEnabled(createLink);
			if (!createLink)
				linkTargetField.setText(""); //$NON-NLS-1$
			else
				applyValidationResult(checkValid());
		}
	};
	createLinkButton.addSelectionListener(listener);
}
/**
 * Creates the folder name specification controls.
 *
 * @param parent the parent composite
 */
private void createFolderNameGroup(Composite parent) {
	// project specification group
	Composite folderGroup = new Composite(parent,SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	folderGroup.setLayout(layout);
	folderGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

	// new project label
	Label folderLabel = new Label(folderGroup,SWT.NONE);
	folderLabel.setText(WorkbenchMessages.getString("NewFolderDialog.nameLabel"));	//$NON-NLS-1$

	// new project name entry field
	folderNameField = new Text(folderGroup, SWT.BORDER);
	GridData data = new GridData(GridData.FILL_HORIZONTAL);
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;
	folderNameField.setLayoutData(data);
}
/**
 * Creates a new folder with the given name and optionally linking to
 * the specified link target.
 * 
 * @param folderName name of the new folder
 * @param linkTargetName name of the link target folder. may be null.
 * @return IFolder the new folder
 */
private IFolder createNewFolder(String folderName, final String linkTargetName) {
	IWorkspaceRoot workspaceRoot = container.getWorkspace().getRoot(); 
	IPath folderPath = container.getFullPath().append(folderName);
	final IFolder folderHandle = workspaceRoot.getFolder(folderPath);
	
	WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
		public void execute(IProgressMonitor monitor) throws CoreException {
			try {
				monitor.beginTask(WorkbenchMessages.getString("NewFolderDialog.progress"), 2000); //$NON-NLS-1$
				if (linkTargetName == null)
					folderHandle.create(false, true, monitor);
				else
					folderHandle.createLink(new Path(linkTargetName), IResource.NONE, monitor);
				if (monitor.isCanceled())
					throw new OperationCanceledException();
			} finally {
				monitor.done();
			}
		}
	};

	try {
		new ProgressMonitorDialog(getShell()).run(true, true, operation);
	} catch (InterruptedException exception) {
		return null;
	} catch (InvocationTargetException exception) {
		if (exception.getTargetException() instanceof CoreException) {
			ErrorDialog.openError(
				getShell(),
				WorkbenchMessages.getString("NewFolderDialog.errorTitle"),  //$NON-NLS-1$
				null,	// no special message
				((CoreException) exception.getTargetException()).getStatus());
		}
		else {
			// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
			WorkbenchPlugin.log(MessageFormat.format(
				"Exception in {0}.createNewFolder(): {1}", 					//$NON-NLS-1$
				new Object[] {getClass().getName(), exception.getTargetException()}));
			MessageDialog.openError(
				getShell(), 
				WorkbenchMessages.getString("NewFolderDialog.errorTitle"), 	//$NON-NLS-1$
				WorkbenchMessages.format("NewFolderDialog.internalError", 	//$NON-NLS-1$
				new Object[] {exception.getTargetException().getMessage()}));
		}
		return null;
	}
	return folderHandle;
}
/**
 * Creates the link target location specification controls.
 *
 * @param folderGroup the parent composite
 * @param enabled sets the initial enabled state of the widgets
 */
private void createUserSpecifiedLinkLocationGroup(Composite folderGroup, boolean enabled) {
	Label fill = new Label(folderGroup, SWT.NONE);
	GridData data = new GridData();
	Button button = new Button(folderGroup, SWT.CHECK);
	data.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	button.dispose();
	fill.setLayoutData(data);
		
	// project location entry field
	linkTargetField = new Text(folderGroup, SWT.BORDER);
	data = new GridData();
	data.widthHint = SIZING_TEXT_FIELD_WIDTH;	
	linkTargetField.setLayoutData(data);
	linkTargetField.setEnabled(enabled);

	// browse button
	browseButton = new Button(folderGroup, SWT.PUSH);
	setButtonLayoutData(browseButton);
	browseButton.setText(WorkbenchMessages.getString("NewFolderDialog.browseButton")); //$NON-NLS-1$
	browseButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleLinkTargetBrowseButtonPressed();
		}
	});
	browseButton.setEnabled(enabled);

	fill = new Label(folderGroup, SWT.NONE);
	data = new GridData();
	data.horizontalSpan = 2;
	fill.setLayoutData(data);

	// variables button
	variablesButton = new Button(folderGroup, SWT.PUSH);
	setButtonLayoutData(variablesButton);
	variablesButton.setText(WorkbenchMessages.getString("NewFolderDialog.variablesButton")); //$NON-NLS-1$
	variablesButton.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent event) {
			handleVariablesButtonPressed();
		}
	});
	variablesButton.setEnabled(enabled);
}
/**
 * Create the listener that is used to validate the folder name and 
 * link location entered by the user
 */
private void createValidationListener() {
	Listener listener = new Listener() {
		public void handleEvent(Event event) {
			applyValidationResult(checkValid());
		}
	};
	linkTargetField.addListener(SWT.Modify, listener);
	folderNameField.addListener(SWT.Modify, listener);
}
/**
 * Opens a directory browser
 */
private void handleLinkTargetBrowseButtonPressed() {
	DirectoryDialog dialog = new DirectoryDialog(linkTargetField.getShell());
	dialog.setMessage(WorkbenchMessages.getString("NewFolderDialog.targetFolderLabel")); //$NON-NLS-1$
	
	String dirName = linkTargetField.getText();
	if (!dirName.equals("")) {//$NON-NLS-1$
		File path = new File(dirName);
		if (path.exists())
			dialog.setFilterPath(dirName);
	}

	String selectedDirectory = dialog.open();
	if (selectedDirectory != null)
		linkTargetField.setText(selectedDirectory);
}
/**
 * Opens a path variable selection dialog
 */
private void handleVariablesButtonPressed() {
	PathVariableSelectionDialog dialog = 
		new PathVariableSelectionDialog(
			linkTargetField.getShell(),
			IResource.FOLDER);
	
	if (dialog.open() == IDialogConstants.OK_ID) {
		String[] variableNames = (String[]) dialog.getResult();
		IPathVariableManager pathVariableManager = ResourcesPlugin.getWorkspace().getPathVariableManager();
		IPath path = pathVariableManager.getValue(variableNames[0]);
		
		if (path != null) {
			linkTargetField.setText(path.toOSString());
		}
	}
}
/**
 * Creates the folder using the name and link target entered
 * by the user.
 * Sets the dialog result to the created folder.  
 */
protected void okPressed() {
	String linkTarget = null; 

	if(createLink)
		linkTarget = linkTargetField.getText();

	IFolder folder = createNewFolder(folderNameField.getText(), linkTarget);
	setSelectionResult(new IFolder[] {folder});
	super.okPressed();
}
}