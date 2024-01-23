/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.themes;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public final class ThemeElementHelper {
	public static void populateDefinition(org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme, ITheme theme,
			FontRegistry registry, FontDefinition definition, IPreferenceStore store) {
		String key = createPreferenceKey(cssTheme, theme, definition.getId());
		String value = store.getString(key);
		if (!IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			definition.appendState(ThemeElementDefinition.State.OVERRIDDEN);
			definition.appendState(ThemeElementDefinition.State.MODIFIED_BY_USER);
			registry.put(definition.getId(), PreferenceConverter.basicGetFontData(value));
		}
	}

	public static void populateRegistry(ITheme theme, FontDefinition[] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process
		// ancestors before children.
		FontDefinition[] copyOfDefinitions = null;

		// the colors to set a default value for, but not a registry value
		FontDefinition[] defaults = null;
		if (!theme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			definitions = addDefaulted(definitions);
			// compute the defaults only if we're setting preferences at this time
			if (store != null) {
				defaults = getDefaults(definitions);
			}
		}

		copyOfDefinitions = new FontDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(definitions));

		for (FontDefinition definition : copyOfDefinitions) {
			installFont(definition, theme, store, true);
		}

		if (defaults != null) {
			for (FontDefinition fontDef : defaults) {
				installFont(fontDef, theme, store, false);
			}
		}
	}

	private static FontDefinition[] addDefaulted(FontDefinition[] definitions) {
		IThemeRegistry registry = WorkbenchPlugin.getDefault().getThemeRegistry();
		FontDefinition[] allDefs = registry.getFonts();

		SortedSet set = addDefaulted(definitions, allDefs);
		return (FontDefinition[]) set.toArray(new FontDefinition[set.size()]);
	}

	/**
	 * Installs the given font in the preference store and optionally the font
	 * registry.
	 *
	 * @param definition    the font definition
	 * @param registry      the font registry
	 * @param store         the preference store from which to set and obtain font
	 *                      data
	 * @param setInRegistry whether the color should be put into the registry as
	 *                      well as having its default preference set
	 */
	private static void installFont(FontDefinition definition, ITheme theme, IPreferenceStore store,
			boolean setInRegistry) {
		FontRegistry registry = theme.getFontRegistry();
		Display display = PlatformUI.getWorkbench().getDisplay();

		String id = definition.getId();
		String key = createPreferenceKey(theme, id);
		FontData[] prefFont = store != null ? PreferenceConverter.getFontDataArray(store, key) : null;
		FontData[] defaultFont = null;
		if (definition.getValue() != null) {
			defaultFont = definition.getValue();
		} else if (definition.getDefaultsTo() != null) {
			String defaultsToKey = createPreferenceKey(theme, definition.getDefaultsTo());
			FontData[] defaultFontData = PreferenceConverter.getDefaultFontDataArray(store, defaultsToKey);
			defaultFont = registry.filterData(defaultFontData, display);
		} else {
			// values pushed in from jface property files. Very ugly.

			// If in high contrast, ignore the defaults in jface and use the default
			// (system) font.
			// This is a hack to address bug #205474. See bug #228207 for a future fix.
			FontData[] fontData = JFaceResources.getFontRegistry()
					.getFontData(display.getHighContrast() ? JFaceResources.DEFAULT_FONT : id);
			defaultFont = registry.bestDataArray(fontData, display);
		}

		if (setInRegistry) {
			if (prefFont == null || prefFont == PreferenceConverter.getFontDataArrayDefaultDefault()) {
				if (definition.getValue() != null) {
					prefFont = definition.getValue();
				} else if (definition.getDefaultsTo() != null) {
					FontData[] fontData = registry.getFontData(definition.getDefaultsTo());
					prefFont = registry.filterData(fontData, display);
				} else {
					prefFont = defaultFont;
				}
			}
			if (!definition.isEditable()) {
				prefFont = defaultFont;
			}

			if (prefFont != null) {
				registry.put(id, prefFont);
			}
		}

		if (defaultFont != null && store != null) {
			PreferenceConverter.setDefault(store, key, defaultFont);
		}
	}

	public static void populateDefinition(org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme, ITheme theme,
			ColorRegistry registry, ColorDefinition definition, IPreferenceStore store) {
		String key = createPreferenceKey(cssTheme, theme, definition.getId());
		String value = store.getString(key);
		if (!IPreferenceStore.STRING_DEFAULT_DEFAULT.equals(value)) {
			definition.appendState(ThemeElementDefinition.State.OVERRIDDEN);
			definition.appendState(ThemeElementDefinition.State.MODIFIED_BY_USER);
			registry.put(definition.getId(), StringConverter.asRGB(value));
		}
	}

	public static void populateRegistry(ITheme theme, ColorDefinition[] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process
		// ancestors before children.

		ColorDefinition[] copyOfDefinitions = null;

		// the colors to set a default value for, but not a registry value
		ColorDefinition[] defaults = null;
		if (!theme.getId().equals(IThemeManager.DEFAULT_THEME)) {
			definitions = addDefaulted(definitions);
			// compute defaults only if we're setting preferences
			if (store != null) {
				defaults = getDefaults(definitions);
			}
		}

		copyOfDefinitions = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(definitions));

		for (ColorDefinition definition : copyOfDefinitions) {
			installColor(definition, theme, store, true);
		}

		if (defaults != null) {
			for (ColorDefinition colorDef : defaults) {
				installColor(colorDef, theme, store, false);
			}
		}
	}

	/**
	 * Return the definitions that should have their default preference value set
	 * but nothing else.
	 *
	 * @param definitions the definitions that will be fully handled
	 * @return the remaining definitions that should be defaulted
	 */
	private static ColorDefinition[] getDefaults(ColorDefinition[] definitions) {
		IThemeRegistry registry = WorkbenchPlugin.getDefault().getThemeRegistry();
		ColorDefinition[] allDefs = registry.getColors();

		SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
		set.addAll(Arrays.asList(allDefs));
		set.removeAll(Arrays.asList(definitions));
		return (ColorDefinition[]) set.toArray(new ColorDefinition[set.size()]);
	}

	/**
	 * Return the definitions that should have their default preference value set
	 * but nothing else.
	 *
	 * @param definitions the definitions that will be fully handled
	 * @return the remaining definitions that should be defaulted
	 */
	private static FontDefinition[] getDefaults(FontDefinition[] definitions) {
		IThemeRegistry registry = WorkbenchPlugin.getDefault().getThemeRegistry();
		FontDefinition[] allDefs = registry.getFonts();

		SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
		set.addAll(Arrays.asList(allDefs));
		set.removeAll(Arrays.asList(definitions));
		return (FontDefinition[]) set.toArray(new FontDefinition[set.size()]);
	}

	private static ColorDefinition[] addDefaulted(ColorDefinition[] definitions) {
		IThemeRegistry registry = WorkbenchPlugin.getDefault().getThemeRegistry();
		ColorDefinition[] allDefs = registry.getColors();

		SortedSet set = addDefaulted(definitions, allDefs);
		return (ColorDefinition[]) set.toArray(new ColorDefinition[set.size()]);
	}

	private static SortedSet addDefaulted(IHierarchalThemeElementDefinition[] definitions,
			IHierarchalThemeElementDefinition[] allDefs) {
		SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
		set.addAll(Arrays.asList(definitions));

		IHierarchalThemeElementDefinition copy[] = new IHierarchalThemeElementDefinition[allDefs.length];
		System.arraycopy(allDefs, 0, copy, 0, allDefs.length);

		Arrays.sort(allDefs, new IThemeRegistry.HierarchyComparator(copy));
		for (IHierarchalThemeElementDefinition def : allDefs) {
			if (def.getDefaultsTo() != null) {
				if (set.contains(def.getDefaultsTo())) {
					set.add(def);
				}
			}
		}
		return set;
	}

	/**
	 * Installs the given color in the preference store and optionally the color
	 * registry.
	 *
	 * @param definition    the color definition
	 * @param theme         the theme defining the color
	 * @param store         the preference store from which to set and obtain color
	 *                      data
	 * @param setInRegistry whether the color should be put into the registry
	 */

	private static void installColor(ColorDefinition definition, ITheme theme, IPreferenceStore store,
			boolean setInRegistry) {
		Assert.isNotNull(store);

		ColorRegistry registry = theme.getColorRegistry();

		String id = definition.getId();
		String key = createPreferenceKey(theme, id);
		RGB defaultColor;
		if (definition.getValue() != null) {
			defaultColor = definition.getValue();
		} else if (definition.getDefaultsTo() != null) {
			String defaultsToKey = createPreferenceKey(theme, definition.getDefaultsTo());
			defaultColor = PreferenceConverter.getDefaultColor(store, defaultsToKey);
		} else {
			defaultColor = PreferenceConverter.COLOR_DEFAULT_DEFAULT;
		}

		RGB prefColor = PreferenceConverter.getColor(store, key);
		if (prefColor == PreferenceConverter.COLOR_DEFAULT_DEFAULT || store.isDefault(key)) {
			if (definition.getValue() != null) {
				prefColor = definition.getValue();
			} else if (definition.getDefaultsTo() != null) {
				prefColor = registry.getRGB(definition.getDefaultsTo());
			}
		}

		if (prefColor == null || !definition.isEditable()) {
			prefColor = defaultColor;
		}

		if (store != null) {
			PreferenceConverter.setDefault(store, key, defaultColor);
		}

		if (setInRegistry) {
			registry.put(id, prefColor);
		}
	}

	public static String createPreferenceKey(ITheme theme, String id) {
		String themeId = theme.getId();
		if (themeId.equals(IThemeManager.DEFAULT_THEME)) {
			return id;
		}

		return themeId + '.' + id;
	}

	public static String createPreferenceKey(org.eclipse.e4.ui.css.swt.theme.ITheme cssTheme, ITheme theme, String id) {
		String cssThemePrefix = cssTheme != null ? cssTheme.getId() + '.' : ""; //$NON-NLS-1$
		return cssThemePrefix + createPreferenceKey(theme, id);
	}

	/**
	 * Not intended to be instantiated.
	 */
	private ThemeElementHelper() {
		// no-op
	}
}
