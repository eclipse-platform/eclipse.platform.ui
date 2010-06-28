/*******************************************************************************
 * Copyright (c) 2010 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.theme;

import java.util.List;

import org.eclipse.e4.ui.css.core.util.resources.IResourceLocator;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 * The theme engine collects available themes (who are composed of stylesheets)
 * and provides the possibility to change the theme
 */
public interface IThemeEngine {
	public static final String DEFAULT_THEME_ID = "org.eclipse.e4.ui.workbench.swt.theme.default";

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
	 */
	public ITheme registerTheme(String id, String label,
			String basestylesheetURI);

	/**
	 * Register a stylesheet
	 * 
	 * @param uri
	 *            the stylsheet uri
	 * @param themes
	 *            the theme ids the stylesheet is added to or empty if should be
	 *            added to all
	 */
	public void registerStylesheet(String uri, String... themes);

	/**
	 * Register a resource locator used to look up image sources
	 * 
	 * @param locator
	 *            the locator
	 * @param themes
	 *            the theme ids the locator is registered for or empty if should
	 *            be added to all
	 */
	public void registerResourceLocator(IResourceLocator locator,
			String... themes);

	/**
	 * @return Unmodifiable list of themes
	 */
	public List<ITheme> getThemes();

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
	public void setTheme(String themeId, boolean restore);

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
	public void setTheme(ITheme theme, boolean restore);

	/**
	 * Force reapplying the style to the widget and its children
	 * 
	 * @param widget
	 *            the widget
	 * @param applyStylesToChildNodes
	 *            if the children should be updated as well
	 */
	public void applyStyles(Widget widget, boolean applyStylesToChildNodes);

	/**
	 * Get the style currently active for a widget
	 * 
	 * @param widget
	 *            the widget
	 * @return the declaration or <code>null</code>
	 */
	public CSSStyleDeclaration getStyle(Widget widget);

	/**
	 * Restore the previously stored theme
	 * 
	 * @param alternate
	 *            the alternate theme if the restored one is not found
	 */
	public void restore(String alternate);

	/**
	 * @return the current active theme
	 */
	public ITheme getActiveTheme();
}
