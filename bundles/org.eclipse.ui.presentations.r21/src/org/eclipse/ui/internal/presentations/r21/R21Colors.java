/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.presentations.r21;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * This class manages the R21 workbench colors (they are fixed).
 */
public class R21Colors {

    static private boolean init = false;

    static private HashMap colorMap;

    static private HashMap systemColorMap;

    static private Color workbenchColors[];

    static private Color[] activeViewGradient;

    static private Color[] deactivatedViewGradient;

    static private Color[] activeEditorGradient;

    static private Color[] activeNoFocusEditorGradient;

    static private Color[] deactivatedEditorGradient;

    static private int[] activeViewPercentages;

    static private int[] deactivatedViewPercentages;

    static private int[] activeEditorPercentages;

    static private int[] activeNoFocusEditorPercentages;

    static private int[] deactivatedEditorPercentages;

    static private final String CLR_VIEW_GRAD_START = "clrViewGradStart";//$NON-NLS-1$

    static private final String CLR_VIEW_GRAD_END = "clrViewGradEnd";//$NON-NLS-1$

    static private final String CLR_EDITOR_GRAD_START = "clrEditorGradStart";//$NON-NLS-1$

    static private final String CLR_EDITOR_GRAD_END = "clrEditorGradEnd";//$NON-NLS-1$

    /**
     * Dispose all color pre-allocated by the workbench.
     */
    private static void disposeWorkbenchColors() {
        for (int i = 0; i < workbenchColors.length; i++) {
            workbenchColors[i].dispose();
        }
    }

    /**
     * Returns the active editor gradient.
     * @return an array of colors
     */
    static public Color[] getActiveEditorGradient() {
        return activeEditorGradient;
    }

    /**
     * Returns the active editor gradient end color.
     * @return the color
     */
    static public Color getActiveEditorGradientEnd() {
        Color clr = (Color) systemColorMap.get(CLR_EDITOR_GRAD_END);
        Assert.isNotNull(clr);
        return clr;
    }

    /**
     * Returns the active editor gradient percents.
     * @return an array of ints
     */
    static public int[] getActiveEditorGradientPercents() {
        return activeEditorPercentages;
    }

    /**
     * Returns the active editor gradient start color.
     * @return the color
     */
    static public Color getActiveEditorGradientStart() {
        Color clr = (Color) systemColorMap.get(CLR_EDITOR_GRAD_START);
        Assert.isNotNull(clr);
        return clr;
    }

    /**
     * Returns the active no focus editor gradient.
     * @return an array of colors
     */
    static public Color[] getActiveNoFocusEditorGradient() {
        return activeNoFocusEditorGradient;
    }

    /**
     * Returns the active no focus editor gradient percents.
     * @return an array of ints
     */
    static public int[] getActiveNoFocusEditorGradientPercents() {
        return activeNoFocusEditorPercentages;
    }

    /**
     * Returns the active gradient for views.
     * @return an arry of colors
     */
    static public Color[] getActiveViewGradient() {
        return activeViewGradient;
    }

    /**
     * Returns the active view gradient end color.
     * @return the color
     */
    static public Color getActiveViewGradientEnd() {
        Color clr = (Color) systemColorMap.get(CLR_VIEW_GRAD_END);
        Assert.isNotNull(clr);
        return clr;
    }

    /**
     * Returns the active view gradient percents.
     * @return an arry of ints
     */
    static public int[] getActiveViewGradientPercents() {
        return activeViewPercentages;
    }

    /**
     * Returns the active view gradient start color.
     * @return the color
     */
    static public Color getActiveViewGradientStart() {
        Color clr = (Color) systemColorMap.get(CLR_VIEW_GRAD_START);
        Assert.isNotNull(clr);
        return clr;
    }

    /**
     * Returns the gradient for editors when the window is deactivated.
     * @return an array of colors
     */
    static public Color[] getDeactivatedEditorGradient() {
        return deactivatedEditorGradient;
    }

    /**
     * Returns the editor gradient percents when the window is deactivated.
     * @return an array of ints
     */
    static public int[] getDeactivatedEditorGradientPercents() {
        return deactivatedEditorPercentages;
    }

    /**
     * Returns the gradient for views when the window is deactivated.
     * @return an array of colors
     */
    static public Color[] getDeactivatedViewGradient() {
        return deactivatedViewGradient;
    }

    /**
     * Returns the view gradient percents when the window is deactivated.
     * @return an array of ints
     */
    static public int[] getDeactivatedViewGradientPercents() {
        return deactivatedViewPercentages;
    }

    /**
     * Returns a color identified by an RGB value.
     * @param rgbValue 
     * @return the color 
     */
    static public Color getColor(RGB rgbValue) {
        Color clr = (Color) colorMap.get(rgbValue);
        if (clr == null) {
            Display disp = Display.getDefault();
            clr = new Color(disp, rgbValue);
            colorMap.put(rgbValue, clr);
        }
        return clr;
    }

