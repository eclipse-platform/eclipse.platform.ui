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
package org.eclipse.ant.internal.ui.editor.text;


import java.util.Iterator;

import org.eclipse.ant.internal.ui.model.AntUIPlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;

public class XMLMarkerAnnotation extends MarkerAnnotation implements IXMLAnnotation {
	private static final int NO_IMAGE= 0;
	private static final int ORIGINAL_MARKER_IMAGE= 1;
	private static final int OVERLAY_IMAGE= 4;
	private static final int GRAY_IMAGE= 5;

	private static ImageRegistry fgGrayMarkersImageRegistry;
	
	private IXMLAnnotation fOverlay;
	private boolean fNotRelevant= false;
	private String fType;
	private int fImageType;
	
	/**
	 * The marker annotation preferences.
	 * @since 3.0
	 */
	private MarkerAnnotationPreferences fMarkerAnnotationPreferences;
	
	public XMLMarkerAnnotation(IMarker marker) {
		super(marker);
	}
	
	/**
	 * Initializes the annotation's icon representation and its drawing layer
	 * based upon the properties of the underlying marker.
	 */
	protected void initialize() {
		fImageType= NO_IMAGE;
		IMarker marker= getMarker();
		
		fMarkerAnnotationPreferences= new MarkerAnnotationPreferences();
		fType= findAnnotationTypeForMarker(marker);
		super.initialize();
	}
	
	/**
	 * Finds the annotation type for the given marker.
	 * 
	 * @param marker the marker
	 * @return the annotation type or <code>null</code> if none was found
	 * @since 3.0
	 */
	private String findAnnotationTypeForMarker(IMarker marker) {
		Iterator e= fMarkerAnnotationPreferences.getAnnotationPreferences().iterator();
		while (e.hasNext()) {
			AnnotationPreference annotationPreference= (AnnotationPreference) e.next();
			boolean isSubtype;
			Integer severity;
			try {
				isSubtype= marker.isSubtypeOf(annotationPreference.getMarkerType());
				severity= (Integer)marker.getAttribute(IMarker.SEVERITY);
			} catch (CoreException ex) {
				AntUIPlugin.log(ex);
				return null;
			}
			if (isSubtype && (severity == null || severity.intValue() == annotationPreference.getSeverity())) {
				return (String)annotationPreference.getAnnotationType();
			}
		}
		return null;
	}
		
	/*
	 * @see IXMLAnnotation#getMessage()
	 */
	public String getMessage() {
		IMarker marker= getMarker();
		if (marker == null || !marker.exists())
			return ""; //$NON-NLS-1$
		else
			return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
	}

	/*
	 * @see IXMLAnnotation#isTemporary()
	 */
	public boolean isTemporary() {
		return false;
	}
	
	/*
	 * @see IXMLAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return WARNING_ANNOTATION_TYPE.equals(fType) || ERROR_ANNOTATION_TYPE.equals(fType);
	}
	
	/*
	 * @see IXMLAnnotation#isRelevant()
	 */
	public boolean isRelevant() {
		return !fNotRelevant;
	}

	/**
	 * Overlays this annotation with the given xmlAnnotation.
	 * 
	 * @param javaAnnotation annotation that is overlaid by this annotation
	 */
	public void setOverlay(IXMLAnnotation xmlAnnotation) {
		if (fOverlay != null)
			fOverlay.removeOverlaid(this);
			
		fOverlay= xmlAnnotation;
		fNotRelevant= (fNotRelevant || fOverlay != null);
		
		if (xmlAnnotation != null)
			xmlAnnotation.addOverlaid(this);
	}
	
	/*
	 * @see IXMLAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return fOverlay != null;
	}
	
	/*
	 * @see MarkerAnnotation#getImage(Display)
	 */
	public Image getImage(Display display) {
		int newImageType= NO_IMAGE;

		if (hasOverlay()) {
			newImageType= OVERLAY_IMAGE;
		} else if (isRelevant()) {
			newImageType= ORIGINAL_MARKER_IMAGE; 
		} else {
			newImageType= GRAY_IMAGE;
		}

		if (fImageType == newImageType && newImageType != OVERLAY_IMAGE) {
			// Nothing changed - simply return the current image
			return super.getImage(display);
		}

		Image newImage= null;
		switch (newImageType) {
			case ORIGINAL_MARKER_IMAGE:
				newImage= null;
				break;
			case OVERLAY_IMAGE:
				newImage= fOverlay.getImage(display);
				break;
			case GRAY_IMAGE:
				if (fImageType != ORIGINAL_MARKER_IMAGE) {
					setImage(null);
				}
				Image originalImage= super.getImage(display);
				if (originalImage != null) {
					ImageRegistry imageRegistry= getGrayMarkerImageRegistry(display);
					if (imageRegistry != null) {
						String key= Integer.toString(originalImage.hashCode());
						Image grayImage= imageRegistry.get(key);
						if (grayImage == null) {
							grayImage= new Image(display, originalImage, SWT.IMAGE_GRAY);
							imageRegistry.put(key, grayImage);
						}
						newImage= grayImage;
					}
				}
				break;
			default:
				Assert.isLegal(false);
		}

		fImageType= newImageType;
		setImage(newImage);
		return super.getImage(display);
	}
	
	private ImageRegistry getGrayMarkerImageRegistry(Display display) {
		if (fgGrayMarkersImageRegistry == null) {
			fgGrayMarkersImageRegistry= new ImageRegistry(display);
		}
		return fgGrayMarkersImageRegistry;
	}
	
	/*
	 * @see IXMLAnnotation#addOverlaid(IXMLAnnotation)
	 */
	public void addOverlaid(IXMLAnnotation annotation) {
		// not supported
	}

	/*
	 * @see IXMLAnnotation#removeOverlaid(IXMLAnnotation)
	 */
	public void removeOverlaid(IXMLAnnotation annotation) {
		// not supported
	}
	
	/*
	 * @see IXMLAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		// not supported
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#getAnnotationType()
	 */
	public String getAnnotationType() {
		return fType;
	}
}
