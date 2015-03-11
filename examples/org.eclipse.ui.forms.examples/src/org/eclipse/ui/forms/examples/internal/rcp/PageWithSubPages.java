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
package org.eclipse.ui.forms.examples.internal.rcp;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
/**
 *
 */
public class PageWithSubPages extends FormPage {
	private CTabFolder tabFolder;
	private Text text;

	class TextSection {
		String text;
		public TextSection(String text) {this.text = text;}
	}
	/**
	 * @param id
	 * @param title
	 */
	public PageWithSubPages(FormEditor editor) {
		super(editor, "composite", "Composite Page");
	}
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Form with subpages");
		form.setBackgroundImage(ExamplesPlugin.getDefault().getImage(
				ExamplesPlugin.IMG_FORM_BG));
		GridLayout layout = new GridLayout();
		layout.marginWidth = 10;
		form.getBody().setLayout(layout);
		tabFolder = new CTabFolder(form.getBody(), SWT.FLAT|SWT.TOP);
		toolkit.adapt(tabFolder, true, true);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 0;
		tabFolder.setLayoutData(gd);
		Color selectedColor = toolkit.getColors().getColor(IFormColors.SEPARATOR);
		tabFolder.setSelectionBackground(new Color[] {selectedColor, toolkit.getColors().getBackground()}, new int[] {50});
		//tabFolder.setCursor(FormsResources.getHandCursor());

		toolkit.paintBordersFor(tabFolder);
		createTabs(toolkit);
		createText(toolkit, form.getBody());
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updateSelection();
			}
		});
		tabFolder.setSelection(0);
		updateSelection();
	}
	private void createTabs(FormToolkit toolkit) {
		createTab(toolkit, "Copyright", "Copyright 2004 IBM and others.");
		createTab(toolkit, "License Agreement", "LICENSE AGREEMENT\n\nUse this feature any way you want.");
		createTab(toolkit, "Description", "A simple description of the feature");
	}
	private void createText(FormToolkit toolkit, Composite parent) {
		Composite tabContent = toolkit.createComposite(parent);
		tabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout();
		tabContent.setLayout(layout);
		layout.numColumns = 2;
		layout.marginWidth = 0;
		GridData gd;
		text = toolkit.createText(tabContent, "", SWT.MULTI|SWT.WRAP);
		gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		text.setLayoutData(gd);
		Button apply = toolkit.createButton(tabContent, "Apply", SWT.PUSH);
		apply.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_BEGINNING));
		Button reset = toolkit.createButton(tabContent, "Reset", SWT.PUSH);
		reset.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL|GridData.VERTICAL_ALIGN_BEGINNING));
	}
	private void updateSelection() {
		CTabItem item = tabFolder.getSelection();
		TextSection section = (TextSection)item.getData();
		text.setText(section.text);
	}
	private void createTab(FormToolkit toolkit, String title, String content) {
		CTabItem item = new CTabItem(tabFolder, SWT.NULL);
		TextSection section = new TextSection(content);
		item.setText(title);
		item.setData(section);
	}
}
