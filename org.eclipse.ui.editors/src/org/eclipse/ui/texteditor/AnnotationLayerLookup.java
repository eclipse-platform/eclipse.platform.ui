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

package org.eclipse.ui.texteditor;

import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;

import org.eclipse.ui.internal.editors.text.EditorsPlugin;


/**
 * Provides the strategy for determining annotation type layer for given markers.
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * <p>
 * TODO: Cache the layer info, offer static API to get the layer and remove the constants.
 *		 There might be a performance issue when replacing the constants. We might also
 *		 completely get rid of this class and declare the missing pieces in XML and for
 *		 backward compatibility we would need redirect Annotation.getLayer() to the
 *		 annotation access if the fLayer is not set. But this might also be expensive.
 * 
 * @since 3.0
 */
public class AnnotationLayerLookup {

	/** 
	 * The layer in which task annotations are located.
	 * @deprecated will be inlined once cache is implemented
	 */
	public final static int TASK_LAYER= getAnnotationLayer(IMarker.TASK);
	
	/** 
	 * The layer in which bookmarks annotatons are located.
	 * @deprecated will be inlined once cache is implemented
	 */
	public final static int BOOKMARK_LAYER= getAnnotationLayer(IMarker.BOOKMARK);

	/** 
	 * The layer in which info annotations are located.
	 * @deprecated will be inlined once cache is implemented
	 */
	public final static int INFO_LAYER= getAnnotationLayer(IMarker.PROBLEM, IMarker.SEVERITY_INFO);
	
	/** 
	 * The layer in which warning annotations representing are located.
	 * @deprecated will be inlined once cache is implemented
	 */
	public final static int WARNING_LAYER= getAnnotationLayer(IMarker.PROBLEM, IMarker.SEVERITY_WARNING);

	/** 
	 * The layer in which error annotations representing are located.
	 * @deprecated will be inlined once cache is implemented
	 */
	public final static int ERROR_LAYER= getAnnotationLayer(IMarker.PROBLEM, IMarker.SEVERITY_ERROR);

	private static IAnnotationAccessExtension fgAnnotationAccess;

	
	/**
	 * Computes the annotation's presentation layer that corresponds to the given marker type.
	 * 
	 * @param markerType the marker type
	 * @return the annotation's presentation layer
	 */
	private static int getAnnotationLayer(String markerType) {
		return getAnnotationLayer(markerType, IMarker.SEVERITY_INFO);
	}

	/**
	 * Computes the annotation's presentation layer that corresponds to the given marker type
	 * and the given marker severity.
	 * 
	 * @param markerType the marker type
	 * @param markerSeverity the marker severity
	 * @return the annotation's presentation layer
	 */
	private static int getAnnotationLayer(String markerType, int markerSeverity) {
		String annotationType= EditorsPlugin.getDefault().getAnnotationTypeLookup().getAnnotationType(markerType, markerSeverity);
		if (annotationType == null)
			return IAnnotationAccessExtension.DEFAULT_LAYER;
		
		Annotation a= new Annotation(annotationType, false, null);
		
		return getAnnotationAccess().getLayer(a);
	}
	
	/**
	 * Returns the annotation access. 
	 * 
	 * @return the annotation access
	 */
	private static IAnnotationAccessExtension getAnnotationAccess() {
		if (fgAnnotationAccess == null)
			fgAnnotationAccess= new DefaultMarkerAnnotationAccess();
		return fgAnnotationAccess;
	}
}
