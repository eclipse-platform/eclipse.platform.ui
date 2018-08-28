/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.osgi.service.component.annotations.Component;

/**
 * @since 3.5
 *
 */
@Component
public class ColorAndFontProviderImpl implements IColorAndFontProvider {

	@Override
	public FontData[] getFont(String symbolicName) {
		return getCurrentTheme().getFontRegistry().getFontData(symbolicName);
	}

	@Override
	public RGB getColor(String symbolicName) {
		return getCurrentTheme().getColorRegistry().getRGB(symbolicName);
	}

	private ITheme getCurrentTheme() {
		return Workbench.getInstance().getThemeManager().getCurrentTheme();
	}
}
