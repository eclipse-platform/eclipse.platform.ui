package org.eclipse.update.internal.ui.preferences;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.update.internal.ui.UpdateUIPlugin;

public class UpdateColors {
	public static final String P_TOPIC_COLOR = "UpdateColors.topicColor";

	private static Hashtable colorTable = new Hashtable();
	//Keep a list of the Colors we have allocated seperately
	//as system colors do not need to be disposed.
	private static ArrayList allocatedColors = new ArrayList();

	/**
	 * Get the Color used for banner backgrounds
	 */

	public static Color getTopicColor(Display display) {
		return getColorSetting(display, P_TOPIC_COLOR);
	}

	public static void clearColor(String colorName) {
		colorTable.remove(colorName);
	}

	/**
	 * Get the color setting for the name.
	 */
	private static Color getColorSetting(
		Display display,
		String preferenceName) {
		if (colorTable.contains(preferenceName))
			return (Color) colorTable.get(preferenceName);

		IPreferenceStore store =
			UpdateUIPlugin.getDefault().getPreferenceStore();
		if (store == null) {
			Color color = getDefaultColor(display, preferenceName);
			colorTable.put(preferenceName, color);
			return color;
		} else {
			setDefaults(store);
			Color color =
				new Color(
					display,
					PreferenceConverter.getColor(store, preferenceName));
			allocatedColors.add(color);
			colorTable.put(preferenceName, color);
			return color;
		}
	}
	
	public static void setDefaults(IPreferenceStore store) {
		PreferenceConverter.setDefault(store, P_TOPIC_COLOR, new RGB(91, 120, 172));
	}

	/**
	 * Return the default color for the preferenceName. If there is
	 * no setting return the system black.
	 */
	private static Color getDefaultColor(
		Display display,
		String preferenceName) {

		if (preferenceName.equals(P_TOPIC_COLOR)) {
			Color color = new Color(display, 91, 120, 172);
			allocatedColors.add(color);
			return color;
		}
		return display.getSystemColor(SWT.COLOR_BLACK);
	}

	/**
	 * Dispose of all allocated colors. Called on workbench
	 * shutdown.
	 */
	public static void disposeColors() {
		Iterator colors = allocatedColors.iterator();
		while (colors.hasNext()) {
			((Color) colors.next()).dispose();
		}
	}
}