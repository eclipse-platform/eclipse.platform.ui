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
package org.eclipse.ui.internal;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.misc.Assert;

/**
 * This class manages the common workbench colors.  
 */
public class WorkbenchColors {
	static private boolean init = false;
	static private HashMap colorMap;
	static private HashMap systemColorMap;
	
	// the base color ID to use for gradients in the workbench
	static private int BASE_COLOR = SWT.COLOR_WHITE;
	
	// colors created for gradients to be disposed on shutdown
	static private Color activeGradientBlend;
	static private Color inactiveGradientBlend;
	
	static private Color[] workbenchColors;
	static private Color activeEditorForeground;
	static private Color[] activeViewGradient;
	static private Color[] deactivatedViewGradient;
	static private Color[] activeEditorGradient;
	//static private Color[] activeNoFocusEditorGradient;
	static private Color[] deactivatedEditorGradient;
	static private Color activeViewForeground;
	static private int[] activeViewPercentages;
	static private int[] deactivatedViewPercentages;
	static private int[] activeEditorPercentages;
	//static private int[] activeNoFocusEditorPercentages;
	static private int[] deactivatedEditorPercentages;
	static private final String CLR_VIEW_GRAD_START = "clrViewGradStart";//$NON-NLS-1$
	static private final String CLR_VIEW_GRAD_END = "clrViewGradEnd";//$NON-NLS-1$
	static private final String CLR_EDITOR_GRAD_START = "clrEditorGradStart";//$NON-NLS-1$
	static private final String CLR_EDITOR_GRAD_END = "clrEditorGradEnd";//$NON-NLS-1$
	static private final String CLR_NOFOCUS_EDITOR_GRAD_END = "clrNoFocusEditorGradEnd";//$NON-NLS-1$

/**
 * Dispose all color pre-allocated by the workbench.
 */
private static void disposeWorkbenchColors() {
	for (int i = 0; i < workbenchColors.length; i++){
		workbenchColors[i].dispose();
	}
	workbenchColors = null;
}

/**
 * Returns the active editor foreground.
 */
//static public Color getActiveEditorForeground() {
//	return activeEditorForeground;
//}

/**
 * Returns the active view foreground.
 */
static public Color getActiveViewForeground() {
	return activeViewForeground;
}

/**
 * Returns the active editor gradient.
 */
//static public Color [] getActiveEditorGradient() {
//	return activeEditorGradient;
//}
/**
 * Returns the active editor gradient end color.
 */
//static public Color getActiveEditorGradientEnd() {
//	Color clr = (Color)systemColorMap.get(CLR_EDITOR_GRAD_END);
//	Assert.isNotNull(clr);
//	return clr;
//}

/**
 * Returns the active no focus editor gradient end color.
 */
static public Color getActiveNoFocusEditorGradientEnd() {
	Color clr = (Color)systemColorMap.get(CLR_NOFOCUS_EDITOR_GRAD_END);
	Assert.isNotNull(clr);
	return clr;
}

/**
 * Returns the active editor gradient percents.
 */
//static public int [] getActiveEditorGradientPercents() {
//	return activeEditorPercentages;
//}
/**
 * Returns the active editor gradient start color.
 */
//static public Color getActiveEditorGradientStart() {
//	Color clr = (Color)systemColorMap.get(CLR_EDITOR_GRAD_START);
//	Assert.isNotNull(clr);
//	return clr;
//}
/**
 * Returns the active no focus editor gradient.
 */
//static public Color [] getActiveNoFocusEditorGradient() {
//	return activeNoFocusEditorGradient;
//}
/**
 * Returns the active no focus editor gradient percents.
 */
//static public int [] getActiveNoFocusEditorGradientPercents() {
//	return activeNoFocusEditorPercentages;
//}
/**
 * Returns the active no focus editor gradient.
 */
//static public Color [] getActiveNoFocusViewGradient() {
//	return activeNoFocusViewGradient;
//}
/**
 * Returns the active no focus editor gradient percents.
 */
//static public int [] getActiveNoFocusViewGradientPercents() {
//	return activeNoFocusViewPercentages;
//}
/**
 * Returns the active gradient for views.
 */
static public Color [] getActiveViewGradient() {
	return activeViewGradient;
}
/**
 * Returns the active view gradient end color.
 */
//static public Color getActiveViewGradientEnd() {
//	Color clr = (Color)systemColorMap.get(CLR_VIEW_GRAD_END);
//	Assert.isNotNull(clr);
//	return clr;
//}
/**
 * Returns the active view gradient percents.
 */
static public int [] getActiveViewGradientPercents() {
	return activeViewPercentages;
}
/**
 * Returns the active view gradient start color.
 */
//static public Color getActiveViewGradientStart() {
//	Color clr = (Color)systemColorMap.get(CLR_VIEW_GRAD_START);
//	Assert.isNotNull(clr);
//	return clr;
//}
/**
 * Returns the gradient for editors when the window
 * is deactivated.
 */
//static public Color [] getDeactivatedEditorGradient() {
//	return deactivatedEditorGradient;
//}
/**
 * Returns the editor gradient percents when the window
 * is deactivated.
 */
//static public int [] getDeactivatedEditorGradientPercents() {
//	return deactivatedEditorPercentages;
//}
/**
 * Returns the gradient for views when the window
 * is deactivated.
 */
static public Color [] getDeactivatedViewGradient() {
	return deactivatedViewGradient;
}
/**
 * Returns the view gradient percents when the window
 * is deactivated.
 */
static public int [] getDeactivatedViewGradientPercents() {
	return deactivatedViewPercentages;
}
/**
 * Returns a color identified by an RGB value.
 */
//static public Color getColor(RGB rgbValue) {
//	Color clr = (Color)colorMap.get(rgbValue);
//	if (clr == null) {
//		Display disp = Display.getDefault();
//		clr = new Color(disp, rgbValue);
//		colorMap.put(rgbValue, clr);
//	}
//	return clr;
//}
/**
 * Returns a system color identified by a SWT constant.
 */
static public Color getSystemColor(int swtId) {
	Integer bigInt = new Integer(swtId);
	Color clr = (Color)systemColorMap.get(bigInt);
	if (clr == null) {
		Display disp = Display.getDefault();
		clr = disp.getSystemColor(swtId);
		systemColorMap.put(bigInt, clr);
	}
	return clr;
}
/**
 * Initialize all colors used in the workbench in case the OS is using
 * a 256 color palette making sure the workbench colors are allocated.
 *
 * This list comes from the designers.
 */
private static void initWorkbenchColors(Display d) {
	if(workbenchColors != null) return;
	
	workbenchColors = new Color[]{
		//Product pallet
		new Color(d,255,255,255),
		new Color(d,255,251,240),
		new Color(d,223,223,191),
		new Color(d,223,191,191),
		new Color(d,192,220,192),
		new Color(d,192,192,192),
		new Color(d,191,191,191),
		new Color(d,191,191,159),
		new Color(d,191,159,191),
		new Color(d,160,160,164),
		new Color(d,159,159,191),
		new Color(d,159,159,159),
		new Color(d,159,159,127),
		new Color(d,159,127,159),
		new Color(d,159,127,127),
		new Color(d,128,128,128),
		new Color(d,127,159,159),
		new Color(d,127,159,127),
		new Color(d,127,127,159),
		new Color(d,127,127,127),
		new Color(d,127,127,95),
		new Color(d,127,95,127),
		new Color(d,127,95,95),
		new Color(d,95,127,127),
		new Color(d,95,127,95),
		new Color(d,95,95,127),
		new Color(d,95,95,95),
		new Color(d,95,95,63),
		new Color(d,95,63,95),
		new Color(d,95,63,63),
		new Color(d,63,95,95),
		new Color(d,63,95,63),
		new Color(d,63,63,95),
		new Color(d,0,0,0),
		//wizban pallet
		new Color(d,195,204,224),
		new Color(d,214,221,235),
		new Color(d,149,168,199),
		new Color(d,128,148,178),
		new Color(d,106,128,158),
		new Color(d,255,255,255),
		new Color(d,0,0,0),
		new Color(d,0,0,0),
		//Perspective 
		new Color(d,132, 130, 132),
		new Color(d,143, 141, 138),
		new Color(d,171, 168, 165),
		//PreferenceDialog and TitleAreaDialog
		new Color(d,230, 226, 221)
	};
}
/**
 * Disposes of the colors. Ignore all
 * system colors as they do not need
 * to be disposed.
 */
static public void shutdown() {
	if (!init)
		return;
	
	disposeWorkbenchColors();
		
	Iterator iter = colorMap.values().iterator();
	while (iter.hasNext()) {
		Color clr = (Color)iter.next();
		if (clr != null) {
			clr.dispose();
		}
	}
	if (activeGradientBlend != null) {
		activeGradientBlend.dispose();
		activeGradientBlend = null;
	}
	if (inactiveGradientBlend != null) {
		inactiveGradientBlend.dispose();
		inactiveGradientBlend = null;
	}
	colorMap.clear();
	systemColorMap.clear();
	init = false;
}
/**
 * Initializes the colors.
 */
static public void startup() {
	if (init)
		return;

	// Initialize the caches first.
	init = true;
	colorMap = new HashMap(10);
	systemColorMap = new HashMap(10);

	Display display = Display.getDefault();
	initWorkbenchColors(display);
	// Define active view gradient using same OS title gradient colors.
	// Color 1 is halfway between the base color (white) and Color 2
	Color base = getSystemColor(BASE_COLOR);
	Color clr2 = JFaceColors.getTabFolderSelectionBackground(display);
	Color clr3 = JFaceColors.getTabFolderInactiveSelectionBackground(display);
	
	activeGradientBlend = createGradient(display, base, clr2);
	//inactiveGradientBlend = createGradient(display, base, clr3);
	
	// TODO: 
	// temporarily experiment with brighter color here
	//inactiveGradientBlend = new Color(display, red, green, blue);
	inactiveGradientBlend = new Color(display, base.getRed(), base.getGreen(), base.getBlue()); 
	
		
	systemColorMap.put(CLR_VIEW_GRAD_START, activeGradientBlend);
	systemColorMap.put(CLR_VIEW_GRAD_END, clr2);

	// Define active editor gradient using same OS title gradient colors.
	systemColorMap.put(CLR_EDITOR_GRAD_START, activeGradientBlend);
	systemColorMap.put(CLR_EDITOR_GRAD_END, clr2);

	systemColorMap.put(CLR_NOFOCUS_EDITOR_GRAD_END, clr3);

	activeEditorGradient = new Color[] {activeGradientBlend, clr2};
	activeEditorPercentages = new int[] {100};

	// Define active editor foreground color
	activeEditorForeground = JFaceColors.getTabFolderSelectionForeground(Display.getDefault());
	
	// Define active no focus editor gradient
//	activeNoFocusEditorGradient = new Color[] {inactiveGradientBlend, clr3};
//	activeNoFocusEditorPercentages = activeEditorPercentages;
	
	// Define editor gradient for deactivated window using same OS title gradient colors.
	deactivatedEditorGradient = new Color[] {clr3, inactiveGradientBlend};
	deactivatedEditorPercentages = new int[] {100};
	
	// Define view gradient for deactivated window using same OS title gradient colors.
	activeViewGradient = activeEditorGradient;
	activeViewPercentages = activeEditorPercentages;

//	// Define active no focus view gradient
//	activeNoFocusViewGradient = activeNoFocusEditorGradient;
//	activeNoFocusViewPercentages = activeNoFocusEditorPercentages;

	deactivatedViewGradient = deactivatedEditorGradient;
	deactivatedViewPercentages = deactivatedEditorPercentages;

	// Define active view foreground color
	activeViewForeground = activeEditorForeground;
	
	// Preload.
	getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	getSystemColor(SWT.COLOR_BLACK);
}

/**
 * Blend the two color values returning a value that is halfway between them
 * @param temp1
 * @param temp2
 * @return int   the blended value
 */
private static int blend(int temp1, int temp2) {
	return (Math.abs(temp1 - temp2) / 2) + Math.min(temp1,temp2);
}

/**
 * @param color
 * @return
 */
//public static Color createGradient(Display display, Color color) {
//	return createGradient(display, getSystemColor(BASE_COLOR), color);
//}

/**
 * Create an blended gradient color between the base color and the end color returning
 * the blended color.
 * 
 * NOTE: the caller must call <code>Color.dispose()</code> on the returned color
 * 
 * @param display
 * @param startColor
 * @param endColor
 * @return a <code>Color</code>  the blend of the two colors provided
 *
 * @deprecated
 */
public static Color createGradient(Display display, Color startColor, Color endColor) {
	int red = blend(startColor.getRed(), endColor.getRed());
	int green = blend(startColor.getGreen(), endColor.getGreen());
	int blue = blend(startColor.getBlue(), endColor.getBlue());
	return new Color(display, red, green, blue);	
}

/**
 * Create an array to be used in combination with a gradient percent array to render colors 
 * blending a start color with the end color and answering an array with the blended and the 
 * end color as the color start and end for the gradient.
 * 
 * NOTE: the caller must call <code>Color.dispose()</code> on the first element of the array
 * 
 * @param display
 * @param startColor
 * @param endColor
 * @return an array of <code>Color</code> objects representing the gradients
 *
 * @deprecated
 */
public static Color[] createGradientArray(Display display, Color startColor, Color endColor) {
	int red = blend(startColor.getRed(), endColor.getRed());
	int green = blend(startColor.getGreen(), endColor.getGreen());
	int blue = blend(startColor.getBlue(), endColor.getBlue());
	return new Color[] {new Color(display, red, green, blue), endColor};	
}

/**
 * Create an array to be used in combination with a gradient percent array to render colors 
 * blending the default base color with the end color and answering an array with the blended and the 
 * end color as the color start and end for the gradient.
 * 
 * NOTE: the caller must call <code>Color.dispose()</code> on the first element of the array
 * 
 * @param display
 * @param endColor
 * @return an array of <code>Color</code> objects representing the gradients
 * 
 * @deprecated
 */
public static Color[] createGradientArray(Display display, Color endColor) {
	return createGradientArray(display, getSystemColor(BASE_COLOR), endColor);
}

}
