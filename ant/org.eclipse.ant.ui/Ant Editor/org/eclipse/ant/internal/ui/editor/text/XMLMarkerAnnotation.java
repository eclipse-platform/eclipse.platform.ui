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
package org.eclipse.ant.internal.ui.editor.text;


import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class XMLMarkerAnnotation extends MarkerAnnotation implements IXMLAnnotation {
	private static final int NO_IMAGE= 0;
	private static final int ORIGINAL_MARKER_IMAGE= 1;
	private static final int OVERLAY_IMAGE= 4;
	private static final int GRAY_IMAGE= 5;

	private static ImageRegistry fgGrayMarkersImageRegistry;
	
	private IXMLAnnotation fOverlay;
	private boolean fNotRelevant= false;
	private int fImageType;
	
	public XMLMarkerAnnotation(String annotationType, IMarker marker) {
		super(annotationType, marker);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.MarkerAnnotation#initialize()
	 */
	protected void initialize() {
		fImageType= NO_IMAGE;
		
		super.initialize();
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getMessage()
	 */
	public String getMessage() {
		IMarker marker= getMarker();
		if (marker == null || !marker.exists()) {
			return ""; //$NON-NLS-1$
		}
		
		return marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#isTemporary()
	 */
	public boolean isTemporary() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#isProblem()
	 */
	public boolean isProblem() {
		return WARNING_ANNOTATION_TYPE.equals(getType()) || ERROR_ANNOTATION_TYPE.equals(getType());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#isRelevant()
	 */
	public boolean isRelevant() {
		return !fNotRelevant;
	}

	/**
	 * Overlays this annotation with the given xmlAnnotation.
	 * 
	 * @param xmlAnnotation annotation that is overlaid by this annotation
	 */
	public void setOverlay(IXMLAnnotation xmlAnnotation) {
		if (fOverlay != null) {
			fOverlay.removeOverlaid(this);
		}
			
		fOverlay= xmlAnnotation;
		fNotRelevant= (fNotRelevant || fOverlay != null);
		
		if (xmlAnnotation != null) {
			xmlAnnotation.addOverlaid(this);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return fOverlay != null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.MarkerAnnotation#getImage(org.eclipse.swt.widgets.Display)
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#addOverlaid(org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation)
	 */
	public void addOverlaid(IXMLAnnotation annotation) {
		// not supported
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#removeOverlaid(org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation)
	 */
	public void removeOverlaid(IXMLAnnotation annotation) {
		// not supported
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ant.internal.ui.editor.text.IXMLAnnotation#getOverlaidIterator()
	 */
	public Iterator getOverlaidIterator() {
		// not supported
		return null;
	}
}