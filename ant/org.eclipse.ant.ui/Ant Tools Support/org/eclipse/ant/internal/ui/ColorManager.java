/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ant.internal.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * Generic color manager.
 */
public class ColorManager implements ISharedTextColors {

	private static ColorManager fgColorManager;

	private ColorManager() {
	}

	public static ColorManager getDefault() {
		if (fgColorManager == null) {
			fgColorManager = new ColorManager();
		}
		return fgColorManager;
	}

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	@Override
	public Color getColor(RGB rgb) {
		Color color = fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}

	@Override
	public void dispose() {
		for (Color color : fColorTable.values()) {
			color.dispose();
		}
	}
}
