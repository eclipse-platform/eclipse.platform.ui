/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.internal.util.Util;

/**
 * An OverlayIcon consists of a main icon and an overlay icon
 */
public class OverlayIcon extends CompositeImageDescriptor {

    // the size of the OverlayIcon
    private Point fSize = null;

    // the main image
    private ImageDescriptor fBase = null;

    // the additional image (a pin for example)
    private ImageDescriptor fOverlay = null;

    /**
     * @param base the main image
     * @param overlay the additional image (a pin for example)
     * @param size the size of the OverlayIcon
     */
    public OverlayIcon(ImageDescriptor base, ImageDescriptor overlay, Point size) {
        fBase = base;
        fOverlay = overlay;
        fSize = size;
    }

    @Override
	protected void drawCompositeImage(int width, int height) {
        ImageData bg;
        if (fBase == null || (bg = fBase.getImageData()) == null) {
			bg = DEFAULT_IMAGE_DATA;
		}
        drawImage(bg, 0, 0);

        if (fOverlay != null) {
			drawTopRight(fOverlay);
		}
    }

    /**
     * @param overlay the additional image (a pin for example)
     * to be drawn on top of the main image
     */
    protected void drawTopRight(ImageDescriptor overlay) {
        if (overlay == null) {
			return;
		}
        int x = getSize().x;
        ImageData id = overlay.getImageData();
        x -= id.width;
        drawImage(id, x, 0);
    }

    @Override
	protected Point getSize() {
        return fSize;
    }

    @Override
	public int hashCode() {
        return Util.hashCode(fBase) * 17 + Util.hashCode(fOverlay);
    }

    @Override
	public boolean equals(Object obj) {
        if (!(obj instanceof OverlayIcon)) {
			return false;
		}
        OverlayIcon overlayIcon = (OverlayIcon) obj;
        return Util.equals(this.fBase, overlayIcon.fBase)
                && Util.equals(this.fOverlay, overlayIcon.fOverlay)
                && Util.equals(this.fSize, overlayIcon.fSize);
    }
}
