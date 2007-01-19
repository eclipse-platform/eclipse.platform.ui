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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessage;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * This page shows how to use the message manager to handle
 * errors in a form page. 
 * @since 3.3
 */
public class ErrorMessagesPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public ErrorMessagesPage(FormEditor editor) {
		super(editor, "messageManager", "Message Manager");
	}

	protected void createFormContent(final IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("Example with message handling");
		toolkit.decorateFormHeading(form.getForm());
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String title = e.getLabel();
				String details = title;
				Object href = e.getHref();
				if (href instanceof IMessage[]) {
					details = managedForm.getMessageManager().createSummary((IMessage[])href);
				}
				int type = form.getForm().getMessageType();
				switch (type) {
				case IMessageProvider.NONE:
				case IMessageProvider.INFORMATION:
					MessageDialog.openInformation(form.getShell(), title,
							details);
					break;
				case IMessageProvider.WARNING:
					MessageDialog.openWarning(form.getShell(), title, details);
					break;
				case IMessageProvider.ERROR:
					MessageDialog.openError(form.getShell(), title, details);
					break;
				}
			}
		});

		final IMessageManager mmng = managedForm.getMessageManager();

		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		Section section = toolkit.createSection(form.getBody(),
				Section.TITLE_BAR);
		section.setText("Local field messages");
		Composite sbody = toolkit.createComposite(section);
		section.setClient(sbody);
		GridLayout glayout = new GridLayout();
		glayout.horizontalSpacing = 10;
		glayout.numColumns = 2;
		sbody.setLayout(glayout);
		toolkit.paintBordersFor(sbody);
		createDecoratedTextField("Field1", toolkit, sbody, mmng);
		createDecoratedTextField("Field2", toolkit, sbody, mmng);
		createDecoratedTextField("Field3", toolkit, sbody, mmng);
		final Button button1 = toolkit.createButton(form.getBody(),
				"Add general error", SWT.CHECK);
		button1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (button1.getSelection()) {
					mmng.addMessage("saveError", "Save Error",
							null, IMessageProvider.ERROR);
				} else {
					mmng.removeMessage("saveError");
				}
			}
		});
		final Button button2 = toolkit.createButton(form.getBody(),
				"Add static message", SWT.CHECK);
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (button2.getSelection()) {
					mmng.addMessage("info", "Secondary info",
							null, IMessageProvider.NONE);
				} else {
					mmng.removeMessage("info");
				}
			}
		});
	}

	private void createDecoratedTextField(String label, FormToolkit toolkit,
			Composite parent, final IMessageManager mmng) {
		toolkit.createLabel(parent, label);
		final Text text = toolkit.createText(parent, "");
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
							null, IMessageProvider.WARNING, text);
				} else if (s.length() > 10) {
					mmng.addMessage("textLength",
							"Text is longer than 10 characters",
							null, IMessageProvider.ERROR, text);
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
							null, IMessageProvider.ERROR, text);
				} else {
					mmng.removeMessage("textType", text);
				}
			}
		});
	}
}