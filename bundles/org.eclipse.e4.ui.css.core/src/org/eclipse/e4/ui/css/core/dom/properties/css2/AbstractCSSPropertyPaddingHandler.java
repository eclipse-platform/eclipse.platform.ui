/*******************************************************************************
 *  Copyright (c) 2009, 2014 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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
		if (property == null) {
			return false;
		}

		switch (property) {
		case "padding":
			applyCSSPropertyPadding(element, value, pseudo, engine);
			break;
		case "padding-top":
			applyCSSPropertyPaddingTop(element, value, pseudo, engine);
			break;
		case "padding-right":
			applyCSSPropertyPaddingRight(element, value, pseudo, engine);
			break;
		case "padding-bottom":
			applyCSSPropertyPaddingBottom(element, value, pseudo, engine);
			break;
		case "padding-left":
			applyCSSPropertyPaddingLeft(element, value, pseudo, engine);
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
		case "padding":
			return retrieveCSSPropertyPadding(element, pseudo, engine);
		case "padding-top":
			return retrieveCSSPropertyPaddingTop(element, pseudo, engine);
		case "padding-right":
			return retrieveCSSPropertyPaddingRight(element, pseudo, engine);
		case "padding-bottom":
			return retrieveCSSPropertyPaddingBottom(element, pseudo, engine);
		case "padding-left":
			return retrieveCSSPropertyPaddingLeft(element, pseudo, engine);
		}

		return null;
	}
}
