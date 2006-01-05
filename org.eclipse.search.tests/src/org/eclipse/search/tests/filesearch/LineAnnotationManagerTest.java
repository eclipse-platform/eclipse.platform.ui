/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import java.util.HashSet;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search.internal.core.text.FileNamePatternSearchScope;
import org.eclipse.search.internal.ui.SearchPlugin;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;
import org.eclipse.search2.internal.ui.text.PositionTracker;

public class LineAnnotationManagerTest extends TestCase {

	private LineBasedFileSearch fLineQuery;
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

	public LineAnnotationManagerTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return setUpTest(new TestSuite(LineAnnotationManagerTest.class));
	}
	
	public static Test suite() {
		return allTests();
	}
	
	public static Test setUpTest(Test test) {
		return new JUnitSourceSetup(test);
	}

	protected void setUp() throws Exception {
		super.setUp();
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLIGHTER_ANNOTATION);
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newWorkspaceScope(false);
		scope.addFileNamePattern("*.java");
	
		fLineQuery= new LineBasedFileSearch(scope,  "", "Test");
	}
	
	protected void tearDown() throws Exception {
		InternalSearchUI.getInstance().removeAllQueries();
		fLineQuery= null;
		
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLLIGHTER_ANY);
		super.tearDown();
	}
	
	public void testLineBasedQuery() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fLineQuery);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fLineQuery.getSearchResult();
		Object[] files= result.getElements();
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor) IDE.openEditor(SearchPlugin.getActivePage(), file, true);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
				annotationModel.getAnnotationIterator();
				HashSet positions= new HashSet();
				for (Iterator iter= annotationModel.getAnnotationIterator(); iter.hasNext();) {
					Annotation annotation= (Annotation) iter.next();
					if (annotation.getType().equals(fAnnotationTypeLookup.getAnnotationType(NewSearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO))) {
						positions.add(annotationModel.getPosition(annotation));
					}
				}

				Match[] matches= result.getMatches(file);
				for (int j= 0; j < matches.length; j++) {

					Position position= computeDocumentPositionFromLineMatch(document, matches[j]);
					assertTrue("position not found at: " + j, positions.remove(position));
				}
				assertEquals(0, positions.size());

			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	private Position computeDocumentPositionFromLineMatch(IDocument document, Match match) throws BadLocationException {
		Position p= new Position(match.getOffset(), match.getLength());
		return PositionTracker.convertToCharacterPosition(p, document);
	}

}
