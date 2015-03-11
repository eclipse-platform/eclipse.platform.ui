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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
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
		form.setBackgroundImage(ExamplesPlugin.getDefault().getImage(
				ExamplesPlugin.IMG_FORM_BG));
		GridLayout layout = new GridLayout();
		layout.makeColumnsEqualWidth = true;
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		//This call is needed because the section will compute
		// the bold version based on the parent.
		Dialog.applyDialogFont(form.getBody());
		Section s1 = createTableSection(form, toolkit, "First Table Section", true);
		Section s2 = createTableSection(form, toolkit, "Second Table Section", false);
		// This call is needed for all the children
		Dialog.applyDialogFont(form.getBody());
		s2.descriptionVerticalSpacing = s1.getTextClientHeightDifference();
		form.reflow(true);
	}

	private Section createTableSection(final ScrolledForm form,
			FormToolkit toolkit, String title, boolean addTextClient) {
		Section section = toolkit.createSection(form.getBody(), Section.TWISTIE
				| Section.TITLE_BAR);
		section.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		section.setToggleColor(toolkit.getColors().getColor(
				IFormColors.SEPARATOR));
		if (addTextClient) {
			ToolBar tbar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
			ToolItem titem = new ToolItem(tbar, SWT.NULL);
			titem.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_TOOL_CUT));
			titem = new ToolItem(tbar, SWT.PUSH);
			titem.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_TOOL_COPY));
			titem = new ToolItem(tbar, SWT.SEPARATOR);
			titem = new ToolItem(tbar, SWT.PUSH);
			titem.setImage(PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_TOOL_DELETE));
			section.setTextClient(tbar);
		}
		FormText description = toolkit.createFormText(section, false);
		description
				.setText(
						"<form><p>This description uses FormText widget and as a result can have <b>bold</b> text.</p></form>",
						true, false);
		section.setDescriptionControl(description);

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
		section
				.setDescription("<form><p>This section has a <b>tree</b> and a button. It also has <a>a link</a> in the description.</p></form>");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		section.setLayoutData(gd);
		return section;
	}
}
