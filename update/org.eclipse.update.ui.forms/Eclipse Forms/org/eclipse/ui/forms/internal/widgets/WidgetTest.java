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
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
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
		final FormToolkit toolkit) {
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
		final Button button = toolkit.createButton(client, "Button", SWT.PUSH);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openFormWizard(button.getShell(), toolkit.getColors());
			}
		});
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
	
	static class FormWizard extends Wizard {
		private FormToolkit toolkit;
		public FormWizard(FormColors colors) {
			toolkit = new FormToolkit(colors); 
			setNeedsProgressMonitor(true);
			URL banner = WidgetTest.class.getResource("migrate_30_wiz.gif");
			ImageDescriptor bd = ImageDescriptor.createFromURL(banner);
			setDefaultPageImageDescriptor(bd);
			setForcePreviousAndNextButtons(true);
		}
		
		public  void addPages() {
			addPage(new FormWizardPage(toolkit));
		}
		public boolean performFinish() {
			try {
			getContainer().run(false, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InterruptedException {
					monitor.beginTask("Processing...", 100);
					for (int i=0; i<100; i++) {
						Thread.sleep(100);
						monitor.worked(1);
					}
					monitor.done();
				}
			});
			}
			catch (InterruptedException e) {
				return false;
			}
			catch (InvocationTargetException e) {
				return false;
			}
			return true;
		}
		
	}
	
	static class FormWizardPage extends WizardPage {
		private FormToolkit toolkit;
		public FormWizardPage(FormToolkit toolkit) {
			super("formPage");
			this.toolkit = toolkit;
			setTitle("Sample Form Page");
			setDescription("This is a sample of a form in the wizard");
		}
		public void createControl(Composite parent) {
			final Form form = toolkit.createForm(parent);
			TableWrapLayout layout = new TableWrapLayout();
			layout.leftMargin = 10;
			//layout.rightMargin = 10;
			//layout.bottomMargin = 0;
			//layout.topMargin = 0;
			form.getBody().setLayout(layout);
			Section sec = toolkit.createSection(form.getBody(), Section.TWISTIE);
			sec.setSeparatorControl(toolkit.createCompositeSeparator(sec));
			sec.setText("A section inside a wizard page");
			sec.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanged(ExpansionEvent e) {
					form.reflow(false);
				}
			});
			Composite group = toolkit.createComposite(sec);
			sec.setClient(group);
			GridLayout glayout = new GridLayout();
			group.setLayout(glayout);
			glayout.numColumns = 2;
			toolkit.createLabel(group, "Some text:");
			toolkit.createText(group, "");
			Button b;
			GridData gd = new GridData();
			b = toolkit.createButton(group, "An option to select", SWT.CHECK);
			gd = new GridData();
			gd.horizontalSpan = 2;
			b.setLayoutData(gd);
			b = toolkit.createButton(group, "Choice 1", SWT.RADIO);
			gd = new GridData();
			gd.horizontalSpan = 2;
			b.setLayoutData(gd);
			b = toolkit.createButton(group, "Choice 2", SWT.RADIO);
			gd = new GridData();
			gd.horizontalSpan = 2;
			b.setLayoutData(gd);
			TableWrapData td = new TableWrapData();
			sec.setLayoutData(td);
			//createExpandable(form, toolkit);
			RichText rtext = toolkit.createRichText(form.getBody(), false);
			loadRichText(rtext, toolkit);
			td = new TableWrapData();
			td.align = TableWrapData.FILL;
			td.grabHorizontal = true;
			rtext.setLayoutData(td);
			setControl(form);
		}
	}
	
	static class ResizableWizardDialog extends WizardDialog {
		FormColors colors;
		public ResizableWizardDialog(Shell shell, Wizard wizard, FormColors colors) {
			super(shell, wizard);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.colors = colors;
		}
		protected Control createDialogArea(Composite parent) {
			Composite c = (Composite)super.createDialogArea(parent);
			setChildColors(c);
			c.setBackground(colors.getBackground());
			c.setForeground(colors.getForeground());
			return c;
		}
		protected Control createButtonBar(Composite parent) {
			Control bar = super.createButtonBar(parent);
			bar.setBackground(colors.getBackground());
			bar.setForeground(colors.getForeground());
			parent.setBackground(colors.getBackground());
			parent.setForeground(colors.getForeground());
			return bar;
		}
		private void setChildColors(Composite parent) {
			Control [] children = parent.getChildren();
			for (int i=0; i<children.length; i++) {
				Control child = children[i];
				child.setBackground(colors.getBackground());
				if (child instanceof ProgressMonitorPart)
					setChildColors((ProgressMonitorPart)child);
				if (child instanceof Composite) {
					Layout l = ((Composite)child).getLayout();
					if (l instanceof PageContainerFillLayout) {
						PageContainerFillLayout pl = (PageContainerFillLayout)l;
						pl.marginWidth = 0;
						pl.marginHeight = 0;
					}
				}
			}
		}
	}
	
	private static void openFormWizard(Shell shell, FormColors colors) {
		FormWizard wizard = new FormWizard(colors);
		WizardDialog wd = new ResizableWizardDialog(shell, wizard, colors);
		wd.create();
		wd.getShell().setText("Sample Form Wizard");
		wd.getShell().setSize(600, 500);
		wd.open();
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
