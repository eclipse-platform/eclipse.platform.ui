/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.ui;

import org.eclipse.core.variables.IDynamicVariable;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.SWTUtil;
import org.eclipse.debug.internal.ui.preferences.StringVariablePreferencePage;
import org.eclipse.debug.internal.ui.stringsubstitution.IArgumentSelector;
import org.eclipse.debug.internal.ui.stringsubstitution.StringSubstitutionMessages;
import org.eclipse.debug.internal.ui.stringsubstitution.StringVariableLabelProvider;
import org.eclipse.debug.internal.ui.stringsubstitution.StringVariablePresentationManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * A dialog that prompts the user to choose and configure a string
 * substitution variable.
 * <p>
 * Clients may instantiate this class; not intended to be subclassed.
 * </p>
 * @since 3.1
 */
public class StringVariableSelectionDialog extends ElementListSelectionDialog {
	
	// button to configure variable's argument
	private Button fArgumentButton;
	// variable description
	private Text fDescriptionText;
	// the argument value
	private Text fArgumentText;
	private String fArgumentValue;
	private Button fEditVariablesButton;

	/**
	 * Constructs a new string substitution variable selection dialog.
	 *  
	 * @param parent parent shell
	 */
	public StringVariableSelectionDialog(Shell parent) {
		super(parent, new StringVariableLabelProvider());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setTitle(StringSubstitutionMessages.StringVariableSelectionDialog_2); 
		setMessage(StringSubstitutionMessages.StringVariableSelectionDialog_3); 
		setMultipleSelection(false);
		setElements(VariablesPlugin.getDefault().getStringVariableManager().getVariables());
	}
	
	/**
	 * Returns the variable expression the user generated from this
	 * dialog, or <code>null</code> if none.
	 *  
	 * @return variable expression the user generated from this
	 * dialog, or <code>null</code> if none
	 */
	public String getVariableExpression() {
		Object[] selected = getResult();
		if (selected != null && selected.length == 1) {
			IStringVariable variable = (IStringVariable)selected[0];
			StringBuffer buffer = new StringBuffer();
			buffer.append("${"); //$NON-NLS-1$
			buffer.append(variable.getName());
			if (fArgumentValue != null && fArgumentValue.length() > 0) {
				buffer.append(":"); //$NON-NLS-1$
				buffer.append(fArgumentValue);
			}
			buffer.append("}"); //$NON-NLS-1$
			return buffer.toString();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Control control = super.createDialogArea(parent);
		createArgumentArea((Composite)control);
		return control;
	}

	/**
	 * Creates an area to display a description of the selected variable
	 * and a button to configure the variable's argument.
	 * 
	 * @param parent parent widget
	 */
	private void createArgumentArea(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		container.setLayoutData(gd);
		container.setFont(parent.getFont());
		
		fEditVariablesButton = new Button(container, SWT.PUSH);
		fEditVariablesButton.setFont(container.getFont());
		fEditVariablesButton.setText(StringSubstitutionMessages.StringVariableSelectionDialog_0); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.horizontalSpan = 2;
		fEditVariablesButton.setLayoutData(gd);
		fEditVariablesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				editVariables();
			}
		});	
		
		Label desc = new Label(container, SWT.NONE);
		desc.setFont(parent.getFont());
		desc.setText(StringSubstitutionMessages.StringVariableSelectionDialog_6); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		desc.setLayoutData(gd);		
		
		Composite args = new Composite(container, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		args.setLayout(layout);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		args.setLayoutData(gd);
		args.setFont(container.getFont());
		
		fArgumentText = new Text(args, SWT.BORDER);
		fArgumentText.setFont(container.getFont());
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fArgumentText.setLayoutData(gd);		
		
		fArgumentButton = new Button(args, SWT.PUSH);
		fArgumentButton.setFont(parent.getFont());
		fArgumentButton.setText(StringSubstitutionMessages.StringVariableSelectionDialog_7); 
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gd.widthHint = SWTUtil.getButtonWidthHint(fArgumentButton);
		fArgumentButton.setLayoutData(gd);
		fArgumentButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				configureArgument();
			}
		});

		
		desc = new Label(container, SWT.NONE);
		desc.setFont(parent.getFont());
		desc.setText(StringSubstitutionMessages.StringVariableSelectionDialog_8); 
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		desc.setLayoutData(gd);
		
		fDescriptionText = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		fDescriptionText.setFont(container.getFont());
		fDescriptionText.setEditable(false);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.heightHint = 50;
		fDescriptionText.setLayoutData(gd);
	}

	protected void editVariables() {
		PreferencePage page = new StringVariablePreferencePage();
		page.setTitle(StringSubstitutionMessages.StringVariableSelectionDialog_1); 
		final IPreferenceNode targetNode = new PreferenceNode("org.eclipse.debug.ui.StringVariablePreferencePage", page); //$NON-NLS-1$
		
		PreferenceManager manager = new PreferenceManager();
		manager.addToRoot(targetNode);
		final PreferenceDialog dialog = new PreferenceDialog(getShell(), manager);
		
		final Display display = DebugUIPlugin.getStandardDisplay();
		BusyIndicator.showWhile(display, new Runnable() {
			public void run() {
				dialog.create();
				dialog.setMessage(targetNode.getLabelText());
				if(dialog.open() == IDialogConstants.OK_ID) {
					final IStringVariable[] elements = VariablesPlugin.getDefault().getStringVariableManager().getVariables();
					display.asyncExec(new Runnable() {
						public void run() {
							setListElements(elements);
						}
					});
				}
			}
		});		
	}

	/**
	 * Configures the argument for the selected variable.
	 */
	protected void configureArgument() {
		Object[] objects = getSelectedElements();
		IStringVariable variable = (IStringVariable)objects[0];
		IArgumentSelector selector = StringVariablePresentationManager.getDefault().getArgumentSelector(variable);
		String value = selector.selectArgument(variable, getShell());
		if (value != null) {
			fArgumentText.setText(value);
		}
	}

	/**
	 * Update variable description and argument button enablement.
	 * 
	 * @see org.eclipse.ui.dialogs.AbstractElementListSelectionDialog#handleSelectionChanged()
	 */
	protected void handleSelectionChanged() {
		super.handleSelectionChanged();
		Object[] objects = getSelectedElements();
		boolean buttonEnabled = false;
		boolean argEnabled = false;
		String text = null;
		if (objects.length == 1) {
			IStringVariable variable = (IStringVariable)objects[0];
			 IArgumentSelector selector = StringVariablePresentationManager.getDefault().getArgumentSelector(variable);
			 if (variable instanceof IDynamicVariable) {
			 	argEnabled = ((IDynamicVariable)variable).supportsArgument();
			 }
			 buttonEnabled = argEnabled && selector != null;
			 text = variable.getDescription();
		}
		if (text == null) {
			text = ""; //$NON-NLS-1$
		}
		fArgumentText.setEnabled(argEnabled);
		fArgumentButton.setEnabled(buttonEnabled);
		fDescriptionText.setText(text);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fArgumentValue = fArgumentText.getText().trim();
		super.okPressed();
	}

	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	private String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".STRING_VARIABLE_SELECTION_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	 /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    protected IDialogSettings getDialogBoundsSettings() {
    	 IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
         IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
         if (section == null) {
             section = settings.addNewSection(getDialogSettingsSectionName());
         } 
         return section;
    }
}
