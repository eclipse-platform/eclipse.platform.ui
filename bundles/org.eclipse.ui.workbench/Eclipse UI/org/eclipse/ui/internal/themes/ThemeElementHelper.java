/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public final class ThemeElementHelper {

    public static void populateRegistry(ITheme theme,
            FontDefinition[] definitions, IPreferenceStore store) {
        // sort the definitions by dependant ordering so that we process 
        // ancestors before children.		
        FontDefinition[] copyOfDefinitions = null;

        // the colors to set a default value for, but not a registry value
        FontDefinition[] defaults = null;
        if (!theme.getId().equals(IThemeManager.DEFAULT_THEME)) {
            definitions = addDefaulted(definitions);
            //compute the defaults only if we're setting preferences at this time
            if (store != null) {
				defaults = getDefaults(definitions);
			}
        }

        copyOfDefinitions = new FontDefinition[definitions.length];
        System.arraycopy(definitions, 0, copyOfDefinitions, 0,
                definitions.length);
        Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(
                definitions));

        for (int i = 0; i < copyOfDefinitions.length; i++) {
            FontDefinition definition = copyOfDefinitions[i];
            installFont(definition, theme, store, true);
        }

        if (defaults != null) {
            for (int i = 0; i < defaults.length; i++) {
                installFont(defaults[i], theme, store, false);
            }
        }
    }

    /**
     * @param definitions
     * @return
     */
    private static FontDefinition[] addDefaulted(FontDefinition[] definitions) {
        IThemeRegistry registry = WorkbenchPlugin.getDefault()
                .getThemeRegistry();
        FontDefinition[] allDefs = registry.getFonts();

        SortedSet set = addDefaulted(definitions, allDefs);
        return (FontDefinition[]) set.toArray(new FontDefinition[set.size()]);
    }

    /**
     * Installs the given font in the preference store and optionally the font 
     * registry.
     * 
     * @param definition
     *            the font definition
     * @param registry
     *            the font registry
     * @param store
     *            the preference store from which to set and obtain font data
     * @param setInRegistry
     * 			  whether the color should be put into the registry as well as
     *            having its default preference set
     */
    private static void installFont(FontDefinition definition, ITheme theme,
            IPreferenceStore store, boolean setInRegistry) {
        FontRegistry registry = theme.getFontRegistry();

        String id = definition.getId();
        String key = createPreferenceKey(theme, id);
        FontData[] prefFont = store != null ? PreferenceConverter
                .getFontDataArray(store, key) : null;
        FontData[] defaultFont = null;
        if (definition.getValue() != null) {
			defaultFont = definition.getValue();
		} else if (definition.getDefaultsTo() != null) {
			defaultFont = registry.filterData(registry
                    .getFontData(definition.getDefaultsTo()), Workbench
                    .getInstance().getDisplay());
		} else {
            // values pushed in from jface property files.  Very ugly.
			defaultFont = registry.bestDataArray(JFaceResources.getFontRegistry().getFontData(id),
                    Workbench.getInstance().getDisplay());
        }

        if (setInRegistry) {
            if (prefFont == null
                    || prefFont == PreferenceConverter.FONTDATA_ARRAY_DEFAULT_DEFAULT) {
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

    public static void populateRegistry(ITheme theme,
            ColorDefinition[] definitions, IPreferenceStore store) {
        // sort the definitions by dependant ordering so that we process 
        // ancestors before children.		

        ColorDefinition[] copyOfDefinitions = null;

        // the colors to set a default value for, but not a registry value
        ColorDefinition[] defaults = null;
        if (!theme.getId().equals(IThemeManager.DEFAULT_THEME)) {
            definitions = addDefaulted(definitions);
            //compute defaults only if we're setting preferences
            if (store != null) {
				defaults = getDefaults(definitions);
			}
        }

        copyOfDefinitions = new ColorDefinition[definitions.length];
        System.arraycopy(definitions, 0, copyOfDefinitions, 0,
                definitions.length);
        Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(
                definitions));

        for (int i = 0; i < copyOfDefinitions.length; i++) {
            ColorDefinition definition = copyOfDefinitions[i];
            installColor(definition, theme, store, true);
        }

        if (defaults != null) {
            for (int i = 0; i < defaults.length; i++) {
                installColor(defaults[i], theme, store, false);
            }
        }
    }

    /**
     * Return the definitions that should have their default preference value
     * set but nothing else.
     * 
     * @param definitions the definitions that will be fully handled
     * @return the remaining definitions that should be defaulted
     */
    private static ColorDefinition[] getDefaults(ColorDefinition[] definitions) {
        IThemeRegistry registry = WorkbenchPlugin.getDefault()
                .getThemeRegistry();
        ColorDefinition[] allDefs = registry.getColors();

        SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
        set.addAll(Arrays.asList(allDefs));
        set.removeAll(Arrays.asList(definitions));
        return (ColorDefinition[]) set.toArray(new ColorDefinition[set.size()]);
    }

    /**
     * Return the definitions that should have their default preference value
     * set but nothing else.
     * 
     * @param definitions the definitions that will be fully handled
     * @return the remaining definitions that should be defaulted
     */
    private static FontDefinition[] getDefaults(FontDefinition[] definitions) {
        IThemeRegistry registry = WorkbenchPlugin.getDefault()
                .getThemeRegistry();
        FontDefinition[] allDefs = registry.getFonts();

        SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
        set.addAll(Arrays.asList(allDefs));
        set.removeAll(Arrays.asList(definitions));
        return (FontDefinition[]) set.toArray(new FontDefinition[set.size()]);
    }

    /**
     * @param definitions
     * @return
     */
    private static ColorDefinition[] addDefaulted(ColorDefinition[] definitions) {
        IThemeRegistry registry = WorkbenchPlugin.getDefault()
                .getThemeRegistry();
        ColorDefinition[] allDefs = registry.getColors();

        SortedSet set = addDefaulted(definitions, allDefs);
        return (ColorDefinition[]) set.toArray(new ColorDefinition[set.size()]);
    }

    /**
     * @param definitions
     * @param allDefs
     * @return
     */
    private static SortedSet addDefaulted(
            IHierarchalThemeElementDefinition[] definitions,
            IHierarchalThemeElementDefinition[] allDefs) {
        SortedSet set = new TreeSet(IThemeRegistry.ID_COMPARATOR);
        set.addAll(Arrays.asList(definitions));
        
        IHierarchalThemeElementDefinition copy [] = new IHierarchalThemeElementDefinition[allDefs.length];
		System.arraycopy(allDefs, 0, copy, 0, allDefs.length);
		
        Arrays.sort(allDefs, new IThemeRegistry.HierarchyComparator(copy));
        for (int i = 0; i < allDefs.length; i++) {
            IHierarchalThemeElementDefinition def = allDefs[i];
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
     * @param definition
     *            the color definition
     * @param theme
     *            the theme defining the color
     * @param store
     *            the preference store from which to set and obtain color data
     * @param setInRegistry
     * 			  whether the color should be put into the registry
     */
    
    private static void installColor(ColorDefinition definition, ITheme theme,
            IPreferenceStore store, boolean setInRegistry) {

        //TODO: store shouldn't be null, should assert instead of checking null all over
    	
    	ColorRegistry registry = theme.getColorRegistry();

        String id = definition.getId();
        String key = createPreferenceKey(theme, id);
        RGB prefColor = store != null 
        	? PreferenceConverter.getColor(store, key) 
        	: null;
        RGB defaultColor = (definition.getValue() != null)
        	? definition.getValue()
            : registry.getRGB(definition.getDefaultsTo());
     
        if (prefColor == null
                || prefColor == PreferenceConverter.COLOR_DEFAULT_DEFAULT) {
            prefColor = defaultColor;
        }

        //if the preference value isn't the default then retain that pref value
        RGB colorToUse = ! store.isDefault(key)
        	 ? prefColor
             : defaultColor;

        if (setInRegistry) {
        	registry.put(id, colorToUse);
        }

        if (store != null) {
            PreferenceConverter.setDefault(store, key, defaultColor);
        }
    }

    /**
     * @param theme
     * @param id
     * @return
     */
    public static String createPreferenceKey(ITheme theme, String id) {
        String themeId = theme.getId();
        if (themeId.equals(IThemeManager.DEFAULT_THEME)) {
			return id;
		}

        return themeId + '.' + id;
    }

    /**
     * @param theme
     * @param property
     * @return
     */
    public static String[] splitPropertyName(Theme theme, String property) {
    	IThemeDescriptor[] descriptors = WorkbenchPlugin.getDefault()
				.getThemeRegistry().getThemes();
		for (int i = 0; i < descriptors.length; i++) {
			IThemeDescriptor themeDescriptor = descriptors[i];
			String id = themeDescriptor.getId();
			if (property.startsWith(id + '.')) { // the property starts with
													// a known theme ID -
													// extract and return it and
													// the remaining property
				return new String[] { property.substring(0, id.length()),
						property.substring(id.length() + 1) };
			}
		}

		// default is simply return the default theme ID and the raw property
		return new String[] { IThemeManager.DEFAULT_THEME, property };
    }

    /**
	 * Not intended to be instantiated.
	 */
    private ThemeElementHelper() {
        // no-op
    }
}
