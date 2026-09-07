/*******************************************************************************
 * Copyright (c) 2026 Vogella GmbH and others.
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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.AbstractCodeMiningProvider;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineHeaderCodeMining;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.LineHeaderAnnotation;
import org.eclipse.jface.text.source.inlined.Positions;

import org.eclipse.ui.tests.harness.util.DisplayHelper;

/**
 * Tests that code minings reappear after a client removed their annotation.
 */
public class CodeMiningAnnotationReattachTest {

	private static final String LABEL= "mining";

	private static final int LINE= 1;

	private Shell fShell;

	private SourceViewer fViewer;

	private AnnotationModel fAnnotationModel;

	@BeforeEach
	public void setUp() {
		fShell= new Shell(Display.getDefault());
		fShell.setSize(500, 200);
		fShell.setLayout(new FillLayout());
		fViewer= new SourceViewer(fShell, null, SWT.NONE);
		StyledText textWidget= fViewer.getTextWidget();
		fAnnotationModel= new AnnotationModel();
		fViewer.setDocument(new Document("line0\nline1\nline2"), fAnnotationModel);
		fShell.open();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().isVisible();
			}
		}.waitForCondition(textWidget.getDisplay(), 3000));
	}

	@AfterEach
	public void tearDown() {
		fViewer.setCodeMiningProviders(null);
		fShell.dispose();
		fShell= null;
		fViewer= null;
		fAnnotationModel= null;
	}

	@Test
	public void testSynchronousProviderReAddsAnnotationRemovedByClient() throws Exception {
		TestCodeMiningProvider provider= new TestCodeMiningProvider(true);
		install(provider);

		Position pos= miningPosition();
		LineHeaderAnnotation annotation= waitForAnnotation(pos);
		assertEquals(LABEL, annotation.getMinings().get(0).getLabel());

		// a client outside the framework drops the annotation
		fAnnotationModel.removeAnnotation(annotation);
		assertTrue(findAnnotations(pos).isEmpty());

		fViewer.updateCodeMinings();

		LineHeaderAnnotation reattached= waitForAnnotation(pos);
		assertEquals(LABEL, reattached.getMinings().get(0).getLabel());
	}

	@Test
	public void testAsynchronousProviderReAddsAnnotationRemovedByClient() throws Exception {
		TestCodeMiningProvider provider= new TestCodeMiningProvider(false);
		install(provider);
		provider.completePending();

		Position pos= miningPosition();
		LineHeaderAnnotation annotation= waitForAnnotation(pos);
		assertEquals(LABEL, annotation.getMinings().get(0).getLabel());

		fAnnotationModel.removeAnnotation(annotation);
		assertTrue(findAnnotations(pos).isEmpty());

		// the second request cancels the first one, so the first one never renders
		fViewer.updateCodeMinings();
		fViewer.updateCodeMinings();
		provider.completePending();

		LineHeaderAnnotation reattached= waitForAnnotation(pos);
		assertEquals(LABEL, reattached.getMinings().get(0).getLabel());
	}

	private void install(ICodeMiningProvider provider) {
		AnnotationPainter painter= new AnnotationPainter(fViewer, null);
		fViewer.setCodeMiningAnnotationPainter(painter);
		fViewer.addPainter(painter);
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] { provider });
	}

	private Position miningPosition() throws BadLocationException {
		return Positions.of(LINE, fViewer.getDocument(), true);
	}

	private LineHeaderAnnotation waitForAnnotation(Position pos) {
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return !findAnnotations(pos).isEmpty();
			}
		}.waitForCondition(fViewer.getTextWidget().getDisplay(), 3000), "No line header annotation at " + pos);
		List<LineHeaderAnnotation> annotations= findAnnotations(pos);
		assertEquals(1, annotations.size(), "Unexpected number of line header annotations at " + pos);
		return annotations.get(0);
	}

	private List<LineHeaderAnnotation> findAnnotations(Position pos) {
		List<LineHeaderAnnotation> result= new ArrayList<>();
		for (Iterator<Annotation> it= fAnnotationModel.getAnnotationIterator(); it.hasNext();) {
			Annotation annotation= it.next();
			if (annotation instanceof LineHeaderAnnotation lineHeader && pos.equals(fAnnotationModel.getPosition(annotation))) {
				result.add(lineHeader);
			}
		}
		return result;
	}

	/** Provides a single line header mining, synchronously or through futures the test completes. */
	private final class TestCodeMiningProvider extends AbstractCodeMiningProvider {

		private final boolean fSynchronous;

		private final List<CompletableFuture<List<? extends ICodeMining>>> fPending= new ArrayList<>();

		TestCodeMiningProvider(boolean synchronous) {
			fSynchronous= synchronous;
		}

		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
			if (fSynchronous) {
				return CompletableFuture.completedFuture(createMinings());
			}
			CompletableFuture<List<? extends ICodeMining>> future= new CompletableFuture<>();
			fPending.add(future);
			return future;
		}

		void completePending() {
			List<CompletableFuture<List<? extends ICodeMining>>> pending= new ArrayList<>(fPending);
			fPending.clear();
			pending.forEach(future -> future.complete(createMinings()));
		}

		private List<ICodeMining> createMinings() {
			try {
				LineHeaderCodeMining mining= new LineHeaderCodeMining(LINE, fViewer.getDocument(), this) {
					// no additional behavior
				};
				mining.setLabel(LABEL);
				return new ArrayList<>(List.of(mining));
			} catch (BadLocationException e) {
				throw new AssertionError(e);
			}
		}
	}
}
