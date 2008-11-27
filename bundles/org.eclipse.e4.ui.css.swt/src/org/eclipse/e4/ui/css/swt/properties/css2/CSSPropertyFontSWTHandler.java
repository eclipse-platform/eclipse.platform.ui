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
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyFontSWTHandler extends AbstractCSSPropertyFontHandler
		implements ICSSPropertyHandler2 {

	public final static ICSSPropertyFontHandler INSTANCE = new CSSPropertyFontSWTHandler();

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			CSS2FontProperties fontProperties = CSSSWTFontHelper
					.getCSS2FontProperties(control, engine
							.getCSSElementContext(control));
			super.applyCSSProperty(fontProperties, property, value, pseudo,
					engine);
			return true;
		} else {
			if (element instanceof CSS2FontProperties) {
				super
						.applyCSSProperty(element, property, value, pseudo,
								engine);
				return true;
			}
		}
		return false;

	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			return super.retrieveCSSProperty(control, property, pseudo, engine);
		}
		return null;
	}

	public String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		return CSSSWTFontHelper.getFontFamily(control);
	}

	public String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		return CSSSWTFontHelper.getFontSize(control);
	}

	public String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		return CSSSWTFontHelper.getFontStyle(control);

	}

	public String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		return CSSSWTFontHelper.getFontWeight(control);
	}

	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		final Control control = SWTElementHelpers.getControl(element);
		if (control == null)
			return;
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(control, engine
						.getCSSElementContext(control));
		if (fontProperties == null)
			return;
		Font font = (Font) engine.convert(fontProperties, Font.class, control);
		control.setFont(font);
	}
}
