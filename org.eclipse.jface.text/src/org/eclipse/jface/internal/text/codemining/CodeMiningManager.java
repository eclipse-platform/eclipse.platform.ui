/**
 *  Copyright (c) 2017 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - [CodeMining] Provide CodeMining support with CodeMiningManager - Bug 527720
 */
package org.eclipse.jface.internal.text.codemining;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.IViewportListener;
import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;

/**
 * Code Mining manager implementation.
 *
 * @since 3.13
 */
public class CodeMiningManager implements Runnable {

	/**
	 * The source viewer
	 */
	private final ISourceViewer fViewer;

	/**
	 * The inlined annotation support used to draw CodeMining in the line spacing.
	 */
	private final InlinedAnnotationSupport fInlinedAnnotationSupport;

	/**
	 * The list of codemining providers.
	 */
	private List<ICodeMiningProvider> fCodeMiningProviders;

	/**
	 * The current progress monitor.
	 */
	private IProgressMonitor fMonitor;

	/**
	 * Tracker of start/end offset of visible lines.
	 */
	private VisibleLines visibleLines;

	/**
	 * Class to track start/end offset of visible lines.
	 *
	 */
	class VisibleLines implements IViewportListener {

		private int startOffset;

		private int endOffset;

		public VisibleLines() {
			fViewer.getTextWidget().getDisplay().asyncExec(() -> {
				startOffset= getInclusiveTopIndexStartOffset();
				endOffset= getExclusiveBottomIndexEndOffset();
			});
		}

		@Override
		public void viewportChanged(int verticalOffset) {
			compute();
		}

		private void compute() {
			startOffset= getInclusiveTopIndexStartOffset();
			endOffset= getExclusiveBottomIndexEndOffset();
		}

		/**
		 * Returns the document offset of the upper left corner of the source viewer's view port,
		 * possibly including partially visible lines.
		 *
		 * @return the document offset if the upper left corner of the view port
		 */
		private int getInclusiveTopIndexStartOffset() {
			if (fViewer != null && fViewer.getTextWidget() != null && !fViewer.getTextWidget().isDisposed()) {
				int top= JFaceTextUtil.getPartialTopIndex(fViewer);
				try {
					IDocument document= fViewer.getDocument();
					return document.getLineOffset(top);
				} catch (BadLocationException x) {
					// Do nothing
				}
			}
			return -1;
		}

		/**
		 * Returns the first invisible document offset of the lower right corner of the source
		 * viewer's view port, possibly including partially visible lines.
		 *
		 * @return the first invisible document offset of the lower right corner of the view port
		 */
		private int getExclusiveBottomIndexEndOffset() {
			if (fViewer != null && fViewer.getTextWidget() != null && !fViewer.getTextWidget().isDisposed()) {
				int bottom= JFaceTextUtil.getPartialBottomIndex(fViewer);
				try {
					IDocument document= fViewer.getDocument();

					if (bottom >= document.getNumberOfLines())
						bottom= document.getNumberOfLines() - 1;

					return document.getLineOffset(bottom) + document.getLineLength(bottom);
				} catch (BadLocationException x) {
					// Do nothing
				}
			}
			return -1;
		}

		/**
		 * Returns true if the given annotation is in visible lines and false otherwise.
		 *
		 * @param ann the codemining annotation
		 * @return true if the given annotation is in visible lines and false otherwise.
		 */
		public boolean isInVisibleLines(CodeMiningAnnotation ann) {
			return ann.getPosition().getOffset() >= startOffset && ann.getPosition().getOffset() <= endOffset;
		}
	}


