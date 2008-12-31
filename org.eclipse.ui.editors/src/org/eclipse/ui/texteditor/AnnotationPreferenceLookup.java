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
package org.eclipse.ui.texteditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.text.source.Annotation;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.AnnotationType;
import org.eclipse.ui.internal.texteditor.AnnotationTypeHierarchy;
import org.eclipse.ui.internal.texteditor.DelegatingAnnotationPreference;

/**
 * Provides the strategy for finding the annotation preference for a given
 * annotation.
 *
 * @since 3.0
 */
public class AnnotationPreferenceLookup {

	/** The map between annotation types and annotation preference fragments. */
	private Map fFragments;

	/**
	 * Creates a new annotation preference lookup object.
	 */
	public AnnotationPreferenceLookup() {
	}

	/**
	 * Returns the annotation preference of a given annotation.
	 *
	 * @param annotation the annotation
	 * @return the annotation preference for the given annotation or <code>null</code>
	 */
	public AnnotationPreference getAnnotationPreference(Annotation annotation) {
		return getAnnotationPreference(annotation.getType());
	}

	/**
	 * Returns the annotation preference defined for the given annotation type.
	 *
	 * @param annotationType the annotation type
	 * @return the annotation preference for the given annotation type or <code>null</code>
	 */
	public AnnotationPreference getAnnotationPreference(String annotationType) {
		if (annotationType == null || annotationType == Annotation.TYPE_UNKNOWN)
			return null;

		AnnotationTypeHierarchy hierarchy= getAnnotationTypeHierarchy();
		AnnotationType type= hierarchy.getAnnotationType(annotationType);
		AnnotationPreference preference= type.getPreference();
		if (preference == null) {
			preference= new DelegatingAnnotationPreference(type, this);
			type.setAnnotationPreference(preference);
		}

		return preference;
	}

	/**
	 * Returns the annotation preference fragment defined for the given
	 * annotation type.
	 * <p>
	 * For internal use only. Not intended to be called by clients.
	 *
	 * @param annotationType the annotation type
	 * @return the defined annotation preference fragment
	 */
	public AnnotationPreference getAnnotationPreferenceFragment(String annotationType) {
		Map fragments= getPreferenceFragments();
		return (AnnotationPreference) fragments.get(annotationType);
	}

	/**
	 * Returns the annotation type hierarchy and creates it when not yet done.
	 *
	 * @return the annotation type hierarchy
	 */
	private AnnotationTypeHierarchy getAnnotationTypeHierarchy() {
		return EditorsPlugin.getDefault().getAnnotationTypeHierarchy();
	}

	/**
	 * Returns a map between annotation type names and annotation preference
	 * fragments and creates it if not yet done.
	 *
	 * @return the map between annotation type names and annotation preference fragments
	 */
	private synchronized Map getPreferenceFragments() {
		if (fFragments == null) {
			fFragments= new HashMap();
			MarkerAnnotationPreferences p= new MarkerAnnotationPreferences();
			Iterator e= p.getAnnotationPreferenceFragments().iterator();
			while (e.hasNext()) {
				AnnotationPreference fragment= (AnnotationPreference) e.next();
				Object annotationType = fragment.getAnnotationType();
				AnnotationPreference preference= (AnnotationPreference) fFragments.get(annotationType);
				if (preference == null)
					fFragments.put(annotationType, fragment);
				else
					preference.merge(fragment);
			}
		}
		return fFragments;
	}
}
