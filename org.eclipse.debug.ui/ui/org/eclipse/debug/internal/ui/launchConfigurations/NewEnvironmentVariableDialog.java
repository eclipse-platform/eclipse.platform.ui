/*******************************************************************************
* Copyright (c) 2000, 2004 IBM Corporation and others.
* All rights reserved. This program and the accompanying materials 
* are made available under the terms of the Common Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/cpl-v10.html
* 
* Contributors:
*     IBM Corporation - initial API and implementation
*******************************************************************************/
package org.eclipse.debug.internal.ui.launchConfigurations;

import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.preferences.IDebugPreferenceConstants;
import org.eclipse.debug.internal.ui.preferences.MultipleInputDialog;
import org.eclipse.debug.internal.ui.stringsubstitution.StringVariableSelectionDialog;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NewEnvironmentVariableDialog extends MultipleInputDialog {
	
	private Button variablesButton;
	private Text nameText, valText;
	private EnvironmentVariable envVar;
	
	public NewEnvironmentVariableDialog(Shell shell, String[] labels) {
		super(shell, LaunchConfigurationsMessages.getString("NewEnvironmentVariableDialog.1"), labels, null); //$NON-NLS-1$ 
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}
	
	protected void createFields(Composite mainComposite) {
		Composite comp = new Composite(mainComposite, SWT.NO_TRIM);
		GridLayout layout= new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		comp.setLayout(layout);
		comp.setLayoutData(gridData);
		
		
		Composite nameComposite= new Composite(comp, SWT.NO_TRIM);
		layout= new GridLayout(2, false);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		nameComposite.setLayout(layout);
		nameComposite.setLayoutData(gridData);
		
		Label nameLabel = new Label(nameComposite, SWT.NONE);
		nameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		nameLabel.setText(fieldLabels[0]);
		
		nameText= new Text(nameComposite, SWT.SINGLE | SWT.BORDER);
		gridData= new GridData(GridData.FILL_HORIZONTAL);
		nameText.setLayoutData(gridData);
		textMap.put(fieldLabels[0], nameText);
		
		
		Composite valComposite = new Composite(comp, SWT.NO_TRIM);
		layout= new GridLayout(3, false);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		valComposite.setLayout(layout);
		valComposite.setLayoutData(gridData);
		
		Label valLabel = new Label(valComposite, SWT.NONE);
		gridData = new GridData(GridData.BEGINNING);
		valLabel.setLayoutData(gridData);
		valLabel.setText(fieldLabels[1]);
		
		valText= new Text(valComposite, SWT.SINGLE | SWT.BORDER);
		gridData= new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.widthHint = 250;
		valText.setLayoutData(gridData);
		textMap.put(fieldLabels[1], valText);
		
		variablesButton = new Button(valComposite, SWT.NONE);
		variablesButton.setText(LaunchConfigurationsMessages.getString("NewEnvironmentVariableDialog.4")); //$NON-NLS-1$
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		variablesButton.setLayoutData(gridData);
		variablesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent se) {
				showVariableDialog();
			}
		});
		
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateButtonState();
			}
		};
		nameText.addModifyListener(modifyListener);
		valText.addModifyListener(modifyListener);
		
		Dialog.applyDialogFont(mainComposite);
	}
	
	public EnvironmentVariable getEnviromentVariable() {
		return envVar;
	}
	
	private void setEnvironmentVariable() {
		String name = nameText.getText();
		String value = valText.getText();
		if (name != null && value != null && name.length() > 0 && value.length() >0) {
			envVar = new EnvironmentVariable(name.trim(), value.trim());
		} else {
			envVar = null;
		}
	}	
	
	private void updateButtonState() {
		String name = nameText.getText();
		String val = valText.getText();
		boolean enableOK = name!=null && val!=null && name.trim().length()>0 && val.trim().length()>0;
		getButton(IDialogConstants.OK_ID).setEnabled(enableOK);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		setEnvironmentVariable();
		super.okPressed();
	}
	
	private void showVariableDialog() {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		int buttonPressed = dialog.open();
		if (buttonPressed != Window.OK) {
			return;
		}
		
		String variable = dialog.getVariableExpression();
		if (variable != null) {
			valText.append(variable);
		}
	}
	
	
	protected IDialogSettings getDialogSettings() {
		IDialogSettings settings = DebugUIPlugin.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		} 
		return section;
	}
	
	/**
	 * Returns the name of the section that this dialog stores its settings in
	 * 
	 * @return String
	 */
	protected String getDialogSettingsSectionName() {
		return IDebugUIConstants.PLUGIN_ID + ".NEW_ENVIRONMENT_VARIABLE_DIALOG_SECTION"; //$NON-NLS-1$
	}
	
	private void persistShellGeometry() {
		Point shellLocation = getShell().getLocation();
		Point shellSize = getShell().getSize();
		IDialogSettings settings = getDialogSettings();
		settings.put(IDebugPreferenceConstants.DIALOG_ORIGIN_X, shellLocation.x);
		settings.put(IDebugPreferenceConstants.DIALOG_ORIGIN_Y, shellLocation.y);
		settings.put(IDebugPreferenceConstants.DIALOG_WIDTH, shellSize.x);
		settings.put(IDebugPreferenceConstants.DIALOG_HEIGHT, shellSize.y);
	}	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		updateButtonState();
		
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IDebugPreferenceConstants.DIALOG_ORIGIN_X);
			y = settings.getInt(IDebugPreferenceConstants.DIALOG_ORIGIN_Y);
			return new Point(x,y);
		} catch (NumberFormatException e) {
		}
		return super.getInitialLocation(initialSize);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point size = super.getInitialSize();
		
		IDialogSettings settings = getDialogSettings();
		try {
			int x, y;
			x = settings.getInt(IDebugPreferenceConstants.DIALOG_WIDTH);
			y = settings.getInt(IDebugPreferenceConstants.DIALOG_HEIGHT);
			return new Point(Math.max(x,size.x),Math.max(y,size.y));
		} catch (NumberFormatException e) {
		}
		return size;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		persistShellGeometry();
		return super.close();
	}
}