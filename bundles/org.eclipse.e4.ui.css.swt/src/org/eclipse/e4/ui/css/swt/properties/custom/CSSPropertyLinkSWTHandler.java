/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz at gmail dot com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.LinkElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Link;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyLinkSWTHandler implements ICSSPropertyHandler {

	public static final String SWT_LINK_FOREGROUND_COLOR = "swt-link-foreground-color"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		// sanity checks for the CSS property
		if (!(element instanceof LinkElement)) {
			return false;
		}
		if (!(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
			return false;
		}

		LinkElement linkElement = (LinkElement) element;
		Link link = linkElement.getLink();

		if (SWT_LINK_FOREGROUND_COLOR.equals(property)) {
			Color newColor = (Color) engine.convert(value, Color.class, link.getDisplay());
			link.setLinkForeground(newColor);
		}
		return false;
	}

}
