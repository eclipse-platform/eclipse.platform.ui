/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import java.util.Objects;
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
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * @since 3.0
 */
public class Theme extends EventManager implements ITheme {

	/**
	 * The translation bundle in which to look up internationalized text.
	 */
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(Theme.class.getName());

	private CascadingColorRegistry themeColorRegistry;

	private CascadingFontRegistry themeFontRegistry;

	private IThemeDescriptor descriptor;

	private IPropertyChangeListener themeListener;

	private CascadingMap dataMap;

	private ThemeRegistry themeRegistry;

	private IPropertyChangeListener propertyListener;

	public Theme(IThemeDescriptor descriptor) {
		themeRegistry = ((ThemeRegistry) WorkbenchPlugin.getDefault().getThemeRegistry());
		this.descriptor = descriptor;
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (descriptor != null) {
			ITheme defaultTheme = workbench.getThemeManager().getTheme(IThemeManager.DEFAULT_THEME);

			ColorDefinition[] colorDefinitions = this.descriptor.getColors();
			themeColorRegistry = new CascadingColorRegistry(defaultTheme.getColorRegistry());
			if (colorDefinitions.length > 0) {
				ThemeElementHelper.populateRegistry(this, colorDefinitions, PrefUtil.getInternalPreferenceStore());
			}

			FontDefinition[] fontDefinitions = this.descriptor.getFonts();
			themeFontRegistry = new CascadingFontRegistry(defaultTheme.getFontRegistry());
			if (fontDefinitions.length > 0) {
				ThemeElementHelper.populateRegistry(this, fontDefinitions, PrefUtil.getInternalPreferenceStore());
			}

			dataMap = new CascadingMap(((ThemeRegistry) WorkbenchPlugin.getDefault().getThemeRegistry()).getData(),
					descriptor.getData());
		}

		getColorRegistry().addListener(getCascadeListener());
		getColorRegistry().addListener(this::registryColorChangeEvent);
		getFontRegistry().addListener(getCascadeListener());
		PrefUtil.getInternalPreferenceStore().addPropertyChangeListener(getPropertyListener());
	}

	/**
	 * When a color in the registry is updated, update the backing preferences
	 * appropriately.
	 *
	 * @param event the color change event
	 */
	private void registryColorChangeEvent(PropertyChangeEvent event) {
		if (event.getNewValue() instanceof RGB) {
			String key = ThemeElementHelper.createPreferenceKey(this, event.getProperty());
			IPreferenceStore store = PrefUtil.getInternalPreferenceStore();
			if (store.contains(key)) {
				RGB newColor = (RGB) event.getNewValue();
				if (store.isDefault(key)) {
					RGB oldColor = PreferenceConverter.getDefaultColor(store, key);
					if (oldColor == PreferenceConverter.COLOR_DEFAULT_DEFAULT) {
						// If the preference is set to default, but there is no actual default value,
						// then the preference state is inconsistent. Do nothing.
					} else if (!newColor.equals(oldColor))
						PreferenceConverter.setValue(store, key, newColor);
				} else {
					RGB oldColor = PreferenceConverter.getColor(store, key);
					if (!newColor.equals(oldColor)) {
						oldColor = PreferenceConverter.getDefaultColor(store, key);
						if (oldColor != PreferenceConverter.COLOR_DEFAULT_DEFAULT && newColor.equals(oldColor))
							store.setToDefault(key);
						else
							PreferenceConverter.setValue(store, key, newColor);
					}
				}
			}
		}
	}

	private static String[] splitPropertyName(String property) {
		IThemeDescriptor[] descriptors = WorkbenchPlugin.getDefault().getThemeRegistry().getThemes();
		for (IThemeDescriptor themeDescriptor : descriptors) {
			String id = themeDescriptor.getId();
			if (property.startsWith(id + '.')) { // the property starts with
													// a known theme ID -
													// extract and return it and
													// the remaining property
				return new String[] { property.substring(0, id.length()), property.substring(id.length() + 1) };
			}
		}

		// default is simply return the default theme ID and the raw property
		return new String[] { IThemeManager.DEFAULT_THEME, property };
	}

