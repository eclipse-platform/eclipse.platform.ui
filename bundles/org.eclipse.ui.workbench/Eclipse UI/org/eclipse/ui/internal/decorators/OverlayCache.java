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
package org.eclipse.ui.internal.decorators;

import org.eclipse.jface.resource.ImageCache;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * The OverlayCache is a helper class used by the DecoratorManger
 * to manage the lifecycle of overlaid images.
 */
class OverlayCache {
    private ImageCache imageCache = new ImageCache(); /*from OverlayIcon to Image*/

    /**
     * Returns and caches an image corresponding to the specified icon.
     * @param icon the icon
     * @return the image
     */
    Image getImageFor(DecoratorOverlayIcon icon) {
    	return imageCache.getImage(icon);
    }

    /**
     * Disposes of all images in the cache.
     */
    void disposeAll() {
        imageCache.dispose();
    }

    /**
     * Apply the descriptors for the receiver to the supplied
     * image.
     * @param source
     * @param descriptors
     * @return Image
     */

    Image applyDescriptors(Image source, ImageDescriptor[] descriptors) {
        Rectangle bounds = source.getBounds();
        Point size = new Point(bounds.width, bounds.height);
        DecoratorOverlayIcon icon = new DecoratorOverlayIcon(source,
                descriptors, size);
        return getImageFor(icon);
    }

}