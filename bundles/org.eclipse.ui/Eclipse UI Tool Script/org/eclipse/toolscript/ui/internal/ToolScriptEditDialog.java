package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.toolscript.core.internal.ToolScript;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

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
	
	private Text nameField;
	private Text locationField;
	private Text argumentsField;
	private Text directoryField;
	private Button refreshCheckBox;
	private Text refreshField;
	private Button locationBrowseWorkspace;
	private Button locationBrowseFileSystem;
	private Button argumentsBrowseVariable;
	private Button directoryBrowseWorkspace;
	private Button directoryBrowseFileSystem;
	private Button refreshOptionButton;
	
	private boolean editMode = false;
	private ToolScript script;

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
		nameLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.nameLabel"));
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
		locationBrowseWorkspace.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseWkspButton1"));
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
		locationLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.locationLabel"));
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
		locationBrowseFileSystem.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseFileSysButton1"));
		buttonData[1] = new FormData();
		buttonData[1].left = new FormAttachment(locationBrowseWorkspace, 0, SWT.LEFT);
		buttonData[1].top = new FormAttachment(locationBrowseWorkspace, WIDGET_SPACE, SWT.BOTTOM);
		locationBrowseFileSystem.setLayoutData(buttonData[1]);
		checkForMaxWidth(locationBrowseFileSystem);

		// Create label for arguments text field.
		Label argumentsLabel = new Label(topComp, SWT.NONE);
		argumentsLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.argumentLabel"));
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
		argumentsBrowseVariable.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseVarsButton"));
		buttonData[2] = new FormData();
		buttonData[2].left = new FormAttachment(locationBrowseFileSystem, 0, SWT.LEFT);
		buttonData[2].bottom = new FormAttachment(argumentsField, 0, SWT.BOTTOM);
		argumentsBrowseVariable.setLayoutData(buttonData[2]);
		checkForMaxWidth(argumentsBrowseVariable);

		// Create directory browse workspace button.
		directoryBrowseWorkspace = new Button(topComp, SWT.PUSH);
		directoryBrowseWorkspace.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseWkspButton2"));
		buttonData[3] = new FormData();
		buttonData[3].left = new FormAttachment(argumentsBrowseVariable, 0, SWT.LEFT);
		buttonData[3].top = new FormAttachment(argumentsBrowseVariable, GROUP_SPACE, SWT.BOTTOM);
		directoryBrowseWorkspace.setLayoutData(buttonData[3]);
		checkForMaxWidth(directoryBrowseWorkspace);

		// Create label for directory text field.
		Label dirLabel = new Label(topComp, SWT.NONE);
		dirLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.dirLabel"));
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
		directoryBrowseFileSystem.setText(ToolScriptMessages.getString("ToolScriptEditDialog.browseFileSysButton2"));
		buttonData[4] = new FormData();
		buttonData[4].left = new FormAttachment(argumentsField, MARGIN_SPACE, SWT.RIGHT);
		buttonData[4].bottom = new FormAttachment(directoryField, 0, SWT.BOTTOM);
		directoryBrowseFileSystem.setLayoutData(buttonData[4]);
		checkForMaxWidth(directoryBrowseFileSystem);
		
		// Create refresh check box and label.
		refreshCheckBox = new Button(topComp, SWT.CHECK);
		refreshCheckBox.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshOption")); //$NON-NLS-1$
		data = new FormData();
		data.left = new FormAttachment(0,0);
		data.top = new FormAttachment(directoryField, GROUP_SPACE+buttonLabelHeightDiff, SWT.BOTTOM);
		refreshCheckBox.setLayoutData(data);
		
		// Create refresh text field.
		refreshField = new Text(topComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		refreshField.setEditable(false);
		data = new FormData();
		data.left = new FormAttachment(refreshCheckBox, MARGIN_SPACE, SWT.RIGHT);
		data.top = new FormAttachment(refreshCheckBox, 0, SWT.TOP);
		data.width = FIELD_WIDTH - MARGIN_SPACE - refreshCheckBox.computeSize(SWT.DEFAULT, SWT.DEFAULT, false).x;
		refreshField.setLayoutData(data);

		// Create refresh option button.
		refreshOptionButton = new Button(topComp, SWT.PUSH);
		refreshOptionButton.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshOptionButton"));
		refreshOptionButton.setEnabled(false);
		buttonData[5] = new FormData();
		buttonData[5].left = new FormAttachment(directoryBrowseFileSystem, 0, SWT.LEFT);
		buttonData[5].top = new FormAttachment(refreshField, 0, SWT.TOP);
		refreshOptionButton.setLayoutData(buttonData[5]);
		
		// give all the buttons the same width
		for (int i=0; i<buttonData.length; i++) {
			buttonData[i].width = maxButtonWidth;	
		}	
		
		refreshCheckBox.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				refreshField.setEnabled(refreshCheckBox.getSelection());
				refreshOptionButton.setEnabled(refreshCheckBox.getSelection());
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
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
		
		// Finish setup
		hookButtonActions();
		
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
				locationField.setText(ToolScript.VAR_DIR_WORKSPACE + resource.getFullPath());
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
					directoryField.setText(ToolScript.VAR_DIR_WORKSPACE + result[0].toString());
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
		script.setRefreshScope(refreshField.getText().trim());
		
		super.okPressed();
	}
	
	private class ResourceSelectionDialog extends SelectionDialog {
		// sizing constants
		private static final int	SIZING_SELECTION_PANE_HEIGHT = 250;
		private static final int	SIZING_SELECTION_PANE_WIDTH = 300;

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
}
