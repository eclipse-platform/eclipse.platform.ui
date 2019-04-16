/*******************************************************************************
 * Copyright (c) 2016, 2017 Leo Ufimtsev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Leo Ufimtsev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Hashtable;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.ui.themes.IColorFactory;

/**
 * Used in org.eclipse.ui.themes extension point of the org.eclipse.ui plug-in
 * for the color definition of elements that provide text information.
 *
 * This provides colors that are easily readable across different platforms.
 * <br>
 * Win32 & Cocoa: Yellow background & black text. (COLOR_INFO_*) <br>
 * GTK: White background and black text. (COLOR_LIST_*)
 */
public class RGBInfoColorFactory implements IColorFactory, IExecutableExtension {
	String color;

	@Override
	public RGB createColor() {
		RGB rgb = new RGB(0, 0, 0); // IColorFactory must return a valid color

		if (color == null) {
			return rgb;
		}
		/**
		 * Starting with ~Gnome 3.06, COLOR_INFO_BACKGROUND and COLOR_INFO_FOREGROUND
		 * are inverted, often producing hoverboxes with black background with white
		 * text on an otherwise white background and black text. However, on
		 * Windows/Cocoa COLOR_INFO_* looks ok. Solution is to generate a different
		 * color based on platform.
		 */
		if (Util.isGtk()) {
			switch (color) {
			case "foreground": //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_LIST_FOREGROUND"); //$NON-NLS-1$
				break;
			case "background": //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_LIST_BACKGROUND"); //$NON-NLS-1$
				break;
			}
		} else {
			switch (color) {
			case "foreground": //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_INFO_FOREGROUND"); //$NON-NLS-1$
				break;
			case "background": //$NON-NLS-1$
				rgb = ColorUtil.getColorValue("COLOR_INFO_BACKGROUND"); //$NON-NLS-1$
				break;
			}
		}
		return rgb;
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) {
		if (data instanceof Hashtable<?, ?>) {
			Hashtable<?, ?> map = (Hashtable<?, ?>) data;
			color = (String) map.get("color"); //$NON-NLS-1$
		}
	}
}