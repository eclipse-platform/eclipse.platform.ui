package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.externaltools.internal.core.*;

/**
 * Dialog box to enter the required information for running
 * an external tool.
 */
public class EditDialog extends TitleAreaDialog {
	// The width of most text fields in the dialog.
	// Fields that have labels on the same line are shorter.
	// As such, all fields end in the same vertical position.
	private static final int FIELD_WIDTH = 300;
	// The spacing used to seperate groups in the dialog.
	private static final int GROUP_SPACE = 20;
	// The spacing used to seperate widgets in a group
	private static final int WIDGET_SPACE = 5;
	// The spacing of margins in the dialog
	private static final int MARGIN_SPACE = 5;
	
	// dialog sizing constants
	private static final int SIZING_SELECTION_PANE_HEIGHT = 300;
	private static final int SIZING_SELECTION_PANE_WIDTH = 300;	
	
	private static final boolean INITIAL_SHOW_LOG = true;
	
	private Text nameField;
	private Text locationField;
	private Text argumentsField;
	private Text directoryField;
	private Text refreshField;
	private Button locationBrowseWorkspace;
	private Button locationBrowseFileSystem;
	private Button argumentsBrowseVariable;
//	private Button directoryBrowseWorkspace;
	private Button directoryBrowseButton;
	private Button refreshOptionButton;
	private Button showLog;
	
	private boolean editMode = false;
	private ExternalTool tool;
	private String refreshScope;

	private int maxButtonWidth = 0;
	// The difference between the height of a button and
	// the height of a label.
	private int buttonLabelHeightDiff;
	
