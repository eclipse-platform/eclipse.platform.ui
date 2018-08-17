/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.akrogen.tkui.css.swt.engine.pseudoClasses;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.resources.CSSSWTResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CSSSWTEnginePseudoClassesTest {

	public static void main(String[] args) {
		try {
			Display display = new Display();
			// Instanciate SWT CSS Engine			
			CSSEngine engine = new CSSSWTEngineImpl(display);
			engine.parseStyleSheet(CSSSWTResources.getSWTPseudoCLass());

			/*---   UI SWT ---*/
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			// Text
			Text text1 = new Text(panel1, SWT.NONE);
			text1.setText("bla bla bla...");
			text1.setEnabled(false);

			Text text2 = new Text(panel1, SWT.NONE);
			text2.setText("bla bla bla...");

			Text text3 = new Text(panel1, SWT.NONE);
			text3.setText("bla bla bla...");

			final Button checkbox = new Button(panel1, SWT.CHECK);
			checkbox.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					System.out.println(checkbox.getSelection());
				}
			});

			final Button radio = new Button(panel1, SWT.RADIO);

			final Button button = new Button(panel1, SWT.BORDER);
			button.setText("SWT Button");

			/*---   End UI SWT  ---*/

			shell.pack();
			shell.open();

			// Apply Styles
			engine.applyStyles(shell, true);

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			display.dispose();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
