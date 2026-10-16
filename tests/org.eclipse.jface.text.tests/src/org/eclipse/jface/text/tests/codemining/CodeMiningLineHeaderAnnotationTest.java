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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import org.eclipse.jface.internal.text.codemining.CodeMiningLineHeaderAnnotation;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.IPlaceholderCodeMining;
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

	@ValueSource(booleans= { false, true })
	@ParameterizedTest(name= "[{index}] placeHolder: {0}")
	public void testGetHeightDoesNotReturnZero(boolean placeholder) throws Exception {
		var cut= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer);
		var s= new InlinedAnnotationSupport();
		s.install(fViewer, new AnnotationPainter(fViewer, null));
		var m= AbstractInlinedAnnotation.class.getDeclaredMethod("setSupport", InlinedAnnotationSupport.class);
		m.setAccessible(true);
		m.invoke(cut, s);

		ICodeMining mining;

		if (placeholder) {
			mining= unresolvedPlaceholder("Placeholder");
		} else {
			mining= codeMining("mining");
		}

		cut.update(List.of(mining), null);
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
	@ValueSource(booleans= { false, true })
	@ParameterizedTest(name= "[{index}] placeHolder: {0}")
	public void testTwoLineCodeMiningHeightMatchesTextWidgetLineHeight(boolean placeholder) throws Exception {
		document.set("line0\nline1\nline2");
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
			ICodeMining mining;

			if (placeholder) {
				mining= unresolvedPlaceholder(codeMiningLabel);
			} else {
				mining= codeMining(codeMiningLabel);
			}

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

	/** An update must not modify a mining list a painting reader already holds. */
	@Test
	public void testUpdateDoesNotModifyMiningsSeenByReaders() throws Exception {
		var annotation= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer);
		annotation.update(List.of(codeMining("first"), codeMining("second")), null);

		List<ICodeMining> minings= annotation.getMinings();
		Iterator<ICodeMining> iterator= minings.iterator();
		assertEquals("first", iterator.next().getLabel());

		annotation.update(List.of(codeMining("third")), null);

		assertEquals("second", iterator.next().getLabel());
		assertEquals(2, minings.size());
		assertEquals(1, annotation.getMinings().size());
	}

	/** The height computation handles labels made only of line breaks and labels with \r\n or \r line ends. */
	@Test
	public void testMultilineHeightWithLineBreaksInLabels() throws Exception {
		var getMultilineHeight= CodeMiningLineHeaderAnnotation.class.getDeclaredMethod("getMultilineHeight", GC.class, List.class, StyledText.class, int.class);
		getMultilineHeight.setAccessible(true);
		List<ICodeMining> minings= List.of(codeMining("mining"), codeMining("\n"));
		var textWidget= fViewer.getTextWidget();

		int heightFromLineCount= (int) getMultilineHeight.invoke(null, null, minings, textWidget, -1);
		assertEquals(textWidget.getLineHeight() + textWidget.getLineSpacing(), heightFromLineCount);

		var gc= new GC(textWidget);
		try {
			int heightFromExtent= (int) getMultilineHeight.invoke(null, gc, minings, null, -1);
			assertEquals(gc.textExtent("mining").y, heightFromExtent);

			List<ICodeMining> crlfMinings= List.of(codeMining("mining"), codeMining("next\r\nmore"));
			int heightWithNextFirstLine= (int) getMultilineHeight.invoke(null, gc, crlfMinings, null, -1);
			assertEquals(gc.textExtent("miningnext").y + gc.textExtent("more").y, heightWithNextFirstLine);

			List<ICodeMining> crMinings= List.of(codeMining("mining"), codeMining("next\rmore"));
			int heightWithCrSeparator= (int) getMultilineHeight.invoke(null, gc, crMinings, null, -1);
			assertEquals(gc.textExtent("miningnext").y + gc.textExtent("more").y, heightWithCrSeparator);
		} finally {
			gc.dispose();
		}
	}

	@Test
	public void testUnresolvedPlaceholderRequestsRedraw() throws Exception {
		AtomicBoolean redrawRequested= new AtomicBoolean(false);
		AtomicBoolean resolveRequested= new AtomicBoolean(false);

		CodeMiningLineHeaderAnnotation annotation= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer) {
			@Override
			public void redraw() {
				redrawRequested.set(true);
				super.redraw();
			}
		};
		StyledText widget= fViewer.getTextWidget();

		CompletableFuture<String> labelDrawn= new CompletableFuture<>();
		String placeholder= "Placeholder";

		ICodeMining mining= new ResolvablePlaceholderMining(0, document, null, placeholder) {

			@Override
			public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
				labelDrawn.complete(getLabel());
				return super.draw(gc, textWidget, color, x, y);
			}

			@Override
			protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
				resolveRequested.set(true);
				return CompletableFuture.runAsync(() -> {
					setLabel("Resolved");
				});
			}
		};

		annotation.update(List.of(mining), new NullProgressMonitor());

		GC gc= new GC(widget);

		try {
			annotation.draw(gc, widget, 0, 0, null, 0, 0);
		} finally {
			gc.dispose();
		}

		assertTrue(resolveRequested.get());
		assertTrue(redrawRequested.get());
		assertEquals(placeholder, labelDrawn.resultNow());
	}

	@Test
	void testResolvedPlaceholderDoesNotRequestRedraw() throws Exception {
		AtomicBoolean redrawRequested= new AtomicBoolean(false);
		AtomicBoolean resolveRequested= new AtomicBoolean(false);

		CodeMiningLineHeaderAnnotation annotation= new CodeMiningLineHeaderAnnotation(new Position(0, 0), fViewer) {
			@Override
			public void redraw() {
				redrawRequested.set(true);
				super.redraw();
			}
		};
		StyledText widget= fViewer.getTextWidget();

		String placeholder= "Placeholder";

		ICodeMining mining= new ResolvablePlaceholderMining(0, document, null, placeholder) {
			@Override
			protected CompletableFuture<Void> doResolve(ITextViewer viewer, IProgressMonitor monitor) {
				resolveRequested.set(true);
				return CompletableFuture.runAsync(() -> {
					setLabel("Resolved");
				});
			}
		};

		mining.resolve(fViewer, new NullProgressMonitor()).get(2, TimeUnit.SECONDS);
		resolveRequested.set(false); // Reset after intentional resolve

		annotation.update(List.of(mining), new NullProgressMonitor());

		GC gc= new GC(widget);

		try {
			annotation.draw(gc, widget, 0, 0, null, 0, 0);

			assertFalse(resolveRequested.get());
			assertFalse(redrawRequested.get());
		} finally {
			gc.dispose();
		}
	}

	private LineHeaderCodeMining unresolvedPlaceholder(String label) throws BadLocationException {
		return new UnresolvedPlaceholderMining(0, document, null, label);
	}

	private LineHeaderCodeMining codeMining(String label) throws BadLocationException {
		return new LineHeaderCodeMining(0, document, null) {
			@Override
			public String getLabel() {
				return label;
			}
		};
	}

	private static Font tryCreateConsolasFont(StyledText textWidget, int height) {
		FontData[] available= textWidget.getDisplay().getFontList("Consolas", true); //$NON-NLS-1$
		if (available == null || available.length == 0) {
			return null;
		}
		return new Font(textWidget.getDisplay(), "Consolas", height, SWT.NORMAL); //$NON-NLS-1$
	}

	private static class UnresolvedPlaceholderMining extends LineHeaderCodeMining implements IPlaceholderCodeMining {

		public UnresolvedPlaceholderMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, String label) throws BadLocationException {
			super(beforeLineNumber, document, provider);
			setLabel(label);
		}

		@Override
		public boolean isResolved() {
			return false;
		}

	}

	private static class ResolvablePlaceholderMining extends LineHeaderCodeMining implements IPlaceholderCodeMining {

		public ResolvablePlaceholderMining(int beforeLineNumber, IDocument document, ICodeMiningProvider provider, String placeholder) throws BadLocationException {
			super(beforeLineNumber, document, provider);
			setLabel(placeholder);
		}

	}

}
