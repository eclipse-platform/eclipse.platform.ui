package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.toolscript.core.internal.ToolScript;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog box to enter the required information for running
 * a tool script.
 */
public class ToolScriptEditDialog extends TitleAreaDialog {
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
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		layout.numColumns = 2;
		topComp.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(data);

		// Build the script name field
		Composite nameComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		nameComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		nameComp.setLayoutData(data);
		
		Label nameLabel = new Label(nameComp, SWT.LEFT);
		nameLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.nameLabel")); //$NON-NLS-1$

		nameField = new Text(nameComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		nameField.setText(""); //$NON-NLS-1$
		nameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label nameSpacer = new Label(topComp, SWT.LEFT); // to fill 2nd column cell
		nameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Build the script location field
		Composite locFieldComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		locFieldComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		locFieldComp.setLayoutData(data);

		Label locLabel = new Label(locFieldComp, SWT.LEFT);
		locLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.locationLabel")); //$NON-NLS-1$
		locLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		locationField = new Text(locFieldComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		locationField.setText(""); //$NON-NLS-1$
		locationField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Build script location browse buttons
		Composite locButtonComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		locButtonComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		locButtonComp.setLayoutData(data);
		
		createPushButton(locButtonComp, "ToolScriptEditDialog.browseWkspButton1", true); //$NON-NLS-1$
		createPushButton(locButtonComp, "ToolScriptEditDialog.browseFileSysButton1", true); //$NON-NLS-1$

		// Build the script arguments field
		Composite argFieldComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		argFieldComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		argFieldComp.setLayoutData(data);

		Label argLabel = new Label(argFieldComp, SWT.LEFT);
		argLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.argumentLabel")); //$NON-NLS-1$
		argLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		argumentsField = new Text(argFieldComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		argumentsField.setText(""); //$NON-NLS-1$
		argumentsField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Build script arguments browse button
		Composite argButtonComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		argButtonComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		argButtonComp.setLayoutData(data);
		
		createPushButton(argButtonComp, "ToolScriptEditDialog.browseVarsButton", true); //$NON-NLS-1$

		// Build the working directory field
		Composite dirFieldComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		dirFieldComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		dirFieldComp.setLayoutData(data);

		Label dirLabel = new Label(dirFieldComp, SWT.LEFT);
		dirLabel.setText(ToolScriptMessages.getString("ToolScriptEditDialog.dirLabel")); //$NON-NLS-1$
		dirLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		directoryField = new Text(dirFieldComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		directoryField.setText(""); //$NON-NLS-1$
		directoryField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Build working directory browse buttons
		Composite dirButtonComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		dirButtonComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		dirButtonComp.setLayoutData(data);
		
		createPushButton(dirButtonComp, "ToolScriptEditDialog.browseWkspButton2", true); //$NON-NLS-1$
		createPushButton(dirButtonComp, "ToolScriptEditDialog.browseFileSysButton2", true); //$NON-NLS-1$

		// Build the refresh option field
		Composite refreshComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		refreshComp.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		refreshComp.setLayoutData(data);

		refreshCheckBox = new Button(refreshComp, SWT.CHECK);
		refreshCheckBox.setText(ToolScriptMessages.getString("ToolScriptEditDialog.refreshOption")); //$NON-NLS-1$
		refreshCheckBox.setLayoutData(new GridData());

		refreshField = new Text(refreshComp, SWT.SINGLE | SWT.H_SCROLL | SWT.BORDER);
		refreshField.setText(""); //$NON-NLS-1$
		refreshField.setEditable(false);
		refreshField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		refreshOptionButton = createPushButton(topComp, "ToolScriptEditDialog.refreshOptionButton", false);
		
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
	 * Creates an intialized push button
	 */
	private Button createPushButton(Composite parent, String labelKey, boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(ToolScriptMessages.getString(labelKey));
		button.setEnabled(enabled);
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		
		return button;
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
/*		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ToolScriptEditDialog dialog;
				dialog = new ToolScriptEditDialog(getShell(), null);
				dialog.open();
				ToolScript script = dialog.getToolScript();
				scripts.add(script);
				listViewer.add(script);
			}
		});
*/	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		String command = locationField.getText().trim();
		if (command.endsWith(".xml")) //$NON-NLS-1$
			script.setType(script.SCRIPT_TYPE_ANT);
		else
			script.setType(script.SCRIPT_TYPE_PROGRAM);
		script.setName(nameField.getText());
		script.setLocation(command);
		script.setArguments(argumentsField.getText());
		script.setWorkingDirectory(directoryField.getText());
		script.setRefreshScope(refreshField.getText());
		
		super.okPressed();
	}
}
