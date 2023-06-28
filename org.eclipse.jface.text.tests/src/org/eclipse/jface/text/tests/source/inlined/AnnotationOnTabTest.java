/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests.source.inlined;

import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.resource.JFaceResources;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.inlined.InlinedAnnotationSupport;
import org.eclipse.jface.text.source.inlined.LineContentAnnotation;
import org.eclipse.jface.text.tests.source.inlined.LineContentBoundsDrawingTest.AccessAllAnnoations;
import org.eclipse.jface.text.tests.source.inlined.LineContentBoundsDrawingTest.TestAnnotationPainter;
import org.eclipse.jface.text.tests.util.DisplayHelper;

public class AnnotationOnTabTest {

	private Shell fParent;

	@Before
	public void setUp() {
		fParent= new Shell();
	}

	@After
	public void tearDown() {
		fParent.dispose();
		fParent = null;
	}

	@Test
	public void testTextBoundsMatchPaintedArea() {
		fParent.setLayout(new FillLayout());

		// Create source viewer and initialize the content
		ISourceViewer sourceViewer = new SourceViewer(fParent,null,
				SWT.V_SCROLL | SWT.BORDER);
		sourceViewer.setDocument(new Document("\t\treference\n\t\tannotated"), new AnnotationModel());
		StyledText textWidget= sourceViewer.getTextWidget();
		textWidget.setFont(JFaceResources.getTextFont());

		// Initialize inlined annotations support
		InlinedAnnotationSupport support = new InlinedAnnotationSupport();
		IAnnotationAccess annotationAccess = new AccessAllAnnoations();
		TestAnnotationPainter painter = new TestAnnotationPainter(sourceViewer, annotationAccess);
		((ITextViewerExtension2) sourceViewer).addPainter(painter);
		support.install(sourceViewer, painter);

		// add annotations
		int annotationIndex = sourceViewer.getDocument().get().indexOf("annotated");
		LineContentAnnotation annotation= new LineContentAnnotation(new Position(annotationIndex, 1), sourceViewer);
		annotation.setText("a"); // single char, so overall annoation is 3 chars, less than default 4 chars
		support.updateAnnotations(Collections.singleton(annotation));
		fParent.open();
		Assert.assertTrue(new DisplayHelper() {
			@Override
			protected boolean condition() {
				return textWidget.isVisible() && painter.wasPainted();
			}
		}.waitForCondition(textWidget.getDisplay(), 2000));
		DisplayHelper.sleep(textWidget.getDisplay(), 1000);
		int referenceIndex = textWidget.getText().indexOf("reference");
		Rectangle referenceBounds = textWidget.getTextBounds(referenceIndex, referenceIndex);
		Rectangle annotatedCharactedBounds = textWidget.getTextBounds(annotationIndex, annotationIndex);
		Assert.assertTrue("Annotation didn't shift target character to the right, it most likely replaced the tab instead of expanding it", referenceBounds.x < annotatedCharactedBounds.x);
	}
}
