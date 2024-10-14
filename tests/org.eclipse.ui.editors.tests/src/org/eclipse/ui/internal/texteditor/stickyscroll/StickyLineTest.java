/*******************************************************************************
 * Copyright (c) 2024 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.texteditor.stickyscroll;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Shell;

public class StickyLineTest {

	private Shell shell;
	private StyledText textWidget;
	private Color color;

	@Before
	public void setUp() {
		shell = new Shell();
		textWidget = new StyledText(shell, SWT.NONE);
		color = new Color(0, 0, 0);
	}

	@After
	public void tearDown() {
		shell.dispose();
		color.dispose();
	}

	@Test
	public void testGetLineNumber() {
		StickyLine stickyLine = new StickyLine(1, textWidget);

		assertEquals(1, stickyLine.getLineNumber());
	}

	@Test
	public void testGetText() {
		textWidget.setText("line1\nline2\nline3");
		StickyLine stickyLine = new StickyLine(1, textWidget);

		assertEquals("line2", stickyLine.getText());
	}

	@Test
	public void testGetStyleRanges() {
		textWidget.setText("line1\nline2\nline3");

		// line1
		textWidget.setStyleRange(new StyleRange(2, 1, color, null));

		// line2
		textWidget.setStyleRange(new StyleRange(6, 1, color, null));
		textWidget.setStyleRange(new StyleRange(8, 2, color, null));

		// line3
		textWidget.setStyleRange(new StyleRange(15, 1, color, null));

		StickyLine stickyLine = new StickyLine(1, textWidget);
		StyleRange[] styleRanges = stickyLine.getStyleRanges();

		assertEquals(2, styleRanges.length);
		assertEquals(0, styleRanges[0].start);
		assertEquals(1, styleRanges[0].length);
		assertEquals(2, styleRanges[1].start);
		assertEquals(2, styleRanges[1].length);
	}

}
