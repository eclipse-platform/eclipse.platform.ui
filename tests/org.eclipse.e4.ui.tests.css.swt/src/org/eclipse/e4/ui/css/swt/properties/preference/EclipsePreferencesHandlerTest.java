/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.preference;

import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.PROPS_OVERRIDDEN_BY_CSS_PROP;
import static org.eclipse.e4.ui.css.swt.properties.preference.EclipsePreferencesHandler.PREFERENCES_PROP;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.preference.EclipsePreferencesElement;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class EclipsePreferencesHandlerTest extends TestCase {
	public void testApplyCSSProperty() throws Exception {
		// given
		CSSEngine engine = mock(CSSEngine.class);

		IEclipsePreferences preferences = new EclipsePreferences();

		EclipsePreferencesElement element = new EclipsePreferencesElement(
				preferences, engine);

		CSSValue value = mock(CSSValue.class);
		doReturn(CSSValue.CSS_PRIMITIVE_VALUE).when(value).getCssValueType();

		EclipsePreferencesHandlerTestable handler = spy(new EclipsePreferencesHandlerTestable());

		// when
		handler.applyCSSProperty(element, PREFERENCES_PROP, value, null, engine);

		// then
		verify(handler, times(1)).overrideProperty(preferences, value);
	}

	public void testApplyCSSPropertyWhenCssValueList() throws Exception {
		// given
		CSSEngine engine = mock(CSSEngine.class);

		IEclipsePreferences preferences = new EclipsePreferences();

		EclipsePreferencesElement element = new EclipsePreferencesElement(
				preferences, engine);

		CSSValue[] values = new CSSValue[] { mock(CSSValue.class),
				mock(CSSValue.class) };

		CSSValueList listValue = mock(CSSValueList.class);
		doReturn(CSSValue.CSS_VALUE_LIST).when(listValue).getCssValueType();
		doReturn(values.length).when(listValue).getLength();
		doReturn(values[0]).when(listValue).item(0);
		doReturn(values[1]).when(listValue).item(1);

		EclipsePreferencesHandlerTestable handler = spy(new EclipsePreferencesHandlerTestable());

		// when
		handler.applyCSSProperty(element, PREFERENCES_PROP, listValue, null,
				engine);

		// then
		verify(handler, times(2)).overrideProperty(
				any(IEclipsePreferences.class), any(CSSValue.class));
		verify(handler, times(1)).overrideProperty(preferences, values[0]);
		verify(handler, times(1)).overrideProperty(preferences, values[1]);
	}

	public void testOverridePropertyWithCSSValue() throws Exception {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();

		CSSValue value1 = mock(CSSValue.class);
		doReturn("name1=value1").when(value1).getCssText();

		CSSValue value2 = mock(CSSValue.class);
		doReturn(" name2  = value2").when(value2).getCssText();

		EclipsePreferencesHandlerTestable handler = spy(new EclipsePreferencesHandlerTestable());

		// when
		handler.overridePropertyUnMocked(preferences, value1);
		handler.overridePropertyUnMocked(preferences, value2);

		// then
		verify(handler, times(1)).overrideProperty(preferences, "name1",
				"value1");
		verify(handler, times(1)).overrideProperty(preferences, "name2",
				"value2");
	}

	public void testOverridePropertyWithNameAndValueSplit() throws Exception {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();

		EclipsePreferencesHandlerTestable handler = new EclipsePreferencesHandlerTestable();

		// when
		handler.overridePropertyUnMocked(preferences, "name", "value");

		// then
		assertEquals("value", preferences.get("name", null));
		assertTrue(preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, "").contains(
				"name"));
	}

	public void testOverridePropertyWithNameAndValueSplitAndNameAlreadyAddedByUser()
			throws Exception {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();
		// pref is already set that means that user has overridden it with the
		// preference dialog
		preferences.put("name", "valueSetByUser");

		EclipsePreferencesHandlerTestable handler = new EclipsePreferencesHandlerTestable();

		// when
		handler.overridePropertyUnMocked(preferences, "name", "value");

		// then
		assertEquals("valueSetByUser", preferences.get("name", null));
		assertNull(preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, null));
	}

	public void testCustomizePreferenceOverriddenByCSS() throws Exception {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();

		EclipsePreferencesHandlerTestable handler = new EclipsePreferencesHandlerTestable();

		// when
		handler.overridePropertyUnMocked(preferences, "name", "value");
		assertEquals("value", preferences.get("name", null));
		assertTrue(preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, "").contains(
				"name"));

		preferences.put("name", "customizedValue");

		// then
		assertEquals("customizedValue", preferences.get("name", null));
		assertFalse(preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, "").contains(
				"name"));
	}

	public static class EclipsePreferencesHandlerTestable extends
	EclipsePreferencesHandler {
		@Override
		public void overrideProperty(IEclipsePreferences preferences,
				CSSValue value) {
		}

		public void overridePropertyUnMocked(IEclipsePreferences preferences,
				CSSValue value) {
			super.overrideProperty(preferences, value);
		}

		@Override
		public void overrideProperty(IEclipsePreferences preferences,
				String name, String value) {
		}

		public void overridePropertyUnMocked(IEclipsePreferences preferences,
				String name, String value) {
			super.overrideProperty(preferences, name, value);
		}
	}
}
