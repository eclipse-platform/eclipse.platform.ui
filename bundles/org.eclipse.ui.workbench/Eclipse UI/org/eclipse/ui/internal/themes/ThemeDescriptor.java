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
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;

/**
 * Concrete implementation of a theme descriptor.
 *
 * @since 3.0
 */
public class ThemeDescriptor implements IThemeDescriptor {
	
	/* Theme */
	private static final String ATT_ID="id";//$NON-NLS-1$
	private static final String ATT_NAME="name";//$NON-NLS-1$	
	/* TabTheme*/
	public static final String TAG_TABTHEME="tabTheme";//$NON-NLS-1$	
	/* ViewTheme */
	public static final String TAG_VIEWTHEME="viewTheme";//$NON-NLS-1$
	
	private Collection colors = new ArrayList();
	private IConfigurationElement configElement;
    private String description;
	private Collection fonts = new ArrayList();
	private Collection gradients = new ArrayList();
	
	private String id;
	private String name;
	private ITabThemeDescriptor tabThemeDescriptor;
	
	private IViewThemeDescriptor viewThemeDescriptor;

	/**
	 * Create a new ThemeDescriptor for an extension.
	 */
	public ThemeDescriptor(IConfigurationElement e) throws CoreException {
		configElement = e;
		processExtension();
	}

    /**
     * Add a color override to this descriptor.
     * 
     * @param definition the definition to add
     */
    public void add(ColorDefinition definition) {
        colors.add(definition);
    }

    /**
     * Add a font override to this descriptor.
     * 
     * @param definition the definition to add
     */
    public void add(FontDefinition definition) {
        fonts.add(definition);        
    }    
    
    /**
     * Add a gradient override to this descriptor.
     * 
     * @param definition the definition to add
     */
    public void add(GradientDefinition definition) {
        gradients.add(definition);
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.themes.IThemeDescriptor#getColorOverrides()
	 */
	public ColorDefinition [] getColors() {	    
	    ColorDefinition [] defs = (ColorDefinition []) colors.toArray(new ColorDefinition [colors.size()]);
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
	    FontDefinition [] defs = (FontDefinition []) fonts.toArray(new FontDefinition [fonts.size()]);
	    Arrays.sort(defs, IThemeRegistry.ID_COMPARATOR);
	    return defs;
    }	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.themes.IThemeDescriptor#getColorOverrides()
	 */
	public GradientDefinition[] getGradients() {
	    GradientDefinition [] defs = (GradientDefinition []) gradients.toArray(new GradientDefinition [gradients.size()]);
	    Arrays.sort(defs, IThemeRegistry.ID_COMPARATOR);
	    return defs;
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getID()
	 */
	public String getId()  {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getName()
	 */
	public String getLabel() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.IThemeDescriptor#getTabLookNFeelDesc()
	 */
	public ITabThemeDescriptor getTabThemeDescriptor () {
		return tabThemeDescriptor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.IThemeDescriptor#getViewThemeDescriptor()
	 */
	public IViewThemeDescriptor getViewThemeDescriptor () {
		return viewThemeDescriptor;
	}
	
	/*
	 * load a theme descriptor from the registry.
	 */
	private void processExtension() throws CoreException {
		id = configElement.getAttribute(ATT_ID);
		name = configElement.getAttribute(ATT_NAME);
		/* process children */
		IConfigurationElement [] children = configElement.getChildren();
		for (int nX = 0; nX < children.length; nX ++) {
			IConfigurationElement child = children[nX];
			String type = child.getName();
			/* TabTheme */
			if (type.equals(TAG_TABTHEME)) {
				tabThemeDescriptor = new TabThemeDescriptor(child);
			}
			/* ViewTheme */
			else if (type.equals(TAG_VIEWTHEME)) {
				viewThemeDescriptor = new ViewThemeDescriptor(child);
			}									
		}
	}
    
    /**
     * Set the description.
     * 
     * @param description the description
     */
    void setDescription(String description) {
        this.description = description;
    }
}
