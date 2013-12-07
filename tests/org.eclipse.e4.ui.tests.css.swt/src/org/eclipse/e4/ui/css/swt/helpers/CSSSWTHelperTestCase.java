/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.COLOR_DEFINITION_MARKER;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.FONT_DEFINITION_MARKER;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.impl.dom.CSSValueImpl;
import org.eclipse.e4.ui.internal.css.swt.CSSActivator;
import org.eclipse.e4.ui.internal.css.swt.definition.IColorAndFontProvider;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public abstract class CSSSWTHelperTestCase extends TestCase {
	protected void registerFontProviderWith(String expectedSymbolicName,
			String family, int size, int style) throws Exception {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(new FontData[] { new FontData(family, size, style) }).when(
				provider).getFont(expectedSymbolicName);
		registerProvider(provider);
	}

	protected void registerColorProviderWith(String expectedSymbolicName,
			RGB rgb) throws Exception {
		IColorAndFontProvider provider = mock(IColorAndFontProvider.class);
		doReturn(rgb).when(provider).getColor(expectedSymbolicName);
		registerProvider(provider);
	}

	private void registerProvider(final IColorAndFontProvider provider) throws Exception {
		new CSSActivator() {
			@Override
			public IColorAndFontProvider getColorAndFontProvider() {
				return provider;
			};
		}.start(null);
	}

	protected CSS2FontProperties fontProperties(String family) throws Exception {
		return fontProperties(family, null, null);
	}

	protected CSS2FontProperties fontProperties(String family, Object size,
			Object style) throws Exception {
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
