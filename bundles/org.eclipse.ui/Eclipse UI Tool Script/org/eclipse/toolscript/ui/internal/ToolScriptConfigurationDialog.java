package org.eclipse.toolscript.ui.internal;

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
**********************************************************************/
import java.util.ArrayList;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
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
import org.eclipse.toolscript.core.internal.ToolScriptPlugin;
import org.eclipse.toolscript.core.internal.ToolScriptRegistry;
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * Dialog box to add, remove, and edit external tool scripts.
 */
public class ToolScriptConfigurationDialog extends TitleAreaDialog {
	private ListViewer listViewer;
	private Button newButton;
	private Button editButton;
	private Button removeButton;
	private Button upButton;
	private Button downButton;
	private Text detailText;
	private ToolScript currentSelection;
	private ArrayList scripts;

	/**
	 * Instantiate a new tool script configuration dialog.
	 *
	 * @param parentShell the parent SWT shell
	 */
	public ToolScriptConfigurationDialog(Shell parentShell) {
		super(parentShell);
	}

	/* (non-Javadoc)
	 * Method declared in Window.
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(ToolScriptMessages.getString("ToolScriptConfigurationDialog.shellTitle")); //$NON-NLS-1$
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
		scripts = new ArrayList(ToolScriptPlugin.getDefault().getRegistry().getToolScripts());
		
		Composite dialogComp = (Composite)super.createDialogArea(parent);
				
		// Set title and message now that the controls exist
		setTitle(ToolScriptMessages.getString("ToolScriptConfigurationDialog.dialogTitle")); //$NON-NLS-1$
		setMessage(ToolScriptMessages.getString("ToolScriptConfigurationDialog.dialogMessage")); //$NON-NLS-1$
		
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

		// Build the script list
		Composite listComp = new Composite(midComp, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		listComp.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		listComp.setLayoutData(data);

		Label label = new Label(listComp, SWT.LEFT);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		label.setText(ToolScriptMessages.getString("ToolScriptConfigurationDialog.scriptList")); //$NON-NLS-1$
		
		listViewer = new ListViewer(listComp, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		listViewer.getList().setLayoutData(new GridData(GridData.FILL_BOTH));
		listViewer.setContentProvider(new ScriptContentProvider());
		listViewer.setLabelProvider(new ScriptLabelProvider());
		listViewer.setInput(scripts);

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
		newButton = createPushButton(buttonComp, "ToolScriptConfigurationDialog.newButton", true); //$NON-NLS-1$
		editButton = createPushButton(buttonComp, "ToolScriptConfigurationDialog.editButton", false); //$NON-NLS-1$
		removeButton = createPushButton(buttonComp, "ToolScriptConfigurationDialog.removeButton", false); //$NON-NLS-1$
		label = new Label(buttonComp, SWT.LEFT); // spacer
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		upButton = createPushButton(buttonComp, "ToolScriptConfigurationDialog.upButton", false); //$NON-NLS-1$
		downButton = createPushButton(buttonComp, "ToolScriptConfigurationDialog.downButton", false); //$NON-NLS-1$
		
		// Build the details field
		label = new Label(topComp, SWT.LEFT);
		label.setText(ToolScriptMessages.getString("ToolScriptConfigurationDialog.details")); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		detailText = new Text(topComp, SWT.WRAP | SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		detailText.setEditable(false);
		detailText.setText("\n\n\n\n\n"); //$NON-NLS-1$
		detailText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
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
	 * Adds the listeners required to handle the button
	 * actions
	 */
	private void hookButtonActions() {
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ToolScriptEditDialog dialog;
				dialog = new ToolScriptEditDialog(getShell(), null);
				dialog.open();
				ToolScript script = dialog.getToolScript();
				scripts.add(script);
				listViewer.add(script);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		editButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ToolScriptEditDialog dialog;
				dialog = new ToolScriptEditDialog(getShell(), currentSelection);
				dialog.open();
				listViewer.update(currentSelection, null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		removeButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				scripts.remove(currentSelection);
				listViewer.remove(currentSelection);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		upButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = scripts.indexOf(currentSelection);
				if (index < 1)
					return;
				Object script = scripts.get(index - 1);
				scripts.set(index - 1, currentSelection);
				scripts.set(index, script);
				listViewer.update(script, null);
				listViewer.update(currentSelection, null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		downButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				int index = scripts.indexOf(currentSelection);
				if (index < 0 || index >= scripts.size() - 1)
					return;
				Object script = scripts.get(index + 1);
				scripts.set(index + 1, currentSelection);
				scripts.set(index, script);
				listViewer.update(script, null);
				listViewer.update(currentSelection, null);
			}
			public void widgetDefaultSelected(SelectionEvent e) {
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
					currentSelection = (ToolScript)sel.getFirstElement();
				}

				int selIndex = listViewer.getList().getSelectionIndex();
				int itemCount = listViewer.getList().getItemCount();
				
				editButton.setEnabled(currentSelection != null);
				removeButton.setEnabled(currentSelection != null);
				upButton.setEnabled(currentSelection != null && selIndex > 0);
				downButton.setEnabled(currentSelection != null && selIndex < itemCount - 1);
			}
		});
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		ToolScriptPlugin.getDefault().getRegistry().setToolScripts(scripts);
		super.okPressed();
	}

	
	/**
	 * Internal content provider of existing tool scripts
	 */
	private class ScriptContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return scripts.toArray();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	/**
	 * Internal label provider of existing tool scripts
	 */
	private class ScriptLabelProvider extends LabelProvider {
		public String getText(Object element) {
			if (element instanceof ToolScript)
				return ((ToolScript)element).getName();
			else
				return "";//$NON-NLS-1$
		}
	}
}
