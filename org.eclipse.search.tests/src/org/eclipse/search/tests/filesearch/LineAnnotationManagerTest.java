/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
import org.eclipse.search.internal.core.text.TextSearchScope;
import org.eclipse.search.tests.SearchTestPlugin;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;
import org.eclipse.search2.internal.ui.text.PositionTracker;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

public class LineAnnotationManagerTest extends TestCase {
	LineBasedFileSearch fLineQuery;

	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

	public LineAnnotationManagerTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		TestSuite suite= new TestSuite();
		
		suite.addTest(new JUnitSetup(new AnnotationManagerSetup(new TestSuite(LineAnnotationManagerTest.class), EditorAnnotationManager.HIGHLIGHTER_ANNOTATION)));
		return suite;
	}
	
	public static Test suite() {
		return allTests();
	}

	protected void setUp() throws Exception {
		super.setUp();
		
		TextSearchScope scope= TextSearchScope.newWorkspaceScope();
		scope.addExtension("*.java");
	
		fLineQuery= new LineBasedFileSearch(scope,  "", "Test");
	}
	
	protected void tearDown() throws Exception {
		InternalSearchUI.getInstance().removeAllQueries();
		fLineQuery= null;
		super.tearDown();
	}
	
	public void testLineBasedQuery() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fLineQuery);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fLineQuery.getSearchResult();
		Object[] files= result.getElements();
		for (int i= 0; i < files.length; i++) {
			IFile file= (IFile) files[0];
			ITextEditor editor= (ITextEditor)IDE.openEditor(SearchTestPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage(), file, true);
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
				assertTrue("position not found at: "+j, positions.remove(position));
			}
			assertEquals(0, positions.size());
		
		}
	}

	private Position computeDocumentPositionFromLineMatch(IDocument document, Match match) throws BadLocationException {
		Position p= new Position(match.getOffset(), match.getLength());
		return PositionTracker.convertToCharacterPosition(p, document);
	}

}
