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
package org.eclipse.ui.forms.internal.parts;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.events.*;
import org.eclipse.ui.forms.parts.*;

public class PartsTest {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		final ScrolledComposite sc =
			new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL);
		//sc.setBackground(sc.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		final Composite c = new Composite(sc, SWT.NONE);
		sc.setContent(c);
		sc.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				Rectangle ssize = sc.getClientArea();
				int swidth = ssize.width;
				Point size = c.computeSize(swidth, SWT.DEFAULT, true);
				Rectangle trim = c.computeTrim(0, 0, size.x, size.y);
				size = new Point(trim.width, trim.height);
				c.setSize(size);
			}
		});
		//c.setBackground(c.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		HTMLTableLayout layout = new HTMLTableLayout();
		layout.numColumns = 1;
		layout.leftMargin = 0;
		layout.rightMargin = 0;
		layout.makeColumnsEqualWidth = false;
		c.setLayout(layout);

		Label label;
		Button b;
		TableData td;
		/*
		 * label = new Label(c, SWT.NULL); label.setText("Text in the left
		 * column");
		 * 
		 * b = new Button(c, SWT.CHECK); b.setText("Checkbox in the right
		 * column");
		 * 
		 * 
		 * label = new Label(c, SWT.WRAP); label.setText("This assignment step
		 * is then repeated for nested tables using the minimum and maximum
		 * widths derived for all such tables in the first pass. In this case,
		 * the width of the parent table cell plays the role of the current
		 * window size in the above description. This process is repeated
		 * recursively for all nested tables. The topmost table is then
		 * rendered using the assigned widths. Nested tables are subsequently
		 * rendered as part of the parent table's cell contents."); td = new
		 * TableData(); td.colspan = 2; td.align = TableData.FILL;
		 * label.setLayoutData(td);
		 */

		FormToolkit toolkit = new FormToolkit(display);

		Hyperlink link =
			toolkit.createHyperlink(
				c,
				"Sample hyperlink with longer text.",
				SWT.WRAP);
		link.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				try {
					Thread.currentThread().sleep(1000);
				} catch (InterruptedException ex) {
				}
			}
		});

		ExpandableComposite exp =
			toolkit.createExpandableComposite(c, ExpandableComposite.TREE_NODE|ExpandableComposite.CLIENT_INDENT);
		exp.setActiveToggleColor(
				toolkit.getHyperlinkGroup().getActiveForeground());
		exp.setToggleColor(toolkit.getColors().getColor(FormColors.SEPARATOR));
		Composite client = toolkit.createComposite(exp);
		exp.setClient(client);
		HTMLTableLayout elayout = new HTMLTableLayout();
		client.setLayout(elayout);
		elayout.leftMargin = elayout.rightMargin = 0;
		Button button = toolkit.createButton(client, "Button", SWT.PUSH);
		exp.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				c.layout(true);
				updateSize(sc, c);
			}
		});
		exp.setText("Expandable Section with a longer title");
		td = new TableData();
		//td.colspan = 2;
		td.align = TableData.FILL;
		exp.setLayoutData(td);

		//StatusLineManager manager = new StatusLineManager();
		/*
		 * Control mcontrol = manager.createControl(c); td = new TableData();
		 * td.colspan = 2; td.align = TableData.FILL;
		 * mcontrol.setLayoutData(td);
		 * 
		 * factory.setHyperlinkUnderlineMode(HyperlinkHandler.UNDERLINE_ROLLOVER);
		 * 
		 * SelectableFormLabel ft = new SelectableFormLabel(c, SWT.WRAP);
		 * ft.setText("Some text in the form text that should also wrap");
		 * factory.turnIntoHyperlink(ft, new IHyperlinkListener() { public void
		 * linkEntered(Control link) { System.out.println("Link entered"); }
		 * public void linkExited(Control link) { System.out.println("Link
		 * exited"); } public void linkActivated(Control link) {
		 * System.out.println("Link activated"); } });
		 * 
		 * ft = new SelectableFormLabel(c, SWT.WRAP); ft.setText("Some more
		 * form text here"); factory.turnIntoHyperlink(ft, new
		 * IHyperlinkListener() { public void linkEntered(Control link) {
		 * System.out.println("Link entered"); } public void linkExited(Control
		 * link) { System.out.println("Link exited"); } public void
		 * linkActivated(Control link) { System.out.println("Link activated"); }
		 * });
		 */

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	/*
	 * 
	 * public static void addFormEngine(Composite c, FormWidgetFactory factory,
	 * IStatusLineManager manager) { new Label(c, SWT.NULL); RichText html =
	 * new RichText(c, SWT.WRAP);
	 * html.setHyperlinkSettings(factory.getHyperlinkHandler());
	 * RichTextHTTPAction action = new RichTextHTTPAction(manager); URL i1URL =
	 * TableLayoutTest.class.getResource("image1.gif"); ImageDescriptor id1 =
	 * ImageDescriptor.createFromURL(i1URL);
	 * html.registerTextObject("urlHandler", action);
	 * html.registerTextObject("image1", id1.createImage());
	 * //html.setBackground(factory.getBackgroundColor());
	 * html.setForeground(factory.getForegroundColor()); InputStream is =
	 * TableLayoutTest.class.getResourceAsStream("index.xml");
	 * //html.setParagraphsSeparated(false); html.setContents(is, true);
	 * TableData td = new TableData(); td.colspan = 1; td.align =
	 * TableData.FILL; html.setLayoutData(td); }
	 */

	public static void addRow(Composite c) {
		Composite row = new Composite(c, SWT.WRAP);
		RowLayout layout = new RowLayout();
		layout.wrap = true;
		row.setLayout(layout);

		for (int i = 0; i < 10; i++) {
			Button button = new Button(row, SWT.PUSH);
			button.setText("Button that should be wrapped");
		}
		TableData td = new TableData();
		td.colspan = 2;
		td.align = TableData.FILL;
		td.grabHorizontal = true;
		row.setLayoutData(td);
	}

	private static void updateSize(ScrolledComposite sc, Composite c) {
		Rectangle ssize = sc.getClientArea();
		int swidth = ssize.width;
		Point size = c.computeSize(swidth, SWT.DEFAULT, true);
		Rectangle trim = c.computeTrim(0, 0, size.x, size.y);
		size = new Point(trim.width, trim.height);
		c.setSize(size);
	}

}
