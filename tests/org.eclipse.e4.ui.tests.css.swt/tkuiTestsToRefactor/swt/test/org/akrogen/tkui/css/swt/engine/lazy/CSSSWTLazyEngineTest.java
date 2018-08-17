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
package org.akrogen.tkui.css.swt.engine.lazy;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.resources.CSSSWTResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CSSSWTLazyEngineTest {

	public static void main(String[] args) {
		try {

			// Instanciate Display at first
			Display display = new Display();

			// Instanciate SWT CSS Engine
			CSSEngine engine = new CSSSWTEngineImpl(display, true);
			engine.parseStyleSheet(CSSSWTResources.getSWT());

			/*---   UI SWT ---*/

			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			// Label
			Label label1 = new Label(panel1, SWT.NONE);
			label1.setText("Label 0");

			// Text
			Text text1 = new Text(panel1, SWT.NONE);
			text1.setText("bla bla bla...");

			Label label2 = new Label(panel1, SWT.NONE);
			label2.setText("Label 2 [label.setData('MyId')]");
			label2.setData("id", "MyId");

			Label label3 = new Label(panel1, SWT.NONE);
			label3.setText("Label 3 [label.setData('MyId2')]");
			label3.setData("id", "MyId2");

			// Composite
			Composite panel2 = new Composite(panel1, SWT.NONE);
			panel2.setLayout(new FillLayout());

			// Label
			Label label4 = new Label(panel2, SWT.NONE);
			label4.setText("Label 4");

			/*---   End UI SWT  ---*/

			shell.pack();
			shell.open();

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
