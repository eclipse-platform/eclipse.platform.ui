/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.themes.ITheme;


/**
 * @since 3.0
 */
public final class ThemeElementHelper {

    public static void populateRegistry(ITheme theme, FontDefinition [] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process 
		// ancestors before children.		
		FontDefinition [] copyOfDefinitions = new FontDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(definitions));

		for (int i = 0; i < copyOfDefinitions.length; i++) {
			FontDefinition definition = copyOfDefinitions[i];
			installFont(definition, theme, store);
		}
    }
    
    
    /**
     * @param definition
     * @param registry
     * @param store
     */
    private static void installFont(FontDefinition definition, ITheme theme, IPreferenceStore store) {
        FontRegistry registry = theme.getFontRegistry();
		
        String id = definition.getId();
        String key = createPreferenceKey(theme, id);
		FontData [] prefFont = store != null ? PreferenceConverter.getFontDataArray(store, key) : null;
		FontData [] defaultFont = null;
		if (definition.getValue() != null)
		    defaultFont = definition.getValue();
		else if (definition.getDefaultsTo() != null)
		    defaultFont = registry.bestDataArray(registry.getFontData(definition.getDefaultsTo()), Workbench.getInstance().getDisplay());
		else {
		    // values pushed in from jface property files.  Very ugly.
		    defaultFont = registry.bestDataArray(registry.getFontData(key), Workbench.getInstance().getDisplay());
		}		    
		
		if (prefFont == null || prefFont == PreferenceConverter.FONTDATA_ARRAY_DEFAULT_DEFAULT) {
		    prefFont = defaultFont;
		}
		
		if (defaultFont != null && store != null) {
			PreferenceConverter.setDefault(
					store, 
					key, 
					defaultFont);
		}

		
		if (prefFont != null) {		    
			registry.put(id, prefFont);
		}
    }

    public static void populateRegistry(ITheme theme, ColorDefinition [] definitions, IPreferenceStore store) {
		// sort the definitions by dependant ordering so that we process 
		// ancestors before children.		
        
		ColorDefinition [] copyOfDefinitions = new ColorDefinition[definitions.length];
		System.arraycopy(definitions, 0, copyOfDefinitions, 0, definitions.length);
		Arrays.sort(copyOfDefinitions, new IThemeRegistry.HierarchyComparator(definitions));

		for (int i = 0; i < copyOfDefinitions.length; i++) {
			ColorDefinition definition = copyOfDefinitions[i];
			installColor(definition, theme, store);
		}        
    }
    
	/**
	 * Installs the given color in the color registry.
	 * 
	 * @param definition
	 *            the color definition
	 * @param registry
	 *            the color registry
	 * @param store
	 *            the preference store from which to set and obtain color data
	 */
	private static void installColor(
		ColorDefinition definition,
		ITheme theme,
		IPreferenceStore store) {

	    ColorRegistry registry = theme.getColorRegistry();
	    		
	    String id = definition.getId();
        String key = createPreferenceKey(theme, id);
		RGB prefColor = store != null ? PreferenceConverter.getColor(store, key) : null;
		RGB defaultColor = null;
		if (definition.getValue() != null)
		    defaultColor = definition.getValue();
		else 
		    defaultColor = registry.getRGB(definition.getDefaultsTo());
		
		if (prefColor == null || prefColor == PreferenceConverter.COLOR_DEFAULT_DEFAULT) {
		    prefColor = defaultColor;
		}
		
		if (defaultColor != null && store != null) {
			PreferenceConverter.setDefault(
					store, 
					key, 
					defaultColor);
		}

		
		if (prefColor != null) {		    
			registry.put(id, prefColor);
		}
	}

    
    /**
     * @param theme
     * @param id
     * @return
     */
    public static String createPreferenceKey(ITheme theme, String id) {        
        String themeId = theme.getId();
        if (themeId == null)
            return id;
        
        return themeId + '.' + id;
    }

    /**
     * @param theme
     * @param property
     * @return
     */
    public static String splitPreferenceKey(Theme theme, String property) {
        String themeId = theme.getId();
        if (themeId == null)
            return property;
        
        if (property.startsWith(themeId + '.')) {
            return property.substring(themeId.length() + 1);            
        }
        return property;
    }
    
    /**
     * Not intended to be instantiated.
     */
    private ThemeElementHelper() {
        // no-op
    }    
}
