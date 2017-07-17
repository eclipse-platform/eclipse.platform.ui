/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBuffer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

import org.eclipse.search.internal.ui.SearchPlugin;
import org.eclipse.search.ui.text.ISearchEditorAccess;
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;


public class EditorAccessHighlighter extends Highlighter {
	private ISearchEditorAccess fEditorAcess;
	private Map<Match, Annotation> fMatchesToAnnotations;

	public EditorAccessHighlighter(ISearchEditorAccess editorAccess) {
		fEditorAcess= editorAccess;
		fMatchesToAnnotations= new HashMap<>();
	}

	@Override
	public void addHighlights(Match[] matches) {
		Map<IAnnotationModel, HashMap<Annotation, Position>> mapsByAnnotationModel= new HashMap<>();
		for (Match match : matches) {
			int offset= match.getOffset();
			int length= match.getLength();
			if (offset >= 0 && length >= 0) {
				try {
					Position position= createPosition(match);
					if (position != null) {
						Map<Annotation, Position> map= getMap(mapsByAnnotationModel, match);
						if (map != null) {
							Annotation annotation= match.isFiltered()
							? new Annotation(SearchPlugin.FILTERED_SEARCH_ANNOTATION_TYPE, true, null)
							: new Annotation(SearchPlugin.SEARCH_ANNOTATION_TYPE, true, null);
							fMatchesToAnnotations.put(match, annotation);
							map.put(annotation, position);
						}
					}
				} catch (BadLocationException e) {
					SearchPlugin.log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, SearchMessages.EditorAccessHighlighter_error_badLocation, e));
				}
			}
		}
		for (Entry<IAnnotationModel, HashMap<Annotation, Position>> entry : mapsByAnnotationModel.entrySet()) {
			addAnnotations(entry.getKey(), entry.getValue());
		}

	}

	private Position createPosition(Match match) throws BadLocationException {
		Position position= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(match);
		if (position == null)
			position= new Position(match.getOffset(), match.getLength());
		else
			// need to clone position, can't have it twice in a document.
			position= new Position(position.getOffset(), position.getLength());
		if (match.getBaseUnit() == Match.UNIT_LINE) {
			IDocument doc= fEditorAcess.getDocument(match);
			if (doc != null) {
				position= PositionTracker.convertToCharacterPosition(position, doc);
			} else {
				SearchPlugin.log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, SearchMessages.AnnotationHighlighter_error_noDocument, null));
				return null;
			}
		}
		return position;
	}

	private Map<Annotation, Position> getMap(Map<IAnnotationModel, HashMap<Annotation, Position>> mapsByAnnotationModel, Match match) {
		IAnnotationModel model= fEditorAcess.getAnnotationModel(match);
		if (model == null)
			return null;
		HashMap<Annotation, Position> map= mapsByAnnotationModel.get(model);
		if (map == null) {
			map= new HashMap<>();
			mapsByAnnotationModel.put(model, map);
		}
		return map;
	}

	private Set<Annotation> getSet(Map<IAnnotationModel, HashSet<Annotation>> setsByAnnotationModel, Match match) {
		IAnnotationModel model= fEditorAcess.getAnnotationModel(match);
		if (model == null)
			return null;
		HashSet<Annotation> set= setsByAnnotationModel.get(model);
		if (set == null) {
			set= new HashSet<>();
			setsByAnnotationModel.put(model, set);
		}
		return set;
	}

	@Override
	public void removeHighlights(Match[] matches) {
		Map<IAnnotationModel, HashSet<Annotation>> setsByAnnotationModel= new HashMap<>();
		for (Match match : matches) {
			Annotation annotation= fMatchesToAnnotations.remove(match);
			if (annotation != null) {
				Set<Annotation> annotations= getSet(setsByAnnotationModel, match);
				if (annotations != null)
					annotations.add(annotation);
			}
		}

		for (IAnnotationModel model : setsByAnnotationModel.keySet()) {
			Set<Annotation> set= setsByAnnotationModel.get(model);
			removeAnnotations(model, set);
		}

	}

	private void addAnnotations(IAnnotationModel model, Map<Annotation, Position> annotationToPositionMap) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) model;
			ame.replaceAnnotations(new Annotation[0], annotationToPositionMap);
		} else {
			for (Annotation element : annotationToPositionMap.keySet()) {
				Position p= annotationToPositionMap.get(element);
				model.addAnnotation(element, p);
			}
		}
	}

	/*
	 * Removes annotations from the given annotation model. The default implementation works for editors that
	 * implement <code>ITextEditor</code>.
	 * Subclasses may override this method.
	 * @param annotations A set containing the annotations to be removed.
	 * 			 @see Annotation
	 */
	private void removeAnnotations(IAnnotationModel model, Set<Annotation> annotations) {
		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) model;
			Annotation[] annotationArray= new Annotation[annotations.size()];
			ame.replaceAnnotations(annotations.toArray(annotationArray), Collections.emptyMap());
		} else {
			for (Annotation element : annotations) {
				model.removeAnnotation(element);
			}
		}
	}

	@Override
	public  void removeAll() {
		Set<Match> matchSet= fMatchesToAnnotations.keySet();
		Match[] matches= new Match[matchSet.size()];
		removeHighlights(matchSet.toArray(matches));
	}

	@Override
	protected void handleContentReplaced(IFileBuffer buffer) {
		if (!(buffer instanceof ITextFileBuffer))
			return;
		IDocument document= null;
		ITextFileBuffer textBuffer= (ITextFileBuffer) buffer;
		for (Match match : fMatchesToAnnotations.keySet()) {
			document= fEditorAcess.getDocument(match);
			if (document != null)
				break;
		}

		if (document != null && document.equals(textBuffer.getDocument())) {
			Match[] matches= new Match[fMatchesToAnnotations.keySet().size()];
			fMatchesToAnnotations.keySet().toArray(matches);
			removeAll();
			addHighlights(matches);
		}
	}
}
