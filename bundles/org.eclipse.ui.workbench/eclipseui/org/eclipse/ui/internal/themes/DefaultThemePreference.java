/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.themes;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.UserScope;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The theme used for new workspaces and for workspaces without an explicit
 * theme. It is recorded in the user scope, so every installation of this
 * product sees it, together with the theme's appearance. Readers that run
 * before the workbench, like the workspace selection dialog, rely on that
 * appearance.
 */
public final class DefaultThemePreference {

	private static final String THEME_PLUGIN_ID = "org.eclipse.e4.ui.css.swt.theme"; //$NON-NLS-1$

	private static final String THEMEID_KEY = "themeid"; //$NON-NLS-1$

	/**
	 * Prefix of the key holding a theme's appearance. The theme id is part of the
	 * key, so a record left behind by another installation cannot be mistaken for
	 * the appearance of the theme in {@link #THEMEID_KEY}.
	 */
	private static final String THEME_IS_DARK_KEY_PREFIX = "themeIsDark."; //$NON-NLS-1$

	private DefaultThemePreference() {
	}

	/** @return the id of the default theme, or {@code null} if none is set */
	public static String getThemeId() {
		return getNode().get(THEMEID_KEY, null);
	}

	public static void set(ITheme theme) {
		IEclipsePreferences node = getNode();
		node.put(THEMEID_KEY, theme.getId());
		node.putBoolean(THEME_IS_DARK_KEY_PREFIX + theme.getId(), theme.isDark());
		flush(node, "Failed to set default theme in user scope"); //$NON-NLS-1$
	}

	public static void remove(String themeId) {
		IEclipsePreferences node = getNode();
		node.remove(THEMEID_KEY);
		node.remove(THEME_IS_DARK_KEY_PREFIX + themeId);
		flush(node, "Failed to remove default theme from user scope"); //$NON-NLS-1$
	}

	private static IEclipsePreferences getNode() {
		IEclipsePreferences baseNode = UserScope.INSTANCE.getNode(THEME_PLUGIN_ID);
		String productOrAppId = getProductOrApplicationId();
		return productOrAppId != null ? (IEclipsePreferences) baseNode.node(productOrAppId) : baseNode;
	}

	/**
	 * Returns the product ID if a product is configured, otherwise the application
	 * ID from the system property, or {@code null} if neither is available.
	 */
	private static String getProductOrApplicationId() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			return product.getId();
		}
		return System.getProperty("eclipse.application"); //$NON-NLS-1$
	}

	private static void flush(IEclipsePreferences node, String errorMessage) {
		try {
			node.flush();
		} catch (BackingStoreException e) {
			WorkbenchPlugin.log(errorMessage, e);
		}
	}
}
