/*******************************************************************************
 * Copyright (c) 2017 SAP SE and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.ui.internal.views.properties.tabbed.css;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyList;
import org.w3c.dom.css.CSSValue;

public class TabbedPropertyListCssPropertyHandler extends AbstractCSSPropertySWTHandler implements ICSSPropertyHandler {

	private static final String TAB_AREA_BACKGROUND_COLOR = "swt-tabAreaBackground-color"; //$NON-NLS-1$
	private static final String TAB_BACKGROUND_COLOR = "swt-tabBackground-color"; //$NON-NLS-1$
	private static final String TAB_NORMAL_SHADOW_COLOR = "swt-tabNormalShadow-color"; //$NON-NLS-1$
	private static final String TAB_DARK_SHADOW_COLOR = "swt-tabDarkShadow-color"; //$NON-NLS-1$
	private static final String COLOR = "color"; //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof TabbedPropertyList)) {
			return;
		}
		TabbedPropertyList list = (TabbedPropertyList) control;
		if (TAB_BACKGROUND_COLOR.equals(property)) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
				list.setListBackgroundColor(color);
			}
		} else if (TAB_AREA_BACKGROUND_COLOR.equals(property)) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
				list.setWidgetBackgroundColor(color);
			}
		} else if (COLOR.equals(property)) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
				list.setWidgetForegroundColor(color);
			}
		} else if (TAB_NORMAL_SHADOW_COLOR.equals(property)) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
				list.setWidgetNormalShadowColor(color);
			}
		} else if (TAB_DARK_SHADOW_COLOR.equals(property)) {
			if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
				Color color = CSSSWTColorHelper.getSWTColor(value, control.getDisplay());
				list.setWidgetDarkShadowColor(color);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
