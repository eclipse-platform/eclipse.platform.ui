/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.forms.performance;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceTestCase;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

public class FormsPerformanceTest extends PerformanceTestCase {
	
	public void test_createForm() {
		tagAsSummary("Create Form", Dimension.ELAPSED_PROCESS);
	    Performance.getDefault();
		
		// Warm up.

		Display display = PlatformUI.getWorkbench().getDisplay();

		FormToolkit toolkit;
		toolkit = new FormToolkit(display);
		for(int samples = 0; samples < 2; samples++) {
			Shell shell = new Shell(display);
			shell.setSize(400, 300);
			shell.setLayout(new FillLayout());
			shell.open();

			for (int i = 0; i < 5; i++) {
				Composite c = new Composite(shell, SWT.H_SCROLL + SWT.V_SCROLL);
				c.setLayout(new FillLayout());
                createPartControl(c, toolkit);
                shell.layout(true); 
                while(display.readAndDispatch()){/*empty*/}
				c.dispose();
			}
			shell.dispose();
			while(display.readAndDispatch()){/*empty*/}
		}

		for(int samples = 0; samples < 50; samples++) {
			Shell shell = new Shell(display);
			shell.setSize(400, 300);
			shell.setLayout(new FillLayout());
			shell.open();
			startMeasuring();
			for (int i = 0; i < 3; i++) {
				Composite c = new Composite(shell, SWT.H_SCROLL + SWT.V_SCROLL);
				c.setLayout(new FillLayout());
                createPartControl(c, toolkit);
                shell.layout(true);
                while(display.readAndDispatch()){/*empty*/}
				c.dispose();
			}
			stopMeasuring(); 
			shell.dispose();
			while(display.readAndDispatch()){/*empty*/}
		}	
		toolkit.dispose();
		commitMeasurements();
		assertPerformance();
	}
	
