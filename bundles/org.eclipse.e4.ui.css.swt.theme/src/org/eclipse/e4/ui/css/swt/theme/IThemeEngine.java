/*******************************************************************************
 * Copyright (c) 2010, 2015 Tom Schindl and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Brian de Alwis <bsd@mt.ca> - theme-change event API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.theme;

import java.util.List;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * The theme engine collects available themes (who are composed of stylesheets)
 * and provides the possibility to change the theme
 */
public interface IThemeEngine {
	String DEFAULT_THEME_ID = "org.eclipse.e4.ui.workbench.swt.theme.default";

	/**
	 * The IThemeEngine may broadcast an event using the OSGi EventAdmin
	 * service, if available, to notify of theme changes. The event will contain
	 * several attributes to provide the context of the event.
	 *
	 * <p>
	 * NB: this event topic and attribute list may change and should not yet be
	 * considered as API.
	 * </p>
	 */
	public static interface Events {
		String TOPIC = "org/eclipse/e4/ui/css/swt/theme/ThemeManager";
		String THEME_CHANGED = TOPIC + "/themeChanged";

		// attributes that can be tested in event handlers

		/**
		 * Attribute for the new theme
		 *
		 * @see ITheme
		 */
		String THEME = "theme";

		/**
		 * Attribute for the affected rendering device (e.g., an SWT
		 * {@link Display}). May be null
		 *
		 * @see org.eclipse.swt.graphics.Device
		 * @see org.eclipse.swt.widgets.Display
		 */
		String DEVICE = "device";

		/**
		 * Attribute for the associated {@link IThemeEngine} theme engine
		 *
		 * @see IThemeEngine
		 */
		String THEME_ENGINE = "themeEngine";

		/**
		 * Attribute describing the theme change's persist state. If true, then
		 * the theme will be restored on subsequent startups.
		 *
		 * @see Boolean
		 */
		String RESTORE = "restore";
	}

	/**
	 * Register a theme
	 *
	 * @param id
	 *            the id of the theme
	 * @param label
	 *            the label
	 * @param basestylesheetURI
	 *            the base stylesheet uri
	 *
	 * @return the theme instance registered
	 * @throws IllegalArgumentException
	 *             if a theme with this id is already registered
	 */
	ITheme registerTheme(String id, String label,
			String basestylesheetURI) throws IllegalArgumentException;

	/**
	 * Register a stylesheet
	 *
	 * @param uri
	 *            the stylsheet uri
	 * @param themes
	 *            the theme ids the stylesheet is added to or empty if should be
	 *            added to all
	 */
	void registerStylesheet(String uri, String... themes);

	/**
	 * Register a resource locator used to look up image sources
	 *
	 * @param locator
	 *            the locator
	 * @param themes
	 *            the theme ids the locator is registered for or empty if should
	 *            be added to all
	 */
	void registerResourceLocator(IResourceLocator locator,
			String... themes);

	/**
	 * @return Unmodifiable list of themes
	 */
	List<ITheme> getThemes();

	/**
	 * Set a theme by its id and restore it for the next time the engine is
	 * initialized ({@link #restore(String)})
	 *
	 * @param themeId
	 *            the theme id
	 * @param restore
	 *            restore the theme set for the next time
	 *            {@link #restore(String)}
	 */
	void setTheme(String themeId, boolean restore);

	/**
	 * Set a theme and restore it for the next time the engine is initialized (
	 * {@link #restore(String)})
	 *
	 * @param theme
	 *            the theme
	 * @param restore
	 *            restore the theme set for the next time
	 *            {@link #restore(String)}
	 */
	void setTheme(ITheme theme, boolean restore);

	/**
	 * Force reapplying the style to the widget and its children
	 *
	 * @param widget
	 *            the widget
	 * @param applyStylesToChildNodes
	 *            if the children should be updated as well
	 */
	void applyStyles(Object widget, boolean applyStylesToChildNodes);

	/**
	 * Get the style currently active for a widget
	 *
	 * @param widget
	 *            the widget
	 * @return the declaration or <code>null</code>
	 */
	CSSStyleDeclaration getStyle(Object widget);

	/**
	 * Restore the previously stored theme
	 *
	 * @param alternate
	 *            the alternate theme if the restored one is not found
	 */
	void restore(String alternate);

	/**
	 * @return the current active theme or <code>null</code> if no active theme
	 */
	ITheme getActiveTheme();

	void addCSSEngine(CSSEngine cssswtEngine);

	void removeCSSEngine(CSSEngine cssswtEngine);
}
