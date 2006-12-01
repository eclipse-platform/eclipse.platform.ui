/*******************************************************************************
 * Copyright (c) 2006 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.snippets.window;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Demonstrate usage of custom toolstips for controls
 * 
 * @author Tom Schindl
 * 
 */
public class Snippet020CustomizedControlTooltips {
	protected class MyToolTip extends ToolTip {
		
		private Shell parentShell;
		
		public MyToolTip(Control control) {
			super(control);
			this.parentShell = control.getShell();
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Composite comp = new Composite(parent,SWT.NONE);
			comp.setLayout(new FillLayout());
			
			Button b = new Button(comp,SWT.PUSH);
			b.setText("Say Hello");
			b.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					hide();
					MessageBox box = new MessageBox(parentShell,SWT.ICON_INFORMATION);
					box.setMessage("Hello World!");
					box.setText("Hello World");
					box.open();
				}
			});
			
			return comp;
		}
	}

	public Snippet020CustomizedControlTooltips(Shell parent) {

		Text text = new Text(parent,SWT.BORDER);
		text.setText("Hello World");

		MyToolTip myTooltipLabel = new MyToolTip(text);
		myTooltipLabel.setShift(new Point(-5, -5));
		myTooltipLabel.setHideOnMouseDown(false);
		myTooltipLabel.activate();

		text = new Text(parent,SWT.BORDER);
		text.setText("Hello World");
		DefaultToolTip toolTip = new DefaultToolTip(text);
		toolTip.setText("Hello World\nHello World");
		toolTip.setBackgroundColor(parent.getDisplay().getSystemColor(SWT.COLOR_RED));
	}

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);
		shell.setLayout(new RowLayout());
		new Snippet020CustomizedControlTooltips(shell);

		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		display.dispose();
	}
}
