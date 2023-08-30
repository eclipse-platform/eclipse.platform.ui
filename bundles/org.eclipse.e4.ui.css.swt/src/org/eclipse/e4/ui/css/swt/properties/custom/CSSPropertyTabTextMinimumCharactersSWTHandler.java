/*******************************************************************************
 * Copyright (c) 2023 SAP SE.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: SAP SE - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

/**
 * CSS property to influence the minimum number of characters for rendering tab text and size
 *
 * Can be used in CSS Scratch Pad with the property name "swt-tab-text-minimum-characters", for example:
 * CTabFolder { swt-tab-text-minimum-characters: 20 }
 *
 * Default value for the property is 1.
 */
public class CSSPropertyTabTextMinimumCharactersSWTHandler extends AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof CTabFolder)) {
			return;
		}

		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
				&& (((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_NUMBER)) {
			int minimumCharacters = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_NUMBER);
			CTabFolder folder = (CTabFolder) control;
			folder.setMinimumCharacters(minimumCharacters);
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		if (control instanceof CTabFolder) {
			CTabFolder folder = (CTabFolder) control;
			return Integer.toString(folder.getMinimumCharacters());
		}
		return null;
	}

}