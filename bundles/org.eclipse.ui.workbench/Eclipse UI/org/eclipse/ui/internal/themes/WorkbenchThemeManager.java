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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IPreferenceConstants;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;


/**
 * Theme manager for the Workbench.
 *
 * @since 3.0
 */
public class WorkbenchThemeManager implements IThemeManager {

	private IThemeRegistry themeRegistry;
	private static WorkbenchThemeManager instance;
	
	/*
	 * Call dispose when we close
	 */
	private WorkbenchThemeManager () {
		//no-op
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
	
	/**
	 * Disposes all ThemeEntries.
	 */
	public void dispose() {		
		for (Iterator i = themes.values().iterator(); i.hasNext();) {
            ITheme theme = (ITheme) i.next();
            theme.removePropertyChangeListener(currentThemeListener);
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
	
	// kims prototype
	public ITheme getTheme(String id) {
	    IThemeDescriptor td = id == null ? null : getThemeRegistry().findTheme(id);
	    return getTheme(td);
	}

    private ITheme getTheme(IThemeDescriptor td) {        
        ITheme theme = (ITheme) themes.get(td);
        if (theme == null) {
            theme = new Theme(td);
            //theme.addPropertyChangeListener(myListener);
            themes.put(td, theme);
        }
        return theme;
    }
    
    private IPropertyChangeListener currentThemeListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            firePropertyChange(event);            
        }
    };        
    
    private Map themes = new HashMap(7);

    /**
     * @return
     */
    public ITheme getCurrentTheme() {
        if (currentTheme == null) {
    		String themeId = PlatformUI.getWorkbench().getPreferenceStore().getString(IPreferenceConstants.CURRENT_THEME_ID);
    		if (themeId.equals("")) //$NON-NLS-1$
    		    themeId = IThemeManager.DEFAULT_THEME;
    		
            setCurrentTheme(themeId);
        }
        return currentTheme;
    }

    public void setCurrentTheme(String id) {
	    ITheme oldTheme = currentTheme;
	    if (WorkbenchThemeManager.getInstance().doSetCurrentTheme(id)) {
	        firePropertyChange(CHANGE_CURRENT_THEME, oldTheme, getCurrentTheme());
		    if (oldTheme != null) 
		        oldTheme.removePropertyChangeListener(currentThemeListener);
		    currentTheme.addPropertyChangeListener(currentThemeListener);	
		    
			WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.CURRENT_THEME_ID, id == null ? "" : id); //$NON-NLS-1$
			WorkbenchPlugin.getDefault().savePluginPreferences();
	    }	    
    }
    
    private boolean doSetCurrentTheme(String id) {
        ITheme oldTheme = currentTheme;
        ITheme newTheme = getTheme(id); 
        if (oldTheme != newTheme) {
	        currentTheme = newTheme;
            return true;
        }       
        
        return false;
    }
    
    private ITheme currentTheme;
    
    protected void firePropertyChange(PropertyChangeEvent event) {
		Object[] listeners = propertyChangeListeners.getListeners();
		
		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}        
    }        
    
    
	protected void firePropertyChange(
			String changeId,
			ITheme oldTheme,
			ITheme newTheme) {

			PropertyChangeEvent event =
				new PropertyChangeEvent(this, changeId, oldTheme, newTheme);
			firePropertyChange(event);
	}    
	    
	private ListenerList propertyChangeListeners = new ListenerList();

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
	    propertyChangeListeners.add(listener);        
	}
	
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
	    propertyChangeListeners.remove(listener);        
	}
}
