/*
 */

package org.eclipse.search2.internal.ui.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.Match;
import org.eclipse.search.ui.text.MatchEvent;
import org.eclipse.search.ui.text.RemoveAllEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class AnnotationManager implements ISearchResultListener, IPartListener {
	private AbstractTextSearchResult fResult;
	private Map fMatchesToAnnotations;
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();
	private IEditorPart fEditor;
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
				addAnnotations(fEditor, new Match[]{match});
			} else {
				removeAnnotations(fEditor, new Match[]{match});
			}
		}
	}

	public synchronized void partActivated(IWorkbenchPart part) {
		
		if (part instanceof IEditorPart && part != fEditor) {
			if (fResult != null)
				removeAnnotations();
			fEditor= (IEditorPart) part;
			addAnnotations();
		}
	}

	private void addAnnotations() {
		if (fEditor == null || fResult == null)
			return;
		IEditorMatchAdapter matchAdapter= fResult.getEditorMatchAdapter();
		if (matchAdapter == null)
			return;
		Match[] matches= matchAdapter.computeContainedMatches(fResult, fEditor);
		if (matches == null)
			return;
		addAnnotations(fEditor, matches);
	}

	private void removeAnnotations() {
		IEditorPart editor= fEditor;
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
		IEditorPart editor= window.getActivePage().getActiveEditor();
		if (editor != null)
			partActivated(editor);
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
			int offset= matches[i].getOffset();
			int length= matches[i].getLength();
			if (offset >= 0 && length >= 0) {
				Annotation annotation= new Annotation(fAnnotationTypeLookup.getAnnotationType(SearchUI.SEARCH_MARKER, IMarker.SEVERITY_INFO), true, null);
				fMatchesToAnnotations.put(matches[i], annotation);
				map.put(annotation, new Position(matches[i].getOffset(), matches[i].getLength()));
			}
		}
		addAnnotations(editor, map);
	}

	private void removeAnnotations(IEditorPart editor, Match[] matches) {
		HashSet annotations= new HashSet(matches.length);
		for (int i= 0; i < matches.length; i++) {
			Annotation annotation= (Annotation) fMatchesToAnnotations.remove(matches[i]);
			if (annotation != null) {
				annotations.add(annotation);
			}
		}
		removeAnnotations(editor, annotations);
	}

	/**
	 * Removes annotations from the given editor. The default implementation works for editors that
	 * implement <code>ITextEditor</code>.
	 * Subclasses may override this method. 
	 * @param editor
	 * @param annotations A set containing the annotations to be removed.
	 * 			 @see Annotation
	 */
	private void removeAnnotations(IWorkbenchPart editor, Set annotations) {
		IAnnotationModel model= getAnnotationModel(editor);
		if (model == null)
			return;
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) model;
			Annotation[] annotationArray= new Annotation[annotations.size()];
			ame.replaceAnnotations((Annotation[]) annotations.toArray(annotationArray), Collections.EMPTY_MAP);
		} else {
			for (Iterator iter= annotations.iterator(); iter.hasNext();) {
				Annotation element= (Annotation) iter.next();
				model.removeAnnotation(element);
			}
		}
	}

	/**
	 * Adds annotations to the given editor. The default implementation works for editors that
	 * implement <code>ITextEditor</code>.
	 * Subclasses may override this method. 
	 * @param editor
	 * @param annotationToPositionMap A map containing annotations as keys and Positions as values.
	 * 			 @see Annotation
	 * 			 @see Position
	 */
	private void addAnnotations(IWorkbenchPart editor, Map annotationToPositionMap) {
		IAnnotationModel model= getAnnotationModel(editor);
		if (model == null) {
			return;
		}
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) model;
			ame.replaceAnnotations(new Annotation[0], annotationToPositionMap);
		} else {
			for (Iterator elements= annotationToPositionMap.keySet().iterator(); elements.hasNext();) {
				Annotation element= (Annotation) elements.next();
				Position p= (Position) annotationToPositionMap.get(element);
				model.addAnnotation(element, p);
			}
		}
	}

	private IAnnotationModel getAnnotationModel(IWorkbenchPart part) {
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

}
