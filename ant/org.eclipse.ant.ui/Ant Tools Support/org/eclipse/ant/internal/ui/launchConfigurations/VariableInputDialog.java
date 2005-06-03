/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui.launchConfigurations;

import org.eclipse.ant.internal.ui.preferences.DialogSettingsHelper;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class VariableInputDialog extends Dialog {
	
	private static String DIALOG_SETTINGS_SECTION = "RuntimeClasspathAction.VariableInputDialog"; //$NON-NLS-1$
	private Text fText;
	private String fVariableString;
	
	public VariableInputDialog(Shell shell) {
		super(shell);
		setShellStyle(SWT.RESIZE | getShellStyle());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
        Composite inner= (Composite) super.createDialogArea(parent);
		((GridLayout)inner.getLayout()).numColumns= 2;
		
		Label label = new Label(inner, SWT.NONE);
		label.setText(AntLaunchConfigurationMessages.AddVariableStringAction_2); //$NON-NLS-1$
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
        gd.horizontalSpan= 2;
		label.setLayoutData(gd);
		
		fText = new Text(inner, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 200;
		fText.setLayoutData(gd);
		
		Button button = new Button(inner, SWT.PUSH); 
		button.setText(AntLaunchConfigurationMessages.AddVariableStringAction_3); //$NON-NLS-1$		
		button.addSelectionListener(new SelectionAdapter() {
			public  void widgetSelected(SelectionEvent se) {
				getVariable();
			}
		});
		
		applyDialogFont(parent);
		return inner;
	}
	
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(AntLaunchConfigurationMessages.AddVariableStringAction_4); //$NON-NLS-1$
	}
	
	private void getVariable() {
		StringVariableSelectionDialog variableDialog = new StringVariableSelectionDialog(getShell());
		int returnCode = variableDialog.open();
		if (returnCode == IDialogConstants.OK_ID) {
			String variable = variableDialog.getVariableExpression();
			if (variable != null) {
				fText.insert(variable);
			}
		}			
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		String variableString = fText.getText();
		if (variableString != null && variableString.trim().length() > 0) {
			fVariableString= variableString;
		} else {
			fVariableString= null;
		}
		super.okPressed();
	}
	
	public String getVariableString() {
		return fVariableString;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#close()
	 */
	public boolean close() {
		DialogSettingsHelper.persistShellGeometry(getShell(), DIALOG_SETTINGS_SECTION);
		return super.close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	protected Point getInitialLocation(Point initialSize) {
		Point p = DialogSettingsHelper.getInitialLocation(DIALOG_SETTINGS_SECTION);
		return p != null ? p : super.getInitialLocation(initialSize);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#getInitialSize()
	 */
	protected Point getInitialSize() {
		Point p = super.getInitialSize();
		return DialogSettingsHelper.getInitialSize(DIALOG_SETTINGS_SECTION, p);
	}
}
