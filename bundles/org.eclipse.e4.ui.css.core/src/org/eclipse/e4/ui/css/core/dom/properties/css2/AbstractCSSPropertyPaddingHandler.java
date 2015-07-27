/*******************************************************************************
 *  Copyright (c) 2009, 2014 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

public abstract class AbstractCSSPropertyPaddingHandler implements ICSSPropertyPaddingHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if ("padding".equals(property))
			applyCSSPropertyPadding(element, value, pseudo, engine);
		else if ("padding-top".equals(property))
			applyCSSPropertyPaddingTop(element, value, pseudo, engine);
		else if ("padding-right".equals(property))
			applyCSSPropertyPaddingRight(element, value, pseudo, engine);
		else if ("padding-bottom".equals(property))
			applyCSSPropertyPaddingBottom(element, value, pseudo, engine);
		else if ("padding-left".equals(property))
			applyCSSPropertyPaddingLeft(element, value, pseudo, engine);
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if ("padding".equals(property)) {
			return retrieveCSSPropertyPadding(element, pseudo, engine);
		}
		if ("padding-top".equals(property)) {
			return retrieveCSSPropertyPaddingTop(element, pseudo, engine);
		}
		if ("padding-right".equals(property)) {
			return retrieveCSSPropertyPaddingRight(element, pseudo, engine);
		}
		if ("padding-bottom".equals(property)) {
			return retrieveCSSPropertyPaddingBottom(element, pseudo, engine);
		}
		if ("padding-left".equals(property)) {
			return retrieveCSSPropertyPaddingLeft(element, pseudo, engine);
		}
		return null;
	}
}
