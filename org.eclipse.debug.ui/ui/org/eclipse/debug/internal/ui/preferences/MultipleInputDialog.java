	/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An input dialog with multiple text fields.
 */
public class MultipleInputDialog extends Dialog {
	
	/**
	 * The dialog's title.
	 */
	protected String title;
	/**
	 * The labels of the fields this dialog will display.
	 */
	protected String[] fieldLabels;
	/**
	 * The initial values of the fields this dialog will display.
	 */
	protected String[] initialValues;
	
	/**
	 * Mapping of field names to the Text fields created
	 * for them.
	 */
	protected Map textMap= new HashMap();
	/**
	 * Mapping of field names to the value the associated
	 * field contained when the user pressed the OK button.
	 */
	protected Map valueMap= new HashMap();
	/**
	 * List of field names (Strings) whose associated text fields
	 * should be validated as the user types.
	 */
	protected List validateList= new ArrayList();

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		Set entries= textMap.entrySet();
		Iterator iter= entries.iterator();
		while (iter.hasNext()) {
			Map.Entry element = (Map.Entry) iter.next();
			valueMap.put(element.getKey(), ((Text) element.getValue()).getText());
		}
		textMap.clear();
		super.okPressed();
	}

	/**
	 * Creates a new input dialog with text fields for each of the input field labels.
	 * The text fields are initialized to the given values.
	 * @param shell the parent shell, or <code>null</code> to create a top-level shell.
	 * @param title the dialog title
	 * @param fieldLabels the labels of the text fields to create
	 * @param initialValues the initial values of the text fields or <code>null</code> if
	 * 		no initial value should be displayed in any fields. The position of the initial
	 * 		values is taken to align with the position of the desired text field. 
	 */
	public MultipleInputDialog(Shell shell, String title, String[] fieldLabels, String[] initialValues) {
		super(shell);
		this.title= title;
		this.fieldLabels= fieldLabels;
		this.initialValues= initialValues;
	}
	
	/**
	 * Creates the dialog and initializes button enablement.
	 * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control control= super.createContents(parent);
		Iterator iter= validateList.iterator();
		while (iter.hasNext()) {
			Text text= (Text) textMap.get(iter.next());
			if (text == null) {
				continue;
			}
			validateNotEmpty(text);
		}
		return control;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite mainComposite= (Composite) super.createDialogArea(parent);
		createFields(mainComposite);
		return mainComposite;
	}
	
	/**
	 * Creates the text fields with their labels.
	 * @param mainComposite
	 */
	protected void createFields(Composite mainComposite) {
		Composite fieldComposite= new Composite(mainComposite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		GridData gridData = new GridData(GridData.FILL_BOTH);
		fieldComposite.setLayout(layout);
		fieldComposite.setLayoutData(gridData);
		for (int i = 0; i < fieldLabels.length; i++) {
			String fieldLabel = fieldLabels[i];
			Label label= new Label(fieldComposite, SWT.NONE);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			label.setText(fieldLabel);
			final Text text= new Text(fieldComposite, SWT.SINGLE | SWT.BORDER);
			gridData= new GridData(GridData.FILL_HORIZONTAL);
			gridData.widthHint= 200;
			text.setLayoutData(gridData);
			if (initialValues != null && initialValues.length >= i) {
				text.setText(initialValues[i]);
			}
			if (validateList.contains(fieldLabel)) {
				text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						validateNotEmpty(text);
					}
				});
			}
			textMap.put(fieldLabel, text);
		}
	}
	
	/**
	 * Validates the given text to make sure it has a non-empty value.
	 * Disabled the OK button if the text is empty. Enables it otherwise.
	 * @param text the text field to examine
	 */
	public void validateNotEmpty(Text text) {
		boolean enable= text.getText().trim().length() > 0; 
		getButton(IDialogConstants.OK_ID).setEnabled(enable);
	}
	
	/**
	 * Tells this dialog to disallow an empty value for the text field
	 * with the given label. The dialog's OK button will be disabled
	 * as long as the field remains empty. Has no effect if a text
	 * field with the given label does not exist.
	 * @param fieldLabel the label of the text field that should not allow
	 * 		an empty value.
	 */
	public void disallowEmpty(String fieldLabel) {
		validateList.add(fieldLabel);
	}

	/**
	 * Sets the dialog title.
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}
	
	/**
	 * Returns the value of the text field with the given label or
	 * <code>null</code> if no field exists with the given label.
	 * @param fieldLabel the label of the field whose value should be retrieved
	 * @return the value of the field with the given label
	 */
	public String getValue(String fieldLabel) {
		return (String) valueMap.get(fieldLabel);
	}

}
