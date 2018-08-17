/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jface.snippets.layout;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @since 3.3
 */
public class Snippet013GridLayoutFactory {

	public static Shell createShell1() {
		Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		{ // Populate the shell
			Label text = new Label(shell, SWT.WRAP);
			text
					.setText("This is a layout test. This text should wrap in the test. You could call it a text test.");
			GridDataFactory.generate(text, 2, 1);

			List theList = new List(shell, SWT.H_SCROLL | SWT.V_SCROLL
					| SWT.BORDER);

			theList.add("Hello");
			theList.add("World");
			GridDataFactory.defaultsFor(theList).hint(300, 300)
					.applyTo(theList);

			Composite buttonBar = new Composite(shell, SWT.NONE);
			{ // Populate buttonBar
				Button add = new Button(buttonBar, SWT.PUSH);
				add.setText("Add");
				Button remove = new Button(buttonBar, SWT.PUSH);
				remove.setText("Remove");
			}
			GridLayoutFactory.fillDefaults().generateLayout(buttonBar);
		}
		GridLayoutFactory.fillDefaults().numColumns(2).margins(
				LayoutConstants.getMargins()).generateLayout(shell);

		return shell;
	}

	public static Shell createShell3() {
		Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		{ // Populate the shell

			Text text = new Text(shell, SWT.WRAP | SWT.BORDER);
			text
					.setText("This shell has asymmetric margins. The left, right, top, and bottom margins should be 0, 10, 40, and 80 pixels respectively");

			Rectangle margins = Geometry.createDiffRectangle(0, 10, 40, 80);

			GridLayoutFactory.fillDefaults().extendedMargins(margins)
					.generateLayout(shell);

		}

		return shell;
	}

	public static Shell createShell2() {
		Shell shell = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
		{ // Populate the shell

			Label text = new Label(shell, SWT.NONE);
			text.setText("Name:");
			new Text(shell, SWT.BORDER);

			Label quest = new Label(shell, SWT.NONE);
			quest.setText("Quest:");
			CCombo combo = new CCombo(shell, SWT.BORDER);
			combo.add("I seek the holy grail");
			combo.add("What? I don't know that");
			combo.add("All your base are belong to us");

			Label colour = new Label(shell, SWT.NONE);
			colour.setText("Color:");
			new Text(shell, SWT.BORDER);

			Composite buttonBar = new Composite(shell, SWT.NONE);
			{ // Populate buttonBar
				Button add = new Button(buttonBar, SWT.PUSH);
				add.setText("Okay");
				Button remove = new Button(buttonBar, SWT.PUSH);
				remove.setText("Cancel");

				GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(
						buttonBar);
			}
			GridDataFactory.fillDefaults().span(2, 1).align(SWT.RIGHT,
					SWT.BOTTOM).applyTo(buttonBar);

			GridLayoutFactory.fillDefaults().numColumns(2).margins(
					LayoutConstants.getMargins()).generateLayout(shell);
		}

		return shell;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = createShell1();
		shell.pack();
		shell.open();

		Shell shell2 = createShell2();
		shell2.pack();
		shell2.open();

		Shell shell3 = createShell3();
		shell3.pack();
		shell3.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

}
