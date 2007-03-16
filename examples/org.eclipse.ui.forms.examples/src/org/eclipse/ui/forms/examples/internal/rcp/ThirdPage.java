/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ThirdPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public ThirdPage(FormEditor editor) {
		super(editor, "third", "Flow Page");
	}
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		//FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Form with wrapped controls");
		ColumnLayout layout = new ColumnLayout();
		layout.topMargin = 0;
		layout.bottomMargin = 5;
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.horizontalSpacing = 10;
		layout.verticalSpacing = 10;
		layout.maxNumColumns = 4;
		layout.minNumColumns = 1;
		form.getBody().setLayout(layout);
		//form.getBody().setBackground(
		//		form.getBody().getDisplay().getSystemColor(SWT.COLOR_GREEN));
		createSectionWithLinks(managedForm, "Link Section",
				"An example of a section with links", 2);
		createSectionWithLinks(managedForm, "Link Section",
				"An example of a section with links", 2);
		createMixedSection(managedForm, "Mixed Section",
				"An example of a section with both links and form controls");
		createSectionWithLinks(managedForm, "Link Section",
				"An example of a section with links", 4);
		createSectionWithControls(managedForm, "Control Section",
				"An example of a section with form controls");
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 3);
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 5);
		createMixedSection(managedForm, "Mixed Section",
				"An example of a section with both links and form controls");
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 2);
		createSectionWithControls(managedForm, "Control Section",
				"An example of a section with links");
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 4);
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 2);
		createMixedSection(managedForm, "Mixed Section",
				"An example of a section with both links and form controls");
		createSectionWithLinks(managedForm, "Sample Section",
				"An example of a section with links", 2);
		createSectionWithControls(managedForm, "Control Section",
				"An example of a section with form controls");
	}
	private void createSectionWithLinks(IManagedForm mform, String title,
			String desc, int nlinks) {
		Composite client = createSection(mform, title, desc, 1);
		FormToolkit toolkit = mform.getToolkit();
		for (int i = 1; i <= nlinks; i++)
			toolkit.createHyperlink(client, "Hyperlink text " + i, SWT.WRAP);
	}
	private void createSectionWithControls(IManagedForm mform, String title,
			String desc) {
		Composite client = createSection(mform, title, desc, 1);
		FormToolkit toolkit = mform.getToolkit();
		toolkit.createButton(client, "A radio button 1", SWT.RADIO);
		toolkit.createButton(client, "A radio button 2", SWT.RADIO);
		toolkit.createButton(client, "A radio button with a longer text",
				SWT.RADIO);
		toolkit.createButton(client, "A checkbox button", SWT.CHECK);
	}
	private void createMixedSection(IManagedForm mform, String title, String desc) {
		Composite client = createSection(mform, title, desc, 2);
		FormToolkit toolkit = mform.getToolkit();
		Hyperlink link = toolkit.createHyperlink(client,
				"A longer hyperlink text example", SWT.WRAP);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		link.setLayoutData(gd);
		link = toolkit.createHyperlink(client, "Another hyperlink text",
				SWT.WRAP);
		gd = new GridData();
		gd.horizontalSpan = 2;
		link.setLayoutData(gd);
		toolkit.createLabel(client, "A text label:");
		Text text = toolkit.createText(client, "", SWT.SINGLE);
		text.setText("Sample text");
		text.setEnabled(false);
		gd = new GridData();
		gd.widthHint = 150;
		text.setLayoutData(gd);
		toolkit.paintBordersFor(client);
	}
	private Composite createSection(IManagedForm mform, String title,
			String desc, int numColumns) {
		final ScrolledForm form = mform.getForm();
		FormToolkit toolkit = mform.getToolkit();
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.SHORT_TITLE_BAR | Section.DESCRIPTION | Section.EXPANDED);
		section.setText(title);
		section.setDescription(desc);
		Composite client = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginWidth = layout.marginHeight = 0;
		layout.numColumns = numColumns;
		client.setLayout(layout);
		section.setClient(client);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		return client;
	}
}
