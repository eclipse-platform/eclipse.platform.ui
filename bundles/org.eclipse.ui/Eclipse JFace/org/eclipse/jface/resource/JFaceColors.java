package org.eclipse.jface.resource;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * JFaceColors is the class that stores references
 * to all of the colors used by JFace.
 */

public class JFaceColors {

	/**
	 * Get the Color used for banner backgrounds
	 */

	public static Color getBannerBackground(Display display) {
		return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	/**
	 * Get the background Color for widgets that
	 * display errors.
	 */

	public static Color getErrorBackground(Display display) {
		return display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	/**
	 * Get the border Color for widgets that
	 * display errors.
	 */

	public static Color getErrorBorder(Display display) {
		return display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
	}

}