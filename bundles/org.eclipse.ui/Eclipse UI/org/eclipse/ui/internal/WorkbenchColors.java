package org.eclipse.ui.internal;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.ui.internal.misc.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import java.util.*;

/**
 * This class manages the common workbench colors.  
 */
public class WorkbenchColors {
	static private boolean init = false;
	static private HashMap colorMap;
	static private HashMap systemColorMap;
	static private Color [] activeViewGradient;
	static private Color [] activeEditorGradient;
	static private Color [] activeNoFocusEditorGradient;
	static private int [] activeViewPercentages;
	static private int [] activeEditorPercentages;
	static private int [] activeNoFocusEditorPercentages;
	static private final String CLR_VIEW_GRAD_START = "clrViewGradStart";
	static private final String CLR_VIEW_GRAD_END = "clrViewGradEnd";
	static private final String CLR_EDITOR_GRAD_START = "clrEditorGradStart";
	static private final String CLR_EDITOR_GRAD_END = "clrEditorGradEnd";
/**
 * Returns the active editor gradient.
 */
static public Color [] getActiveEditorGradient() {
	return activeEditorGradient;
}
/**
 * Returns the active editor gradient end color.
 */
static public Color getActiveEditorGradientEnd() {
	Color clr = (Color)systemColorMap.get(CLR_EDITOR_GRAD_END);
	Assert.isNotNull(clr);
	return clr;
}
/**
 * Returns the active editor gradient percents.
 */
static public int [] getActiveEditorGradientPercents() {
	return activeEditorPercentages;
}
/**
 * Returns the active editor gradient start color.
 */
static public Color getActiveEditorGradientStart() {
	Color clr = (Color)systemColorMap.get(CLR_EDITOR_GRAD_START);
	Assert.isNotNull(clr);
	return clr;
}
/**
 * Returns the active no focus editor gradient.
 */
static public Color [] getActiveNoFocusEditorGradient() {
	return activeNoFocusEditorGradient;
}
/**
 * Returns the active no focus editor gradient percents.
 */
static public int [] getActiveNoFocusEditorGradientPercents() {
	return activeNoFocusEditorPercentages;
}
/**
 * Returns the active gradient for views.
 */
static public Color [] getActiveViewGradient() {
	return activeViewGradient;
}
/**
 * Returns the active view gradient end color.
 */
static public Color getActiveViewGradientEnd() {
	Color clr = (Color)systemColorMap.get(CLR_VIEW_GRAD_END);
	Assert.isNotNull(clr);
	return clr;
}
/**
 * Returns the active view gradient percents.
 */
static public int [] getActiveViewGradientPercents() {
	return activeViewPercentages;
}
/**
 * Returns the active view gradient start color.
 */
static public Color getActiveViewGradientStart() {
	Color clr = (Color)systemColorMap.get(CLR_VIEW_GRAD_START);
	Assert.isNotNull(clr);
	return clr;
}
/**
 * Returns a color identified by an RGB value.
 */
static public Color getColor(RGB rgbValue) {
	Color clr = (Color)colorMap.get(rgbValue);
	if (clr == null) {
		Display disp = Display.getDefault();
		clr = new Color(disp, rgbValue);
		colorMap.put(rgbValue, clr);
	}
	return clr;
}
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
 * Disposes of the colors. Ignore all
 * system colors as they do not need
 * to be disposed.
 */
static public void shutdown() {
	if (!init)
		return;
		
	Iterator iter = colorMap.values().iterator();
	while (iter.hasNext()) {
		Color clr = (Color)iter.next();
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
	if (init)
		return;

	// Initialize the caches first.
	init = true;
	colorMap = new HashMap(10);
	systemColorMap = new HashMap(10);

	// Define active view gradient using same OS title gradient colors.
	Color clr1 = getSystemColor(SWT.COLOR_TITLE_BACKGROUND);
	Color clr2 = getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT);
	Color clr3 = getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	systemColorMap.put(CLR_VIEW_GRAD_START, clr1);
	systemColorMap.put(CLR_VIEW_GRAD_END, clr3);
	activeViewGradient = new Color[] {clr1, clr2, clr3};
	activeViewPercentages = new int[] {50, 100};

	// Define active editor gradient using same OS title gradient colors.
	systemColorMap.put(CLR_EDITOR_GRAD_START, clr1);
	systemColorMap.put(CLR_EDITOR_GRAD_END, null);	// use widget default background
	activeEditorGradient = new Color[] {clr1, clr2, null, null};
	activeEditorPercentages = new int[] {50, 90, 100};
	
	// Define active no focus editor gradient
	activeNoFocusEditorGradient = new Color[] {getSystemColor(SWT.COLOR_WHITE)};
	activeNoFocusEditorPercentages = new int[0];
	
	// Preload.
	getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
	getSystemColor(SWT.COLOR_BLACK);
}
}
