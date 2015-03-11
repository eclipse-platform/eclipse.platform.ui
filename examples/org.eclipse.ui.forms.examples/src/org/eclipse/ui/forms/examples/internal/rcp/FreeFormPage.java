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
import java.io.*;
import java.io.InputStream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.layout.GridLayout;
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
public class FreeFormPage extends FormPage {
	/**
	 * @param id
	 * @param title
	 */
	public FreeFormPage(FormEditor editor) {
		super(editor, "first", "First Page");
	}
	public static final void createSharedFormContent(IManagedForm managedForm) {
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);
		form.setText("Free-form text with links");
		form.setBackgroundImage(ExamplesPlugin.getDefault().getImage(ExamplesPlugin.IMG_FORM_BG));
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		form.getBody().setLayout(layout);
		TableWrapData td;
		Hyperlink link = toolkit.createHyperlink(form.getBody(),
				"Sample hyperlink with longer text.", SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		});
		td = new TableWrapData();
		td.align = TableWrapData.LEFT;
		link.setLayoutData(td);
		createExpandable(form, toolkit);
		createFormTextSection(form, toolkit);
	}

	protected void createFormContent(IManagedForm managedForm) {
		createSharedFormContent(managedForm);
	}
	private static void createExpandable(final ScrolledForm form, final FormToolkit toolkit) {
		final ExpandableComposite exp = toolkit.createExpandableComposite(form
				.getBody(), ExpandableComposite.TREE_NODE
		//	ExpandableComposite.NONE
				);
		exp.setActiveToggleColor(toolkit.getHyperlinkGroup()
				.getActiveForeground());
		exp.setToggleColor(toolkit.getColors().getColor(IFormColors.SEPARATOR));
		Composite client = toolkit.createComposite(exp);
		exp.setClient(client);
		TableWrapLayout elayout = new TableWrapLayout();
		client.setLayout(elayout);
		elayout.leftMargin = elayout.rightMargin = 0;
		final Button button = toolkit.createButton(client, "Button", SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				//openFormWizard(button.getShell(), toolkit.getColors());
			}
		});
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(true);
			}
		});
		exp.setText("Expandable Section with a longer title");
		TableWrapData td = new TableWrapData();
		//td.colspan = 2;
		td.align = TableWrapData.LEFT;
		//td.align = TableWrapData.FILL;
		exp.setLayoutData(td);
	}

	private static void createFormTextSection(final ScrolledForm form, FormToolkit toolkit) {
		Section section =
			toolkit.createSection(
				form.getBody(),
				Section.TWISTIE | Section.DESCRIPTION);
		section.setActiveToggleColor(
			toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(
			toolkit.getColors().getColor(IFormColors.SEPARATOR));
		toolkit.createCompositeSeparator(section);
		FormText rtext = toolkit.createFormText(section, false);
		section.setClient(rtext);
		loadFormText(rtext, toolkit);

		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});

		section.setText("Section title");
		section.setDescription(
		"This is a section description that should be rendered below the separator.");
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		td.grabHorizontal = true;
		section.setLayoutData(td);
	}

	private static void loadFormText(final FormText rtext, FormToolkit toolkit) {
		rtext.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				MessageDialog.openInformation(rtext.getShell(), "Eclipse Forms",
				"Link activated: href=" + e.getHref());
			}
		});
		rtext.setHyperlinkSettings(toolkit.getHyperlinkGroup());
		rtext.setImage("image1", ExamplesPlugin.getDefault().getImage(ExamplesPlugin.IMG_LARGE));
		createCompositeControl(rtext, toolkit);
		createControl(rtext, toolkit);
		Menu menu = new Menu(rtext);
		final MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("&Copy");
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				rtext.copy();
			}
		});
		//rtext.setMenu(menu);
		item.setEnabled(false);
		rtext.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				item.setEnabled(rtext.canCopy());
			}
		});
		InputStream is = FreeFormPage.class.getResourceAsStream("index.xml");
		if (is!=null) {
			rtext.setContents(is, true);
			try {
				is.close();
			}
			catch (IOException e) {
			}
		}
	}
	private static void createControl(FormText ftext, FormToolkit toolkit) {
		TreeNode node = new TreeNode(ftext, SWT.NULL);
		toolkit.adapt(node, true, true);
		ftext.setControl("node", node);
	}
	private static void createCompositeControl(FormText ftext, FormToolkit toolkit) {
		Composite comp = toolkit.createComposite(ftext);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		comp.setLayout(layout);
		toolkit.createLabel(comp, "Sample text:");
		Text text = toolkit.createText(comp, "");
		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		toolkit.createButton(comp, "Browse...", SWT.PUSH);
		ftext.setControl("control1", comp);
		toolkit.paintBordersFor(comp);
	}
}
