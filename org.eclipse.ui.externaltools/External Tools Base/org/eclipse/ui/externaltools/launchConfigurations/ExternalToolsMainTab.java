package org.eclipse.ui.externaltools.launchConfigurations;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsImages;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;

public class ExternalToolsMainTab extends AbstractLaunchConfigurationTab {

	protected Text locationField;
	protected Text workDirectoryField;
	protected Text descriptionField;
	private Button fileLocationButton;
	private Button workspaceLocationButton;
	private Button fileWorkingDirectoryButton;
	private Button workspaceWorkingDirectoryButton;
	
	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			updateLaunchConfigurationDialog();
		}
	};

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite mainComposite = new Composite(parent, SWT.NONE);
		setControl(mainComposite);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createSpacer(parent);
		createDescriptionComponent(mainComposite);
	}
	
	/**
	 * Creates the controls needed to edit the description
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createDescriptionComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Description:");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		descriptionField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		descriptionField.setLayoutData(data);
	}
	
	/**
	 * Creates the controls needed to edit the location
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Location: ");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		locationField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.verticalSpan= 2;
		locationField.setLayoutData(data);
		
		workspaceLocationButton= createPushButton(parent, "Browse Workspace...", null);
		workspaceLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkspaceLocationButtonSelected();
			}
		});
		fileLocationButton= createPushButton(parent, "Browse File System...", null);
		fileLocationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleLocationButtonSelected();
			}
		});
	}
	
	/**
	 * Creates the controls needed to edit the working directory
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Working Directory:");
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		workDirectoryField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		data.verticalSpan= 2;
		workDirectoryField.setLayoutData(data);
		
		workspaceWorkingDirectoryButton= createPushButton(parent, "Browse Workspace...", null);
		workspaceWorkingDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleWorkspaceWorkingDirectoryButtonSelected();
			}
		});
		fileWorkingDirectoryButton= createPushButton(parent, "Browse File System...", null);
		fileWorkingDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent evt) {
				handleFileWorkingDirectoryButtonSelected();
			}
		});
		createSpacer(parent);
	}
	
	/**
	 * Creates a vertical space between controls.
	 */
	protected void createSpacer(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		updateLocation(configuration);
		updateWorkingDirectory(configuration);
		updateDescription(configuration);
	}
	/**
	 * Method updateDescription.
	 * @param configuration
	 */
	private void updateDescription(ILaunchConfiguration configuration) {
		String desc= "";
		try {
			desc= configuration.getAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		descriptionField.setText(desc);
		descriptionField.addModifyListener(modifyListener);
	}
	
	private void updateWorkingDirectory(ILaunchConfiguration configuration) {
		String workingDir= "";
		try {
			workingDir= configuration.getAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		workDirectoryField.setText(workingDir);
		workDirectoryField.addModifyListener(modifyListener);
		
	}
	
	private void updateLocation(ILaunchConfiguration configuration) {
		String location= "";
		try {
			location= configuration.getAttribute(IExternalToolConstants.ATTR_LOCATION, "");
		} catch (CoreException ce) {
			ExternalToolsPlugin.getDefault().log("Error reading configuration", ce);
		}
		locationField.setText(location);
		locationField.addModifyListener(modifyListener);
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String location= locationField.getText().trim();
		if (location.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_LOCATION, location);
		}
		
		String workingDirectory= workDirectoryField.getText().trim();
		if (workingDirectory.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, workingDirectory);
		}
		
		String desc= descriptionField.getText().trim();
		if (desc.length() == 0) {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, (String)null);
		} else {
			configuration.setAttribute(IExternalToolConstants.ATTR_TOOL_DESCRIPTION, desc);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return "Main";
	}
	
	/**
	 * @see ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		setMessage(null);
		return validateLocation() && validateWorkDirectory();
	}
	
	/**
	 * Validates the content of the location field.
	 */
	protected boolean validateLocation() {
		String value = locationField.getText().trim();
		if (value.length() < 1) {
			setErrorMessage("External tool location cannot be empty");
			setMessage(null);
			return false;
		}

		// Translate field contents to the actual file location so we
		// can check to ensure the file actually exists.
		MultiStatus multiStatus = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		value = ToolUtil.expandFileLocation(value, ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
		if (!multiStatus.isOK()) {
			IStatus[] children = multiStatus.getChildren();
			if (children.length > 0) {
				setErrorMessage(children[0].getMessage());
				setMessage(null);
			}
			return false;
		}
		
		File file = new File(value);
		if (!file.exists()) { // The file does not exist.
			setErrorMessage("External tool location does not exist");
			return false;
		}
		return true;
	}

	/**
	 * Validates the content of the working directory field.
	 */
	protected boolean validateWorkDirectory() {
		
		String value = workDirectoryField.getText().trim();
		if (value.length() > 0) {
			// Translate field contents to the actual directory location so we
			// can check to ensure the directory actually exists.
			MultiStatus multiStatus = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			value = ToolUtil.expandDirectoryLocation(value, ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
			if (!multiStatus.isOK()) {
				IStatus[] children = multiStatus.getChildren();
				if (children.length > 0) {
					setErrorMessage(children[0].getMessage());
				}
				return false;
			}
				
			File file = new File(value);
			if (!file.exists()) { // The directory does not exist.
				setErrorMessage("External tool working directory does not exist or is invalid");
				return false;
			}
		}
		return true;
	}
	
	private void handleLocationButtonSelected() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(locationField.getText());
		String text= fileDialog.open();
		if (text != null) {
			locationField.setText(text);
		}
	}
	
	/**
	 * Prompts the user for a workspace location and returns the location
	 * as a String containing the workspace_loc variable or <code>null</code>
	 * if no location was obtained from the user.
	 */
	private void handleWorkspaceLocationButtonSelected() {
		ResourceSelectionDialog dialog;
		dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), "Select a resource");
		dialog.open();
		Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return;
		}
		IResource resource = (IResource)results[0];
		StringBuffer buf = new StringBuffer();
		ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, resource.getFullPath().toString(), buf);
		String text= buf.toString();
		if (text != null) {
			locationField.setText(text);
		}
	}
	
	private void handleWorkspaceWorkingDirectoryButtonSelected() {
		ContainerSelectionDialog containerDialog;
		containerDialog = new ContainerSelectionDialog(
			getShell(), 
			ResourcesPlugin.getWorkspace().getRoot(),
			false,
			"Select a directory");
		containerDialog.open();
		Object[] resource = containerDialog.getResult();
		String text= null;
		if (resource != null && resource.length > 0) {
			text= ToolUtil.buildVariableTag(IExternalToolConstants.VAR_RESOURCE_LOC, ((IPath)resource[0]).toString());
		}
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}
	
	private void handleFileWorkingDirectoryButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage("Select a directory");
		dialog.setFilterPath(workDirectoryField.getText());
		String text= dialog.open();
		if (text != null) {
			workDirectoryField.setText(text);
		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return ExternalToolsImages.getImage(IExternalToolConstants.IMG_TAB_MAIN);
	}

}
