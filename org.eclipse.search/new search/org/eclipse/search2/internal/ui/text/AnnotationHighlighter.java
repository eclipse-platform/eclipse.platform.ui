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
import org.eclipse.search.ui.SearchUI;
import org.eclipse.search.ui.text.Match;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AnnotationTypeLookup;


public class AnnotationHighlighter extends Highlighter {
	private IAnnotationModel fModel;
	private Map fMatchesToAnnotations;
	private AnnotationTypeLookup fAnnotationTypeLookup= EditorsUI.getAnnotationTypeLookup();
	
	public AnnotationHighlighter(IAnnotationModel model) {
		fModel= model;
		fMatchesToAnnotations= new HashMap();
	}

	public void addHighlights(Match[] matches) {
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
		addAnnotations(map);
		
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
	private void removeAnnotations(Set annotations) {
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

	public  void removeAll() {
		Set matchSet= fMatchesToAnnotations.keySet();
		Match[] matches= new Match[matchSet.size()];
		removeHighlights((Match[]) matchSet.toArray(matches));
	}

}
