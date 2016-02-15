/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.SearchTestPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;
import org.eclipse.search2.internal.ui.text.PositionTracker;

public class LineAnnotationManagerTest {

	private LineBasedFileSearch fLineQuery;
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

	@ClassRule
	public static JUnitSourceSetup fgJUnitSource= new JUnitSourceSetup();

	@Before
	public void setUp() throws Exception {
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLIGHTER_ANNOTATION);
		
		String[] fileNamePatterns= { "*.java" };
		FileTextSearchScope scope= FileTextSearchScope.newWorkspaceScope(fileNamePatterns, false);
	
		fLineQuery= new LineBasedFileSearch(scope, false, true, "Test");
	}
	
	@After
	public void tearDown() throws Exception {
		InternalSearchUI.getInstance().removeAllQueries();
		fLineQuery= null;
		
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLLIGHTER_ANY);
	}
	
	@Test		
	public void testLineBasedQuery() throws Exception {
		NewSearchUI.runQueryInForeground(null, fLineQuery);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fLineQuery.getSearchResult();
		Object[] files= result.getElements();
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor)SearchTestPlugin.openTextEditor(SearchPlugin.getActivePage(), file);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
				annotationModel.getAnnotationIterator();
				ArrayList<Position> positions= new ArrayList<>();
				for (Iterator<Annotation> iter= annotationModel.getAnnotationIterator(); iter.hasNext();) {
					Annotation annotation= iter.next();
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
