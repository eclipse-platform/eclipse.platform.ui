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
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.editor.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.examples.internal.ExamplesPlugin;
import org.eclipse.ui.forms.widgets.*;
/**
 * @author dejan
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class SecondPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public SecondPage(FormEditor editor) {
		super(editor, "second", "Section Page");
	}
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		form.setText("Title for the second page");
		form.setBackgroundImage(ExamplesPlugin.getDefault().getImage(ExamplesPlugin.IMG_FORM_BG));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		createTableSection(form, toolkit, "First Table Section");
		createTableSection(form, toolkit, "Second Table Section");		
	}
	
	private void createTableSection(final ScrolledForm form, FormToolkit toolkit, String title) {
		Section section =
			toolkit.createSection(
				form.getBody(),
				Section.TWISTIE | Section.DESCRIPTION);
		section.setActiveToggleColor(
			toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(
			toolkit.getColors().getColor(FormColors.SEPARATOR));
		toolkit.createCompositeSeparator(section);
		Composite client = toolkit.createComposite(section, SWT.WRAP);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;

		client.setLayout(layout);
		Table t = toolkit.createTable(client, SWT.NULL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 200;
		gd.widthHint = 100;
		t.setLayoutData(gd);
		toolkit.paintBordersFor(client);
		Button b = toolkit.createButton(client, "Add...", SWT.PUSH);
		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING);
		b.setLayoutData(gd);
		section.setText(title);
		section.setDescription("This section has a tree and a button.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);
	}
}
