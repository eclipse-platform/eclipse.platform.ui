package org.eclipse.ui.externaltools.internal.ui;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.externaltools.internal.core.*;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog box to add, remove, and edit external tools.
 */
public class ConfigurationDialog extends TitleAreaDialog {
	// Minimum height in chars of the details text box.
	private static final int DETAILS_HEIGHT = 5;
	
	private ListViewer listViewer;
	private Button newButton;
	private Button editButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private Text detailText;
	private ExternalTool currentSelection;
	private ArrayList tools;

	/**
	 * Instantiate a new external tool configuration dialog.
	 *
	 * @param parentShell the parent SWT shell
	 */
	public ConfigurationDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(ToolMessages.getString("ConfigurationDialog.shellTitle")); //$NON-NLS-1$
		WorkbenchHelp.setHelp(
			shell,
			IHelpContextIds.CONFIGURE_DIALOG);
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
		tools = new ArrayList(ExternalToolsPlugin.getDefault().getRegistry().getExternalTools());
		
		Composite dialogComp = (Composite)super.createDialogArea(parent);
				
		// Set title and message now that the controls exist
		setTitle(ToolMessages.getString("ConfigurationDialog.dialogTitle")); //$NON-NLS-1$
		setMessage(ToolMessages.getString("ConfigurationDialog.dialogMessage")); //$NON-NLS-1$
		setTitleImage(ExternalToolsPlugin.getDefault().getImageDescriptor(ExternalToolsPlugin.IMG_WIZBAN_EXTERNAL_TOOLS).createImage());
			
		// Build the top container
		Composite topComp = new Composite(dialogComp, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		topComp.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		topComp.setLayoutData(data);

		// Build middle container with 2 columns
		Composite midComp = new Composite(topComp, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		midComp.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		midComp.setLayoutData(data);

		// Build the tools list
		Composite listComp = new Composite(midComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		listComp.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		listComp.setLayoutData(data);

		Label label = new Label(listComp, SWT.LEFT);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(ToolMessages.getString("ConfigurationDialog.toolList")); //$NON-NLS-1$
		
		listViewer = new ListViewer(listComp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer.setContentProvider(new ToolContentProvider());
		listViewer.setLabelProvider(new ToolLabelProvider());
		listViewer.setInput(tools);
		listViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				openEditToolDialog();
			}
		});

		// Build the button list
		Composite buttonComp = new Composite(midComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonComp.setLayout(layout);
		data = new GridData(GridData.FILL_VERTICAL);
		buttonComp.setLayoutData(data);

		label = new Label(buttonComp, SWT.LEFT); // spacer
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		newButton = createPushButton(buttonComp, "ConfigurationDialog.newButton", true); //$NON-NLS-1$
		editButton = createPushButton(buttonComp, "ConfigurationDialog.editButton", false); //$NON-NLS-1$
		removeButton = createPushButton(buttonComp, "ConfigurationDialog.removeButton", false); //$NON-NLS-1$
		label = new Label(buttonComp, SWT.LEFT); // spacer
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		upButton = createPushButton(buttonComp, "ConfigurationDialog.upButton", false); //$NON-NLS-1$
		downButton = createPushButton(buttonComp, "ConfigurationDialog.downButton", false); //$NON-NLS-1$
		
		// Build the details field
		label = new Label(topComp, SWT.LEFT);
		label.setText(ToolMessages.getString("ConfigurationDialog.details")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		detailText = new Text(topComp, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		detailText.setEditable(false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.heightHint = convertHeightInCharsToPixels(DETAILS_HEIGHT);
		detailText.setLayoutData(gridData);
		
		// Build the separator line
		Label separator = new Label(topComp, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Finish setup
		hookButtonActions();
		hookStateUpdates();
		
		return dialogComp;
	}
	
	private Button createPushButton(Composite parent, String labelKey, boolean enabled) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(ToolMessages.getString(labelKey));
		button.setEnabled(enabled);
		
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.heightHint = convertVerticalDLUsToPixels(IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		
		return button;
	}
	
	/**
	 * Adds the listeners required to handle the button
	 * actions
	 */
	private void hookButtonActions() {
		newButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				EditDialog dialog;
				dialog = new EditDialog(getShell(), null);
				if (dialog.open() == Window.OK) {
					ExternalTool tool = dialog.getExternalTool();
					tools.add(tool);
					listViewer.add(tool);
					listViewer.setSelection(new StructuredSelection(tool), true);
				}
			}
		});

		editButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openEditToolDialog();
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tools.remove(currentSelection);
				listViewer.remove(currentSelection);
			}
		});

		upButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = tools.indexOf(currentSelection);
				if (index < 1)
					return;
				Object tool = tools.get(index - 1);
				tools.set(index - 1, currentSelection);
				tools.set(index, tool);
				listViewer.refresh(false);
				updateUpDownButtons();
			}
		});

		downButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = tools.indexOf(currentSelection);
				if (index < 0 || index >= tools.size() - 1)
					return;
				Object tool = tools.get(index + 1);
				tools.set(index + 1, currentSelection);
				tools.set(index, tool);
				listViewer.refresh(false);
				updateUpDownButtons();
			}
		});
	}

	/**
	 * Adds a listener to control button enablement based on
	 * the current selection.
	 */
	private void hookStateUpdates() {
		listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				currentSelection = null;
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection() ;
					currentSelection = (ExternalTool)sel.getFirstElement();
				}

				editButton.setEnabled(currentSelection != null);
				removeButton.setEnabled(currentSelection != null);
				updateUpDownButtons();
				updateDetails();
			}
		});
	}

	/**
	 * Update the enable state of the up/down buttons
	 */
	private void updateUpDownButtons() {
		int selIndex = listViewer.getList().getSelectionIndex();
		int itemCount = listViewer.getList().getItemCount();
		upButton.setEnabled(currentSelection != null && selIndex > 0);
		downButton.setEnabled(currentSelection != null && selIndex < itemCount - 1);
	}

	/**
	 * Opens the edit external tool dialog on
	 * the currently selected external tool.
	 */
	private void openEditToolDialog() {
		if (currentSelection == null)
			return;
		EditDialog dialog;
		dialog = new EditDialog(getShell(), currentSelection);
		dialog.open();
		listViewer.update(currentSelection, null);
		updateDetails();
	}
	
	/**
	 * Update the detail field
	 */
	private void updateDetails() {
		if (currentSelection == null)
			detailText.setText(""); //$NON-NLS-1$
		else
			detailText.setText(ToolMessages.format("ConfigurationDialog.detailMessage", new Object[] {currentSelection.getLocation(), currentSelection.getArguments(), currentSelection.getWorkingDirectory()})); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		ExternalToolsPlugin.getDefault().getRegistry().setExternalTools(tools);
		super.okPressed();
	}

	
	/**
	 * Internal content provider of existing tool tools
	 */
	private class ToolContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return tools.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Internal label provider of existing tool tools
	 */
	private class ToolLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof ExternalTool)
				return ((ExternalTool)element).getName();
			else
				return "";//$NON-NLS-1$
		}
	}
}
