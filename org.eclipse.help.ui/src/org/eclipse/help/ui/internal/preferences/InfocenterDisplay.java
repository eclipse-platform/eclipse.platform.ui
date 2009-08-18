/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.ui.internal.preferences;

import org.eclipse.help.ui.internal.IHelpUIConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

public class InfocenterDisplay implements IHelpContentBlockContainer {

	private HelpContentBlock fHelpContentBlock = new HelpContentBlock();
	private HelpContentPreferencePage fPreferencePage;

	/**
	 * Creates an instance.
	 */
	public InfocenterDisplay(HelpContentPreferencePage preferencePage) {
		fPreferencePage = preferencePage;
	}

	/**
	 * This create the page controls
	 */
	protected Composite createContents(Composite parent) {
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent,
				IHelpUIConstants.PREF_PAGE_HELP_CONTENT);

		Composite top = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		top.setLayout(layout);

		top.setLayoutData(new GridData(GridData.FILL_BOTH));

		fHelpContentBlock.setContainer(this);
		fHelpContentBlock.createContents(top);
		Dialog.applyDialogFont(top);
		return top;
	}

	public Button createPushButton(Composite parent, String buttonText) {
		Button button = new Button(parent, SWT.PUSH);
		button.setFont(parent.getFont());
		button.setText(buttonText);
		fPreferencePage.setButtonLayoutData(button);
		return button;
	}

	public void setErrorMessage(String message) {

	}

	public void setMessage(String message) {

	}

	public void update() {
		if (fHelpContentBlock.isValidated()) {
			return;
		}
		setMessage(null);
		setErrorMessage(null);
		fPreferencePage.setValid(true);
	}

	public HelpContentBlock getHelpContentBlock()
	{
		return fHelpContentBlock;
	}
}
