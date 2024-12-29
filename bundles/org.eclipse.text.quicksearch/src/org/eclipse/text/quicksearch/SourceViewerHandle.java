/*******************************************************************************
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jozef Tomek - initial API and implementation
 *******************************************************************************/
package org.eclipse.text.quicksearch;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.CursorLinePainter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.ILineDiffInfo;
import org.eclipse.jface.text.source.ILineDiffer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.LineBackgroundEvent;
import org.eclipse.swt.custom.LineBackgroundListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.ITextViewerCreator.ITextViewerHandle;
import org.eclipse.text.quicksearch.internal.ui.QuickSearchActivator;

/**
 * Implementation of {@link ITextViewerHandle} that provides common aspects expected from quicksearch text viewers:
 * <ul>
 * <li>focusing on selected quicksearch match as described in the documentation of
 * {@link ITextViewerHandle#focusMatch(IRegion, IRegion, int, IRegion) focusMatch()}
 * <li>highlighting all matches in the presented content by applying common text style through
 * {@link StyledText#setStyleRange(StyleRange)} on the viewert's text widget.
 * </ul>
 * <p>
 * Highlighting the selected match line is achieved by:
 * <ul>
 * <li>highlighting line number in the line numbers vertical ruler column by announcing it as a changed line for the
 * quick diff feature (if a viewer provides {@link IChangeRulerColumn})
 * <li>highlighting whole line by using {@link FixedLineHighlighter} (if a viewer provides one)
 * </ul>
 * <p>
 * This class uses {@link SourceViewerConfigurer} that does viewer creation and setup necessary for
 * aforementioned aspects.
 *
 * @since 1.3
 */
public class SourceViewerHandle<T extends SourceViewer> implements ITextViewerHandle {

	protected final T fSourceViewer;
	protected final IChangeRulerColumn fChangeRulerColumn;
	protected final FixedLineChangedAnnotationModel fFixedLineChangeModel;
	protected final FixedLineHighlighter fMatchLineHighlighter;
	protected StyleRange[] fMatchRanges = null;

	/**
	 * Creates new instance that will use <code>sourceViewerConfigurer</code> to create viewer under the
	 * <code>parent</code>.
	 *
	 * @param sourceViewerConfigurer the viewer configurer responsible for creation & setup of the viewer
	 * @param parent the parent SWT control for the viewer
	 */
	public SourceViewerHandle(ISourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent) {
		Assert.isNotNull(sourceViewerConfigurer);
		fSourceViewer = sourceViewerConfigurer.getSourceViewer(parent);
		Assert.isNotNull(fSourceViewer);
		fChangeRulerColumn = sourceViewerConfigurer.getChangeRulerColumn();
		fMatchLineHighlighter = sourceViewerConfigurer.getMatchLineHighlighter();
		if (fChangeRulerColumn != null) {
			fFixedLineChangeModel = new FixedLineChangedAnnotationModel();
			fChangeRulerColumn.setModel(fFixedLineChangeModel);
		} else {
			fFixedLineChangeModel = null;
		}
	}

