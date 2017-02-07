/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@vogella.com> - Bug 440136
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.eclipse.ui.themes.IThemeManager;

/**
 * The central manager for Theme descriptors.
 *
 * @since 3.0
 */
public class ThemeRegistry implements IThemeRegistry {

    private List themes;

    private List colors;

    private List fonts;

    private List categories;

    private Map dataMap;

    /**
     * Map from String (categoryId) -> Set (presentationIds)
     */
    private Map categoryBindingMap;

    /**
     * Create a new ThemeRegistry.
     */
    public ThemeRegistry() {
        themes = new ArrayList();
        colors = new ArrayList();
        fonts = new ArrayList();
        categories = new ArrayList();
        dataMap = new HashMap();
        categoryBindingMap = new HashMap();
    }

    /**
     * Add a descriptor to the registry.
     */
    void add(IThemeDescriptor desc) {
        if (findTheme(desc.getId()) != null) {
			return;
		}
        themes.add(desc);
    }

    /**
     * Add a descriptor to the registry.
     */
    void add(ColorDefinition desc) {
    	addOrReplaceDescriptor(colors, desc);
    }

    @Override
	public ThemeElementCategory findCategory(String id) {
        return (ThemeElementCategory) findDescriptor(getCategories(), id);
    }

    @Override
	public ColorDefinition findColor(String id) {
        return (ColorDefinition) findDescriptor(getColors(), id);
    }

    @Override
	public IThemeDescriptor findTheme(String id) {
        return (IThemeDescriptor) findDescriptor(getThemes(), id);
    }

    /**
     * @param descriptors
     * @param id
     * @return
     */
    private IThemeElementDefinition findDescriptor(
            IThemeElementDefinition[] descriptors, String id) {
        int idx = Arrays.binarySearch(descriptors, id, ID_COMPARATOR);
        if (idx < 0) {
			return null;
		}
        return descriptors[idx];
    }

    /*
     * Add newElement to descriptors.
     * If one with the same id already exists, replace it.
     * Return the existing element in the case of replacing, null in the case of adding.
     */
    private IThemeElementDefinition addOrReplaceDescriptor(
            List descriptors, IThemeElementDefinition newElement) {
    	String id = newElement.getId();
    	for (int i = 0; i < descriptors.size(); i++) {
    		IThemeElementDefinition existingElement = (IThemeElementDefinition) descriptors.get(i);
    		if(existingElement.getId().equals(id)) {
    			descriptors.remove(i);
    			descriptors.add(newElement);
    			return existingElement;
    		}
		}
    	descriptors.add(newElement);
        return null;
    }

    @Override
	public IThemeDescriptor[] getThemes() {
        int nSize = themes.size();
        IThemeDescriptor[] retArray = new IThemeDescriptor[nSize];
        themes.toArray(retArray);
        Arrays.sort(retArray, ID_COMPARATOR);
        return retArray;
    }

    @Override
	public ColorDefinition[] getColors() {
        int nSize = colors.size();
        ColorDefinition[] retArray = new ColorDefinition[nSize];
        colors.toArray(retArray);
        Arrays.sort(retArray, ID_COMPARATOR);
        return retArray;
    }

    @Override
	public ColorDefinition[] getColorsFor(String themeId) {
        ColorDefinition[] defs = getColors();
        if (themeId.equals(IThemeManager.DEFAULT_THEME)) {
			return defs;
		}

        IThemeDescriptor desc = findTheme(themeId);
        ColorDefinition[] overrides = desc.getColors();
        return (ColorDefinition[]) overlay(defs, overrides);
    }

    @Override
	public FontDefinition[] getFontsFor(String themeId) {
        FontDefinition[] defs = getFonts();
        if (themeId.equals(IThemeManager.DEFAULT_THEME)) {
			return defs;
		}

        IThemeDescriptor desc = findTheme(themeId);
        FontDefinition[] overrides = desc.getFonts();
        return (FontDefinition[]) overlay(defs, overrides);
    }

    /**
     * Overlay the overrides onto the base definitions.
     *
     * @param defs the base definitions
     * @param overrides the overrides
     * @return the overlayed elements
     */
    private IThemeElementDefinition[] overlay(IThemeElementDefinition[] defs,
            IThemeElementDefinition[] overrides) {
        for (IThemeElementDefinition override : overrides) {
			int idx = Arrays.binarySearch(defs, override, IThemeRegistry.ID_COMPARATOR);
            if (idx >= 0) {
                defs[idx] = overlay(defs[idx], override);
            }
        }
        return defs;
    }

    /**
     * Overlay the override onto the base definition.
     *
     * @param defs the base definition
     * @param overrides the override
     * @return the overlayed element
     */
    private IThemeElementDefinition overlay(IThemeElementDefinition original,
            IThemeElementDefinition overlay) {
        if (original instanceof ColorDefinition) {
            ColorDefinition originalColor = (ColorDefinition) original;
            ColorDefinition overlayColor = (ColorDefinition) overlay;
            return new ColorDefinition(originalColor, overlayColor.getValue());
        } else if (original instanceof FontDefinition) {
            FontDefinition originalFont = (FontDefinition) original;
            FontDefinition overlayFont = (FontDefinition) overlay;
            return new FontDefinition(originalFont, overlayFont.getValue());
        }
        return null;
    }

    /**
     * @param definition
     */
    void add(FontDefinition definition) {
        if (findFont(definition.getId()) != null) {
			return;
		}
        fonts.add(definition);
    }

    @Override
	public FontDefinition[] getFonts() {
        int nSize = fonts.size();
        FontDefinition[] retArray = new FontDefinition[nSize];
        fonts.toArray(retArray);
        Arrays.sort(retArray, ID_COMPARATOR);
        return retArray;
    }

    @Override
	public FontDefinition findFont(String id) {
        return (FontDefinition) findDescriptor(getFonts(), id);
    }

    /**
     * @param definition
     */
    void add(ThemeElementCategory definition) {
        if (findCategory(definition.getId()) != null) {
			return;
		}
        categories.add(definition);
    }

    @Override
	public ThemeElementCategory[] getCategories() {
        int nSize = categories.size();
        ThemeElementCategory[] retArray = new ThemeElementCategory[nSize];
        categories.toArray(retArray);
        Arrays.sort(retArray, ID_COMPARATOR);
        return retArray;
    }

    /**
     * @param name
     * @param value
     */
    void setData(String name, String value) {
        if (dataMap.containsKey(name)) {
			return;
		}

        dataMap.put(name, value);
    }

    @Override
	public Map getData() {
        return Collections.unmodifiableMap(dataMap);
    }

    /**
     * Add the data from another map to this data
     *
     * @param otherData the other data to add
     */
    public void addData(Map<?, ?> otherData) {
		for (Entry<?, ?> entry : otherData.entrySet()) {
			Object key = entry.getKey();
            if (dataMap.containsKey(key)) {
				continue;
			}
			dataMap.put(key, entry.getValue());
        }
    }

    @Override
	public Set getPresentationsBindingsFor(ThemeElementCategory category) {
        return (Set) categoryBindingMap.get(category.getId());
    }
}
