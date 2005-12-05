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
import org.eclipse.search.internal.ui.text.FileSearchQuery;
import org.eclipse.search.internal.ui.text.FileSearchResult;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.text.EditorAnnotationManager;

import org.eclipse.search.tests.SearchTestPlugin;

public class AnnotationManagerTest extends TestCase {
	private FileSearchQuery fQuery1;
	private FileSearchQuery fQuery2;

	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();

	public AnnotationManagerTest(String name) {
		super(name);
	}
		
	public static Test allTests() {
		return setUpTest(new TestSuite(AnnotationManagerTest.class));
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
		
		FileNamePatternSearchScope scope= FileNamePatternSearchScope.newWorkspaceScope();
		scope.addFileNamePattern("*.java"); //$NON-NLS-1$
		scope.setIncludeDerived(false);
		fQuery1= new FileSearchQuery(scope,  "", "Test");  //$NON-NLS-1$//$NON-NLS-2$
		fQuery2= new FileSearchQuery(scope, "", "TestCase"); //$NON-NLS-1$//$NON-NLS-2$
	} 
	
	protected void tearDown() throws Exception {
		InternalSearchUI.getInstance().removeAllQueries();
		fQuery1= null;
		fQuery2= null;
		
		EditorAnnotationManager.debugSetHighlighterType(EditorAnnotationManager.HIGHLLIGHTER_ANY);

		super.tearDown();
	}
	
	public void testAddAnnotation() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] files= result.getElements();
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor)IDE.openEditor(SearchPlugin.getActivePage(), file, true);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
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
					Position position= new Position(matches[j].getOffset(), matches[j].getLength());
					assertTrue("position not found at: "+j, positions.remove(position)); //$NON-NLS-1$
				}
				assertEquals(0, positions.size());
			
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}
	
	public void testBogusAnnotation() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		FileSearchResult result= (FileSearchResult) fQuery1.getSearchResult();
		IFile file= (IFile) result.getElements()[0];
		IDE.openEditor(SearchTestPlugin.getDefault().getWorkbench().getWorkbenchWindows()[0].getPages()[0], file, true);
		result.addMatch(new Match(file, -1, -1));
	}
	
	public void testRemoveQuery() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] files= result.getElements();
		InternalSearchUI.getInstance().removeQuery(fQuery1);
		
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor) IDE.openEditor(SearchPlugin.getActivePage(), file, true);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				int annotationCount= 0;
				for (Iterator annotations= annotationModel.getAnnotationIterator(); annotations.hasNext();) {
					Annotation annotation= (Annotation) annotations.next();
					if (annotation.getType().equals(fAnnotationTypeLookup.getAnnotationType(NewSearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO))) {
						annotationCount++;
					}
				}
				assertEquals(0, annotationCount);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	
	public void testReplaceQuery() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] files= result.getElements();
		NewSearchUI.runQueryInForeground(null, fQuery2);
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor) IDE.openEditor(SearchPlugin.getActivePage(), file, true);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				int annotationCount= 0;
				IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
				for (Iterator annotations= annotationModel.getAnnotationIterator(); annotations.hasNext();) {
					Annotation annotation= (Annotation) annotations.next();
					if (annotation.getType().equals(fAnnotationTypeLookup.getAnnotationType(NewSearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO))) {
						Position p= annotationModel.getPosition(annotation);
						String text= document.get(p.getOffset(), p.getLength());
						assertTrue(text.equalsIgnoreCase(fQuery2.getSearchString()));
					}
				}
				assertEquals(0, annotationCount);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

	public void testSwitchQuery() throws Exception {
		NewSearchUI.activateSearchResultView();
		NewSearchUI.runQueryInForeground(null, fQuery1);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fQuery1.getSearchResult();
		Object[] files= result.getElements();
		NewSearchUI.runQueryInForeground(null, fQuery2);
		SearchTestPlugin.getDefault().getSearchView().showSearchResult(result);
		try {
			for (int i= 0; i < files.length; i++) {
				IFile file= (IFile) files[0];
				ITextEditor editor= (ITextEditor) IDE.openEditor(SearchPlugin.getActivePage(), file, true);
				IAnnotationModel annotationModel= editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput());
				int annotationCount= 0;
				IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
				for (Iterator annotations= annotationModel.getAnnotationIterator(); annotations.hasNext();) {
					Annotation annotation= (Annotation) annotations.next();
					if (annotation.getType().equals(fAnnotationTypeLookup.getAnnotationType(NewSearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO))) {
						Position p= annotationModel.getPosition(annotation);
						String text= document.get(p.getOffset(), p.getLength());
						assertTrue(text.equalsIgnoreCase(fQuery1.getSearchString()));
					}
				}
				assertEquals(0, annotationCount);
			}
		} finally {
			SearchPlugin.getActivePage().closeAllEditors(false);
		}
	}

}
