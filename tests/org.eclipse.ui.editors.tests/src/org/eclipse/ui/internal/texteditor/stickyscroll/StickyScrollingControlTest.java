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
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;

import org.eclipse.ui.editors.tests.TestUtil;

public class StickyScrollingControlTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private Color lineNumberColor;
	private Color hoverColor;
	private Color backgroundColor;
	private Color separatorColor;
	private StickyScrollingControl stickyScrollingControl;
	private IVerticalRuler ruler;
	private StickyScrollingControlSettings settings;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		shell.setSize(200, 200);
		shell.setLayout(new FillLayout());
		ruler = mock(IVerticalRuler.class);
		sourceViewer = new SourceViewer(shell, ruler, SWT.V_SCROLL | SWT.H_SCROLL);
		sourceViewer.setDocument(new Document());
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);

		lineNumberColor = new Color(0, 0, 0);
		hoverColor = new Color(1, 1, 1);
		backgroundColor = new Color(2, 2, 2);
		separatorColor = new Color(3, 3, 3);
		settings = new StickyScrollingControlSettings(2, lineNumberColor, hoverColor, backgroundColor, separatorColor,
				true);
		stickyScrollingControl = new StickyScrollingControl(sourceViewer, ruler, settings, null);
	}

	@After
	public void teardown() {
		shell.dispose();
		stickyScrollingControl.dispose();
		lineNumberColor.dispose();
		hoverColor.dispose();
		backgroundColor.dispose();
		TestUtil.cleanUp();
	}

	@Test
	public void testShowStickyLineTexts() {
		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineNumber = getStickyLineNumber();
		String expLineNumber = "10" + System.lineSeparator() + "20";
		assertEquals(expLineNumber, stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10" + System.lineSeparator() + "line 20";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testCorrectColorsApplied() {
		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals(lineNumberColor, stickyLineNumber.getStyleRangeAtOffset(0).foreground);
		assertEquals(backgroundColor, stickyLineNumber.getBackground());

		StyledText stickyLineText = getStickyLineText();
		assertEquals(backgroundColor, stickyLineText.getBackground());

		Composite stickyLineSeparator = getStickyLineSeparator();
		assertEquals(separatorColor, stickyLineSeparator.getBackground());
	}

	@Test
	public void testLimitStickyLinesCount() {
		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		settings = new StickyScrollingControlSettings(1, lineNumberColor, hoverColor, backgroundColor, separatorColor,
				true);
		stickyScrollingControl.applySettings(settings);

		StyledText stickyLineNumber = getStickyLineNumber();
		String expLineNumber = "10";
		assertEquals(expLineNumber, stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testCopyStyleRanges() {
		StyleRange styleRangeLine1 = new StyleRange(0, 1, lineNumberColor, backgroundColor);
		StyleRange styleRangeLine2 = new StyleRange(0, 2, hoverColor, separatorColor);
		List<IStickyLine> stickyLines = List.of(//
				new StickyLineStub("line 1", 0, new StyleRange[] { styleRangeLine1 }),
				new StickyLineStub("line 2", 0, new StyleRange[] { styleRangeLine2 }));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineText = getStickyLineText();

		StyleRange[] styleRanges = stickyLineText.getStyleRanges();
		assertEquals(2, styleRanges.length);
		assertEquals(0, styleRanges[0].start);
		assertEquals(1, styleRanges[0].length);
		assertEquals(lineNumberColor, styleRanges[0].foreground);
		assertEquals(backgroundColor, styleRanges[0].background);
		int startRangeLine2 = stickyLines.get(0).getText().length() + System.lineSeparator().length();
		assertEquals(startRangeLine2, styleRanges[1].start);
		assertEquals(2, styleRanges[1].length);
		assertEquals(hoverColor, styleRanges[1].foreground);
		assertEquals(separatorColor, styleRanges[1].background);
	}

	@Test
	public void testCopyStyleRangesWithLimitedStickyLines() {
		settings = new StickyScrollingControlSettings(1, lineNumberColor, hoverColor, backgroundColor, separatorColor,
				true);
		stickyScrollingControl.applySettings(settings);

		StyleRange styleRangeLine1 = new StyleRange(0, 1, lineNumberColor, backgroundColor);
		StyleRange styleRangeLine2 = new StyleRange(0, 2, hoverColor, separatorColor);
		List<IStickyLine> stickyLines = List.of(//
				new StickyLineStub("line 1", 0, new StyleRange[] { styleRangeLine1 }),
				new StickyLineStub("line 2", 0, new StyleRange[] { styleRangeLine2 }));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineText = getStickyLineText();

		StyleRange[] styleRanges = stickyLineText.getStyleRanges();
		assertEquals(1, styleRanges.length);
		assertEquals(0, styleRanges[0].start);
		assertEquals(1, styleRanges[0].length);
		assertEquals(lineNumberColor, styleRanges[0].foreground);
		assertEquals(backgroundColor, styleRanges[0].background);
	}

	@Test
	public void testWithoutVerticalRuler() {
		SourceViewer sourceViewerWithoutRuler = new SourceViewer(shell, null, SWT.None);
		StickyScrollingControl stickyScrollingControlWithoutRuler = new StickyScrollingControl(sourceViewerWithoutRuler,
				new StickyScrollingControlSettings(5, lineNumberColor, hoverColor, backgroundColor, separatorColor,
						true));
		try {
			StyledText stickyLineNumber = getStickyLineNumber();
			assertFalse(stickyLineNumber.isVisible());
		} finally {
			sourceViewerWithoutRuler.getControl().dispose();
			stickyScrollingControlWithoutRuler.dispose();
		}
	}

	@Test
	public void testWithoutLineNumber() {
		when(ruler.getWidth()).thenReturn(20);
		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertThat(stickyLineNumber.getLeftMargin(), greaterThan(0));

		settings = new StickyScrollingControlSettings(5, lineNumberColor, hoverColor, backgroundColor, separatorColor,
				false);
		stickyScrollingControl.applySettings(settings);

		stickyLineNumber = getStickyLineNumber();
		assertEquals(0, stickyLineNumber.getLeftMargin());
	}

	@Test
	public void testStyling() {
		Font font = new Font(shell.getDisplay(), new FontData("Arial", 12, SWT.BOLD));
		sourceViewer.getTextWidget().setLineSpacing(10);
		sourceViewer.getTextWidget().setFont(font);
		sourceViewer.getTextWidget().setForeground(hoverColor);

		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 1", 0));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals(10, stickyLineNumber.getLineSpacing());
		assertEquals(font, stickyLineNumber.getFont());

		StyledText stickyLineText = getStickyLineText();
		assertEquals(10, stickyLineText.getLineSpacing());
		assertEquals(font, stickyLineText.getFont());
		assertEquals(hoverColor, stickyLineText.getForeground());
	}

	@Test
	public void testLayoutStickyLinesCanvasOnResize() {
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);

		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 1", 0));
		stickyScrollingControl.setStickyLines(stickyLines);

		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		Rectangle boundsBeforeResize = stickyControlCanvas.getBounds();
		assertEquals(0, boundsBeforeResize.x);
		assertEquals(0, boundsBeforeResize.y);
		assertThat(boundsBeforeResize.width, greaterThan(0));
		assertThat(boundsBeforeResize.height, greaterThan(0));

		sourceViewer.getTextWidget().setBounds(0, 0, 150, 200);

		stickyControlCanvas = getStickyControlCanvas(shell);
		Rectangle boundsAfterResize = stickyControlCanvas.getBounds();
		assertEquals(0, boundsAfterResize.x);
		assertEquals(0, boundsAfterResize.y);
		assertThat(boundsAfterResize.width, greaterThan(0));
		assertThat(boundsBeforeResize.width, greaterThan(boundsAfterResize.width));
		assertEquals(boundsAfterResize.height, boundsBeforeResize.height);
	}

	@Test
	public void testDontLayoutOutDatedStickyLinesOnResize() {
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);

		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 1", 1));
		stickyScrollingControl.setStickyLines(stickyLines);

		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		Rectangle boundsBeforeResize = stickyControlCanvas.getBounds();

		sourceViewer.getTextWidget().setBounds(0, 0, 150, 200);

		stickyControlCanvas = getStickyControlCanvas(shell);
		Rectangle boundsAfterResize = stickyControlCanvas.getBounds();

		// No IllegalArgumentException: Index out of bounds
		assertEquals(boundsAfterResize, boundsBeforeResize);
	}

	@Test
	public void testNavigateToStickyLine() {
		String text = """
				line 1
				line 2""";
		sourceViewer.setInput(new Document(text));
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);

		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 2", 1));
		stickyScrollingControl.setStickyLines(stickyLines);

		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		stickyControlCanvas.notifyListeners(SWT.MouseDown, new Event());

		Point selectedRange = sourceViewer.getSelectedRange();
		assertEquals(7, selectedRange.x);
		assertEquals(0, selectedRange.y);
	}

	@Test
	public void testVerticalScrollingIsDispatched() {
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 0);
		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		String text = """
				line 1
				line 2""";
		sourceViewer.setInput(new Document(text));
		sourceViewer.getTextWidget().getVerticalBar().setIncrement(10);
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());

		Event event = new Event();
		event.count = -1;
		stickyControlCanvas.notifyListeners(SWT.MouseVerticalWheel, event);

		assertEquals(10, sourceViewer.getTextWidget().getTopPixel());
	}

	@Test
	public void testHorizontalScrollingIsDispatched() {
		sourceViewer.getTextWidget().setBounds(0, 0, 0, 200);
		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		String text = """
				line 1
				line 2""";
		sourceViewer.setInput(new Document(text));
		sourceViewer.getTextWidget().getHorizontalBar().setIncrement(10);
		assertEquals(0, sourceViewer.getTextWidget().getHorizontalPixel());

		Event event = new Event();
		event.count = -1;
		stickyControlCanvas.notifyListeners(SWT.MouseHorizontalWheel, event);

		assertEquals(10, sourceViewer.getTextWidget().getHorizontalPixel());
	}

	@Test
	public void testLimitStickyLinesToTextWidgetHeight() {
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);
		sourceViewer.getTextWidget().setText("line1\nline2");
		List<IStickyLine> stickyLines = List.of(new StickyLineStub("line 2", 1));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineText = getStickyLineText();
		assertEquals("line 2", stickyLineText.getText());

		sourceViewer.getTextWidget().setBounds(0, 0, 200, 20);
		stickyLineText = getStickyLineText();
		assertEquals("", stickyLineText.getText());
	}

	@Test
	public void testListenForCaretAfterKeyDown() {
		// set height to 10 so that scrolling is needed
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 10);
		String text = """
				line 1
				line 2
				line 3
				line 4
				line 5
				line 6
				line 7
				line 8
				line 9""";
		sourceViewer.setInput(new Document(text));
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());

		sourceViewer.getTextWidget().notifyListeners(SWT.KeyDown, new Event());
		sourceViewer.getTextWidget().setCaretOffset(42);

		drainDisplayEventQueue();
		assertThat(sourceViewer.getTextWidget().getTopPixel(), greaterThan(0));
	}

	@Test
	public void testDontListenForCaretWithoutKeyDown() {
		// set height to 10 so that scrolling is needed
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 10);
		String text = """
				line 1
				line 2
				line 3
				line 4
				line 5
				line 6
				line 7
				line 8
				line 9""";
		sourceViewer.setInput(new Document(text));
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());

		sourceViewer.getTextWidget().setCaretOffset(42);

		drainDisplayEventQueue();
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());
	}

	@Test
	public void testDontScrollOnCaretAtDocumentEnd() {
		// set height to 10 so that scrolling is needed
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 10);
		String text = """
				line 1
				line 2
				line 3
				line 4
				line 5
				line 6
				line 7
				line 8
				line 9""";
		sourceViewer.setInput(new Document(text));
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());

		sourceViewer.getTextWidget().notifyListeners(SWT.KeyDown, new Event());
		sourceViewer.getTextWidget().setCaretOffset(62);

		drainDisplayEventQueue();
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());
	}

	@Test
	public void testDontScrollOnCaretWhenDocumentChangedBeforeExecution() {
		// set height to 10 so that scrolling is needed
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 10);
		String text = """
				line 1
				line 2
				line 3
				line 4
				line 5
				line 6
				line 7
				line 8
				line 9""";
		sourceViewer.setInput(new Document(text));
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());
		sourceViewer.getTextWidget().notifyListeners(SWT.KeyDown, new Event());
		sourceViewer.getTextWidget().setCaretOffset(62);

		// change document before event queue is drained
		sourceViewer.setInput(new Document("line"));

		drainDisplayEventQueue();
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());
	}

	private Canvas getStickyControlCanvas(Composite composite) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Canvas canvas) {
				if (canvas.getChildren().length == 3 && canvas.getChildren()[0] instanceof StyledText
						&& canvas.getChildren()[1] instanceof StyledText
						&& canvas.getChildren()[2] instanceof Composite) {
					return canvas;
				}
			}
			if (control instanceof Composite childComposite) {
				return getStickyControlCanvas(childComposite);
			}
		}
		return null;
	}

	private StyledText getStickyLineNumber() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[0];
	}

	private StyledText getStickyLineText() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[1];
	}

	private Composite getStickyLineSeparator() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (Composite) canvas.getChildren()[2];
	}

	private void drainDisplayEventQueue() {
		while (Display.getDefault().readAndDispatch()) {
		}
	}

	private class StickyLineStub implements IStickyLine {

		private final String text;
		private final int lineNumber;
		private StyleRange[] styleRanges;

		public StickyLineStub(String text, int lineNumber) {
			this(text, lineNumber, null);
		}

		public StickyLineStub(String text, int lineNumber, StyleRange[] styleRanges) {
			this.text = text;
			this.lineNumber = lineNumber;
			this.styleRanges = styleRanges;
		}

		@Override
		public int getLineNumber() {
			return lineNumber;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public StyleRange[] getStyleRanges() {
			return styleRanges;
		}
	}
}
