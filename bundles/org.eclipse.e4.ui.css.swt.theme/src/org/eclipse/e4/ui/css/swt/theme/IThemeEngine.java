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

public interface IThemeEngine {
	public static final String DEFAULT_THEME_ID = "org.eclipse.e4.ui.workbench.swt.theme.default";
	
	public ITheme registerTheme(String id, String label, String basestylesheetURI);

	public void registerStylesheet(String uri, String... themes);

	public void registerResourceLocator(IResourceLocator locator,
			String... themes);

	public List<ITheme> getThemes();
	public void setTheme(String themeId, boolean restore);
	public void setTheme(ITheme theme, boolean restore);

	public void applyStyles(Widget widget, boolean applyStylesToChildNodes);
	
	public CSSStyleDeclaration getStyle(Widget widget);
	public void restore(String alternate);
	public ITheme getActiveTheme();
}
