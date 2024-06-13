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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;

public class StickyScrollingControlTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private Color lineNumberColor;
	private Color hoverColor;
	private Color backgroundColor;
	private StickyScrollingControl stickyScrollingControl;
	private CompositeRuler ruler;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		shell.setSize(200, 200);
		shell.setLayout(new FillLayout());
		ruler = new CompositeRuler();
		sourceViewer = new SourceViewer(shell, ruler, SWT.V_SCROLL | SWT.H_SCROLL);

		lineNumberColor = new Color(0, 0, 0);
		hoverColor = new Color(1, 1, 1);
		backgroundColor = new Color(2, 2, 2);
		StickyScrollingControlSettings settings = new StickyScrollingControlSettings(5, lineNumberColor, hoverColor,
				backgroundColor, true);
		stickyScrollingControl = new StickyScrollingControl(sourceViewer, ruler, settings, null);
	}

	@After
	public void teardown() {
		shell.dispose();
		stickyScrollingControl.dispose();
		lineNumberColor.dispose();
		hoverColor.dispose();
		backgroundColor.dispose();
	}

	@Test
	public void testShowStickyLineTexts() {
		List<StickyLine> stickyLines = List.of(new StickyLine("line 10", 9), new StickyLine("line 20", 19));
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
		List<StickyLine> stickyLines = List.of(new StickyLine("line 10", 9), new StickyLine("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertEquals(lineNumberColor, stickyLineNumber.getStyleRangeAtOffset(0).foreground);
		assertEquals(backgroundColor, stickyLineNumber.getBackground());

		StyledText stickyLineText = getStickyLineText();
		assertEquals(backgroundColor, stickyLineText.getBackground());
	}

	@Test
	public void testLimitStickyLinesCount() {
		List<StickyLine> stickyLines = List.of(new StickyLine("line 10", 9), new StickyLine("line 20", 19));
		stickyScrollingControl.setStickyLines(stickyLines);

		StickyScrollingControlSettings settings = new StickyScrollingControlSettings(1, lineNumberColor, hoverColor,
				backgroundColor, true);
		stickyScrollingControl.applySettings(settings);

		StyledText stickyLineNumber = getStickyLineNumber();
		String expLineNumber = """
				10""";
		assertEquals(expLineNumber, stickyLineNumber.getText());
		StyledText stickyLineText = getStickyLineText();
		String expStickyLineText = """
				line 10""";
		assertEquals(expStickyLineText, stickyLineText.getText());
	}

	@Test
	public void testCopyStyleRanges() {
		sourceViewer.getTextWidget().setText("line 1");
		sourceViewer.getTextWidget().setStyleRange(new StyleRange(0, 6, lineNumberColor, backgroundColor));

		List<StickyLine> stickyLines = List.of(new StickyLine("line 1", 0));
		stickyScrollingControl.setStickyLines(stickyLines);

		StyledText stickyLineText = getStickyLineText();
		assertEquals(lineNumberColor, stickyLineText.getStyleRangeAtOffset(0).foreground);
		assertEquals(backgroundColor, stickyLineText.getStyleRangeAtOffset(0).background);
	}

	@Test
	public void testWithoutVeticalRuler() {
		sourceViewer = new SourceViewer(shell, null, SWT.None);
		StickyScrollingControlSettings settings = new StickyScrollingControlSettings(5, lineNumberColor, hoverColor,
				backgroundColor, true);
		stickyScrollingControl = new StickyScrollingControl(sourceViewer, settings);

		StyledText stickyLineNumber = getStickyLineNumber();
		assertFalse(stickyLineNumber.isVisible());
	}

	@Test
	public void testStyling() {
		Font font = new Font(shell.getDisplay(), new FontData("Arial", 12, SWT.BOLD));
		sourceViewer.getTextWidget().setLineSpacing(10);
		sourceViewer.getTextWidget().setFont(font);
		sourceViewer.getTextWidget().setForeground(hoverColor);

		List<StickyLine> stickyLines = List.of(new StickyLine("line 1", 0));
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

		List<StickyLine> stickyLines = List.of(new StickyLine("line 1", 0));
		stickyScrollingControl.setStickyLines(stickyLines);

		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		assertEquals(0, stickyControlCanvas.getBounds().x);
		assertEquals(0, stickyControlCanvas.getBounds().y);
		assertEquals(getExpectedWitdh(200), stickyControlCanvas.getBounds().width);
		assertThat(stickyControlCanvas.getBounds().height, greaterThan(0));

		sourceViewer.getTextWidget().setBounds(0, 0, 150, 200);

		stickyControlCanvas = getStickyControlCanvas(shell);
		assertEquals(0, stickyControlCanvas.getBounds().x);
		assertEquals(0, stickyControlCanvas.getBounds().y);
		assertEquals(getExpectedWitdh(150), stickyControlCanvas.getBounds().width);
		assertThat(stickyControlCanvas.getBounds().height, greaterThan(0));
	}

	@Test
	public void testNavigateToStickyLine() {
		String text = """
				line 1
				line 2""";
		sourceViewer.getTextWidget().setText(text);
		sourceViewer.getTextWidget().setBounds(0, 0, 200, 200);
		sourceViewer.setDocument(new Document(text));

		List<StickyLine> stickyLines = List.of(new StickyLine("line 2", 1));
		stickyScrollingControl.setStickyLines(stickyLines);

		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		stickyControlCanvas.notifyListeners(SWT.MouseDown, new Event());

		Point selectedRange = sourceViewer.getSelectedRange();
		assertEquals(7, selectedRange.x);
		assertEquals(0, selectedRange.y);
	}

	@Test
	public void testVerticalScrollingIsDispatched() {
		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		String text = """
				line 1
				line 2""";
		sourceViewer.getTextWidget().setText(text);
		sourceViewer.getTextWidget().getVerticalBar().setIncrement(10);
		assertEquals(0, sourceViewer.getTextWidget().getTopPixel());

		Event event = new Event();
		event.count = -1;
		stickyControlCanvas.notifyListeners(SWT.MouseVerticalWheel, event);

		assertEquals(10, sourceViewer.getTextWidget().getTopPixel());
	}

	@Test
	public void testHorizontalScrollingIsDispatched() {
		Canvas stickyControlCanvas = getStickyControlCanvas(shell);
		String text = """
				line 1
				line 2""";
		sourceViewer.getTextWidget().setText(text);
		sourceViewer.getTextWidget().getHorizontalBar().setIncrement(10);
		assertEquals(0, sourceViewer.getTextWidget().getHorizontalPixel());

		Event event = new Event();
		event.count = -1;
		stickyControlCanvas.notifyListeners(SWT.MouseHorizontalWheel, event);

		assertEquals(10, sourceViewer.getTextWidget().getHorizontalPixel());
	}

	private Canvas getStickyControlCanvas(Composite composite) {
		for (Control control : composite.getChildren()) {
			if (control instanceof Canvas canvas) {
				if (canvas.getChildren().length == 4) {
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

	private int getExpectedWitdh(int textWidgetWitdth) {
		ScrollBar verticalBar = sourceViewer.getTextWidget().getVerticalBar();
		if (verticalBar.isVisible()) {
			return textWidgetWitdth - verticalBar.getSize().x + 1;
		} else {
			return textWidgetWitdth + 1;
		}
	}

}
