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
 * Default spelling problem collector, that manages spelling annotations
 * corresponding to spelling problems on an {@link IAnnotationModel}. If more
 * than one thread reports problems to this collector in parallel, only the
 * thread which called {@link #beginReporting()} last will be adhered to.
 * <p>
 * This class is not intended to be subclassed by clients.
 * </p>
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
	private final IAnnotationModel fAnnotationModel;

	/** Annotations to add */
	private Map fAddAnnotations;
	
	/** Last thread that called {@link #beginReporting()} */
	private Thread fThread;

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
		String message= problem.getMessage();
		int offset= problem.getOffset();
		int length= problem.getLength();
		
		synchronized (this) {
			if (fThread != Thread.currentThread())
				return;
			fAddAnnotations.put(new Annotation(SPELLING_ANNOTATION_TYPE, false, message), new Position(offset, length));
		}
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#beginReporting()
	 */
	public synchronized void beginReporting() {
		fThread= Thread.currentThread();
		fAddAnnotations= new HashMap();
	}

	/*
	 * @see org.eclipse.ui.texteditor.spelling.ISpellingProblemCollector#endReporting()
	 */
	public void endReporting() {
		synchronized (this) {
			if (fThread != Thread.currentThread())
				return;
		}
		
		List removeAnnotations= new ArrayList();
		for (Iterator iter= fAnnotationModel.getAnnotationIterator(); iter.hasNext();) {
			Annotation annotation= (Annotation) iter.next();
			if (SPELLING_ANNOTATION_TYPE.equals(annotation.getType()))
				removeAnnotations.add(annotation);
		}
		
		Map addAnnotations;
		synchronized (this) {
			if (fThread != Thread.currentThread())
				return;
			addAnnotations= fAddAnnotations;
		}
		
		if (fAnnotationModel instanceof IAnnotationModelExtension)
			((IAnnotationModelExtension) fAnnotationModel).replaceAnnotations((Annotation[]) removeAnnotations.toArray(new Annotation[removeAnnotations.size()]), addAnnotations);
		else {
			for (Iterator iter= removeAnnotations.iterator(); iter.hasNext();)
				fAnnotationModel.removeAnnotation((Annotation) iter.next());
			for (Iterator iter= addAnnotations.keySet().iterator(); iter.hasNext();) {
				Annotation annotation= (Annotation) iter.next();
				fAnnotationModel.addAnnotation(annotation, (Position) addAnnotations.get(annotation));
			}
		}
		
		synchronized (this) {
			if (fThread != Thread.currentThread())
				return;
			fThread= null;
			fAddAnnotations= null;
		}
	}
}
