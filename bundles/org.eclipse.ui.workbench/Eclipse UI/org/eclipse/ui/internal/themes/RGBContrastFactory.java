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
package org.eclipse.ui.internal.themes;

import java.util.Hashtable;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * A <code>IColorFactory</code> that may be used to select a colour with
 * a higher contrast.  The colors to blend are specified as per method
 * number two in  {@link org.eclipse.core.runtime.IExecutableExtension}.
 * <p>
 * Example usage:
 * <br/>
 * <code>
 * &lt;colorDefinition
 *     label="Red/Blue Contrast"
 *     id="example.redblueblend"&gt;
 *     &lt;colorFactory 
 * 				plugin="org.eclipse.ui" 
 * 				class="org.eclipse.ui.internal.themes.RGBContrastFactory"&gt;
 *      	&lt;parameter name="foreground" value="0,0,0" /&gt;
 *  		&lt;parameter name="background1" value="COLOR_RED" /&gt;
 *          &lt;parameter name="background2" value="COLOR_BLUE" /&gt;
 *     &lt;/colorFactory&gt;
 * &lt;/colorDefinition&gt;
 * </code>
 * </p>
 * 
 * <p>
 * This will select whichever of Red or Blue has a higher contrst with black.
 * The color values may be specified as RGB triples or as SWT constants.
 * </p>
 * 
 * @see org.eclipse.swt.SWT
 * @since 3.0
 */
public class RGBContrastFactory implements IColorFactory, IExecutableExtension {
    private String fg, bg1, bg2;

    /**
     * Returns the intensity of an RGB component using the
     * sRGB gamma function.
     * 
     * @param val Value to convert.
     * @return Light intensity of the component.
     */
    double voltage_to_intensity_srgb(double val) {
        /* Handle invalid values before doing a gamma transform. */
        if (val < 0.0) {
			return 0.0;
		}
        if (val > 1.0) {
			return 1.0;
		}

        if (val <= 0.04045) {
            return val / 12.92;
        }
        return Math.pow((val + 0.055) / 1.055, 2.4);
    }

    /**
     * Returns a measure of the lightness in the perceptual colourspace
     * IPT.
     * 
     * @param color The colour in sRGB
     * @return Lightness in IPT space.
     */
    double lightness(RGB color) {
        double r = voltage_to_intensity_srgb(color.red / 255.0);
        double g = voltage_to_intensity_srgb(color.green / 255.0);
        double b = voltage_to_intensity_srgb(color.blue / 255.0);
        double l = (0.3139 * r) + (0.6395 * g) + (0.0466 * b);
        double m = (0.1516 * r) + (0.7482 * g) + (0.1000 * b);
        double s = (0.0177 * r) + (0.1095 * g) + (0.8729 * b);
        double lp, mp, sp;

        if (l < 0.0) {
			lp = -Math.pow(-l, 0.43);
		} else {
			lp = Math.pow(l, 0.43);
		}
        if (m < 0.0) {
			mp = -Math.pow(-m, 0.43);
		} else {
			mp = Math.pow(m, 0.43);
		}
        if (s < 0.0) {
			sp = -Math.pow(-s, 0.43);
		} else {
			sp = Math.pow(s, 0.43);
		}

        return (0.4000 * lp) + (0.4000 * mp) + (0.2000 * sp);
    }

    public RGB createColor() {
        /**
         * Determine which pair has a higher contrast by selecting
         * the colour with the furthest distance in lightness.
         */
        RGB cfg, cbg1, cbg2;

        if (fg != null) {
            cfg = ColorUtil.getColorValue(fg);
        } else {
            cfg = new RGB(255, 255, 255);
        }
        if (bg1 != null) {
            cbg1 = ColorUtil.getColorValue(bg1);
        } else {
            cbg1 = new RGB(0, 0, 0);
        }
        if (bg2 != null) {
            cbg2 = ColorUtil.getColorValue(bg2);
        } else {
            cbg2 = new RGB(0, 0, 0);
        }

        double lfg = lightness(cfg);
        double lbg1 = lightness(cbg1);
        double lbg2 = lightness(cbg2);

        if (Math.abs(lbg1 - lfg) > Math.abs(lbg2 - lfg)) {
            return cbg1;
        } else {
            return cbg2;
        }
    }

    /**
     * This executable extension requires parameters to be explicitly declared 
     * via the second method described in the <code>IExecutableExtension</code> 
     * documentation.  This class expects that there will be three parameters, 
     * <code>foreground</code>, <code>background1</code> and
     * <code>background2</code>, that describe the two colors to be blended.
     * These values may either be RGB triples or SWT constants.
     * 
     * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement, java.lang.String, java.lang.Object)
     */
    public void setInitializationData(IConfigurationElement config,
            String propertyName, Object data) throws CoreException {
        if (data instanceof Hashtable) {
            Hashtable table = (Hashtable) data;
            fg = (String) table.get("foreground"); //$NON-NLS-1$
            bg1 = (String) table.get("background1"); //$NON-NLS-1$
            bg2 = (String) table.get("background2"); //$NON-NLS-1$
        }

    }
}
