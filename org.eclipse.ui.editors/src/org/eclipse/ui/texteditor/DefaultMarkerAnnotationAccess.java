/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.texteditor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.internal.texteditor.*;


/**
 * Default class for accessing marker annotation properties.
 *
 * @since 2.1
 */
public class DefaultMarkerAnnotationAccess implements IAnnotationAccess, IAnnotationAccessExtension {

	/**
	 * Constant for the unknown marker type.
	 *
	 * @deprecated As of 3.0, replaced by Annotation.TYPE_UNKNOWN
	 */
	public static final String UNKNOWN= Annotation.TYPE_UNKNOWN;

	/**
	 * Constant for the error system image.
	 * Value: <code>error</code>
	 *
	 * @since 3.0
	 */
	public static final String ERROR_SYSTEM_IMAGE= "error"; //$NON-NLS-1$
	/**
	 * Constant for the warning system image.
	 * Value: <code>warning</code>
	 *
	 * @since 3.0
	 */
	public static final String WARNING_SYSTEM_IMAGE= "warning"; //$NON-NLS-1$
	/**
	 * Constant for the info system image.
	 * Value: <code>info</code>
	 *
	 * @since 3.0
	 */
	public static final String INFO_SYSTEM_IMAGE= "info"; //$NON-NLS-1$
	/**
	 * Constant for the task system image.
	 * Value: <code>task</code>
	 *
	 * @since 3.0
	 */
	public static final String TASK_SYSTEM_IMAGE= "task"; //$NON-NLS-1$
	/**
	 * Constant for the bookmark system image.
	 * Value: <code>bookmark</code>
	 *
	 * @since 3.0
	 */
	public static final String BOOKMARK_SYSTEM_IMAGE= "bookmark"; //$NON-NLS-1$

	/**
	 * The mapping between external and internal symbolic system image names.
	 *
	 * @since 3.0
	 */
	private final static Map MAPPING;

	static {
		MAPPING= new HashMap();
		MAPPING.put(ERROR_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_ERROR_TSK);
		MAPPING.put(WARNING_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_WARN_TSK);
		MAPPING.put(INFO_SYSTEM_IMAGE, ISharedImages.IMG_OBJS_INFO_TSK);
		MAPPING.put(TASK_SYSTEM_IMAGE, IDE.SharedImages.IMG_OBJS_TASK_TSK);
		MAPPING.put(BOOKMARK_SYSTEM_IMAGE, IDE.SharedImages.IMG_OBJS_BKMRK_TSK);
	}


	/**
	 * The marker annotation preferences.
	 *
	 * @deprecated As of 3.0, no replacement
	 */
	protected MarkerAnnotationPreferences fMarkerAnnotationPreferences;

	/**
	 * Returns a new default marker annotation access with the given preferences.
	 *
	 * @param markerAnnotationPreferences
	 * @deprecated As of 3.0, replaced by {@link org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess#DefaultMarkerAnnotationAccess()}
	 */
	public DefaultMarkerAnnotationAccess(MarkerAnnotationPreferences markerAnnotationPreferences) {
		fMarkerAnnotationPreferences= markerAnnotationPreferences;
	}

	/**
	 * Creates a new default marker annotation access using the standard
	 * preference lookup strategy which is the one provided by the enclosing
	 * plug-in.
	 *
	 * @since 3.0
	 */
	public DefaultMarkerAnnotationAccess() {
	}

	/**
	 * Returns the annotation preference for the given annotation.
	 *
	 * @param annotation the annotation
	 * @return the annotation preference for the given annotation or <code>null</code>
	 */
	private AnnotationPreference getAnnotationPreference(Annotation annotation) {
		AnnotationPreferenceLookup lookup= getAnnotationPreferenceLookup();
		if (lookup != null)
			return lookup.getAnnotationPreference(annotation);
		return null;
	}

