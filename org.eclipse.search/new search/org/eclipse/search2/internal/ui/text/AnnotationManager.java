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

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;

public class AnnotationManager implements ISearchResultListener, IPartListener {

	private AbstractTextSearchResult fResult;
	private Map fMatchesToAnnotations;
	private ITextEditor fEditor;
	private IWorkbenchWindow fWindow;
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();
	
	private static HashMap fSearchResultMap;
	private static AnnotationManager fgManager;
	
	static {
		fSearchResultMap= new HashMap();
		fgManager= new AnnotationManager();
		IWindowListener listener= new IWindowListener() {
			
			public void windowActivated(IWorkbenchWindow window) {
				switchedTo(window);
			}

			public void windowDeactivated(IWorkbenchWindow window) {
				// ignore
			}

			public void windowClosed(IWorkbenchWindow window) {
				fSearchResultMap.remove(window);
			}

			public void windowOpened(IWorkbenchWindow window) {
				// ignore
			}
		};
		PlatformUI.getWorkbench().addWindowListener(listener);
	}
	
	public static void searchResultActivated(IWorkbenchWindow window, AbstractTextSearchResult result) {
		fSearchResultMap.put(window, result);
		switchedTo(window);
	}
	
	public static void switchedTo(IWorkbenchWindow window) {
		fgManager.setWindow(window);
		AbstractTextSearchResult result= (AbstractTextSearchResult) fSearchResultMap.get(window);
		fgManager.setSearchResult(result);
	}
	
	public AnnotationManager() {
		fMatchesToAnnotations= new HashMap();
	}

	public synchronized void setSearchResult(AbstractTextSearchResult result) {
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
	public synchronized void searchResultChanged(SearchResultEvent e) {
		if (e instanceof MatchEvent) {
			MatchEvent me= (MatchEvent) e;
			if (fEditor != null && fResult.isShownInEditor(me.getMatch(), fEditor)) {
				if (me.getKind() == MatchEvent.ADDED) {
					addAnnotation(fEditor, me.getMatch());
				} else {
					removeAnnotation(fEditor, me.getMatch());
				}
			}
		} else if (e instanceof RemoveAllEvent)
			removeAnnotations();
	}


	private void addAnnotation(ITextEditor textEditor, Match match) {
		IAnnotationModel model= textEditor.getDocumentProvider().getAnnotationModel(textEditor.getEditorInput());
		if (model != null) {
			Annotation annotation= new Annotation(fAnnotationTypeLookup.getAnnotationType(SearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO), true, null);
			fMatchesToAnnotations.put(match, annotation);
			model.addAnnotation(annotation, new Position(match.getOffset(), match.getLength()));
		}
	}

	private void removeAnnotation(ITextEditor textEditor, Match match) {
		Annotation annotation= (Annotation) fMatchesToAnnotations.remove(match);
		if (annotation != null) {
			IAnnotationModel model= textEditor.getDocumentProvider().getAnnotationModel(textEditor.getEditorInput());
			if (model != null) {
				model.removeAnnotation(annotation);
			}
		}
		
	}

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
		Match[] matches= fResult.findContainedMatches(fEditor);
		if (matches == null)
			return;
		for (int i= 0; i < matches.length; i++) {
			addAnnotation(fEditor, matches[i]);
		}
	}

	private void removeAnnotations() {
		ITextEditor editor= fEditor;
		if (editor == null)
			return;
		Set matches= new HashSet(); 
		matches.addAll(fMatchesToAnnotations.keySet());
		for (Iterator annotations= matches.iterator(); annotations.hasNext();) {
			removeAnnotation(editor, (Match) annotations.next());
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
	
	public void setWindow(IWorkbenchWindow window) {
		if (fWindow != null)
			fWindow.getPartService().removePartListener(AnnotationManager.this);
		fWindow= window;
		fWindow.getPartService().addPartListener(this);
		partActivated(window.getActivePage().getActiveEditor());
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
