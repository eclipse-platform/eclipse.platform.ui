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
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.ui.internal.texteditor.stickyscroll.IStickyLinesProvider.StickyLinesProperties;

public class DefaultStickyLinesProviderTest {

	private Shell shell;
	private SourceViewer sourceViewer;
	private DefaultStickyLinesProvider stickyLinesProvider;
	private StyledText textWidget;
	private StickyLinesProperties stickyLinesProperties;

	@Before
	public void setup() {
		shell = new Shell(Display.getDefault());
		sourceViewer = new SourceViewer(shell, null, SWT.None);
		stickyLinesProvider = new DefaultStickyLinesProvider();
		textWidget = sourceViewer.getTextWidget();
		stickyLinesProperties = new StickyLinesProperties(4);
	}

	@Test
	public void testEmptySourceCode() {
		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, is(empty()));
	}

	@Test
	public void testSingleStickyLine() {
		String text = """
				line 1
				 line 2<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0)));
	}

	@Test
	public void testLineUnderStickyLine() {
		String text = """
				line 1
				 line 2<
				  line 3
				  line 4""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0), new StickyLine(" line 2<", 1)));
	}

	@Test
	public void testNewStickyRoot() {
		String text = """
				line 1
				 line 2
				line 3
				 line 4<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 3", 2)));
	}

	@Test
	public void testIgnoreEmptyLines() {
		String text = """
				line 1

				 line 2

				  line 3<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0), new StickyLine(" line 2", 2)));
	}

	@Test
	public void testLinesWithTabs() {
		stickyLinesProperties = new StickyLinesProperties(2);
		String text = """
				line 1
				\tline 2
				\t\tline 3<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0), new StickyLine("\tline 2", 1)));

	}

	@Test
	public void testStartAtEmptyLineWithNext() {
		String text = """
				line 1

				 line 2

				  line 3""";
		textWidget.setText(text);
		textWidget.setTopIndex(3);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0), new StickyLine(" line 2", 2)));

	}

	@Test
	public void testStartAtEmptyLineWithPrevious() {
		String text = """
				line 1
				 line 2
				  line 3<
				line 4""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 0)));
	}

	@Test
	public void testRemoveStickyLines() {
		String text = """
				line 1
				 line 2
				line 3
				 line 4<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 3", 2)));
	}

	@Test
	public void testSourceViewerWithDifferentModelAndWindgetLines() {
		sourceViewer = new SourceViewerLineMapping(shell, null, SWT.None);
		textWidget = sourceViewer.getTextWidget();

		String text = """
				line 1
				 line 2<""";
		setText(text);

		List<StickyLine> stickyLines = stickyLinesProvider.getStickyLines(sourceViewer, stickyLinesProperties);

		assertThat(stickyLines, contains(new StickyLine("line 1", 42)));
	}

	/**
	 * Set the text into the text widget and set the top index to the line
	 * containing the <.
	 */
	private void setText(String text) {
		textWidget.setText(text);

		String[] lines = text.split("\n");
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains(String.valueOf("<"))) {
				textWidget.setTopIndex(i);
				return;
			}
		}
	}

	private class SourceViewerLineMapping extends SourceViewer implements ITextViewerExtension5 {

		public SourceViewerLineMapping(Composite parent, IVerticalRuler ruler, int styles) {
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
			return 42;
		}

	}

}