	/**
	 * Returns the annotation preference lookup used by this annotation access.
	 *
	 * @return the annotation preference lookup
	 * @since 3.0
	 */
	protected AnnotationPreferenceLookup getAnnotationPreferenceLookup() {
		return EditorsPlugin.getDefault().getAnnotationPreferenceLookup();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#getType(org.eclipse.jface.text.source.Annotation)
	 */
	public Object getType(Annotation annotation) {
		return annotation.getType();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isMultiLine(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isMultiLine(Annotation annotation) {
		return true;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccess#isTemporary(org.eclipse.jface.text.source.Annotation)
	 */
	public boolean isTemporary(Annotation annotation) {
		return !annotation.isPersistent();
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getLabel(org.eclipse.jface.text.source.Annotation)
	 * @since 3.0
	 */
	public String getTypeLabel(Annotation annotation) {
		AnnotationPreference preference= getAnnotationPreference(annotation);
		return preference != null ? preference.getPreferenceLabel() : null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getLayer(org.eclipse.jface.text.source.Annotation)
	 * @since 3.0
	 */
	public int getLayer(Annotation annotation) {
		if (annotation instanceof IAnnotationPresentation) {
			IAnnotationPresentation presentation= (IAnnotationPresentation) annotation;
			return presentation.getLayer();
		}
		
		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference != null)
			return preference.getPresentationLayer();
		
		// backward compatibility, ignore exceptions, just return default layer
		try {

			Method method= annotation.getClass().getMethod("getLayer", null); //$NON-NLS-1$
			Integer result= (Integer) method.invoke(annotation, null);
			return result.intValue();

		} catch (SecurityException x) {
		} catch (IllegalArgumentException x) {
		} catch (NoSuchMethodException x) {
		} catch (IllegalAccessException x) {
		} catch (InvocationTargetException x) {
		}

		return IAnnotationAccessExtension.DEFAULT_LAYER;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#paint(org.eclipse.jface.text.source.Annotation, org.eclipse.swt.graphics.GC, org.eclipse.swt.widgets.Canvas, org.eclipse.swt.graphics.Rectangle)
	 * @since 3.0
	 */
	public void paint(Annotation annotation, GC gc, Canvas canvas, Rectangle bounds) {

		if (annotation instanceof IAnnotationPresentation) {
			IAnnotationPresentation presentation= (IAnnotationPresentation) annotation;
			presentation.paint(gc, canvas, bounds);
			return;
		}

		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference != null) {
			Object type= getType(annotation);
			String annotationType= (type == null ? null : type.toString());
			Image image= getImage(annotation, preference, annotationType);
			if (image != null) {
				ImageUtilities.drawImage(image, gc, canvas, bounds, SWT.CENTER, SWT.TOP);
				return;
			}
		}

		// backward compatibility, ignore exceptions, just don't paint
		try {

			Method method= annotation.getClass().getMethod("paint", new Class[] { GC.class, Canvas.class, Rectangle.class }); //$NON-NLS-1$
			method.invoke(annotation, new Object[] {gc, canvas, bounds });

		} catch (SecurityException x) {
		} catch (IllegalArgumentException x) {
		} catch (NoSuchMethodException x) {
		} catch (IllegalAccessException x) {
		} catch (InvocationTargetException x) {
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#isPaintable(org.eclipse.jface.text.source.Annotation)
	 * @since 3.0
	 */
	public boolean isPaintable(Annotation annotation) {
		if (annotation instanceof IAnnotationPresentation)
			return true;

		AnnotationPreference preference= getAnnotationPreference(annotation);
		if (preference == null)
			return false;

		Object type= getType(annotation);
		String annotationType= (type == null ? null : type.toString());
		Image image= getImage(annotation, preference, annotationType);
		return image != null;
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#isSubtype(java.lang.Object, java.lang.Object)
	 */
	public boolean isSubtype(Object annotationType, Object potentialSupertype) {
		AnnotationTypeHierarchy hierarchy= getAnnotationTypeHierarchy();
		return hierarchy.isSubtype(potentialSupertype.toString(), annotationType.toString());
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationAccessExtension#getSupertypes(java.lang.Object)
	 */
	public Object[] getSupertypes(Object annotationType) {
		AnnotationTypeHierarchy hierarchy= getAnnotationTypeHierarchy();
		AnnotationType type= hierarchy.getAnnotationType(annotationType.toString());
		return type.getSuperTypes();
	}

	/**
	 * Returns the annotation type hierarchy used by this annotation access.
	 *
	 * @return the annotation type hierarchy
	 * @since 3.0
	 */
	protected AnnotationTypeHierarchy getAnnotationTypeHierarchy() {
		return EditorsPlugin.getDefault().getAnnotationTypeHierarchy();
	}

	/**
	 * Translates the given symbolic image name into the according symbolic image name
	 * the {@link org.eclipse.ui.ISharedImages} understands.
	 *
	 * @param symbolicImageName the symbolic system image name to be translated
	 * @return the shared image name
	 * @since 3.0
	 */
	private String translateSymbolicImageName(String symbolicImageName) {
		return (String) MAPPING.get(symbolicImageName);
	}

	/**
	 * Returns the image for the given annotation and the given annotation preferences or
	 * <code>null</code> if there is no such image.
	 *
	 * @param annotation the annotation
	 * @param preference the annotation preference
	 * @param annotationType the annotation type
	 * @return the image or <code>null</code>
	 * @since 3.0
	 */
	private Image getImage(Annotation annotation, AnnotationPreference preference, String annotationType) {

		ImageRegistry registry= EditorsPlugin.getDefault().getImageRegistry();

		IAnnotationImageProvider annotationImageProvider = preference.getAnnotationImageProvider();
		if (annotationImageProvider != null) {

			Image image= annotationImageProvider.getManagedImage(annotation);
			if (image != null)
				return image;

			String id= annotationImageProvider.getImageDescriptorId(annotation);
			if (id != null) {
				image= registry.get(id);
				if (image == null) {
					ImageDescriptor descriptor= annotationImageProvider.getImageDescriptor(id);
					registry.put(id, descriptor);
					image= registry.get(id);
				}
				return image;
			}
		}

		if (annotationType == null)
			return null;

		Image image= registry.get(annotationType);
		if (image == null) {
			ImageDescriptor descriptor= preference.getImageDescriptor();
			if (descriptor != null) {
				registry.put(annotationType, descriptor);
				image= registry.get(annotationType);
			} else {
				String key= translateSymbolicImageName(preference.getSymbolicImageName());
				if (key != null) {
					ISharedImages sharedImages= PlatformUI.getWorkbench().getSharedImages();
					image= sharedImages.getImage(key);
				}
			}
		}
		return image;
	}
}
