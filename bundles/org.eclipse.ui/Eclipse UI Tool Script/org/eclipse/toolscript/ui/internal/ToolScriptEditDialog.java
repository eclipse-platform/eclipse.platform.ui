package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.io.File;
import java.util.*;

import org.apache.tools.ant.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.toolscript.core.internal.*;
import org.eclipse.ui.*;
import org.eclipse.ui.dialogs.*;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.*;
import org.eclipse.swt.widgets.List;

/**
 * Dialog box to enter the required information for running
 * a tool script.
 */
public class ToolScriptEditDialog extends TitleAreaDialog {
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
	private static final int SIZING_SELECTION_PANE_HEIGHT = 250;
	private static final int SIZING_SELECTION_PANE_WIDTH = 300;	
	
	private Text nameField;
	private Text locationField;
	private Text argumentsField;
	private Text directoryField;
	private Text refreshField;
	private Button locationBrowseWorkspace;
	private Button locationBrowseFileSystem;
	private Button argumentsBrowseVariable;
	private Button directoryBrowseWorkspace;
	private Button directoryBrowseFileSystem;
	private Button refreshOptionButton;
	
	private boolean editMode = false;
	private ToolScript script;
	private String refreshScope;

	private int maxButtonWidth = 0;
	// The difference between the height of a button and
	// the height of a label.
	private int buttonLabelHeightDiff;
	
	/**
	 * Instantiate a new tool script edit dialog.
	 *
	 * @param parentShell the parent SWT shell
	 * @param script the tool script to edit, <code>null</code> if new
	 */
	public ToolScriptEditDialog(Shell parentShell, ToolScript toolScript) {
		super(parentShell);
		if (toolScript == null) {
			this.script = new ToolScript();
			this.editMode = false;
		} else {
			this.script = toolScript;
			this.editMode = true;
		}
	}
	
	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (editMode)
			shell.setText(ToolScriptMessages.getString("ToolScriptEditDialog.editShellTitle")); //$NON-NLS-1$
		else
			shell.setText(ToolScriptMessages.getString("ToolScriptEditDialog.newShellTitle")); //$NON-NLS-1$
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
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite dialogComp = (Composite)super.createDialogArea(parent);
				
		// Set title and message now that the controls exist
		setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.dialogTitle")); //$NON-NLS-1$
		if (editMode)
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.editDialogMessage")); //$NON-NLS-1$
		else
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.newDialogMessage")); //$NON-NLS-1$
		
		// Build the top container
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		FormLayout layout = new FormLayout();
		topComp.setLayout(layout);
		layout.marginHeight = MARGIN_SPACE;
		layout.marginWidth = MARGIN_SPACE;
		
		// Need to keep track of the FormData's for the buttons to set
		// the width of all the buttons to be the same as the largest
		// button width
		FormData[] buttonData = new FormData[6];
		
		// Create name label
		Label nameLabel = new Label(topComp, SWT.NONE);
		nameLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.nameLabel")); //$NON-NLS-1$
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
		locationBrowseWorkspace.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseWkspButton1")); //$NON-NLS-1$
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
		locationLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.locationLabel")); //$NON-NLS-1$
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
		locationBrowseFileSystem.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseFileSysButton1")); //$NON-NLS-1$
		buttonData[1] = new FormData();
		buttonData[1].left = new FormAttachment(locationBrowseWorkspace, 0, SWT.LEFT);
		buttonData[1].top = new FormAttachment(locationBrowseWorkspace, WIDGET_SPACE, SWT.BOTTOM);
		locationBrowseFileSystem.setLayoutData(buttonData[1]);
		checkForMaxWidth(locationBrowseFileSystem);

		// Create label for arguments text field.
		Label argumentsLabel = new Label(topComp, SWT.NONE);
		argumentsLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.argumentLabel")); //$NON-NLS-1$
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
		argumentsBrowseVariable.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseVarsButton")); //$NON-NLS-1$
		buttonData[2] = new FormData();
		buttonData[2].left = new FormAttachment(locationBrowseFileSystem, 0, SWT.LEFT);
		buttonData[2].bottom = new FormAttachment(argumentsField, 0, SWT.BOTTOM);
		argumentsBrowseVariable.setLayoutData(buttonData[2]);
		checkForMaxWidth(argumentsBrowseVariable);

