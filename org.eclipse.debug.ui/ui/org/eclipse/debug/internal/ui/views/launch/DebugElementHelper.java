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
package org.eclipse.debug.internal.ui.views.launch;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.internal.ui.DelegatingModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;

/**
 * Translates images, colors, and fonts into image descriptors, RGBs, and font
 * datas for workbench adapaters. Also provides labels. 
 * 
 * @since 3.1
 */
public class DebugElementHelper {
    
    // a model presentation that can provide images & labels for debug elements
    private static DelegatingModelPresentation fgPresenetation;
    
    // map of images to image descriptors
    private static Map fgImages = new HashMap();
    
    /**
     * Disposes this adapater
     */
    public static void dispose() {
        fgImages.clear();
        if (fgPresenetation != null) {
            fgPresenetation.dispose();
            fgPresenetation = null;
        }
    }

    /**
     * Returns an image descriptor for the given debug element.
     * 
     * @param object object for which an image descriptor is required
     */
    public static ImageDescriptor getImageDescriptor(Object object) {
        Image image = getPresentation().getImage(object);
        if (image != null) {
            ImageDescriptor descriptor = (ImageDescriptor) fgImages.get(image);
            if (descriptor == null) {
                descriptor = new ImageImageDescriptor(image);
                fgImages.put(image, descriptor);
            }
            return descriptor;
        }
        return null;
    }

    /**
     * Returns a label for the given debug element.
     * 
     * @param o object for which a label is required
     */
    public static String getLabel(Object o) {
        return getPresentation().getText(o);
    }
    
    /**
     * Returns a model presentation to use to retrieve lables & images.
     * 
     * @return a model presentation to use to retrieve lables & images
     */
    public static DelegatingModelPresentation getPresentation() {
        if (fgPresenetation == null) {
            fgPresenetation = new DelegatingModelPresentation();
        }
        return fgPresenetation;
    }

    /**
     * Returns the RGB of the foreground color for the given element, or
     * <code>null</code> if none.
     * 
     * @param element object for which a foreground color is required
     * @return the RGB of the foreground color for the given element, or
     * <code>null</code> if none
     */
    public static RGB getForeground(Object element) {
        Color color = getPresentation().getForeground(element);
        if (color != null) {
            return color.getRGB();
        }
        return null;
    }

    /**
     * Returns the RGB of the background color for the given element, or
     * <code>null</code> if none.
     * 
     * @param element object for which a background color is required
     * @return the RGB of the background color for the given element, or
     * <code>null</code> if none
     */
    public static RGB getBackground(Object element) {
        Color color = getPresentation().getBackground(element);
        if (color != null) {
            return color.getRGB();
        }
        return null;
    }

    /**
     * Returns the font data for the given element, or
     * <code>null</code> if none.
     * 
     * @param element object for which font data is required
     * @return the font data for the given element, or
     * <code>null</code> if none
     */
    public static FontData getFont(Object element) {
        Font font = getPresentation().getFont(element);
        if (font != null) {
            return font.getFontData()[0];
        }
        return null;
    }
}
