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

public interface IThemeEngine {
	public ITheme registerTheme(String id, String label, String basestylesheetURI);

	public void registerStylsheet(String uri, String... themes);

	public void registerResourceLocator(IResourceLocator locator,
			String... themes);

	public List<ITheme> getThemes();
	public void setTheme(String themeId);
	public void setTheme(ITheme theme);

	public void applyStyles(Widget widget, boolean applyStylesToChildNodes);
}
