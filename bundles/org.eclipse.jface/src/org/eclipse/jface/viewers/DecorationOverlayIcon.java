/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.function.Supplier;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.Point;

/**
 * A <code>DecorationOverlayIcon</code> is an image descriptor that can be used
 * to overlay decoration images on to the 4 corner quadrants of a base image.
 * The four quadrants are {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
 * {@link IDecoration#BOTTOM_LEFT} and {@link IDecoration#BOTTOM_RIGHT}. Additionally,
 * the overlay can be used to provide an underlay corresponding to {@link IDecoration#UNDERLAY},
 * and to replace the base image with {@link IDecoration#REPLACE} (if supported by the context).
 *
 * @since 3.3
 * @see IDecoration
 */
public class DecorationOverlayIcon extends CompositeImageDescriptor {

	private Object referenceImageOrDescriptor;

    // the overlay images
    private ImageDescriptor[] overlays;

	private ImageDataProvider baseImageDataProvider;

	/**
	 * The size of the base image (that's also the size of this composite image)
	 */
	private Supplier<Point> size;

    /**
     * Create the decoration overlay for the base image using the array of
     * provided overlays. The indices of the array correspond to the values
     * of the 6 overlay constants defined on {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT},
     * {@link IDecoration#UNDERLAY}, and {@link IDecoration#REPLACE}).
     *
     * @param baseImage the base image
     * @param overlaysArray the overlay images, may contain null values
     * @param sizeValue the size of the resulting image
     */
    public DecorationOverlayIcon(Image baseImage,
            ImageDescriptor[] overlaysArray, Point sizeValue) {
		this.referenceImageOrDescriptor = baseImage;
        this.overlays = overlaysArray;
		this.baseImageDataProvider = createCachedImageDataProvider(baseImage);
		this.size = () -> sizeValue;
    }

    /**
     * Create the decoration overlay for the base image using the array of
     * provided overlays. The indices of the array correspond to the values
     * of the 6 overlay constants defined on {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT},
     * {@link IDecoration#UNDERLAY}, and {@link IDecoration#REPLACE}).
     *
     * @param baseImage the base image
     * @param overlaysArray the overlay images, may contain null values
     */
    public DecorationOverlayIcon(Image baseImage, ImageDescriptor[] overlaysArray) {
    	this(baseImage, overlaysArray, new Point(baseImage.getBounds().width, baseImage.getBounds().height));
    }

    /**
     * Create a decoration overlay icon that will place the given overlay icon in
     * the given quadrant of the base image.
	 * @param baseImage the base image
	 * @param overlayImage the overlay image
	 * @param quadrant the quadrant (one of {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
     * or {@link IDecoration#UNDERLAY})
	 */
	public DecorationOverlayIcon(Image baseImage, ImageDescriptor overlayImage, int quadrant) {
		this(baseImage, createArrayFrom(overlayImage, quadrant));
	}

	/**
	 * Create a decoration overlay icon that will place the given overlay icon
	 * in the given quadrant of the base image descriptor.
	 *
	 * @param baseImageDescriptor
	 *            the base image descriptor
	 * @param overlayImageDescriptor
	 *            the overlay image descriptor
	 * @param quadrant
	 *            the quadrant (one of {@link IDecoration}
	 *            ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
	 *            {@link IDecoration#BOTTOM_LEFT},
	 *            {@link IDecoration#BOTTOM_RIGHT} or
	 *            {@link IDecoration#UNDERLAY})
	 * @since 3.13
	 */
	public DecorationOverlayIcon(ImageDescriptor baseImageDescriptor, ImageDescriptor overlayImageDescriptor,
			int quadrant) {
		this.referenceImageOrDescriptor = baseImageDescriptor;
		this.overlays = createArrayFrom(overlayImageDescriptor, quadrant);
		this.baseImageDataProvider = createCachedImageDataProvider(baseImageDescriptor);
		this.size = () -> {
			int zoomLevel = getZoomLevel();
			if (zoomLevel != 0) {
				ImageData data = baseImageDataProvider.getImageData(zoomLevel);
				if (data != null) {
					return new Point(autoScaleDown(data.width), autoScaleDown(data.height));
				}
			}
			ImageData data = baseImageDataProvider.getImageData(100);
			return new Point(data.width, data.height);
		};
	}

	/**
	 * Convert the given image and quadrant into the proper input array.
	 * @param overlayImage the overlay image
	 * @param quadrant the quadrant
	 * @return an array with the given image in the proper quadrant
	 */
	private static ImageDescriptor[] createArrayFrom(
			ImageDescriptor overlayImage, int quadrant) {
		ImageDescriptor[] descs = new ImageDescriptor[] { null, null, null, null, null };
		descs[quadrant] = overlayImage;
		return descs;
	}

	/**
     * Draw the overlays for the receiver.
     * @param overlaysArray
     */
    private void drawOverlays(ImageDescriptor[] overlaysArray) {

        for (int i = 0; i < overlays.length; i++) {
            ImageDescriptor overlay = overlaysArray[i];
            if (overlay == null) {
				continue;
			}
            CachedImageDataProvider overlayImageProvider = createCachedImageDataProvider(overlay);

            switch (i) {
            case IDecoration.TOP_LEFT:
				drawImage(overlayImageProvider, 0, 0);
                break;
            case IDecoration.TOP_RIGHT:
				int overlayWidth = overlayImageProvider.getWidth();
				drawImage(overlayImageProvider, getSize().x - overlayWidth, 0);
                break;
            case IDecoration.BOTTOM_LEFT:
				int overlayHeight = overlayImageProvider.getWidth();
				drawImage(overlayImageProvider, 0, getSize().y - overlayHeight);
                break;
            case IDecoration.BOTTOM_RIGHT:
				overlayWidth = overlayImageProvider.getWidth();
				overlayHeight = overlayImageProvider.getHeight();
				drawImage(overlayImageProvider, getSize().x - overlayWidth, getSize().y - overlayHeight);
                break;
            }
        }
    }

	@Override
	public boolean equals(Object o) {
        if (!(o instanceof DecorationOverlayIcon)) {
			return false;
		}
        DecorationOverlayIcon other = (DecorationOverlayIcon) o;
		return referenceImageOrDescriptor.equals(other.referenceImageOrDescriptor)
                && Arrays.equals(overlays, other.overlays);
    }

    @Override
	public int hashCode() {
		int code = System.identityHashCode(referenceImageOrDescriptor);
        for (ImageDescriptor overlay : overlays) {
            if (overlay != null) {
				code ^= overlay.hashCode();
			}
        }
        return code;
    }

    @Override
	protected void drawCompositeImage(int width, int height) {
    	if (overlays.length > IDecoration.UNDERLAY) {
	        ImageDescriptor underlay = overlays[IDecoration.UNDERLAY];
	        if (underlay != null) {
				drawImage(createCachedImageDataProvider(underlay), 0, 0);
			}
    	}
    	if (overlays.length > IDecoration.REPLACE && overlays[IDecoration.REPLACE] != null) {
    		drawImage(createCachedImageDataProvider(overlays[IDecoration.REPLACE]), 0, 0);
    	} else {
			drawImage(baseImageDataProvider, 0, 0);
    	}
        drawOverlays(overlays);
    }

	@Override
	protected Point getSize() {
		return size.get();
    }

    @Override
	protected int getTransparentPixel() {
		return baseImageDataProvider.getImageData(100).transparentPixel;
    }

}
