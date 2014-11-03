/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 433608
 *******************************************************************************/

package org.eclipse.jface.tests.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class TheTestWizardPage extends WizardPage {
	public static final String BAD_TEXT_FIELD_CONTENTS = "BAD VALUE";
	public static final String BAD_TEXT_FIELD_STATUS = "A bad value was entered";
	public static final String GOOD_TEXT_FIELD_CONTENTS = "GOOD VALUE";
	public Text textInputField;
	private boolean throwExceptionOnDispose;

	public TheTestWizardPage(String name) {
		super(name);
	}

	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText(getName());

		textInputField = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		textInputField.setLayoutData(gd);
		textInputField.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		initialize();
		dialogChanged();
		setControl(container);
	}

	private void initialize() {}


	/**
	 * Handle dialog values changing
	 */
	private void dialogChanged() {
		if (textInputField.getText().equals(BAD_TEXT_FIELD_CONTENTS)) {
			setPageComplete(false);
			updateStatus(BAD_TEXT_FIELD_STATUS);
			return;
		}
		//any other value, including no value, is good
		setPageComplete(true);
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	/**
	 * @param throwExceptionOnDispose The throwExceptionOnDispose to set.
	 */
	public void setThrowExceptionOnDispose(boolean throwExceptionOnDispose) {
		this.throwExceptionOnDispose = throwExceptionOnDispose;
	}

	@Override
	public void dispose() {
		super.dispose();
		if(throwExceptionOnDispose) {
			throw new NullPointerException();
		}
	}

}