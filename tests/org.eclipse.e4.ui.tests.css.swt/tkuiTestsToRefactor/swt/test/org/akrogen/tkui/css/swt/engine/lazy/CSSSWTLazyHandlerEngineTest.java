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

import java.io.StringReader;

import org.akrogen.tkui.css.core.impl.engine.CSSErrorHandlerImpl;
import org.akrogen.tkui.css.swt.engine.CSSSWTLazyHandlerEngineImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CSSSWTLazyHandlerEngineTest {

	public static void main(String[] args) {
		Display display = new Display();
		CSSSWTLazyHandlerEngineImpl engine = new CSSSWTLazyHandlerEngineImpl(
				display);
		// CSSEngine Print stack trace when Exception is thrown
		engine.setErrorHandler(CSSErrorHandlerImpl.INSTANCE);

		try {
			engine
					.parseStyleSheet(new StringReader(
							"Label:hover {color:red;border:solid green 2px;font:30px Arial italic normal;background: url(./images/icons/type/class.gif) yellow;} "
									+ "Text {cursor:wait;background-color:white red 100%;} "));

			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			FillLayout layout = new FillLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new FillLayout());

			// Label
			Label label1 = new Label(panel1, SWT.NONE);
			label1.setText("vfvfvf");

			// Text
			Text text1 = new Text(panel1, SWT.NONE);
			text1.setText("bla bla bla...");

			// Apply Styles
			engine.applyStyles(shell, true);

			shell.pack();
			shell.open();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}

			display.dispose();
			// engine.getResourcesRegistry().dispose();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
