/**********************************************************************
Copyright (c) 2000, 2003 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.editor.text;


import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.Assert;

import org.eclipse.ui.externaltools.internal.model.ExternalToolsPlugin;
import org.eclipse.ui.texteditor.MarkerAnnotation;


public class XMLMarkerAnnotation extends MarkerAnnotation implements IXMLAnnotation {
	
	private static final int NO_IMAGE= 0;
	private static final int ORIGINAL_MARKER_IMAGE= 1;
	private static final int OVERLAY_IMAGE= 4;
	private static final int GRAY_IMAGE= 5;

	private static ImageRegistry fgGrayMarkersImageRegistry;
	
	private IXMLAnnotation fOverlay;
	private boolean fNotRelevant= false;
	private AnnotationType fType;
	private int fImageType;
	
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
		
		fType= AnnotationType.UNKNOWN;
		try {
			if (marker != null && marker.exists()) {
				if (marker.isSubtypeOf(IMarker.BOOKMARK)) {
					fType= AnnotationType.BOOKMARK;
				} else if (marker.isSubtypeOf(IMarker.TASK)) {
					fType= AnnotationType.TASK;
				} else if (marker.isSubtypeOf(IMarker.PROBLEM)) {
					int severity= marker.getAttribute(IMarker.SEVERITY, -1);
					switch (severity) {
						case IMarker.SEVERITY_ERROR:
							fType= AnnotationType.ERROR;
							break;
						case IMarker.SEVERITY_WARNING:
							fType= AnnotationType.WARNING;
							break;
						case IMarker.SEVERITY_INFO:
							fType= AnnotationType.INFO;
							break;
					}
//				} else if (marker.isSubtypeOf(SearchUI.SEARCH_MARKER)) { TODO: fix
//					result= AnnotationType.SEARCH;
				}
			}
		} catch (CoreException e) {
			ExternalToolsPlugin.getDefault().log(e);
		}
		super.initialize();
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
		return fType == AnnotationType.WARNING || fType == AnnotationType.ERROR;
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

		if (hasOverlay())
			newImageType= OVERLAY_IMAGE;
		else if (isRelevant())
			newImageType= ORIGINAL_MARKER_IMAGE; 
		else
			newImageType= GRAY_IMAGE;

		if (fImageType == newImageType && newImageType != OVERLAY_IMAGE)
			// Nothing changed - simply return the current image
			return super.getImage(display);

		Image newImage= null;
		switch (newImageType) {
			case ORIGINAL_MARKER_IMAGE:
				newImage= null;
				break;
			case OVERLAY_IMAGE:
				newImage= fOverlay.getImage(display);
				break;
			case GRAY_IMAGE:
				if (fImageType != ORIGINAL_MARKER_IMAGE)
					setImage(null);
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
		if (fgGrayMarkersImageRegistry == null)
			fgGrayMarkersImageRegistry= new ImageRegistry(display);
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
	
	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IXMLAnnotation#getAnnotationType()
	 */
	public AnnotationType getAnnotationType() {
		return fType;
	}
}