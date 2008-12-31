/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.ltk.core.refactoring.RefactoringStatus;

public class RefactoringStatusDialog extends Dialog {

	private RefactoringStatus fStatus;
	private String fWindowTitle;
	private boolean fBackButton;
	private boolean fLightWeight;

	public RefactoringStatusDialog(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton) {
		super(parent);
		fStatus= status;
		fWindowTitle= windowTitle;
		fBackButton= backButton;
	}

	/*
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 * @since 3.4
	 */
	protected boolean isResizable() {
		return true;
	}

	public RefactoringStatusDialog(RefactoringStatus status, Shell parent, String windowTitle, boolean backButton, boolean light) {
		this(status, parent, windowTitle, backButton);
		fLightWeight= light;
	}

	public RefactoringStatusDialog(Shell parent, ErrorWizardPage page, boolean backButton) {
		this(page.getStatus(), parent, parent.getText(), backButton);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(fWindowTitle);
	}

	protected Control createDialogArea(Composite parent) {
		Composite result= new Composite(parent, SWT.NONE);
		initializeDialogUnits(result);
		GridLayout layout= new GridLayout();
		result.setLayout(layout);
		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.widthHint= 600;
		gd.heightHint= 400;
		result.setLayoutData(gd);
		if (!fLightWeight) {
			Color background= parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			ViewForm messagePane= new ViewForm(result, SWT.BORDER | SWT.FLAT);
			messagePane.marginWidth= layout.marginWidth;
			messagePane.marginHeight= layout.marginHeight;
			gd= new GridData(GridData.FILL_HORIZONTAL);
			// XXX http://bugs.eclipse.org/bugs/show_bug.cgi?id=27572
			Rectangle rect= messagePane.computeTrim(0, 0, 0, convertHeightInCharsToPixels(2) + messagePane.marginHeight * 2);
			gd.heightHint= rect.height;
			messagePane.setLayoutData(gd);
			messagePane.setBackground(background);
			Label label= new Label(messagePane, SWT.LEFT | SWT.WRAP);
			if (fStatus.hasFatalError())
				label.setText(RefactoringUIMessages.RefactoringStatusDialog_Cannot_proceed);
			else
				label.setText(RefactoringUIMessages.RefactoringStatusDialog_Please_look);
			label.setBackground(background);
			messagePane.setContent(label);
		}
		RefactoringStatusViewer viewer= new RefactoringStatusViewer(result, SWT.NONE);
		viewer.setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setStatus(fStatus);
		applyDialogFont(result);
		return result;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.BACK_ID) {
			setReturnCode(IDialogConstants.BACK_ID);
			close();
		} else {
			super.buttonPressed(buttonId);
		}
	}

	protected void createButtonsForButtonBar(Composite parent) {
		if (!fStatus.hasFatalError()) {
			if (fBackButton)
				createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, false);
			createButton(parent, IDialogConstants.OK_ID, fLightWeight ? IDialogConstants.OK_LABEL : RefactoringUIMessages.RefactoringStatusDialog_Continue, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		} else {
			if (fBackButton)
				createButton(parent, IDialogConstants.BACK_ID, IDialogConstants.BACK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, fBackButton ? false : true);
		}
	}
}
