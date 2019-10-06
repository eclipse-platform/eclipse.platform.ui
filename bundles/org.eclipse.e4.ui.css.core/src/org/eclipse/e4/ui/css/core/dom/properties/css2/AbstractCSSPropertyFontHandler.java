/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedPropertyException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyFontHandler extends
AbstractCSSPropertyFontCompositeHandler implements
ICSSPropertyFontHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return false;
		}

		switch (property) {
		case "font":
			applyCSSPropertyFont(element, value, pseudo, engine);
			break;
		case "font-family":
			applyCSSPropertyFontFamily(element, value, pseudo, engine);
			break;
		case "font-size":
			applyCSSPropertyFontSize(element, value, pseudo, engine);
			break;
		case "font-adjust":
			applyCSSPropertyFontSizeAdjust(element, value, pseudo, engine);
			break;
		case "font-stretch":
			applyCSSPropertyFontStretch(element, value, pseudo, engine);
			break;
		case "font-style":
			applyCSSPropertyFontStyle(element, value, pseudo, engine);
			break;
		case "font-variant":
			applyCSSPropertyFontVariant(element, value, pseudo, engine);
			break;
		case "font-weight":
			applyCSSPropertyFontWeight(element, value, pseudo, engine);
			break;
		default:
			return false;
		}
		return true;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return null;
		}

		switch (property) {
		case "font-family":
			return retrieveCSSPropertyFontFamily(element, pseudo, engine);
		case "font-size":
			return retrieveCSSPropertyFontSize(element, pseudo, engine);
		case "font-adjust":
			return retrieveCSSPropertyFontAdjust(element, pseudo, engine);
		case "font-stretch":
			return retrieveCSSPropertyFontStretch(element, pseudo, engine);
		case "font-style":
			return retrieveCSSPropertyFontStyle(element, pseudo, engine);
		case "font-variant":
			return retrieveCSSPropertyFontVariant(element, pseudo, engine);
		case "font-weight":
			return retrieveCSSPropertyFontWeight(element, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyFont(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		super.applyCSSPropertyComposite(element, "font", value, pseudo, engine);
	}

	@Override
	public void applyCSSPropertyFontFamily(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSS2FontProperties) {
			applyCSSPropertyFontFamily((CSS2FontProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-family");
	}

	protected void applyCSSPropertyFontFamily(CSS2FontProperties font,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setFamily((CSSPrimitiveValue) value);
		}
	}

	@Override
	public void applyCSSPropertyFontSize(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSS2FontProperties) {
			applyCSSPropertyFontSize((CSS2FontProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-size");
	}

	protected void applyCSSPropertyFontSize(CSS2FontProperties font,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setSize((CSSPrimitiveValue) value);
		}
	}

	@Override
	public void applyCSSPropertyFontSizeAdjust(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("font-adjust");
	}

	@Override
	public void applyCSSPropertyFontStretch(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("font-stretch");
	}

	@Override
	public void applyCSSPropertyFontStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSS2FontProperties) {
			applyCSSPropertyFontStyle((CSS2FontProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-style");
	}

	protected void applyCSSPropertyFontStyle(CSS2FontProperties font,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setStyle((CSSPrimitiveValue) value);
		}
	}

	@Override
	public void applyCSSPropertyFontVariant(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("font-variant");
	}

	@Override
	public void applyCSSPropertyFontWeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSS2FontProperties) {
			applyCSSPropertyFontWeight((CSS2FontProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("font-weight");
	}

	protected void applyCSSPropertyFontWeight(CSS2FontProperties font,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			font.setWeight((CSSPrimitiveValue) value);
		}
	}

}
