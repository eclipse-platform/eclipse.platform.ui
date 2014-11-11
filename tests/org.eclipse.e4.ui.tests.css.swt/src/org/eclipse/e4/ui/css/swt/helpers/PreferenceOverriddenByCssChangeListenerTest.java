/*******************************************************************************
 * Copyright (c) 2008, 2014 IBM Corporation and others. All rights reserved. This
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.core.internal.preferences.EclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper.PreferenceOverriddenByCssChangeListener;
import org.junit.Test;

public class PreferenceOverriddenByCssChangeListenerTest {
	@Test
	public void testPreferenceChangeEvent() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();
		preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP, SEPARATOR + "name"
				+ SEPARATOR);

		PreferenceChangeEvent event = new PreferenceChangeEvent(preferences,
				"name", "oldValue", "newValue");

		PreferenceOverriddenByCssChangeListenerTestable preferenceChangeListener = spy(new PreferenceOverriddenByCssChangeListenerTestable());

		// when
		preferenceChangeListener.preferenceChange(event);

		// then
		verify(preferenceChangeListener, times(1))
		.removeOverriddenByCssProperty(event);
	}

	@Test
	public void testPreferenceChangeEventWhenAddPropertyEvent() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();
		preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP, SEPARATOR + "name"
				+ SEPARATOR);

		PreferenceChangeEvent event = new PreferenceChangeEvent(preferences,
				"name", null, "newValue");

		PreferenceOverriddenByCssChangeListenerTestable preferenceChangeListener = spy(new PreferenceOverriddenByCssChangeListenerTestable());

		// when
		preferenceChangeListener.preferenceChange(event);

		// then
		verify(preferenceChangeListener, never())
		.removeOverriddenByCssProperty(event);
	}

	@Test
	public void testPreferenceChangeEventWhenRemovePropertyEvent() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();
		preferences.put(PROPS_OVERRIDDEN_BY_CSS_PROP, SEPARATOR + "name"
				+ SEPARATOR);

		PreferenceChangeEvent event = new PreferenceChangeEvent(preferences,
				"name", "oldValue", null);

		PreferenceOverriddenByCssChangeListenerTestable preferenceChangeListener = spy(new PreferenceOverriddenByCssChangeListenerTestable());

		// when
		preferenceChangeListener.preferenceChange(event);

		// then
		verify(preferenceChangeListener, never())
		.removeOverriddenByCssProperty(event);
	}

	@Test
	public void testPreferenceChangeEventWhenModifyPropertyEventButPropertyIsNotOverriddenByCss() {
		// given
		IEclipsePreferences preferences = new EclipsePreferences();

		PreferenceChangeEvent event = new PreferenceChangeEvent(preferences,
				"name", "oldValue", "newValue");

		PreferenceOverriddenByCssChangeListenerTestable preferenceChangeListener = spy(new PreferenceOverriddenByCssChangeListenerTestable());

		// when
		preferenceChangeListener.preferenceChange(event);

		// then
		verify(preferenceChangeListener, never())
		.removeOverriddenByCssProperty(event);
	}

	public static class PreferenceOverriddenByCssChangeListenerTestable extends
	PreferenceOverriddenByCssChangeListener {
		@Override
		public void removeOverriddenByCssProperty(PreferenceChangeEvent event) {
		}
	}
}
