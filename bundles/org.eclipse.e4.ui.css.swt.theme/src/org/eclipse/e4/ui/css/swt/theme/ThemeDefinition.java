/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.theme;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.e4.ui.css.swt.internal.theme.ThemeEngine;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * A theme as declared by a <code>theme</code> element of the
 * <code>org.eclipse.e4.ui.css.swt.theme</code> extension point, resolved for
 * the current platform. This is what the theme engine registers, and it can
 * also be read when there is no theme engine.
 *
 * @param id         the id the theme is registered with, including the OS
 *                   version if the theme declares one
 * @param label      the label
 * @param osVersion  the OS version the theme declares, or the empty string
 * @param dark       whether the theme is dark
 * @param stylesheet the <code>basestylesheeturi</code> as declared
 * @param contributor the name of the contributing bundle
 */
public record ThemeDefinition(String id, String label, String osVersion, boolean dark, String stylesheet,
		String contributor) implements ITheme {

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public boolean isDark() {
		return dark;
	}

	/**
	 * Reads the themes contributed for the current platform, in the order of the
	 * extension registry. A theme id contributed more than once is returned more
	 * than once.
	 *
	 * @return the themes
	 */
	public static List<ThemeDefinition> readAll() {
		List<ThemeDefinition> result = new ArrayList<>();
		IExtensionPoint extPoint = RegistryFactory.getRegistry().getExtensionPoint(ThemeEngine.THEME_PLUGIN_ID);
		String currentOS = Platform.getOS();
		boolean e4DarkMacFound = false;
		for (IExtension extension : extPoint.getExtensions()) {
			for (IConfigurationElement ce : getPlatformMatches(extension.getConfigurationElements())) {
				if (!ce.getName().equals("theme")) { //$NON-NLS-1$
					continue;
				}
				String id = ce.getAttribute("id"); //$NON-NLS-1$
				String os = ce.getAttribute("os"); //$NON-NLS-1$
				String version = ce.getAttribute("os_version"); //$NON-NLS-1$

				/*
				 * Code to support e4 dark theme on Mac 10.13 and older. For e4 dark theme on
				 * Mac, register the theme with matching OS version if specified.
				 */
				if (ThemeEngine.E4_DARK_THEME_ID.equals(id) && Platform.OS_MACOSX.equals(currentOS) && os != null
						&& os.equals(currentOS)) {
					// If e4 dark theme on Mac was already registered, don't try to match or
					// register again.
					if (e4DarkMacFound) {
						continue;
					}
					if (version != null && !isOsVersionMatch(version)) {
						continue;
					}
					e4DarkMacFound = true;
					version = ""; //$NON-NLS-1$
				}

				if (version == null) {
					version = ""; //$NON-NLS-1$
				}
				result.add(new ThemeDefinition(id + version, ce.getAttribute("label"), version, isDarkTheme(ce), //$NON-NLS-1$
						ce.getAttribute("basestylesheeturi"), ce.getContributor().getName())); //$NON-NLS-1$
			}
		}
		return result;
	}

	/**
	 * Get all elements that have os/ws attributes that best match the current
	 * platform.
	 *
	 * @param elements the elements to check
	 * @return the best matches, if any
	 */
	public static IConfigurationElement[] getPlatformMatches(IConfigurationElement[] elements) {
		Bundle bundle = FrameworkUtil.getBundle(ThemeDefinition.class);
		String osname = bundle.getBundleContext().getProperty("osgi.os"); //$NON-NLS-1$
		// TODO: Need to differentiate win32 versions
		String wsname = bundle.getBundleContext().getProperty("osgi.ws"); //$NON-NLS-1$
		ArrayList<IConfigurationElement> matchingElements = new ArrayList<>();
		for (IConfigurationElement element : elements) {
			String elementOs = element.getAttribute("os"); //$NON-NLS-1$
			String elementWs = element.getAttribute("ws"); //$NON-NLS-1$
			if (osname != null && (elementOs == null || elementOs.contains(osname))) {
				matchingElements.add(element);
			} else if (wsname != null && wsname.equalsIgnoreCase(elementWs)) {
				matchingElements.add(element);
			}
		}
		return matchingElements.toArray(new IConfigurationElement[matchingElements.size()]);
	}

	/**
	 * Themes declare their appearance with the {@code isDarkTheme} attribute. Themes
	 * contributed before that attribute existed are classified by their id, which is
	 * the convention the platform relied on so far.
	 */
	private static boolean isDarkTheme(IConfigurationElement themeElement) {
		String dark = themeElement.getAttribute("isDarkTheme"); //$NON-NLS-1$
		if (dark != null) {
			return Boolean.parseBoolean(dark);
		}
		String id = themeElement.getAttribute("id"); //$NON-NLS-1$
		return id != null && id.contains("dark"); //$NON-NLS-1$
	}

	private static boolean isOsVersionMatch(String osVersionList) {
		String osVersion = System.getProperty("os.version"); //$NON-NLS-1$
		if (osVersion == null) {
			return false;
		}
		for (String osVersionFromTheme : osVersionList.split(",")) { //$NON-NLS-1$
			if (osVersion.contains(osVersionFromTheme)) {
				return true;
			}
		}
		return false;
	}
}
