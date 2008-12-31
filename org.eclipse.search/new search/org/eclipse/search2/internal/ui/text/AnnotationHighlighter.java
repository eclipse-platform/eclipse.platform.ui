/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.search2.internal.ui.text;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
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
import org.eclipse.search.ui.text.Match;

import org.eclipse.search2.internal.ui.InternalSearchUI;
import org.eclipse.search2.internal.ui.SearchMessages;


public class AnnotationHighlighter extends Highlighter {
	private IAnnotationModel fModel;
	private IDocument fDocument;
	private Map fMatchesToAnnotations;

	public AnnotationHighlighter(IAnnotationModel model, IDocument document) {
		fModel= model;
		fDocument= document;
		fMatchesToAnnotations= new HashMap();
	}

	public void addHighlights(Match[] matches) {
		HashMap map= new HashMap(matches.length);
		for (int i= 0; i < matches.length; i++) {
			int offset= matches[i].getOffset();
			int length= matches[i].getLength();
			if (offset >= 0 && length >= 0) {
				Position position= createPosition(matches[i]);
				if (position != null) {
					Annotation annotation= matches[i].isFiltered()
						? new Annotation(SearchPlugin.FILTERED_SEARCH_ANNOTATION_TYPE, true, null)
						: new Annotation(SearchPlugin.SEARCH_ANNOTATION_TYPE, true, null);
					fMatchesToAnnotations.put(matches[i], annotation);
					map.put(annotation, position);
				}
			}
		}
		addAnnotations(map);

	}

	private Position createPosition(Match match) {
		Position position= InternalSearchUI.getInstance().getPositionTracker().getCurrentPosition(match);
		if (position == null)
			position= new Position(match.getOffset(), match.getLength());
		else
			// need to clone position, can't have it twice in a document.
			position= new Position(position.getOffset(), position.getLength());
		if (match.getBaseUnit() == Match.UNIT_LINE) {
			if (fDocument != null) {
				try {
					position= PositionTracker.convertToCharacterPosition(position, fDocument);
				} catch (BadLocationException e) {
					// ignore, match must be outdated
					return null;
				}
			} else {
				SearchPlugin.log(new Status(IStatus.ERROR, SearchPlugin.getID(), 0, SearchMessages.AnnotationHighlighter_error_noDocument, null));
				return null;
			}
		}
		return position;
	}

	public void removeHighlights(Match[] matches) {
		HashSet annotations= new HashSet(matches.length);
		for (int i= 0; i < matches.length; i++) {
			Annotation annotation= (Annotation) fMatchesToAnnotations.remove(matches[i]);
			if (annotation != null) {
				annotations.add(annotation);
			}
		}
		removeAnnotations(annotations);
	}

	public  void removeAll() {
		Collection matchSet= fMatchesToAnnotations.values();
		removeAnnotations(matchSet);
		fMatchesToAnnotations.clear();
	}

	private void addAnnotations(Map annotationToPositionMap) {
		if (fModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) fModel;
			ame.replaceAnnotations(new Annotation[0], annotationToPositionMap);
		} else {
			for (Iterator elements= annotationToPositionMap.keySet().iterator(); elements.hasNext();) {
				Annotation element= (Annotation) elements.next();
				Position p= (Position) annotationToPositionMap.get(element);
				fModel.addAnnotation(element, p);
			}
		}
	}

	/**
	 * Removes annotations from the given annotation model. The default implementation works for editors that
	 * implement <code>ITextEditor</code>.
	 * Subclasses may override this method.
	 * @param annotations A set containing the annotations to be removed.
	 * 			 @see Annotation
	 */
	private void removeAnnotations(Collection annotations) {
		if (fModel instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension ame= (IAnnotationModelExtension) fModel;
			Annotation[] annotationArray= new Annotation[annotations.size()];
			ame.replaceAnnotations((Annotation[]) annotations.toArray(annotationArray), Collections.EMPTY_MAP);
		} else {
			for (Iterator iter= annotations.iterator(); iter.hasNext();) {
				Annotation element= (Annotation) iter.next();
				fModel.removeAnnotation(element);
			}
		}
	}

	protected void handleContentReplaced(IFileBuffer buffer) {
		if (!(buffer instanceof ITextFileBuffer))
			return;

		ITextFileBuffer textBuffer= (ITextFileBuffer) buffer;
		if (fDocument != null && fDocument.equals(textBuffer.getDocument())) {
			Set allMatches= fMatchesToAnnotations.keySet();
			Match[] matchesCopy= (Match[]) allMatches.toArray(new Match[allMatches.size()]);
			removeAll();
			addHighlights(matchesCopy);
		}
	}
}