		// Create directory browse workspace button.
		directoryBrowseWorkspace = new Button(topComp, SWT.PUSH);
		directoryBrowseWorkspace.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseWkspButton2")); //$NON-NLS-1$
		buttonData[3] = new FormData();
		buttonData[3].left = new FormAttachment(argumentsBrowseVariable, 0, SWT.LEFT);
		buttonData[3].top = new FormAttachment(argumentsBrowseVariable, GROUP_SPACE, SWT.BOTTOM);
		directoryBrowseWorkspace.setLayoutData(buttonData[3]);
		checkForMaxWidth(directoryBrowseWorkspace);

		// Create label for directory text field.
		Label dirLabel = new Label(topComp, SWT.NONE);
		dirLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.dirLabel")); //$NON-NLS-1$
		data = new FormData();
		data.bottom = new FormAttachment(directoryBrowseWorkspace, 0, SWT.BOTTOM);
		dirLabel.setLayoutData(data);

		// Create directory text field.
		directoryField = new Text(topComp, SWT.BORDER);
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(directoryBrowseWorkspace, -MARGIN_SPACE, SWT.LEFT);
		data.top = new FormAttachment(directoryBrowseWorkspace, WIDGET_SPACE, SWT.BOTTOM);
		data.width = FIELD_WIDTH;
		directoryField.setLayoutData(data);

		// Create directory browse file system button.
		directoryBrowseFileSystem = new Button(topComp, SWT.PUSH);
		directoryBrowseFileSystem.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseFileSysButton2")); //$NON-NLS-1$
		buttonData[4] = new FormData();
		buttonData[4].left = new FormAttachment(argumentsField, MARGIN_SPACE, SWT.RIGHT);
		buttonData[4].bottom = new FormAttachment(directoryField, 0, SWT.BOTTOM);
		directoryBrowseFileSystem.setLayoutData(buttonData[4]);
		checkForMaxWidth(directoryBrowseFileSystem);
		
		// Create refresh check box and label.
		Label refreshLabel = new Label(topComp, SWT.NONE);
		refreshLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshOption")); //$NON-NLS-1$
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
		refreshOptionButton.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshOptionButton")); //$NON-NLS-1$
		buttonData[5] = new FormData();
		buttonData[5].left = new FormAttachment(directoryBrowseFileSystem, 0, SWT.LEFT);
		buttonData[5].top = new FormAttachment(refreshField, 0, SWT.TOP);
		refreshOptionButton.setLayoutData(buttonData[5]);
		
		// give all the buttons the same width
		for (int i=0; i<buttonData.length; i++) {
			buttonData[i].width = maxButtonWidth;	
		}	
		
