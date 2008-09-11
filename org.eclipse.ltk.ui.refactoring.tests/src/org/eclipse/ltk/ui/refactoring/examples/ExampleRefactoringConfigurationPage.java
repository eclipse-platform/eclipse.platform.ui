/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.ui.refactoring.examples;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.IWizardPage;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;

class ExampleRefactoringConfigurationPage extends UserInputWizardPage {

	private final ExampleRefactoring fRefactoring;
	private Text fOldText;
	private Text fNewText;

	public ExampleRefactoringConfigurationPage(ExampleRefactoring refactoring) {
		super("MyExampleRefactoringInputPage");
		fRefactoring= refactoring;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
    public void createControl(Composite parent) {
    	Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setFont(parent.getFont());

		Label label1= new Label(composite, SWT.NONE);
		label1.setText("&Find:");
		label1.setLayoutData(new GridData());

		fOldText= new Text(composite, SWT.BORDER);
		fOldText.setText("A");
		fOldText.selectAll();
		fOldText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

		Label label2= new Label(composite, SWT.NONE);
		label2.setText("&Replace with:");
		label2.setLayoutData(new GridData());

		fNewText= new Text(composite, SWT.BORDER);
		fNewText.setText("B");
		fNewText.selectAll();
		fNewText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));

		setControl(composite);

		Dialog.applyDialogFont(composite);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#performFinish()
     */
    protected boolean performFinish() {
		initializeRefactoring();
		storeSettings();
		return super.performFinish();
	}

    /* (non-Javadoc)
     * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#getNextPage()
     */
	public IWizardPage getNextPage() {
		initializeRefactoring();
		storeSettings();
		return super.getNextPage();
	}

	private void storeSettings() {
    }

	private void initializeRefactoring() {
		fRefactoring.setOldText(fOldText.getText());
		fRefactoring.setNewText(fNewText.getText());
    }
}