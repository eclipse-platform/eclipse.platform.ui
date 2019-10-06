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

public abstract class AbstractCSSPropertyMarginHandler implements ICSSPropertyMarginHandler {

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (property == null) {
			return false;
		}

		switch (property) {
		case "margin":
			applyCSSPropertyMargin(element, value, pseudo, engine);
			break;
		case "margin-top":
			applyCSSPropertyMarginTop(element, value, pseudo, engine);
			break;
		case "margin-right":
			applyCSSPropertyMarginRight(element, value, pseudo, engine);
			break;
		case "margin-bottom":
			applyCSSPropertyMarginBottom(element, value, pseudo, engine);
			break;
		case "margin-left":
			applyCSSPropertyMarginLeft(element, value, pseudo, engine);
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
		case "margin":
			return retrieveCSSPropertyMargin(element, pseudo, engine);
		case "margin-top":
			return retrieveCSSPropertyMarginTop(element, pseudo, engine);
		case "margin-right":
			return retrieveCSSPropertyMarginRight(element, pseudo, engine);
		case "margin-bottom":
			return retrieveCSSPropertyMarginBottom(element, pseudo, engine);
		case "margin-left":
			return retrieveCSSPropertyMarginLeft(element, pseudo, engine);
		}
		return null;
	}
}
