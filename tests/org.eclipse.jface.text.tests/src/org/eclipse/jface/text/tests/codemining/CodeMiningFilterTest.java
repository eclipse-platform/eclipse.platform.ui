/*******************************************************************************
 * Copyright (c) 2026 SAP SE and others.
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.internal.text.codemining.ICodeMiningAnnotation;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMining;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.DocumentFooterCodeMining;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

public class CodeMiningFilterTest {

	private Shell shell;
	private SourceViewer viewer;
	private AnnotationModel model;

	@BeforeEach
	public void setUp() {
		shell= new Shell(Display.getDefault());
		shell.setSize(500, 200);
		shell.setLayout(new FillLayout());
		viewer= new SourceViewer(shell, null, SWT.NONE);
		model= new AnnotationModel();
		viewer.setDocument(new Document("line0\nline1\nline2\nline3"), model);
		AnnotationPainter painter= new AnnotationPainter(viewer, null);
		viewer.setCodeMiningAnnotationPainter(painter);
		viewer.addPainter(painter);
		shell.open();
	}

	@AfterEach
	public void tearDown() {
		shell.dispose();
	}

	@Test
	public void testFiltersAllMiningTypesAndRestoresProviders() {
		TestProvider provider= new TestProvider();
		install(provider);
		waitForAnnotations(3);
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		viewer.addCodeMiningFilter(hide);
		waitForAnnotations(0);
		assertEquals(0, provider.disposals);
		assertMiningsDisposedExactlyOnce(provider);
		viewer.removeCodeMiningFilter(hide);
		waitForAnnotations(3);
		assertEquals(0, provider.disposals);
		assertSame(provider, provider.created.getLast().getProvider());
	}

	@Test
	public void testBackgroundRenderingDoesNotWaitForUiThreadAfterTyping() throws Exception {
		TestProvider provider= new TestProvider();
		install(provider);
		waitForAnnotations(3);
		viewer.getDocument().replace(0, 0, "typed");
		provider.delayed= true;
		viewer.updateCodeMinings();
		List<ICodeMining> minings= provider.createMinings();
		CompletableFuture<List<? extends ICodeMining>> pending= provider.pending.getLast();
		CompletableFuture<Void> completion= CompletableFuture.runAsync(() -> pending.complete(minings));
		try {
			completion.get(2, TimeUnit.SECONDS);
			viewer.updateCodeMinings();
		} finally {
			DisplayHelper.sleep(shell.getDisplay(), 100);
		}
		assertEquals(1, provider.created.getLast().disposals);
		provider.pending.getLast().complete(provider.createMinings());
		waitForAnnotations(3);
	}

	@Test
	public void testUiRefreshDoesNotWaitForBackgroundFilter() throws Exception {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		CountDownLatch entered= new CountDownLatch(1);
		CountDownLatch release= new CountDownLatch(1);
		viewer.addCodeMiningFilter(mining -> {
			if (Display.getCurrent() == null) {
				entered.countDown();
				try {
					if (!release.await(2, TimeUnit.SECONDS)) {
						throw new CancellationException();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new CancellationException();
				}
			}
			return mining.getProvider() == provider;
		});
		install(provider);
		List<ICodeMining> minings= provider.createMinings();
		CompletableFuture<List<? extends ICodeMining>> pending= provider.pending.getLast();
		CompletableFuture<Void> completion= CompletableFuture.runAsync(() -> pending.complete(minings));
		try {
			assertTrue(entered.await(2, TimeUnit.SECONDS));
			long started= System.nanoTime();
			viewer.updateCodeMinings();
			assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(1),
					"UI refresh waited for foreign code holding the manager lock");
		} finally {
			release.countDown();
			DisplayHelper.sleep(shell.getDisplay(), 100);
		}
		completion.get(2, TimeUnit.SECONDS);
		provider.pending.getLast().complete(provider.createMinings());
		waitForAnnotations(3);
	}

	@Test
	public void testThrowingRejectedMiningDisposalDoesNotDropAcceptedMinings() {
		TestProvider provider= new TestProvider();
		RuntimeException failure= new IllegalStateException("synthetic disposal failure");
		provider.headerDisposalFailure= failure;
		List<IStatus> errors= new ArrayList<>();
		ILogListener listener= (status, plugin) -> {
			if (status.getException() == failure) {
				errors.add(status);
			}
		};
		Platform.addLogListener(listener);
		try {
			viewer.addCodeMiningFilter(mining -> !(mining instanceof LineHeaderCodeMining));
			install(provider);
			waitForAnnotations(2);
			assertEquals(1, provider.created.getFirst().disposals);
			assertEquals(1, errors.size());
			assertEquals(0, provider.otherDisposals.getFirst().get());
			viewer.setCodeMiningProviders(null);
			assertTrue(provider.otherDisposals.stream().allMatch(disposals -> disposals.get() == 1));
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	@Test
	public void testThrowingCanceledMiningDisposalDoesNotLeakFollowingMinings() {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		RuntimeException failure= new IllegalStateException("synthetic canceled disposal failure");
		provider.headerDisposalFailure= failure;
		AtomicInteger errors= new AtomicInteger();
		ILogListener listener= (status, plugin) -> {
			if (status.getException() == failure) {
				errors.incrementAndGet();
			}
		};
		Platform.addLogListener(listener);
		try {
			install(provider);
			viewer.updateCodeMinings();
			provider.pending.getFirst().complete(provider.createMinings());
			assertMiningsDisposedExactlyOnce(provider);
			assertEquals(1, errors.get());
		} finally {
			Platform.removeLogListener(listener);
		}
	}

	@Test
	public void testFiltersComposeAndRegistrationUsesIdentity() {
		TestProvider provider= new TestProvider();
		install(provider);
		Predicate<ICodeMining> noHeaders= mining -> !(mining instanceof LineHeaderCodeMining);
		Predicate<ICodeMining> noInline= mining -> !(mining instanceof LineContentCodeMining);
		viewer.addCodeMiningFilter(noHeaders);
		waitForAnnotations(2);
		int requests= provider.requests;
		viewer.addCodeMiningFilter(noHeaders);
		assertEquals(requests, provider.requests);
		viewer.addCodeMiningFilter(noInline);
		waitForAnnotations(1);
		viewer.removeCodeMiningFilter(noHeaders);
		waitForAnnotations(2);
		viewer.removeCodeMiningFilter(noHeaders);
		assertEquals(requests + 2, provider.requests);
		viewer.removeCodeMiningFilter(noInline);
		waitForAnnotations(3);
	}

	@Test
	public void testOffThreadCallsAreRejectedBeforeProvidersAreInstalled() {
		Predicate<ICodeMining> filter= mining -> mining instanceof LineHeaderCodeMining;
		CompletableFuture.runAsync(() -> {
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS,
					assertThrows(SWTException.class, () -> viewer.addCodeMiningFilter(filter)).code);
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS,
					assertThrows(SWTException.class, () -> viewer.removeCodeMiningFilter(filter)).code);
		}).join();
		install(new TestProvider());
		waitForAnnotations(3);
	}

	@Test
	public void testOffThreadCallsCannotChangeRegisteredFilters() {
		TestProvider provider= new TestProvider();
		install(provider);
		Predicate<ICodeMining> headers= mining -> mining instanceof LineHeaderCodeMining;
		Predicate<ICodeMining> inline= mining -> mining instanceof LineContentCodeMining;
		viewer.addCodeMiningFilter(headers);
		waitForAnnotations(1);
		int requests= provider.requests;
		CompletableFuture.runAsync(() -> {
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS,
					assertThrows(SWTException.class, () -> viewer.addCodeMiningFilter(headers)).code);
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS,
					assertThrows(SWTException.class, () -> viewer.addCodeMiningFilter(inline)).code);
			assertEquals(SWT.ERROR_THREAD_INVALID_ACCESS,
					assertThrows(SWTException.class, () -> viewer.removeCodeMiningFilter(headers)).code);
		}).join();
		assertEquals(requests, provider.requests);
		assertEquals(1, annotationCount());
		viewer.removeCodeMiningFilter(headers);
		waitForAnnotations(3);
	}

	@Test
	public void testFilterCallsAfterDisposalAreRejected() {
		Predicate<ICodeMining> filter= mining -> mining instanceof LineHeaderCodeMining;
		shell.dispose();
		assertEquals(SWT.ERROR_WIDGET_DISPOSED,
				assertThrows(SWTException.class, () -> viewer.addCodeMiningFilter(filter)).code);
		assertEquals(SWT.ERROR_WIDGET_DISPOSED,
				assertThrows(SWTException.class, () -> viewer.removeCodeMiningFilter(filter)).code);
	}

	@Test
	public void testRepeatedRegistrationAndRemovalDoNotRecomputeMinings() {
		TestProvider provider= new TestProvider();
		install(provider);
		Predicate<ICodeMining> filter= mining -> mining instanceof LineHeaderCodeMining;
		viewer.addCodeMiningFilter(filter);
		int requests= provider.requests;
		for (int attempt= 0; attempt < 1000; attempt++) {
			viewer.addCodeMiningFilter(filter);
		}
		assertEquals(requests, provider.requests);
		viewer.removeCodeMiningFilter(filter);
		requests= provider.requests;
		for (int attempt= 0; attempt < 1000; attempt++) {
			viewer.removeCodeMiningFilter(filter);
		}
		assertEquals(requests, provider.requests);
		waitForAnnotations(3);
	}

	@Test
	public void testNullProviderResultsAreIgnoredBeforeFiltering() {
		TestProvider provider= new TestProvider();
		provider.includeNullEntries= true;
		AtomicInteger filtered= new AtomicInteger();
		Predicate<ICodeMining> filter= mining -> {
			filtered.incrementAndGet();
			return mining.getProvider() == provider;
		};
		viewer.addCodeMiningFilter(filter);
		viewer.setCodeMiningProviders(new ICodeMiningProvider[] {
				new EmptyProvider(false), new EmptyProvider(true), provider });
		waitForAnnotations(3);
		assertEquals(3, filtered.get());
		viewer.removeCodeMiningFilter(filter);
		waitForAnnotations(3);
		assertEquals(0, provider.disposals);
	}

	@Test
	public void testNullEntriesAreIgnoredWithoutFilters() {
		TestProvider provider= new TestProvider();
		provider.includeNullEntries= true;
		install(provider);
		waitForAnnotations(3);
	}

	@Test
	public void testCanceledResultWithNullEntriesDisposesValidMinings() {
		TestProvider provider= new TestProvider();
		provider.includeNullEntries= true;
		provider.delayed= true;
		install(provider);
		viewer.addCodeMiningFilter(mining -> mining.getProvider() != provider);
		provider.pending.getFirst().complete(provider.createMinings());
		assertMiningsDisposedExactlyOnce(provider);
		provider.pending.getLast().complete(provider.createMinings());
		assertMiningsDisposedExactlyOnce(provider);
		assertEquals(0, annotationCount());
	}

	@Test
	public void testFilterRetainedAcrossProviderReconfiguration() {
		Predicate<ICodeMining> headers= mining -> mining instanceof LineHeaderCodeMining;
		viewer.addCodeMiningFilter(headers);
		TestProvider first= new TestProvider();
		install(first);
		waitForAnnotations(1);
		viewer.setCodeMiningProviders(null);
		assertEquals(1, first.disposals);
		TestProvider second= new TestProvider();
		install(second);
		waitForAnnotations(1);
		viewer.removeCodeMiningFilter(headers);
		waitForAnnotations(3);
		assertEquals(0, second.disposals);
	}

	@Test
	public void testCanceledFutureCannotRestoreHiddenMinings() {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		install(provider);
		CompletableFuture<List<? extends ICodeMining>> stale= provider.pending.getFirst();
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		viewer.addCodeMiningFilter(hide);
		stale.complete(provider.createMinings());
		assertMiningsDisposedExactlyOnce(provider);
		assertEquals(0, annotationCount());
		provider.pending.getLast().complete(provider.createMinings());
		assertEquals(0, annotationCount());
		assertMiningsDisposedExactlyOnce(provider);
		viewer.removeCodeMiningFilter(hide);
		provider.pending.getLast().complete(provider.createMinings());
		waitForAnnotations(3);
	}

	@Test
	public void testDisposalCancelsPendingRequestAndDisposesItsMinings() {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		install(provider);
		shell.dispose();
		provider.pending.getFirst().complete(provider.createMinings());
		assertEquals(1, provider.disposals);
		assertMiningsDisposedExactlyOnce(provider);
	}

	@Test
	public void testStaleFilteredRequestCannotRemoveRestoredMinings() {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		install(provider);
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		viewer.addCodeMiningFilter(hide);
		CompletableFuture<List<? extends ICodeMining>> filtered= provider.pending.getLast();
		viewer.removeCodeMiningFilter(hide);
		provider.pending.getLast().complete(provider.createMinings());
		waitForAnnotations(3);
		CompletableFuture.runAsync(() -> filtered.complete(provider.createMinings())).join();
		assertEquals(3, annotationCount());
		assertEquals(1, provider.created.getLast().disposals);
		provider.pending.getFirst().complete(provider.createMinings());
		assertEquals(3, annotationCount());
		assertEquals(1, provider.created.getLast().disposals);
	}

	@Test
	public void testProviderReplacementCancelsPendingRequestWhileFiltering() {
		TestProvider first= new TestProvider();
		first.delayed= true;
		Predicate<ICodeMining> headers= mining -> mining instanceof LineHeaderCodeMining;
		viewer.addCodeMiningFilter(headers);
		install(first);
		TestProvider second= new TestProvider();
		install(second);
		waitForAnnotations(1);
		first.pending.getFirst().complete(first.createMinings());
		assertMiningsDisposedExactlyOnce(first);
		assertEquals(1, first.disposals);
		assertEquals(1, annotationCount());
		viewer.removeCodeMiningFilter(headers);
		waitForAnnotations(3);
		assertEquals(0, second.disposals);
	}

	@Test
	public void testFilteringReleasesOffscreenReservedHeight() {
		viewer.getDocument().set("line\n".repeat(200));
		TestProvider provider= new TestProvider();
		provider.headerLine= 150;
		install(provider);
		viewer.getTextWidget().setTopIndex(150);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return viewer.getTextWidget().getLineVerticalIndent(150) > 0;
			}
		}.waitForCondition(shell.getDisplay(), 3000));
		viewer.getTextWidget().setTopIndex(0);
		viewer.addCodeMiningFilter(mining -> mining.getProvider() != provider);
		waitForAnnotations(0);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return viewer.getTextWidget().getLineVerticalIndent(150) == 0;
			}
		}.waitForCondition(shell.getDisplay(), 3000));
	}

	@Test
	public void testFilterChangesRetainAcceptedAnnotationsUntilRefreshCompletes() {
		TestProvider provider= new TestProvider();
		install(provider);
		waitForAnnotations(3);
		CountingHeaderMining original= provider.created.getFirst();
		Annotation header= null;
		for (Iterator<Annotation> annotations= model.getAnnotationIterator(); annotations.hasNext();) {
			Annotation candidate= annotations.next();
			if (candidate instanceof LineHeaderAnnotation) {
				header= candidate;
			}
		}
		assertTrue(header != null);
		provider.delayed= true;
		Predicate<ICodeMining> headers= mining -> mining instanceof LineHeaderCodeMining;
		viewer.addCodeMiningFilter(headers);
		assertEquals(1, annotationCount());
		assertTrue(model.getPosition(header) != null, "accepted header must not be removed during refresh");
		assertEquals(0, original.disposals);
		viewer.removeCodeMiningFilter(headers);
		assertEquals(1, annotationCount());
		assertTrue(model.getPosition(header) != null);
		assertEquals(0, original.disposals);
		provider.pending.getLast().complete(provider.createMinings());
		waitForAnnotations(3);
		assertEquals(1, original.disposals);
	}

	@Test
	public void testFilterRejectsCachedMiningsForAllAnnotationTypes() {
		TestProvider original= new TestProvider();
		install(original);
		waitForAnnotations(3);
		TestProvider replacement= new TestProvider();
		List<ICodeMining> replacements= replacement.createMinings();
		int annotationsUpdated= 0;
		for (Iterator<Annotation> annotations= model.getAnnotationIterator(); annotations.hasNext();) {
			if (annotations.next() instanceof ICodeMiningAnnotation miningAnnotation) {
				ICodeMining current= miningAnnotation.getMinings().getFirst();
				ICodeMining next= replacements.stream()
						.filter(mining -> mining.getPosition().equals(current.getPosition())).findFirst().orElseThrow();
				((AbstractCodeMining) next).setLabel(null);
				miningAnnotation.update(List.of(next), new NullProgressMonitor());
				assertSame(replacement, miningAnnotation.getMinings().getFirst().getProvider());
				assertSame(original, miningAnnotation.getCachedMinings()[0].getProvider());
				annotationsUpdated++;
			}
		}
		assertEquals(3, annotationsUpdated);
		original.delayed= true;
		viewer.addCodeMiningFilter(mining -> mining.getProvider() == replacement);
		assertEquals(0, annotationCount(), "annotations with rejected cached content must disappear immediately");
		assertMiningsDisposedExactlyOnce(original);
		assertMiningsDisposedExactlyOnce(replacement);
	}

	@Test
	public void testFilterRegisteredFromModelCallbackSeesNewAnnotations() {
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		AtomicBoolean registered= new AtomicBoolean();
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		IAnnotationModelListener listener= annotationModel -> {
			if (annotationCount() > 0 && registered.compareAndSet(false, true)) {
				viewer.addCodeMiningFilter(hide);
			}
		};
		model.addAnnotationModelListener(listener);
		try {
			install(provider);
			provider.pending.getFirst().complete(provider.createMinings());
			assertTrue(registered.get());
			assertEquals(0, annotationCount(), "callback filter must remove the just-installed annotations");
			assertMiningsDisposedExactlyOnce(provider);
			provider.pending.getLast().complete(provider.createMinings());
			assertEquals(0, annotationCount());
			assertMiningsDisposedExactlyOnce(provider);
			viewer.removeCodeMiningFilter(hide);
			provider.pending.getLast().complete(provider.createMinings());
			waitForAnnotations(3);
			viewer.addCodeMiningFilter(hide);
			assertEquals(0, annotationCount(), "nested update must not leave stale support/manager state");
		} finally {
			model.removeAnnotationModelListener(listener);
		}
	}

	@Test
	public void testNonBatchCallbackDoesNotAbandonPendingRemovals() {
		IAnnotationModel nonBatchModel= (IAnnotationModel) Proxy.newProxyInstance(
				IAnnotationModel.class.getClassLoader(), new Class<?>[] { IAnnotationModel.class },
				(proxy, method, arguments) -> {
					try {
						return method.invoke(model, arguments);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				});
		viewer.setDocument(viewer.getDocument(), nonBatchModel);
		TestProvider provider= new TestProvider();
		provider.delayed= true;
		install(provider);
		provider.pending.getFirst().complete(provider.createMinings());
		waitForAnnotations(3);
		AtomicBoolean registered= new AtomicBoolean();
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		IAnnotationModelListener listener= annotationModel -> {
			if (annotationCount() == 2 && registered.compareAndSet(false, true)) {
				viewer.addCodeMiningFilter(hide);
			}
		};
		model.addAnnotationModelListener(listener);
		try {
			viewer.updateCodeMinings();
			provider.pending.getLast().complete(List.of());
			assertTrue(registered.get());
			assertEquals(0, annotationCount(), "superseded non-batch removals must still finish");
			assertMiningsDisposedExactlyOnce(provider);
			provider.pending.getLast().complete(List.of());
			viewer.updateCodeMinings();
			provider.pending.getLast().complete(List.of());
			assertEquals(0, annotationCount(), "no orphan may survive later empty refreshes");
			assertMiningsDisposedExactlyOnce(provider);
			viewer.removeCodeMiningFilter(hide);
			provider.pending.getLast().complete(provider.createMinings());
			waitForAnnotations(3);
		} finally {
			model.removeAnnotationModelListener(listener);
		}
	}

	@Test
	public void testFilteringReleasesInlineGlyphMetrics() {
		TestProvider provider= new TestProvider();
		install(provider);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				var style= viewer.getTextWidget().getStyleRangeAtOffset(0);
				return style != null && style.metrics != null;
			}
		}.waitForCondition(shell.getDisplay(), 3000));
		viewer.addCodeMiningFilter(mining -> !(mining instanceof LineContentCodeMining));
		waitForAnnotations(2);
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				var style= viewer.getTextWidget().getStyleRangeAtOffset(0);
				return style == null || style.metrics == null;
			}
		}.waitForCondition(shell.getDisplay(), 3000));
	}

	@Test
	public void testFilterDoesNotAffectAnotherViewer() {
		TestProvider provider= new TestProvider();
		install(provider);
		SourceViewer otherViewer= new SourceViewer(shell, null, SWT.NONE);
		AnnotationModel otherModel= new AnnotationModel();
		otherViewer.setDocument(new Document(viewer.getDocument().get()), otherModel);
		AnnotationPainter painter= new AnnotationPainter(otherViewer, null);
		otherViewer.setCodeMiningAnnotationPainter(painter);
		otherViewer.addPainter(painter);
		otherViewer.setCodeMiningProviders(new ICodeMiningProvider[] { new TestProvider() });
		assertEquals(3, annotationCount(otherModel));
		Predicate<ICodeMining> hide= mining -> mining.getProvider() != provider;
		viewer.addCodeMiningFilter(hide);
		waitForAnnotations(0);
		assertEquals(3, annotationCount(otherModel));
		viewer.removeCodeMiningFilter(hide);
		waitForAnnotations(3);
		assertEquals(3, annotationCount(otherModel));
	}

	private void install(TestProvider provider) {
		viewer.setCodeMiningProviders(new ICodeMiningProvider[] { provider });
	}

	private void assertMiningsDisposedExactlyOnce(TestProvider provider) {
		assertTrue(provider.created.stream().allMatch(mining -> mining.disposals == 1));
		assertTrue(provider.otherDisposals.stream().allMatch(disposals -> disposals.get() == 1));
	}

	private void waitForAnnotations(int count) {
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return annotationCount() == count;
			}
		}.waitForCondition(shell.getDisplay(), 3000), "Expected " + count + " mining annotations");
	}

	private int annotationCount() {
		return annotationCount(model);
	}

	private int annotationCount(AnnotationModel annotationModel) {
		int count= 0;
		for (Iterator<Annotation> annotations= annotationModel.getAnnotationIterator(); annotations.hasNext();) {
			if (annotations.next() instanceof AbstractInlinedAnnotation) {
				count++;
			}
		}
		return count;
	}

	private final class TestProvider extends AbstractCodeMiningProvider {
		private final IDocument document= viewer.getDocument();
		private int disposals;
		private int requests;
		private int headerLine= 1;
		private boolean delayed;
		private boolean includeNullEntries;
		private RuntimeException headerDisposalFailure;
		private final List<CountingHeaderMining> created= new ArrayList<>();
		private final List<AtomicInteger> otherDisposals= new ArrayList<>();
		private final List<CompletableFuture<List<? extends ICodeMining>>> pending= new ArrayList<>();

		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer textViewer, IProgressMonitor monitor) {
			requests++;
			if (!delayed) {
				return CompletableFuture.completedFuture(createMinings());
			}
			CompletableFuture<List<? extends ICodeMining>> future= new CompletableFuture<>();
			pending.add(future);
			return future;
		}

		private List<ICodeMining> createMinings() {
			try {
				CountingHeaderMining header= new CountingHeaderMining(this);
				created.add(header);
				AtomicInteger inlineDisposals= new AtomicInteger();
				otherDisposals.add(inlineDisposals);
				LineContentCodeMining inline= new LineContentCodeMining(new Position(0, 1), this) {
					@Override
					public void dispose() {
						inlineDisposals.incrementAndGet();
						super.dispose();
					}
				};
				inline.setLabel("inline");
				AtomicInteger footerDisposals= new AtomicInteger();
				otherDisposals.add(footerDisposals);
				DocumentFooterCodeMining footer= new DocumentFooterCodeMining(document, this, null) {
					@Override
					public void dispose() {
						footerDisposals.incrementAndGet();
						super.dispose();
					}
				};
				footer.setLabel("footer");
				List<ICodeMining> minings= new ArrayList<>(List.of(header, inline, footer));
				if (includeNullEntries) {
					minings.addFirst(null);
					minings.add(2, null);
					minings.addLast(null);
				}
				return minings;
			} catch (BadLocationException e) {
				throw new AssertionError(e);
			}
		}

		@Override
		public void dispose() {
			disposals++;
		}
	}

	private static final class EmptyProvider extends AbstractCodeMiningProvider {
		private final boolean nullList;

		private EmptyProvider(boolean nullList) {
			this.nullList= nullList;
		}

		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer,
				IProgressMonitor monitor) {
			return nullList ? CompletableFuture.completedFuture(null) : null;
		}
	}

	private final class CountingHeaderMining extends LineHeaderCodeMining {
		private int disposals;
		private final RuntimeException disposalFailure;

		private CountingHeaderMining(TestProvider provider) throws BadLocationException {
			super(provider.headerLine, provider.document, provider);
			disposalFailure= provider.headerDisposalFailure;
			setLabel("header");
		}

		@Override
		public void dispose() {
			disposals++;
			super.dispose();
			if (disposalFailure != null) {
				throw disposalFailure;
			}
		}
	}
}