	public void createPartControl(Composite parent, FormToolkit toolkit) {
		//Label l = new Label(parent, SWT.NULL);
		//l.setText ("a label");
		ScrolledForm form;
		form = toolkit.createScrolledForm(parent);
		form.setText("Hello, Eclipse Forms");
		TableWrapLayout layout = new TableWrapLayout();
		form.getBody().setLayout(layout);
		
		Hyperlink link = toolkit.createHyperlink(form.getBody(), "Click here.",
				SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link activated!");
			}
		});
		link.setText("This is an example of a form that is much longer and will need to wrap.");
		layout.numColumns = 2;
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		link.setLayoutData(td);
		toolkit.createLabel(form.getBody(), "Text field label:");
		Text text = toolkit.createText(form.getBody(), "");
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		text.setLayoutData(td);
		Button button = toolkit.createButton(form.getBody(),
				"An example of a checkbox in a form", SWT.CHECK);
		td = new TableWrapData();
		td.colspan = 2;
		button.setLayoutData(td);
		
		ImageHyperlink ih = toolkit.createImageHyperlink(form.getBody(), SWT.NULL);
		ih.setText("Image link with no image");
		ih = toolkit.createImageHyperlink(form.getBody(), SWT.NULL);
		//ih.setImage(ExamplesPlugin.getDefault().getImageRegistry().get(ExamplesPlugin.IMG_SAMPLE));
		ih.setText("Link with image and text");
		
		ExpandableComposite ec = toolkit.createExpandableComposite(form.getBody(), ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
		ImageHyperlink eci = toolkit.createImageHyperlink(ec, SWT.NULL);
		//eci.setImage(ExamplesPlugin.getDefault().getImageRegistry().get(ExamplesPlugin.IMG_SAMPLE));
		ec.setTextClient(eci);
		ec.setText("Expandable Composite title");
		String ctext = "We will now create a somewhat long text so that "+
		"we can use it as content for the expandable composite. "+
		"Expandable composite is used to hide or show the text using the "+
		"toggle control";
		Label client = toolkit.createLabel(ec, ctext, SWT.WRAP);
		ec.setClient(client);
		td = new TableWrapData();
		td.colspan = 2;
		ec.setLayoutData(td);
		ec.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				//form.reflow(true);
			}
		});
		Section section = toolkit.createSection(form.getBody(), Section.DESCRIPTION|Section.TWISTIE|Section.EXPANDED);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		section.setLayoutData(td);
		section.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				//form.reflow(true);
			}
		});
		section.setText("Section title");
		toolkit.createCompositeSeparator(section);
		section.setDescription("This is the description that goes below the title");
		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new GridLayout());
		button = toolkit.createButton(sectionClient, "Radio 1", SWT.RADIO);
		button = toolkit.createButton(sectionClient, "Radio 2", SWT.RADIO);
		section.setClient(sectionClient);

		StringBuffer buf = new StringBuffer();
		buf.append("<form>");
		buf.append("<p>");
		buf.append("Here is some plain text for the text to render; ");
		buf.append("this text is at <a href=\"http://www.eclipse.org\" nowrap=\"true\">http://www.eclipse.org</a> web site.");
		buf.append("</p>");
		buf.append("<p>");
		buf.append("<span color=\"header\" font=\"header\">This text is in header font.</span>");
		buf.append("</p>");
		buf.append("<p>This line will contain some <b>bold</b> and some <span font=\"code\">source</span> text. ");
		buf.append("We can also add <img href=\"image\"/> an image. ");
		buf.append("</p>");
		buf.append("<li>A default (bulleted) list item.</li>");
		buf.append("<li>Another bullet list item.</li>");
		buf.append("<li style=\"text\" value=\"1.\">A list item with text.</li>");
		buf.append("<li style=\"text\" value=\"2.\">Another list item with text</li>");
		buf.append("<li style=\"image\" value=\"image\">List item with an image bullet</li>");
		buf.append("<li style=\"text\" bindent=\"20\" indent=\"40\" value=\"3.\">A list item with text.</li>");
		buf.append("<li style=\"text\" bindent=\"20\" indent=\"40\" value=\"4.\">A list item with text.</li>");
		buf.append("<p>     leading blanks;      more white \n\n new lines   <br/><br/><br/> \n more <b>   bb   </b>  white  . </p>");
		buf.append("</form>");
		FormText rtext = toolkit.createFormText(form.getBody(), false);
		//rtext.setWhitespaceNormalized(false);
		td = new TableWrapData(TableWrapData.FILL);
		td.colspan = 2;
		rtext.setLayoutData(td);
		//rtext.setImage("image", ExamplesPlugin.getDefault().getImageRegistry().get(ExamplesPlugin.IMG_SAMPLE));
		//rtext.setColor("header", JFaceColors.getErrorText(display));
		rtext.setFont("header", JFaceResources.getHeaderFont());
		rtext.setFont("code", JFaceResources.getTextFont());
		rtext.setText(buf.toString(), true, false);
		rtext.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				System.out.println("Link active: "+e.getHref());
			}
		});
/*		layout.numColumns = 3;
		Label label;
		TableWrapData td;
		
		label = toolkit.createLabel(form.getBody(), "Some text to put in the first column", SWT.WRAP);
		label = toolkit.createLabel(form.getBody() ,"Some text to put in the second column and make it a bit longer so that we can see what happens with column distribution. This text must be the longest so that it can get more space allocated to the columns it belongs to.", SWT.WRAP);
		td = new TableWrapData();
		td.colspan = 2;
		label.setLayoutData(td);
		label = toolkit.createLabel(form.getBody(), "This text will span two rows and should not grow the column.", SWT.WRAP);
		td = new TableWrapData();
		td.rowspan = 2;
		label.setLayoutData(td);
		label = toolkit.createLabel(form.getBody(), "This text goes into column 2 and consumes only one cell", SWT.WRAP);
		label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		label = toolkit.createLabel(form.getBody(), "This text goes into column 3 and consumes only one cell too", SWT.WRAP);
		label.setLayoutData(new TableWrapData(TableWrapData.FILL));
		label = toolkit.createLabel(form.getBody(), "This text goes into column 2 and consumes only one cell", SWT.WRAP);
		label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		label = toolkit.createLabel(form.getBody(), "This text goes into column 3 and consumes only one cell too", SWT.WRAP);
		label.setLayoutData(new TableWrapData(TableWrapData.FILL));
		form.getBody().setBackground(form.getBody().getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));*/
		
		toolkit.paintBordersFor(form.getBody());
	}

}
