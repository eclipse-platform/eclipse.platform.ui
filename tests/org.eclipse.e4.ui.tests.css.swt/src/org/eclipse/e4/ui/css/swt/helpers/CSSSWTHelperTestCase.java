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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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

import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper.FONT_DEFINITION_MARKER;
import static org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper.COLOR_DEFINITION_MARKER;

@SuppressWarnings("restriction")
public abstract class CSSSWTHelperTestCase extends TestCase {	
	protected void registerFontProviderWith(final String expectedSymbolicName, final String family, final int size, final int style) throws Exception {
		registerProvider(new IColorAndFontProvider() {			
			public FontData[] getFont(String symbolicName) {	
				if (symbolicName.equals(expectedSymbolicName)) {
					return new FontData[]{new FontData(family, size, style)};
				}
				return null;
			}					
			public RGB getColor(String symbolicName) {
				return null;
			}
		});
	}
	
	protected void registerColorProviderWith(final String expectedSymbolicName, final RGB rgb) throws Exception {
		registerProvider(new IColorAndFontProvider() {			
			public FontData[] getFont(String symbolicName) {		
				return null;
			}					
			public RGB getColor(String symbolicName) {		
				if (symbolicName.equals(expectedSymbolicName)) {
					return rgb;
				}
				return null;
			}
		});
	}
	
	private void registerProvider(final IColorAndFontProvider provider) throws Exception {
		new CSSActivator() {
			@Override
			public IColorAndFontProvider getColorAndFontProvider() {
				return provider;
			};
		}.start(null);
	}
	
	protected CSS2FontProperties fontProperties(final String family, final Object size, final Object style) throws Exception {
		return (CSS2FontProperties) Proxy.newProxyInstance(getClass().getClassLoader(),new Class<?>[] {CSS2FontProperties.class}, 
				new InvocationHandler() {			
			public Object invoke(Object arg0, Method method, Object[] arg2)
					throws Throwable {
				if (method.getName().equals("getFamily")) {
					return valueImpl(family);
				} 
				if (method.getName().equals("getSize")) {
					return valueImpl(size);
				} 
				if (method.getName().equals("getStyle")) {
					return valueImpl(style);
				}
				return null;
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
		});				
	}	
		
	protected CSSValueImpl colorProperties(final String value) {
		return colorProperties(value, CSSValue.CSS_PRIMITIVE_VALUE);
	}
	
	protected CSSValueImpl colorProperties(final String value, final short type) {
		return new CSSValueImpl() {
			@Override
			public short getPrimitiveType() {
				return CSSPrimitiveValue.CSS_STRING;
			}
			@Override
			public short getCssValueType() {
				return type;
			}				
			@Override
			public String getStringValue() throws DOMException {
				return value;
			}
		};
	}
	
	protected String fontDefinition(String fontDefinitionId) {
		return FONT_DEFINITION_MARKER + fontDefinitionId;
	}
	
	protected String colorDefinition(String colorDefinitionId) {
		return COLOR_DEFINITION_MARKER + colorDefinitionId;
	}
}