	/**
	 * Instantiate a new tool tool edit dialog.
	 *
	 * @param parentShell the parent SWT shell
	 * @param tool the tool tool to edit, <code>null</code> if new
	 */
	public EditDialog(Shell parentShell, ExternalTool tool) {
		super(parentShell);
		if (tool == null) {
			this.tool = new ExternalTool();
			this.editMode = false;
		} else {
			this.tool = tool;
			this.editMode = true;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (editMode)
			shell.setText(ToolMessages.getString("EditDialog.editShellTitle")); //$NON-NLS-1$
		else
			shell.setText(ToolMessages.getString("EditDialog.newShellTitle")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			shell,
			IHelpContextIds.EDIT_DIALOG);
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		
		if (!editMode)
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			
		// Now that both the dialog area and buttons have been
		// created, update the message description.
		validateFields();
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogComp = (Composite)super.createDialogArea(parent);
				
		// Set title and message now that the controls exist
		setTitle(ToolMessages.getString("EditDialog.dialogTitle")); //$NON-NLS-1$
		if (editMode)
			setMessage(ToolMessages.getString("EditDialog.editDialogMessage")); //$NON-NLS-1$
		else
			setMessage(ToolMessages.getString("EditDialog.newDialogMessage")); //$NON-NLS-1$
		setTitleImage(ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_WIZBAN_EXTERNAL_TOOLS).createImage());
		
		// Build the top container
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		FormLayout layout = new FormLayout();
		topComp.setLayout(layout);
		layout.marginHeight = MARGIN_SPACE;
		layout.marginWidth = MARGIN_SPACE;
		
		// Need to keep track of the FormData's for the buttons to set
		// the width of all the buttons to be the same as the largest
		// button width
		FormData[] buttonData = new FormData[5];
		
		// Create name label
		Label nameLabel = new Label(topComp, SWT.NONE);
		nameLabel.setText(ToolMessages.getString("EditDialog.nameLabel")); //$NON-NLS-1$
		FormData data = new FormData();
		data.top = new FormAttachment(0, MARGIN_SPACE);
		nameLabel.setLayoutData(data);

		// Create name text field
		nameField = new Text(topComp, SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(nameLabel, MARGIN_SPACE, SWT.RIGHT);
		data.top = new FormAttachment(nameLabel, 0, SWT.CENTER);
		data.width = FIELD_WIDTH - MARGIN_SPACE - nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).x;
		nameField.setLayoutData(data);

		// Create location browse workspace button
		locationBrowseWorkspace = new Button(topComp, SWT.PUSH);
		locationBrowseWorkspace.setText(ToolMessages.getString("EditDialog.browseWkspButton1")); //$NON-NLS-1$
		buttonData[0] = new FormData();
		buttonData[0].left = new FormAttachment(nameField, MARGIN_SPACE, SWT.RIGHT);
		buttonData[0].top = new FormAttachment(nameField, GROUP_SPACE, SWT.BOTTOM);
		locationBrowseWorkspace.setLayoutData(buttonData[0]);
		checkForMaxWidth(locationBrowseWorkspace);
		
		// Calculate the difference in height between a label and a button.
		// This variable will be used to ensure equal spacing between groups.
		buttonLabelHeightDiff = locationBrowseWorkspace.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).y - nameLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).y;

		// Create label for location text field.
		Label locationLabel = new Label(topComp, SWT.NONE);
		locationLabel.setText(ToolMessages.getString("EditDialog.locationLabel")); //$NON-NLS-1$
		data = new FormData();
		data.bottom = new FormAttachment(locationBrowseWorkspace, 0, SWT.BOTTOM);
		locationLabel.setLayoutData(data);

		// Create location text field.
		locationField = new Text(topComp, SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(locationBrowseWorkspace, -MARGIN_SPACE, SWT.LEFT);
		data.top = new FormAttachment(locationBrowseWorkspace, WIDGET_SPACE, SWT.BOTTOM);
		data.width = FIELD_WIDTH;
		locationField.setLayoutData(data);

		// Create location browse file system button.
		locationBrowseFileSystem = new Button(topComp, SWT.PUSH);
		locationBrowseFileSystem.setText(ToolMessages.getString("EditDialog.browseFileSysButton1")); //$NON-NLS-1$
		buttonData[1] = new FormData();
		buttonData[1].left = new FormAttachment(locationBrowseWorkspace, 0, SWT.LEFT);
		buttonData[1].top = new FormAttachment(locationBrowseWorkspace, WIDGET_SPACE, SWT.BOTTOM);
		locationBrowseFileSystem.setLayoutData(buttonData[1]);
		checkForMaxWidth(locationBrowseFileSystem);

		// Create label for arguments text field.
		Label argumentsLabel = new Label(topComp, SWT.NONE);
		argumentsLabel.setText(ToolMessages.getString("EditDialog.argumentLabel")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(locationField, GROUP_SPACE+buttonLabelHeightDiff, SWT.BOTTOM);
		argumentsLabel.setLayoutData(data);

		// Create arguments text field.
		argumentsField = new Text(topComp, SWT.BORDER);
		data = new FormData ();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(locationBrowseFileSystem, -MARGIN_SPACE, SWT.LEFT);
		data.top = new FormAttachment(argumentsLabel, WIDGET_SPACE, SWT.BOTTOM);
		data.width = FIELD_WIDTH;
		argumentsField.setLayoutData(data);

		// Create argument browse variable button.
		argumentsBrowseVariable = new Button(topComp, SWT.PUSH);
		argumentsBrowseVariable.setText(ToolMessages.getString("EditDialog.browseVarsButton")); //$NON-NLS-1$
		buttonData[2] = new FormData();
		buttonData[2].left = new FormAttachment(locationBrowseFileSystem, 0, SWT.LEFT);
		buttonData[2].bottom = new FormAttachment(argumentsField, 0, SWT.BOTTOM);
		argumentsBrowseVariable.setLayoutData(buttonData[2]);
		checkForMaxWidth(argumentsBrowseVariable);

		// Create label for directory text field.
		Label dirLabel = new Label(topComp, SWT.NONE);
		dirLabel.setText(ToolMessages.getString("EditDialog.dirLabel")); //$NON-NLS-1$
		data = new FormData();
		data.top = new FormAttachment(argumentsField, GROUP_SPACE+buttonLabelHeightDiff, SWT.BOTTOM);
		dirLabel.setLayoutData(data);

		// Create directory text field.
		directoryField = new Text(topComp, SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(argumentsBrowseVariable, -MARGIN_SPACE, SWT.LEFT);
		data.top = new FormAttachment(dirLabel, WIDGET_SPACE, SWT.BOTTOM);
		data.width = FIELD_WIDTH;
		directoryField.setLayoutData(data);

		// Create directory browse file system button.
		directoryBrowseButton = new Button(topComp, SWT.PUSH);
		directoryBrowseButton.setText(ToolMessages.getString("EditDialog.directoryBrowseButton")); //$NON-NLS-1$
		buttonData[3] = new FormData();
		buttonData[3].left = new FormAttachment(argumentsField, MARGIN_SPACE, SWT.RIGHT);
		buttonData[3].bottom = new FormAttachment(directoryField, 0, SWT.BOTTOM);
		directoryBrowseButton.setLayoutData(buttonData[3]);
		checkForMaxWidth(directoryBrowseButton);
		
		// Create refresh check box and label.
		Label refreshLabel = new Label(topComp, SWT.NONE);
		refreshLabel.setText(ToolMessages.getString("EditDialog.refreshOption")); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0,0);
		data.top = new FormAttachment(directoryField, GROUP_SPACE+buttonLabelHeightDiff, SWT.BOTTOM);
		refreshLabel.setLayoutData(data);
		
		// Create refresh text field.
		refreshField = new Text(topComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		refreshField.setEditable(false);
		data = new FormData();
		data.left = new FormAttachment(refreshLabel, MARGIN_SPACE, SWT.RIGHT);
		data.top = new FormAttachment(refreshLabel, 0, SWT.CENTER);
		data.width = FIELD_WIDTH - MARGIN_SPACE - refreshLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).x;
		refreshField.setLayoutData(data);

		// Create refresh scope.
		refreshOptionButton = new Button(topComp, SWT.PUSH);
		refreshOptionButton.setText(ToolMessages.getString("EditDialog.refreshOptionButton")); //$NON-NLS-1$
		buttonData[4] = new FormData();
		buttonData[4].left = new FormAttachment(directoryBrowseButton, 0, SWT.LEFT);
		buttonData[4].top = new FormAttachment(refreshField, 0, SWT.TOP);
		refreshOptionButton.setLayoutData(buttonData[4]);
		checkForMaxWidth(refreshOptionButton);
		
		// Create show log checkbox
		showLog = new Button(topComp, SWT.CHECK);
		showLog.setText(ToolMessages.getString("EditDialog.showLogLabel")); //$NON-NLS-1$
		showLog.setSelection(INITIAL_SHOW_LOG);
		data = new FormData();
		data.left = new FormAttachment(0,0);
		data.top = new FormAttachment(refreshField, GROUP_SPACE, SWT.BOTTOM);
		showLog.setLayoutData(data);
		
		// give all the buttons the same width
		for (int i=0; i<buttonData.length; i++) {
			buttonData[i].width = maxButtonWidth;	
		}	
		
		// Build the separator line
		Label separator = new Label(dialogComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Init field values
		if (editMode) {
			nameField.setText(tool.getName());
			locationField.setText(tool.getLocation());
			argumentsField.setText(tool.getArguments());
			directoryField.setText(tool.getWorkingDirectory());
			showLog.setSelection(tool.getShowLog());
		}
		refreshScope = tool.getRefreshScope();
		updateRefreshField();

		// Set the proper tab order
		Control[] tabList = new Control[] {
			nameField, 
			locationField, 
			locationBrowseWorkspace,
			locationBrowseFileSystem,
			argumentsField,
			argumentsBrowseVariable,
			directoryField,
			directoryBrowseButton,
			refreshField,
			refreshOptionButton};
		topComp.setTabList(tabList);
		
		// Finish setup
		hookButtonActions();
		hookFieldValidation();
		nameField.setFocus();

		return dialogComp;
	}
	
	/**
	 * Check to see if the supplied button has the maximum width of
	 * all the buttons so far. If it is, store the width in
	 * the integer variable <code>maxButtonWidth</code>.
	 */
	private void checkForMaxWidth(Button button) {
		Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
		if (size.x > maxButtonWidth)
			maxButtonWidth = size.x;		
	}
	
	/**
	 * Returns the tool tool applicable to this dialog.
	 */
	public ExternalTool getExternalTool() {
		return tool;
	}
	
	/**
	 * Hooks the action handler for when a button is pressed
	 */
	private void hookButtonActions() {
		locationBrowseWorkspace.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionDialog dialog;
				dialog = new ResourceSelectionDialog(
					getShell(), 
					ResourcesPlugin.getWorkspace().getRoot(),
					ToolMessages.getString("EditDialog.selectTool")); //$NON-NLS-1$
				dialog.open();
				Object[] results = dialog.getResult();
				if (results == null || results.length < 1)
					return;
				IResource resource = (IResource)results[0];
				StringBuffer buf = new StringBuffer();
				ToolUtil.buildVariableTag(ExternalTool.VAR_WORKSPACE_LOC, resource.getFullPath().toString(), buf);
				locationField.setText(buf.toString());
			}
		});
		
		locationBrowseFileSystem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
				dialog.setFileName(locationField.getText());
				String filename = dialog.open();
				if (filename != null) {
					locationField.setText(filename);
				}
			}
		});

		argumentsBrowseVariable.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				VariableSelectionDialog dialog;
				dialog = new VariableSelectionDialog(getShell());
				dialog.open();
				Object[] results = dialog.getResult();
				if (results == null || results.length < 1)
					return;
				String args = argumentsField.getText();
				args = args + (String)results[0];
				argumentsField.setText(args);
			}
		});
		
		directoryBrowseButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectorySelectionDialog dialog = new DirectorySelectionDialog(getShell());
				dialog.open();
				Object[] results = dialog.getResult();
				String selectedDirectory = null;
				if (results != null && results.length > 0)
					selectedDirectory = (String)results[0];
				if (selectedDirectory != null) {
					directoryField.setText(selectedDirectory);
				}
			}
		});

		refreshOptionButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				RefreshSelectionDialog dialog;
				dialog = new RefreshSelectionDialog(getShell());
				dialog.open();
				Object[] results = dialog.getResult();
				if (results == null || results.length < 1)
					return;
				refreshScope = (String)results[0];
				updateRefreshField();
			}
		});
	}
	
	/**
	 * Hooks the necessary field validation
	 */
	private void hookFieldValidation() {
		nameField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateFields();
			}
		});
		
		locationField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateFields();
			}
		});
		
		directoryField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateFields();
			}
		});
	}

	/**
	 * Validate the fields for acceptable values
	 */
	private void validateFields() {
		String value = nameField.getText().trim();
		if (value.length() < 1) {
			setMessage(ToolMessages.getString("EditDialog.noToolName"), IMessageProvider.NONE); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}
		
		value = locationField.getText().trim();
		if (value.length() < 1) {
			setMessage(ToolMessages.getString("EditDialog.noToolLocation"), IMessageProvider.NONE); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}

		getButton(IDialogConstants.OK_ID).setEnabled(true);
		
		// Translate field contents to the actual file location so we
		// can check to ensure the file actually exists.
		value = ToolUtil.getLocationFromText(value);
		
		if (value == null) { // The resource could not be found.
			setMessage(ToolMessages.getString("EditDialog.missingToolLocation"), IMessageProvider.WARNING); //$NON-NLS-1$
			return;			
		}
		File file = new File(value);
		if (!file.exists()) { // The file does not exist.
			setMessage(ToolMessages.getString("EditDialog.missingToolLocation"), IMessageProvider.WARNING); //$NON-NLS-1$
			return;
		}
		String fileName = value;
		
		value = directoryField.getText().trim();
		if (value.length() > 0) {
			// Translate field contents to the actual directory location so we
			// can check to ensure the directory actually exists.
			value = ToolUtil.getLocationFromText(value);
			
			if (value == null) { // The resource could not be found.
				setMessage(ToolMessages.getString("EditDialog.missingToolDirectory"), IMessageProvider.WARNING); //$NON-NLS-1$
				return;			
			}			
			file = new File(value);
			if (!file.exists()) { // The directory does not exist.
				setMessage(ToolMessages.getString("EditDialog.missingToolDirectory"), IMessageProvider.WARNING); //$NON-NLS-1$
				return;
			}
		}
		
		if (fileName.endsWith(".xml")) { //$NON-NLS-1$
			setMessage(ToolMessages.getString("EditDialog.howToSelectAntTargets"), IMessageProvider.INFORMATION); //$NON-NLS-1$
			return;
		}
		
		if (editMode)
			setMessage(ToolMessages.getString("EditDialog.editDialogMessage")); //$NON-NLS-1$
		else
			setMessage(ToolMessages.getString("EditDialog.newDialogMessage")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		String command = locationField.getText().trim();
		String value = ToolUtil.getLocationFromText(command);
		if (value != null && value.endsWith(".xml")) //$NON-NLS-1$
			tool.setType(ExternalTool.TOOL_TYPE_ANT);
		else
			tool.setType(ExternalTool.TOOL_TYPE_PROGRAM);
		tool.setName(nameField.getText().trim());
		tool.setLocation(command);
		tool.setArguments(argumentsField.getText().trim());
		tool.setWorkingDirectory(directoryField.getText().trim());
		tool.setRefreshScope(refreshScope);
		tool.setShowLog(showLog.getSelection());
		
		super.okPressed();
	}
	
	/**
	 * Update the refresh scope field
	 */
	private void updateRefreshField() {
		ToolUtil.VariableDefinition result = ToolUtil.extractVariableTag(refreshScope, 0);
		if (result.name == null) {
			refreshScope = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_NONE, null);
			result.name = ExternalTool.REFRESH_SCOPE_NONE;
		}
		
		if (ExternalTool.REFRESH_SCOPE_NONE.equals(result.name)) {
			refreshField.setText(ToolMessages.getString("EditDialog.refreshScopeNone")); //$NON-NLS-1$
			return;
		}
		if (ExternalTool.REFRESH_SCOPE_WORKSPACE.equals(result.name)) {
			refreshField.setText(ToolMessages.getString("EditDialog.refreshScopeWorkspace")); //$NON-NLS-1$
			return;
		}
		if (ExternalTool.REFRESH_SCOPE_PROJECT.equals(result.name)) {
			if (result.argument == null)
				refreshField.setText(ToolMessages.getString("EditDialog.refreshScopeProject")); //$NON-NLS-1$
			else
				refreshField.setText(ToolMessages.format("EditDialog.refreshScopeProjectX", new Object[] {result.argument})); //$NON-NLS-1$
			return;
		}
		if (ExternalTool.REFRESH_SCOPE_WORKING_SET.equals(result.name)) {
			if (result.argument == null) {
				refreshScope = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_NONE, null);
				refreshField.setText(ToolMessages.getString("EditDialog.refreshScopeNone")); //$NON-NLS-1$
			}
			else
				refreshField.setText(ToolMessages.format("EditDialog.refreshScopeWorkingSet", new Object[] {result.argument})); //$NON-NLS-1$
			return;
		}
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
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
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


	/**
	 * Internal dialog to show available variables from which
	 * the user can select one.
	 */
	private class VariableSelectionDialog extends SelectionDialog {
		String location;
		List list;
		
		public VariableSelectionDialog(Shell parent) {
			super(parent);
			setTitle(ToolMessages.getString("EditDialog.browseVarTitle")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.VARIABLE_SELECTION_DIALOG);
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolMessages.getString("EditDialog.selectVar")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);

			list.add(ToolMessages.getString("EditDialog.varWorkspaceLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourceLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectPathLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerPathLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourcePathLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectNameLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerNameLabel")); //$NON-NLS-1$	
			list.add(ToolMessages.getString("EditDialog.varResourceNameLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectXLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerXLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourceXLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectXPathLabel")); //$NON-NLS-1$	
			list.add(ToolMessages.getString("EditDialog.varContainerXPathLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourceXPathLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectXNameLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerXNameLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourceXNameLabel")); //$NON-NLS-1$	
			list.add(ToolMessages.getString("EditDialog.varBuildTypeNameLabel")); //$NON-NLS-1$	

			location = ToolUtil.getLocationFromText(locationField.getText().trim());
			if (location != null && location.endsWith(".xml")) { //$NON-NLS-1$
				list.add(ToolMessages.getString("EditDialog.varAntTargetLabel")); //$NON-NLS-1$
			}		
			
			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			int sel = list.getSelectionIndex();
			String result = null;
			
			switch (sel) {
				case 0 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_WORKSPACE_LOC, null);
					break;

				case 1 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_PROJECT_LOC, null);
					break;

				case 2 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_CONTAINER_LOC, null);
					break;

				case 3 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_RESOURCE_LOC, null);
					break;

				case 4 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_PROJECT_PATH, null);
					break;

				case 5 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_CONTAINER_PATH, null);
					break;

				case 6 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_RESOURCE_PATH, null);
					break;

				case 7 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_PROJECT_NAME, null);
					break;

				case 8 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_CONTAINER_NAME, null);
					break;

				case 9 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_RESOURCE_NAME, null);
					break;

				case 10 :
					result = showResourceDialog(ExternalTool.VAR_PROJECT_LOC);
					break;

				case 11 :
					result = showResourceDialog(ExternalTool.VAR_CONTAINER_LOC);
					break;

				case 12 :
					result = showResourceDialog(ExternalTool.VAR_RESOURCE_LOC);
					break;

				case 13 :
					result = showResourceDialog(ExternalTool.VAR_PROJECT_PATH);
					break;

				case 14 :
					result = showResourceDialog(ExternalTool.VAR_CONTAINER_PATH);
					break;

				case 15 :
					result = showResourceDialog(ExternalTool.VAR_RESOURCE_PATH);
					break;

				case 16 :
					result = showResourceDialog(ExternalTool.VAR_PROJECT_NAME);
					break;

				case 17 :
					result = showResourceDialog(ExternalTool.VAR_CONTAINER_NAME);
					break;

				case 18 :
					result = showResourceDialog(ExternalTool.VAR_RESOURCE_NAME);
					break;
					
				case 19 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_BUILD_TYPE, null);
					break;
					
				case 20 :
					AntTargetList targetList = null;
					try {
						targetList = AntUtil.getTargetList(new Path(location));
					} catch (CoreException e) {
						ErrorDialog.openError(
							getShell(),
							ToolMessages.getString("EditDialog.errorTitle"), //$NON-NLS-1$
							ToolMessages.format("EditDialog.errorReadAntFile", new Object[] {location}), //$NON-NLS-1$;
							e.getStatus());
						break;
					}
					
					if (targetList == null) {
						MessageDialog.openError(
							getShell(),
							ToolMessages.getString("EditDialog.errorTitle"), //$NON-NLS-1$;
							ToolMessages.format("EditDialog.noAntTargets", new Object[] {location})); //$NON-NLS-1$;
						break;
					}

					TargetSelectionDialog targetDialog;
					targetDialog = new TargetSelectionDialog(getShell(), targetList);
					targetDialog.open();
					Object[] targets = targetDialog.getResult();
					if (targets != null && targets.length > 0) {
						StringBuffer buf = new StringBuffer();
						ToolUtil.buildVariableTags(ExternalTool.VAR_ANT_TARGET, (String[])targets, buf);
						result = buf.toString().trim();
					}
					break;
			}
			
			if (result != null)
				setSelectionResult(new Object[] {result});
			super.okPressed();
		}
		
		private String showResourceDialog(String varName) {
			ResourceSelectionDialog resDialog;
			resDialog = new ResourceSelectionDialog(
				getShell(), 
				ResourcesPlugin.getWorkspace().getRoot(),
				ToolMessages.getString("EditDialog.selectResource")); //$NON-NLS-1$
			resDialog.open();
			Object[] resource = resDialog.getResult();
			if (resource != null && resource.length > 0)
				return ToolUtil.buildVariableTag(varName, ((IResource)resource[0]).getFullPath().toString());
			else
				return null;
		}
	}
	
		/**
	 * Internal dialog to show available variables from which
	 * the user can select one.
	 */
	private class DirectorySelectionDialog extends SelectionDialog {
		String location;
		List list;
		
		public DirectorySelectionDialog(Shell parent) {
			super(parent);
			setTitle(ToolMessages.getString("EditDialog.browseDirTitle")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.VARIABLE_SELECTION_DIALOG);
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolMessages.getString("EditDialog.selectDir")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);

			list.add(ToolMessages.getString("EditDialog.varWorkspaceLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varProjectLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varContainerLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.varResourceLocLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.dirBrowseWorkspace")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.dirBrowseFileSystem")); //NON-NLS-1$)
			
			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			int sel = list.getSelectionIndex();
			String result = null;
			
			switch (sel) {
				case 0 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_WORKSPACE_LOC, null);
					break;

				case 1 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_PROJECT_LOC, null);
					break;

				case 2 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_CONTAINER_LOC, null);
					break;

				case 3 :
					result = ToolUtil.buildVariableTag(ExternalTool.VAR_RESOURCE_LOC, null);
					break;

				case 4 :
					result = showContainerDialog();
					break;

				case 5 :
					result = showDirectoryDialog();
					break;
				
			}
			
			if (result != null)
				setSelectionResult(new Object[] {result});
			super.okPressed();
		}
		
		private String showContainerDialog() {
			String varName = ExternalTool.VAR_WORKSPACE_LOC;
			ContainerSelectionDialog containerDialog;
			containerDialog = new ContainerSelectionDialog(
				getShell(), 
				ResourcesPlugin.getWorkspace().getRoot(),
				false,
				ToolMessages.getString("EditDialog.selectContainer")); //$NON-NLS-1$
			containerDialog.open();
			Object[] resource = containerDialog.getResult();
			if (resource != null && resource.length > 0)
				return ToolUtil.buildVariableTag(varName, ((IPath)resource[0]).toString());
			else
				return null;
		}
		
		private String showDirectoryDialog() {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
				dialog.setMessage(ToolMessages.getString("EditDialog.selectDirectory")); //$NON-NLS-1$
				dialog.setFilterPath(directoryField.getText());
				return dialog.open();	
		}
	}
	
	/**
	 * Internal dialog to show available refresh scope from which
	 * the user can select one.
	 */
	private class RefreshSelectionDialog extends SelectionDialog {
		List list;
		
		public RefreshSelectionDialog(Shell parent) {
			super(parent);
			setTitle(ToolMessages.getString("EditDialog.browseRefreshTitle")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.REFRESH_SELECTION_DIALOG);

		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolMessages.getString("EditDialog.selectRefresh")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);
			
			list.add(ToolMessages.getString("EditDialog.refreshNothingLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.refreshWorkspaceLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.refreshProjectLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.refreshProjectXLabel")); //$NON-NLS-1$
			list.add(ToolMessages.getString("EditDialog.refreshWorkingSetLabel")); //$NON-NLS-1$

			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			int sel = list.getSelectionIndex();
			String result = null;
			
			switch (sel) {
				case 0 :
					result = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_NONE, null);
					break;
					
				case 1 :
					result = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_WORKSPACE, null);
					break;

				case 2 :
					result = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_PROJECT, null);
					break;

				case 3 :
					ProjectSelectionDialog prjDialog;
					prjDialog = new ProjectSelectionDialog(getShell());
					prjDialog.open();
					Object[] name = prjDialog.getResult();
					if (name != null && name.length > 0)
						result = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_PROJECT, (String)name[0]);
					break;

				case 4 :
					IWorkingSetSelectionDialog setDialog;
					setDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false);
					setDialog.open();
					IWorkingSet[] sets = setDialog.getSelection();
					if (sets != null && sets.length > 0)
						result = ToolUtil.buildVariableTag(ExternalTool.REFRESH_SCOPE_WORKING_SET, sets[0].getName());
					break;
			}
			
			if (result != null)
				setSelectionResult(new Object[] {result});
			super.okPressed();
		}
	}
	
	/**
	 * Internal dialog to show available projects from which
	 * the user can select one.
	 */
	private class ProjectSelectionDialog extends SelectionDialog {
		List list;
		
		public ProjectSelectionDialog(Shell parent) {
			super(parent);
			setTitle(ToolMessages.getString("EditDialog.browseProjectTitle")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.PROJECT_SELECTION_DIALOG);
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolMessages.getString("EditDialog.selectProject")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);
			
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IProject[] projects = root.getProjects();
			for (int i = 0; i < projects.length; i++) {
				list.add(projects[i].getName());
			}
			
			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			setSelectionResult(list.getSelection());
			super.okPressed();
		}
	}
	
	private class TargetSelectionDialog extends SelectionDialog implements ICheckStateListener {
		private ArrayList selectedTargets = new ArrayList();
		private CheckboxTableViewer listViewer;
		private AntTargetList targetList;
		private AntTargetLabelProvider labelProvider = new AntTargetLabelProvider();	
		
		public TargetSelectionDialog(Shell parent, AntTargetList targetList) {
			super(parent);
			this.targetList = targetList;
			setTitle(ToolMessages.getString("EditDialog.varAntTargetLabel")); //$NON-NLS-1$
			WorkbenchHelp.setHelp(parent, IHelpContextIds.TARGET_SELECTION_DIALOG);
		}
		
		public void checkStateChanged(CheckStateChangedEvent e) {
			String checkedTarget = (String)e.getElement();
			if (e.getChecked())
				selectedTargets.add(checkedTarget);
			else
				selectedTargets.remove(checkedTarget);
				
			labelProvider.setSelectedTargets(selectedTargets);
			listViewer.refresh();
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolMessages.getString("EditDialog.selectTargets")); //$NON-NLS-1$
			
			listViewer = CheckboxTableViewer.newCheckList(dialogArea, SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			listViewer.getTable().setLayoutData(data);
			listViewer.setSorter(new ViewerSorter() {
				public int compare(Viewer viewer,Object o1,Object o2) {
					return ((String)o1).compareTo((String)o2);
				}
			});
			
			if (targetList != null && targetList.getDefaultTarget() != null)
				labelProvider.setDefaultTargetName(targetList.getDefaultTarget());
			listViewer.setLabelProvider(labelProvider);
			listViewer.setContentProvider(new AntTargetContentProvider());
			listViewer.setInput(targetList);
			
			listViewer.addCheckStateListener(this);
			listViewer.refresh();
			
			return dialogArea;
		}
		
		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected void okPressed() {
			setSelectionResult(getTargetNames());
			super.okPressed();
		}
		
		protected String[] getTargetNames() {
			String[] result = new String[selectedTargets.size()];
			selectedTargets.toArray(result);
			return result;
		}
	}
	
}
