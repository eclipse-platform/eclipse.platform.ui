/*******************************************************************************
 * Copyright (c) 2022 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.projection.IProjectionPosition;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org.eclipse.jface.text.source.projection.ProjectionViewer;

public class ProjectionViewerTest {

	/**
	 * A {@link ProjectionViewer} that provides access to {@link #getVisibleDocument()}.
	 */
	private final class TestProjectionViewer extends ProjectionViewer {
		private TestProjectionViewer(Composite parent, IVerticalRuler ruler, IOverviewRuler overviewRuler, boolean showsAnnotationOverview, int styles) {
			super(parent, ruler, overviewRuler, showsAnnotationOverview, styles);
		}

		@Override
		public IDocument getVisibleDocument() {
			return super.getVisibleDocument();
		}
	}

	private static final class ProjectionPosition extends Position implements IProjectionPosition {

		public ProjectionPosition(IDocument document) {
			super(0, document.getLength());
		}

		@Override
		public IRegion[] computeProjectionRegions(IDocument document) throws BadLocationException {
			int firstNewLine= document.get().indexOf('\n');
			int secondNewLine= document.get().indexOf('\n', firstNewLine + 1);
			return new IRegion[] { new Region(0, firstNewLine + 1), new Region(secondNewLine + 1, document.getLength() - secondNewLine - 1) };
		}

		@Override
		public int computeCaptionOffset(IDocument document) throws BadLocationException {
			return document.get().indexOf('\n') + 1;
		}

	}

	@Test
	public void testCopyPaste() {
		Shell shell = new Shell();
		shell.setLayout(new FillLayout());
		ProjectionViewer viewer = new ProjectionViewer(shell, null, null, false, SWT.NONE);
		Document document= new Document("/*\n * content\n */");
		viewer.setDocument(document, new AnnotationModel());
		viewer.enableProjection();
		viewer.getProjectionAnnotationModel().addAnnotation(new ProjectionAnnotation(false), new ProjectionPosition(document));
		shell.setVisible(true);
		viewer.getTextOperationTarget().doOperation(ProjectionViewer.COLLAPSE_ALL);
		viewer.getTextOperationTarget().doOperation(ITextOperationTarget.SELECT_ALL);
		try {
			assertEquals(document.get(), ((ITextSelection) viewer.getSelection()).getText());
			viewer.getTextOperationTarget().doOperation(ITextOperationTarget.COPY);
			assertEquals(document.get(), new Clipboard(viewer.getTextWidget().getDisplay()).getContents(TextTransfer.getInstance()));
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testVisibleRegionDoesNotChangeWithProjections() {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		ProjectionViewer viewer= new ProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World
				123
				456
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int regionLength= documentContent.indexOf('\n');
		viewer.setVisibleRegion(0, regionLength);
		viewer.enableProjection();
		viewer.getProjectionAnnotationModel().addAnnotation(new ProjectionAnnotation(false), new ProjectionPosition(document));
		shell.setVisible(true);
		try {
			assertEquals(0, viewer.getVisibleRegion().getOffset());
			assertEquals(regionLength, viewer.getVisibleRegion().getLength());

			viewer.getTextOperationTarget().doOperation(ProjectionViewer.COLLAPSE_ALL);
			assertEquals(0, viewer.getVisibleRegion().getOffset());
			assertEquals(regionLength, viewer.getVisibleRegion().getLength());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testVisibleRegionProjectionCannotBeExpanded() {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World
				123
				456
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int secondLineStart= documentContent.indexOf("World");
		int secondLineEnd= documentContent.indexOf('\n', secondLineStart);
		viewer.setVisibleRegion(secondLineStart, secondLineEnd - secondLineStart);
		viewer.enableProjection();
		shell.setVisible(true);
		try {
			assertEquals("World", viewer.getVisibleDocument().get());
			viewer.getTextOperationTarget().doOperation(ProjectionViewer.EXPAND_ALL);
			assertEquals("World", viewer.getVisibleDocument().get());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testVisibleRegionAddsProjectionAnnotationsIfProjectionsEnabled() {
		testProjectionAnnotationsFromVisibleRegion(true);
	}

	@Test
	public void testEnableProjectionAddsProjectionAnnotationsIfVisibleRegionEnabled() {
		testProjectionAnnotationsFromVisibleRegion(false);
	}

	private void testProjectionAnnotationsFromVisibleRegion(boolean enableProjectionFirst) {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World
				123
				456
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int secondLineStart= documentContent.indexOf("World");
		int secondLineEnd= documentContent.indexOf('\n', secondLineStart);

		shell.setVisible(true);
		if (enableProjectionFirst) {
			viewer.enableProjection();
			viewer.setVisibleRegion(secondLineStart, secondLineEnd - secondLineStart);
		} else {
			viewer.setVisibleRegion(secondLineStart, secondLineEnd - secondLineStart);
			viewer.enableProjection();
		}

		boolean startAnnotationFound= false;
		boolean endAnnotationFound= false;
		int annotationCount= 0;

		try {
			assertEquals("World", viewer.getVisibleDocument().get().trim());

			for (Iterator<Annotation> it= viewer.getProjectionAnnotationModel().getAnnotationIterator(); it.hasNext();) {
				Annotation annotation= it.next();
				assertEquals("org.eclipse.jface.text.source.projection.ProjectionViewer.InvisibleCollapsedProjectionAnnotation", annotation.getClass().getCanonicalName());
				Position position= viewer.getProjectionAnnotationModel().getPosition(annotation);

				if (position.getOffset() == 0) {
					assertEquals("org.eclipse.jface.text.source.projection.ProjectionViewer.ExactRegionProjectionPosition", position.getClass().getCanonicalName());
					assertEquals("Hello\n".length(), position.getLength());
					startAnnotationFound= true;
				} else {
					assertEquals(secondLineEnd + 1, position.getOffset());
					assertEquals(8, position.getLength());
					endAnnotationFound= true;
				}
				annotationCount++;
			}
			assertEquals(2, annotationCount);
			assertTrue(startAnnotationFound);
			assertTrue(endAnnotationFound);
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testInsertIntoVisibleRegion() throws BadLocationException {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World
				123
				456
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int secondLineStart= documentContent.indexOf("World");
		int secondLineEnd= documentContent.indexOf('\n', secondLineStart);

		shell.setVisible(true);

		try {
			viewer.setVisibleRegion(secondLineStart, secondLineEnd - secondLineStart);
			viewer.enableProjection();

			assertEquals("World", viewer.getVisibleDocument().get());

			viewer.getDocument().replace(documentContent.indexOf("rld"), 0, "---");

			assertEquals("Wo---rld", viewer.getVisibleDocument().get());
		} finally {
			shell.dispose();
		}
	}

}
