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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * An input dialog with multiple text fields.
 */
public class MultipleInputDialog extends Dialog {
	
	protected String title;
	protected String[] fieldLabels;
	protected String[] initialValues;
	protected int[] styles;
	
	protected Map textMap= new HashMap();
	protected Map valueMap= new HashMap();

	protected void cancelPressed() {
		super.cancelPressed();
	}

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
	 * 		no initial value should be displayed in any fields.
	 * @param styles the SWT style bits that should be applied to the text fields or
	 * 		<code>null</code> if the default style bits (SWT.SINGLE | SWT.BORDER)
	 * 		should be used.
	 */
	public MultipleInputDialog(Shell shell, String title, String[] fieldLabels, String[] initialValues, int[] styles) {
		super(shell);
		this.title= title;
		this.fieldLabels= fieldLabels;
		this.initialValues= initialValues;
		this.styles= styles;
	}

	protected Control createDialogArea(Composite parent) {
		Composite mainComposite= (Composite) super.createDialogArea(parent);
		for (int i = 0; i < fieldLabels.length; i++) {
			String fieldLabel = fieldLabels[i];
			Label label= new Label(mainComposite, SWT.NONE);
			label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
			label.setText(fieldLabel);
			int style= SWT.SINGLE | SWT.BORDER;
			if (styles != null && styles.length >= i) {
				style= styles[i];
			}
			Text text= new Text(mainComposite, style);
			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			if (initialValues != null && initialValues.length >= i) {
				text.setText(initialValues[i]);
			}
			textMap.put(fieldLabel, text);
		}
		return mainComposite;
	}
	/* (non-Javadoc)
	* Method declared in Window.
	*/
   protected void configureShell(Shell shell) {
	   super.configureShell(shell);
	   if (title != null)
		   shell.setText(title);
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
