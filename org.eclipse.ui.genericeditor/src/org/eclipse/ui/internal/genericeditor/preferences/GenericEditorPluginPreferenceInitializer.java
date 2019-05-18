/**
 *  Copyright (c) 2018 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v2.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v20.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - Bug 538111 - [generic editor] Extension point for ICharacterPairMatcher
 */
package org.eclipse.ui.internal.genericeditor.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;

/**
 * Preference initializer for Generic Editor plug-in.
 *
 * @since 1.2
 */
public class GenericEditorPluginPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = GenericEditorPreferenceConstants.getPreferenceStore();
		GenericEditorPreferenceConstants.initializeDefaultValues(store);
	}

	public static void setThemeBasedPreferences(IPreferenceStore store, boolean fireEvent) {
		ColorRegistry registry = null;
		if (PlatformUI.isWorkbenchRunning())
			registry = PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		setDefault(store, GenericEditorPreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR,
				findRGB(registry, IGenericEditorThemeConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(127, 0, 85)),
				fireEvent);

	}

	/**
	 * Sets the default value and fires a property change event if necessary.
	 *
	 * @param store     the preference store
	 * @param key       the preference key
	 * @param newValue  the new value
	 * @param fireEvent <code>false</code> if no event should be fired
	 * @since 1.2
	 */
	private static void setDefault(IPreferenceStore store, String key, RGB newValue, boolean fireEvent) {
		if (!fireEvent) {
			PreferenceConverter.setDefault(store, key, newValue);
			return;
		}

		RGB oldValue = null;
		if (store.isDefault(key))
			oldValue = PreferenceConverter.getDefaultColor(store, key);

		PreferenceConverter.setDefault(store, key, newValue);

		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}

	/**
	 * Returns the RGB for the given key in the given color registry.
	 *
	 * @param registry   the color registry
	 * @param key        the key for the constant in the registry
	 * @param defaultRGB the default RGB if no entry is found
	 * @return RGB the RGB
	 * @since 1.2
	 */
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry == null)
			return defaultRGB;

		RGB rgb = registry.getRGB(key);
		if (rgb != null)
			return rgb;

		return defaultRGB;
	}

}
