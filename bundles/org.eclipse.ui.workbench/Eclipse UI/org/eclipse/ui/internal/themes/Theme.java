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

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.GradientRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.internal.presentation.CascadingColorRegistry;
import org.eclipse.ui.internal.presentation.CascadingFontRegistry;
import org.eclipse.ui.internal.presentation.CascadingGradientRegistry;
import org.eclipse.ui.internal.presentation.ColorDefinition;
import org.eclipse.ui.internal.presentation.FontDefinition;
import org.eclipse.ui.internal.presentation.GradientDefinition;
import org.eclipse.ui.internal.presentation.PresentationRegistryPopulator;


/**
 * @since 3.0
 */
public class Theme implements ITheme {

    private CascadingColorRegistry themeColorRegistry;
    private CascadingGradientRegistry themeGradientRegistry;
    private CascadingFontRegistry themeFontRegistry;
    private IThemeDescriptor descriptor;
    
    private IPropertyChangeListener themeListener;
    
    /**
     * @param descriptor
     */
    public Theme(IThemeDescriptor descriptor) {
        this.descriptor = descriptor;
        if (descriptor != null) {
	        ColorDefinition [] definitions = this.descriptor.getColorOverrides();
	        if (definitions.length > 0) {
	            themeColorRegistry = new CascadingColorRegistry(JFaceResources.getColorRegistry());
	            themeColorRegistry.addListener(getListener());
	            PresentationRegistryPopulator.populateRegistry(themeColorRegistry, definitions, null);
	        }
	        
	        GradientDefinition [] gradientDefinitions = this.descriptor.getGradientOverrides();
	        if (gradientDefinitions.length > 0) {
	            themeGradientRegistry = new CascadingGradientRegistry(JFaceResources.getGradientRegistry());
	            themeGradientRegistry.addListener(getListener());
	            PresentationRegistryPopulator.populateRegistry(themeGradientRegistry, gradientDefinitions, null);
	        }
	        FontDefinition [] fontDefinitions = this.descriptor.getFontOverrides();
	        if (fontDefinitions.length > 0) {
	            themeFontRegistry = new CascadingFontRegistry(JFaceResources.getFontRegistry());
	            themeFontRegistry.addListener(getListener());
	            PresentationRegistryPopulator.populateRegistry(themeFontRegistry, fontDefinitions, null);
	        }	        	        
        }
    }
    
    /**
     * @return
     */
    private IPropertyChangeListener getListener() {
        if (themeListener == null) {
            themeListener = new IPropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    firePropertyChange(CHANGE_THEME);
                }                
            };
        }
        return themeListener;
    }

    public ColorRegistry getColorRegistry() {
        if (themeColorRegistry != null) 
            return themeColorRegistry;
        else 
            return JFaceResources.getColorRegistry();
    }
    
    public FontRegistry getFontRegistry() {
        if (themeFontRegistry != null) 
            return themeFontRegistry;
        else 
            return JFaceResources.getFontRegistry();
    }
    
    public void dispose() {
        if (themeColorRegistry != null) {
            themeColorRegistry.removeListener(themeListener);
            themeColorRegistry.dispose();
        }
        if (themeGradientRegistry != null) {
            themeGradientRegistry.removeListener(themeListener);
        	themeGradientRegistry.dispose();
        }
        if (themeFontRegistry != null) {
            themeFontRegistry.removeListener(themeListener);
            themeFontRegistry.dispose();
        }
    }
    
    public ITabThemeDescriptor getTabTheme() {
        return descriptor == null ? null : descriptor.getTabThemeDescriptor();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getGradientRegistry()
     */
    public GradientRegistry getGradientRegistry() {
        if (themeGradientRegistry != null) 
            return themeGradientRegistry;
        else 
            return JFaceResources.getGradientRegistry();

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getId()
     */
    public String getId() {       
        return descriptor == null ? null : descriptor.getID();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.add(listener);        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        propertyChangeListeners.remove(listener);        
    }
    
	private void firePropertyChange(String changeId) {
		Object[] listeners = propertyChangeListeners.getListeners();
		PropertyChangeEvent event =
			new PropertyChangeEvent(this, changeId, this, this);

		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}    
    
    private ListenerList propertyChangeListeners = new ListenerList();
}
