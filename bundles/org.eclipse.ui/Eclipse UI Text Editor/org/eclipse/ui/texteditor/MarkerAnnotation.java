/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.texteditor;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.source.Annotation;

import org.eclipse.core.resources.IMarker;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;


/**
 * Annotation representing a marker on a resource in the workspace.
 * This class may be instantiated or be subclassed.
 *
 * @see IMarker
 */
public class MarkerAnnotation extends Annotation {
	
	/** 
	 * The layer in which markers representing problem are located.
	 * @since 2.0
	 */
	public final static int PROBLEM_LAYER= 5;
	
	/** Internal image registry */
	private static Map fgImageRegistry;
	
	/**
	 * Returns an image for the given display as specified by the given image descriptor.
	 * @param display the display
	 * @param descriptor the image descriptor
	 * @return an image for the display as specified by the descriptor
	 */
	protected static Image getImage(Display display, ImageDescriptor descriptor) {
		Map map= getImageRegistry(display);
		Image image= (Image) map.get(descriptor);
		if (image == null) {
			image= descriptor.createImage();
			map.put(descriptor, image);
		}
		return image;
	}
	
	/**
	 * Returns an image registry for the given display. If no such registry exists
	 * the resgitry is created.
	 * @param display the display
	 * @return the image registry for the given display
	 */
	protected static Map getImageRegistry(Display display) {
		if (fgImageRegistry == null) {
			fgImageRegistry= new HashMap();
			display.disposeExec(new Runnable() {
				public void run() {
					if (fgImageRegistry != null) {
						Map map= fgImageRegistry;
						fgImageRegistry= null;
						Iterator e= map.values().iterator();
						while (e.hasNext()) {
							Image image= (Image) e.next();
							if (!image.isDisposed())
								image.dispose();
						}
					}
				}
			});
		}
		return fgImageRegistry;
	}		
		
		
	/** The marker this annotation represents */
	private IMarker fMarker;
	/** The image, i.e., visual presentation of this annotation */
	private Image fImage;
	/** The image name */
	private String fImageName;

	/**
	 * Creates a new annotation for the given marker.
	 *
	 * @param marker the marker
	 */
	public MarkerAnnotation(IMarker marker) {
		fMarker= marker;
		initialize();
	}
	
	/**
	 * Sets the marker image to the given image.
	 *
	 * @param image the new marker image
	 */
	protected void setImage(Image image) {
		fImage= image;
	}
	
	/**
	 * The <code>MarkerAnnotation</code> implementation of this
	 * <code>Object</code> method returns <code>true</code> iff the other
	 * object is also a <code>MarkerAnnotation</code> and the marker handles are
	 * equal.
	 */
	public boolean equals(Object o) {
		if (o != null && o.getClass() == getClass())
			return fMarker.equals(((MarkerAnnotation) o).fMarker);
		return false;
	}
	
	/*
	 * @see Object#hashCode
	 */
	public int hashCode() {
		return fMarker.hashCode();
	}
		
	/**
	 * Returns this annotation's underlying marker.
	 *
	 * @return the marker
	 */
	public IMarker getMarker() {
		return fMarker;
	}
	
	/**
	 * Initializes the annotation's icon representation and its drawing layer
	 * based upon the properties of the underlying marker.
	 */
	protected void initialize() {
		
		String name= null;
		int layer= -1;
		
		if (MarkerUtilities.isMarkerType(fMarker, IMarker.TASK)) {
			name= ISharedImages.IMG_OBJS_TASK_TSK;
			layer= 1;
		} else if (MarkerUtilities.isMarkerType(fMarker, IMarker.BOOKMARK)) {
			name= ISharedImages.IMG_OBJS_BKMRK_TSK;
			layer= 2;
		} else if (MarkerUtilities.isMarkerType(fMarker, IMarker.PROBLEM)) {
			switch (fMarker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO)) {
				case IMarker.SEVERITY_INFO:
					name= ISharedImages.IMG_OBJS_INFO_TSK;
					layer= 3;
					break;
				case IMarker.SEVERITY_WARNING:
					layer= 3;
					name= ISharedImages.IMG_OBJS_WARN_TSK;
					break;
				case IMarker.SEVERITY_ERROR:
					layer= PROBLEM_LAYER;
					name= ISharedImages.IMG_OBJS_ERROR_TSK;
					break;
			};
		} else {
			name= getUnknownImageName(fMarker);
			layer= 1;
		}
		
		// setImage(getImage(name));
		fImage= null;
		fImageName= name;
		setLayer(layer);
	}
	
	/*
	 * @see Annotation#paint
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
		Image image= getImage(canvas.getDisplay());
		if (image != null) {
			// http://dev.eclipse.org/bugs/show_bug.cgi?id=19184
			drawImage(image, gc, canvas, r, SWT.CENTER, SWT.TOP);
		}
	}
	
	/**
	 * Informs this annotation about changes applied to its underlying marker
	 * and adapts to those changes.
	 */
	public void update() {
		initialize();
	}
		
	/**
	 * Returns the name of an image used to visually represent markers of 
	 * unknown type. This implementation returns <code>null</code>.
	 * Subclasses may replace this method.
	 *
	 * @param marker the marker of unkown type
	 * @return the name of an image for markers of unknown type.
	 */
	protected String getUnknownImageName(IMarker marker) {
		return null;
	}
	
	/**
	 * Returns the image of the given name. Subclasses may extend this method.
	 * If so, subclasses must assume responsibility for disposing the images
	 * they create.
	 * 
	 * @param name the name of the requested image
	 * @return the image or <code>null</code> if there is no such image
	 */
	protected Image getImage(String name) {			
		if (name != null)
			return PlatformUI.getWorkbench().getSharedImages().getImage(name);
		return null;
	}
	
	
	/**
	 * Returns an image for this annotation. It first consults the workbench adapter
	 * for this annotation's marker. If none is defined, it tries to find an image for 
	 * the image name of this annotation.
	 * 
	 * @param display the display for which the image is requested
	 * @return the image for this annotation
	 */
	protected Image getImage(Display display) {
		if (fImage == null) {
			
			IWorkbenchAdapter adapter= (IWorkbenchAdapter) fMarker.getAdapter(IWorkbenchAdapter.class);
			if (adapter != null) {
				ImageDescriptor descriptor= adapter.getImageDescriptor(fMarker);
				if (descriptor != null)
					fImage= getImage(display, descriptor);
			}
			
			if (fImage == null)
				fImage= getImage(fImageName);
				
		}
		return fImage;
	}
}