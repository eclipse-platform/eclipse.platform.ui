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

package org.eclipse.ui.texteditor.spelling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

/**
 * Spelling problem collector.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
 * TODO Not yet thread aware.
 * <p>
 * Not yet for public use. API under construction.
 * </p>
 * 
 * @since 3.1
 */
public class DefaultSpellingProblemCollector implements ISpellingProblemCollector {

	/** Spelling annotation type */
	private static final String SPELLING_ANNOTATION_TYPE= "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$

	/** Annotation model */
	private IAnnotationModel fAnnotationModel;

	/** Annotations to add */
	private Map fAddAnnotations;

	/**
	 * Initializes this collector with the given annotation model.
	 * 
	 * @param annotationModel the annotation model
	 */
	public DefaultSpellingProblemCollector(IAnnotationModel annotationModel) {
		fAnnotationModel= annotationModel;
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#accept(org.eclipse.ui.texteditor.spelling.SpellingProblem)
	 */
	public void accept(SpellingProblem problem) {
		fAddAnnotations.put(new Annotation(SPELLING_ANNOTATION_TYPE, false, problem.getMessage()), new Position(problem.getOffset(), problem.getLength()));
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginReporting()
	 */
	public void beginReporting() {
		fAddAnnotations= new HashMap();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endReporting()
	 */
	public void endReporting() {
		List removeAnnotations= new ArrayList();
		for (Iterator iter= fAnnotationModel.getAnnotationIterator(); iter.hasNext();) {
			Annotation annotation= (Annotation) iter.next();
			if (SPELLING_ANNOTATION_TYPE.equals(annotation.getType()))
				removeAnnotations.add(annotation);
		}
		
		if (fAnnotationModel instanceof IAnnotationModelExtension)
			((IAnnotationModelExtension) fAnnotationModel).replaceAnnotations((Annotation[]) removeAnnotations.toArray(new Annotation[removeAnnotations.size()]), fAddAnnotations);
		else {
			for (Iterator iter= removeAnnotations.iterator(); iter.hasNext();)
				fAnnotationModel.removeAnnotation((Annotation) iter.next());
			for (Iterator iter= fAddAnnotations.keySet().iterator(); iter.hasNext();) {
				Annotation annotation= (Annotation) iter.next();
				fAnnotationModel.addAnnotation(annotation, (Position) fAddAnnotations.get(annotation));
			}
		}
		
		fAddAnnotations= null;
	}
}