	@Override
	public int getVisibleLines() {
		StyledText details = fSourceViewer.getTextWidget();
		if (details != null && !details.isDisposed()) {
			int lineHeight = details.getLineHeight();
			int areaHeight = details.getClientArea().height;
			return (areaHeight + lineHeight - 1) / lineHeight;
		}
		return 0;
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] allMatchesStyles, IFile file) {
		fMatchRanges = allMatchesStyles;
		fSourceViewer.setInput(document);
	}

	@Override
	public void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange) {
		// limit content of the document that we can scroll to
		if (!fSourceViewer.getVisibleRegion().equals(visibleRegion)) {
			fSourceViewer.setVisibleRegion(visibleRegion.getOffset(), visibleRegion.getLength());
		}
		// scroll to range to be presented
		fSourceViewer.revealRange(revealedRange.getOffset(), revealedRange.getLength());
		// sets caret position
		fSourceViewer.setSelectedRange(matchRange.getOffset(), 0);
		// does horizontal scrolling if necessary to reveal 1st occurrence in target line
		fSourceViewer.revealRange(matchRange.getOffset(), matchRange.getLength());

		if (fMatchLineHighlighter != null) {
			try {
				fMatchLineHighlighter.setTargetLineOffset(fSourceViewer.getDocument().getLineOffset(matchLine) - visibleRegion.getOffset());
			} catch (BadLocationException e) {
				QuickSearchActivator.log(e);
			}
		}

		if (fFixedLineChangeModel != null) {
			fFixedLineChangeModel.selectedMatchLine = matchLine;
			fChangeRulerColumn.redraw();
		}

	}

	/**
	 * Applies all matches highlighting styles previously passed to
	 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()} method considering projection of
	 * model (document) ranges to source viewer's text widget ranges.
	 *
	 * @see #setViewerInput(IDocument, StyleRange[], IFile)
	 */
	protected void applyMatchesStyles() {
		if (fMatchRanges == null || fMatchRanges.length == 0) {
			return;
		}
		for (StyleRange styleRange : fMatchRanges) {
			if (modelRange2WidgetStyleRange(styleRange) instanceof StyleRange range) {
				fSourceViewer.getTextWidget().setStyleRange(range);
			}
		}
	}

	private StyleRange modelRange2WidgetStyleRange(StyleRange range) {
		IRegion region= fSourceViewer.modelRange2WidgetRange(new Region(range.start, range.length));
		if (region != null) {
			StyleRange result= (StyleRange) range.clone();
			result.start= region.getOffset();
			result.length= region.getLength();
			return result;
		}
		return null;
	}

	/**
	 * Annotation model implementation that announces single configured line as {@link ILineDiffInfo#CHANGED} for
	 * quick diff feature of the viewer's {@link IChangeRulerColumn}.
	 */
	public static class FixedLineChangedAnnotationModel implements IAnnotationModel, ILineDiffer {

		protected int selectedMatchLine;

		@Override
		public void addAnnotationModelListener(IAnnotationModelListener listener) {
			// no-op
		}

		@Override
		public void removeAnnotationModelListener(IAnnotationModelListener listener) {
			// no-op
		}

		@Override
		public void connect(IDocument document) {
			// no-op
		}

		@Override
		public void disconnect(IDocument document) {
			// no-op
		}

		@Override
		public void addAnnotation(Annotation annotation, Position position) {
			// no-op
		}

		@Override
		public void removeAnnotation(Annotation annotation) {
			// no-op
		}

		@Override
		public Iterator<Annotation> getAnnotationIterator() {
			return Collections.emptyIterator();
		}

		@Override
		public Position getPosition(Annotation annotation) {
			return null;
		}

		@Override
		public ILineDiffInfo getLineInfo(int line) {
			return line == selectedMatchLine ? FixedLineChangedDiffInfo.INSTANCE : null;
		}

		@Override
		public void revertLine(int line) throws BadLocationException {
			// no-op
		}

		@Override
		public void revertBlock(int line) throws BadLocationException {
			// no-op
		}

		@Override
		public void revertSelection(int line, int nLines) throws BadLocationException {
			// no-op
		}

		@Override
		public int restoreAfterLine(int line) throws BadLocationException {
			// no-op
			return 0;
		}

	}

	private static class FixedLineChangedDiffInfo implements ILineDiffInfo {

		static final FixedLineChangedDiffInfo INSTANCE = new FixedLineChangedDiffInfo();

		@Override
		public int getRemovedLinesBelow() {
			return 0;
		}

		@Override
		public int getRemovedLinesAbove() {
			return 0;
		}

		@Override
		public int getChangeType() {
			return CHANGED;
		}

		@Override
		public boolean hasChanges() {
			return true;
		}

		@Override
		public String[] getOriginalText() {
			return new String[0];
		}

	}

	/**
	 * A line background listener that provides the color that is used for current line highlighting (what
	 * {@link CursorLinePainter} does) but for single fixed line only and does so always regardless of show current
	 * line highlighting on/off preference.
	 *
	 * @see CursorLinePainter
	 */
	public static class FixedLineHighlighter implements LineBackgroundListener {

		private int lineOffset = -1;
		private Color highlightColor;

		public void setHighlightColor(Color highlightColor) {
			this.highlightColor = highlightColor;
		}

		public void setTargetLineOffset(int lineOffset) {
			this.lineOffset = lineOffset;
		}

		@Override
		public void lineGetBackground(LineBackgroundEvent event) {
			if (lineOffset == event.lineOffset) {
				event.lineBackground = highlightColor;
			}
		}

	}

}
