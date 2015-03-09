/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.5
 *
 */
public class ColorAndFontProviderImpl implements IColorAndFontProvider {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider#getFont
	 * (java.lang.String)
	 */
	@Override
	public FontData[] getFont(String symbolicName) {
		return getCurrentTheme().getFontRegistry().getFontData(symbolicName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider#getColor
	 * (java.lang.String)
	 */
	@Override
	public RGB getColor(String symbolicName) {
		return getCurrentTheme().getColorRegistry().getRGB(symbolicName);
	}

	private ITheme getCurrentTheme() {
		return Workbench.getInstance().getThemeManager().getCurrentTheme();
	}
}
