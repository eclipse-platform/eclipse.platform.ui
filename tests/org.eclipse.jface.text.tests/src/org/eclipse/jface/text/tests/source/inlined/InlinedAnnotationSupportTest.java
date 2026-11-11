/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.source.inlined;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.AbstractInlinedAnnotation;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;
import org.eclipse.jface.text.tests.source.inlined.LineContentBoundsDrawingTest.AccessAllAnnoations;
import org.eclipse.jface.text.tests.source.inlined.LineContentBoundsDrawingTest.TestAnnotationPainter;

public class InlinedAnnotationSupportTest {

	/**
	 * Runs an action the first time the lock is taken after it was armed.
	 */
	private static class LockHookAnnotationModel extends AnnotationModel {
		Runnable onLock;

		@Override
		public Object getLockObject() {
			Runnable action= onLock;
			onLock= null;
			if (action != null) {
				action.run();
			}
			return super.getLockObject();
		}
	}

	private Shell fShell;
	private SourceViewer fViewer;
	private LockHookAnnotationModel fModel;
	private TestAnnotationPainter fPainter;
	private InlinedAnnotationSupport fSupport;

	@BeforeEach
	public void setUp() {
		fShell= new Shell();
		fViewer= new SourceViewer(fShell, null, SWT.NONE);
		fModel= new LockHookAnnotationModel();
		fViewer.setDocument(new Document("annotated"), fModel);
		fPainter= new TestAnnotationPainter(fViewer, new AccessAllAnnoations());
		fViewer.addPainter(fPainter);
		fSupport= new InlinedAnnotationSupport();
		fSupport.install(fViewer, fPainter);
	}

	@AfterEach
	public void tearDown() {
		fShell.dispose();
	}

	@Test
	public void testInvalidPositionsDoNotBlockBatchUpdate() {
		int documentLength= fViewer.getDocument().getLength();
		List<Position> invalidPositions= List.of(new Position(documentLength, 1),
				new Position(documentLength + 1, 0), new Position(1, Integer.MAX_VALUE));
		AtomicInteger changes= new AtomicInteger();
		fModel.addAnnotationModelListener(_ -> changes.incrementAndGet());
		for (Position position : invalidPositions) {
			LineContentAnnotation valid= new LineContentAnnotation(new Position(0, 1), fViewer);
			LineContentAnnotation invalid= new LineContentAnnotation(position, fViewer);
			changes.set(0);
			fSupport.updateAnnotations(new LinkedHashSet<>(List.of(invalid, valid)));
			assertEquals(valid.getPosition(), fModel.getPosition(valid));
			assertNull(fModel.getPosition(invalid));
			assertTrue(invalid.isMarkedDeleted());
			assertNull(fSupport.findExistingAnnotation(position));
			assertTrue(changes.get() > 0, "valid annotations did not notify the painter");
			fSupport.updateAnnotations(Collections.emptySet());
		}
	}

	@Test
	public void testInvalidExistingAnnotationIsRemoved() {
		LineContentAnnotation annotation= new LineContentAnnotation(new Position(0, 1), fViewer);
		fSupport.updateAnnotations(Collections.singleton(annotation));
		annotation.getPosition().length= Integer.MAX_VALUE;
		fSupport.updateAnnotations(Collections.singleton(annotation));
		assertNull(fModel.getPosition(annotation));
		assertTrue(annotation.isMarkedDeleted());
		assertFalse(fModel.getAnnotationIterator().hasNext());
	}

	@Test
	public void testZeroLengthPositionAtDocumentEndIsRetained() {
		AbstractInlinedAnnotation annotation= new LineContentAnnotation(new Position(fViewer.getDocument().getLength(), 0), fViewer);
		fSupport.updateAnnotations(Set.of(annotation));
		assertEquals(annotation.getPosition(), fModel.getPosition(annotation));
		assertFalse(annotation.isMarkedDeleted());
	}

	@Test
	public void testUpdateRacingUninstallAddsNoAnnotations() {
		fModel.onLock= fSupport::uninstall;
		fSupport.updateAnnotations(Collections.singleton(new LineContentAnnotation(new Position(0, 1), fViewer)));
		assertFalse(fModel.getAnnotationIterator().hasNext(), "update added annotations after uninstall");
	}

	@Test
	public void testUpdateAfterReinstallAddsAnnotations() {
		fSupport.uninstall();
		fSupport.install(fViewer, fPainter);
		fSupport.updateAnnotations(Collections.singleton(new LineContentAnnotation(new Position(0, 1), fViewer)));
		assertTrue(fModel.getAnnotationIterator().hasNext());
	}
}