	/**
	 * Listener that is responsible for responding to preference changes.
	 *
	 * @return the property change listener
	 */
	private IPropertyChangeListener getPropertyListener() {
		if (propertyListener == null) {
			propertyListener = new IPropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent event) {
					String[] split = splitPropertyName(event.getProperty());
					String key = split[1];
					String theme = split[0];
					if (key.equals(IWorkbenchPreferenceConstants.CURRENT_THEME_ID)) {
						return;
					}
					try {
						String thisTheme = getId();

						if (Objects.equals(thisTheme, theme)) {
							if (getFontRegistry().hasValueFor(key)) {
								FontData[] data = event.getNewValue() instanceof String
										? PreferenceConverter.basicGetFontData((String) event.getNewValue())
										: (FontData[]) event.getNewValue();

								getFontRegistry().put(key, data);
								processDefaultsTo(key, data);
							} else if (getColorRegistry().hasValueFor(key)) {
								RGB rgb = event.getNewValue() instanceof String
										? StringConverter.asRGB((String) event.getNewValue())
										: (RGB) event.getNewValue();
								if (!Objects.equals(getColorRegistry().getRGB(key), rgb)) {
									getColorRegistry().put(key, rgb);
									processDefaultsTo(key, rgb);
								}
							}
						}
					} catch (DataFormatException e) {
						// no-op
					}
				}

				/**
				 * Process all fonts that default to the given ID.
				 *
				 * @param key the font ID
				 * @param fd  the new FontData for defaulted fonts
				 */
				private void processDefaultsTo(String key, FontData[] fd) {
					FontDefinition[] defs = WorkbenchPlugin.getDefault().getThemeRegistry().getFontsFor(getId());
					for (FontDefinition fontDefinition : defs) {
						String defaultsTo = fontDefinition.getDefaultsTo();
						if (defaultsTo != null && defaultsTo.equals(key)) {
							IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
							if (store.isDefault(
									ThemeElementHelper.createPreferenceKey(Theme.this, fontDefinition.getId()))) {
								getFontRegistry().put(fontDefinition.getId(), fd);
								processDefaultsTo(fontDefinition.getId(), fd);
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
					ColorDefinition[] defs = WorkbenchPlugin.getDefault().getThemeRegistry().getColorsFor(getId());
					for (ColorDefinition colorDefinition : defs) {
						String defaultsTo = colorDefinition.getDefaultsTo();
						if (defaultsTo != null && defaultsTo.equals(key)) {
							IPreferenceStore store = WorkbenchPlugin.getDefault().getPreferenceStore();
							String prefkey = ThemeElementHelper.createPreferenceKey(Theme.this,
									colorDefinition.getId());
							if (store.isDefault(prefkey)) {
								PreferenceConverter.setDefault(store, prefkey, rgb);
								getColorRegistry().put(colorDefinition.getId(), rgb);
								processDefaultsTo(colorDefinition.getId(), rgb);
							}
						}
					}
				}
			};
		}
		return propertyListener;
	}

	/**
	 * Listener that is responsible for rebroadcasting events fired from the base
	 * font/color registry
	 */
	private IPropertyChangeListener getCascadeListener() {
		if (themeListener == null) {
			themeListener = this::firePropertyChange;
		}
		return themeListener;
	}

	@Override
	public ColorRegistry getColorRegistry() {
		if (themeColorRegistry != null) {
			return themeColorRegistry;
		}

		return WorkbenchThemeManager.getInstance().getDefaultThemeColorRegistry();
	}

	@Override
	public FontRegistry getFontRegistry() {
		if (themeFontRegistry != null) {
			return themeFontRegistry;
		}

		return WorkbenchThemeManager.getInstance().getDefaultThemeFontRegistry();
	}

	@Override
	public void dispose() {
		if (themeColorRegistry != null) {
			themeColorRegistry.removeListener(themeListener);
			themeColorRegistry.removeListener(this::registryColorChangeEvent);
			themeColorRegistry.dispose();
		}
		if (themeFontRegistry != null) {
			themeFontRegistry.removeListener(themeListener);
			themeFontRegistry.dispose();
		}
		PrefUtil.getInternalPreferenceStore().removePropertyChangeListener(getPropertyListener());
	}

	@Override
	public String getId() {
		return descriptor == null ? IThemeManager.DEFAULT_THEME : descriptor.getId();
	}

	@Override
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	@Override
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
	}

	private void firePropertyChange(PropertyChangeEvent event) {
		for (Object listener : getListeners()) {
			((IPropertyChangeListener) listener).propertyChange(event);
		}
	}

	@Override
	public String getLabel() {
		return descriptor == null ? RESOURCE_BUNDLE.getString("DefaultTheme.label") : descriptor.getName(); //$NON-NLS-1$
	}

	@Override
	public String getString(String key) {
		if (dataMap != null) {
			return (String) dataMap.get(key);
		}
		return (String) themeRegistry.getData().get(key);
	}

	@Override
	public Set keySet() {
		if (dataMap != null) {
			return dataMap.keySet();
		}

		return themeRegistry.getData().keySet();
	}

	@Override
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

	@Override
	public boolean getBoolean(String key) {
		String string = getString(key);
		if (string == null) {
			return false;
		}

		return Boolean.parseBoolean(getString(key));
	}
}
