/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.preference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.preference.EclipsePreferencesElement;
import org.eclipse.e4.ui.css.swt.helpers.EclipsePreferencesHelper;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class EclipsePreferencesHandler implements ICSSPropertyHandler {
	public final static String PREFERENCES_PROP = "preferences";

	private final static Pattern PROPERTY_NAME_AND_VALUE_PATTERN = Pattern
			.compile("(.+)\\s*=\\s*(.*)");

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!property.equals(PREFERENCES_PROP)
				|| !(element instanceof EclipsePreferencesElement)) {
			return false;
		}

		IEclipsePreferences preferences = (IEclipsePreferences) ((EclipsePreferencesElement) element)
				.getNativeWidget();

		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList list = (CSSValueList) value;
			for (int i = 0; i < list.getLength(); i++) {
				overrideProperty(preferences, list.item(i));
			}
		} else {
			overrideProperty(preferences, value);
		}

		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	protected void overrideProperty(IEclipsePreferences preferences,
			CSSValue value) {
		Matcher matcher = PROPERTY_NAME_AND_VALUE_PATTERN.matcher(value
				.getCssText());
		if (matcher.find()) {
			overrideProperty(preferences, matcher.group(1).trim(), matcher
					.group(2).trim());
		}
	}

	protected void overrideProperty(IEclipsePreferences preferences,
			String name, String value) {
		if (preferences.get(name, null) == null || EclipsePreferencesHelper.isThemeChanged()) {
			preferences.put(name, value);
			EclipsePreferencesHelper.appendOverriddenPropertyName(preferences,
					name);
		}
	}
}
