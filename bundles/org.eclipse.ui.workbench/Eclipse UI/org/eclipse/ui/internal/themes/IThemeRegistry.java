/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
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
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Registry of color, font, gradient, category and theme descriptors.
 *
 * @since 3.0
 */
public interface IThemeRegistry {

    /**
     * A comparator that will sort IHierarchalThemeElementDefinition elements
     * by defaultsTo depth.
     * 
     * @since 3.0
     */
    public static class HierarchyComparator implements Comparator {

        private IHierarchalThemeElementDefinition[] definitions;

        /**
         * Create a new comparator.
         * 
         * @param definitions the elements to be sorted by depth, in ID order.
         */
        public HierarchyComparator(
                IHierarchalThemeElementDefinition[] definitions) {
            this.definitions = definitions;
        }

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {
            String def0 = arg0 == null ? null
                    : ((IHierarchalThemeElementDefinition) arg0)
                            .getDefaultsTo();
            String def1 = arg1 == null ? null
                    : ((IHierarchalThemeElementDefinition) arg1)
                            .getDefaultsTo();

            if (def0 == null && def1 == null) {
				return 0;
			}

            if (def0 == null) {
				return -1;
			}

            if (def1 == null) {
				return 1;
			}

            return compare(getDefaultsTo(def0), getDefaultsTo(def1));
        }

        /** 
         * @param id the identifier to search for.
         * @return the <code>IHierarchalThemeElementDefinition</code> that 
         * matches the id.
         */
        private IHierarchalThemeElementDefinition getDefaultsTo(String id) {
            int idx = Arrays.binarySearch(definitions, id, ID_COMPARATOR);
            if (idx >= 0) {
				return definitions[idx];
			}
            return null;
        }
    }

    /**
     * A comparator that will sort <code>IThemeElementDefinition</code> elements
     * by id depth.  You may use this on both <code>String</code> and 
     * <code>IThemeElementDefinition</code> objects in order to perform 
     * searching.
     * 
     * @since 3.0
     */
    public static final Comparator ID_COMPARATOR = new Comparator() {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object arg0, Object arg1) {
            String str0 = getCompareString(arg0);
            String str1 = getCompareString(arg1);
            return str0.compareTo(str1);
        }

        /**
         * @param object
         * @return <code>String</code> representation of the object.
         */
        private String getCompareString(Object object) {
            if (object instanceof String) {
				return (String) object;
			} else if (object instanceof IThemeElementDefinition) {
				return ((IThemeElementDefinition) object).getId();
			}
            return ""; //$NON-NLS-1$
        }
    };

    /**
     * Returns the category matching the provided id.
     * 
     * @param id the id to search for
     * @return the element matching the provided id, or <code>null</code> if 
     * not found
     */
    public ThemeElementCategory findCategory(String id);

    /**
     * Returns the color matching the provided id.
     * 
     * @param id the id to search for
     * @return the element matching the provided id, or <code>null</code> if 
     * not found
     */
    public ColorDefinition findColor(String id);

    /**
     * Returns the font matching the provided id.
     * 
     * @param id the id to search for
     * @return the element matching the provided id, or <code>null</code> if 
     * not found
     */
    public FontDefinition findFont(String id);

    /**
     *  Returns the theme matching the provided id.
     * 
     * @param id the id to search for
     * @return the element matching the provided id, or <code>null</code> if 
     * not found
     */
    public IThemeDescriptor findTheme(String id);

    /**
     * Returns a list of categories defined in the registry.
     * 
     * @return the categories in this registry
     */
    public ThemeElementCategory[] getCategories();

    /**
     * Returns a list of colors defined in the registry.
     * 
     * @return the colors in this registry
     */
    public ColorDefinition[] getColors();

    /**
     * Returns a list of colors defined for the given theme.  This is the
     * set of base colours overlayed with any theme specific overrides.
     * 
     * @param themeId the theme id
     * @return the colors in this theme
     */
    public ColorDefinition[] getColorsFor(String themeId);

    /**
     * Returns a list of fonts defined for the given theme.  This is the
     * set of base fonts overlayed with any theme specific overrides.
     * 
     * @param themeId the theme id
     * @return the fonts in this theme
     */
    public FontDefinition[] getFontsFor(String themeId);

    /**
     * Returns a list of fonts defined in the registry.
     * 
     * @return the fonts in this registry
     */
    public FontDefinition[] getFonts();

    /**
     * Returns a list of themes defined in the registry.
     * 
     * @return the themes in this registry
     */
    public IThemeDescriptor[] getThemes();

    /**
     * Return the data map.
     * 
     * @return the data map
     */
    public Map getData();

    /**
     * Return the set of category presentation bindings.
     * 
     * @param category the category to test
     * @return the set of bindings or <code>null</code> if this category has no bindings
     */
    public Set getPresentationsBindingsFor(ThemeElementCategory category);
}
