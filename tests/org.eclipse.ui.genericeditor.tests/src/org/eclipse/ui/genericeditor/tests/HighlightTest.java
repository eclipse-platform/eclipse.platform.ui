/*******************************************************************************
 * Copyright (c) 2017, 2025 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Lucas Bullen (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.genericeditor.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.resources.IStorage;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.genericeditor.tests.contributions.EnabledPropertyTester;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.tests.harness.util.DisplayHelper;

import org.eclipse.ui.texteditor.IDocumentProvider;

public class HighlightTest extends AbstratGenericEditorTest {

	private static final String ANNOTATION_TYPE= "org.eclipse.ui.genericeditor.text"; //$NON-NLS-1$

	private static final String EDITOR_TEXT= "Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit.\n" +
			"Lorem ipsum dolor sit amet, consectetur adipiscing elit.";

	@Override
	protected void createAndOpenFile() throws Exception {
		//leave editor creation to individual tests
	}

	@Test
	public void testCustomHighlightReconciler() throws Exception {
		createAndOpenFile("bar.txt", "bar 'bar'");

		checkHighlightForCaretOffset(0, "'bar'", 1);
	}

	@Test
	public void testCustomHighlightReconcilerForFileFromHistory() throws Exception {
		createAndOpenFile("bar.txt", "bar 'bar'", () -> new FileEditorInput(file) {
			@Override
			public String getName() {
				// append a revision number
				return super.getName() + " 61e418fdac6";
			}

			@SuppressWarnings("unchecked")
			@Override
			public <T> T getAdapter(Class<T> adapter) {
				// adapt to IStorage as in FileRevisionEditorInput
				return adapter == IStorage.class ? (T) getStorage() : super.getAdapter(adapter);
			}
		});

		checkHighlightForCaretOffset(0, "'bar'", 1);
	}

	@Test
	public void testEnabledWhenCustomHighlightReconciler() throws Exception {
		EnabledPropertyTester.setEnabled(true);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		checkHighlightForCaretOffset(0, "'bar'", 1);
		cleanFileAndEditor();

		EnabledPropertyTester.setEnabled(false);
		createAndOpenFile("enabledWhen.txt", "bar 'bar'");
		checkHighlightForCaretOffset(0, "'bar'", 0);
	}

	@Test
	public void testHighlightWordAtDocumentStart() throws Exception {
		createAndOpenFile("foo.txt", EDITOR_TEXT);

		checkHighlightForCaretOffset(0, "Lorem", 3);
	}

	@Test
	public void testHighlightWordWithNonLetterParts() throws Exception {
		String complexWord= "dolor_sit123amet45";
		String editorText= EDITOR_TEXT.replaceFirst("dolor sit amet", complexWord).replaceFirst("dolor sit amet", complexWord);
		createAndOpenFile("foo.txt", editorText);

		checkHighlightForCaretOffset(editorText.indexOf("dolor") + 3, complexWord, 2);
		checkHighlightForCaretOffset(editorText.indexOf("_sit") + 3, complexWord, 2);
		checkHighlightForCaretOffset(editorText.indexOf("123") + 1, complexWord, 2);
		checkHighlightForCaretOffset(editorText.indexOf("amet") + 1, complexWord, 2);
	}

	@Test
	public void testHighlightSimpleWordNotMatchingWordPart() throws Exception {
		String complexWord= "dolor_sit123amet45";
		String editorText= EDITOR_TEXT.replaceFirst("dolor sit amet", complexWord);
		createAndOpenFile("foo.txt", editorText);

		checkHighlightForCaretOffset(editorText.indexOf("dolor ") + 1, "dolor", 2);
		checkHighlightForCaretOffset(editorText.indexOf(" sit") + 1, "sit", 2);
		checkHighlightForCaretOffset(editorText.indexOf(" amet") + 1, "amet", 2);
	}

	@Test
	public void testHighlightNonAsciiCharacters() throws Exception {
		String complexWord= "sit\u00f6\u00f6amet";
		String editorText= EDITOR_TEXT.replaceFirst("sit amet", complexWord).replaceFirst("sit amet", complexWord);
		createAndOpenFile("foo.txt", editorText);

		checkHighlightForCaretOffset(editorText.indexOf("sit") + 1, complexWord, 2);
		checkHighlightForCaretOffset(editorText.indexOf("\u00f6") + 1, complexWord, 2);
		checkHighlightForCaretOffset(editorText.indexOf("amet") + 1, complexWord, 2);
	}

	private void checkHighlightForCaretOffset(int pos, String expectedHighlight, int expectedHighlightCount) throws Exception {
		clearAnnotations();

		editor.selectAndReveal(pos, 0);
		DisplayHelper.waitForCondition(Display.getDefault(), 2000,
				() -> getAnnotationsFromAnnotationModel().size() == expectedHighlightCount);

		List<Annotation> annotations= getAnnotationsFromAnnotationModel();

		IAnnotationModel annotationModel= getAnnotationModel();
		IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
		for (int i= 0; i < annotations.size(); i++) {
			Annotation annotation= annotations.get(i);
			Position position= annotationModel.getPosition(annotation);
			String highlight= document.get(position.offset, position.length);
			assertEquals("Wrong highlight " + i + " at position " + position.offset, expectedHighlight, highlight);
		}
		Assert.assertEquals("Wrong number of highlights", expectedHighlightCount, annotations.size());
	}

	private void clearAnnotations() {
		editor.selectAndReveal(0, 0);
		IAnnotationModel annotationModel= getAnnotationModel();
		List<Annotation> annotations= getAnnotationsFromAnnotationModel();
		for (Annotation annotation : annotations) {
			annotationModel.removeAnnotation(annotation);
		}
	}

	private IAnnotationModel getAnnotationModel() {
		IDocumentProvider dp= editor.getDocumentProvider();
		IAnnotationModel am= dp.getAnnotationModel(editor.getEditorInput());
		return am;
	}

	private List<Annotation> getAnnotationsFromAnnotationModel() {
		List<Annotation> annotationList= new ArrayList<>();
		Iterator<Annotation> annotationIterator= getAnnotationModel().getAnnotationIterator();
		while (annotationIterator.hasNext()) {
			Annotation ann= annotationIterator.next();
			if (ann.getType().indexOf(ANNOTATION_TYPE) > -1) {
				annotationList.add(ann);
			}
		}
		return annotationList;
	}
}
