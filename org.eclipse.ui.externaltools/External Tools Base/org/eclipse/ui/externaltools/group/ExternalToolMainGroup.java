package org.eclipse.ui.externaltools.group;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others. All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
 
Contributors:
**********************************************************************/

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.externaltools.internal.dialog.ExternalToolVariableForm;
import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.externaltools.internal.model.IHelpContextIds;
import org.eclipse.ui.externaltools.internal.model.ToolMessages;
import org.eclipse.ui.externaltools.model.ExternalTool;
import org.eclipse.ui.externaltools.model.IExternalToolConstants;
import org.eclipse.ui.externaltools.model.ToolUtil;
import org.eclipse.ui.externaltools.variable.ExpandVariableContext;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Group of components applicable to most external tools. This group
 * will collect from the user the location, working directory,
 * name, and description for the tool.
 * <p>
 * This group can be used or extended by clients.
 * </p>
 */
public class ExternalToolMainGroup extends ExternalToolGroup {
	private String initialLocation = ""; //$NON-NLS-1$
	private String initialName = ""; //$NON-NLS-1$
	private String initialWorkDirectory = ""; //$NON-NLS-1$
	protected Text locationField;
	protected Text workDirectoryField;
	protected Text nameField;
	protected Text descriptionField;
	private Button fileLocationButton;
	private Button workspaceLocationButton;
	private Button fileWorkingDirectoryButton;
	private Button workspaceWorkingDirectoryButton;
	
	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			validate();
		}
	};
	
	/**
	 * Creates the group
	 */
	public ExternalToolMainGroup() {
		super();
	}

	/* (non-Javadoc)
	 * Method declared on ExternalToolGroup.
	 */
	protected Control createGroupContents(Composite parent, ExternalTool tool) {
		// main composite
		Composite mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(gridData);
		
		createLocationComponent(mainComposite);
		createWorkDirectoryComponent(mainComposite);
		createNameComponent(mainComposite);
		createDescriptionComponent(mainComposite);

		if (locationField != null) {
			locationField.setText(isEditMode() ? tool.getLocation() : initialLocation);
			locationField.addModifyListener(modifyListener);
		}
		
		if (workDirectoryField != null) {
			workDirectoryField.setText(isEditMode() ? tool.getWorkingDirectory() : initialWorkDirectory);
			workDirectoryField.addModifyListener(modifyListener);
		}
		
		if (nameField != null) {
			nameField.setText(isEditMode() ? tool.getName() : initialName);
			if (isEditMode())
				nameField.setEditable(false);
			else
				nameField.addModifyListener(modifyListener);
		}
		
		validate();

		return mainComposite;
	}

	/**
	 * Creates the controls needed to edit the description
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createDescriptionComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.descriptionLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		descriptionField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		descriptionField.setLayoutData(data);
		
		createSpacer(parent);
	}
	
	/**
	 * Creates the controls needed to edit the location
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createLocationComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.locationLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		locationField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.verticalSpan= 2;
		locationField.setLayoutData(data);
		
		workspaceLocationButton= createPushButton(parent, "Browse Workspace...");
		fileLocationButton= createPushButton(parent, "Browse File System...");
		
		createSpacer(parent);
	}
	
	private Button createPushButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.PUSH );
		button.setText(ToolMessages.getString(label));
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleButtonPressed((Button)e.widget);
			}
		});
		getPage().setButtonGridData(button);
		return button;
	}
	
	private void handleButtonPressed(Button button) {
		String text= null;
		Text field= null;
		if (button == fileLocationButton) {
			text= getFileLocation();
			field= locationField;
		} else if (button == workspaceLocationButton) {
			text= getWorkspaceLocation();
			field= locationField;
		} else if (button == fileWorkingDirectoryButton) {
			DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
			dialog.setMessage("Select a directory");
			dialog.setFilterPath(workDirectoryField.getText());
			text= dialog.open();
			field= workDirectoryField;
		} else if (button == workspaceWorkingDirectoryButton) {
			ContainerSelectionDialog containerDialog;
			containerDialog = new ContainerSelectionDialog(
				getShell(), 
				ResourcesPlugin.getWorkspace().getRoot(),
				false,
				"Select a directory");
			containerDialog.open();
			Object[] resource = containerDialog.getResult();
			if (resource != null && resource.length > 0) {
				text= ToolUtil.buildVariableTag(IExternalToolConstants.VAR_RESOURCE_LOC, ((IPath)resource[0]).toString());
			}
			field= workDirectoryField;
		}
		if (text != null && field != null) {
			field.setText(text);
		}
	}
	
	private String getFileLocation() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.NONE);
		fileDialog.setFileName(locationField.getText());
		return fileDialog.open();
	}
	
	/**
	 * Prompts the user for a workspace location and returns the location
	 * as a String containing the workspace_loc variable or <code>null</code>
	 * if no location was obtained from the user.	 */
	private String getWorkspaceLocation() {
		ResourceSelectionDialog dialog;
		dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), "Select a resource");
		dialog.open();
		Object[] results = dialog.getResult();
		if (results == null || results.length < 1) {
			return null;
		}
		IResource resource = (IResource)results[0];
		StringBuffer buf = new StringBuffer();
		ToolUtil.buildVariableTag(IExternalToolConstants.VAR_WORKSPACE_LOC, resource.getFullPath().toString(), buf);
		return buf.toString();
	}
	
	/**
	 * Internal dialog to show available resources from which
	 * the user can select one
	 */
	private class ResourceSelectionDialog extends SelectionDialog {
		String labelText;
		IContainer root;
		TreeViewer wsTree;
		
		public ResourceSelectionDialog(Shell parent, IContainer root, String labelText) {
			super(parent);
			this.root = root;
			this.labelText = labelText;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setTitle(ToolMessages.getString("EditDialog.browseWorkspaceTitle")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.RESOURCE_SELECTION_DIALOG);
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(labelText);
			
			Tree tree = new Tree(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = 300;
			data.widthHint = 300;
			tree.setLayoutData(data);
			wsTree = new TreeViewer(tree);
			wsTree.setContentProvider(new WorkbenchContentProvider());
			wsTree.setLabelProvider(new WorkbenchLabelProvider());
			wsTree.setInput(root);
			
			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			IStructuredSelection sel = (IStructuredSelection)wsTree.getSelection();
			if (sel != null)
				setSelectionResult(sel.toArray());
			super.okPressed();
		}
	}
	
	private class WorkspaceSelectionDialog extends SelectionDialog {
		public WorkspaceSelectionDialog() {
			super(ExternalToolMainGroup.this.getShell());
		}
		protected Control createDialogArea(Composite parent) {
			Composite composite= (Composite)super.createDialogArea(parent);
			ExternalToolVariableForm form= new ExternalToolVariableForm("Select from workspace", ExternalToolsPlugin.getDefault().getDirectoryLocationVariableRegistry().getPathLocationVariables());
			form.createContents(composite, new IGroupDialogPage() {
				public GridData setButtonGridData(Button button) {
					return null;
				}

				public void setMessage(String newMessage, int newType) {
				}

				public void updateValidState() {
				}

				public int convertHeightHint(int chars) {
					return 0;
				}

				public String getMessage() {
					return null;
				}

				public int getMessageType() {
					return 0;
				}
			});
			return composite;
		}

	}
	
	private Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	/**
	 * Creates the controls needed to edit the name
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createNameComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.nameLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		nameField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		nameField.setLayoutData(data);
		
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
	 * Creates the controls needed to edit the working directory
	 * attribute of an external tool
	 * 
	 * @param parent the composite to create the controls in
	 */
	protected void createWorkDirectoryComponent(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(ToolMessages.getString("ExternalToolMainGroup.workDirLabel")); //$NON-NLS-1$
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		
		workDirectoryField = new Text(parent, SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		data.verticalSpan= 2;
		workDirectoryField.setLayoutData(data);
		
		workspaceWorkingDirectoryButton= createPushButton(parent, "Browse Workspace...");
		fileWorkingDirectoryButton= createPushButton(parent, "Browse File System...");

		createSpacer(parent);
	}

	/**
	 * Returns the proposed initial location for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial location when editing new tool.
	 */
	public final String getInitialLocation() {
		return initialLocation;
	}

	/**
	 * Returns the proposed initial name for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial name when editing new tool.
	 */
	public final String getInitialName() {
		return initialName;
	}

	/**
	 * Returns the proposed initial working directory for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @return the proposed initial working directory when editing new tool.
	 */
	public final String getInitialWorkDirectory() {
		return initialWorkDirectory;
	}

	/**
	 * Returns the name given to the external tool as
	 * found in the text field, or <code>null</code> if
	 * field does not exist
	 */
	public String getNameFieldValue() {
		if (nameField != null)
			return nameField.getText().trim();
		else
			return null;
	}
	
	/**
	 * Returns the location of the external tool as 
	 * found in the text field, or <code>null</code> if
	 * field does not exist.
	 */
	public String getLocationFieldValue() {
		if (locationField != null)
			return locationField.getText().trim();
		else
			return null;	
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void restoreValues(ExternalTool tool) {
		if (locationField != null)
			locationField.setText(tool.getLocation());
		if (workDirectoryField != null)
			workDirectoryField.setText(tool.getWorkingDirectory());
		if (nameField != null)
			nameField.setText(tool.getName());
		if (descriptionField != null)
			descriptionField.setText(tool.getDescription());
	}
	
	/**
	 * Sets the proposed initial location for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialLocation the proposed initial location when editing new tool.
	 */
	public final void setInitialLocation(String initialLocation) {
		if (initialLocation != null)
			this.initialLocation = initialLocation;
	}

	/**
	 * Sets the proposed initial name for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialName the proposed initial name when editing new tool.
	 */
	public final void setInitialName(String initialName) {
		if (initialName != null)
			this.initialName = initialName;
	}

	/**
	 * Sets the proposed initial working directory for the external
	 * tool if no tool provided in the createContents.
	 * 
	 * @param initialWorkDirectory the proposed initial working directory when editing new tool.
	 */
	public final void setInitialWorkDirectory(String initialWorkDirectory) {
		if (initialWorkDirectory != null)
			this.initialWorkDirectory = initialWorkDirectory;
	}

	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void updateTool(ExternalTool tool) {
		if (locationField != null)
			tool.setLocation(locationField.getText().trim());
		if (workDirectoryField != null)
			tool.setWorkingDirectory(workDirectoryField.getText().trim());
		if (descriptionField != null)
			tool.setDescription(descriptionField.getText().trim());
	}
	
	/* (non-Javadoc)
	 * Method declared on IExternalToolGroup.
	 */
	public void validate() {
		ValidationStatus status = new ValidationStatus();
		validateLocation(status);
		validateWorkDirectory(status);
		validateName(status);
		
		getPage().setMessage(status.message, status.messageType);
		setIsValid(status.isValid);
	}

	/**
	 * Validates the content of the location field, and
	 * updates the validation status. Does nothing if the
	 * validation status is already invalid.
	 */
	protected void validateLocation(ValidationStatus status) {
		if (locationField == null || !status.isValid)
			return;

		String value = locationField.getText().trim();
		if (value.length() < 1) {
			status.message = ToolMessages.getString("ExternalToolMainGroup.locationRequired"); //$NON-NLS-1$
			status.messageType = IMessageProvider.NONE;
			status.isValid = false;
			return;
		}

		// Translate field contents to the actual file location so we
		// can check to ensure the file actually exists.
		MultiStatus multiStatus = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		value = ToolUtil.expandFileLocation(value, ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
		if (!multiStatus.isOK()) {
			IStatus[] children = multiStatus.getChildren();
			if (children.length > 0) {
				status.message = children[0].getMessage();
				status.messageType = IMessageProvider.WARNING;
			}
			status.isValid = false;
			return;
		}
		
		if (value == null) { // The resource could not be found.
			status.message = ToolMessages.getString("ExternalToolMainGroup.invalidLocation"); //$NON-NLS-1$
			status.messageType = IMessageProvider.INFORMATION;
			status.isValid = true;
			return;
		}
		
		File file = new File(value);
		if (!file.exists()) { // The file does not exist.
			status.message = ToolMessages.getString("ExternalToolMainGroup.invalidLocation"); //$NON-NLS-1$
			status.messageType = IMessageProvider.INFORMATION;
			status.isValid = true;
			return;
		}
	}
	
	/**
	 * Validates the content of the name field, and
	 * updates the validation status. Does nothing if the
	 * validation status is already invalid.
	 */
	protected void validateName(ValidationStatus status) {
		if (isEditMode() || nameField == null || !status.isValid)
			return;
			
		String value = nameField.getText().trim();
		if (value.length() < 1) {
			status.message = ToolMessages.getString("ExternalToolMainGroup.nameRequired"); //$NON-NLS-1$
			status.messageType = IMessageProvider.WARNING;
			status.isValid = false;
			return;
		}
		
		String errorText = ExternalTool.validateToolName(value);
		if (errorText != null) {
			status.message = errorText;
			status.messageType = IMessageProvider.WARNING;
			status.isValid = false;
			return;
		}
		
		boolean exists = ExternalToolsPlugin.getDefault().getToolRegistry(nameField.getShell()).hasToolNamed(value);
		if (exists) {
			status.message = ToolMessages.getString("ExternalToolMainGroup.nameAlreadyExist"); //$NON-NLS-1$
			status.messageType = IMessageProvider.WARNING;
			status.isValid = false;
			return;
		}
	}
	
	/**
	 * Validates the content of the working directory field, and
	 * updates the validation status. Does nothing if the
	 * validation status is already invalid.
	 */
	protected void validateWorkDirectory(ValidationStatus status) {
		if (workDirectoryField == null || !status.isValid)
			return;
			
		String value = workDirectoryField.getText().trim();
		if (value.length() > 0) {
			// Translate field contents to the actual directory location so we
			// can check to ensure the directory actually exists.
			MultiStatus multiStatus = new MultiStatus(IExternalToolConstants.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
			value = ToolUtil.expandDirectoryLocation(value, ExpandVariableContext.EMPTY_CONTEXT, multiStatus);
			if (!multiStatus.isOK()) {
				IStatus[] children = multiStatus.getChildren();
				if (children.length > 0) {
					status.message = children[0].getMessage();
					status.messageType = IMessageProvider.WARNING;
				}
				status.isValid = false;
				return;
			}
			
			if (value == null) { // The resource could not be found.
				status.message = ToolMessages.getString("ExternalToolMainGroup.invalidWorkDir"); //$NON-NLS-1$
				status.messageType = IMessageProvider.INFORMATION;
				status.isValid = true;
				return;
			}			
			File file = new File(value);
			if (!file.exists()) { // The directory does not exist.
				status.message = ToolMessages.getString("ExternalToolMainGroup.invalidWorkDir"); //$NON-NLS-1$
				status.messageType = IMessageProvider.INFORMATION;
				status.isValid = true;
				return;
			}
		}
	}
}
