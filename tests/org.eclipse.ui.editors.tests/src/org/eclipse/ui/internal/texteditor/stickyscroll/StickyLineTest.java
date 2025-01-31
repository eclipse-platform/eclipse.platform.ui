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
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;

public class StickyLineTest {

	private Shell shell;
	private StyledText textWidget;
	private Color color;
	private ISourceViewer sourceViewer;

	@Before
	public void setUp() {
		shell = new Shell();
		sourceViewer = new SourceViewer(shell, null, SWT.None);
		sourceViewer.setDocument(new Document());
		textWidget = sourceViewer.getTextWidget();
		color = new Color(0, 0, 0);
	}

	@After
	public void tearDown() {
		shell.dispose();
		color.dispose();
	}

	@Test
	public void testGetLineNumber() {
		StickyLine stickyLine = new StickyLine(1, sourceViewer);

		assertEquals(1, stickyLine.getLineNumber());
	}

	@Test
	public void testGetText() {
		textWidget.setText("line1\nline2\nline3");
		StickyLine stickyLine = new StickyLine(1, sourceViewer);

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

		StickyLine stickyLine = new StickyLine(1, sourceViewer);
		StyleRange[] styleRanges = stickyLine.getStyleRanges();

		assertEquals(2, styleRanges.length);
		assertEquals(0, styleRanges[0].start);
		assertEquals(1, styleRanges[0].length);
		assertEquals(2, styleRanges[1].start);
		assertEquals(2, styleRanges[1].length);
	}

	@Test
	public void testGetStyleRangesIgnoresOutOfBoundLines() {
		textWidget.setText("line1\nline2\nline3");

		StickyLine stickyLineOutOfBound = new StickyLine(10, sourceViewer);
		assertNull(stickyLineOutOfBound.getStyleRanges());

		stickyLineOutOfBound = new StickyLine(3, sourceViewer);
		assertNull(stickyLineOutOfBound.getStyleRanges());
	}

	@Test
	public void WithSourceViewerLineMapping() {
		sourceViewer = new SourceViewerWithLineMapping(shell, null, SWT.None);
		sourceViewer.setDocument(new Document());
		textWidget = sourceViewer.getTextWidget();

		textWidget.setText("line1\nline2\nline3");

		// line1
		textWidget.setStyleRange(new StyleRange(2, 1, color, null));

		// line2
		textWidget.setStyleRange(new StyleRange(6, 1, color, null));
		textWidget.setStyleRange(new StyleRange(8, 2, color, null));

		// line3
		textWidget.setStyleRange(new StyleRange(15, 1, color, null));

		StickyLine stickyLine = new StickyLine(1 + 42, sourceViewer);
		StyleRange[] styleRanges = stickyLine.getStyleRanges();

		assertEquals(1 + 42, stickyLine.getLineNumber());

		assertEquals("line2", stickyLine.getText());

		assertEquals(2, styleRanges.length);
		assertEquals(0, styleRanges[0].start);
		assertEquals(1, styleRanges[0].length);
		assertEquals(2, styleRanges[1].start);
		assertEquals(2, styleRanges[1].length);
	}

	private class SourceViewerWithLineMapping extends SourceViewer implements ITextViewerExtension5 {

		public SourceViewerWithLineMapping(Composite parent, IVerticalRuler ruler, int styles) {
			super(parent, ruler, styles);
		}

		@Override
		public IRegion[] getCoveredModelRanges(IRegion modelRange) {
			return null;
		}

		@Override
		public boolean exposeModelRange(IRegion modelRange) {
			return false;
		}

		@Override
		public int widgetLine2ModelLine(int widgetLine) {
			return widgetLine + 42;
		}

		@Override
		public int modelLine2WidgetLine(int widgetLine) {
			return widgetLine - 42;
		}

	}

}
