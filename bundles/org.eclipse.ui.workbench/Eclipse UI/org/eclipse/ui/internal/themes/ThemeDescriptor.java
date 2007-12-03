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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Concrete implementation of a theme descriptor.
 *
 * @since 3.0
 */
public class ThemeDescriptor implements IThemeDescriptor {

    /* Theme */
    public static final String ATT_ID = "id";//$NON-NLS-1$

    private static final String ATT_NAME = "name";//$NON-NLS-1$	

    private Collection colors = new HashSet();

    private String description;

    private Collection fonts = new HashSet();

    private String id;

    private String name;

    private Map dataMap = new HashMap();

    /**
     * Create a new ThemeDescriptor
     * @param id
     */
    public ThemeDescriptor(String id) {
        this.id = id;
    }

    /**
     * Add a color override to this descriptor.
     * 
     * @param definition the definition to add
     */
    void add(ColorDefinition definition) {
    	if (colors.contains(definition)) {
    		colors.remove(definition);
		}
        colors.add(definition);
    }

    /**
     * Add a font override to this descriptor.
     * 
     * @param definition the definition to add
     */
    void add(FontDefinition definition) {
        if (fonts.contains(definition)) {
			return;
		}
        fonts.add(definition);
    }

    /**
     * Add a data object to this descriptor.
     * 
     * @param key the key
     * @param data the data
     */
    void setData(String key, Object data) {
        if (dataMap.containsKey(key)) {
			return;
		}
            
        dataMap.put(key, data);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeDescriptor#getColorOverrides()
     */
    public ColorDefinition[] getColors() {
        ColorDefinition[] defs = (ColorDefinition[]) colors
                .toArray(new ColorDefinition[colors.size()]);
        Arrays.sort(defs, IThemeRegistry.ID_COMPARATOR);
        return defs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeElementDefinition#getDescription()
     */
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeDescriptor#getFontOverrides()
     */
    public FontDefinition[] getFonts() {
        FontDefinition[] defs = (FontDefinition[]) fonts
                .toArray(new FontDefinition[fonts.size()]);
        Arrays.sort(defs, IThemeRegistry.ID_COMPARATOR);
        return defs;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getID()
     */
    public String getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getName()
     */
    public String getName() {
    	if (name == null)
    		return getId();
        return name;
    }

    /*
     * load the name if it is not already set.
     */
    void extractName(IConfigurationElement configElement) {
        if (name == null) {
			name = configElement.getAttribute(ATT_NAME);
		}
    }

    /**
     * Set the description.
     * 
     * @param description the description
     */
    void setDescription(String description) {
        if (this.description == null) {
			this.description = description;
		}
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeDescriptor#getData()
     */
    public Map getData() {
        return Collections.unmodifiableMap(dataMap);
    }
}
