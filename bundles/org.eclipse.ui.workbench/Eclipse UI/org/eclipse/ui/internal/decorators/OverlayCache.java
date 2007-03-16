/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.decorators;

import java.util.*;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.*;

/**
 * The OverlayCache is a helper class used by the DecoratorManger
 * to manage the lifecycle of overlaid images.
 */
class OverlayCache {
    private Map cache = new HashMap(); /*from OverlayIcon to Image*/

    /**
     * Returns and caches an image corresponding to the specified icon.
     * @param icon the icon
     * @return the image
     */
    Image getImageFor(DecorationOverlayIcon icon) {
        Image image = (Image) cache.get(icon);
        if (image == null) {
            image = icon.createImage();
            cache.put(icon, image);
        }
        return image;
    }

    /**
     * Disposes of all images in the cache.
     */
    void disposeAll() {
        for (Iterator it = cache.values().iterator(); it.hasNext();) {
            Image image = (Image) it.next();
            image.dispose();
        }
        cache.clear();
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
        DecorationOverlayIcon icon = new DecorationOverlayIcon(source,
                descriptors, size);
        return getImageFor(icon);
    }

}
