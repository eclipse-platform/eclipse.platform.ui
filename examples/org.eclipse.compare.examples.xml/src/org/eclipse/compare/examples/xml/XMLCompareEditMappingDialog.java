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
package org.eclipse.compare.examples.xml;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import org.eclipse.compare.examples.xml.ui.StatusDialog;
import org.eclipse.compare.examples.xml.ui.StatusInfo;

/**
 * This class is used to add or edit a particular ID Mapping
 */
public class XMLCompareEditMappingDialog extends StatusDialog {
	
	private Mapping fMapping;
	private HashMap fIdmapHM;
	private boolean fEdit;
	
	private Text fElementText;
	private Text fSignatureText;
	private Text fIdAttributeText;

	private Button fIdTypeAttributeButton;
	private Button fIdTypeChildBodyButton;
	
	/*
	 * Constructs a new edit mapping dialog.
	 */		
	public XMLCompareEditMappingDialog(Shell parent, Mapping mapping, HashMap idmapHM, boolean edit) {
		super(parent);
	
		int shellStyle= getShellStyle();
		setShellStyle(shellStyle | SWT.MAX | SWT.RESIZE);

	
		fEdit= edit;
		if (fEdit)
			setTitle(XMLCompareMessages.getString("XMLCompareEditMappingDialog.editTitle")); //$NON-NLS-1$
		else
			setTitle(XMLCompareMessages.getString("XMLCompareEditMappingDialog.newTitle")); //$NON-NLS-1$

		fMapping= mapping;
		fIdmapHM= idmapHM;
	}
	
	/**
	 * Creates and returns the contents of the upper part 
	 * of the dialog (above the button bar).
	 *
	 * Subclasses should override.
	 *
	 * @param ancestor the parent composite to contain the dialog area
	 * @return the dialog area control
	 */	
	protected Control createDialogArea(Composite ancestor) {
		Composite composite= (Composite) super.createDialogArea(ancestor);
		
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		inner.setLayout(layout);
		inner.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		//Element
		Label label= new Label(inner, SWT.NULL);
		label.setText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.element")); //$NON-NLS-1$
		label.setLayoutData(new GridData());

		fElementText= new Text(inner, SWT.BORDER);
		fElementText.setText(fMapping.getElement());
		fElementText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fElementText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				doValidation();
			}
		});

		//Signature
		label= new Label(inner, SWT.NULL);
		label.setText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.signature")); //$NON-NLS-1$
		label.setLayoutData(new GridData());

		fSignatureText= new Text(inner, SWT.BORDER);
		fSignatureText.setText(fMapping.getSignature());
		GridData data= new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint= convertWidthInCharsToPixels(50);
		fSignatureText.setLayoutData(data);
		fSignatureText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				doValidation();
			}
		});
		
		//Id Attribute
		label= new Label(inner, SWT.NULL);
		label.setText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.idattribute")); //$NON-NLS-1$
		label.setLayoutData(new GridData());

		fIdAttributeText= new Text(inner, SWT.BORDER);

		fIdAttributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fIdAttributeText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e){
				doValidation();
			}
		});

		//Id Source
		createIdSourceGroup(inner);

		fElementText.setFocus();

		return composite;
	}
	
	/**
	 * Validate user input
	 */	
	private void doValidation() {
		StatusInfo status= new StatusInfo();
		String text= fElementText.getText();
		String mappingKey= Mapping.getKey(fSignatureText.getText(), text);
		String errormsg= ""; //$NON-NLS-1$
		boolean isError= false;
		if (text.length() == 0) {
			errormsg= XMLCompareMessages.getString("XMLCompareEditMappingDialog.error.noname"); //$NON-NLS-1$
			isError= true;
		} else if (XMLComparePreferencePage.containsInvalidCharacters(text)) {
			if (errormsg == "") errormsg= XMLCompareMessages.getString("XMLCompareEditMappingDialog.error.invalidname"); //$NON-NLS-2$ //$NON-NLS-1$
			isError= true;
		} else if (!fEdit && fIdmapHM != null && fIdmapHM.containsKey(mappingKey)) {
			if (errormsg == "") errormsg= XMLCompareMessages.getString("XMLCompareEditMappingDialog.error.mappingExists"); //$NON-NLS-2$ //$NON-NLS-1$
			isError= true;
		}
		text= fSignatureText.getText();
		if (XMLComparePreferencePage.containsInvalidCharacters(text)) {
			if (errormsg == "") errormsg= XMLCompareMessages.getString("XMLCompareEditMappingDialog.error.invalidsignature"); //$NON-NLS-2$ //$NON-NLS-1$
			isError= true;
		}
		text= fIdAttributeText.getText();
		if (text.length() == 0)
			isError= true;
		else if (XMLComparePreferencePage.containsInvalidCharacters(text)) {
			if (errormsg == "") errormsg= XMLCompareMessages.getString("XMLCompareEditMappingDialog.error.invalididattribute"); //$NON-NLS-2$ //$NON-NLS-1$
			isError= true;
		}
		if (isError) status.setError(errormsg);
		updateStatus(status);
	}
	
	/**
	 * Notifies that the ok button of this dialog has been pressed.
	 */	
	protected void okPressed() {
		fMapping.setElement(fElementText.getText());
		fMapping.setSignature(fSignatureText.getText());
		String idtext= fIdAttributeText.getText();
		if (fIdTypeChildBodyButton.getSelection()) {
			idtext= new Character(XMLStructureCreator.ID_TYPE_BODY) + idtext;
		}
		fMapping.setIdAttribute(idtext);
		super.okPressed();
	}
	
	private void createIdSourceGroup(Composite composite) {
		Label titleLabel= new Label(composite, SWT.NONE);
		titleLabel.setText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.idtype")); //$NON-NLS-1$
		titleLabel.setToolTipText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.idtype.tooltip")); //$NON-NLS-1$
	
		Composite buttonComposite= new Composite(composite, SWT.LEFT);
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		buttonComposite.setLayout(layout);
		composite.setData(new GridData());
	
		//attribute button
		fIdTypeAttributeButton= createRadioButton(buttonComposite, XMLCompareMessages.getString("XMLComparePreference.idtype.attribute")); //$NON-NLS-1$
		fIdTypeAttributeButton.setToolTipText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.idtype.attribute.tooltip")); //$NON-NLS-1$
	
		//child body button
		fIdTypeChildBodyButton= createRadioButton(buttonComposite, XMLCompareMessages.getString("XMLComparePreference.idtype.child_body")); //$NON-NLS-1$
		fIdTypeChildBodyButton.setToolTipText(XMLCompareMessages.getString("XMLCompareEditMappingDialog.idtype.childbody.tooltip")); //$NON-NLS-1$
	
		String idtext= fMapping.getIdAttribute();
		if (fEdit && idtext.charAt(0) == XMLStructureCreator.ID_TYPE_BODY) {
			idtext= idtext.substring(1,idtext.length());
			fIdTypeChildBodyButton.setSelection(true);
		} else
			fIdTypeAttributeButton.setSelection(true);
		fIdAttributeText.setText(idtext);
	
	}

	private Button createRadioButton(Composite parent, String label) {
		Button button= new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		GridData data= new GridData();
		button.setLayoutData(data);
		return button;
	}	
}