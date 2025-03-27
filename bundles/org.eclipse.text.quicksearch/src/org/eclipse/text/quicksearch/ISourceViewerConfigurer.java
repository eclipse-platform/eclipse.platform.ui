package org.eclipse.text.quicksearch;

import org.eclipse.jface.text.source.IChangeRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.text.quicksearch.SourceViewerConfigurer.ISourceViewerCreator;
import org.eclipse.text.quicksearch.SourceViewerHandle.FixedLineHighlighter;

/**
 * Factory used by {@link SourceViewerHandle} responsible for creation and necessary setup of source viewers so that
 * they provide common aspects of quicksearch text viewers:
 * <ul>
 * <li>vertical ruler with line numbers supporting selected match line number highlighting
 * <li>selected match line highlighting
 * <li>current (caret position) line highlighting
 * <li>colors and fonts consistent with text viewers/editors preferences
 * </ul>
 *
 * Actual source viewer instance creation is delegated to provided {@link ISourceViewerCreator}.
 * @since 1.3
 * @see SourceViewerConfigurer
 */
public interface ISourceViewerConfigurer<T extends SourceViewer> {

	/**
	 * Creates, configures and returns source viewer that provides common aspects of quicksearch text viewers. Delegates
	 * source viewer creation to {@link ISourceViewerCreator} provided on initialization.
	 * @param parent the parent SWT control for the viewer
	 * @return configured source viewer
	 * @see ISourceViewerCreator
	 */
	T getSourceViewer(Composite parent);

	/**
	 * Returns change ruler column installed to the viewer.
	 * @return viewer's change ruler column
	 */
	IChangeRulerColumn getChangeRulerColumn();

	/**
	 * Returns fixed line highlighter installed to the viewer.
	 * @return viewer's fixed line highlighter
	 */
	FixedLineHighlighter getMatchLineHighlighter();
}
