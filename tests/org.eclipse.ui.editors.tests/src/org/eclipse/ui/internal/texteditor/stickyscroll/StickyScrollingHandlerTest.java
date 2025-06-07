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

import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.StringJoiner;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ui.texteditor.stickyscroll.IStickyLine;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider;
import org.eclipse.ui.texteditor.stickyscroll.IStickyLinesProvider.StickyLinesProperties;

import org.eclipse.ui.editors.tests.TestUtil;

public class StickyScrollingHandlerTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private Color lineNumberColor;
	private Color hoverColor;
	private CompositeRuler ruler;
	private IPreferenceStore store;
	private IStickyLinesProvider linesProvider;
	private StickyScrollingHandler stickyScrollingHandler;
	private StickyLinesProperties stickyLinesProperties;
	private StyledText textWidget;
	private IEditorPart editorPart;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		shell.setBounds(0, 0, 200, 80);
		ruler = new CompositeRuler();
		sourceViewer = new SourceViewer(shell, ruler, SWT.None);
		sourceViewer.setDocument(new Document());
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 100);
		textWidget = sourceViewer.getTextWidget();
		textWidget.setText("first 1 \nline 2 \nline 3 \nline 4 \nline 5 \nline 6 \nline 7 \nline 8 \nline 9 \nline 10");
		textWidget.setTopIndex(1);
		editorPart = mock(IEditorPart.class);

		lineNumberColor = new Color(0, 0, 0);
		hoverColor = new Color(1, 1, 1);

		store = createPreferenceStore();
		linesProvider = mock(IStickyLinesProvider.class);

		stickyScrollingHandler = new StickyScrollingHandler(sourceViewer, ruler, store, linesProvider, editorPart);
		stickyLinesProperties = new StickyLinesProperties(4, editorPart);
	}

	@After
	public void teardown() {
		shell.dispose();
		TestUtil.cleanUp();
	}

	@Test
	public void testShowStickyLines() {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineNumber = getStickyLineNumber();
		String expLineNumber = "10";
		assertEquals(expLineNumber, stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testDontCalculateStickyLinesForFirstLine() {
		textWidget.setTopIndex(0);

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals("", stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		assertEquals("", stickyLineText.getText());
		verify(linesProvider, never()).getStickyLines(any(), anyInt(), any());
	}

	@Test
	public void testUnistallStickyLines() {
		Canvas stickyControlCanvas = getStickyControlCanvas(this.shell);

		stickyScrollingHandler.uninstall();

		assertTrue(stickyControlCanvas.isDisposed());
	}

	@Test
	public void testPreferencesLoaded() {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals(lineNumberColor, stickyLineNumber.getStyleRangeAtOffset(0).foreground);
	}

	@Test
	public void testPreferencesUpdated() {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19)));
		when(linesProvider.getStickyLines(sourceViewer, 2, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9), new StickyLineStub("line 20", 19)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 10" + System.lineSeparator() + "line 20";
		assertEquals(expStickyLineText, stickyLineText.getText());

		// change maximum count of sticky lines to 1
		store.setValue(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT, 1);

		expStickyLineText = "line 10";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testThrottledExecution() throws InterruptedException {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 10", 9)));

		stickyScrollingHandler.viewportChanged(100);
		Thread.sleep(10);
		stickyScrollingHandler.viewportChanged(200);
		Thread.sleep(10);
		stickyScrollingHandler.viewportChanged(300);
		Thread.sleep(10);
		stickyScrollingHandler.viewportChanged(400);

		waitInUi(300);

		// Call to lines provider should be throttled, at least one and at most
		// 3 calls expected
		verify(linesProvider, atMost(3)).getStickyLines(sourceViewer, 1, stickyLinesProperties);
		verify(linesProvider, atLeastOnce()).getStickyLines(sourceViewer, 1, stickyLinesProperties);
	}

	@Test
	public void testRemoveStickyLines() {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 1", 0), new StickyLineStub("line 2", 1)));
		when(linesProvider.getStickyLines(sourceViewer, 2, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 3", 2)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 1";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testLineUnderStickyLine() {
		when(linesProvider.getStickyLines(sourceViewer, 1, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 1", 0)));
		when(linesProvider.getStickyLines(sourceViewer, 2, stickyLinesProperties))
				.thenReturn(List.of(new StickyLineStub("line 1", 0), new StickyLineStub("line 2", 1)));

		stickyScrollingHandler.viewportChanged(100);

		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = "line 1" + System.lineSeparator() + "line 2";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	private void waitInUi(int ms) throws InterruptedException {
		while (shell.getDisplay().readAndDispatch()) {
		}
		Thread.sleep(ms);
		while (shell.getDisplay().readAndDispatch()) {
		}
	}

	private IPreferenceStore createPreferenceStore() {
		store = new PreferenceStore();
		store.setValue(EDITOR_TAB_WIDTH, 4);
		store.setValue(EDITOR_STICKY_SCROLLING_MAXIMUM_COUNT, 4);
		store.setValue(EDITOR_LINE_NUMBER_RULER_COLOR, colorToString(lineNumberColor));
		store.setValue(EDITOR_CURRENT_LINE_COLOR, colorToString(hoverColor));
		store.setValue(EDITOR_LINE_NUMBER_RULER, true);
		return store;
	}

	private StyledText getStickyLineNumber() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[0];
	}

	private StyledText getStickyLineText() {
		Canvas canvas = getStickyControlCanvas(shell);
		return (StyledText) canvas.getChildren()[1];
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

	private String colorToString(Color color) {
		StringJoiner joiner = new StringJoiner(",");
		joiner.add(String.valueOf(color.getRed()));
		joiner.add(String.valueOf(color.getGreen()));
		joiner.add(String.valueOf(color.getBlue()));
		return joiner.toString();
	}

	private class StickyLineStub implements IStickyLine {

		private final String text;
		private final int lineNumber;

		public StickyLineStub(String text, int lineNumber) {
			this.text = text;
			this.lineNumber = lineNumber;
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
			return null;
		}
	}

}
