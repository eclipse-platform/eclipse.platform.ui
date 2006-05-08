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
 * Identifies a font by its name, height, and style.
 * 
 * @since 3.1
 */
final class NamedFontDescriptor extends FontDescriptor {

    private FontData data;
    
    /**
     * Creates a font descriptor for a font with the given name, height,
     * and style. These arguments are passed directly to the constructor
     * of Font.
     * 
     * @param data FontData describing the font to create
     * 
     * @see org.eclipse.swt.graphics.Font#Font(org.eclipse.swt.graphics.Device, org.eclipse.swt.graphics.FontData)
     */
    public NamedFontDescriptor(FontData data) {
        this.data = data;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.FontDescriptor#createFont(org.eclipse.swt.graphics.Device)
     */
    public Font createFont(Device device) {
        return new Font(device, data);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if ((obj.getClass() == NamedFontDescriptor.class)) {
            NamedFontDescriptor descr = (NamedFontDescriptor)obj;
            
            return data.equals(descr.data);
        }
        
        return super.equals(obj);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return data.hashCode();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.resource.FontDescriptor#destroyFont(org.eclipse.swt.graphics.Font)
     */
    public void destroyFont(Font previouslyCreatedFont) {
        previouslyCreatedFont.dispose();
    }

}
