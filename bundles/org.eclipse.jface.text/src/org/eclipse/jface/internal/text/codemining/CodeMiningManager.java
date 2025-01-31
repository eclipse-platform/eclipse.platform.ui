/**
 *  Copyright (c) 2017 Angelo ZERR.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
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
	 * Constructor of codemining manager with the given arguments.
	 *
	 * @param viewer                   the source viewer
	 * @param inlinedAnnotationSupport the inlined annotation support used to draw code minings
	 * @param codeMiningProviders      the array of codemining providers, must not be empty
	 */
	public CodeMiningManager(ISourceViewer viewer, InlinedAnnotationSupport inlinedAnnotationSupport,
			ICodeMiningProvider[] codeMiningProviders) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(inlinedAnnotationSupport);
		Assert.isNotNull(codeMiningProviders);
		fViewer= viewer;
		fInlinedAnnotationSupport= inlinedAnnotationSupport;
		setCodeMiningProviders(codeMiningProviders);
	}

	/**
	 * Set the codemining providers.
	 *
	 * @param codeMiningProviders the codemining providers.
	 */
	public void setCodeMiningProviders(ICodeMiningProvider[] codeMiningProviders) {
		cancel();
		if (fCodeMiningProviders != null) {
			fCodeMiningProviders.stream().forEach(ICodeMiningProvider::dispose);
		}
		fCodeMiningProviders= Arrays.asList(codeMiningProviders);
	}

	/**
	 * Uninstalls this codemining manager.
	 */
	public void uninstall() {
		cancel();
		if (fInlinedAnnotationSupport != null) {
			fInlinedAnnotationSupport.updateAnnotations(Collections.emptySet());
		}
	}

	/**
	 * Collect, resolve and render the code minings of the viewer.
	 */
	@Override
	public void run() {
		if (fViewer == null || fInlinedAnnotationSupport == null || fCodeMiningProviders == null
				|| fCodeMiningProviders.isEmpty() || fViewer.getAnnotationModel() == null) {
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
			Map<Position, List<ICodeMining>> groups= groupByLines(symbols, fCodeMiningProviders);
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

	private static void logCodeMiningProviderException(Throwable e) {
		if (e instanceof CancellationException || e.getCause() instanceof CancellationException) {
			return;
		}
		String PLUGIN_ID= "org.eclipse.jface.text"; //$NON-NLS-1$
		Bundle plugin= Platform.getBundle(PLUGIN_ID);
		if (plugin != null) {
			// In OSGi context, uses Platform Text log
			ILog log= ILog.of(plugin);
			log.log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e));
		} else {
			// In java main context, print stack trace
			System.err.println("Error while code mining process: " + e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining}
	 * for the given <code>viewer</code> by using the given providers.
	 *
	 * @param viewer    the text viewer.
	 * @param providers the CodeMining list providers.
	 * @param monitor   the progress monitor.
	 * @return the list of {@link CompletableFuture} which provides the list of {@link ICodeMining}
	 *         for the given <code>viewer</code> by using the given providers.
	 */
	private static CompletableFuture<List<? extends ICodeMining>> getCodeMinings(ITextViewer viewer,
			List<ICodeMiningProvider> providers, IProgressMonitor monitor) {
		List<CompletableFuture<List<? extends ICodeMining>>> com= providers.stream()
				.map(provider -> provider.provideCodeMinings(viewer, monitor))
				.filter(c -> c != null)
				.map(future -> future.exceptionally(e -> {
					logCodeMiningProviderException(e);
					return Collections.emptyList();
				}))
				.collect(Collectors.toList());
		return CompletableFuture.allOf(com.toArray(new CompletableFuture[com.size()])).thenApply(
				v -> com.stream().map(CompletableFuture::join).filter(Objects::nonNull).flatMap(java.util.Collection::stream).collect(Collectors.toList()));
	}

	/**
	 * Returns a sorted Map which groups the given code minings by same position line.
	 *
	 * @param codeMinings list of code minings to group.
	 * @param providers   CodeMining providers used to retrieve code minings.
	 * @return a sorted Map which groups the given code minings by same position line.
	 */
	private static Map<Position, List<ICodeMining>> groupByLines(List<? extends ICodeMining> codeMinings,
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
	 * @param groups  code minings grouped by lines position
	 * @param viewer  the viewer
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
		Set<ICodeMiningAnnotation> annotationsToRedraw= new HashSet<>();
		Set<AbstractInlinedAnnotation> currentAnnotations= new HashSet<>();
		// Loop for grouped code minings
		groups.entrySet().stream().forEach(g -> {
			// check if request was canceled.
			monitor.isCanceled();

			Position pos= new Position(g.getKey().offset, g.getKey().length);
			List<ICodeMining> minings= g.getValue();
			ICodeMining first= minings.get(0);
			boolean inLineHeader= !minings.isEmpty() ? (first instanceof LineHeaderCodeMining) : true;
			// Try to find existing annotation
			AbstractInlinedAnnotation ann= fInlinedAnnotationSupport.findExistingAnnotation(pos);
			if (ann == null) {
				// The annotation doesn't exists, create it.
				boolean afterPosition= false;
				if (first instanceof LineContentCodeMining m) {
					afterPosition= m.isAfterPosition();
				}
				Consumer<MouseEvent> mouseHover= null;
				Consumer<MouseEvent> mouseOut= null;
				Consumer<MouseEvent> mouseMove= null;
				if (first != null) {
					mouseHover= first.getMouseHover();
					mouseOut= first.getMouseOut();
					mouseMove= first.getMouseMove();
				}
				ann= inLineHeader
						? new CodeMiningLineHeaderAnnotation(pos, viewer, mouseHover, mouseOut, mouseMove)
						: new CodeMiningLineContentAnnotation(pos, viewer, afterPosition, mouseHover, mouseOut, mouseMove);
			} else if (ann instanceof ICodeMiningAnnotation && ((ICodeMiningAnnotation) ann).isInVisibleLines()) {
				// annotation is in visible lines
				annotationsToRedraw.add((ICodeMiningAnnotation) ann);
			}
			((ICodeMiningAnnotation) ann).update(minings, monitor);
			currentAnnotations.add(ann);
		});
		// check if request was canceled.
		monitor.isCanceled();
		fInlinedAnnotationSupport.updateAnnotations(currentAnnotations);
		// redraw the existing codemining annotations since their content can change
		annotationsToRedraw.stream().forEach(ICodeMiningAnnotation::redraw);
	}

	/**
	 * Returns <code>true</code> if the given mining has a non empty label and <code>false</code>
	 * otherwise.
	 *
	 * @param mining the mining to check
	 * @return <code>true</code> if the given mining has a non empty label and <code>false</code>
	 *         otherwise.
	 */
	static boolean isValidMining(ICodeMining mining) {
		return mining != null && mining.getLabel() != null && !mining.getLabel().isEmpty();
	}

	/**
	 * Returns the valid code mining at the given location by using the bounds of codemining
	 * annotations which stores only the valid code mining.
	 *
	 * @param minings the list of mining of the codemining annotation.
	 * @param bounds  the bounds of the valid minings of the codemining annotation.
	 * @param x       the x location
	 * @param y       the y location
	 * @return the valid code mining at the given location by using the bounds of codemining
	 *         annotations which stores only the valid code mining.
	 */
	static ICodeMining getValidCodeMiningAtLocation(ICodeMining[] minings, List<Rectangle> bounds, int x, int y) {
		for (int i= 0; i < bounds.size(); i++) {
			Rectangle bound= bounds.get(i);
			if (bound.contains(x, y)) {
				return getCodeValidMiningAtIndex(minings, i);
			}
		}
		return null;
	}

	/**
	 * Returns the valid code mining at the given index.
	 *
	 * @param minings the list of minings
	 * @param index   the index
	 * @return the valid code mining at the given index.
	 */
	private static ICodeMining getCodeValidMiningAtIndex(ICodeMining[] minings, int index) {
		int validIndex= 0;
		for (ICodeMining mining : minings) {
			if (isValidMining(mining)) {
				if (validIndex == index) {
					return mining;
				}
				validIndex++;
			}
		}
		return null;
	}
}
