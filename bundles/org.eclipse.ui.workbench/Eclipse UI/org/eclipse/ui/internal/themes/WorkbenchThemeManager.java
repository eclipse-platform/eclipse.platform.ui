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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Theme manager for the Workbench.
 *
 * @since 3.0
 */
public class WorkbenchThemeManager {

	private Map themeEntries = new HashMap(11);
	private IThemeRegistry themeRegistry;
	private static WorkbenchThemeManager instance;
	
	/*
	 * Call dispose when we close
	 */
	private WorkbenchThemeManager () {
		Display display = Display.getDefault();
		if (display != null) {
			display.disposeExec(new Runnable() {
				public void run() {
					WorkbenchThemeManager.this.dispose();
				}	
			});
		}
	}
	
	/**
	 * Returns the singelton instance of the WorkbenchThemeManager
	 * 
	 * @return singleton instance
	 */
	public static WorkbenchThemeManager getInstance() {
		if (instance == null)
			instance = new WorkbenchThemeManager();
		return instance;
	}
	
	/*
	 * Hash entry, holds all attributes of a Theme, caches colors and images, 
	 * and disposes them when the workbench closes.
	 */
	private class ThemeEntry {
		private HashMap images = new HashMap(11);
		private HashMap colors = new HashMap(11);
		private HashMap gradients = new HashMap(11);
		private HashMap fonts = new HashMap(11);
		
		/* 
		 * dispose all SWT resources here.
		 */
		void dispose() {
			// dispose of images
			Iterator iter = images.values().iterator();
			while (iter.hasNext()) {
				Image image = (Image)iter.next();
				if (image != null) {
					image.dispose();
				}
			}	
			images.clear();
			
			// dispose of colors
			iter = colors.values().iterator();
			while (iter.hasNext()) {
				Color color = (Color)iter.next();
				if (color != null) {
					color.dispose();
				}
			}	
			colors.clear();
			
			// dispose of gradients
			iter = gradients.values().iterator();
			while (iter.hasNext()) {
				Color[] grads = (Color[])iter.next();
				if (grads != null) {
					for (int i = 0; i<=grads.length-1; i++)
						grads[1].dispose();
				}
				grads = null;
			}	
			gradients.clear();
				
			// dispose of images
			iter = fonts.values().iterator();
			while (iter.hasNext()) {
				Font font = (Font)iter.next();
				if (font != null) {
					font.dispose();
				}
			}	
			fonts.clear();
		}
	}
	
	/*
	 * Get the entry that maps to the theme id string
	 */
	 private ThemeEntry getThemeEntry(String theme) {
		ThemeEntry entry = (ThemeEntry) themeEntries.get(theme);
		if (entry == null) {
			entry = new ThemeEntry();
			themeEntries.put(theme, entry);
		}
		return entry;
	}
	
	/**
	 * Disposes all ThemeEntries.
	 */
	public void dispose() {
		Iterator iter = themeEntries.values().iterator();
		while (iter.hasNext()) {
			ThemeEntry entry = (ThemeEntry)iter.next();
			if (entry != null) {
				entry.dispose();
			}
		}	
		themeEntries.clear();
		
		for (Iterator i = themes.values().iterator(); i.hasNext();) {
            ITheme theme = (ITheme) i.next();
            theme.removePropertyChangeListener(myListener);
            theme.dispose();
        }
		themes.clear();
	}
	
	/*
	 * Answer the IThemeRegistry for the Workbench 
	 */
	private IThemeRegistry getThemeRegistry () {
		if (themeRegistry == null) {
			themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
		}
		return themeRegistry;
	}
	
	public Color getViewColor (String theme, String key) {
		if (theme == null)
			return null;
		ThemeEntry entry = getThemeEntry(theme);
		Color value = (Color)entry.colors.get(key);
		if (value == null) {
			IViewThemeDescriptor vtd = getThemeRegistry().findTheme(theme).getViewThemeDescriptor();
			value = vtd.getColor(key);
			entry.colors.put(key, value);
		}		
		return value;
	}
	
	public Color[] getViewGradientColors (String theme, String key) {
		if (theme == null)
			return null;
		ThemeEntry entry = getThemeEntry(theme);
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return null;	
		Color[] value = (Color[])entry.gradients.get(key);
		if (value == null) {
			IViewThemeDescriptor vtd = td.getViewThemeDescriptor();	
			value = vtd.getGradientColors(key);
			entry.gradients.put(key, value);
		}		
		return value;
	}
	
