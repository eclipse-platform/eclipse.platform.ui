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

package org.eclipse.search2.internal.ui.text;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.ISearchEditorAccess;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorAnnotationManager implements ISearchResultListener {
	private AbstractTextSearchResult fResult;
	private IEditorPart fEditor;
	private Highlighter fHighlighter;
	
	public static final int HIGHLLIGHTER_ANY= 0;
	public static final int HIGHLIGHTER_MARKER= 1;
	public static final int HIGHLIGHTER_ANNOTATION= 2;
	public static final int HIGHLIGHTER_EDITOR_ACCESS= 3;
	private static int fgHighlighterType= HIGHLLIGHTER_ANY;
	
	
	public EditorAnnotationManager(IEditorPart editorPart) {
		fEditor= editorPart;
		fHighlighter= createHighlighter(editorPart);

	}
	

	public static final void debugSetHighlighterType(int type) {
		fgHighlighterType= type;
	}


	void dispose() {
		removeAnnotations();
		if (fHighlighter != null)
			fHighlighter.dispose();
		if (fResult != null)
			fResult.removeListener(this);
	}


	public synchronized void setSearchResult(AbstractTextSearchResult result) {
		if (result == fResult)
			return;
		if (fResult != null) {
			removeAnnotations();
			fResult.removeListener(this);
		}
		fResult= result;
		if (fResult != null) {
			fResult.addListener(this);
			addAnnotations();
		}
	}

	public synchronized void searchResultChanged(SearchResultEvent e) {
		if (fResult == null)
			return;
		if (e instanceof MatchEvent) {
			MatchEvent me= (MatchEvent) e;
			Match[] matches = me.getMatches();
			int kind = me.getKind();
			for (int i = 0; i < matches.length; i++) {
				updateMatch(matches[i], kind);
			}
		} else if (e instanceof RemoveAllEvent)
			removeAnnotations();
	}

	private void updateMatch(Match match, int kind) {
		IEditorMatchAdapter adapter= fResult.getEditorMatchAdapter();
		if (fEditor != null && adapter != null && adapter.isShownInEditor(match, fEditor)) {
			if (kind == MatchEvent.ADDED) {
				addAnnotations(new Match[]{match});
			} else {
				removeAnnotations(new Match[]{match});
			}
		}
	}


	private void removeAnnotations() {
		if (fHighlighter != null)
			fHighlighter.removeAll();
	}

	private static Highlighter createHighlighter(IEditorPart editor) {
		if (fgHighlighterType != HIGHLLIGHTER_ANY) {
			return debugCreateHighlighter(editor);
		}
		ISearchEditorAccess access= (ISearchEditorAccess) editor.getAdapter(ISearchEditorAccess.class);
		if (access != null)
			return new EditorAccessHighlighter(access);
		IAnnotationModel model= getAnnotationModel(editor);
		if (model != null)
			return new AnnotationHighlighter(model, getDocument(editor));
		IEditorInput input= editor.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file= ((IFileEditorInput)input).getFile();
			if (file != null)
				return new MarkerHighlighter(file);
		}
		return null;
	}

	private static Highlighter debugCreateHighlighter(IEditorPart editor) {
		if (fgHighlighterType == HIGHLIGHTER_ANNOTATION) {
			IAnnotationModel model= getAnnotationModel(editor);
			if (model != null)
				return new AnnotationHighlighter(model, getDocument(editor));		
		} else if (fgHighlighterType == HIGHLIGHTER_MARKER) {
			IEditorInput input= editor.getEditorInput();
			if (input instanceof IFileEditorInput) {
				IFile file= ((IFileEditorInput)input).getFile();
				if (file != null)
					return new MarkerHighlighter(file);
			}
			
		} else if (fgHighlighterType == HIGHLIGHTER_EDITOR_ACCESS) {
			ISearchEditorAccess access= (ISearchEditorAccess) editor.getAdapter(ISearchEditorAccess.class);
			if (access != null)
				return new EditorAccessHighlighter(access);
		}
		return null;
	}

	private void addAnnotations() {
		if (fResult == null)
			return;
		IEditorMatchAdapter matchAdapter= fResult.getEditorMatchAdapter();
		if (matchAdapter == null)
			return;
		Match[] matches= matchAdapter.computeContainedMatches(fResult, fEditor);
		if (matches == null)
			return;
		addAnnotations(matches);
	}

	private void addAnnotations(Match[] matches) {
		if (fHighlighter != null)
			fHighlighter.addHighlights(matches);
	}

	private void removeAnnotations(Match[] matches) {
		if (fHighlighter != null)
			fHighlighter.removeHighlights(matches);
	}



	private static IAnnotationModel getAnnotationModel(IWorkbenchPart part) {
		IAnnotationModel model= null;
		model= (IAnnotationModel) part.getAdapter(IAnnotationModel.class); 
		if (model == null) {
			ITextEditor textEditor= null;
			if (part instanceof ITextEditor) {
				textEditor= (ITextEditor) part; 
			}
			if (textEditor != null) {
				IDocumentProvider dp= textEditor.getDocumentProvider();
				if (dp != null)
					model= dp.getAnnotationModel(textEditor.getEditorInput());
			}
		}
		return model;
	}

	private static IDocument getDocument(IWorkbenchPart part) {
		IDocument doc= null;
		doc= (IDocument) part.getAdapter(IDocument.class); 
		if (doc == null) {
			ITextEditor textEditor= null;
			if (part instanceof ITextEditor) {
				textEditor= (ITextEditor) part; 
			}
			if (textEditor != null) {
				IDocumentProvider dp= textEditor.getDocumentProvider();
				if (dp != null)
					doc= dp.getDocument(textEditor.getEditorInput());
			}
		}
		return doc;
	}
}
