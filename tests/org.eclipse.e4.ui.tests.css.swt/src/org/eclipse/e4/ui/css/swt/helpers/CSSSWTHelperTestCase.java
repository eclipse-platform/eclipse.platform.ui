/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.COLOR_DEFINITION_MARKER;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.FONT_DEFINITION_MARKER;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.impl.dom.CSSValueImpl;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public abstract class CSSSWTHelperTestCase {
	protected void registerFontProviderWith(String expectedSymbolicName,
 String family, int size, int style) {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(new FontData[] { new FontData(family, size, style) }).when(
				provider).getFont(expectedSymbolicName);
		registerProvider(provider);
	}

	protected void registerColorProviderWith(String expectedSymbolicName,
			RGB rgb) {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(rgb).when(provider).getColor(expectedSymbolicName);
		registerProvider(provider);

	}

	private void registerProvider(final IColorAndFontProvider provider) {
		try {
			new CSSActivator() {
				@Override
				public IColorAndFontProvider getColorAndFontProvider() {
					return provider;
				};
			}.start(null);
		} catch (Exception e) {
			fail();
		}

	}

	protected CSS2FontProperties fontProperties(String family) {
		return fontProperties(family, null, null);
	}

	protected CSS2FontProperties fontProperties(String family, Object size,
			Object style) {
		CSS2FontProperties result = mock(CSS2FontProperties.class);
		doReturn(valueImpl(family)).when(result).getFamily();
		if (size != null) {
			doReturn(valueImpl(size)).when(result).getSize();
		}
		if (style != null) {
			doReturn(valueImpl(style)).when(result).getStyle();
		}
		return result;
	}

	private CSSValueImpl valueImpl(final Object value) {
		if (value != null) {
			return new CSSValueImpl() {
				@Override
				public String getCssText() {
					return value.toString();
				}

				@Override
				public String getStringValue() {
					return getCssText();
				}

				@Override
				public float getFloatValue(short valueType) throws DOMException {
					return Float.parseFloat(getCssText());
				}
			};
		}
		return null;
	}

	protected CSSValueImpl colorValue(String value) {
		return colorValue(value, CSSValue.CSS_PRIMITIVE_VALUE);
	}

	protected CSSValueImpl colorValue(String value, short type) {
		CSSValueImpl result = mock(CSSValueImpl.class);
		doReturn(CSSPrimitiveValue.CSS_STRING).when(result).getPrimitiveType();
		doReturn(type).when(result).getCssValueType();
		doReturn(value).when(result).getStringValue();
		doReturn(value).when(result).getCssText();
		return result;
	}

	protected String addFontDefinitionMarker(String fontDefinitionId) {
		return FONT_DEFINITION_MARKER + fontDefinitionId;
	}

	protected String addColorDefinitionMarker(String colorDefinitionId) {
		return COLOR_DEFINITION_MARKER + colorDefinitionId;
	}
}
