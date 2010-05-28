/*******************************************************************************
 * Copyright (c) 2008, 2009 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		// Border
		if ("border".equals(property))
			applyCSSPropertyBorder(element, value, pseudo, engine);
		// Border bottom
		else if ("border-bottom".equals(property))
			applyCSSPropertyBorderBottom(element, value, pseudo, engine);
		else if ("border-bottom-color".equals(property))
			applyCSSPropertyBorderBottomColor(element, value, pseudo, engine);
		else if ("border-bottom-style".equals(property))
			applyCSSPropertyBorderBottomStyle(element, value, pseudo, engine);
		else if ("border-bottom-width".equals(property))
			applyCSSPropertyBorderBottomWidth(element, value, pseudo, engine);
		// Border color
		else if ("border-color".equals(property))
			applyCSSPropertyBorderColor(element, value, pseudo, engine);
		// Border left
		else if ("border-left".equals(property))
			applyCSSPropertyBorderLeft(element, value, pseudo, engine);
		else if ("border-left-color".equals(property))
			applyCSSPropertyBorderLeftColor(element, value, pseudo, engine);
		else if ("border-left-style".equals(property))
			applyCSSPropertyBorderLeftStyle(element, value, pseudo, engine);
		else if ("border-left-width".equals(property))
			applyCSSPropertyBorderLeftWidth(element, value, pseudo, engine);
		// Border right
		else if ("border-right".equals(property))
			applyCSSPropertyBorderRight(element, value, pseudo, engine);
		else if ("border-right-color".equals(property))
			applyCSSPropertyBorderRightColor(element, value, pseudo, engine);
		else if ("border-right-style".equals(property))
			applyCSSPropertyBorderRightStyle(element, value, pseudo, engine);
		else if ("border-right-width".equals(property))
			applyCSSPropertyBorderRightWidth(element, value, pseudo, engine);
		// Border style
		else if ("border-style".equals(property))
			applyCSSPropertyBorderStyle(element, value, pseudo, engine);
		// Border top
		else if ("border-top".equals(property))
			applyCSSPropertyBorderTop(element, value, pseudo, engine);
		else if ("border-top-color".equals(property))
			applyCSSPropertyBorderTopColor(element, value, pseudo, engine);
		else if ("border-top-style".equals(property))
			applyCSSPropertyBorderTopStyle(element, value, pseudo, engine);
		else if ("border-top-width".equals(property))
			applyCSSPropertyBorderTopWidth(element, value, pseudo, engine);
		// Border width
		else if ("border-width".equals(property))
			applyCSSPropertyBorderWidth(element, value, pseudo, engine);
		return false;
	}

	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// Border
		if ("border".equals(property))
			return retrieveCSSPropertyBorder(element, pseudo, engine);
		// Border bottom
		if ("border-bottom".equals(property))
			return retrieveCSSPropertyBorderBottom(element, pseudo, engine);
		if ("border-bottom-color".equals(property))
			return retrieveCSSPropertyBorderBottomColor(element, pseudo, engine);
		if ("border-bottom-style".equals(property))
			return retrieveCSSPropertyBorderBottomStyle(element, pseudo, engine);
		if ("border-bottom-width".equals(property))
			return retrieveCSSPropertyBorderBottomWidth(element, pseudo, engine);
		// Border color
		if ("border-color".equals(property))
			return retrieveCSSPropertyBorderColor(element, pseudo, engine);
		// Border left
		if ("border-left".equals(property))
			return retrieveCSSPropertyBorderLeft(element, pseudo, engine);
		if ("border-left-color".equals(property))
			return retrieveCSSPropertyBorderLeftColor(element, pseudo, engine);
		if ("border-left-style".equals(property))
			return retrieveCSSPropertyBorderLeftStyle(element, pseudo, engine);
		if ("border-left-width".equals(property))
			return retrieveCSSPropertyBorderLeftWidth(element, pseudo, engine);
		// Border right
		if ("border-right".equals(property))
			return retrieveCSSPropertyBorderRight(element, pseudo, engine);
		if ("border-right-color".equals(property))
			return retrieveCSSPropertyBorderRightColor(element, pseudo, engine);
		if ("border-right-style".equals(property))
			return retrieveCSSPropertyBorderRightStyle(element, pseudo, engine);
		if ("border-right-width".equals(property))
			return retrieveCSSPropertyBorderRightWidth(element, pseudo, engine);
		// Border style
		if ("border-style".equals(property))
			return retrieveCSSPropertyBorderStyle(element, pseudo, engine);
		// Border top
		if ("border-top".equals(property))
			return retrieveCSSPropertyBorderTop(element, pseudo, engine);
		if ("border-top-color".equals(property))
			return retrieveCSSPropertyBorderTopColor(element, pseudo, engine);
		if ("border-top-style".equals(property))
			return retrieveCSSPropertyBorderTopStyle(element, pseudo, engine);
		if ("border-top-width".equals(property))
			return retrieveCSSPropertyBorderTopWidth(element, pseudo, engine);
		// Border width
		if ("border-width".equals(property))
			return retrieveCSSPropertyBorderWidth(element, pseudo, engine);
		return null;
	}

	public void applyCSSPropertyBorder(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		super.applyCSSPropertyComposite(element, "border", value, pseudo,
				engine);
	}

	public void applyCSSPropertyBorderBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom");
	}

	public void applyCSSPropertyBorderBottomColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-color");
	}

	public void applyCSSPropertyBorderBottomStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-style");
	}

	public void applyCSSPropertyBorderBottomWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-bottom-width");
	}

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

	public void applyCSSPropertyBorderLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left");
	}

	public void applyCSSPropertyBorderLeftColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-color");
	}

	public void applyCSSPropertyBorderLeftStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-style");
	}

	public void applyCSSPropertyBorderLeftWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-left-width");
	}

	public void applyCSSPropertyBorderRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right");
	}

	public void applyCSSPropertyBorderRightColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-color");
	}

	public void applyCSSPropertyBorderRightStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-style");
	}

	public void applyCSSPropertyBorderRightWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-right-width");
	}

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

	public void applyCSSPropertyBorderTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top");

	}

	public void applyCSSPropertyBorderTopColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-color");
	}

	public void applyCSSPropertyBorderTopStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-style");

	}

	public void applyCSSPropertyBorderTopWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		throw new UnsupportedPropertyException("border-top-width");
	}

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

	public String retrieveCSSPropertyBorder(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderBottomColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderBottomStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderBottomWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderColor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderLeftColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderLeftStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderLeftWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderRight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderRightColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderRightStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderRightWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderTop(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderTopColor(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderTopStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderTopWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	public String retrieveCSSPropertyBorderWidth(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return "0";
	}

}
