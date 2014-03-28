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
		if ("color".equals(property)) {
			applyCSSPropertyColor(element, value, pseudo, engine);
		}
		if ("text-transform".equals(property)) {
			applyCSSPropertyTextTransform(element, value, pseudo, engine);
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if ("color".equals(property)) {
			return retrieveCSSPropertyColor(element, pseudo, engine);
		}
		if ("text-transform".equals(property)) {
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
			String textTransform = primitiveValue.getStringValue();
			if ("capitalize".equals(textTransform)) {
				return StringUtils.capitalize(text);
			}
			if ("uppercase".equals(textTransform)) {
				if (text != null)
					return text.toUpperCase();
			}
			if ("lowercase".equals(textTransform)) {
				if (text != null)
					return text.toLowerCase();
			}
			if ("inherit".equals(textTransform)) {
				return text;
			}
			// TODO : manage inherit
		}
		if (defaultText != null)
			return defaultText;
		return text;
	}

	protected String getTextTransform(String textToInsert, String oldText,
			CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			String textTransform = primitiveValue.getStringValue();
			if ("capitalize".equals(textTransform)) {
				String newText = StringUtils.capitalize(oldText + textToInsert);
				if (newText.length() > 0) {
					return newText.substring(newText.length() - 1);
				}
			}
			if ("uppercase".equals(textTransform)) {
				if (textToInsert != null)
					return textToInsert.toUpperCase();
			}
			if ("lowercase".equals(textTransform)) {
				if (textToInsert != null)
					return textToInsert.toLowerCase();
			}
			if ("inherit".equals(textTransform)) {
				return textToInsert;
			}
			// TODO : manage inherit
		}
		return textToInsert;
	}

	protected boolean hasTextTransform(CSSValue value) {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			String textTransform = primitiveValue.getStringValue();
			if ("capitalize".equals(textTransform))
				return true;
			if ("uppercase".equals(textTransform))
				return true;
			if ("lowercase".equals(textTransform))
				return true;
		}
		return false;
	}

}
