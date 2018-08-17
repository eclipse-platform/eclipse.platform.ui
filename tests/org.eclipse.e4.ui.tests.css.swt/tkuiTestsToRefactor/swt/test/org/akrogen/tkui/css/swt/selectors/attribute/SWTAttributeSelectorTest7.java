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
package org.akrogen.tkui.css.swt.selectors.attribute;

import java.io.StringReader;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * Selector=E[hreflang|="en"] an E element whose "hreflang" attribute has a
 * hyphen-separated list of values beginning (from the left) with "en"
 * 
 */
public class SWTAttributeSelectorTest7 {

	public static void main(String[] args) {
		try {
			Display display = new Display();
			CSSEngine engine = new CSSSWTEngineImpl(display);
			engine.parseStyleSheet(new StringReader(
					"Label[hreflang|=\"en\"] {color:red} Label {color:green}"));

			/*---   UI SWT ---*/
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			// Label
			Label label1 = new Label(panel1, SWT.NONE);
			label1.setText("Label england");
			label1.setData("hreflang", "england");

			// Label
			Label label2 = new Label(panel1, SWT.NONE);
			label2.setText("Label ");

			/*---   End UI SWT  ---*/
			// Apply Styles
			engine.applyStyles(shell, true);

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
