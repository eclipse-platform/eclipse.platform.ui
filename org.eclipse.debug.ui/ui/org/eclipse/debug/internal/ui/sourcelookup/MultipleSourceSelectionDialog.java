/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.debug.internal.ui.sourcelookup;

import org.eclipse.debug.internal.ui.IDebugHelpContextIds;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Dialog for source selection, in the case that multiple sources were found
 * in the path.
 * 
 * @since 3.0
 */
public class MultipleSourceSelectionDialog extends Dialog {
	private java.util.List fSourceList;
	private int fSelection;
	private Button fOkButton;
	private List fChoiceList;
	
	/**
	 * MultipleSourceSelectionDialog constructor.
	 * @param shell The parent shell, or <code>null</code> to create top level shell.
	 * @param methodName The name of the method being called.
	 */
	public MultipleSourceSelectionDialog(
			Shell shell,				
			java.util.List sourceList)
	{
		super(shell);	
		fSourceList = sourceList;		
	}
	
	/**
	 * @see Window#configureShell(Shell)
	 */
	protected void configureShell(Shell shell)
	{
		super.configureShell(shell);
		shell.setText(SourceLookupUIMessages.getString("multipleSourceDialog.title")); //$NON-NLS-1$
	}
	
	/**
	 * @see Window#create()
	 */
	public void create() {
		super.create();
		fOkButton.setFocus();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		
		// Label 1: "More than one source was found for ..."        
		Label message = new Label(composite, SWT.NULL); 
		//String[] substituteText = {fSourceName};
		message.setText(SourceLookupUIMessages.getString("multipleSourceDialog.message")); //$NON-NLS-1$
		
		// Blank space
		new Label(composite, SWT.NULL);
		
		// Label 2: "Select the source to display:"		
		Label instruction = new Label(composite, SWT.NULL);
		instruction.setText(SourceLookupUIMessages.getString("multipleSourceDialog.label")); //$NON-NLS-1$		
		
		// Create selectionComposite (file list)		
		Composite selectionComposite = new Composite(composite, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.numColumns = 2;
		selectionComposite.setLayout(layout);
		selectionComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		selectionComposite.setFont(parent.getFont());
		
		// Create single selection list
		fChoiceList = new List(selectionComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING; //gridData.FILL;
		gridData.verticalAlignment = GridData.BEGINNING;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;
		gridData.widthHint = convertWidthInCharsToPixels(80);
		gridData.heightHint = convertWidthInCharsToPixels(15);
		fChoiceList.setLayoutData(gridData);
		createSelectionList();
		
		WorkbenchHelp.setHelp(getShell(),  IDebugHelpContextIds.MULTIPLE_SOURCE_DIALOG);
		Dialog.applyDialogFont(composite);
		return composite;
	}
	
	/**
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		fSelection = fChoiceList.getSelectionIndex();
		if (fSelection != -1)
			super.okPressed();
	}
	
	/**
	 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
	 */
	protected void cancelPressed() {
		//TODO user will get standard source not found editor instead of
		//model specific editor, should the debug adapter have one
		fSelection = -1;
		super.cancelPressed();
	}
	
	/**
	 * Get the selection in the dialog
	 * @return the user's selection
	 */
	public Object getSelection() {
		if (fSelection != -1)
			return fSourceList.get(fSelection);
		else
			return null;
	}
	
	
	/**
	 * Create a button widget.
	 * @param parent The parent composite.
	 * @param style The button style.
	 * @param label The button label.
	 */
	protected Button createButton(Composite parent, int style, String label) {
		Button button = new Button(parent, style);
		button.setText(label);
		return button;
	}	
	
	/**
	 * Adds the source choices to the list in the dialog.
	 */
	private void createSelectionList () {
		String [] sourceStringList;
		
		if (fSourceList != null) {
			sourceStringList = generateSourceElementStringList();	    
			for (int i = 0; i < fSourceList.size(); i++)
			{
				fChoiceList.add(sourceStringList[i]);
				
			}			    
			if(fChoiceList.getItemCount() > 0)
				fChoiceList.setSelection(0);			
		}
	}	
	
	/** 
	 * Generate a string list of Source Elements for the dialog display
	 * 
	 */
	private String [] generateSourceElementStringList () {
		String[] sourceElementStringList = new String [fSourceList.size()];		
				
		for (int i = 0; i < fSourceList.size(); i++) {		
			//TODO need a way to get to model presentation? or label provider?
			sourceElementStringList[i] = fSourceList.get(i).toString();
		}		
		return sourceElementStringList;
	}    
	
		
}
