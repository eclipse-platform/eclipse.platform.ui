package org.eclipse.text.quicksearch;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

/**
 * Extension of {@link SourceViewerHandle} which by means of registering itself as a {@link ITextPresentationListener}
 * observes styles applied to source viewer's text from presentation reconcilers and merges quick search matches
 * highlighting styles into them. It also collects all these styles to be later able to apply them when the handle
 * is asked to focus different match in the document. This is to overcome shortcomings of those source viewers that use
 * multiple presentation reconcilers but trigger only some of them on when viewer's visible area is changed on match
 * focus.
 *
 * @since 1.4
 */
public class ReconcilingAwareSourceViewerHandle<T extends SourceViewer> extends SourceViewerHandle<T>
		implements ITextPresentationListener, DisposeListener{

	private final boolean fCollectStylesInSetInput;
	private final boolean fCollectStylesInFocusMatch;

	private boolean fFirstFocusMatch;
	private boolean fDoCollectStyles;
	/**
	 * Helps with applying {@link SourceViewerHandle#fMatchRanges} to some text presentation considering its extent.
	 */
	private TextPresentation fMatchStylesPresentation;
	/**
	 * Union of styles collected from text presentations previously applied to the viewer. Already have match styles
	 * merged in.
	 */
	private TextPresentation fCollectedStylesPresentation;

	/**
	 * Creates new instance which, depending on <code>collectStylesInSetInput</code> and
	 * <code>collectStylesInFocusMatch</code>, will also collect applied styles while executing
	 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()} and
	 * {@link #focusMatch(IRegion, IRegion, int, IRegion) focusMatch()} respectively.
	 *
	 * @param sourceViewerConfigurer the viewer configurer responsible for creation & setup of the viewer
	 * @param parent the parent SWT control for the viewer
	 * @param collectStylesInSetInput whether to collect styles also during execution of
	 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()} method
	 * @param collectStylesInFocusMatch whether to collect styles also during execution of
	 * {@link #focusMatch(IRegion, IRegion, int, IRegion) focusMatch()}
	 */
	public ReconcilingAwareSourceViewerHandle(ISourceViewerConfigurer<T> sourceViewerConfigurer, Composite parent,
			boolean collectStylesInSetInput, boolean collectStylesInFocusMatch) {
		super(sourceViewerConfigurer, parent);
		fCollectStylesInSetInput = collectStylesInSetInput;
		fCollectStylesInFocusMatch = collectStylesInFocusMatch;
		fSourceViewer.addTextPresentationListener(this);
		parent.addDisposeListener(this);
	}

	@Override
	public void setViewerInput(IDocument document, StyleRange[] matchRangers, IFile file) {
		fFirstFocusMatch = true;
		fDoCollectStyles = fCollectStylesInSetInput;
		if (matchRangers != null && matchRangers.length > 0) {
			fMatchStylesPresentation = new TextPresentation(matchRangers.length);
			for (StyleRange styleRange : matchRangers) {
				fMatchStylesPresentation.addStyleRange((StyleRange) styleRange.clone());
			}
		} else {
			// should never happen
			fMatchStylesPresentation = null;
		}
		fCollectedStylesPresentation = new TextPresentation(1024);
		super.setViewerInput(document, matchRangers, file);
		fDoCollectStyles = true;
	}

	@Override
	public void focusMatch(IRegion visibleRegion, IRegion revealedRange, int matchLine, IRegion matchRange) {
		fDoCollectStyles = fCollectStylesInFocusMatch;
		try {
			super.focusMatch(visibleRegion, revealedRange, matchLine, matchRange);
			if (fFirstFocusMatch) {
				// on 1st focus we don't need to apply collected styles yet
				fFirstFocusMatch = false;
			} else if (fCollectedStylesPresentation != null) { // should never be NULL here
				fDoCollectStyles = false;
				int mergedStyles = fCollectedStylesPresentation.getDenumerableRanges();
				if (mergedStyles > 0) {
					var presentation = new TextPresentation(fCollectedStylesPresentation.getCoverage(), mergedStyles);
					Iterator<StyleRange> iter = fCollectedStylesPresentation.getAllStyleRangeIterator();
					while (iter.hasNext()) {
						presentation.addStyleRange((StyleRange) iter.next().clone());
					}
					fSourceViewer.changeTextPresentation(presentation, false);
				}
			}
		} finally {
			fDoCollectStyles = true;
		}
	}

	@Override
    protected void postFocusMatch() {
		// no-op to avoid replacing styles directly on text widget
    }

	@Override
	public void applyTextPresentation(TextPresentation textPresentation) {
		applyMatchStyles(textPresentation, true); // always enrich with match styles
		if (fDoCollectStyles && fCollectedStylesPresentation != null) { // should never be NULL here
			for (Iterator<StyleRange> iter = textPresentation.getAllStyleRangeIterator(); iter.hasNext();) {
				// viewer's text widget also replaces style ranges with those arriving last
				fCollectedStylesPresentation.replaceStyleRange((StyleRange) iter.next().clone());
			}
		}
	}

	/**
	 * Applies all matches highlighting styles previously passed to
	 * {@link #setViewerInput(IDocument, StyleRange[], IFile) setViewerInput()} method to <code>presentation</code>
	 * considering presentation's extent. Styles either replace ({@link TextPresentation#replaceStyleRange(StyleRange)})
	 * or are merged ({@link TextPresentation#mergeStyleRange(StyleRange)}) with text presentation's styles in the
	 * particular ranges depending on <code>mergeStyles</code> parameter.
	 *
	 * @param mergeStyles <code>true</code> if the styles should be merged, <code>false</code> if they should replace
	 * text presentation styles in the same ranges
	 * @see #setViewerInput(IDocument, StyleRange[], IFile)
	 */
	protected void applyMatchStyles(TextPresentation presentation, boolean mergeStyles) {
		if (fMatchStylesPresentation == null) {
			// should never happen
			return;
		}
		var extent = presentation.getExtent();
		int extentStart = extent.getOffset();
		fMatchStylesPresentation.setResultWindow(extent);
		for (Iterator<StyleRange> iter = fMatchStylesPresentation.getAllStyleRangeIterator(); iter.hasNext();) {
			var style = iter.next();
			style.start += extentStart;
			if (mergeStyles) {
				presentation.mergeStyleRange(style);
			} else {
				presentation.replaceStyleRange(style);
			}
		}
	}

	@Override
	public void widgetDisposed(DisposeEvent e) {
		fCollectedStylesPresentation = null;
		fMatchStylesPresentation = null;
		fSourceViewer.removeTextPresentationListener(this);
	}

}
