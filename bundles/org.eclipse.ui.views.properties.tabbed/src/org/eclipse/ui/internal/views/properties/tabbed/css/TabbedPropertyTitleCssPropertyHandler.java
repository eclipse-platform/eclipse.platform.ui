/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.css;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyTitleCssPropertyHandler extends AbstractCSSPropertySWTHandler
		implements ICSSPropertyHandler {

	private static final String GRADIENT_START = "swt-backgroundGradientStart-color"; //$NON-NLS-1$
	private static final String GRADIENT_END = "swt-backgroundGradientEnd-color"; //$NON-NLS-1$
	private static final String BOTTOM_KEYLINE_1 = "swt-backgroundBottomKeyline1-color"; //$NON-NLS-1$
	private static final String BOTTOM_KEYLINE_2 = "swt-backgroundBottomKeyline2-color"; //$NON-NLS-1$

	private final Map<String, String> cssPropertyToSWTProperty = new HashMap<>();

	public TabbedPropertyTitleCssPropertyHandler() {
		cssPropertyToSWTProperty.put(BOTTOM_KEYLINE_1, IFormColors.H_BOTTOM_KEYLINE1);
		cssPropertyToSWTProperty.put(BOTTOM_KEYLINE_2, IFormColors.H_BOTTOM_KEYLINE2);
		cssPropertyToSWTProperty.put(GRADIENT_START, IFormColors.H_GRADIENT_START);
		cssPropertyToSWTProperty.put(GRADIENT_END, IFormColors.H_GRADIENT_END);
	}

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof TabbedPropertyTitle) || value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE
				|| property == null || !cssPropertyToSWTProperty.containsKey(property)) {
			return;
		}

		TabbedPropertyTitle title = (TabbedPropertyTitle) control;
		title.setColor(cssPropertyToSWTProperty.get(property), CSSSWTColorHelper.getRGBA(value));
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
