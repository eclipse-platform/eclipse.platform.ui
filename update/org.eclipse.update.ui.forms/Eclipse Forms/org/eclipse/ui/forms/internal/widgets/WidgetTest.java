/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.forms.internal.widgets;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.jface.resource.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.*;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.widgets.*;

public class WidgetTest {
	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		FormToolkit toolkit = new FormToolkit(display);
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(
			HyperlinkSettings.UNDERLINE_ROLLOVER);
		CTabFolder folder = new CTabFolder(shell, SWT.NULL);
		CTabItem t1 = new CTabItem(folder, SWT.NULL);
		Form f1 = createForm1(folder, toolkit);
		t1.setControl(f1);
		t1.setText(f1.getText());
		
		CTabItem t2 = new CTabItem(folder, SWT.NULL);
		Form f2 = createForm2(folder, toolkit);
		t2.setControl(f2);
		t2.setText(f2.getText());
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	private static Form createForm1(Composite parent, FormToolkit toolkit) {
		Form form = toolkit.createForm(parent);
		form.setText("Wrapped Form");
		URL bdURL = WidgetTest.class.getResource("form_banner.gif");
		ImageDescriptor bd = ImageDescriptor.createFromURL(bdURL);
		form.setBackgroundImage(bd.createImage());
		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		//layout.numColumns = 2;
		//layout.makeColumnsEqualWidth = true;
		form.getBody().setLayout(layout);

		Label label;
		Button b;
		TableWrapData td;
		Hyperlink link =
			toolkit.createHyperlink(
				form.getBody(),
				"Sample hyperlink with longer text.",
				SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		});
		td = new TableWrapData();
		//td.colspan = 2;
		td.align = TableWrapData.LEFT;
		link.setLayoutData(td);
		createExpandable(form, toolkit);
		createRichTextSection(form, toolkit);
		return form;
	}
	
	private static Form createForm2(Composite parent, FormToolkit toolkit) {
		Form form = toolkit.createForm(parent);
		form.setText("Jelly Form");
		URL bdURL = WidgetTest.class.getResource("form_banner.gif");
		ImageDescriptor bd = ImageDescriptor.createFromURL(bdURL);
		form.setBackgroundImage(bd.createImage());
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		form.getBody().setLayout(layout);
		createTableSection(form, toolkit, "Extensions");
		createTableSection(form, toolkit, "Extension Points");
		return form;
	}

	private static void createExpandable(
		final Form form,
		FormToolkit toolkit) {
		ExpandableComposite exp =
			toolkit
				.createExpandableComposite(
					form.getBody(),
					ExpandableComposite.TREE_NODE
			//	ExpandableComposite.NONE
	);
		exp.setActiveToggleColor(
			toolkit.getHyperlinkGroup().getActiveForeground());
		exp.setToggleColor(toolkit.getColors().getColor(FormColors.SEPARATOR));
		Composite client = toolkit.createComposite(exp);
		exp.setClient(client);
		TableWrapLayout elayout = new TableWrapLayout();
		client.setLayout(elayout);
		elayout.leftMargin = elayout.rightMargin = 0;
		Button button = toolkit.createButton(client, "Button", SWT.PUSH);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		exp.setText("Expandable Section with a longer title");
		TableWrapData td = new TableWrapData();
		//td.colspan = 2;
		td.align = TableWrapData.LEFT;
		//td.align = TableWrapData.FILL;
		exp.setLayoutData(td);
	}

	private static void createRichTextSection(final Form form, FormToolkit toolkit) {
		Section section =
			toolkit.createSection(
				form.getBody(),
				Section.TWISTIE | Section.DESCRIPTION);
		section.setActiveToggleColor(
			toolkit.getHyperlinkGroup().getActiveForeground());
		section.setToggleColor(
			toolkit.getColors().getColor(FormColors.SEPARATOR));
		toolkit.createCompositeSeparator(section);
		RichText rtext = toolkit.createRichText(section, false);
		section.setClient(rtext);
		loadRichText(rtext, toolkit);
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

	private static void createStaticSection(final Form form, FormToolkit toolkit) {
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
		client.setLayout(layout);
		toolkit.createButton(client, "Radio 1", SWT.RADIO);
		toolkit.createButton(client, "Radio 2", SWT.RADIO);
		toolkit.createButton(client, "Radio 3", SWT.RADIO);
		toolkit.createButton(
			client,
			"Checkbox with somewhat longer text",
			SWT.CHECK);
		section.setText("Static Section");
		section.setDescription("This section contains a list of links.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.FILL;
		//td.grabHorizontal = true;
		section.setLayoutData(td);
	}

	private static void createTableSection(final Form form, FormToolkit toolkit, String title) {
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
		section.setDescription("This section a tree and a button.");
		section.setClient(client);
		section.setExpanded(true);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				form.reflow(false);
			}
		});
		gd = new GridData(GridData.FILL_BOTH);
		//td.valign = TableWrapData.FILL;
		//td.grabHorizontal = true;
		section.setLayoutData(gd);
	}

	private static void loadRichText(RichText rtext, FormToolkit toolkit) {
		rtext.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated: href=" + e.getHref());
			}
		});
		rtext.setHyperlinkSettings(toolkit.getHyperlinkGroup());
		URL i1URL = WidgetTest.class.getResource("image1.gif");
		ImageDescriptor id1 = ImageDescriptor.createFromURL(i1URL);
		rtext.setImage("image1", id1.createImage());
		InputStream is = WidgetTest.class.getResourceAsStream("index.xml");
		rtext.setContents(is, true);
	}
}
