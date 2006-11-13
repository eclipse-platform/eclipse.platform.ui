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

package org.eclipse.jface.snippets.viewers;

import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Demonstrate usage of custom toolstips for controls
 * 
 * @author Tom Schindl
 * 
 */
public class Snippet020CustomizedControlTooltips {
	protected class MyToolTipPart extends ToolTip {
		
		private Shell parentShell;
		
		public MyToolTipPart(Control control) {
			super(control);
			this.parentShell = control.getShell(); 
			setHideOnMouseDown(false);
		}

		protected Composite createToolTipContentArea(Event event,
				Composite parent) {
			Color bgColor = event.widget.getDisplay().getSystemColor(
					SWT.COLOR_INFO_BACKGROUND);
			Composite comp = new Composite(parent, SWT.NONE);
			comp.setBackground(bgColor);
			comp.setLayout(new RowLayout());
			CLabel l1 = new CLabel(comp, SWT.NONE);
			l1.setText("Elemen 1\nElement 1");
			l1.setBackground(bgColor);

			CLabel l2 = new CLabel(comp, SWT.NONE);
			l2.setText("Elemen 2\nElemen 2");
			l2.setBackground(bgColor);
			
			Button b = new Button(comp,SWT.TOGGLE);
			b.setText("Hello");
			b.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					hide();
					Shell shell = new Shell(parentShell);
					shell.open();
				}
				
			});

			return comp;
		}
	}

	public Snippet020CustomizedControlTooltips(Shell shell) {

		Label l = new Label(shell, SWT.NONE);
		l.setText("Label:");

		MyToolTipPart myTooltipLabel = new MyToolTipPart(l);
		myTooltipLabel.setShift(new Point(-5, -5));
		myTooltipLabel.activate();

		Text t = new Text(shell, SWT.BORDER);
		t.setText("Hello World");
		MyToolTipPart myTooltipText = new MyToolTipPart(t);
		myTooltipText.setShift(new Point(5, 5));
		myTooltipText.activate();
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
