/*
 * Created on 13.11.2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.ISearchResultChangedListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.ITextSearchResult;
import org.eclipse.search.ui.text.Match;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class AnnotationManager implements ISearchResultChangedListener, IPartListener {

	private ITextSearchResult fResult;
	private Map fMatchesToAnnotations;
	private ITextEditor fEditor;
	private AnnotationTypeLookup fAnnotationTypeLookup= new AnnotationTypeLookup();
	
	public AnnotationManager() {
		fMatchesToAnnotations= new HashMap();
		IWindowListener listener= new IWindowListener() {
			private IWorkbenchWindow fWindow;
			private void stopListeningToParts() {
				if (fWindow == null)
					return;
				fWindow.getPartService().removePartListener(AnnotationManager.this);
			}

			private void listenToParts() {
				if (fWindow == null)
					return;
				fWindow.getPartService().addPartListener(AnnotationManager.this);
			}
			
			public void windowActivated(IWorkbenchWindow window) {
				if (window != fWindow) {
					stopListeningToParts();
					fWindow= window;
					listenToParts();
				}
			}

			public void windowDeactivated(IWorkbenchWindow window) {
				if (window == fWindow) {
					stopListeningToParts();
					fWindow= null;
				}				
			}

			public void windowClosed(IWorkbenchWindow window) {
				windowDeactivated(window);
			}

			public void windowOpened(IWorkbenchWindow window) {
				// TODO Auto-generated method stub

			}
		};
		IWorkbenchWindow activeWindow= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWindow != null)
			listener.windowActivated(activeWindow);
		PlatformUI.getWorkbench().addWindowListener(listener);
	}
	
	public void setSearchResult(ITextSearchResult result) {
		if (result == fResult)
			return;
		removeAnnotations();
		if (fResult != null) {
			fResult.removeListener(this);
		}
		fResult= result;
		if (fResult != null) {
			fResult.addListener(this);
			addAnnotations();
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.search2.ui.ISearchResultChangedListener#searchResultsChanged(org.eclipse.search2.ui.SearchResultEvent)
	 */
	public synchronized void searchResultsChanged(SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent me= (MatchEvent) e;
			if (fEditor != null && fResult.getStructureProvider().isShownInEditor(me.getMatch(), fEditor)) {
				if (me.getKind() == MatchEvent.ADDED) {
					addAnnotation(fEditor, me.getMatch());
				} else {
					removeAnnotation(fEditor, me.getMatch());
				}
			}
		}
	}


	/**
	 * @param match
	 */
	private void addAnnotation(ITextEditor textEditor, Match match) {
		IAnnotationModel model= textEditor.getDocumentProvider().getAnnotationModel(textEditor.getEditorInput());
		if (model != null) {
			Annotation annotation= new Annotation(fAnnotationTypeLookup.getAnnotationType(SearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO), true, null);
			fMatchesToAnnotations.put(match, annotation);
			model.addAnnotation(annotation, new Position(match.getOffset(), match.getLength()));
		}
	}

	/**
	 * @param textEditor
	 * @param match
	 */
	private void removeAnnotation(ITextEditor textEditor, Match match) {
		Annotation annotation= (Annotation) fMatchesToAnnotations.remove(match);
		if (annotation != null) {
			IAnnotationModel model= textEditor.getDocumentProvider().getAnnotationModel(textEditor.getEditorInput());
			if (model != null) {
				model.removeAnnotation(annotation);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public synchronized void partActivated(IWorkbenchPart part) {
		if (part instanceof ITextEditor && part != fEditor) {
			removeAnnotations();
			fEditor= (ITextEditor) part;
			addAnnotations();
		}
	}
	
	private void addAnnotations() {
		if (fEditor == null || fResult == null)
			return;
		Match[] matches= fResult.getStructureProvider().findContainedMatches(fResult, fEditor.getEditorInput());
		if (matches == null)
			return;
		for (int i= 0; i < matches.length; i++) {
			addAnnotation(fEditor, matches[i]);
		}
	}

	private void removeAnnotations() {
		if (fEditor == null)
			return;
		Set matches= new HashSet(); 
		matches.addAll(fMatchesToAnnotations.keySet());
		for (Iterator annotations= matches.iterator(); annotations.hasNext();) {
			removeAnnotation(fEditor, (Match) annotations.next());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part == fEditor) {
			removeAnnotations();
			fEditor= null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated(IWorkbenchPart part) {
		//partClosed(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened(IWorkbenchPart part) {
		// ignore, will be handled by activate
	}
	
}
