/*
 */

package org.eclipse.search2.internal.ui.text;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

import org.eclipse.ui.editors.text.EditorsUI;

import org.eclipse.ui.IEditorPart;
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
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();
	private ITextEditor fEditor;
	private IWorkbenchWindow fWindow;
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
		if (e instanceof MatchEvent) {
			MatchEvent me= (MatchEvent) e;
			if (fEditor != null
					&& fResult.isShownInEditor(me.getMatch(), fEditor)) {
				if (me.getKind() == MatchEvent.ADDED) {
					addAnnotations(fEditor, new Match[]{me.getMatch()});
				} else {
					removeAnnotations(fEditor, new Match[]{me.getMatch()});
				}
			}
		} else if (e instanceof RemoveAllEvent)
			removeAnnotations();
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
		addAnnotations(fEditor, matches);
	}

	private void removeAnnotations() {
		ITextEditor editor= fEditor;
		if (editor == null)
			return;
		Set matchSet= fMatchesToAnnotations.keySet();
		Match[] matches= new Match[matchSet.size()];
		removeAnnotations(editor, (Match[]) matchSet.toArray(matches));
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		partActivated(part);
	}

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

	public void partDeactivated(IWorkbenchPart part) {
		//partClosed(part);
	}

	public void partOpened(IWorkbenchPart part) {
		// ignore, will be handled by activate
	}

	private void addAnnotations(IEditorPart editor, Match[] matches) {
		HashMap map= new HashMap(matches.length);
		for (int i= 0; i < matches.length; i++) {
			Annotation annotation= new Annotation(fAnnotationTypeLookup.getAnnotationType(SearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO), true, null);
			fMatchesToAnnotations.put(matches[i], annotation);
			map.put(annotation, new Position(matches[i].getOffset(), matches[i].getLength()));
		}
		fResult.addAnnotations(editor, map);
	}

	private void removeAnnotations(IEditorPart editor, Match[] matches) {
		HashSet annotations= new HashSet(matches.length);
		for (int i= 0; i < matches.length; i++) {
			Annotation annotation= (Annotation) fMatchesToAnnotations.remove(matches[i]);
			if (annotation != null) {
				annotations.add(annotation);
			}
		}
		fResult.removeAnnotations(editor, annotations);
	}


}