		// Build the separator line
		Label separator = new Label(dialogComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Init field values
		if (editMode) {
			nameField.setText(script.getName());
			locationField.setText(script.getLocation());
			argumentsField.setText(script.getArguments());
			directoryField.setText(script.getWorkingDirectory());
		}
		refreshScope = script.getRefreshScope();
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
			directoryBrowseWorkspace,
			directoryBrowseFileSystem,
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
	 * Returns the tool script applicable to this dialog.
	 */
	public ToolScript getToolScript() {
		return script;
	}
	
	/**
	 * Hooks the action handler for when a button is pressed
	 */
	private void hookButtonActions() {
		locationBrowseWorkspace.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ResourceSelectionDialog dialog;
				dialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot());
				dialog.open();
				Object[] results = dialog.getResult();
				if (results == null || results.length < 1)
					return;
				IResource resource = (IResource)results[0];
				String var = ToolScript.buildVariableTag(ToolScript.VAR_DIR_WORKSPACE, null);
				locationField.setText(var + resource.getFullPath());
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
		
		directoryBrowseWorkspace.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(
					getShell(), 
					ResourcesPlugin.getWorkspace().getRoot(), 
					false, 
					ToolScriptMessages.getString("ToolScriptEditDialog.selectFolder")); //$NON-NLS-1$
				dialog.showClosedProjects(false);
				dialog.setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.browseWorkspaceTitle")); //$NON-NLS-1$
				dialog.open();
				Object[] result = dialog.getResult();
				if (result != null && result.length == 1) {
					String var = ToolScript.buildVariableTag(ToolScript.VAR_DIR_WORKSPACE, null);
					directoryField.setText(var + result[0].toString());
				}
			}
		});
		
		directoryBrowseFileSystem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
				dialog.setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.selectDirectory")); //$NON-NLS-1$
				dialog.setFilterPath(directoryField.getText());
				String selectedDirectory = dialog.open();
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
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.noScriptName"), IMessageProvider.NONE); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}
		
		value = locationField.getText().trim();
		if (value.length() < 1) {
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.noScriptLocation"), IMessageProvider.NONE); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			return;
		}

		getButton(IDialogConstants.OK_ID).setEnabled(true);
		
		File file = new File(value);
		if (!file.exists()) {
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.missingScriptLocation"), IMessageProvider.WARNING); //$NON-NLS-1$
			return;
		}
		
		value = directoryField.getText().trim();
		if (value.length() > 0) {
			file = new File(value);
			if (!file.exists()) {
				setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.missingScriptDirectory"), IMessageProvider.WARNING); //$NON-NLS-1$
				return;
			}
		}
		
		if (editMode)
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.editDialogMessage")); //$NON-NLS-1$
		else
			setMessage(ToolScriptMessages.getString("ToolScriptEditDialog.newDialogMessage")); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		String command = locationField.getText().trim();
		if (command.endsWith(".xml")) //$NON-NLS-1$
			script.setType(script.SCRIPT_TYPE_ANT);
		else
			script.setType(script.SCRIPT_TYPE_PROGRAM);
		script.setName(nameField.getText().trim());
		script.setLocation(command);
		script.setArguments(argumentsField.getText().trim());
		script.setWorkingDirectory(directoryField.getText().trim());
		script.setRefreshScope(refreshScope);
		
		super.okPressed();
	}
	
	/**
	 * Update the refresh scope field
	 */
	private void updateRefreshField() {
		String[] result = script.extractVariableTag(refreshScope);
		if (result[0] == null) {
			refreshScope = script.buildVariableTag(script.REFRESH_SCOPE_NONE, null);
			result[0] = script.REFRESH_SCOPE_NONE;
		}
		
		if (script.REFRESH_SCOPE_NONE.equals(result[0])) {
			refreshField.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshScopeNone")); //$NON-NLS-1$
			return;
		}
		if (script.REFRESH_SCOPE_WORKSPACE.equals(result[0])) {
			refreshField.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshScopeWorkspace")); //$NON-NLS-1$
			return;
		}
		if (script.REFRESH_SCOPE_PROJECT.equals(result[0])) {
			if (result[1] == null)
				refreshField.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshScopeProject")); //$NON-NLS-1$
			else
				refreshField.setText(ToolScriptMessages.format("ToolScriptEditDialog.refreshScopeProjectX", new Object[] {result[1]})); //$NON-NLS-1$
			return;
		}
		if (script.REFRESH_SCOPE_WORKING_SET.equals(result[0])) {
			if (result[1] == null) {
				refreshScope = script.buildVariableTag(script.REFRESH_SCOPE_NONE, null);
				refreshField.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshScopeNone")); //$NON-NLS-1$
			}
			else
				refreshField.setText(ToolScriptMessages.format("ToolScriptEditDialog.refreshScopeWorkingSet", new Object[] {result[1]})); //$NON-NLS-1$
			return;
		}
	}
	
	/**
	 * Internal dialog to show available resources from which
	 * the user can select one
	 */
	private class ResourceSelectionDialog extends SelectionDialog {
		IContainer root;
		TreeViewer wsTree;
		
		public ResourceSelectionDialog(Shell parent, IContainer root) {
			super(parent);
			this.root = root;
			setShellStyle(getShellStyle() | SWT.RESIZE);
			setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.browseWorkspaceTitle")); //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolScriptMessages.getString("ToolScriptEditDialog.selectResource")); //$NON-NLS-1$
			
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
		List list;
		
		public VariableSelectionDialog(Shell parent) {
			super(parent);
			setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.browseVarTitle")); //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolScriptMessages.getString("ToolScriptEditDialog.selectVar")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);
			
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.varWorkspaceDirLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.varProjectDirLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.varProjectXDirLabel")); //$NON-NLS-1$
			
			Path path = new Path(locationField.getText().trim());
			Project project = AntUtil.createAntProject(path);
			if (project != null)
				list.add(ToolScriptMessages.getString("ToolScriptEditDialog.varAntTargetLabel")); //$NON-NLS-1$
			
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
					result = ToolScript.buildVariableTag(ToolScript.VAR_DIR_WORKSPACE, null);
					break;

				case 1 :
					result = ToolScript.buildVariableTag(ToolScript.VAR_DIR_PROJECT, null);
					break;

				case 2 :
					ProjectSelectionDialog dialog;
					dialog = new ProjectSelectionDialog(getShell());
					dialog.open();
					Object[] name = dialog.getResult();
					if (name != null && name.length > 0)
						result = ToolScript.buildVariableTag(ToolScript.VAR_DIR_PROJECT, (String)name[0]);
					break;

				case 3 :
					TargetSelectionDialog targetDialog;
					targetDialog = new TargetSelectionDialog(getShell(), locationField.getText().trim());
					targetDialog.open();
					Object[] targets = targetDialog.getResult();
					if (targets != null && targets.length > 0) {
						result = ""; // $NON-NLS-1$
						for(int i=0; i<targets.length; i++) {
							result = result + ToolScript.buildVariableTag(ToolScript.VAR_ANT_TARGET, (String)targets[i]); 
						}
					}
					break;
			}
			
			if (result != null)
				setSelectionResult(new Object[] {result});
			super.okPressed();
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
			setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.browseRefreshTitle")); //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolScriptMessages.getString("ToolScriptEditDialog.selectRefresh")); //$NON-NLS-1$
			
			list = new List(dialogArea, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE | SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			list.setLayoutData(data);
			
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.refreshNothingLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.refreshWorkspaceLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.refreshProjectLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.refreshProjectXLabel")); //$NON-NLS-1$
			list.add(ToolScriptMessages.getString("ToolScriptEditDialog.refreshWorkingSetLabel")); //$NON-NLS-1$

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
					result = ToolScript.buildVariableTag(ToolScript.REFRESH_SCOPE_NONE, null);
					break;
					
				case 1 :
					result = ToolScript.buildVariableTag(ToolScript.REFRESH_SCOPE_WORKSPACE, null);
					break;

				case 2 :
					result = ToolScript.buildVariableTag(ToolScript.REFRESH_SCOPE_PROJECT, null);
					break;

				case 3 :
					ProjectSelectionDialog prjDialog;
					prjDialog = new ProjectSelectionDialog(getShell());
					prjDialog.open();
					Object[] name = prjDialog.getResult();
					if (name != null && name.length > 0)
						result = ToolScript.buildVariableTag(ToolScript.REFRESH_SCOPE_PROJECT, (String)name[0]);
					break;

				case 4 :
					IWorkingSetSelectionDialog setDialog;
					setDialog = PlatformUI.getWorkbench().getWorkingSetManager().createWorkingSetSelectionDialog(getShell(), false);
					setDialog.open();
					IWorkingSet[] sets = setDialog.getSelection();
					if (sets != null && sets.length > 0)
						result = ToolScript.buildVariableTag(ToolScript.REFRESH_SCOPE_WORKING_SET, sets[0].getName());
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
			setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.browseProjectTitle")); //$NON-NLS-1$
		}

		/* (non-Javadoc)
		 * Method declared on Dialog.
		 */
		protected Control createDialogArea(Composite parent) {
			// create composite 
			Composite dialogArea = (Composite)super.createDialogArea(parent);
			
			Label label = new Label(dialogArea, SWT.LEFT);
			label.setText(ToolScriptMessages.getString("ToolScriptEditDialog.selectProject")); //$NON-NLS-1$
			
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
		private Project project;
		private AntTargetLabelProvider labelProvider = new AntTargetLabelProvider();	
		
		public TargetSelectionDialog(Shell parent, String location) {
			super(parent);
			IPath path = new Path(location);
			project = AntUtil.createAntProject(path);
			setTitle(ToolScriptMessages.getString("ToolScriptEditDialog.varAntTargetLabel")); //$NON-NLS-1$
		}
		
		public void checkStateChanged(CheckStateChangedEvent e) {
			Target checkedTarget = (Target)e.getElement();
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
			label.setText(ToolScriptMessages.getString("ToolScriptEditDialog.selectTargets")); //$NON-NLS-1$
			
			listViewer = CheckboxTableViewer.newCheckList(dialogArea, SWT.BORDER);
			GridData data = new GridData(GridData.FILL_BOTH);
			data.heightHint = SIZING_SELECTION_PANE_HEIGHT;
			data.widthHint = SIZING_SELECTION_PANE_WIDTH;
			listViewer.getTable().setLayoutData(data);
			listViewer.setSorter(new ViewerSorter() {
				public int compare(Viewer viewer,Object o1,Object o2) {
					return ((Target)o1).getName().compareTo(((Target)o2).getName());
				}
			});
			
			if (project != null && project.getDefaultTarget() != null)
				labelProvider.setDefaultTargetName(project.getDefaultTarget());
			listViewer.setLabelProvider(labelProvider);
			listViewer.setContentProvider(new AntTargetContentProvider());
			listViewer.setInput(project);
			
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
			for (int i = 0; i < selectedTargets.size(); i++)
				result[i] = ((Target) selectedTargets.get(i)).getName();
			return result;
		}
	}
	
}
