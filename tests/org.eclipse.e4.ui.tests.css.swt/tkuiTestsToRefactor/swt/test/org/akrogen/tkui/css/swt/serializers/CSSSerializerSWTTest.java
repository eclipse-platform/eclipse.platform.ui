/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
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
package org.akrogen.tkui.css.swt.serializers;

import java.io.StringWriter;

import org.akrogen.tkui.css.core.engine.CSSEngine;
import org.akrogen.tkui.css.core.serializers.CSSHTMLSerializerConfiguration;
import org.akrogen.tkui.css.core.serializers.CSSSerializer;
import org.akrogen.tkui.css.swt.engine.CSSSWTEngineImpl;
import org.akrogen.tkui.css.swt.engine.html.CSSSWTHTMLEngineImpl;
import org.akrogen.tkui.css.swt.serializers.CSSSWTSerializerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class CSSSerializerSWTTest {

	private static Color COLOR_GREEN = null;

	private static Cursor CURSOR_HELP = null;

	public static void main(String[] args) {
		try {

			/*---   UI SWT ---*/
			Display display = new Display();
			createResources(display);
			Shell shell = new Shell(display, SWT.SHELL_TRIM);
			GridLayout layout = new GridLayout();
			shell.setLayout(layout);

			Composite panel1 = new Composite(shell, SWT.NONE);
			panel1.setLayout(new GridLayout());

			// Text
			Text text1 = new Text(panel1, SWT.NONE);
			text1.setText("bla bla bla...");

			// TextArea
			Text textarea = new Text(panel1, SWT.MULTI);
			textarea.setText("bla bla bla...");

			// Label
			Label label3 = new Label(panel1, SWT.NONE);
			label3.setText("Label 3");
			label3.setBackground(COLOR_GREEN);
			label3.setCursor(CURSOR_HELP);

			// Create Button
			Button button = new Button(panel1, SWT.BORDER);
			button.setText("SWT Button");

			// Create Button [SWT.CHECK]
			Button checkbox = new Button(panel1, SWT.CHECK);
			checkbox.setText("SWT Button [SWT.CHECK]");

			// Create Button [SWT.RADIO]
			Button radio = new Button(panel1, SWT.RADIO);
			radio.setText("SWT Button [SWT.RADIO]");

			// Create Combo
			Combo combo = new Combo(panel1, SWT.BORDER);
			combo.add("Item 1");
			combo.add("Item 2");
			combo.select(0);

			/*---   End UI SWT  ---*/

			// Serialize Shell and children SWT Widgets
			// STWT Selector
			CSSEngine engine = new CSSSWTEngineImpl(display);
			System.out
					.println("***************** CSS SWT Selector *****************");
			CSSSerializer serializer = new CSSSerializer();
			StringWriter writer = new StringWriter();
			serializer.serialize(writer, engine, shell, true,
					CSSSWTSerializerConfiguration.INSTANCE);
			System.out.println(writer.toString());

			// HTML Selector
			engine = new CSSSWTHTMLEngineImpl(display);
			System.out
					.println("***************** CSS SWT-HTML Selector *****************");
			writer = new StringWriter();
			serializer.serialize(writer, engine, shell, true,
					CSSHTMLSerializerConfiguration.INSTANCE);
			System.out.println(writer.toString());

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

	private static void createResources(Display display) {
		COLOR_GREEN = new Color(display, 0, 255, 0);
		CURSOR_HELP = new Cursor(display, SWT.CURSOR_HELP);

		display.addListener(SWT.Dispose, event -> disposeResources());
	}

	private static void disposeResources() {
		COLOR_GREEN.dispose();
		CURSOR_HELP.dispose();
	}
}
