/*******************************************************************************
 * Copyright (c) 2025 SAP SE
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class CodeMiningLineHeaderAnnotationTest {

	private SourceViewer fViewer;

	private Shell fShell;

	private Document document;

	@BeforeEach
	public void setUp() {
		fShell= new Shell(Display.getDefault());
		fShell.setSize(500, 200);
		fShell.setLayout(new FillLayout());
		fViewer= new SourceViewer(fShell, null, SWT.NONE);
		final StyledText textWidget= fViewer.getTextWidget();
		document= new Document("a");
		textWidget.setText(document.get());
		fViewer.setDocument(document, new AnnotationModel());
		final Display display= textWidget.getDisplay();
		fShell.open();
		Assertions.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().isVisible();
			}
		}.waitForCondition(display, 3000));
		DisplayHelper.sleep(textWidget.getDisplay(), 1000);
	}

	@AfterEach
	public void tearDown() {
		fViewer= null;
	}

	@Test
	public void testDrawTakesLineSpacingForSingleLineIntoAccount() throws Exception {
		assertDrawTakesLineSpacingIntoAccount("line1\nline2\nline3", "code mining single line");
	}

	@Test
	public void testDrawTakesLineSpacingForMultiLineIntoAccount() throws Exception {
		assertDrawTakesLineSpacingIntoAccount("line1\nline2\nline3", "code mining line1\ncode mining line2");
	}

	private void assertDrawTakesLineSpacingIntoAccount(String source, String codeMiningLabel) throws Exception {
		var doc= fViewer.getDocument();
		doc.set(source);
		var textWidget= fViewer.getTextWidget();
		textWidget.setLineSpacing(10);
		var mining= new LineHeaderCodeMining(0, doc, null, null) {
			@Override
			public String getLabel() {
				return codeMiningLabel;
			}
		};
		var gc= new GC(textWidget);
		try {
			Point result= mining.draw(gc, textWidget, null, 0, 0);
			String[] codeMiningLabelsByLine= codeMiningLabel.split("\n");
			assertEquals(codeMiningLabelsByLine.length * (gc.stringExtent(codeMiningLabelsByLine[0]).y + textWidget.getLineSpacing()), result.y);
		} finally {
			gc.dispose();
		}
	}

	@Test
	public void testGetHeightDoesNotReturnZero() throws Exception {
		var cut= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer);
		var s= new InlinedAnnotationSupport();
		s.install(fViewer, new AnnotationPainter(fViewer, null));
		var m= AbstractInlinedAnnotation.class.getDeclaredMethod("setSupport", InlinedAnnotationSupport.class);
		m.setAccessible(true);
		m.invoke(cut, s);
		cut.update(Arrays.asList(new LineHeaderCodeMining(0, document, null) {
			@Override
			public String getLabel() {
				return "mining";
			}
		}), null);
		// https: //github.com/eclipse-platform/eclipse.platform.ui/issues/2786
		assertNotEquals(0, cut.getHeight()); // getHeight should not return 0, otherwise editor content starts jumping around
	}

	/**
	 * Verifies that a multi-line line-header code mining contributes a height which is an exact
	 * multiple of the StyledText's regular line height (plus line spacing). Otherwise the line
	 * drawn below the code mining would no longer be vertically aligned with the regular text
	 * lines. This was visible on Windows with Consolas at odd point sizes (e.g. 9, 11, 13), where
	 * {@code gc.stringExtent(line).y} returned a different value than
	 * {@link StyledText#getLineHeight()}, so the lines below a multiline code mining were shifted
	 * by a few pixels relative to the regular grid.
	 */
	@Test
	public void testTwoLineCodeMiningHeightMatchesTextWidgetLineHeight() throws Exception {
		var doc= fViewer.getDocument();
		doc.set("line0\nline1\nline2");
		var textWidget= fViewer.getTextWidget();
		// A line spacing != 0 makes a regression in either factor immediately visible.
		textWidget.setLineSpacing(3);
		// Try to use Consolas at an odd size on Windows since this is the configuration in which
		// the original bug was reported. On other platforms / when Consolas is not available the
		// test still runs with the default font - the assertions below must hold for any font.
		Font consolasFont= tryCreateConsolasFont(textWidget, 11);
		try {
			if (consolasFont != null) {
				textWidget.setFont(consolasFont);
			}
			String codeMiningLabel= "code mining line1\ncode mining line2";
			var mining= new LineHeaderCodeMining(0, doc, null, null) {
				@Override
				public String getLabel() {
					return codeMiningLabel;
				}
			};
			int normalLineHeight= textWidget.getLineHeight();
			int lineSpacing= textWidget.getLineSpacing();
			int numMiningLines= codeMiningLabel.split("\n").length;
			int expectedMiningHeight= numMiningLines * (normalLineHeight + lineSpacing);

			// 1) The size returned by LineHeaderCodeMining.draw() must be a multiple of the
			// StyledText's regular line height + line spacing - it must not depend on
			// gc.stringExtent(...).y, which on Consolas/odd sizes differs from getLineHeight().
			var gc= new GC(textWidget);
			try {
				Point result= mining.draw(gc, textWidget, null, 0, 0);
				assertEquals(expectedMiningHeight, result.y,
						"two-line code mining height must be 2 * (textWidget.getLineHeight() + lineSpacing)");
			} finally {
				gc.dispose();
			}

			// 2) CodeMiningLineHeaderAnnotation.getHeight(GC) drives the line vertical indent of the
			// line below the mining (see InlinedAnnotationDrawingStrategy). Its result must equal
			// expectedMiningHeight - meaning the first regular text line below the mining ends up
			// at exactly that pixel offset above its un-indented position, on the same vertical
			// grid as all other text lines.
			var annotation= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer);
			var support= new InlinedAnnotationSupport();
			support.install(fViewer, new AnnotationPainter(fViewer, null));
			var setSupport= AbstractInlinedAnnotation.class.getDeclaredMethod("setSupport", InlinedAnnotationSupport.class);
			setSupport.setAccessible(true);
			setSupport.invoke(annotation, support);
			annotation.update(List.of(mining), null);

			GC gc2= new GC(textWidget);
			try {
				int multilineHeight= annotation.getHeight(gc2);
				assertEquals(expectedMiningHeight, multilineHeight,
						"multiline mining height must be a whole-line multiple of the StyledText line height");
				// The pixel position of the line directly below the code mining is
				// (multilineHeight) above its un-indented y position, and that offset must be a
				// whole number of regular text line heights so the lines below the mining stay
				// aligned with the regular grid.
				assertEquals(0, multilineHeight % (normalLineHeight + lineSpacing),
						"line below the code mining must land on a regular text-line boundary");
			} finally {
				gc2.dispose();
			}
		} finally {
			if (consolasFont != null) {
				textWidget.setFont(null);
				consolasFont.dispose();
			}
		}
	}

	private static Font tryCreateConsolasFont(StyledText textWidget, int height) {
		FontData[] available= textWidget.getDisplay().getFontList("Consolas", true); //$NON-NLS-1$
		if (available == null || available.length == 0) {
			return null;
		}
		return new Font(textWidget.getDisplay(), "Consolas", height, SWT.NORMAL); //$NON-NLS-1$
	}
}
