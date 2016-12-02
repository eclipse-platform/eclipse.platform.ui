/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * JFaceColors is the class that stores references
 * to all of the colors used by JFace.
 */
public class JFaceColors {

    /**
     * @param display the display the color is from
     * @return the Color used for banner backgrounds
     * @see SWT#COLOR_LIST_BACKGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getBannerBackground(Display display) {
        return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the Color used for banner foregrounds
     * @see SWT#COLOR_LIST_FOREGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getBannerForeground(Display display) {
        return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the background Color for widgets that display errors.
     * @see SWT#COLOR_WIDGET_BACKGROUND
     * @see Display#getSystemColor(int)
     */
    public static Color getErrorBackground(Display display) {
        return display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    }

    /**
     * @param display the display the color is from
     * @return the border Color for widgets that display errors.
     * @see SWT#COLOR_WIDGET_DARK_SHADOW
     * @see Display#getSystemColor(int)
     */
    public static Color getErrorBorder(Display display) {
        return display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying errors.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#ERROR_COLOR
     */
    public static Color getErrorText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.ERROR_COLOR);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying hyperlinks.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#HYPERLINK_COLOR
     */
    public static Color getHyperlinkText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.HYPERLINK_COLOR);
    }

    /**
     * @param display the display the color is from
     * @return the default color to use for displaying active hyperlinks.
     * @see ColorRegistry#get(String)
     * @see JFacePreferences#ACTIVE_HYPERLINK_COLOR
     */
    public static Color getActiveHyperlinkText(Display display) {
        return JFaceResources.getColorRegistry().get(
                JFacePreferences.ACTIVE_HYPERLINK_COLOR);
    }

	/**
	 * Background color intended for widgets that display text.
	 * This color is compatible with with Gtk system themes, for example
	 * on the white theme this color is white and on the dark theme it is dark.
	 * <p>
	 * Note, there is no need to free this color because it's a color managed by
	 * the system not the application.
	 * </p>
	 *
	 * @param display
	 *            the display the color is from
	 * @return Color most suitable for presenting text background depending on
	 *         the platform, to match the rest of the environment.
	 *
	 * @since 3.13
	 */
	public static Color getInformationViewerBackgroundColor(Display display) {
		if (Util.isWin32() || Util.isCocoa()) {
			// Technically COLOR_INFO_* should only be used for tooltips. But on
			// Windows/Cocoa COLOR_INFO_* gives info viewers/hovers a
			// yellow background which is very suitable for information
			// presentation.
			return display.getSystemColor(SWT.COLOR_INFO_BACKGROUND);
		}

		// Technically, COLOR_LIST_* is not the best system color for this
		// because it is only supposed to be used for Tree/List controls. But at
		// the moment COLOR_TEXT_* is not implemented, so this should work for
		// now. See Bug 508612.
		return display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	/**
	 * Foreground color intended for widgets that display text.
	 * This color is compatible with with Gtk system themes, for example
	 * on the white theme this color is black and on the dark theme it is bright.
	 * <p>
	 * Note, there is no need to free this color because it's a color managed by
	 * the system not the application.
	 * </p>
	 *
	 * @param display
	 *            the display the color is from
	 * @return Color most suitable for presenting text foreground depending on
	 *         the platform, to match the rest of the environment.
	 *
	 * @since 3.13
	 */
	public static Color getInformationViewerForegroundColor(Display display) {
		if (Util.isWin32() || Util.isCocoa()) {
			// Technically COLOR_INFO_* should only be used for tooltips. But on
			// Windows/Cocoa COLOR_INFO_* gives info viewers/hovers a
			// yellow background which is very suitable for information
			// presentation.
			return display.getSystemColor(SWT.COLOR_INFO_FOREGROUND);
		}

		// Technically, COLOR_LIST_* is not the best system color for this
		// because it is only supposed to be used for Tree/List controls. But at
		// the moment COLOR_TEXT_* is not implemented, so this should work for
		// now. See Bug 508612.
		return display.getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	}
    /**
     * Clear out the cached color for name. This is generally
     * done when the color preferences changed and any cached colors
     * may be disposed. Users of the colors in this class should add a IPropertyChangeListener
     * to detect when any of these colors change.
     * @param colorName name of the color
     *
     * @deprecated JFaceColors no longer maintains a cache of colors.  This job
     * is now handled by the ColorRegistry.
     */
    @Deprecated
	public static void clearColor(String colorName) {
        //no-op
    }

    /**
     * Dispose of all allocated colors. Called on workbench
     * shutdown.
     *
     * @deprecated JFaceColors no longer maintains a cache of colors.  This job
     * is now handled by the ColorRegistry.
     */
    @Deprecated
	public static void disposeColors() {
        //no-op
    }

    /**
     * Set the foreground and background colors of the
     * control to the specified values. If the values are
     * null than ignore them.
     * @param control the control the foreground and/or background color should be set
     *
     * @param foreground Color the foreground color (maybe <code>null</code>)
     * @param background Color the background color (maybe <code>null</code>)
     */
    public static void setColors(Control control, Color foreground,
            Color background) {
        if (foreground != null) {
			control.setForeground(foreground);
		}
        if (background != null) {
			control.setBackground(background);
		}
    }

}
