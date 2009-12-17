/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.themes;

import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;

/**
 * A resuable <code>IColorFactory</code> that may be used to blend two colors.  
 * The colors to blend are specified as per method number two in 
 * {@link org.eclipse.core.runtime.IExecutableExtension}.
 * <p>
 * Example usage:
 * <br/>
 * <code>
 * &lt;colorDefinition
 *     label="Red/Blue Blend"
 *     id="example.redblueblend"&gt;
 *     &lt;colorFactory 
 * 				plugin="org.eclipse.ui" 
 * 				class="org.eclipse.ui.themes.RGBBlendColorFactory"&gt;
 *      	&lt;parameter name="color1" value="255,0,0" /&gt;
 *  		&lt;parameter name="color2" value="COLOR_BLUE" /&gt;
 *     &lt;/colorFactory&gt;
 * &lt;/colorDefinition&gt;
 * </code>
 * </p>
 * 
 * <p>
 * The color values may be specified as RGB triples or as SWT constants.
 * </p>
 * 
 * @see org.eclipse.swt.SWT
 * @since 3.0
 */
public class RGBBlendColorFactory implements IColorFactory,
        IExecutableExtension {

    private String color1, color2;

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.IColorFactory#createColor()
     */
    public RGB createColor() {
        if (color1 == null && color2 == null) {
            return new RGB(0, 0, 0);
        } else if (color1 != null && color2 == null) {
            return ColorUtil.getColorValue(color1);
        } else if (color1 == null && color2 != null) {
            return ColorUtil.getColorValue(color2);
        } else {
            RGB rgb1 = ColorUtil.getColorValue(color1);
            RGB rgb2 = ColorUtil.getColorValue(color2);
            return ColorUtil.blend(rgb1, rgb2);
        }
    }

    /**
     * This executable extension requires parameters to be explicitly declared 
     * via the second method described in the <code>IExecutableExtension</code> 
     * documentation.  This class expects that there will be two parameters, 
     * <code>color1</code> and <code>color2</code>, that describe the two colors
     * to be blended.  These values may either be RGB triples or SWT constants.
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {

        if (data instanceof Hashtable) {
            Hashtable table = (Hashtable) data;
            color1 = (String) table.get("color1"); //$NON-NLS-1$
            color2 = (String) table.get("color2"); //$NON-NLS-1$            
        }
    }
}