	/**
	 * Constructor of codemining manager with the given arguments.
	 *
	 * @param viewer the source viewer
	 * @param inlinedAnnotationSupport the inlined annotation support used to draw code minings
	 * @param codeMiningProviders the array of codemining providers, must not be empty
	 */
	public CodeMiningManager(ISourceViewer viewer, InlinedAnnotationSupport inlinedAnnotationSupport,
			ICodeMiningProvider[] codeMiningProviders) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(inlinedAnnotationSupport);
		Assert.isNotNull(codeMiningProviders);
		fViewer= viewer;
		visibleLines= new VisibleLines();
		fViewer.addViewportListener(visibleLines);
		fInlinedAnnotationSupport= inlinedAnnotationSupport;
		setCodeMiningProviders(codeMiningProviders);
	}

	/**
	 * Set the codemining providers.
	 *
	 * @param codeMiningProviders the codemining providers.
	 */
	public void setCodeMiningProviders(ICodeMiningProvider[] codeMiningProviders) {
		fCodeMiningProviders= Arrays.asList(codeMiningProviders);
	}

	/**
	 * Uninstalls this codemining manager.
	 */
	public void uninstall() {
		cancel();
		fViewer.removeViewportListener(visibleLines);
	}

	/**
	 * Collect, resolve and render the code minings of the viewer.
	 */
	@Override
	public void run() {
		if (fViewer == null || fInlinedAnnotationSupport == null || fCodeMiningProviders == null
				|| fCodeMiningProviders.size() == 0 || fViewer.getAnnotationModel() == null) {
			return;
		}
		// Cancel the last progress monitor to cancel last resolve and render of code
		// minings
		cancel();
		// Update the code minings
		updateCodeMinings();
	}

	/**
	 * Update the code minings.
	 */
	private void updateCodeMinings() {
		// Refresh the code minings by using the new progress monitor.
		fMonitor= new CancellationExceptionMonitor();
		IProgressMonitor monitor= fMonitor;
		// Collect the code minings for the viewer
		getCodeMinings(fViewer, fCodeMiningProviders, monitor).thenAccept(symbols -> {
			// check if request was canceled.
			monitor.isCanceled();
			// then group code minings by lines position
			Map<Position, List<ICodeMining>> groups= goupByLines(symbols, fCodeMiningProviders);
			// resolve and render code minings
			renderCodeMinings(groups, fViewer, monitor);
		});
	}

	/**
	 * Cancel the codemining process.
	 */
	private void cancel() {
		// Cancel the last progress monitor.
		if (fMonitor != null) {
			fMonitor.setCanceled(true);
		}
	}

	/**
	 * Return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining}
	 * for the given <code>viewer</code> by using the given providers.
	 *
	 * @param viewer the text viewer.
	 * @param providers the CodeMining list providers.
	 * @param monitor the progress monitor.
	 * @return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining}
	 *         for the given <code>viewer</code> by using the given providers.
	 */
	private static CompletableFuture<List<? extends ICodeMining>> getCodeMinings(ITextViewer viewer,
			List<ICodeMiningProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeMining>>> com= providers.stream()
				.map(provider -> provider.provideCodeMinings(viewer, monitor)).filter(c -> c != null)
				.collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).flatMap(l -> l.stream()).collect(Collectors.toList()));
	}

	/**
	 * Returns a sorted Map which groups the given code minings by same position line
	 *
	 * @param codeMinings list of code minings to group.
	 * @param providers CodeMining providers used to retrieve code minings.
	 * @return a sorted Map which groups the given code minings by same position line.
	 */
	private static Map<Position, List<ICodeMining>> goupByLines(List<? extends ICodeMining> codeMinings,
			List<ICodeMiningProvider> providers) {
		// sort code minings by lineNumber and provider-rank if
		Collections.sort(codeMinings, (a, b) -> {
			if (a.getPosition().offset < b.getPosition().offset) {
				return -1;
			} else if (a.getPosition().offset > b.getPosition().offset) {
				return 1;
			} else if (providers.indexOf(a.getProvider()) < providers.indexOf(b.getProvider())) {
				return -1;
			} else if (providers.indexOf(a.getProvider()) > providers.indexOf(b.getProvider())) {
				return 1;
			} else {
				return 0;
			}
		});
		return codeMinings.stream().collect(Collectors.groupingBy(ICodeMining::getPosition, LinkedHashMap::new,
				Collectors.mapping(Function.identity(), Collectors.toList())));
	}

	/**
	 * Render the codemining grouped by line position.
	 *
	 * @param groups code minings grouped by lines position
	 * @param viewer the viewer
	 * @param monitor the progress monitor
	 */
	private void renderCodeMinings(Map<Position, List<ICodeMining>> groups, ISourceViewer viewer,
			IProgressMonitor monitor) {
		// check if request was canceled.
		monitor.isCanceled();
		IDocument document= viewer != null ? viewer.getDocument() : null;
		if (document == null) {
			// this case comes from when editor is closed before codemining rendered is
			// done.
			return;
		}
		Set<CodeMiningAnnotation> annotationsToRedraw= new HashSet<>();
		Set<AbstractInlinedAnnotation> currentAnnotations= new HashSet<>();
		// Loop for grouped code minings
		groups.entrySet().stream().forEach(g -> {
			// check if request was canceled.
			monitor.isCanceled();

			Position pos= new Position(g.getKey().offset, g.getKey().length);
			List<ICodeMining> minings= g.getValue();
			// Try to find existing annotation
			CodeMiningAnnotation ann= fInlinedAnnotationSupport.findExistingAnnotation(pos);
			if (ann == null) {
				// The annotation doesn't exists, create it.
				ann= new CodeMiningAnnotation(pos, viewer);
			} else if (visibleLines.isInVisibleLines(ann)) {
				// annotation is in visible lines
				annotationsToRedraw.add(ann);
			}
			ann.update(minings, monitor);
			currentAnnotations.add(ann);
		});
		// check if request was canceled.
		monitor.isCanceled();
		fInlinedAnnotationSupport.updateAnnotations(currentAnnotations);
		// redraw the existing codemining annotations since their content can change
		annotationsToRedraw.stream().forEach(ann -> ann.redraw());
	}

}