	/**
	 *  Get the percents of the gradient fill for the specified theme.
	 * 
	 * @return direction
	 */
	public int[] getViewGradientPercents (String theme, String key) {
		if (theme == null) {
			return null;
		}
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return null;
		IViewThemeDescriptor vtd = td.getViewThemeDescriptor();
		return vtd.getGradientPercents(key);
	}
	
	/**
	 *  Get the direction of the gradient fill for the specified theme.
	 * 
	 * @return direction
	 */
	public int getViewGradientDirection (String theme, String key) {
		if (theme == null) {
			return 0;
		}
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return 0;
		IViewThemeDescriptor vtd = td.getViewThemeDescriptor();
		return vtd.getGradientDirection(key);
	}
	
	/**
	 *  Get the view border style of the specified theme.
	 * 
	 * @return direction
	 */
	public int getViewBorderStyle (String theme, String key) {
		if (theme == null) {
			return 0;
		}
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return 0;
		IViewThemeDescriptor vtd = td.getViewThemeDescriptor();
		return vtd.getBorderStyle();
	}
	
	/**
	 * Get the font of the titlebar text for the specified theme.
	 * 
	 * @return font
	 */
	public Font getViewFont (String theme, String key) {
		if (theme == null) {
			return null;
		}
		ThemeEntry entry = getThemeEntry(theme);
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return null;
		Font result = (Font)entry.fonts.get(key);
		if (result == null) {
			IViewThemeDescriptor vtd = td.getViewThemeDescriptor();
			result = vtd.getFont(key);
			entry.fonts.put(key, result);
		}
		return result;
	}

	
	/**
	 *  Get the font of the titlebar text for the specified theme.
	 * 
	 * @return font
	 */
	public Font getTabFont (String theme, String key) {
		if (theme == null) {
			return null;
		}
		ThemeEntry entry = getThemeEntry(theme);
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td == null) return null;
		Font result = (Font)entry.fonts.get(key);
		if (result == null) {
			ITabThemeDescriptor tabtd = td.getTabThemeDescriptor();
			result = tabtd.getFont(key);
			entry.fonts.put(key, result);
		}
		return result;
	}

	public Color getTabColor (String theme, String key) {
		if (theme == null)
			return null;
		ThemeEntry entry = getThemeEntry(theme);
		Color value = (Color)entry.colors.get(key);
		if (value == null) {
			ITabThemeDescriptor tabtd = getThemeRegistry().findTheme(theme).getTabThemeDescriptor();
			value = tabtd.getColor(key);
			entry.colors.put(key, value);
		}		
		return value;
	}

	/**
	 * Return the TabThemeDescriptor that maps to the theme id string
	 */
	public ITabThemeDescriptor getTabThemeDescriptor(String theme) {
		ITabThemeDescriptor tabtd = null;
		IThemeDescriptor td =  getThemeRegistry().findTheme(theme);
		if (td != null) {
			tabtd = td.getTabThemeDescriptor();			
		}
		return tabtd;
	}
	
	// kims prototype
	public ITheme getTheme(String id) {
	    IThemeDescriptor td = id == null ? null : getThemeRegistry().findTheme(id);
	    return getTheme(td);
	}

    // kims prototype
    private ITheme getTheme(IThemeDescriptor td) {        
        ITheme theme = (ITheme) themes.get(td);
        if (theme == null) {
            theme = new Theme(td);
            theme.addPropertyChangeListener(myListener);
            themes.put(td, theme);
        }
        return theme;
    }
    
    private IPropertyChangeListener myListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event.getProperty(), (ITheme) event.getSource());            
        }        
    };
    
    private Map themes = new HashMap(7);

    /**
     * @return
     */
    public ITheme getCurrentTheme() {
        if (currentTheme == null)
            setCurrentTheme(null);
        return currentTheme;
    }
    
    public boolean setCurrentTheme(String id) {
        ITheme oldTheme = currentTheme;
        ITheme newTheme = getTheme(id); 
        if (oldTheme != newTheme) {
	        currentTheme = newTheme;
            return true;
        }
        
        return false;
    }
    
    private ITheme currentTheme;
    
	protected void firePropertyChange(
			String changeId,
			ITheme theme) {
			Object[] listeners = propertyChangeListeners.getListeners();
			PropertyChangeEvent event =
				new PropertyChangeEvent(theme, changeId, theme, theme);

			for (int i = 0; i < listeners.length; i++) {
				((IPropertyChangeListener) listeners[i]).propertyChange(event);
			}
		}    
	    
	private ListenerList propertyChangeListeners = new ListenerList();

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	    propertyChangeListeners.add(listener);        
	}
	
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	    propertyChangeListeners.remove(listener);        
	}
}
