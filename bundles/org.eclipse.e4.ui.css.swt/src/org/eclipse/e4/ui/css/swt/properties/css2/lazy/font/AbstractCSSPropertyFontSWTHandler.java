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
package org.eclipse.e4.ui.css.swt.properties.css2.lazy.font;

import org.eclipse.e4.ui.css.core.css2.CSS2FontPropertiesHelpers;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2Delegate;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyFontSWTHandler extends
		AbstractCSSPropertySWTHandler implements ICSSPropertyHandler2Delegate {

	public void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		CSS2FontProperties font = CSSSWTFontHelper.getCSS2FontProperties(
				control, engine.getCSSElementContext(control));
		this.applyCSSProperty(font, property, value, pseudo, engine);
	}

	public ICSSPropertyHandler2 getCSSPropertyHandler2() {
		return CSSPropertyFontSWTHandler2.INSTANCE;
	}

	public void applyCSSProperty(CSS2FontProperties font, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		CSS2FontPropertiesHelpers.updateCSSPropertyFont(font, property, value);
	}

	public abstract String retrieveCSSProperty(Control control,
			String property, String pseudo, CSSEngine engine) throws Exception;

}
