/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.resource.StringConverter;

import org.eclipse.ui.editors.text.EditorsUI;


/**
 * Provides the strategy for determining annotation types for given markers.
 *
 * @since 3.0
 */
public final class AnnotationTypeLookup {

	/**
	 * Record representing an annotation type mapping.
	 */
	private static class AnnotationTypeMapping {

		final static int UNDEFINED= -1;

		String fAnnotationType;
		String fMarkerType;
		int fMarkerSeverity= UNDEFINED;

		boolean isMarkerSeverityDefined() {
			return fMarkerSeverity != UNDEFINED;
		}
	}

	/**
	 * The lookup table for marker to annotation type mappings. The value type is a {@link String}
	 * or a {@code Map<Integer, String>}.
	 */
	private Map<String, Object> fMapping;

	/**
	 * Creates a new annotation lookup object.
	 */
	public AnnotationTypeLookup() {
	}

	/**
	 * Computes the annotation type that corresponds to the state of
	 * the given marker.
	 *
	 * @param marker the marker
	 * @return the annotation type or <code>null</code>
	 */
	public String getAnnotationType(IMarker marker) {
		String markerType= MarkerUtilities.getMarkerType(marker);
		if (markerType != null) {
			int severity= MarkerUtilities.getSeverity(marker);
			return getAnnotationType(markerType, severity);
		}
		return null;
	}

	/**
	 * Computes the annotation type that corresponds to the given marker type and
	 * the given marker severity.
	 *
	 * @param markerType the marker type
	 * @param markerSeverity the marker severity
	 * @return the annotation type or <code>null</code>
	 */
	public String getAnnotationType(String markerType, int markerSeverity) {
		String annotationType= lookupAnnotationType(markerType, markerSeverity);
		if (annotationType != null) {
			return annotationType;
		}
		String[] superTypes= MarkerUtilities.getSuperTypes(markerType);
		for (String superType : superTypes) {
			annotationType = lookupAnnotationType(superType, markerSeverity);
			if (annotationType != null) {
				return annotationType;
			}
		}
		return null;
	}

	/**
	 * Returns the annotation type for the given marker type and the given
	 * marker severity.
	 *
	 * @param markerType the marker type
	 * @param severity the marker severity
	 * @return the annotation type
	 */
	private String lookupAnnotationType(String markerType, int severity) {
		if (fMapping == null) {
			initializeMapping();
		}

		Object value= fMapping.get(markerType);

		if (value instanceof String ) {
			return (String) value;
		}

		if (value instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<Integer, String> severityMap= (Map<Integer, String>) value;
			return severityMap.get(Integer.valueOf(severity));
		}

		return null;
	}

	/**
	 * Initializes the mapping between markers and their property values and
	 * annotation types.
	 */
	private void initializeMapping() {
		fMapping= new HashMap<>();
		for (AnnotationTypeMapping atm : getAnnotationTypeMappings()) {
			if (atm.isMarkerSeverityDefined()) {
				Object severityMap= fMapping.get(atm.fMarkerType);
				if (!(severityMap instanceof Map)) {
					severityMap= new HashMap<>();
					fMapping.put(atm.fMarkerType, severityMap);
				}
				@SuppressWarnings("unchecked")
				Map<Integer, String> map= (Map<Integer, String>) severityMap;
				map.put(Integer.valueOf(atm.fMarkerSeverity), atm.fAnnotationType);
			} else {
				fMapping.put(atm.fMarkerType, atm.fAnnotationType);
			}
		}
	}

	/**
	 * Returns the list of annotation type mappings generated from the
	 * extensions provided for the annotation type extension point.
	 *
	 * @return a list of annotation type mappings
	 */
	private List<AnnotationTypeMapping> getAnnotationTypeMappings() {
		List<AnnotationTypeMapping> annotationTypeMappings= new ArrayList<>();
		// read compatibility mode
		readExtensionPoint(annotationTypeMappings, "markerAnnotationSpecification", "annotationType"); //$NON-NLS-1$ //$NON-NLS-2$
		// read new extension point
		readExtensionPoint(annotationTypeMappings, "annotationTypes", "name"); //$NON-NLS-1$ //$NON-NLS-2$
		return annotationTypeMappings;
	}

	/**
	 * Reads the extensions provided for the given extension point name. Uses
	 * the given type attribute name to create annotation type mappings that
	 * are appended to the given list.
	 *
	 * @param annotationTypeMappings the list to be populated
	 * @param extensionPointName the name of the extension point to read
	 * @param typeAttributeName the name of attribute specifying the annotation
	 *            type
	 */
	private void readExtensionPoint(List<AnnotationTypeMapping> annotationTypeMappings, String extensionPointName, String typeAttributeName) {
		IExtensionPoint extensionPoint= Platform.getExtensionRegistry().getExtensionPoint(EditorsUI.PLUGIN_ID, extensionPointName);
		if (extensionPoint != null) {
			IConfigurationElement[] elements= extensionPoint.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				AnnotationTypeMapping mapping = createMapping(element, typeAttributeName);
				if (mapping != null) {
					annotationTypeMappings.add(mapping);
				}
			}
		}
	}

	/**
	 * Creates an annotation type mapping from the given configuration element.
	 *
	 * @param element the configuration element
	 * @param typeAttributeName the name of the attribute specifying the
	 *            annotation type
	 * @return the annotation type mapping or <code>null</code>
	 */
	private AnnotationTypeMapping createMapping(IConfigurationElement element, String typeAttributeName) {

		AnnotationTypeMapping mapping= new AnnotationTypeMapping();

		String s= element.getAttribute(typeAttributeName);
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		mapping.fAnnotationType= s;

		s= element.getAttribute("markerType");  //$NON-NLS-1$
		if (s == null || s.trim().isEmpty()) {
			return null;
		}
		mapping.fMarkerType= s;

		s= element.getAttribute("markerSeverity");  //$NON-NLS-1$
		if (s != null && !s.trim().isEmpty()) {
			mapping.fMarkerSeverity= StringConverter.asInt(s, AnnotationTypeMapping.UNDEFINED);
		}

		return mapping;
	}
}
