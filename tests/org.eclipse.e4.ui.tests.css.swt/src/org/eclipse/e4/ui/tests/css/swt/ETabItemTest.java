/*******************************************************************************
 * Copyright (c) 2009 Remy Chi Jian Suen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Remy Chi Jian Suen <remy.suen@gmail.com> - initial API and implementation
 *     IBM Corporation
 ******************************************************************************/

/*
 * This class was copied from CTabItemTest 1.3 to verify bug #283742.
 * It also takes into account differences in 
 * ETabFolder.getBackground() v.s. ETabFolder.getUnselectedBackground()
 */

package org.eclipse.e4.ui.tests.css.swt;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.SWTElement;
import org.eclipse.swt.SWT;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.e4.ui.widgets.ETabItem;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ETabItemTest extends CSSSWTTestCase {

	private CSSEngine engine;

	private Shell shell;

	protected void tearDown() throws Exception {
		if (shell != null) {
			shell.dispose();
			shell = null;
		}
		super.tearDown();
	}

	private void spinEventLoop() {
		while (shell.getDisplay().readAndDispatch())
			;
	}

	private ETabFolder createFolder(Composite composite) {
		ETabFolder folderToTest = new ETabFolder(composite, SWT.BORDER);
		for (int i = 0; i < 4; i++) {
			final ETabItem item = new ETabItem(folderToTest, SWT.NONE);
			item.setText("Item " + i);

			Button control = new Button(folderToTest, SWT.PUSH);
			item.setControl(control);
		}
		return folderToTest;
	}

	private ETabFolder createTestTabFolder() {
		return createTestTabFolder(true);
	}

	private ETabFolder createTestTabFolder(boolean open) {
		Display display = Display.getDefault();

		// Create widgets
		shell = new Shell(display, SWT.SHELL_TRIM);
		FillLayout layout = new FillLayout();
		shell.setLayout(layout);

		ETabFolder folderToTest = createFolder(shell);

		if (open) {
			shell.open();
		}
		return folderToTest;
	}

	private ETabFolder createTestTabFolder(String styleSheet) {
		return createTestTabFolder(styleSheet, true);
	}

	protected ETabFolder createTestTabFolder(String styleSheet, boolean open) {
		ETabFolder folder = createTestTabFolder(open);

		engine = createEngine(styleSheet, folder.getDisplay());

		// Apply styles
		engine.applyStyles(folder.getShell(), true);

		return folder;
	}

	public void testFontRegular() throws Exception {
		ETabFolder folder = createTestTabFolder("Button { font-family: Verdana; font-size: 12 }\n"
				+ "ETabItem { font-family: Verdana; font-size: 16 }");
		spinEventLoop();
		ETabItem[] items = folder.getETabItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals("Verdana", fontData.getName());
			assertEquals(16, fontData.getHeight());
			assertEquals(SWT.NORMAL, fontData.getStyle());

			// verify retrieval
			assertEquals("Verdana", engine.retrieveCSSProperty(items[i],
					"font-family", null));
			assertEquals("16", engine.retrieveCSSProperty(items[i],
					"font-size", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals("Verdana", fontData.getName());
			assertEquals(12, fontData.getHeight());
			assertEquals(SWT.NORMAL, fontData.getStyle());
		}
	}

	public void testFontBold() throws Exception {
		ETabFolder folder = createTestTabFolder("Button { font-weight: bold }\n"
				+ "ETabItem { font-weight: bold }");
		spinEventLoop();

		ETabItem[] items = folder.getETabItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());

			// verify retrieval
			assertEquals("bold", engine.retrieveCSSProperty(items[i],
					"font-weight", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());
		}
	}

	public void testFontItalic() throws Exception {
		ETabFolder folder = createTestTabFolder("Button { font-weight: bold }\n"
				+ "ETabItem { font-style: italic }");
		spinEventLoop();

		ETabItem[] items = folder.getETabItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			assertEquals(SWT.ITALIC, fontData.getStyle());

			// verify retrieval
			assertEquals("italic", engine.retrieveCSSProperty(items[i],
					"font-style", null));

			// make sure child controls are styled
			Control button = items[i].getControl();
			fontData = button.getFont().getFontData()[0];
			assertEquals(SWT.BOLD, fontData.getStyle());
		}
	}

	private void testSelectedFontBold(ETabFolder folder, int selectionIndex)
			throws Exception {
		folder.setSelection(selectionIndex);
		spinEventLoop();

		ETabItem[] items = folder.getETabItems();
		for (int i = 0; i < items.length; i++) {
			FontData fontData = items[i].getFont().getFontData()[0];
			if (i == selectionIndex) {
				assertEquals(SWT.BOLD, fontData.getStyle());
			} else {
				assertEquals(SWT.NORMAL, fontData.getStyle());
			}
		}
	}

	public void testSelectedFontBold() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem:selected { font-weight: bold }");
		spinEventLoop();
		for (int i = 0; i < folder.getItemCount(); i++) {
			testSelectedFontBold(folder, i);
		}
	}

	public void testSelectedFontMerged() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { font-weight: normal; font-style: italic }\n"
				+ "ETabItem:selected { font-weight: bold }");
		spinEventLoop();
		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			FontData fd = item.getFont().getFontData()[0];
			if (item == folder.getSelection()) {
				assertEquals(SWT.BOLD | SWT.ITALIC, fd.getStyle());
			} else {
				assertEquals(SWT.ITALIC, fd.getStyle());
			}
		}
	}

	public void testSelectedFontMerged2() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { font-style: italic }\n"
				+ "ETabItem:selected { font-weight: bold }");
		spinEventLoop();
		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			FontData fd = item.getFont().getFontData()[0];
			if (item == folder.getSelection()) {
				assertEquals(SWT.BOLD | SWT.ITALIC, fd.getStyle());
			} else {
				assertEquals(SWT.ITALIC, fd.getStyle());
			}
		}
	}

	public void testSelectedFontMerged3() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { font-weight: bold }\n"
				+ "ETabItem:selected { font-style: italic; font-weight: normal }");
		spinEventLoop();
		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			FontData fd = item.getFont().getFontData()[0];
			if (item == folder.getSelection()) {
				assertEquals(SWT.ITALIC, fd.getStyle());
			} else {
				assertEquals(SWT.BOLD, fd.getStyle());
			}
		}
	}

	private void testShowClose(boolean showClose) throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { show-close: "
				+ Boolean.toString(showClose) + " }");
		ETabItem[] items = folder.getETabItems();
		for (int i = 0; i < items.length; i++) {
			assertEquals(showClose, items[i].getShowClose());
			assertEquals(Boolean.toString(showClose), engine
					.retrieveCSSProperty(items[i], "show-close", null));
		}
	}

	public void testShowCloseFalse() throws Exception {
		testShowClose(false);
	}

	public void testShowCloseTrue() throws Exception {
		testShowClose(true);
	}

	public void testShowClose() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { show-close: true }");
		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(true, folder.getItem(i).getShowClose());
		}

		engine = createEngine("ETabItem { show-close: false }", folder
				.getDisplay());
		engine.applyStyles(folder.getShell(), true);
		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(false, folder.getItem(i).getShowClose());
		}
	}

	public void testShowClose2() throws Exception {
		ETabFolder folder = createTestTabFolder();
		ETabFolder folder2 = createFolder(folder.getShell());
		engine = createEngine("ETabItem { show-close: true }", folder
				.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(true, folder.getItem(i).getShowClose());
		}
		for (int i = 0; i < folder2.getItemCount(); i++) {
			assertEquals(true, folder2.getItem(i).getShowClose());
		}

		engine = createEngine("ETabItem { show-close: false }", folder
				.getDisplay());
		engine.applyStyles(folder.getShell(), true);
		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(false, folder.getItem(i).getShowClose());
		}
		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(false, folder2.getItem(i).getShowClose());
		}
	}

	private void testSelectedShowClose(ETabFolder folder, int index) {
		ETabItem[] items = folder.getETabItems();
		folder.setSelection(index);
		spinEventLoop();

		for (int i = 0; i < items.length; i++) {
			if (i == index) {
				assertEquals(true, items[i].getShowClose());
				assertEquals("true", engine.retrieveCSSProperty(items[i],
						"show-close", null));
			} else {
				assertEquals(false, items[i].getShowClose());
				assertEquals("false", engine.retrieveCSSProperty(items[i],
						"show-close", null));
			}
		}
	}

	public void testSelectedShowClose() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem:selected { show-close: true }");
		for (int i = 0; i < folder.getItemCount(); i++) {
			testSelectedShowClose(folder, i);
		}

		engine = createEngine("ETabItem:selected { show-close: false }", folder
				.getDisplay());
		engine.applyStyles(folder.getShell(), true);
		for (int i = 0; i < folder.getItemCount(); i++) {
			assertFalse(folder.getItem(i).getShowClose());
		}
	}

	public void testSelectedShowClose2() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { show-close: false }\n"
				+ "ETabItem:selected { show-close: true }");
		for (int i = 0; i < folder.getItemCount(); i++) {
			testSelectedShowClose(folder, i);
		}
	}

	public void testClassSelectedShowClose() throws Exception {
		ETabFolder folder = createTestTabFolder();
		SWTElement.setCSSClass(folder, "editorStack");

		CSSEngine engine = createEngine(
				"ETabFolder.editorStack ETabItem { show-close: true }", folder
						.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertTrue(folder.getItem(i).getShowClose());
		}
	}

	public void testFontsEditorStackClass() {
		ETabFolder folder = createTestTabFolder(false);
		ETabFolder folder2 = createFolder(folder.getShell());

		SWTElement.setCSSClass(folder2, "editorStack");
		engine = createEngine(
				"ETabItem { font-size: 10 }"
						+ "ETabItem:selected { font-size: 14; font-weight: bold }"
						+ "ETabFolder.editorStack ETabItem { font-size: 11; }"
						+ "ETabFolder.editorStack ETabItem:selected { font-size: 13; font-style: italic }",
				folder.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		folder.getShell().open();
		folder.setSelection(0);

		spinEventLoop();

		assertNotNull(folder.getSelection());
		assertNull(folder2.getSelection());

		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];

			if (item == folder.getSelection()) {
				assertEquals(14, data.getHeight());
				assertEquals(SWT.BOLD, data.getStyle());
			} else {
				assertEquals(10, data.getHeight());
				assertEquals(SWT.NORMAL, data.getStyle());
			}
		}

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];

			assertEquals(11, data.getHeight());
			assertEquals(SWT.NORMAL, data.getStyle());
		}

		folder2.setSelection(0);
		spinEventLoop();

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];
			if (item == folder2.getSelection()) {
				assertEquals(13, data.getHeight());
				assertEquals(SWT.ITALIC | SWT.BOLD, data.getStyle());
			} else {
				assertEquals(11, data.getHeight());
				assertEquals(SWT.NORMAL, data.getStyle());
			}
		}
	}

	public void testFontsEditorStackClass2() {
		ETabFolder folder = createTestTabFolder(false);
		ETabFolder folder2 = createFolder(folder.getShell());

		SWTElement.setCSSClass(folder2, "editorStack");
		engine = createEngine(
				"ETabItem { font-size: 10 }"
						+ "ETabItem:selected { font-size: 14; font-weight: bold }"
						+ "ETabFolder.editorStack ETabItem { font-size: 11; }"
						+ "ETabFolder.editorStack ETabItem:selected { font-size: 13; font-weight: normal; font-style: italic }",
				folder.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		folder.getShell().open();
		folder.setSelection(0);

		spinEventLoop();

		assertNotNull(folder.getSelection());
		assertNull(folder2.getSelection());

		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];

			if (item == folder.getSelection()) {
				assertEquals(14, data.getHeight());
				assertEquals(SWT.BOLD, data.getStyle());
			} else {
				assertEquals(10, data.getHeight());
				assertEquals(SWT.NORMAL, data.getStyle());
			}
		}

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];

			assertEquals(11, data.getHeight());
			assertEquals(SWT.NORMAL, data.getStyle());
		}

		folder2.setSelection(0);
		spinEventLoop();

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			FontData data = item.getFont().getFontData()[0];
			if (item == folder2.getSelection()) {
				assertEquals(13, data.getHeight());
				assertEquals(SWT.ITALIC, data.getStyle());
			} else {
				assertEquals(11, data.getHeight());
				assertEquals(SWT.NORMAL, data.getStyle());
			}
		}
	}

	public void testShowCloseEditorStack() {
		ETabFolder folder = createTestTabFolder(false);
		ETabFolder folder2 = createFolder(folder.getShell());

		SWTElement.setCSSClass(folder2, "editorStack");
		engine = createEngine("ETabItem { show-close: false }"
				+ "ETabItem:selected { show-close: true }"
				+ "ETabFolder.editorStack ETabItem { show-close: true }",
				folder.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		folder.getShell().open();
		folder.setSelection(0);

		spinEventLoop();

		assertNotNull(folder.getSelection());
		assertNull(folder2.getSelection());

		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			if (item == folder.getSelection()) {
				assertTrue(item.getShowClose());
			} else {
				assertFalse(item.getShowClose());
			}
		}

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			assertTrue(item.getShowClose());
		}

		folder2.setSelection(0);
		spinEventLoop();

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			assertTrue(item.getShowClose());
		}
	}

	public void testShowCloseViewStack() {
		ETabFolder folder = createTestTabFolder(false);
		ETabFolder folder2 = createFolder(folder.getShell());

		SWTElement.setCSSClass(folder2, "viewStack");
		engine = createEngine(
				"ETabItem { show-close: false }"
						+ "ETabItem:selected { show-close: true }"
						+ "ETabFolder.viewStack ETabItem { show-close: false }"
						+ "ETabFolder.viewStack ETabItem.selected { show-close: true }",
				folder.getDisplay());
		engine.applyStyles(folder.getShell(), true);

		folder.getShell().open();
		folder.setSelection(0);

		spinEventLoop();

		assertNotNull(folder.getSelection());
		assertNull(folder2.getSelection());

		for (int i = 0; i < folder.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			if (item == folder.getSelection()) {
				assertTrue(item.getShowClose());
			} else {
				assertFalse(item.getShowClose());
			}
		}

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder2.getETabItem(i);
			assertFalse(item.getShowClose());
		}

		folder2.setSelection(0);
		spinEventLoop();

		for (int i = 0; i < folder2.getItemCount(); i++) {
			ETabItem item = folder.getETabItem(i);
			if (item == folder.getSelection()) {
				assertTrue(item.getShowClose());
			} else {
				assertFalse(item.getShowClose());
			}
		}
	}

	public void testBackground() throws Exception {
		ETabFolder folder = createTestTabFolder(
				"ETabItem { background-color: #0000ff }", false);
		assertEquals(new RGB(0, 0, 255), folder.getUnselectedTabBackgroundColor().getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals("#0000ff", engine.retrieveCSSProperty(folder
					.getItem(i), "background-color", null));
		}
	}

	public void testBackground2() throws Exception {
		ETabFolder folder = createTestTabFolder(false);
		Color preStyledSelectionBackground = folder.getSelectionBackground();

		RGB rgb = new RGB(0, 0, 255);
		String colour = "#0000ff";

		// we want to make sure we pick a unique colour so that we actually test that the selection's colour has not changed
		if (rgb.equals(preStyledSelectionBackground.getRGB())) {
			rgb = new RGB(0, 255, 0);
			colour = "#00ff00";
		}

		CSSEngine engine = createEngine("ETabItem { background-color: " + colour + " }",
				folder.getDisplay());
		engine.applyStyles(folder, true);

		assertEquals(rgb, folder.getUnselectedTabBackgroundColor().getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(colour, engine.retrieveCSSProperty(folder.getItem(i),
					"background-color", null));
		}

		assertEquals(preStyledSelectionBackground.getRGB(), folder
				.getSelectionBackground().getRGB());
	}

	public void testSelectionBackground() throws Exception {
		ETabFolder folder = createTestTabFolder(
				"ETabItem:selected { background-color: #00ff00 }", false);
		assertEquals(new RGB(0, 255, 0), folder.getSelectionBackground()
				.getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals("#00ff00", engine.retrieveCSSProperty(folder
					.getItem(i), "background-color", "selected"));
		}
	}

	public void testForeground() throws Exception {
		ETabFolder folder = createTestTabFolder("ETabItem { color: #0000ff }",
				false);
		assertEquals(new RGB(0, 0, 255), folder.getForeground().getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals("#0000ff", engine.retrieveCSSProperty(folder
					.getItem(i), "color", null));
		}
	}

	public void testForeground2() throws Exception {
		ETabFolder folder = createTestTabFolder(false);
		Color preStyledSelectionForeground = folder.getSelectionForeground();

		RGB rgb = new RGB(0, 0, 255);
		String colour = "#0000ff";

		// we want to make sure we pick a unique colour so that we actually test that the selection's colour has not changed
		if (rgb.equals(preStyledSelectionForeground.getRGB())) {
			rgb = new RGB(0, 255, 0);
			colour = "#00ff00";
		}

		CSSEngine engine = createEngine("ETabItem { color: " + colour + " }",
				folder.getDisplay());
		engine.applyStyles(folder, true);

		assertEquals(rgb, folder.getForeground().getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals(colour, engine.retrieveCSSProperty(folder.getItem(i),
					"color", null));
		}

		assertEquals(preStyledSelectionForeground.getRGB(), folder
				.getSelectionForeground().getRGB());
	}

	public void testSelectionForeground() throws Exception {
		ETabFolder folder = createTestTabFolder(
				"ETabItem:selected { color: #00ff00 }", false);
		assertEquals(new RGB(0, 255, 0), folder.getSelectionForeground()
				.getRGB());

		for (int i = 0; i < folder.getItemCount(); i++) {
			assertEquals("#00ff00", engine.retrieveCSSProperty(folder
					.getItem(i), "color", "selected"));
		}
	}
}
