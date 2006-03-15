/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.util.FileNameMatcher;
import org.eclipse.ui.PlatformUI;

public class IgnoreResourcesDialog extends TrayDialog {
	// resources that should be ignored
	private IResource[] resources;

	// preference keys
	private final String ACTION_KEY = "Action"; //$NON-NLS-1$
	private static final int ADD_NAME_ENTRY = 0;
	private static final int ADD_EXTENSION_ENTRY = 1;
	private static final int ADD_CUSTOM_ENTRY = 2;

	// dialogs settings that are persistent between workbench sessions
	private IDialogSettings settings;

	// buttons
	private Button addNameEntryButton;
	private Button addExtensionEntryButton;
	private Button addCustomEntryButton;
	private Text customEntryText;
	private Label statusMessageLabel;
	
	private int selectedAction;
	private String customPattern;
	
	// layout controls
	private static final int LABEL_INDENT_WIDTH = 32;

	/**
	 * Creates a new dialog for ignoring resources.
	 * @param shell the parent shell
	 * @param resources the array of resources to be ignored
	 */
	public IgnoreResourcesDialog(Shell shell, IResource[] resources) {
		super(shell);
		this.resources = resources;

		IDialogSettings workbenchSettings = CVSUIPlugin.getPlugin().getDialogSettings();
		this.settings = workbenchSettings.getSection("IgnoreResourcesDialog");//$NON-NLS-1$
		if (settings == null) {
			this.settings = workbenchSettings.addNewSection("IgnoreResourcesDialog");//$NON-NLS-1$
		}
		
		try {
			selectedAction = settings.getInt(ACTION_KEY);
		} catch (NumberFormatException e) {
			selectedAction = ADD_NAME_ENTRY;
		}
	}
	
	/**
	 * Determines the ignore pattern to use for a resource given the selected action.
	 * 
	 * @param resource the resource
	 * @return the ignore pattern for the specified resource
	 */
	public String getIgnorePatternFor(IResource resource) {
		switch (selectedAction) {
			case ADD_NAME_ENTRY:
				return resource.getName();
			case ADD_EXTENSION_ENTRY: {
				String extension = resource.getFileExtension();
				return (extension == null) ? resource.getName() : "*." + extension; //$NON-NLS-1$
			}
			case ADD_CUSTOM_ENTRY:
				return customPattern;
		}
		throw new IllegalStateException();
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
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		if (resources.length == 1) {
			newShell.setText(NLS.bind(CVSUIMessages.IgnoreResourcesDialog_titleSingle, new String[] { resources[0].getName() })); 
		} else {
			newShell.setText(NLS.bind(CVSUIMessages.IgnoreResourcesDialog_titleMany, new String[] { Integer.toString(resources.length) })); 
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		updateEnablements();
		return control;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected Control createDialogArea(Composite parent) {
		Composite top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout());
		
        PlatformUI.getWorkbench().getHelpSystem().setHelp(top, IHelpContextIds.ADD_TO_CVSIGNORE);
		
		createIndentedLabel(top, CVSUIMessages.IgnoreResourcesDialog_prompt, 0); 
		
		Listener selectionListener = new Listener() {
			public void handleEvent(Event event) {
				updateEnablements();
			}
		};
		Listener modifyListener = new Listener() {
			public void handleEvent(Event event) {
				validate();
			}
		};
		
		addNameEntryButton = createRadioButton(top, CVSUIMessages.IgnoreResourcesDialog_addNameEntryButton); 
		addNameEntryButton.addListener(SWT.Selection, selectionListener);
		addNameEntryButton.setSelection(selectedAction == ADD_NAME_ENTRY);
		createIndentedLabel(top, CVSUIMessages.IgnoreResourcesDialog_addNameEntryExample, LABEL_INDENT_WIDTH); 
		
		addExtensionEntryButton = createRadioButton(top, CVSUIMessages.IgnoreResourcesDialog_addExtensionEntryButton); 
		addExtensionEntryButton.addListener(SWT.Selection, selectionListener);
		addExtensionEntryButton.setSelection(selectedAction == ADD_EXTENSION_ENTRY);
		createIndentedLabel(top, CVSUIMessages.IgnoreResourcesDialog_addExtensionEntryExample, LABEL_INDENT_WIDTH); 

		addCustomEntryButton = createRadioButton(top, CVSUIMessages.IgnoreResourcesDialog_addCustomEntryButton); 
		addCustomEntryButton.addListener(SWT.Selection, selectionListener);
		addCustomEntryButton.setSelection(selectedAction == ADD_CUSTOM_ENTRY);
		createIndentedLabel(top, CVSUIMessages.IgnoreResourcesDialog_addCustomEntryExample, LABEL_INDENT_WIDTH); 
		
		customEntryText = createIndentedText(top, resources[0].getName(), LABEL_INDENT_WIDTH);
		customEntryText.addListener(SWT.Modify, modifyListener);

		statusMessageLabel = createIndentedLabel(top, "", 0); //$NON-NLS-1$
        Dialog.applyDialogFont(parent);
		return top;
	}

	/* (non-Javadoc)
	 * Method declared on Dialog.
	 */
	protected void okPressed() {
		settings.put(ACTION_KEY, selectedAction);
		super.okPressed();
	}

	private Label createIndentedLabel(Composite parent, String text, int indent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText(text);
		GridData data = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		data.horizontalIndent = indent;
		label.setLayoutData(data);
		return label;
	}

	private Text createIndentedText(Composite parent, String text, int indent) {
		Text textbox = new Text(parent, SWT.BORDER);
		textbox.setText(text);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data.horizontalIndent = indent;
		textbox.setLayoutData(data);
		return textbox;
	}
	
	private Button createRadioButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.RADIO);
		button.setText(text);
		button.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		return button;
	}

	private void updateEnablements() {
		if (addNameEntryButton.getSelection()) {
			selectedAction = ADD_NAME_ENTRY;
		} else if (addExtensionEntryButton.getSelection()) {
			selectedAction = ADD_EXTENSION_ENTRY;
		} else if (addCustomEntryButton.getSelection()) {
			selectedAction = ADD_CUSTOM_ENTRY;
		}
		customEntryText.setEnabled(selectedAction == ADD_CUSTOM_ENTRY);
		validate();
	}
	
	private void validate() {
		if (selectedAction == ADD_CUSTOM_ENTRY) {
			customPattern = customEntryText.getText();
			if (customPattern.length() == 0) {
				setError(CVSUIMessages.IgnoreResourcesDialog_patternMustNotBeEmpty); 
				return;
			}
			FileNameMatcher matcher = new FileNameMatcher(new String[] { customPattern });
			for (int i = 0; i < resources.length; i++) {
				String name = resources[i].getName();
				if (! matcher.match(name)) {
					setError(NLS.bind(CVSUIMessages.IgnoreResourcesDialog_patternDoesNotMatchFile, new String[] { name })); 
					return;
				}
			}
		}
		setError(null);
	}
	
	private void setError(String text) {
		if (text == null) {
			statusMessageLabel.setText(""); //$NON-NLS-1$
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		} else {
			statusMessageLabel.setText(text);
			statusMessageLabel.setForeground(JFaceColors.getErrorText(getShell().getDisplay()));
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
	}
}
