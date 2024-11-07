/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.ui.internal.preferences;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.themes.ITheme;

/**
 * @since 3.1
 */
public class ThemeAdapter extends PropertyMapAdapter {

	private ITheme targetTheme;

	private IPropertyChangeListener listener = event -> firePropertyChange(event.getProperty());

	public ThemeAdapter(ITheme targetTheme) {
		this.targetTheme = targetTheme;
	}

	@Override
	protected void attachListener() {
		targetTheme.addPropertyChangeListener(listener);
	}

	@Override
	protected void detachListener() {
		targetTheme.removePropertyChangeListener(listener);
	}

	@Override
	public Set<String> keySet() {
		return getKeySet(targetTheme);
	}

	@Override
	public Object getValue(String propertyId, Class propertyType) {
		return getValue(targetTheme, propertyId, propertyType);
	}

	public static Set<String> getKeySet(ITheme targetTheme) {
		Set<String> result = new HashSet<>();

		result.addAll(targetTheme.keySet());
		result.addAll(targetTheme.getColorRegistry().getKeySet());
		result.addAll(targetTheme.getFontRegistry().getKeySet());

		return result;
	}

	public static Object getValue(ITheme targetTheme, String propertyId, Class<?> propertyType) {

		if (propertyType.isAssignableFrom(String.class)) {
			return targetTheme.getString(propertyId);
		}

		if (propertyType.isAssignableFrom(Color.class)) {
			Color result = targetTheme.getColorRegistry().get(propertyId);
			if (result != null) {
				return result;
			}
		}

		if (propertyType.isAssignableFrom(Font.class)) {
			FontRegistry fonts = targetTheme.getFontRegistry();

			if (fonts.hasValueFor(propertyId)) {
				return fonts.get(propertyId);
			}
		}

		if (propertyType == Integer.class) {
			return Integer.valueOf(targetTheme.getInt(propertyId));
		}

		if (propertyType == Boolean.class) {
			return targetTheme.getBoolean(propertyId) ? Boolean.TRUE : Boolean.FALSE;
		}

		return null;
	}

	@Override
	public boolean propertyExists(String propertyId) {
		return keySet().contains(propertyId);
	}

	@Override
	public void setValue(String propertyId, Object newValue) {
		throw new UnsupportedOperationException();
	}

}
