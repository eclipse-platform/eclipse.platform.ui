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

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyListCssPropertyHandler extends AbstractCSSPropertySWTHandler {

	private static final String TAB_AREA_BACKGROUND_COLOR = "swt-tabAreaBackground-color"; //$NON-NLS-1$
	private static final String TAB_BACKGROUND_COLOR = "swt-tabBackground-color"; //$NON-NLS-1$
	private static final String TAB_NORMAL_SHADOW_COLOR = "swt-tabNormalShadow-color"; //$NON-NLS-1$
	private static final String TAB_DARK_SHADOW_COLOR = "swt-tabDarkShadow-color"; //$NON-NLS-1$
	private static final String COLOR = "color"; //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof TabbedPropertyList) || property == null
				|| value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return;
		}

		TabbedPropertyList list = (TabbedPropertyList) control;
		Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());

		switch (property) {
		case TAB_BACKGROUND_COLOR:
			list.setListBackgroundColor(color);
			break;
		case TAB_AREA_BACKGROUND_COLOR:
			list.setWidgetBackgroundColor(color);
			break;
		case COLOR:
			list.setWidgetForegroundColor(color);
			break;
		case TAB_NORMAL_SHADOW_COLOR:
			list.setWidgetNormalShadowColor(color);
			break;
		case TAB_DARK_SHADOW_COLOR:
			list.setWidgetDarkShadowColor(color);
			break;
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
