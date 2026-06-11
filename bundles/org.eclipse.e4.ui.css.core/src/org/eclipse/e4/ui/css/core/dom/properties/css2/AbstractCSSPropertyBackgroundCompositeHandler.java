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

import org.eclipse.e4.ui.css.core.dom.properties.AbstractCSSPropertyCompositeHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssColor;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssText;
import org.w3c.dom.css.CSSValue;

/**
 * Abstract CSS property composite background which is enable to dispatch to well
 * CSS Property background-color, background-image...
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public abstract class AbstractCSSPropertyBackgroundCompositeHandler extends
		AbstractCSSPropertyCompositeHandler {

	private static final String[] BACKROUND_CSSPROPERTIES = {
			"background-attachment", "background-color", "background-image",
			"background-position", "background-repeat" };

	@Override
	public void applyCSSProperty(Object element, CSSValue value, String pseudo,
			CSSEngine engine) throws Exception {
		if (value instanceof CssColor
				|| (value instanceof CssText text && text.kind() == CssText.Kind.IDENT)) {
			engine.applyCSSProperty(element, "background-color", value, pseudo);
		} else if (value instanceof CssText text && text.kind() == CssText.Kind.URI) {
			engine.applyCSSProperty(element, "background-image", value, pseudo);
		}
	}

	@Override
	public boolean isCSSPropertyComposite(String property) {
		return "background".equals(property);
	}

	@Override
	public String[] getCSSPropertiesNames(String property) {
		if ("background".equals(property)) {
			return BACKROUND_CSSPROPERTIES;
		}
		return null;
	}
}
