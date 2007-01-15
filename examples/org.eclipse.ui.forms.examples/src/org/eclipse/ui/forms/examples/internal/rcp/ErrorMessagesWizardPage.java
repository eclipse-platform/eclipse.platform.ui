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
package org.eclipse.ui.forms.examples.internal.rcp;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ErrorMessagesWizardPage extends WizardPage {
	//private MessageManager mmng;

	/**
	 * @param id
	 * @param title
	 */
	public ErrorMessagesWizardPage(String id) {
		super(id);
		setTitle("Example with message handling");
		setDescription("This page shows how MessageManager can be used in wizards");
	}

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		setControl(container);
		/*
		mmng = new MessageManager(this);

		GridLayout glayout = new GridLayout();
		glayout.horizontalSpacing = 10;
		glayout.numColumns = 2;
		container.setLayout(glayout);
		createDecoratedTextField("Field1", container);
		createDecoratedTextField("Field2", container);
		createDecoratedTextField("Field3", container);
		GridData gd;
		final Button button1 = new Button(container, SWT.CHECK);
		button1.setText("Add general error");
		gd = new GridData();
		gd.horizontalSpan = 2;
		button1.setLayoutData(gd);
		button1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (button1.getSelection()) {
					mmng.addMessage("saveError", "Save Error",
							IMessageProvider.ERROR, null);
				} else {
					mmng.removeMessage("saveError");
				}
			}
		});
		final Button button2 = new Button(container, SWT.CHECK);
		button2.setText("Add static message");
		gd = new GridData();
		gd.horizontalSpan = 2;
		button2.setLayoutData(gd);
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (button2.getSelection()) {
					mmng.addMessage("info", "Secondary info",
							IMessageProvider.NONE, null);
				} else {
					mmng.removeMessage("info");
				}
			}
		});
	}

	private void createDecoratedTextField(String label, Composite parent) {
		Label l = new Label(parent, SWT.NULL);
		l.setText(label);
		final Text text = new Text(parent, SWT.BORDER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.widthHint = 150;
		text.setLayoutData(gd);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String s = text.getText();
				// flag length
				if (s.length() > 5 && s.length() <= 10) {
					mmng.addMessage("textLength",
							"Text is longer than 5 characters",
							IMessageProvider.WARNING, null, text);
				} else if (s.length() > 10) {
					mmng.addMessage("textLength",
							"Text is longer than 10 characters",
							IMessageProvider.ERROR, null, text);
				} else {
					mmng.removeMessage("textLength", text);
				}
				// flag type
				boolean badType = false;
				for (int i = 0; i < s.length(); i++) {
					if (!Character.isLetter(s.charAt(i))) {
						badType = true;
						break;
					}
				}
				if (badType) {
					mmng.addMessage("textType",
							"Text must only contain letters",
							IMessageProvider.ERROR, text);
				} else {
					mmng.removeMessage("textType", text);
				}
			}
		});
		*/
	}
}