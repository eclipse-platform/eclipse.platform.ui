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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.source.Annotation;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * Default annotation.
 * This class may be instantiated or be subclassed.
 * <p>
 * XXX: This is work in progress and can change anytime until API for 3.0 is frozen.
 * </p>
 * @since 3.0
 */
public class DefaultAnnotation extends Annotation implements IAnnotationExtension {

	/** 
	 * The layer in which markers representing problem are located.
	 */
	public final static int PROBLEM_LAYER= 5;
	
	/** Internal image registry */
	private static Map fgImageRegistry;
	/** Internal type to descriptor map */
	private static Map fgType2Descriptor= new HashMap();
	/** Indicates that no descriptor was found */
	private static final Object NO_DESCRIPTOR= new Object();
	
	protected Image fImage;
	protected String fImageName;
	
	private int fSeverity;
	private boolean fIsTemporary;
	private String fMessage;
	private String fAnnotationType;

	private boolean fIsInitialized= false;

	/**
	 * Returns an image for the given display as specified by the given image descriptor.
	 * 
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
	 * 
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
	
	/**
	 * Returns whether the given annotationType is of the given type.
	 * <p>
	 * XXX: See bug 41478 why we don't match sub-types
	 * </p>
	 *
	 * @param annotationType the annotationType to be checked
	 * @param type the reference type
	 * @return <code>true</code>if annotationType is an instance of the reference type
	 */
	private static boolean isAnnotationType(String annotationType, String type) {
		return annotationType != null && annotationType.equals(type);
	}
	
	/**
	 * Creates a new editor annotation.
	 * 
	 * @param annotationType the type of this annotation
	 * @param severity the severity of this annotation
	 * @param isTemporary <code>true</code> if this is a temporary annotation
	 * @param message the message of this annotation
	 */
	public DefaultAnnotation(String annotationType, int severity, boolean isTemporary, String message) {
		Assert.isTrue(severity == IMarker.SEVERITY_INFO || severity == IMarker.SEVERITY_WARNING || severity == IMarker.SEVERITY_ERROR);
		fSeverity= severity;
		fIsTemporary= isTemporary;
		fMessage= message;
		setLayer(PROBLEM_LAYER + 1);
		fAnnotationType= annotationType;
	}
	
	/**
	 * Initializes the annotation's icon representation and its drawing layer
	 * based upon the properties of the underlying marker.
	 */
	protected void initialize() {
		
		String name= getUnknownImageName(fAnnotationType);
		int layer= 1;
		
		if (isAnnotationType(fAnnotationType, IMarker.TASK)) {
			name= ISharedImages.IMG_OBJS_TASK_TSK;
			layer= 1;
		} else if (isAnnotationType(fAnnotationType, IMarker.BOOKMARK)) {
			name= ISharedImages.IMG_OBJS_BKMRK_TSK;
			layer= 2;
		} else if (isAnnotationType(fAnnotationType, IMarker.PROBLEM)) {
			switch (fSeverity) {
				case IMarker.SEVERITY_INFO:
					name= ISharedImages.IMG_OBJS_INFO_TSK;
					layer= 3;
					break;
				case IMarker.SEVERITY_WARNING:
					name= ISharedImages.IMG_OBJS_WARN_TSK;
					layer= 3;
					break;
				case IMarker.SEVERITY_ERROR:
					name= ISharedImages.IMG_OBJS_ERROR_TSK;
					layer= PROBLEM_LAYER;
					break;
			}
		}
		
		fImage= null;
		fImageName= name;
		setLayer(layer);
	}
	
	/**
	 * Returns the name of an image used to visually represent markers of 
	 * unknown type. This implementation returns <code>null</code>.
	 * Subclasses may replace this method.
	 *
	 * @return the name of an image for markers of unknown type.
	 */
	protected String getUnknownImageName(String annotationType) {
		return null;
	}
	
	/*
	 * @see Annotation#paint(GC, Canvas, Rectangle)
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
		Image image= getImage(canvas.getDisplay());
		if (image != null)
			drawImage(image, gc, canvas, r, SWT.CENTER, SWT.TOP);
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
	
	/*
	 * @see IJavaAnnotation#getImage(Display)
	 */
	protected Image getImage(final Display display) {
		
		if (!fIsInitialized) {
			fIsInitialized= true;
			initialize();
		}
		
		if (fImage != null)
			return fImage;
		
		final String key= fAnnotationType + fSeverity;
		
		Object descriptor= fgType2Descriptor.get(key);
		if (descriptor == NO_DESCRIPTOR)
			fImage= getImage(fImageName);
		else if (descriptor != null)
			fImage= getImage(display, (ImageDescriptor)descriptor);
		
		if (fImage != null)
			return fImage;

		// XXX: hack since I cannot get the image for a marker type
		WorkspaceModifyOperation r= new WorkspaceModifyOperation() {
			/*
			 * @see WorkspaceModifyOperation
			 */
			public void execute(IProgressMonitor monitor) throws CoreException,InvocationTargetException, InterruptedException {
				IMarker tempMarker= ResourcesPlugin.getWorkspace().getRoot().createMarker(fAnnotationType);
				tempMarker.setAttribute(IMarker.SEVERITY, fSeverity);
				if (tempMarker.exists()) {
					IWorkbenchAdapter adapter= (IWorkbenchAdapter) tempMarker.getAdapter(IWorkbenchAdapter.class);
					if (adapter != null) {
						Object imageDescriptor= adapter.getImageDescriptor(tempMarker);
						if (imageDescriptor != null) {
							fImage= getImage(display, (ImageDescriptor)imageDescriptor);
							fgType2Descriptor.put(key, imageDescriptor);
						} else {
							fgType2Descriptor.put(key, NO_DESCRIPTOR);
						}
					}
					tempMarker.delete();
				}
			}
		};
		try {
			r.run(null);
		} catch (InvocationTargetException ex) {
			fgType2Descriptor.put(key, NO_DESCRIPTOR);
		} catch (InterruptedException ex) {
			fgType2Descriptor.put(key, NO_DESCRIPTOR);
		}
		
		if (fImage == null)
			fImage= getImage(fImageName);

		return fImage;
	}
	
	/*
	 * @see IAnnotationExtension#getMessage()
	 */
	public String getMessage() {
		return fMessage;
	}

	public String getMarkerType() {
		return fAnnotationType;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IAnnotationExtension#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}
	
	/*
	 * @see IAnnotationExtension#isTemporary()
	 */
	public boolean isTemporary() {
		return fIsTemporary;
	}
}
