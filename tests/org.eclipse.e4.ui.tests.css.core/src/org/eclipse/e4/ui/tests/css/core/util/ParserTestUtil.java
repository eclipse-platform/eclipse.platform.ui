/*******************************************************************************
 * Copyright (c) 2009 EclipseSource and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   EclipseSource - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.css.core.util;

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.engine.CSSErrorHandler;
import org.eclipse.e4.ui.css.swt.engine.CSSSWTEngineImpl;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.stylesheets.StyleSheet;


public final class ParserTestUtil {

	private static final CSSErrorHandler ERROR_HANDLER = e -> {
		throw new RuntimeException(e);
	};

	private ParserTestUtil() {
		// prevent instantiation
	}

	public static CSSStyleSheet parseCss(String css)
			throws IOException {
		CSSEngine engine = createEngine();
		StyleSheet result = engine.parseStyleSheet(new StringReader(css));
		return (CSSStyleSheet) result;
	}

	public static CSSEngine createEngine() {
		Display display = Display.getDefault();
		CSSEngine engine = new CSSSWTEngineImpl(display);
		engine.setErrorHandler(ERROR_HANDLER);
		return engine;
	}
}
