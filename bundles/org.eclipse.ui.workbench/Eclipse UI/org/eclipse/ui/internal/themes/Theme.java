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

import java.util.Set;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public class Theme implements ITheme {

    private CascadingColorRegistry themeColorRegistry;
    private CascadingFontRegistry themeFontRegistry;
    private IThemeDescriptor descriptor;
    
    private IPropertyChangeListener themeListener;
    private CascadingMap dataMap;
    
    private ListenerList propertyChangeListeners = new ListenerList();
    private ThemeRegistry themeRegistry;
    private IPropertyChangeListener propertyListener;
    
    
    /**
     * @param descriptor
     */
    public Theme(IThemeDescriptor descriptor) {
        themeRegistry = ((ThemeRegistry)WorkbenchPlugin.getDefault().getThemeRegistry());
        this.descriptor = descriptor;
        IWorkbench workbench = WorkbenchPlugin.getDefault().getWorkbench();
        if (descriptor != null) {            
            
	        ColorDefinition [] definitions = this.descriptor.getColors();
	        
	        ITheme theme = workbench.getThemeManager().getTheme(IThemeManager.DEFAULT_THEME);
            if (definitions.length > 0) {	           
                themeColorRegistry = new CascadingColorRegistry(theme.getColorRegistry());
	            ThemeElementHelper.populateRegistry(this, definitions, workbench.getPreferenceStore());	            
	        }
            
	        FontDefinition [] fontDefinitions = this.descriptor.getFonts();
	        if (fontDefinitions.length > 0) {
	            themeFontRegistry = new CascadingFontRegistry(theme.getFontRegistry());	            
	            ThemeElementHelper.populateRegistry(this, fontDefinitions, workbench.getPreferenceStore());
	        }
	        
	        dataMap = new CascadingMap(((ThemeRegistry)WorkbenchPlugin.getDefault().getThemeRegistry()).getData(), descriptor.getData());   
        }
        
        getColorRegistry().addListener(getCascadeListener());
        getFontRegistry().addListener(getCascadeListener());
        workbench.getPreferenceStore().addPropertyChangeListener(getPropertyListener());
    }
    
    /**
     * Listener that is responsible for responding to preference changes.
     * 
     * @return
     */
    private IPropertyChangeListener getPropertyListener() {
        if (propertyListener == null) {
            propertyListener = new IPropertyChangeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent event) {                    
                    String key = ThemeElementHelper.splitPreferenceKey(Theme.this, event.getProperty());
                    try { 
	                    if (themeColorRegistry != null) { // we're using cascading registries
	                        if (((CascadingColorRegistry)getColorRegistry()).hasOverrideFor(key)) {
		                        RGB rgb = StringConverter.asRGB((String) event.getNewValue());
		                        getColorRegistry().put(key, rgb);
		                    }
		                    else if (((CascadingFontRegistry)getFontRegistry()).hasOverrideFor(key)) {
		                        FontData data = StringConverter.asFontData((String) event.getNewValue());
		                        getFontRegistry().put(key, new FontData [] {data});
		                    }	                        
	                    }
	                    else { // we're the default theme
	                        if (getColorRegistry().hasValueFor(key)) {
		                        RGB rgb = StringConverter.asRGB((String) event.getNewValue());
		                        getColorRegistry().put(key, rgb);
		                    }
		                    else if (getFontRegistry().hasValueFor(key)) {
		                        FontData data = StringConverter.asFontData((String) event.getNewValue());
		                        getFontRegistry().put(key, new FontData [] {data});
		                    }	                        
	                        
	                    }
                    }
                    catch (DataFormatException e) {
                        //no-op
                    }                                                                    
                }                
            };
        }
        return propertyListener;
    }
    
    /**
     * Listener that is responsible for rebroadcasting events fired from the base font/color registry
     */
    private IPropertyChangeListener getCascadeListener() {
        if (themeListener == null) {
            themeListener = new IPropertyChangeListener() {
            
                /* (non-Javadoc)
                 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent event) {
                    firePropertyChange(event);
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
        if (themeFontRegistry != null) {
            themeFontRegistry.removeListener(themeListener);
            themeFontRegistry.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getId()
     */
    public String getId() {       
        return descriptor == null ? null : descriptor.getId();
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
    
	private void firePropertyChange(PropertyChangeEvent event) {
		Object[] listeners = propertyChangeListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}    
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getLabel()
     */
    public String getLabel() {
        return descriptor == null ? null : descriptor.getLabel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getString(java.lang.String)
     */
    public String getString(String key) {
        if (dataMap != null)
            return (String) dataMap.get(key);
        return (String) themeRegistry.getData().get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#keySet()
     */
    public Set keySet() {
        if (dataMap != null)
            return dataMap.keySet();
        
        return themeRegistry.getData().keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getInt(java.lang.String)
     */
    public int getInt(String key) {
        String string = getString(key);
        if (string == null)
            return 0;
        try {            
            return Integer.parseInt(string);
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String key) {
        String string = getString(key);
        if (string == null)
            return false;
        
        return Boolean.valueOf(getString(key)).booleanValue();
    }
}
