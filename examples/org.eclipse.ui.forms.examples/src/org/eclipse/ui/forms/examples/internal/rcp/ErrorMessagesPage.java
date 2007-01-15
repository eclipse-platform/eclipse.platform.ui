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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
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
import org.eclipse.ui.forms.MessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ErrorMessagesPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public ErrorMessagesPage(FormEditor editor) {
		super(editor, "messageManager", "Message Manager");
	}

	protected void createFormContent(IManagedForm managedForm) {
		final ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
				HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("Example with message handling");
		toolkit.decorateFormHeading(form.getForm());
		form.getForm().addMessageHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String title = e.getLabel();
				String details = (String)e.getHref();
				switch (form.getForm().getMessageType()) {
				case IMessageProvider.NONE:
				case IMessageProvider.INFORMATION:
					MessageDialog.openInformation(form.getShell(), title, details);
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

		final MessageManager mmng = new MessageManager(form);

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
							IMessageProvider.ERROR, null);
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
							IMessageProvider.NONE, null);
				} else {
					mmng.removeMessage("info");
				}
			}
		});
		final Button button3 = toolkit.createButton(form.getBody(),
				"Open Wizard", SWT.PUSH);
		button3.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Wizard w = new Wizard() {
					public boolean performFinish() {
						return true;
					}

					public void addPages() {
						addPage(new ErrorMessagesWizardPage("id"));
					}
				};
				WizardDialog dialog = new WizardDialog(form.getShell(), w);
				dialog.create();
				dialog.getShell().setSize(300, 400);
				dialog.getShell().setText("Field Error Messages");
				dialog.open();
			}
		});

	}

	private void createDecoratedTextField(String label, FormToolkit toolkit,
			Composite parent, final MessageManager mmng) {
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
							IMessageProvider.ERROR, null, text);
				} else {
					mmng.removeMessage("textType", text);
				}
			}
		});
	}
}