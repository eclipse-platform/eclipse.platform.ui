/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text;

import org.eclipse.swt.graphics.RGB;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.texteditor.ITextEditorThemeConstants;

import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import org.eclipse.ui.editors.text.TextEditorPreferenceConstants;


/**
 * Preference initializer for Editors UI plug-in.
 *
 * @since 3.1
 */
public class EditorsPluginPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 * @since 3.1
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store= EditorsPlugin.getDefault().getPreferenceStore();
		TextEditorPreferenceConstants.initializeDefaultValues(store);
		migrateOverviewRulerPreference(store);
	}

	public static void setThemeBasedPreferences(IPreferenceStore store, boolean fireEvent) {
		ColorRegistry registry= null;
		if (PlatformUI.isWorkbenchRunning())
			registry= PlatformUI.getWorkbench().getThemeManager().getCurrentTheme().getColorRegistry();

		setDefault(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR,
				findRGB(registry, ITextEditorThemeConstants.CURRENT_LINE_COLOR, new RGB(232, 242, 254)), fireEvent);

		setDefault(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR,
				findRGB(registry,ITextEditorThemeConstants.PRINT_MARGIN_COLOR, new RGB(176, 180 , 185)), fireEvent);

		setDefault(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR,
				findRGB(registry, ITextEditorThemeConstants.LINE_NUMBER_RULER_COLOR, new RGB(120, 120, 120)), fireEvent);

		setDefault(store,
				AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND,
				findRGB(registry, ITextEditorThemeConstants.PREFERENCE_COLOR_BACKGROUND, new RGB(255, 255, 255)), fireEvent);

		setDefault(store,
				AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND,
				findRGB(registry, ITextEditorThemeConstants.PREFERENCE_COLOR_FOREGROUND, new RGB(0,0,0)), fireEvent);

		setDefault(store,
				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINK_COLOR,
				findRGB(registry,ITextEditorThemeConstants.HYPERLINK_COLOR, new RGB(0, 0, 255)), fireEvent);

		setDefault(store,
				AbstractTextEditor.PREFERENCE_COLOR_FIND_SCOPE,
				findRGB(registry, ITextEditorThemeConstants.FIND_SCOPE_COLOR, new RGB(185, 176 , 180)), fireEvent);

	}

	/**
	 * Sets the default value and fires a property
	 * change event if necessary.
	 *
	 * @param store	the preference store
	 * @param key the preference key
	 * @param newValue the new value
	 * @param fireEvent <code>false</code> if no event should be fired
	 * @since 3.4
	 */
	private static void setDefault(IPreferenceStore store, String key, RGB newValue, boolean fireEvent) {
		if (!fireEvent) {
			PreferenceConverter.setDefault(store, key, newValue);
			return;
		}

		RGB oldValue= null;
		if (store.isDefault(key))
			oldValue= PreferenceConverter.getDefaultColor(store, key);

		PreferenceConverter.setDefault(store, key, newValue);

		if (oldValue != null && !oldValue.equals(newValue))
			store.firePropertyChangeEvent(key, oldValue, newValue);
	}

	/**
	 * Returns the RGB for the given key in the given color registry.
	 *
	 * @param registry the color registry
	 * @param key the key for the constant in the registry
	 * @param defaultRGB the default RGB if no entry is found
	 * @return RGB the RGB
	 * @since 3.4
	 */
	private static RGB findRGB(ColorRegistry registry, String key, RGB defaultRGB) {
		if (registry == null)
			return defaultRGB;

		RGB rgb= registry.getRGB(key);
		if (rgb != null)
			return rgb;

		return defaultRGB;
	}


	/**
	 * Migrates the overview ruler preference by re-enabling it.
	 *
	 * @param store the preference store to migrate
	 * @since 3.1
	 */
	private void migrateOverviewRulerPreference(IPreferenceStore store) {
		String preference= AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER;
		String postfix= "_migration"; //$NON-NLS-1$
		String MIGRATED= "migrated_3.1"; //$NON-NLS-1$
		String migrationKey= preference + postfix;

		String migrationValue= store.getString(migrationKey);
		if (!MIGRATED.equals(migrationValue)) {
			store.putValue(migrationKey, MIGRATED);
			if (!store.getBoolean(preference))
				store.putValue(preference, Boolean.TRUE.toString());
		}
	}
}
