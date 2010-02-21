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

import java.util.ResourceBundle;
import java.util.Set;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.DataFormatException;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public class Theme extends EventManager implements ITheme {

    /**
     * The translation bundle in which to look up internationalized text.
     */
    private final static ResourceBundle RESOURCE_BUNDLE = ResourceBundle
            .getBundle(Theme.class.getName());

    private CascadingColorRegistry themeColorRegistry;

    private CascadingFontRegistry themeFontRegistry;

    private IThemeDescriptor descriptor;

    private IPropertyChangeListener themeListener;

    private CascadingMap dataMap;

    private ThemeRegistry themeRegistry;

    private IPropertyChangeListener propertyListener;

    /**
     * @param descriptor
     */
    public Theme(IThemeDescriptor descriptor) {
        themeRegistry = ((ThemeRegistry) WorkbenchPlugin.getDefault()
                .getThemeRegistry());
        this.descriptor = descriptor;
        IWorkbench workbench = PlatformUI.getWorkbench();
        if (descriptor != null) {
        	ITheme defaultTheme = workbench.getThemeManager().getTheme(
                    IThemeManager.DEFAULT_THEME);
        	
            ColorDefinition[] colorDefinitions = this.descriptor.getColors();
            themeColorRegistry = new CascadingColorRegistry(defaultTheme
                    .getColorRegistry());
            if (colorDefinitions.length > 0) {
                ThemeElementHelper.populateRegistry(this, colorDefinitions,
                		PrefUtil.getInternalPreferenceStore());
            }

            FontDefinition[] fontDefinitions = this.descriptor.getFonts();
            themeFontRegistry = new CascadingFontRegistry(defaultTheme
                    .getFontRegistry());
            if (fontDefinitions.length > 0) {
                ThemeElementHelper.populateRegistry(this, fontDefinitions,
                		PrefUtil.getInternalPreferenceStore());
            }

            dataMap = new CascadingMap(((ThemeRegistry) WorkbenchPlugin
                    .getDefault().getThemeRegistry()).getData(), descriptor
                    .getData());
        }

        getColorRegistry().addListener(getCascadeListener());
        getFontRegistry().addListener(getCascadeListener());
        PrefUtil.getInternalPreferenceStore().addPropertyChangeListener(
                getPropertyListener());
    }

    /**
     * Listener that is responsible for responding to preference changes.
     * 
     * @return the property change listener
     */
    private IPropertyChangeListener getPropertyListener() {
        if (propertyListener == null) {
            propertyListener = new IPropertyChangeListener() {

                /* (non-Javadoc)
                 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
                 */
                public void propertyChange(PropertyChangeEvent event) {
                    String[] split = ThemeElementHelper.splitPropertyName(
                            Theme.this, event.getProperty());
                    String key = split[1];
                    String theme = split[0];
                    if (key.equals(IWorkbenchPreferenceConstants.CURRENT_THEME_ID)) {
						return;
					}
                    try {
                    	String thisTheme = getId();
                                          
                        if (Util.equals(thisTheme, theme)) {
							if (getFontRegistry().hasValueFor(key)) {
								FontData[] data = PreferenceConverter
										.basicGetFontData((String) event
												.getNewValue());
								
								getFontRegistry().put(key, data);
								processDefaultsTo(key, data);
								return;
							}
							else if (getColorRegistry().hasValueFor(key)) {
								RGB rgb = StringConverter.asRGB((String) event
										.getNewValue());
								getColorRegistry().put(key, rgb);
								processDefaultsTo(key, rgb);
								return;
							}
						}                        
                    } catch (DataFormatException e) {
                        //no-op
                    }
                }

                /**
                 * Process all fonts that default to the given ID.
                 * 
                 * @param key the font ID
                 * @param fd the new FontData for defaulted fonts
                 */
                private void processDefaultsTo(String key, FontData[] fd) {
                    FontDefinition[] defs = WorkbenchPlugin.getDefault()
                            .getThemeRegistry().getFontsFor(getId());
                    for (int i = 0; i < defs.length; i++) {
                        String defaultsTo = defs[i].getDefaultsTo();
                        if (defaultsTo != null && defaultsTo.equals(key)) {
                            IPreferenceStore store = WorkbenchPlugin
                                    .getDefault().getPreferenceStore();
                            if (store.isDefault(ThemeElementHelper
                                    .createPreferenceKey(Theme.this, defs[i]
                                            .getId()))) {
                                getFontRegistry().put(defs[i].getId(), fd);
                                processDefaultsTo(defs[i].getId(), fd);
                            }
                        }
                    }
                }

                /**
                 * Process all colors that default to the given ID.
                 * 
                 * @param key the color ID
                 * @param rgb the new RGB value for defaulted colors
                 */
                private void processDefaultsTo(String key, RGB rgb) {
                    ColorDefinition[] defs = WorkbenchPlugin.getDefault()
                            .getThemeRegistry().getColorsFor(getId());
                    for (int i = 0; i < defs.length; i++) {
                        String defaultsTo = defs[i].getDefaultsTo();
                        if (defaultsTo != null && defaultsTo.equals(key)) {
                            IPreferenceStore store = WorkbenchPlugin
                                    .getDefault().getPreferenceStore();
                            if (store.isDefault(ThemeElementHelper
                                    .createPreferenceKey(Theme.this, defs[i]
                                            .getId()))) {
                                getColorRegistry().put(defs[i].getId(), rgb);
                                processDefaultsTo(defs[i].getId(), rgb);
                            }
                        }
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
		if (themeColorRegistry != null) {
			return themeColorRegistry;
		}

		return WorkbenchThemeManager.getInstance()
				.getDefaultThemeColorRegistry();
	}

    public FontRegistry getFontRegistry() {
		if (themeFontRegistry != null) {
			return themeFontRegistry;
		}

		return WorkbenchThemeManager.getInstance()
				.getDefaultThemeFontRegistry();
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
        PrefUtil.getInternalPreferenceStore()
                .removePropertyChangeListener(getPropertyListener());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getId()
     */
    public String getId() {
        return descriptor == null ? IThemeManager.DEFAULT_THEME : descriptor
                .getId();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        addListenerObject(listener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbench#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
     */
    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        removeListenerObject(listener);
    }

    private void firePropertyChange(PropertyChangeEvent event) {
        Object[] listeners = getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyChangeListener) listeners[i]).propertyChange(event);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.themes.ITheme#getLabel()
     */
    public String getLabel() {
        return descriptor == null ? RESOURCE_BUNDLE
                .getString("DefaultTheme.label") : descriptor.getName(); //$NON-NLS-1$ 
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getString(java.lang.String)
     */
    public String getString(String key) {
        if (dataMap != null) {
			return (String) dataMap.get(key);
		}
        return (String) themeRegistry.getData().get(key);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#keySet()
     */
    public Set keySet() {
        if (dataMap != null) {
			return dataMap.keySet();
		}

        return themeRegistry.getData().keySet();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getInt(java.lang.String)
     */
    public int getInt(String key) {
        String string = getString(key);
        if (string == null) {
			return 0;
		}
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.themes.ITheme#getBoolean(java.lang.String)
     */
    public boolean getBoolean(String key) {
        String string = getString(key);
        if (string == null) {
			return false;
		}

        return Boolean.valueOf(getString(key)).booleanValue();
    }
}
