/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.jface.text.tests.codemining;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.util.Util;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.tests.ScreenshotOnFailureRule;
import org.eclipse.jface.text.tests.TextViewerTest;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class CodeMiningTest {

	private SourceViewer fViewer;
	private Shell fShell;

	@Rule public ScreenshotOnFailureRule rule = new ScreenshotOnFailureRule();

	@Before
	public void setUp() {
		fShell= new Shell(Display.getDefault());
		fShell.setSize(500, 200);
		fShell.setLayout(new FillLayout());
		fViewer= new SourceViewer(fShell, null, SWT.NONE);
		final StyledText textWidget= fViewer.getTextWidget();
		textWidget.setText("a");
		textWidget.setText("");
		MonoReconciler reconciler = new MonoReconciler(new IReconcilingStrategy() {
			@Override
			public void setDocument(IDocument document) {
				fViewer.updateCodeMinings();
			}

			@Override
			public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
				// nothing to do
			}

			@Override
			public void reconcile(IRegion partition) {
				fViewer.updateCodeMinings();
			}
		}, false);
		reconciler.install(fViewer);
		fViewer.setDocument(new Document(), new AnnotationModel());
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new DelayedEchoCodeMiningProvider() });
		AnnotationPainter annotationPainter = new AnnotationPainter(fViewer, null);
		fViewer.setCodeMiningAnnotationPainter(annotationPainter);
		fViewer.addPainter(annotationPainter);
		// this currently needs to be repeated
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new DelayedEchoCodeMiningProvider() });
		final Display display = textWidget.getDisplay();
		fShell.open();
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().isVisible();
			}
		}.waitForCondition(display, 3000));
		DisplayHelper.sleep(textWidget.getDisplay(), 1000);
	}

	@After
	public void tearDown() {
		fShell.dispose();
		fViewer = null;
	}

	@Test
	public void testCodeMiningFirstLine() {
		fViewer.getDocument().set("echo");
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getLineVerticalIndent(0) > 0;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
	}

	@Test
	public void testCodeMiningCtrlHome() throws BadLocationException {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		DelayedEchoCodeMiningProvider.DELAY = 500;
		fViewer.getDocument().set(TextViewerTest.generate5000Lines());
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getText().length() > 5000;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
		TextViewerTest.ctrlEnd(fViewer);
		final int lastLine = fViewer.getDocument().getNumberOfLines() - 1;
		final int lastLineOffset = fViewer.getDocument().getLineOffset(lastLine);
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return lastLineOffset >= fViewer.getVisibleRegion().getOffset() && lastLineOffset <= fViewer.getVisibleRegion().getOffset() + fViewer.getVisibleRegion().getLength();
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
		DisplayHelper.sleep(fViewer.getControl().getDisplay(), 500);
		AtomicInteger events = new AtomicInteger();
		fViewer.addViewportListener(offset ->
			events.incrementAndGet());
		TextViewerTest.ctrlHome(fViewer);
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return events.get() > 0;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
		Assert.assertEquals(0, fViewer.getVisibleRegion().getOffset());
		// wait for codemining to style line
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getLineVerticalIndent(0) > 0;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 300000));
	}

	@Test
	public void testCodeMiningCtrlEnd() throws BadLocationException {
		Assume.assumeFalse("See bug 541415. For whatever reason, this shortcut doesn't work on Mac", Util.isMac());
		fViewer.getDocument().set(TextViewerTest.generate5000Lines());
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getText().length() > 5000 && fViewer.getTextWidget().getLineVerticalIndent(0) > 0;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
		DisplayHelper.sleep(fViewer.getTextWidget().getDisplay(), 500);
		TextViewerTest.ctrlEnd(fViewer);
		final int lastLine = fViewer.getDocument().getNumberOfLines() - 1;
		final int lastLineOffset = fViewer.getDocument().getLineOffset(lastLine);
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return lastLineOffset >= fViewer.getVisibleRegion().getOffset() && lastLineOffset <= fViewer.getVisibleRegion().getOffset() + fViewer.getVisibleRegion().getLength();
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getLineVerticalIndent(lastLine) > 0;
			}
		}.waitForCondition(fViewer.getControl().getDisplay(), 3000));
	}

	@Test
	public void testCodeMiningEmptyLine() {
		fViewer.getDocument().set("\n");
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new ICodeMiningProvider() {
			@Override
			public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
				return CompletableFuture.completedFuture(Collections.singletonList(new StaticContentLineCodeMining(new Position(0, 1), "mining", this)));
			}

			@Override
			public void dispose() {
			}
		} });
		StyledText widget= fViewer.getTextWidget();
		Assert.assertTrue("Code mining is not visible in 1st empty line after line break character", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					StyleRange range= widget.getStyleRangeAtOffset(0);
					return range == null && hasCodeMiningPrintedAfterTextOnLine(fViewer, 0);
				} catch (BadLocationException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(fViewer.getTextWidget().getDisplay(), 1000));
	}

	@Test
	public void testCodeMiningEndOfLine() {
		fViewer.getDocument().set("a\n");
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new ICodeMiningProvider() {
			@Override
			public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
				return CompletableFuture.completedFuture(Collections.singletonList(new StaticContentLineCodeMining(new Position(1, 1), "mining", this)));
			}

			@Override
			public void dispose() {
			}
		} });
		StyledText widget= fViewer.getTextWidget();
		Assert.assertTrue("Code mining is not visible in 1st line after character a before line break character", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					StyleRange range= widget.getStyleRangeAtOffset(0);
					return range != null && range.metrics != null && hasCodeMiningPrintedAfterTextOnLine(fViewer, 0) == false;
				} catch (BadLocationException e) {
					e.printStackTrace();
					return false;
				}
			}
		}.waitForCondition(fViewer.getTextWidget().getDisplay(), 1000));
	}

	@Test
	public void testCodeMiningMultiLine() {
		fViewer.getDocument().set("a\nbc");
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new ICodeMiningProvider() {
			@Override
			public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
				return CompletableFuture.completedFuture(Collections.singletonList(new StaticContentLineCodeMining(new Position(0, 3), "long enough code mining to be wider than actual text", this)));
			}

			@Override
			public void dispose() {
			}
		} });
		StyledText widget = fViewer.getTextWidget();
		Assert.assertFalse("Code mining is visible on 2nd line", new DisplayHelper() {
			@Override
			protected boolean condition() {
				try {
					return widget.getStyleRangeAtOffset(0) != null && widget.getStyleRangeAtOffset(0).metrics != null
							&& hasCodeMiningPrintedAfterTextOnLine(fViewer, 1);
				} catch (BadLocationException e) {
					e.printStackTrace();
					return true;
				}
			}
		}.waitForCondition(fViewer.getTextWidget().getDisplay(), 1000));
	}

	private static boolean hasCodeMiningPrintedAfterTextOnLine(ITextViewer viewer, int line) throws BadLocationException {
		StyledText widget = viewer.getTextWidget();
		IDocument document= viewer.getDocument();
		int lineLength= document.getLineLength(line) - 1;
		if (lineLength < 0) {
			lineLength= 0;
		}
		Rectangle secondLineBounds= widget.getTextBounds(document.getLineOffset(line), document.getLineOffset(line) + lineLength);
		Image image = new Image(widget.getDisplay(), widget.getSize().x, widget.getSize().y);
		GC gc = new GC(widget);
		gc.copyArea(image, 0, 0);
		gc.dispose();
		ImageData imageData = image.getImageData();
		secondLineBounds.x += secondLineBounds.width; // look only area after text
		for (int x = secondLineBounds.x + 1; x < image.getBounds().width && x < imageData.width; x++) {
			for (int y = secondLineBounds.y; y < secondLineBounds.y + secondLineBounds.height && y < imageData.height; y++) {
				if (!imageData.palette.getRGB(imageData.getPixel(x, y)).equals(widget.getBackground().getRGB())) {
					// code mining printed
					image.dispose();
					return true;
				}
			}
		}
		image.dispose();
		return false;
	}
}
