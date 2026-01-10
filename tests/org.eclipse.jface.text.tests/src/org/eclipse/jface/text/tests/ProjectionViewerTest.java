/*******************************************************************************
 * Copyright (c) 2022, 2026 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jface.text.tests;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
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

		try {
			assertEquals("World", viewer.getVisibleDocument().get().trim());
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

	@Test
	public void testRemoveVisibleRegionEnd() throws BadLocationException {
		testReplaceVisibleRegionEnd("");
	}

	@Test
	public void testReplaceVisibleRegionEnd() throws BadLocationException {
		testReplaceVisibleRegionEnd("---");
	}


	private void testReplaceVisibleRegionEnd(String toReplaceWith) throws BadLocationException {
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

			viewer.getDocument().replace(documentContent.indexOf("d\n1"), 3, toReplaceWith);

			assertEquals("Worl" + toReplaceWith, viewer.getVisibleDocument().get());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testRemoveVisibleRegionStart() throws BadLocationException {
		testReplaceVisibleRegionStart("");
	}

	@Test
	public void testReplaceVisibleRegionStart() throws BadLocationException {
		testReplaceVisibleRegionStart("---");
	}


	private void testReplaceVisibleRegionStart(String toReplaceWith) throws BadLocationException {
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

			viewer.getDocument().replace(documentContent.indexOf("o\nW"), 3, toReplaceWith);

			assertEquals(toReplaceWith + "orld", viewer.getVisibleDocument().get());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testVisibleRegionEndsWithWhitespace() {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World\t\t
				123
				456
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int secondLineStart= documentContent.indexOf("World");
		int secondLineTextEnd= documentContent.indexOf('\n', secondLineStart);
		int secondLineEnd= documentContent.indexOf('\n', secondLineStart);

		shell.setVisible(true);

		try {
			viewer.setVisibleRegion(secondLineStart, secondLineEnd - secondLineStart);
			viewer.enableProjection();

			assertEquals("World\t\t", viewer.getVisibleDocument().get());

			viewer.setVisibleRegion(secondLineStart, secondLineTextEnd - secondLineStart);

			assertEquals("World\t\t\n", viewer.getVisibleDocument().get());


		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testRemoveEntireVisibleRegion() throws BadLocationException {
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
			document.replace(secondLineStart, secondLineEnd - secondLineStart, "");
			assertEquals("", viewer.getVisibleDocument().get());
			assertEquals(new Region(secondLineStart, 0), viewer.getVisibleRegion());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testSetVisibleRegionDoesNotExpandOutsideProjectionRegions() {
		Shell shell= new Shell();
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, false, SWT.NONE);
		String documentContent= """
				Hello
				World
				abc
				123
				456
				789
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		viewer.enableProjection();
		ProjectionAnnotation firstAnnotation= new ProjectionAnnotation(true);
		ProjectionAnnotation secondAnnotation= new ProjectionAnnotation(true);
		viewer.getProjectionAnnotationModel().addAnnotation(firstAnnotation, new Position(0, documentContent.indexOf("World")));
		viewer.getProjectionAnnotationModel().addAnnotation(secondAnnotation, new Position(documentContent.indexOf("456"), documentContent.length() - documentContent.indexOf("456")));

		viewer.setVisibleRegion(documentContent.indexOf("abc"), documentContent.indexOf("123") - documentContent.indexOf("abc"));
		shell.setVisible(true);
		try {
			assertTrue(firstAnnotation.isCollapsed());
			assertTrue(secondAnnotation.isCollapsed());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testSetVisibleRegionExpandsBorderingProjectionRegions() {
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
		viewer.enableProjection();
		ProjectionAnnotation firstAnnotation= new ProjectionAnnotation(true);
		ProjectionAnnotation secondAnnotation= new ProjectionAnnotation(true);
		viewer.getProjectionAnnotationModel().addAnnotation(firstAnnotation, new Position(0, documentContent.indexOf("123")));
		viewer.getProjectionAnnotationModel().addAnnotation(secondAnnotation, new Position(documentContent.indexOf("123"), documentContent.length() - documentContent.indexOf("123")));

		viewer.setVisibleRegion(documentContent.indexOf("World"), documentContent.indexOf("456") - documentContent.indexOf("World"));
		shell.setVisible(true);
		try {
			assertFalse(firstAnnotation.isCollapsed());
			assertFalse(secondAnnotation.isCollapsed());
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testProjectionRegionsShownOnlyInVisibleRegion() {
		Shell shell= new Shell(Display.getCurrent());
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, true, SWT.ALL);
		String documentContent= """

				visible_region_start

				projection_start

				visible_region_end

				projection_end

				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		ProjectionAnnotation annotation= addVisibleRegionAndProjection(viewer, documentContent);
		try {
			assertEquals("""
					visible_region_start

					projection_start

					visible_region_end
					""", viewer.getVisibleDocument().get());

			annotation.paint(null, null, null); //should exit early and not throw NPE
		} finally {
			shell.dispose();
		}
	}

	@Test
	public void testProjectionRegionsShownWithinVisibleRegion() {
		Shell shell= new Shell(Display.getCurrent());
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, true, SWT.ALL);
		String documentContent= """

				visible_region_start

				projection_start

				projection_end

				visible_region_end

				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		ProjectionAnnotation annotation= addVisibleRegionAndProjection(viewer, documentContent);
		try {
			assertEquals("""
					visible_region_start

					projection_start

					projection_end

					visible_region_end
					""", viewer.getVisibleDocument().get());

			assertThrows(NullPointerException.class, () -> annotation.paint(null, null, null), "expected to run painting logic");
		} finally {
			shell.dispose();
		}
	}

	private ProjectionAnnotation addVisibleRegionAndProjection(TestProjectionViewer viewer, String documentContent) {
		int visibleRegionStart= documentContent.indexOf("visible_region_start");
		int visibleRegionEnd= documentContent.indexOf("\n", documentContent.indexOf("visible_region_end")) + 1;

		int projectionStart= documentContent.indexOf("projection_start");
		int projectionEnd= documentContent.indexOf("\n", documentContent.indexOf("projection_end")) + 1;

		viewer.setVisibleRegion(visibleRegionStart, visibleRegionEnd - visibleRegionStart);
		viewer.enableProjection();
		ProjectionAnnotation annotation= new ProjectionAnnotation();
		viewer.getProjectionAnnotationModel().addAnnotation(annotation, new Position(projectionStart, projectionEnd - projectionStart));
		return annotation;
	}

	@ParameterizedTest
	@CsvSource({ "true", "false" })
	void testDifferentLineEndings(boolean crlf) {
		Shell shell= new Shell(Display.getCurrent());
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, true, SWT.ALL);
		String documentContent= """
				// before
				{
					// within
				}
				// after
				""";
		if (crlf) {
			documentContent= documentContent.replace("\n", "\r\n");
		}
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int start= documentContent.indexOf('{');
		int end= documentContent.indexOf('}') + 1;
		viewer.enableProjection();
		viewer.setVisibleRegion(start, end - start);
		assertEquals(documentContent.substring(start, documentContent.indexOf("// after")), viewer.getVisibleDocument().get());
	}

	@Test
	void testIncludesLastLineIfAdditionalTextPresent() {
		Shell shell= new Shell(Display.getCurrent());
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, true, SWT.ALL);
		String documentContent= """
				// before
				{
					// within
				}// ...
				// should be hidden
				""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int start= documentContent.indexOf('{');
		int end= documentContent.indexOf('}') + 1;
		viewer.enableProjection();
		viewer.setVisibleRegion(start, end - start);
		assertEquals(documentContent.substring(start, documentContent.indexOf("// should be hidden")), viewer.getVisibleDocument().get());
	}

	@Test
	void testSetVisibleRegionUntilEOF() {
		Shell shell= new Shell(Display.getCurrent());
		shell.setLayout(new FillLayout());
		TestProjectionViewer viewer= new TestProjectionViewer(shell, null, null, true, SWT.ALL);
		String documentContent= """
				// before
				{
					// within
				}""";
		Document document= new Document(documentContent);
		viewer.setDocument(document, new AnnotationModel());
		int start= documentContent.indexOf('{');
		int end= documentContent.indexOf('}') + 1;
		viewer.enableProjection();
		viewer.setVisibleRegion(start, end - start);
		assertEquals(documentContent.substring(start), viewer.getVisibleDocument().get());
	}
}
