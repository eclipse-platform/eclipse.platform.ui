/*******************************************************************************
 * Copyright (c) 2013, 2026 IBM Corporation and others.
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
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.COLOR_DEFINITION_MARKER;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.FONT_DEFINITION_MARKER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.eclipse.e4.ui.css.core.css2.CSS2FontHelper;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssList;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumber;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssValue;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.osgi.framework.FrameworkUtil;

public abstract class CSSSWTHelperTestCase {

	protected static final String CSS_ITALIC = CSS2FontHelper.getFontStyle(true);
	protected static final String CSS_BOLD = CSS2FontHelper.getFontWeight(true);

	protected void registerFontProviderWith(String expectedSymbolicName, String family, int size, int style) {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(new FontData[] { new FontData(family, size, style) }).when(provider).getFont(expectedSymbolicName);
		registerProvider(provider);
	}

	protected void registerColorProviderWith(String expectedSymbolicName, RGB rgb) {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(rgb).when(provider).getColor(expectedSymbolicName);
		registerProvider(provider);

	}

	private void registerProvider(final IColorAndFontProvider provider) {
		FrameworkUtil.getBundle(getClass()).getBundleContext().registerService(IColorAndFontProvider.class, provider,
				null);
	}

	protected CSS2FontProperties fontProperties(String family) {
		return fontProperties(family, null, null, null);
	}

	protected CSS2FontProperties fontProperties(String family, Object size, Object style, Object weight) {
		CSS2FontProperties result = new CSS2FontPropertiesImpl();
		if (family != null) {
			result.setFamily(new CssText(CssText.Kind.IDENT, family));
		}
		if (size != null) {
			result.setSize(new CssNumber(((Number) size).doubleValue(), true));
			result.setSizeFromCSS(true);
		}
		if (style != null) {
			result.setStyle(new CssText(CssText.Kind.IDENT, style.toString()));
		}
		if (weight != null) {
			result.setWeight(new CssText(CssText.Kind.IDENT, weight.toString()));
		}
		return result;
	}

	/** A string-typed primitive value, the form the engine sees for quoted CSS strings. */
	protected CssPrimitive colorValue(String value) {
		return new CssText(CssText.Kind.STRING, value);
	}

	/** A value that is deliberately not a primitive, for the rejection paths. */
	protected CssValue nonPrimitiveValue(String value) {
		return new CssList(List.of(new CssText(CssText.Kind.STRING, value)));
	}

	protected String addFontDefinitionMarker(String fontDefinitionId) {
		return FONT_DEFINITION_MARKER + fontDefinitionId;
	}

	protected String addColorDefinitionMarker(String colorDefinitionId) {
		return COLOR_DEFINITION_MARKER + colorDefinitionId;
	}
}
