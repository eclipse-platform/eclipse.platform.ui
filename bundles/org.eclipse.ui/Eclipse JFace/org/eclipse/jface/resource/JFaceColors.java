package org.eclipse.jface.resource;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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

	/**
	 * Get the defualt color to use for displaying errors.
	 */
	public static Color getErrorText(Display display) {

		IPreferenceStore store = JFacePreferences.getPreferenceStore();
		if (store == null)
			//Red is the default
			return display.getSystemColor(SWT.COLOR_RED);
		else
			return new Color(
				display,
				PreferenceConverter.getColor(store, JFacePreferences.ERROR_COLOR));
	}
	
	/**
	 * Get the defualt color to use for displaying hyperlinks.
	 */
	public static Color getHyperlinkText(Display display) {

		IPreferenceStore store = JFacePreferences.getPreferenceStore();
		if (store == null)
			//Blue is the default
			return display.getSystemColor(SWT.COLOR_BLUE);
		else
			return new Color(
				display,
				PreferenceConverter.getColor(store, JFacePreferences.HYPERLINK_COLOR));
	}

}