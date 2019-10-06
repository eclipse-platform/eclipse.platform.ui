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
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.exceptions.UnsupportedPropertyException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract CSS property background which is enable to manage
 * apply CSS Property border, border-color, border-style...
 */
public abstract class AbstractCSSPropertyBorderHandler extends
AbstractCSSPropertyBorderCompositeHandler implements
ICSSPropertyBorderHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {

		if (property == null) {
			return false;
		}

		switch (property) {
		case "border":
			applyCSSPropertyBorder(element, value, pseudo, engine);
			break;
		case "border-bottom":
			applyCSSPropertyBorderBottom(element, value, pseudo, engine);
			break;
		case "border-bottom-color":
			applyCSSPropertyBorderBottomColor(element, value, pseudo, engine);
			break;
		case "border-bottom-style":
			applyCSSPropertyBorderBottomStyle(element, value, pseudo, engine);
			break;
		case "border-bottom-width":
			applyCSSPropertyBorderBottomWidth(element, value, pseudo, engine);
			break;
		case "border-color":
			applyCSSPropertyBorderColor(element, value, pseudo, engine);
			break;
		case "border-left":
			applyCSSPropertyBorderLeft(element, value, pseudo, engine);
			break;
		case "border-left-color":
			applyCSSPropertyBorderLeftColor(element, value, pseudo, engine);
			break;
		case "border-left-style":
			applyCSSPropertyBorderLeftStyle(element, value, pseudo, engine);
			break;
		case "border-left-width":
			applyCSSPropertyBorderLeftWidth(element, value, pseudo, engine);
			break;
		case "border-right":
			applyCSSPropertyBorderRight(element, value, pseudo, engine);
			break;
		case "border-right-color":
			applyCSSPropertyBorderRightColor(element, value, pseudo, engine);
			break;
		case "border-right-style":
			applyCSSPropertyBorderRightStyle(element, value, pseudo, engine);
			break;
		case "border-right-width":
			applyCSSPropertyBorderRightWidth(element, value, pseudo, engine);
			break;
		case "border-style":
			applyCSSPropertyBorderStyle(element, value, pseudo, engine);
			break;
		case "border-top":
			applyCSSPropertyBorderTop(element, value, pseudo, engine);
			break;
		case "border-top-color":
			applyCSSPropertyBorderTopColor(element, value, pseudo, engine);
			break;
		case "border-top-style":
			applyCSSPropertyBorderTopStyle(element, value, pseudo, engine);
			break;
		case "border-top-width":
			applyCSSPropertyBorderTopWidth(element, value, pseudo, engine);
			break;
		case "border-width":
			applyCSSPropertyBorderWidth(element, value, pseudo, engine);
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
		case "border":
			return retrieveCSSPropertyBorder(element, pseudo, engine);
		case "border-bottom":
			return retrieveCSSPropertyBorderBottom(element, pseudo, engine);
		case "border-bottom-color":
			return retrieveCSSPropertyBorderBottomColor(element, pseudo, engine);
		case "border-bottom-style":
			return retrieveCSSPropertyBorderBottomStyle(element, pseudo, engine);
		case "border-bottom-width":
			return retrieveCSSPropertyBorderBottomWidth(element, pseudo, engine);
		case "border-color":
			return retrieveCSSPropertyBorderColor(element, pseudo, engine);
		case "border-left":
			return retrieveCSSPropertyBorderLeft(element, pseudo, engine);
		case "border-left-color":
			return retrieveCSSPropertyBorderLeftColor(element, pseudo, engine);
		case "border-left-style":
			return retrieveCSSPropertyBorderLeftStyle(element, pseudo, engine);
		case "border-left-width":
			return retrieveCSSPropertyBorderLeftWidth(element, pseudo, engine);
		case "border-right":
			return retrieveCSSPropertyBorderRight(element, pseudo, engine);
		case "border-right-color":
			return retrieveCSSPropertyBorderRightColor(element, pseudo, engine);
		case "border-right-style":
			return retrieveCSSPropertyBorderRightStyle(element, pseudo, engine);
		case "border-right-width":
			return retrieveCSSPropertyBorderRightWidth(element, pseudo, engine);
		case "border-style":
			return retrieveCSSPropertyBorderStyle(element, pseudo, engine);
		case "border-top":
			return retrieveCSSPropertyBorderTop(element, pseudo, engine);
		case "border-top-color":
			return retrieveCSSPropertyBorderTopColor(element, pseudo, engine);
		case "border-top-style":
			return retrieveCSSPropertyBorderTopStyle(element, pseudo, engine);
		case "border-top-width":
			return retrieveCSSPropertyBorderTopWidth(element, pseudo, engine);
		case "border-width":
			return retrieveCSSPropertyBorderWidth(element, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyBorder(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		super.applyCSSPropertyComposite(element, "border", value, pseudo,
				engine);
	}

	@Override
	public void applyCSSPropertyBorderBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom");
	}

	@Override
	public void applyCSSPropertyBorderBottomColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-color");
	}

	@Override
	public void applyCSSPropertyBorderBottomStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-style");
	}

	@Override
	public void applyCSSPropertyBorderBottomWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-width");
	}

	@Override
	public void applyCSSPropertyBorderColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSSBorderProperties) {
			applyCSSPropertyBorderColor((CSSBorderProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("border-color");
	}

	public void applyCSSPropertyBorderColor(CSSBorderProperties border,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			border.setColor((CSSPrimitiveValue) value);
		}
	}

	@Override
	public void applyCSSPropertyBorderLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left");
	}

	@Override
	public void applyCSSPropertyBorderLeftColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-color");
	}

	@Override
	public void applyCSSPropertyBorderLeftStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-style");
	}

	@Override
	public void applyCSSPropertyBorderLeftWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-width");
	}

	@Override
	public void applyCSSPropertyBorderRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right");
	}

	@Override
	public void applyCSSPropertyBorderRightColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-color");
	}

	@Override
	public void applyCSSPropertyBorderRightStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-style");
	}

	@Override
	public void applyCSSPropertyBorderRightWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-width");
	}

	@Override
	public void applyCSSPropertyBorderStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSSBorderProperties) {
			applyCSSPropertyBorderStyle((CSSBorderProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("border-style");
	}

	public void applyCSSPropertyBorderStyle(CSSBorderProperties border,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			border.setStyle(((CSSPrimitiveValue) value).getStringValue());
		}
	}

	@Override
	public void applyCSSPropertyBorderTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top");

	}

	@Override
	public void applyCSSPropertyBorderTopColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-color");
	}

	@Override
	public void applyCSSPropertyBorderTopStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-style");

	}

	@Override
	public void applyCSSPropertyBorderTopWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-width");
	}

	@Override
	public void applyCSSPropertyBorderWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof CSSBorderProperties) {
			applyCSSPropertyBorderWidth((CSSBorderProperties) element, value,
					pseudo, engine);
			return;
		}
		throw new UnsupportedPropertyException("border-width");
	}

	public void applyCSSPropertyBorderWidth(CSSBorderProperties border,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			border.setWidth((int) ((CSSPrimitiveValue) value)
					.getFloatValue(CSSPrimitiveValue.CSS_PT));
		}
	}

	@Override
	public String retrieveCSSPropertyBorder(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderBottomColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderBottomStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderBottomWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderColor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderLeftColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderLeftStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderLeftWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderRight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderRightColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderRightStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderRightWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderTop(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderTopColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderTopStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderTopWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyBorderWidth(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return "0";
	}

}
