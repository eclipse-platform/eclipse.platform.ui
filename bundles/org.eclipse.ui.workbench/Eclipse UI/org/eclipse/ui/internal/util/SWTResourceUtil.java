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

package org.eclipse.ui.internal.util;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * SWTResourceUtil is a class that holds onto Colors, Fonts and Images and
 * disposes them on shutdown.
 */
public class SWTResourceUtil {

    /**
     * The cache of images that have been dispensed by this provider. Maps
     * ImageDescriptor->Image. Caches are all static to avoid creating extra
     * system resources for very common images, font and colors.
     */
    private static Map imageTable = new Hashtable(40);

    /**
     * The cache of colors that have been dispensed by this provider. Maps
     * RGB->Color.
     */
    private static Map colorTable = new Hashtable(7);

    /**
     * The cache of fonts that have been dispensed by this provider. Maps
     * FontData->Font.
     */
    private static Map fontTable = new Hashtable(7);

    /**
     * Disposes of all allocated images, colors and fonts when shutting down the
     * plug-in.
     */
    public static final void shutdown() {
        if (imageTable != null) {
            for (Iterator i = imageTable.values().iterator(); i.hasNext();) {
                ((Image) i.next()).dispose();
            }
            imageTable = null;
        }
        if (colorTable != null) {
            for (Iterator i = colorTable.values().iterator(); i.hasNext();) {
                ((Color) i.next()).dispose();
            }
            colorTable = null;
        }
        if (fontTable != null) {
            for (Iterator i = fontTable.values().iterator(); i.hasNext();) {
                ((Font) i.next()).dispose();
            }
            fontTable = null;
        }
    }

    /**
     * Get the Map of RGBs to Colors.
     * @return Returns the colorTable.
     */
    public static Map getColorTable() {
        return colorTable;
    }

    /**
     * Return the map of FontDatas to Fonts. 
     * @return Returns the fontTable.
     */
    public static Map getFontTable() {
        return fontTable;
    }

    /**
     * Return the map of ImageDescriptors to Images.
     * @return Returns the imageTable.
     */
    public static Map getImageTable() {
        return imageTable;
    }
}
