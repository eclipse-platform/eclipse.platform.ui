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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The central manager for Theme descriptors.
 *
 * @since 3.0
 */
public class ThemeRegistry implements IThemeRegistry {

    private List themes;
	private List colors;
	private List fonts;
	private List gradients;	
	private List categories;

	/**
	 * Create a new ThemeRegistry.
	 */
	public ThemeRegistry() {
		themes = new ArrayList();
		colors = new ArrayList();
		fonts = new ArrayList();
		gradients = new ArrayList();
		categories = new ArrayList();
	}

	/**
	 * Add a descriptor to the registry.
	 */
	void add(IThemeDescriptor desc) {
		themes.add(desc);
	}
	
	/**
	 * Add a descriptor to the registry.
	 */
	void add(ColorDefinition desc) {
		colors.add(desc);
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findColor(java.lang.String)
     */
    public ColorDefinition findColor(String id) {
        return (ColorDefinition) findDescriptor(getColors(), id);
    }	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#find(java.lang.String)
	 */
	public IThemeDescriptor findTheme(String id) {
	    return (IThemeDescriptor) findDescriptor(getThemes(), id);
	}

	/**
     * @param descriptors
     * @param id
     * @return
     */
    private IThemeElementDefinition findDescriptor(IThemeElementDefinition [] descriptors, String id) {
        int idx =
			Arrays.binarySearch(
			        descriptors,
				    id,
				    ID_COMPARATOR);
		if (idx < 0)
			return null;
		return descriptors[idx];
    }

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#getLookNFeels()
	 */
	public IThemeDescriptor [] getThemes() {
		int nSize = themes.size();
		IThemeDescriptor [] retArray = new IThemeDescriptor[nSize];
		themes.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeRegistry#getLookNFeels()
	 */
	public ColorDefinition [] getColors() {
		int nSize = colors.size();
		ColorDefinition [] retArray = new ColorDefinition[nSize];
		colors.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
	}

    /**
     * @param definition
     */
    public void add(GradientDefinition definition) {
        gradients.add(definition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getGradients()
     */
    public GradientDefinition[] getGradients() {
		int nSize = gradients.size();
		GradientDefinition [] retArray = new GradientDefinition[nSize];
		gradients.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
    }
    
    /**
     * @param definition
     */
    public void add(FontDefinition definition) {
        fonts.add(definition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getGradients()
     */
    public FontDefinition [] getFonts() {
		int nSize = fonts.size();
		FontDefinition [] retArray = new FontDefinition[nSize];
		fonts.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findFont(java.lang.String)
     */
    public FontDefinition findFont(String id) { 
        return (FontDefinition) findDescriptor(getFonts(), id);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#findGradient(java.lang.String)
     */
    public GradientDefinition findGradient(String id) {
        return (GradientDefinition) findDescriptor(getGradients(), id);
    }

    /**
     * @param definition
     */
    public void add(ThemeElementCategory definition) {
        categories.add(definition);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.IThemeRegistry#getCategories()
     */
    public ThemeElementCategory [] getCategories() {
		int nSize = categories.size();
		ThemeElementCategory [] retArray = new ThemeElementCategory[nSize];
		categories.toArray(retArray);
		Arrays.sort(retArray, ID_COMPARATOR);
		return retArray;
    }
}
