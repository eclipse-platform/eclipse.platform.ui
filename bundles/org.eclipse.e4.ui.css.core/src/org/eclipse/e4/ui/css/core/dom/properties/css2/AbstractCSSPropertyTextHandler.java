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
import org.eclipse.e4.ui.css.core.utils.StringUtils;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyTextHandler implements
ICSSPropertyTextHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return false;
		}

		switch (property) {
		case "color":
			applyCSSPropertyColor(element, value, pseudo, engine);
			break;
		case "text-transform":
			applyCSSPropertyTextTransform(element, value, pseudo, engine);
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
		case "color":
			return retrieveCSSPropertyColor(element, pseudo, engine);
		case "text-transform":
			return retrieveCSSPropertyTextTransform(element, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("color");
	}

	@Override
	public void applyCSSPropertyTextTransform(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("text-transform");
	}

	@Override
	public String retrieveCSSPropertyColor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyTextTransform(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	protected String getTextTransform(String text, CSSValue value,
			String defaultText) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			switch (primitiveValue.getStringValue()) {
			case "capitalize":
				return StringUtils.capitalize(text);
			case "uppercase":
				if (text != null) {
					return text.toUpperCase();
				}
				break;
			case "lowercase":
				if (text != null) {
					return text.toLowerCase();
				}
				break;
			case "inherit":
				return text;
			default:
				// TODO : manage inherit
			}
		}
		if (defaultText != null) {
			return defaultText;
		}
		return text;
	}

	protected String getTextTransform(String textToInsert, String oldText,
			CSSValue value) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return textToInsert;
		}

		switch (((CSSPrimitiveValue) value).getStringValue()) {
		case "capitalize":
			String newText = StringUtils.capitalize(oldText + textToInsert);
			if (newText.length() > 0) {
				return newText.substring(newText.length() - 1);
			}
			return textToInsert;
		case "uppercase":
			if (textToInsert != null) {
				return textToInsert.toUpperCase();
			}
			return textToInsert;
		case "lowercase":
			if (textToInsert != null) {
				return textToInsert.toLowerCase();
			}
			return textToInsert;
		case "inherit":
		default:
			// TODO : manage inherit
			return textToInsert;
		}
	}

	protected boolean hasTextTransform(CSSValue value) {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE
				|| ((CSSPrimitiveValue) value).getStringValue() == null) {
			return false;
		}

		String textTransform = ((CSSPrimitiveValue) value).getStringValue();
		switch (textTransform) {
		case "capitalize":
		case "uppercase":
		case "lowercase":
			return true;
		}

		return false;
	}

}
