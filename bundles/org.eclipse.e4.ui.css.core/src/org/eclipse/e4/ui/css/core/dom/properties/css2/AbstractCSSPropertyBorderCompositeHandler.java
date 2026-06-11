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

import org.eclipse.e4.ui.css.core.css2.CSS2ColorHelper;
import org.eclipse.e4.ui.css.core.dom.properties.AbstractCSSPropertyCompositeHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssNumeric;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssUnit;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract class which dispatch border CSS Property defined to call the
 * applyCSSProperty methods CSS Properties border-color, border-style,
 * border-width.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public abstract class AbstractCSSPropertyBorderCompositeHandler extends
		AbstractCSSPropertyCompositeHandler {

	private static final String[] BORDER_CSSPROPERTIES = { "border-width",
			"border-style", "border-color" };

	@Override
	public void applyCSSProperty(Object element, CSSValue value, String pseudo,
			CSSEngine engine) throws Exception {
		if (value instanceof CssText text && text.kind() == CssText.Kind.IDENT) {
			if (CSS2ColorHelper.isColorName(text.value())) {
				engine.applyCSSProperty(element, "border-color", value, pseudo);
			} else {
				engine.applyCSSProperty(element, "border-style", value, pseudo);
			}
		} else if (value instanceof CssColor) {
			engine.applyCSSProperty(element, "border-color", value, pseudo);
		} else if (value instanceof CssNumeric numeric && (numeric.unit() == CssUnit.PT
				|| numeric.unit() == CssUnit.NUMBER || numeric.unit() == CssUnit.PX)) {
			engine.applyCSSProperty(element, "border-width", value, pseudo);
		}
	}

	@Override
	public boolean isCSSPropertyComposite(String property) {
		return "border".equals(property);
	}

	@Override
	public String[] getCSSPropertiesNames(String property) {
		if ("border".equals(property)) {
			return BORDER_CSSPROPERTIES;
		}
		return null;
	}

}
