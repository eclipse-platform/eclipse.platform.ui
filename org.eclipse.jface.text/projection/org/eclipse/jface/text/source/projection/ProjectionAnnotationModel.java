/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.text.source.projection;

import java.util.Iterator;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModel;


/**
 * A projection annotation model.
 * <p>
 * Internal class. Do not use. Public only for testing purposes.
 * 
 * @since 3.0
 */
public class ProjectionAnnotationModel extends AnnotationModel {

	/**
	 * Changes the state of the given annotation to collapsed. An appropriate
	 * annotation model change event is sent out.
	 * 
	 * @param annotation the annotation
	 */
	public void collaps(Annotation annotation) {
		if (annotation instanceof ProjectionAnnotation) {
			ProjectionAnnotation projection= (ProjectionAnnotation) annotation;
			if (!projection.isFolded()) {
				projection.markFolded();
				modifyAnnotation(projection, true);
			}
		}
	}

	/**
	 * Changes the state of the given annotation to expanded. An appropriate
	 * annotation model change event is sent out.
	 * 
	 * @param annotation the annotation
	 */
	public void expand(Annotation annotation) {
		if (annotation instanceof ProjectionAnnotation) {
			ProjectionAnnotation projection= (ProjectionAnnotation) annotation;
			if (projection.isFolded()) {
				projection.markUnfolded();
				modifyAnnotation(projection, true);
			}
		}
	}
	
	/**
	 * Toggles the expansion state of the given annotation. An appropriate
	 * annotation model change event is sent out.
	 * 
	 * @param annotation the annotation
	 */
	public void toggleExpansionState(Annotation annotation) {
		if (annotation instanceof ProjectionAnnotation) {
			ProjectionAnnotation projection= (ProjectionAnnotation) annotation;
			
			if (projection.isFolded())
				projection.markUnfolded();
			else
				projection.markFolded();
	
			modifyAnnotation(projection, true);
		}
	}
	
	/**
	 * Expands all annotations that overlap with the given range and are collapsed.
	 * 
	 * @param offset the range offset
	 * @param length the range length
	 * @return <code>true</code> if any annotation has been expanded, <code>false</code> otherwise
	 */
	public boolean expandAll(int offset, int length) {
		
		boolean unfolded= false;
		
		Iterator iterator= getAnnotationIterator();
		while (iterator.hasNext()) {
			ProjectionAnnotation annotation= (ProjectionAnnotation) iterator.next();
			if (annotation.isFolded()) {
				Position position= getPosition(annotation);
				if (position.overlapsWith(offset, length) /* || is a delete at the boundary */ ) {
					annotation.markUnfolded();
					modifyAnnotation(annotation, false);
					unfolded= true;
				}
			}
		}
		
		if (unfolded)
			fireModelChanged();
		
		return unfolded;
	}
}