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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Theme manager for the Workbench.
 * 
 * @since 3.0
 */
public class WorkbenchThemeManager extends EventManager implements
		IThemeManager {

	private static final String SYSTEM_DEFAULT_THEME = "org.eclipse.ui.ide.systemDefault";//$NON-NLS-1$

	private static WorkbenchThemeManager instance;

	/**
	 * Returns the singelton instance of the WorkbenchThemeManager
	 * 
	 * @return singleton instance
	 */
	public static synchronized WorkbenchThemeManager getInstance() {
		if (instance == null) {
			instance = new WorkbenchThemeManager();
			instance.getCurrentTheme(); // initialize the current theme
		}
		return instance;
	}

	private ITheme currentTheme;

	private IPropertyChangeListener currentThemeListener = new IPropertyChangeListener() {

		public void propertyChange(PropertyChangeEvent event) {
			firePropertyChange(event);
			if (event.getSource() instanceof FontRegistry) {
				JFaceResources.getFontRegistry().put(event.getProperty(),
						(FontData[]) event.getNewValue());
			} else if (event.getSource() instanceof ColorRegistry) {
				JFaceResources.getColorRegistry().put(event.getProperty(),
						(RGB) event.getNewValue());
			}
		}
	};

	private ColorRegistry defaultThemeColorRegistry;

	private FontRegistry defaultThemeFontRegistry;

	private IThemeRegistry themeRegistry;

	private Map themes = new HashMap(7);

	/*
	 * Initialize the WorkbenchThemeManager.
	 * Determine the default theme according to the following rules:
	 *   1) If we're in HC mode then default to system default
	 *   2) Otherwise, if preference already set (e.g. via plugin_customization.ini), then observe that value
	 *   3) Otherwise, use our default
	 * Call dispose when we close.
	 */
	private WorkbenchThemeManager() {
		defaultThemeColorRegistry = new ColorRegistry(PlatformUI.getWorkbench()
				.getDisplay());

		defaultThemeFontRegistry = new FontRegistry(PlatformUI.getWorkbench()
				.getDisplay());

		// copy the font values from preferences.
		FontRegistry jfaceFonts = JFaceResources.getFontRegistry();
		for (Iterator i = jfaceFonts.getKeySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			defaultThemeFontRegistry.put(key, jfaceFonts.getFontData(key));
		}

		//Theme might be set via plugin_configuration.ini
		String themeId = PrefUtil.getAPIPreferenceStore().getDefaultString(IWorkbenchPreferenceConstants.CURRENT_THEME_ID);

		//If not set, use default
		if(themeId.length() == 0)
			themeId = IThemeManager.DEFAULT_THEME;
			
		// Check if we are in high contrast mode. If so then set the theme to
		// the system default
		if (PlatformUI.getWorkbench().getDisplay() != null) {
			// Determine the high contrast setting before
			// any access to preferences
			final boolean[] highContrast = new boolean[] { false };
			PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see java.lang.Runnable#run()
				 */
				public void run() {
					highContrast[0] = Display.getCurrent().getHighContrast();

					Display.getCurrent().addListener(SWT.Settings, new Listener() {
						public void handleEvent(Event event) {
							updateThemes();
						}
					});
				}
			});
			
			//If in HC, *always* use the system default.
			//This ignores any default theme set via plugin_customization.ini
			if (highContrast[0])
				themeId = SYSTEM_DEFAULT_THEME;
		}

		PrefUtil.getAPIPreferenceStore().setDefault(
				IWorkbenchPreferenceConstants.CURRENT_THEME_ID, themeId);
	}

	/*
	 * Update existing theme contents, descriptors, and registries.
	 * Reread the themes and recompute the registries.
	 */	
	private void updateThemes() {
		//reread the themes since their descriptors have changed in value
        ThemeRegistryReader reader = new ThemeRegistryReader();
        reader.readThemes(Platform.getExtensionRegistry(),(ThemeRegistry) getThemeRegistry());   

        //DEFAULT_THEME is not in getThemes() list so must be handled special
        ThemeElementHelper.populateRegistry(getTheme(IThemeManager.DEFAULT_THEME), getThemeRegistry().getColors(), PrefUtil.getInternalPreferenceStore());			
        
        IThemeDescriptor[] themeDescriptors = getThemeRegistry().getThemes();

       	for (int i=0; i < themeDescriptors.length; i++) {
        	IThemeDescriptor themeDescriptor = themeDescriptors[i];
    		ITheme theme = (ITheme) themes.get(themeDescriptor);
    		//If theme is in our themes table then its already been populated
    		if (theme != null) {
                ColorDefinition[] colorDefinitions = themeDescriptor.getColors();
              
               if (colorDefinitions.length > 0) {
                	ThemeElementHelper.populateRegistry(theme, colorDefinitions,PrefUtil.getInternalPreferenceStore());
                }
    		}
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IThemeManager#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		addListenerObject(listener);
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

	private boolean doSetCurrentTheme(String id) {
		ITheme oldTheme = currentTheme;
		ITheme newTheme = getTheme(id);
		if (oldTheme != newTheme && newTheme != null) {
			currentTheme = newTheme;
			return true;
		}

		return false;
	}

	protected void firePropertyChange(PropertyChangeEvent event) {
		Object[] listeners = getListeners();

		for (int i = 0; i < listeners.length; i++) {
			((IPropertyChangeListener) listeners[i]).propertyChange(event);
		}
	}

	protected void firePropertyChange(String changeId, ITheme oldTheme,
			ITheme newTheme) {

		PropertyChangeEvent event = new PropertyChangeEvent(this, changeId,
				oldTheme, newTheme);
		firePropertyChange(event);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IThemeManager#getCurrentTheme()
	 */
	public ITheme getCurrentTheme() {
		if (currentTheme == null) {
			String themeId = PrefUtil.getAPIPreferenceStore().getString(
					IWorkbenchPreferenceConstants.CURRENT_THEME_ID);

			if (themeId == null) // missing preference
				setCurrentTheme(IThemeManager.DEFAULT_THEME);
			else {
				setCurrentTheme(themeId);
				if (currentTheme == null) { // still null - the preference
											// didn't resolve to a proper theme
					setCurrentTheme(IThemeManager.DEFAULT_THEME);
					StatusManager
							.getManager()
							.handle(
									StatusUtil
											.newStatus(
													PlatformUI.PLUGIN_ID,
													"Could not restore current theme: " + themeId, null)); //$NON-NLS-1$
				}
			}			
		}
		return currentTheme;
	}

	/**
	 * Return the default color registry.
	 * 
	 * @return the default color registry
	 */
	public ColorRegistry getDefaultThemeColorRegistry() {
		return defaultThemeColorRegistry;
	}

	/**
	 * Return the default font registry.
	 * 
	 * @return the default font registry
	 */
	public FontRegistry getDefaultThemeFontRegistry() {
		return defaultThemeFontRegistry;
	}

	private ITheme getTheme(IThemeDescriptor td) {
		ITheme theme = (ITheme) themes.get(td);
		if (theme == null) {
			theme = new Theme(td);
			themes.put(td, theme);
		}
		return theme;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IThemeManager#getTheme(java.lang.String)
	 */
	public ITheme getTheme(String id) {
		if (id.equals(IThemeManager.DEFAULT_THEME)) {
			return getTheme((IThemeDescriptor) null);
		}

		IThemeDescriptor td = getThemeRegistry().findTheme(id);
		if (td == null) {
			return null;
		}
		return getTheme(td);
	}

	/**
	 * Answer the IThemeRegistry for the Workbench
	 */
	private IThemeRegistry getThemeRegistry() {
		if (themeRegistry == null) {
			themeRegistry = WorkbenchPlugin.getDefault().getThemeRegistry();
		}
		return themeRegistry;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IThemeManager#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.themes.IThemeManager#setCurrentTheme(java.lang.String)
	 */
	public void setCurrentTheme(String id) {
		ITheme oldTheme = currentTheme;
		if (WorkbenchThemeManager.getInstance().doSetCurrentTheme(id)) {
			firePropertyChange(CHANGE_CURRENT_THEME, oldTheme,
					getCurrentTheme());
			if (oldTheme != null) {
				oldTheme.removePropertyChangeListener(currentThemeListener);
			}
			currentTheme.addPropertyChangeListener(currentThemeListener);

			// update the preference if required.
			if (!PrefUtil.getAPIPreferenceStore().getString(
					IWorkbenchPreferenceConstants.CURRENT_THEME_ID).equals(id)) {
				PrefUtil.getAPIPreferenceStore().setValue(
						IWorkbenchPreferenceConstants.CURRENT_THEME_ID, id);
				PrefUtil.saveAPIPrefs();
			}

			// update the jface registries
			{
				ColorRegistry jfaceColors = JFaceResources.getColorRegistry();
				ColorRegistry themeColors = currentTheme.getColorRegistry();
				for (Iterator i = themeColors.getKeySet().iterator(); i
						.hasNext();) {
					String key = (String) i.next();
					jfaceColors.put(key, themeColors.getRGB(key));
				}
			}
			{
				FontRegistry jfaceFonts = JFaceResources.getFontRegistry();
				FontRegistry themeFonts = currentTheme.getFontRegistry();
				for (Iterator i = themeFonts.getKeySet().iterator(); i
						.hasNext();) {
					String key = (String) i.next();
					jfaceFonts.put(key, themeFonts.getFontData(key));
				}
			}
		}
	}
}
