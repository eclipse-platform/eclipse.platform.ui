/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 443094
 *******************************************************************************/

package org.eclipse.e4.ui.css.swt.helpers;

import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.PROPS_OVERRIDDEN_BY_CSS_PROP;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.SEPARATOR;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.appendOverriddenPropertyName;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.getOverriddenPropertyNames;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.getPreferenceChangeListener;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.removeOverriddenByCssProperty;
import static org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.removeOverriddenPropertyNames;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.junit.Test;

public class EclipsePreferencesHelperTest {
	@Test
	public void testAppendOverriddenPropertyName() {
		// given
		IEclipsePreferences preferences = spy(new EclipsePreferences());

		// when
		appendOverriddenPropertyName(preferences, "prop1");
		appendOverriddenPropertyName(preferences, "prop2");
		appendOverriddenPropertyName(preferences, "prop3");

		String overriddenPreferences = preferences.get(
				PROPS_OVERRIDDEN_BY_CSS_PROP, "");

		// then
		assertTrue(overriddenPreferences.contains(SEPARATOR + "prop1"
				+ SEPARATOR));
		assertTrue(overriddenPreferences.contains(SEPARATOR + "prop2"
				+ SEPARATOR));
		assertTrue(overriddenPreferences.contains(SEPARATOR + "prop3"
				+ SEPARATOR));

		verify(preferences, times(1)).addPreferenceChangeListener(
				getPreferenceChangeListener());
	}

	@Test
	public void testGetOverriddenPropertyNames() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();
		appendOverriddenPropertyName(preferences, "prop1");
		appendOverriddenPropertyName(preferences, "prop2");
		appendOverriddenPropertyName(preferences, "prop3");

		// when
		List<String> propertyNames = getOverriddenPropertyNames(preferences);

		// then
		assertEquals(3, propertyNames.size());
		assertTrue(propertyNames.add("prop1"));
		assertTrue(propertyNames.add("prop2"));
		assertTrue(propertyNames.add("prop3"));
	}

	@Test
	public void testRemoveOverriddenPropertyNames() {
		// given
		IEclipsePreferences preferences = spy(new EclipsePreferences());
		appendOverriddenPropertyName(preferences, "prop1");

		// when
		removeOverriddenPropertyNames(preferences);

		// then
		assertNull(preferences.get(PROPS_OVERRIDDEN_BY_CSS_PROP, null));

		verify(preferences, times(1)).removePreferenceChangeListener(
				getPreferenceChangeListener());
	}

	@Test
	public void testRemoveOverriddenByCssProperty() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();

		// when
		appendOverriddenPropertyName(preferences, "prop1");
		appendOverriddenPropertyName(preferences, "prop2");
		appendOverriddenPropertyName(preferences, "prop3");

		removeOverriddenByCssProperty(preferences, "prop2");

		String overriddenPreferences = preferences.get(
				PROPS_OVERRIDDEN_BY_CSS_PROP, "");

		// then
		assertTrue(overriddenPreferences.contains(SEPARATOR + "prop1"
				+ SEPARATOR));
		assertFalse(overriddenPreferences.contains(SEPARATOR + "prop2"
				+ SEPARATOR));
		assertTrue(overriddenPreferences.contains(SEPARATOR + "prop3"
				+ SEPARATOR));
	}
}