    /**
     * Returns a system color identified by a SWT constant.
     * @param swtId 
     * @return the color
     */
    static public Color getSystemColor(int swtId) {
        Integer bigInt = new Integer(swtId);
        Color clr = (Color) systemColorMap.get(bigInt);
        if (clr == null) {
            Display disp = Display.getDefault();
            clr = disp.getSystemColor(swtId);
            systemColorMap.put(bigInt, clr);
        }
        return clr;
    }

    /**
     * Initialize all colors used in the workbench in case the OS is using a 256
     * color palette making sure the workbench colors are allocated.
     * 
     * This list comes from the designers.
     */
    private static void initWorkbenchColors(Display d) {
        if (workbenchColors != null) {
			return;
		}

        workbenchColors = new Color[] {
        //Product pallet
                new Color(d, 255, 255, 255), new Color(d, 255, 251, 240),
                new Color(d, 223, 223, 191), new Color(d, 223, 191, 191),
                new Color(d, 192, 220, 192), new Color(d, 192, 192, 192),
                new Color(d, 191, 191, 191), new Color(d, 191, 191, 159),
                new Color(d, 191, 159, 191), new Color(d, 160, 160, 164),
                new Color(d, 159, 159, 191), new Color(d, 159, 159, 159),
                new Color(d, 159, 159, 127), new Color(d, 159, 127, 159),
                new Color(d, 159, 127, 127), new Color(d, 128, 128, 128),
                new Color(d, 127, 159, 159), new Color(d, 127, 159, 127),
                new Color(d, 127, 127, 159), new Color(d, 127, 127, 127),
                new Color(d, 127, 127, 95), new Color(d, 127, 95, 127),
                new Color(d, 127, 95, 95), new Color(d, 95, 127, 127),
                new Color(d, 95, 127, 95), new Color(d, 95, 95, 127),
                new Color(d, 95, 95, 95), new Color(d, 95, 95, 63),
                new Color(d, 95, 63, 95), new Color(d, 95, 63, 63),
                new Color(d, 63, 95, 95), new Color(d, 63, 95, 63),
                new Color(d, 63, 63, 95), new Color(d, 0, 0, 0),
                //wizban pallet
                new Color(d, 195, 204, 224), new Color(d, 214, 221, 235),
                new Color(d, 149, 168, 199), new Color(d, 128, 148, 178),
                new Color(d, 106, 128, 158), new Color(d, 255, 255, 255),
                new Color(d, 0, 0, 0), new Color(d, 0, 0, 0),
                //Perspective
                new Color(d, 132, 130, 132), new Color(d, 143, 141, 138),
                new Color(d, 171, 168, 165),
                //PreferenceDialog and TitleAreaDialog
                new Color(d, 230, 226, 221) };
    }

    /**
     * Disposes of the colors. Ignore all system colors as they do not need to
     * be disposed.
     */
    static public void shutdown() {
        if (!init) {
			return;
		}

        disposeWorkbenchColors();

        Iterator iter = colorMap.values().iterator();
        while (iter.hasNext()) {
            Color clr = (Color) iter.next();
            if (clr != null) {
                clr.dispose();
            }
        }

        colorMap.clear();
        systemColorMap.clear();
        init = false;
    }

    /**
     * Initializes the colors.
     */
    static public void startup() {
        if (init) {
			return;
		}

        // Initialize the caches first.
        init = true;
        colorMap = new HashMap(10);
        systemColorMap = new HashMap(10);

        initWorkbenchColors(Display.getDefault());
        // Define active view gradient using same OS title gradient colors.
        Color clr1 = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
        Color clr2 = getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
        Color clr3 = getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        systemColorMap.put(CLR_VIEW_GRAD_START, clr1);
        systemColorMap.put(CLR_VIEW_GRAD_END, clr3);
        activeViewGradient = new Color[] { clr1, clr2, clr3 };
        activeViewPercentages = new int[] { 50, 100 };

        // Define active editor gradient using same OS title gradient colors.
        systemColorMap.put(CLR_EDITOR_GRAD_START, clr1);
        systemColorMap.put(CLR_EDITOR_GRAD_END, null); // use widget default
        // background
        activeEditorGradient = new Color[] { clr1, clr2, null, null };
        activeEditorPercentages = new int[] { 50, 90, 100 };

        // Define active no focus editor gradient
        activeNoFocusEditorGradient = new Color[] { getSystemColor(SWT.COLOR_LIST_BACKGROUND) };
        activeNoFocusEditorPercentages = new int[0];

        // Define view gradient for deactivated window using same OS title
        // gradient colors.
        clr1 = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND);
        clr2 = getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        clr3 = getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        deactivatedViewGradient = new Color[] { clr1, clr2, clr3 };
        deactivatedViewPercentages = new int[] { 70, 100 };

        // Define editor gradient for deactivated window using same OS title
        // gradient colors.
        deactivatedEditorGradient = new Color[] { clr1, clr2, null, null };
        deactivatedEditorPercentages = new int[] { 70, 95, 100 };

        // Preload.
        getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        getSystemColor(SWT.COLOR_BLACK);
    }
}
