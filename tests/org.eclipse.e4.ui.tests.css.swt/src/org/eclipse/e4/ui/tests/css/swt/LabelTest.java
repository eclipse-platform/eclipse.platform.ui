/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
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
 *      Remy Chi Jian Suen <remy.suen@gmail.com> - bug 137650
 *      Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.swt;

import static org.eclipse.e4.ui.tests.css.swt.CssSwtEngine.BLUE;
import static org.eclipse.e4.ui.tests.css.swt.CssSwtEngine.RED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class LabelTest {

	@RegisterExtension
	CssSwtEngine css = new CssSwtEngine();

	@Test
	void testColor() {
		Label labelToTest = css.createTestLabel("Label { background-color: #FF0000; color: #0000FF }");
		assertEquals(RED, labelToTest.getBackground().getRGB());
		assertEquals(BLUE, labelToTest.getForeground().getRGB());
	}

	@Test
	void testFontRegular() {
		Label labelToTest = css.createTestLabel("Label { font: Verdana 16pt }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertEquals(16, fontData.getHeight());
		assertEquals(SWT.NORMAL, fontData.getStyle());
	}

	@Test
	void testFontBold() {
		Label labelToTest = css.createTestLabel("Label { font: Arial 12pt; font-weight: bold }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Arial", fontData.getName());
		assertEquals(12, fontData.getHeight());
		assertEquals(SWT.BOLD, fontData.getStyle());
	}

	@Test
	void testFontItalic() {
		Label labelToTest = css.createTestLabel("Label { font-style: italic }");
		assertEquals(1, labelToTest.getFont().getFontData().length);
		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals(SWT.ITALIC, fontData.getStyle());
	}

	@Test
	void testFontSizeInPercent() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();

		Label labelToTest = css.createTestLabel("Label { font-size: 200% }");

		assertEquals(inheritedHeight * 2, labelToTest.getFont().getFontData()[0].getHeight());
	}

	@Test
	void testFontSizeInEm() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();

		Label labelToTest = css.createTestLabel("Label { font-size: 2em }");

		assertEquals(inheritedHeight * 2, labelToTest.getFont().getFontData()[0].getHeight());
	}

	@Test
	void testFontSizeLarger() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();

		Label labelToTest = css.createTestLabel("Label { font-size: larger }");

		assertTrue(labelToTest.getFont().getFontData()[0].getHeight() > inheritedHeight);
	}

	@Test
	void testFontSizeInPixels() {
		Label labelToTest = css.createTestLabel("Label { font-size: 16px }");

		assertEquals(12, labelToTest.getFont().getFontData()[0].getHeight());
	}

	@Test
	void testFontSizeInPoints() {
		Label labelToTest = css.createTestLabel("Label { font-size: 16pt }");

		assertEquals(16, labelToTest.getFont().getFontData()[0].getHeight());
	}

	@Test
	void testFontSizeSmallerInShorthand() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();

		Label labelToTest = css.createTestLabel("Label { font: Verdana smaller }");

		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertTrue(fontData.getHeight() < inheritedHeight);
	}

	@Test
	void testFontSizeInPercentIsNotCompoundedWhenStylesAreReapplied() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();
		Label labelToTest = css.createTestLabel("Label { font-size: 200% }");
		FontData styledFontData = labelToTest.getFont().getFontData()[0];

		// styling the label again with a cold font cache must scale the font the
		// label started with, not the one the first pass gave it
		css.getEngine().getResourcesRegistry().dispose();
		Font styledFont = new Font(css.getDisplay(), styledFontData);
		labelToTest.setFont(styledFont);
		css.getEngine().applyStyles(labelToTest, true);

		assertEquals(inheritedHeight * 2, labelToTest.getFont().getFontData()[0].getHeight());
		styledFont.dispose();
	}

	@Test
	void testFontSizeInPercentIsResolvedPerWidget() {
		CSSEngine engine = css.createEngine("Label { font-size: 200% }");
		Shell shell = new Shell(css.getDisplay());
		Label smallLabel = new Label(shell, SWT.NONE);
		Label largeLabel = new Label(shell, SWT.NONE);
		// same family and style, so the two only differ in the size they scale from
		Font smallFont = new Font(css.getDisplay(), "Arial", 10, SWT.NORMAL);
		Font largeFont = new Font(css.getDisplay(), "Arial", 20, SWT.NORMAL);
		smallLabel.setFont(smallFont);
		largeLabel.setFont(largeFont);

		engine.applyStyles(smallLabel, true);
		engine.applyStyles(largeLabel, true);

		assertEquals(20, smallLabel.getFont().getFontData()[0].getHeight());
		assertEquals(40, largeLabel.getFont().getFontData()[0].getHeight());
		shell.dispose();
		smallFont.dispose();
		largeFont.dispose();
	}

	@Test
	void testFontSizeKeywordInShorthandIsCaseInsensitive() {
		int inheritedHeight = css.getDisplay().getSystemFont().getFontData()[0].getHeight();

		Label labelToTest = css.createTestLabel("Label { font: Verdana LARGER }");

		FontData fontData = labelToTest.getFont().getFontData()[0];
		assertEquals("Verdana", fontData.getName());
		assertTrue(fontData.getHeight() > inheritedHeight);
	}

	@Test
	void testQuotedFontSizeKeywordInShorthandIsAFamily() {
		Label labelToTest = css.createTestLabel("Label { font: 'smaller' 12pt }");

		assertEquals(12, labelToTest.getFont().getFontData()[0].getHeight());
	}

	@Test
	void testAlignment() {
		Label labelToTest = css.createTestLabel("Label { swt-alignment: right }");
		assertEquals(SWT.RIGHT, labelToTest.getAlignment());

		labelToTest = css.createTestLabel("Label { swt-alignment: center; }");
		assertEquals(SWT.CENTER, labelToTest.getAlignment());

		labelToTest = css.createTestLabel("Label { swt-alignment: left; }");
		assertEquals(SWT.LEFT, labelToTest.getAlignment());

	}

	@Test
	void testAlignment2() {
		Label labelToTest = css.createTestLabel("Label { swt-alignment: trail }");
		assertEquals(SWT.TRAIL, labelToTest.getAlignment());

		labelToTest = css.createTestLabel("Label { swt-alignment: lead; }");
		assertEquals(SWT.LEAD, labelToTest.getAlignment());
	}
}
