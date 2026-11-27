/**
 *  Copyright (c) 2017, 2026 Angelo ZERR and others.
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
 *  Dietrich Travkin <dietrich.travkin@solunar.de> - Fix code mining redrawing - Issue 3405
 *  Alexander Fedorov <alexander.fedorov@arsysop.ru> - Remove redundant null checks
 */
package org.eclipse.jface.internal.text.codemining;

import java.util.ArrayList;
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
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.DocumentFooterCodeMining;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.Annotation;
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

	private final Display fDisplay;

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

	private List<Predicate<? super ICodeMining>> fCodeMiningFilters= List.of();

	private Set<AbstractInlinedAnnotation> fCurrentAnnotations= Collections.emptySet();

	/**
	 * Constructor of codemining manager with the given arguments.
	 *
	 * @param viewer                   the source viewer
	 * @param inlinedAnnotationSupport the inlined annotation support used to draw code minings
	 * @param codeMiningProviders      the array of codemining providers, must not be empty
	 */
	public CodeMiningManager(ISourceViewer viewer, InlinedAnnotationSupport inlinedAnnotationSupport,
			ICodeMiningProvider[] codeMiningProviders) {
		this(viewer, inlinedAnnotationSupport, codeMiningProviders, List.of());
	}

	/**
	 * Creates a manager with the initial filters.
	 *
	 * @param viewer the source viewer
	 * @param inlinedAnnotationSupport the installed annotation support
	 * @param codeMiningProviders the installed providers
	 * @param filters the initial filters
	 */
	public CodeMiningManager(ISourceViewer viewer, InlinedAnnotationSupport inlinedAnnotationSupport,
			ICodeMiningProvider[] codeMiningProviders, List<Predicate<? super ICodeMining>> filters) {
		Assert.isNotNull(viewer);
		Assert.isNotNull(inlinedAnnotationSupport);
		Assert.isNotNull(codeMiningProviders);
		fViewer= viewer;
		fDisplay= viewer.getTextWidget().getDisplay();
		fInlinedAnnotationSupport= inlinedAnnotationSupport;
		setCodeMiningProviders(codeMiningProviders);
		fCodeMiningFilters= List.copyOf(filters);
	}

	/**
	 * Replaces the filters and refreshes minings on the viewer's UI thread.
	 *
	 * @param filters the filters to apply
	 */
	public void setCodeMiningFilters(List<Predicate<? super ICodeMining>> filters) {
		List<Predicate<? super ICodeMining>> snapshot= List.copyOf(filters);
		synchronized (this) {
			cancel();
			fCodeMiningFilters= snapshot;
		}
		Set<AbstractInlinedAnnotation> retained= new HashSet<>();
		for (AbstractInlinedAnnotation annotation : fCurrentAnnotations) {
			try {
				ICodeMiningAnnotation miningAnnotation= (ICodeMiningAnnotation) annotation;
				if (miningAnnotation.getMinings().stream().allMatch(mining -> acceptsMining(mining, snapshot))
						&& Arrays.stream(miningAnnotation.getCachedMinings()).filter(Objects::nonNull)
								.allMatch(mining -> acceptsMining(mining, snapshot))) {
					retained.add(annotation);
				}
			} catch (Throwable e) {
				logCodeMiningProviderException(e);
			}
		}
		fCurrentAnnotations= retained;
		fInlinedAnnotationSupport.updateAnnotations(retained);
		run();
	}

	/**
	 * Set the codemining providers.
	 *
	 * @param codeMiningProviders the codemining providers.
	 */
	public void setCodeMiningProviders(ICodeMiningProvider[] codeMiningProviders) {
		List<ICodeMiningProvider> previous;
		synchronized (this) {
			cancel();
			previous= fCodeMiningProviders;
			fCodeMiningProviders= List.copyOf(Arrays.asList(codeMiningProviders));
		}
		if (previous != null) {
			previous.forEach(ICodeMiningProvider::dispose);
		}
	}

	/**
	 * Uninstalls this codemining manager.
	 */
	public void uninstall() {
		List<ICodeMiningProvider> previous;
		synchronized (this) {
			cancel();
			previous= fCodeMiningProviders;
			fCodeMiningProviders= List.of();
		}
		fCurrentAnnotations= Collections.emptySet();
		fInlinedAnnotationSupport.updateAnnotations(Collections.emptySet());
		if (previous != null) {
			previous.forEach(ICodeMiningProvider::dispose);
		}
	}

	/**
	 * Collect, resolve and render the code minings of the viewer.
	 */
	@Override
	public void run() {
		if (fViewer.getAnnotationModel() == null) {
			return;
		}
		updateCodeMinings();
	}

	/**
	 * Update the code minings.
	 */
	private void updateCodeMinings() {
		IProgressMonitor monitor;
		List<ICodeMiningProvider> providers;
		List<Predicate<? super ICodeMining>> filters;
		synchronized (this) {
			if (fCodeMiningProviders == null || fCodeMiningProviders.isEmpty()) {
				return;
			}
			cancel();
			fMonitor= new CancellationExceptionMonitor();
			monitor= fMonitor;
			providers= fCodeMiningProviders;
			filters= fCodeMiningFilters;
		}
		// Collect the code minings for the viewer
		getCodeMinings(fViewer, providers, monitor).thenAccept(symbols -> {
			List<ICodeMining> accepted= new ArrayList<>();
			List<ICodeMining> rejected= new ArrayList<>();
			try {
				monitor.isCanceled();
				for (ICodeMining mining : symbols) {
					if (acceptsMining(mining, filters)) {
						accepted.add(mining);
					} else {
						rejected.add(mining);
					}
				}
			} catch (Throwable e) {
				disposeMinings(symbols);
				logCodeMiningProviderException(e);
				return;
			}
			disposeMinings(rejected);
			Runnable render= () -> {
				try {
					monitor.isCanceled();
					if (fViewer.getDocument() == null || fViewer.getTextWidget() == null
							|| fViewer.getTextWidget().isDisposed()) {
						disposeMinings(accepted);
						return;
					}
				} catch (CancellationException e) {
					disposeMinings(accepted);
					return;
				}
				try {
					renderCodeMinings(groupByLines(accepted, providers), fViewer, monitor);
				} catch (Throwable e) {
					logCodeMiningProviderException(e);
				}
			};
			if (Display.getCurrent() == fDisplay) {
				render.run();
			} else {
				try {
					fDisplay.asyncExec(render);
				} catch (RuntimeException e) {
					disposeMinings(accepted);
					logCodeMiningProviderException(e);
				}
			}
		});
	}

	static void disposeMinings(List<? extends ICodeMining> minings) {
		for (ICodeMining mining : minings) {
			try {
				mining.dispose();
			} catch (Throwable e) {
				logCodeMiningProviderException(e);
			}
		}
	}

	private static boolean acceptsMining(ICodeMining mining, List<Predicate<? super ICodeMining>> filters) {
		for (Predicate<? super ICodeMining> filter : filters) {
			if (!filter.test(mining)) {
				return false;
			}
		}
		return true;
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
				v -> com.stream().map(CompletableFuture::join).filter(Objects::nonNull)
						.flatMap(java.util.Collection::stream).filter(Objects::nonNull).collect(Collectors.toList()));
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

	private static enum CodeMiningMode {
		InLine(LineContentCodeMining.class, CodeMiningLineContentAnnotation.class),
		HeaderLine(LineHeaderCodeMining.class, CodeMiningLineHeaderAnnotation.class),
		FooterLine(DocumentFooterCodeMining.class, CodeMiningDocumentFooterAnnotation.class);

		public final Class<? extends ICodeMining> codeMiningType;
		public final Class<? extends AbstractInlinedAnnotation> annotationType;

		CodeMiningMode(Class<? extends ICodeMining> codeMiningType,
				Class<? extends AbstractInlinedAnnotation> annotationType) {
			this.codeMiningType= codeMiningType;
			this.annotationType= annotationType;
		}

		public static CodeMiningMode createFor(List<ICodeMining> minings) {
			Assert.isNotNull(minings);

			CodeMiningMode mode= CodeMiningMode.HeaderLine;
			if (!minings.isEmpty()) {
				ICodeMining first= minings.get(0);

				if (CodeMiningMode.InLine.codeMiningType.isInstance(first)) {
					mode= CodeMiningMode.InLine;
				} else if (CodeMiningMode.HeaderLine.codeMiningType.isInstance(first)) {
					mode= CodeMiningMode.HeaderLine;
				} else if (CodeMiningMode.FooterLine.codeMiningType.isInstance(first)) {
					mode= CodeMiningMode.FooterLine;
				} else {
					mode= CodeMiningMode.InLine;
				}
			}
			return mode;
		}
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
		IDocument document= viewer.getDocument();
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

			CodeMiningMode mode= CodeMiningMode.createFor(minings);

			// Try to find existing annotation
			AbstractInlinedAnnotation ann= fInlinedAnnotationSupport.findExistingAnnotation(pos);
			if (ann == null || !mode.annotationType.isInstance(ann)) {
				// The annotation doesn't exists or has wrong type => create a new one.
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

				ann= switch (mode) {
					case InLine -> new CodeMiningLineContentAnnotation(pos, viewer, afterPosition, mouseHover, mouseOut, mouseMove);
					case HeaderLine -> new CodeMiningLineHeaderAnnotation(pos, viewer, mouseHover, mouseOut, mouseMove);
					case FooterLine -> new CodeMiningDocumentFooterAnnotation(pos, viewer, mouseHover, mouseOut, mouseMove);

					default -> throw new IllegalStateException("Found unexpected code mining display mode: " + mode); //$NON-NLS-1$
				};
			} else if (ann instanceof ICodeMiningAnnotation && ((ICodeMiningAnnotation) ann).isInVisibleLines()) {
				// annotation is in visible lines
				annotationsToRedraw.add((ICodeMiningAnnotation) ann);
			}
			((ICodeMiningAnnotation) ann).update(minings, monitor);
			currentAnnotations.add(ann);
		});
		// check if request was canceled.
		monitor.isCanceled();
		fCurrentAnnotations= currentAnnotations;
		fInlinedAnnotationSupport.updateAnnotations(currentAnnotations);
		monitor.isCanceled();
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

	/**
	 * returns true if the given annotation is a deleted {@link ICodeMiningAnnotation}
	 */
	public static boolean isDeletedCodeMining(Annotation annotation) {
		if (annotation.isMarkedDeleted() && annotation instanceof ICodeMiningAnnotation) {
			return true;
		}
		return false;
	}
}
