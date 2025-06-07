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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider.StickyLinesProperties;

import org.eclipse.ui.editors.tests.TestUtil;

public class DefaultStickyLinesProviderTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private DefaultStickyLinesProvider stickyLinesProvider;
	private StyledText textWidget;
	private StickyLinesProperties stickyLinesProperties;
	private IEditorPart editorPart;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		sourceViewer = new SourceViewer(shell, null, SWT.None);
		sourceViewer.setDocument(new Document());
		stickyLinesProvider = new DefaultStickyLinesProvider();
		textWidget = sourceViewer.getTextWidget();
		editorPart = mock(IEditorPart.class);
		stickyLinesProperties = new StickyLinesProperties(4, editorPart);
	}

	@After
	public void teardown() {
		TestUtil.cleanUp();
	}

	@Test
	public void testEmptySourceCode() {
		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 0, stickyLinesProperties);

		assertThat(stickyLines, is(empty()));
	}

	@Test
	public void testSingleStickyLine() {
		String text = """
				line 1
				 line 2<""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties);

		assertEquals(1, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
		assertEquals("line 1", stickyLines.get(0).getText());
	}

	@Test
	public void testLineUnderStickyLine() {
		String text = """
				line 1
				 line 2<
				  line 3
				  line 4""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties);

		assertEquals(1, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
	}

	@Test
	public void testNewStickyRoot() {
		String text = """
				line 1
				 line 2
				line 3
				 line 4<""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 3, stickyLinesProperties);

		assertEquals(1, stickyLines.size());
		assertEquals(2, stickyLines.get(0).getLineNumber());
	}

	@Test
	public void testIgnoreEmptyLines() {
		String text = """
				line 1

				 line 2

				  line 3<""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 4, stickyLinesProperties);

		assertEquals(2, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
		assertEquals(2, stickyLines.get(1).getLineNumber());
	}

	@Test
	public void testLinesWithTabs() {
		stickyLinesProperties = new StickyLinesProperties(2, editorPart);
		String text = """
				line 1
				\tline 2
				\t\tline 3<""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 2, stickyLinesProperties);

		assertEquals(2, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
		assertEquals(1, stickyLines.get(1).getLineNumber());
	}

	@Test
	public void testStartAtEmptyLineWithNext() {
		String text = """
				line 1

				 line 2

				  line 3""";
		textWidget.setText(text);
		textWidget.setTopIndex(3);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 3, stickyLinesProperties);

		assertEquals(2, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
		assertEquals(2, stickyLines.get(1).getLineNumber());
	}

	@Test
	public void testStartAtEmptyLineWithPrevious() {
		String text = """
				line 1
				 line 2
				  line 3

				line 4""";
		textWidget.setText(text);

		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 3, stickyLinesProperties);

		assertEquals(2, stickyLines.size());
		assertEquals(0, stickyLines.get(0).getLineNumber());
		assertEquals(1, stickyLines.get(1).getLineNumber());
	}

	@Test
	public void testStickyLineWithSourceViewerLineMapping() {
		sourceViewer = new SourceViewerWithLineMapping(shell, null, SWT.None);
		sourceViewer.setDocument(new Document());
		textWidget = sourceViewer.getTextWidget();

		String text = """
				line 1
				 line 2<""";
		textWidget.setText(text);

		// Source viewer line 43 that is mapped to line 1 in the text widget
		List<IStickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, 1 + 42, stickyLinesProperties);

		assertEquals(1, stickyLines.size());
		// Source viewer line 42 that is mapped to line 0 in the text widget
		assertEquals(0 + 42, stickyLines.get(0).getLineNumber());
		assertEquals("line 1", stickyLines.get(0).getText());
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
