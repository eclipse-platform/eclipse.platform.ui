/*******************************************************************************
 * Copyright (c) 2010, 2012 Tom Schindl and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.internal.theme;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.dom.WidgetElement;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.swt.widgets.Display;

public class ThemeEngineManager implements IThemeManager {
	private static final String KEY = "org.eclipse.e4.ui.css.swt.theme";

	@Override
	public IThemeEngine getEngineForDisplay(Display display) {
		IThemeEngine engine = (IThemeEngine) display.getData(KEY);

		if( engine == null ) {
			engine = new ThemeEngine(display);
			engine.addCSSEngine(getCSSSWTEngine(display));
			display.setData(KEY, engine);
		}
		return engine;
	}

	private CSSEngine getCSSSWTEngine(Display display) {
		CSSEngine cssEngine = WidgetElement.getEngine(display);
		if (cssEngine != null) {
			return cssEngine;
		}
		cssEngine = new CSSSWTEngineImpl(display, true);
		cssEngine.setErrorHandler(new CSSErrorHandler() {
			@Override
			public void error(Exception e) {
				// TODO Use the logger
				e.printStackTrace();
			}
		});
		WidgetElement.setEngine(display, cssEngine);
		return cssEngine;
	}
}