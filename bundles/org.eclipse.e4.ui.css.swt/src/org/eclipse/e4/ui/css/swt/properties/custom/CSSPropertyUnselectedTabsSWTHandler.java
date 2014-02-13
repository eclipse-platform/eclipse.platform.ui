/*******************************************************************************
 * Copyright (c) 2010, 2014 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyUnselectedTabsSWTHandler extends AbstractCSSPropertySWTHandler {
	private static final String UNSELECTED_TABS_COLOR_PROP = "swt-unselected-tabs-color";

	private static final String DEPRECATED_UNSELECTED_TABS_COLOR_PROP = "unselected-tabs-color";

	public static final ICSSPropertyHandler INSTANCE = new CSSPropertyUnselectedTabsSWTHandler();

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) {
			return;
		}
		CTabFolder folder = ((CTabFolder) control);
		CTabFolderRenderer renderer = folder.getRenderer();
		if (!(renderer instanceof ICTabRendering)) {
			return;
		}

		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color color = (Color) engine.convert(value, Color.class,
					control.getDisplay());
			((ICTabRendering) renderer).setUnselectedTabsColor(color);
			folder.setBackground(color);
			return;
		}
		if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Gradient grad = (Gradient) engine.convert(value, Gradient.class, control.getDisplay());
			Color[] colors = null;
			int[] percents = null;
			if (!grad.getValues().isEmpty()) {
				colors = CSSSWTColorHelper.getSWTColors(grad,
						control.getDisplay(), engine);
				percents = CSSSWTColorHelper.getPercents(grad);
			}
			if (isUnselectedTabsColorProp(property)) {
				((ICTabRendering) renderer).setUnselectedTabsColor(colors,
						percents);
			}
			folder.setBackground(colors, percents, true);
		}
	}


	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isUnselectedTabsColorProp(String property) {
		return UNSELECTED_TABS_COLOR_PROP.equals(property)
				|| DEPRECATED_UNSELECTED_TABS_COLOR_PROP.equals(property);
	}

}
