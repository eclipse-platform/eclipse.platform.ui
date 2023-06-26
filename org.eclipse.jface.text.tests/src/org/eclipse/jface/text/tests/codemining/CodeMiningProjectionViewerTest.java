/**
 *  Copyright (c) 2019 Red Hat Inc., and others
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *  - Mickael Istria (Red Hat Inc.)
 */
package org.eclipse.jface.text.tests.codemining;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.codemining.ICodeMining;
import org.eclipse.jface.text.codemining.ICodeMiningProvider;
import org.eclipse.jface.text.codemining.LineContentCodeMining;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.source.AnnotationPainter;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.text.tests.contentassist.BarContentAssistProcessor;
import org.eclipse.jface.text.tests.source.inlined.LineContentBoundsDrawingTest.AccessAllAnnoations;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class CodeMiningProjectionViewerTest {

	private static final class RepeatLettersCodeMiningProvider implements ICodeMiningProvider {
		@Override
		public CompletableFuture<List<? extends ICodeMining>> provideCodeMinings(ITextViewer viewer, IProgressMonitor monitor) {
			List<LineContentCodeMining> codeMinings = new ArrayList<>();
			for (int i = 0; i < viewer.getDocument().getLength(); i++) {
				try {
					char c= viewer.getDocument().getChar(i);
					if (Character.isLetter(c)) {
						codeMinings.add(new StaticContentLineCodeMining(i, c, this));
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			}
			return CompletableFuture.completedFuture(codeMinings);
		}

		@Override
		public void dispose() {
		}
	}

	private Shell fParent;
	private ProjectionViewer fViewer;

	@Before
	public void setUp() {
		Shell[] shells= Display.getDefault().getShells();
		for (Shell shell : shells) {
			shell.dispose();
		}
		fParent= new Shell(SWT.ON_TOP);
		fParent.setSize(500, 200);
		fParent.setLayout(new FillLayout());
		fViewer= new ProjectionViewer(fParent, null, null, false, SWT.NONE);
		IAnnotationAccess annotationAccess = new AccessAllAnnoations();
		// code minings
		AnnotationPainter painter = new AnnotationPainter(fViewer, annotationAccess);
		fViewer.addPainter(painter);
		fViewer.setCodeMiningAnnotationPainter(painter);
		// projection/folding
		fViewer.setDocument(new Document(), new ProjectionAnnotationModel());
		ProjectionSupport projectionSupport = new ProjectionSupport(fViewer, annotationAccess, new ISharedTextColors() {
			@Override
			public Color getColor(RGB rgb) {
				return null;
			}
		});
		projectionSupport.install();
		fViewer.doOperation(ProjectionViewer.TOGGLE);
	}

	@After
	public void tearDown() {
		fParent.dispose();
	}

	protected List<Shell> getCurrentShells() {
		return Arrays.stream(fParent.getDisplay().getShells())
				.filter(Shell::isVisible)
				.toList();
	}

	protected List<Shell> findNewShells(Collection<Shell> beforeShells) {
		return Arrays.stream(fParent.getDisplay().getShells())
				.filter(Shell::isVisible)
				.filter(shell -> !beforeShells.contains(shell))
				.toList();
	}

	protected Shell findNewShell(Collection<Shell> beforeShells) {
		DisplayHelper.sleep(fParent.getDisplay(), 100);
		List<Shell> afterShells= findNewShells(beforeShells);
		if (afterShells.isEmpty()) {
			DisplayHelper.sleep(fParent.getDisplay(), 1000);
		}
		afterShells= findNewShells(beforeShells);
		assertTrue("No new shell found, existing: " + beforeShells, afterShells.size() > beforeShells.size());
		return afterShells.get(0);
	}

	@Test
	public void testCollapse() throws Exception {
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] {
			new RepeatLettersCodeMiningProvider()
		});
		fViewer.getDocument().set("1a\n2a\n3a\n4a\n5a\n6a\n");
		ProjectionAnnotation annotation= new ProjectionAnnotation(true);
		fViewer.getProjectionAnnotationModel().addAnnotation(annotation, new Position(0, fViewer.getDocument().getLineOffset(4)));
		fViewer.doOperation(ProjectionViewer.COLLAPSE_ALL);
		fViewer.updateCodeMinings();
		fParent.open();

		Bundle bundle = Platform.getBundle("org.eclipse.ui.workbench");
		ILog log = null;
		AtomicReference<IStatus> logError = new AtomicReference<>();
		ILogListener logListener= (status, plugin) -> {
			logError.set(status);
		};
		if (bundle != null && bundle.getState() == Bundle.ACTIVE) {
			log = ILog.of(bundle);
			log.addLogListener(logListener);
		}
		try {
			// without workbench, next line throws Exception directly
			DisplayHelper.sleep(fParent.getDisplay(), 1000);
			Assert.assertNull(logError.get());
		} finally {
			if (log != null) {
				log.removeLogListener(logListener);
			}
		}
	}

	@Test
	public void testCodeMiningDoesntAlterFocus() {
		fViewer.configure(new SourceViewerConfiguration() {
			@Override
			public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
				ContentAssistant contentAssistant= new ContentAssistant(true);
				contentAssistant.addContentAssistProcessor(new BarContentAssistProcessor("1hi"), IDocument.DEFAULT_CONTENT_TYPE);
				contentAssistant.addContentAssistProcessor(new BarContentAssistProcessor("1hello"), IDocument.DEFAULT_CONTENT_TYPE);
				contentAssistant.setShowEmptyList(true);
				return contentAssistant;
			}
		});
		final List<Shell> beforeShells= getCurrentShells();
		fViewer.setCodeMiningProviders(new ICodeMiningProvider[] {
				new DelayedEchoCodeMiningProvider()
		});
		fViewer.getDocument().set("1a\n2a\n3a\n4a\n5a\n6a\n");
		fParent.setSize(200, 4 * fViewer.getTextWidget().getLineHeight());
		//fParent.pack(true);
		fParent.open();
		DisplayHelper.driveEventQueue(fParent.getDisplay());
		// ensure ViewportGuard is initialized
		fViewer.getControl().notifyListeners(SWT.KeyUp, new Event());
		fViewer.setSelectedRange(1, 0);
		fViewer.doOperation(ISourceViewer.CONTENTASSIST_PROPOSALS);
		Display display= fParent.getDisplay();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return display.getShells().length > beforeShells.size();
			}
		}.waitForCondition(display, 1000));
		Shell completionShell= findNewShell(beforeShells);
		// ↓ showing codeminings changing viewport while completion popup is open
		fViewer.updateCodeMinings();
		assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return fViewer.getTextWidget().getLineVerticalIndent(0) > 0;
			}
		}.waitForCondition(display, 3000));
		Event e= new Event();
		e.widget= fViewer.getTextWidget();
		e.keyCode= SWT.ARROW_DOWN;
		fViewer.getTextWidget().notifyListeners(SWT.KeyDown, e);
		assertTrue(completionShell.isVisible());
	}
}
