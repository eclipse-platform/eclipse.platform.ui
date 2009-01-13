/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.engine;

import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBackgroundHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyBorderHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyClassificationHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyTextHandler;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyBackgroundSWTHandler;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyBorderSWTHandler;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyClassificationSWTHandler;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyFontSWTHandler;
import org.eclipse.e4.ui.css.swt.properties.css2.CSSPropertyTextSWTHandler;
import org.eclipse.e4.ui.css.xml.properties.css2.CSSPropertyBackgroundXMLHandler;
import org.eclipse.e4.ui.css.xml.properties.css2.CSSPropertyFontXMLHandler;
import org.eclipse.e4.ui.css.xml.properties.css2.CSSPropertyTextXMLHandler;
import org.eclipse.swt.widgets.Display;

/**
 * CSS SWT Engine implementation which configure CSSEngineImpl to apply styles
 * to SWT widgets with static handler strategy.
 */
public class CSSSWTEngineImpl extends AbstractCSSSWTEngineImpl {

	public CSSSWTEngineImpl(Display display) {
		super(display);
	}

	public CSSSWTEngineImpl(Display display, boolean lazyApplyingStyles) {
		super(display, lazyApplyingStyles);
	}

	protected void initializeCSSPropertyHandlers() {
		// Register SWT CSS Property Background Handler
		super.registerCSSPropertyHandler(ICSSPropertyBackgroundHandler.class,
				CSSPropertyBackgroundSWTHandler.INSTANCE);
		// Register SWT CSS Property Border Handler
		super.registerCSSPropertyHandler(ICSSPropertyBorderHandler.class,
				CSSPropertyBorderSWTHandler.INSTANCE);
		// Register SWT CSS Property Classification Handler
		super.registerCSSPropertyHandler(
				ICSSPropertyClassificationHandler.class,
				CSSPropertyClassificationSWTHandler.INSTANCE);
		// Register SWT CSS Property Text Handler
		super.registerCSSPropertyHandler(ICSSPropertyTextHandler.class,
				CSSPropertyTextSWTHandler.INSTANCE);
		// Register SWT CSS Property Font Handler
		super.registerCSSPropertyHandler(ICSSPropertyFontHandler.class,
				CSSPropertyFontSWTHandler.INSTANCE);

		// Register XML CSS Property Background Handler
		super.registerCSSPropertyHandler(ICSSPropertyBackgroundHandler.class,
				CSSPropertyBackgroundXMLHandler.INSTANCE);
		// Register XML CSS Property Text Handler
		super.registerCSSPropertyHandler(ICSSPropertyTextHandler.class,
				CSSPropertyTextXMLHandler.INSTANCE);
		// Register XML CSS Property Font Handler
		super.registerCSSPropertyHandler(ICSSPropertyFontHandler.class,
				CSSPropertyFontXMLHandler.INSTANCE);
	}

}
