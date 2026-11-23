/*******************************************************************************
 * Copyright (c) 2026 SAP and others.
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;

/**
 * Tests that static code minings stop repainting after reserving their space.
 * The viewer is opened in a shell so that SWT delivers real native paint events;
 * the test never requests further redraws while measuring those events.
 */
public class CodeMiningRepaintTest {

	@ParameterizedTest(name= "{0}")
	@CsvSource({
			"same position, 0, false, 0, false",
			"adjacent positions at line start, 0, false, 1, false",
			"tab at line start, 0, false, 1, false",
			"suffix and next prefix, 1, true, 2, false",
			"separate host characters, 1, false, 2, false",
			"end of line, 3, true, 3, true",
			"end of document, 7, true, 7, true" })
	public void testOverlongCodeMiningsStopRepainting(String scenario, int firstOffset, boolean firstAfter,
			int secondOffset, boolean secondAfter) throws Exception {
		Display display= Display.getDefault();
		Shell shell= new Shell(display);
		shell.setLayout(new FillLayout());
		shell.setSize(300, 180);
		SourceViewer viewer= new SourceViewer(shell, null, SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setDocument(new Document(scenario.startsWith("tab") ? "\tab\ndef" : "abc\ndef"), new AnnotationModel());
		StyledText widget= viewer.getTextWidget();
		AtomicInteger paints= new AtomicInteger();
		widget.addPaintListener(_ -> paints.incrementAndGet());
		List<String> widths= new ArrayList<>();
		List<RecordingMining> minings= new ArrayList<>();
		AbstractCodeMiningProvider provider= new AbstractCodeMiningProvider() {
			@Override
			public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer textViewer, IProgressMonitor monitor) {
				return CompletableFuture.completedFuture(new ArrayList<>(minings));
			}
		};
		minings.add(new RecordingMining(firstOffset, firstAfter, provider, "prediction one ".repeat(8), widths));
		minings.add(new RecordingMining(secondOffset, secondAfter, provider, "prediction two ".repeat(12), widths));
		AnnotationPainter painter= new AnnotationPainter(viewer, null);
		viewer.setCodeMiningAnnotationPainter(painter);
		viewer.addPainter(painter);
		viewer.setCodeMiningProviders(new ICodeMiningProvider[] { provider });
		try {
			shell.open();
			viewer.updateCodeMinings();
			pumpEvents(display, 3000);
			for (RecordingMining mining : minings) {
				assertTrue(mining.draws > 0, scenario + ": mining was never drawn");
				assertTrue(mining.width > widget.getClientArea().width, "prediction must exceed the editor width");
			}
			RecordingMining first= minings.get(0);
			RecordingMining second= minings.get(1);
			int host= firstAfter ? firstOffset : Math.max(0, firstOffset - 1);
			boolean sharedHost= firstOffset != secondOffset && !secondAfter && secondOffset - 1 == host;
			int before= paints.get();
			pumpEvents(display, 1000);
			int repeated= paints.get() - before;
			System.out.println(scenario + ": paints in final second=" + repeated + ", widths=" + widths);
			assertTrue(repeated < 50, scenario + ": static predictions keep repainting (" + repeated
					+ " paints in one second); host metrics before drawing: " + widths);
			assertTrue(second.x >= first.x + first.width, "predictions overlap");
			if (sharedHost) {
				StyleRange style= widget.getStyleRangeAtOffset(host);
				assertTrue(style != null && style.metrics != null
						&& style.metrics.width >= first.width + second.width, "shared glyph must reserve both predictions");
				int combined= widget.getStyleRangeAtOffset(host).metrics.width;
				minings.remove(second);
				viewer.updateCodeMinings();
				pumpEvents(display, 1000);
				assertEquals(combined - second.width, widget.getStyleRangeAtOffset(host).metrics.width,
						"removing one prediction must release only its width");
			}
			// Removing the minings must break the loop without closing the editor.
			viewer.setCodeMiningProviders(null);
			pumpEvents(display, 500);
			int afterRemoval= paints.get();
			pumpEvents(display, 500);
			assertTrue(paints.get() - afterRemoval < 50, "editor keeps repainting after removing the minings");
		} finally {
			viewer.setCodeMiningProviders(null);
			shell.dispose();
		}
	}

	/** Check the deadline after every dispatch: draining until idle would hang on a paint loop. */
	private static void pumpEvents(Display display, long milliseconds) throws InterruptedException {
		long deadline= System.nanoTime() + milliseconds * 1_000_000;
		while (System.nanoTime() < deadline) {
			if (!display.readAndDispatch()) {
				Thread.sleep(1);
			}
		}
	}

	private static class RecordingMining extends LineContentCodeMining {
		private final List<String> widths;
		int draws;
		int width;
		int x;

		RecordingMining(int offset, boolean after, ICodeMiningProvider provider, String label, List<String> widths) {
			super(new Position(offset, offset == 7 ? 0 : 1), after, provider);
			setLabel(label);
			this.widths= widths;
		}

		@Override
		public Point draw(GC gc, StyledText textWidget, Color color, int x, int y) {
			draws++;
			this.x= x;
			int host= Math.min(getPosition().offset, textWidget.getCharCount() - 1);
			if (!isAfterPosition() && host > 0
					&& textWidget.getLineAtOffset(host) == textWidget.getLineAtOffset(host - 1)) {
				host--;
			}
			Point size= super.draw(gc, textWidget, color, x, y);
			if (widths.size() < 12) {
				StyleRange style= textWidget.getStyleRangeAtOffset(host);
				widths.add("offset " + getPosition().offset + " host " + host + " metrics "
						+ (style == null || style.metrics == null ? "none" : style.metrics.width)
						+ " mining width " + size.x);
			}
			width= size.x;
			return size;
		}
	}
}
