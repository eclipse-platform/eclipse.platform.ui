/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import java.net.URL;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * An image descriptor is an object that knows how to create
 * an SWT image.  It does not hold onto images or cache them,
 * but rather just creates them on demand.  An image descriptor
 * is intended to be a lightweight representation of an image
 * that can be manipulated even when no SWT display exists.
 * <p>
 * This package defines a concrete image descriptor implementation
 * which reads an image from a file ({@link #createFromFile(Class, String)}).
 * It also provides abstract framework classes (this one and
 * {@link CompositeImageDescriptor}) which may be subclassed to define
 * news kinds of image descriptors.
 * </p>
 * <p>
 * Using this abstract class involves defining a concrete subclass
 * and re-implementing the {@link #getImageData(int)} method.
 * Legacy subclasses that are not HiDPI-aware used to override the
 * deprecated {@link #getImageData()} method. Subclasses <b>must</b>
 * re-implement exactly one of the {@code getImageData} methods
 * and they should not re-implement both.
 * </p>
 * <p>
 * There are two ways to get an Image from an ImageDescriptor. The method
 * createImage will always return a new Image which must be disposed by
 * the caller. Alternatively, createResource() returns a shared
 * Image. When the caller is done with an image obtained from createResource,
 * they must call destroyResource() rather than disposing the Image directly.
 * The result of createResource() can be safely cast to an Image.
 * </p>
 *
 * @see org.eclipse.swt.graphics.Image
 */
public abstract class ImageDescriptor extends DeviceResourceDescriptor {

    /**
     * A small red square used to warn that an image cannot be created.
     * <p>
     */
    protected static final ImageData DEFAULT_IMAGE_DATA = new ImageData(6, 6,
            1, new PaletteData(new RGB[] { new RGB(255, 0, 0) }));

    /**
     * Constructs an image descriptor.
     */
    protected ImageDescriptor() {
        // do nothing
    }

    /**
     * Creates and returns a new image descriptor from a file.
     *
     * @param location the class whose resource directory contain the file
     * @param filename the file name
     * @return a new image descriptor
     */
    public static ImageDescriptor createFromFile(Class<?> location, String filename) {
        return new FileImageDescriptor(location, filename);
    }

    /**
     * Creates and returns a new image descriptor given ImageData
     * describing the image.
     *
     * @since 3.1
     *
     * @param data contents of the image
     * @return newly created image descriptor
     * @deprecated use {@link #createFromImageDataProvider(ImageDataProvider)}
     */
    @Deprecated
	public static ImageDescriptor createFromImageData(ImageData data) {
        return new ImageDataImageDescriptor(data);
    }

    /**
     * Creates and returns a new image descriptor given an ImageDataProvider
     * describing the image.
     *
     * @param provider contents of the image
     * @return newly created image descriptor
     * @since 3.13
     */
    public static ImageDescriptor createFromImageDataProvider(ImageDataProvider provider) {
    	return new ImageDataImageDescriptor(provider);
    }

    /**
     * Creates and returns a new image descriptor for the given image. Note
     * that disposing the original Image will cause the descriptor to become invalid.
     *
     * @since 3.1
     *
     * @param img image to create
     * @return a newly created image descriptor
     */
    public static ImageDescriptor createFromImage(Image img) {
        return new ImageDataImageDescriptor(img);
    }

    /**
     * Creates an ImageDescriptor based on the given original descriptor, but with additional
     * SWT flags.
     *
     * <p>
     * Note that this sort of ImageDescriptor is slower and consumes more resources than
     * a regular image descriptor. It will also never generate results that look as nice as
     * a hand-drawn image. Clients are encouraged to supply their own disabled/grayed/etc. images
     * rather than using a default image and transforming it.
     * </p>
     *
     * @param originalImage image to transform
     * @param swtFlags any flag that can be passed to the flags argument of Image#Image(Device, Image, int)
     * @return an ImageDescriptor that creates new images by transforming the given image descriptor
     *
     * @see Image#Image(Device, Image, int)
     * @since 3.1
     *
     */
    public static ImageDescriptor createWithFlags(ImageDescriptor originalImage, int swtFlags) {
        return new DerivedImageDescriptor(originalImage, swtFlags);
    }

    /**
     * Creates and returns a new image descriptor for the given image. This
     * method takes the Device that created the Image as an argument, allowing
     * the original Image to be reused if the descriptor is asked for another
     * Image on the same device. Note that disposing the original Image will
     * cause the descriptor to become invalid.
     *
     * @deprecated use {@link ImageDescriptor#createFromImage(Image)}
     * @since 3.1
     *
     * @param img image to create
     * @param theDevice the device that was used to create the Image
     * @return a newly created image descriptor
     */
    @Deprecated
	public static ImageDescriptor createFromImage(Image img, Device theDevice) {
        return new ImageDataImageDescriptor(img);
    }

    /**
     * Creates and returns a new image descriptor from a URL.
     *
     * @param url The URL of the image file.
     * @return a new image descriptor
     */
    public static ImageDescriptor createFromURL(URL url) {
        if (url == null) {
            return getMissingImageDescriptor();
        }
        return new URLImageDescriptor(url);
    }

    @Override
	public Object createResource(Device device) throws DeviceResourceException {
        Image result = createImage(false, device);
        if (result == null) {
            throw new DeviceResourceException(this);
        }
        return result;
    }

    @Override
	public void destroyResource(Object previouslyCreatedObject) {
        ((Image)previouslyCreatedObject).dispose();
    }

    /**
	 * Creates and returns a new SWT image for this image descriptor. Note that
	 * each call returns a new SWT image object. The returned image must be
	 * explicitly disposed using the image's dispose call. The image will not be
	 * automatically garbage collected. A default image is returned in the event
	 * of an error.
     *
     * <p>
     * Note: this method differs from createResource(Device) in that the returned image
     * must be disposed directly, whereas an image obtained from createResource(...)
     * must be disposed by calling destroyResource(...). It is not possible to
     * mix-and-match. If you obtained the Image from this method, you must not dispose
     * it by calling destroyResource. Clients are encouraged to use
     * create/destroyResource and downcast the result to Image rather than using
     * createImage.
     * </p>
     *
	 * <p>
	 * Note: it is still possible for this method to return <code>null</code>
	 * in extreme cases, for example if SWT runs out of image handles.
	 * </p>
	 *
	 * @return a new image or <code>null</code> if the image could not be
	 *         created
	 */
    public Image createImage() {
        return createImage(true);
    }

    /**
	 * Creates and returns a new SWT image for this image descriptor. The
	 * returned image must be explicitly disposed using the image's dispose
	 * call. The image will not be automatically garbage collected. In the event
	 * of an error, a default image is returned if
	 * <code>returnMissingImageOnError</code> is true, otherwise
	 * <code>null</code> is returned.
	 * <p>
	 * Note: Even if <code>returnMissingImageOnError</code> is true, it is
	 * still possible for this method to return <code>null</code> in extreme
	 * cases, for example if SWT runs out of image handles.
	 * </p>
	 *
	 * @param returnMissingImageOnError
	 *            flag that determines if a default image is returned on error
	 * @return a new image or <code>null</code> if the image could not be
	 *         created
	 */
    public Image createImage(boolean returnMissingImageOnError) {
        return createImage(returnMissingImageOnError, Display.getCurrent());
    }

    /**
	 * Creates and returns a new SWT image for this image descriptor. The
	 * returned image must be explicitly disposed using the image's dispose
	 * call. The image will not be automatically garbage collected. A default
	 * image is returned in the event of an error.
	 * <p>
	 * Note: it is still possible for this method to return <code>null</code>
	 * in extreme cases, for example if SWT runs out of image handles.
	 * </p>
	 *
	 * @param device
	 *            the device on which to create the image
	 * @return a new image or <code>null</code> if the image could not be
	 *         created
	 * @since 2.0
	 */
    public Image createImage(Device device) {
        return createImage(true, device);
    }

    /**
	 * Creates and returns a new SWT image for this image descriptor. The
	 * returned image must be explicitly disposed using the image's dispose
	 * call. The image will not be automatically garbage collected. In the even
	 * of an error, a default image is returned if
	 * <code>returnMissingImageOnError</code> is true, otherwise
	 * <code>null</code> is returned.
	 * <p>
	 * Note: Even if <code>returnMissingImageOnError</code> is true, it is
	 * still possible for this method to return <code>null</code> in extreme
	 * cases, for example if SWT runs out of image handles.
	 * </p>
	 *
	 * @param returnMissingImageOnError
	 *            flag that determines if a default image is returned on error
	 * @param device
	 *            the device on which to create the image
	 * @return a new image or <code>null</code> if the image could not be
	 *         created
	 * @since 2.0
	 */
	public Image createImage(boolean returnMissingImageOnError, Device device) {
		/*
		 * Try to create the supplied image. If there is an SWT Exception try
		 * and create the default image if that was requested. Return null if
		 * this fails.
		 */
		try {
			return new Image(device, this::getImageData);
		} catch (IllegalArgumentException | SWTException e) {
			if (returnMissingImageOnError) {
				try {
					return new Image(device, DEFAULT_IMAGE_DATA);
				} catch (SWTException e2) {
					return null;
				}
			}
			return null;
		}
	}

	/**
	 * Creates and returns a new SWT <code>ImageData</code> object for this
	 * image descriptor. Note that each call returns a new SWT image data
	 * object.
	 * <p>
	 * This framework method is declared public so that it is possible to
	 * request an image descriptor's image data without creating an SWT image
	 * object.
	 * </p>
	 * <p>
	 * Returns <code>null</code> if the image data could not be created or if no
	 * image data is available for the given zoom level.
	 * </p>
	 * <p>
	 * Since 3.13, subclasses should re-implement this method and should not implement
	 * {@link #getImageData()} any more.
	 * </p>
	 * <p>
	 * <b>Warning:</b> This method does <b>not</b> implement
	 * {@link ImageDataProvider#getImageData(int)}, since legal implementations
	 * of that method must return a non-null value for zoom == 100.
	 * </p>
	 *
	 * @param zoom
	 *            The zoom level in % of the standard resolution (which is 1
	 *            physical monitor pixel / 1 SWT logical point). Typically 100,
	 *            150, or 200.
	 * @return a new image data or <code>null</code>
	 * @since 3.13
	 */
	public ImageData getImageData(int zoom) {
		if (zoom == 100) {
			return getImageData();
		}
		return null;
	}

    /**
     * Creates and returns a new SWT <code>ImageData</code> object
     * for this image descriptor.
     * Note that each call returns a new SWT image data object.
     * <p>
     * This framework method is declared public so that it is
     * possible to request an image descriptor's image data without
     * creating an SWT image object.
     * </p>
     * <p>
     * Returns <code>null</code> if the image data could not be created.
     * </p>
     * <p>
     * This method was abstract until 3.13. Clients should stop re-implementing
     * this method and should re-implement {@link #getImageData(int)} instead.
     * </p>
     *
     * @return a new image data or <code>null</code>
	 * @deprecated Use {@link #getImageData(int)} instead.
	 */
	@Deprecated
	public ImageData getImageData() {
		return getImageData(100);
	}

    /**
     * Returns the shared image descriptor for a missing image.
     *
     * @return the missing image descriptor
     */
    public static ImageDescriptor getMissingImageDescriptor() {
        return MissingImageDescriptor.getInstance();
    }
}
