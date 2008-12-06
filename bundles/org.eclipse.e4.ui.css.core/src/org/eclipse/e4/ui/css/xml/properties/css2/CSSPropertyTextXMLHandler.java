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
package org.eclipse.e4.ui.css.xml.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyTextHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * 
 */
public class CSSPropertyTextXMLHandler extends AbstractCSSPropertyTextHandler {

	public final static ICSSPropertyTextHandler INSTANCE = new CSSPropertyTextXMLHandler();

	public boolean applyCSSProperty(Object node, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (node instanceof Element) {
			super.applyCSSProperty((Element) node, property, value, pseudo,
					engine);
			return true;
		}
		return false;
	}

	public void applyCSSPropertyColor(Object node, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			// Add color attribute
			Element element = (Element) node;
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			element.setAttribute("color", primitiveValue.getStringValue());
		}
	}

	public String retrieveCSSPropertyColor(Object node, CSSEngine engine)
			throws Exception {
		Element element = (Element) node;
		return element.getAttribute("color");
	}

}
