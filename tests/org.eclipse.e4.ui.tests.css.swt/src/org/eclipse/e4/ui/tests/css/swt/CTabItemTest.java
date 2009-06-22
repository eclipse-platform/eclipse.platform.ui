/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CTabItemTest extends CSSSWTTestCase {
	
	private CSSEngine engine;

	protected CTabFolder createTestTabFolder(String styleSheet) {
		Display display = Display.getDefault();
		engine = createEngine(styleSheet, display);

		// Create widgets
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		CTabFolder buttonToTest = new CTabFolder(shell, SWT.BORDER);
		buttonToTest.setSelectionForeground(display
				.getSystemColor(SWT.COLOR_RED));
		for (int i = 0; i < 4; i++) {
			final CTabItem item = new CTabItem(buttonToTest, SWT.CLOSE);
			item.setText("Item " + i);

			Button control = new Button(buttonToTest, SWT.PUSH);
			item.setControl(control);
		}

		// Apply styles
		engine.applyStyles(shell, true);

		shell.pack();
		return buttonToTest;
	}

	public void testFontRegular() throws Exception {
		CTabFolder folder = createTestTabFolder("Button { font-family: Verdana; font-size: 12 }\n"
				+ "CTabItem { font-family: Verdana; font-size: 16 }");
		CTabItem[] items = folder.getItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals("Verdana", fontData.getName());
			assertEquals(16, fontData.getHeight());
			assertEquals(SWT.NORMAL, fontData.getStyle());
			
			// verify retrieval
			assertEquals("Verdana", engine.retrieveCSSProperty(items[i], "font-family", null));
			assertEquals("16", engine.retrieveCSSProperty(items[i], "font-size", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals("Verdana", fontData.getName());
			assertEquals(12, fontData.getHeight());
			assertEquals(SWT.NORMAL, fontData.getStyle());
		}
	}

	public void testFontBold() throws Exception {
		CTabFolder folder = createTestTabFolder("Button { font-weight: bold }\n"
				+ "CTabItem { font-weight: bold }");
		CTabItem[] items = folder.getItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());

			// verify retrieval
			assertEquals("bold", engine.retrieveCSSProperty(items[i], "font-weight", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());
		}
	}

	public void testFontItalic() throws Exception {
		CTabFolder folder = createTestTabFolder("Button { font-weight: bold }\n"
				+ "CTabItem { font-style: italic }");
		CTabItem[] items = folder.getItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals(SWT.ITALIC, fontData.getStyle());
			
			// verify retrieval
			assertEquals("italic", engine.retrieveCSSProperty(items[i], "font-style", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());
		}
	}
}
