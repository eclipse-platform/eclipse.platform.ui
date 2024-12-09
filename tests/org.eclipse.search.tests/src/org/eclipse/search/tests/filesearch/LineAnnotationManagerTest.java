/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search.tests.filesearch;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.tests.SearchTestUtil;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.FileTextSearchScope;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;

public class LineAnnotationManagerTest {

	private LineBasedFileSearch fLineQuery;
	private final AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

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
			for (Object f : files) {
				IFile file= (IFile) f;
				ITextEditor editor= (ITextEditor)SearchTestUtil.openTextEditor(SearchPlugin.getActivePage(), file);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				annotationModel.getAnnotationIterator();
				ArrayList<Position> positions= new ArrayList<>();
				for (Iterator<Annotation> iter= annotationModel.getAnnotationIterator(); iter.hasNext();) {
					Annotation annotation= iter.next();
					if (annotation.getType().equals(fAnnotationTypeLookup.getAnnotationType(NewSearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO))) {
						positions.add(annotationModel.getPosition(annotation));
					}
				}
				for (Match match : result.getMatches(file)) {
					Position matchPosition= new Position(match.getOffset(), match.getLength());
					assertThat("no annotation found for match", positions, hasItem(matchPosition));
					positions.remove(matchPosition);
				}
				assertThat("annotations exist without matches", positions, empty());

			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

}
