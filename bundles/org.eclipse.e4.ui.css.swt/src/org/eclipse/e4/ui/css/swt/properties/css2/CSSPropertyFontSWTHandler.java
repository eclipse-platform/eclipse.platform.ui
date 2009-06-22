/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     Remy Chi Jian Suen <remy.suen@gmail.com>
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler2;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyFontSWTHandler extends AbstractCSSPropertyFontHandler
		implements ICSSPropertyHandler2 {

	public final static ICSSPropertyFontHandler INSTANCE = new CSSPropertyFontSWTHandler();
	
	private static void setFont(Widget widget, Font font) {
		if (widget instanceof CTabItem) {
			((CTabItem) widget).setFont(font);
		} else if (widget instanceof Control) {
			((Control) widget).setFont(font);
		}
	}

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			CSS2FontProperties fontProperties = CSSSWTFontHelper
					.getCSS2FontProperties(widget, engine
							.getCSSElementContext(widget));
			if (fontProperties != null) {
				super.applyCSSProperty(fontProperties, property, value, pseudo,
						engine);
			}
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
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget != null) {
			return super.retrieveCSSProperty(widget, property, pseudo, engine);
		}
		return null;
	}

	public String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontFamily(widget);
	}

	public String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontSize(widget);
	}

	public String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontStyle(widget);

	}

	public String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Widget widget = (Widget) element;
		return CSSSWTFontHelper.getFontWeight(widget);
	}

	public void onAllCSSPropertiesApplyed(Object element, CSSEngine engine)
			throws Exception {
		final Widget widget = SWTElementHelpers.getWidget(element);
		if (widget == null)
			return;
		CSS2FontProperties fontProperties = CSSSWTFontHelper
				.getCSS2FontProperties(widget, engine
						.getCSSElementContext(widget));
		if (fontProperties == null)
			return;
		Font font = (Font) engine.convert(fontProperties, Font.class, widget);
		setFont(widget, font);
	}
}
