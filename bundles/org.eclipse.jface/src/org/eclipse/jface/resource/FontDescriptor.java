/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;

/**
 * Lightweight descriptor for a font. Creates the described font on demand.
 * Subclasses can implement different ways of describing a font. These objects
 * will be compared, so hashCode(...) and equals(...) must return something 
 * meaningful.
 * 
 * @since 3.1
 */
public abstract class FontDescriptor extends DeviceResourceDescriptor {
    
    /**
     * Creates a FontDescriptor that describes an existing font. The resulting
     * descriptor depends on the Font. Disposing the Font while the descriptor
     * is still in use may throw a graphic disposed exception.
     * 
     * @since 3.1
     *
     * @param font a font to describe
     * @param originalDevice must be the same Device that was passed into
     * the font's constructor when it was first created.
     * @return a newly created FontDescriptor.
     */
    public static FontDescriptor createFrom(Font font, Device originalDevice) {
        return new ArrayFontDescriptor(font, originalDevice);
    }
    
    /**
     * Creates a FontDescriptor that describes an existing font. The resulting
     * descriptor depends on the original Font, and disposing the original Font
     * while the descriptor is still in use may cause SWT to throw a graphic
     * disposed exception. This is less efficient than <code>createFrom(Font, Device)
     * </code>, so the other version should be used whenever possible.
     * 
     * @since 3.1
     *
     * @param font font to create
     * @return a newly created FontDescriptor that describes the given font
     */
    public static FontDescriptor createFrom(Font font) {
        return new ArrayFontDescriptor(font);
    }
    
    /**
     * Creates a new FontDescriptor given the an array of FontData that describes 
     * the font.
     * 
     * @since 3.1
     *
     * @param data an array of FontData that describes the font (will be passed into
     * the Font's constructor)
     * @return a FontDescriptor that describes the given font
     */
    public static FontDescriptor createFrom(FontData[] data) {
        return new ArrayFontDescriptor(data);
    }
    
    /**
     * Creates a new FontDescriptor given the associated FontData
     * 
     * @param data FontData describing the font to create
     * @return a newly created FontDescriptor
     */
    public static FontDescriptor createFrom(FontData data) {
        return new NamedFontDescriptor(data);
    }
    
    /**
     * Creates a new FontDescriptor given an OS-specific font name, height, and style.
     * 
     * @see Font#Font(org.eclipse.swt.graphics.Device, java.lang.String, int, int)
     *
     * @param name os-specific font name
     * @param height height (pixels)
     * @param style a bitwise combination of NORMAL, BOLD, ITALIC 
     * @return a new FontDescriptor
     */
    public static FontDescriptor createFrom(String name, int height, int style) {
        return createFrom(new FontData(name, height, style));
    }
    
    /**
     * Creates the Font described by this descriptor. 
     * 
     * @since 3.1 
     *
     * @param device device on which to allocate the font
     * @return a newly allocated Font (never null)
     * @throws DeviceResourceException if unable to allocate the Font
     */
    public abstract Font createFont(Device device) throws DeviceResourceException;
    
    /**
     * Deallocates anything that was allocated by createFont, given a font
     * that was allocated by an equal FontDescriptor.
     * 
     * @since 3.1 
     *
     * @param previouslyCreatedFont previously allocated font
     */
    public abstract void destroyFont(Font previouslyCreatedFont);
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#create(org.eclipse.swt.graphics.Device)
     */
    public final Object createResource(Device device) throws DeviceResourceException {
        return createFont(device);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.DeviceResourceDescriptor#destroy(java.lang.Object)
     */
    public final void destroyResource(Object previouslyCreatedObject) {
        destroyFont((Font)previouslyCreatedObject);
    }
}
