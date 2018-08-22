/*******************************************************************************
 * Copyright (c) 2015, 2016 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyUnselectHotTabsColorBackgroundHandler extends AbstractCSSPropertySWTHandler {

	public static final String UNSELECTED_HOT_TAB_COLOR_BACKGROUND = "swt-unselected-hot-tab-color-background";

	public static final String DEPRECATED_UNSELECTED_HOT_TAB_COLOR_BACKGROUND = "unselected-hot-tab-color-background";

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (UNSELECTED_HOT_TAB_COLOR_BACKGROUND.equals(property)
				|| DEPRECATED_UNSELECTED_HOT_TAB_COLOR_BACKGROUND.equals(property)) {
			if (!(control instanceof CTabFolder)) {
				return;
			}
			if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
				Color newColor = (Color) engine.convert(value, Color.class, control.getDisplay());
				CTabFolderRenderer renderer = ((CTabFolder) control).getRenderer();
				if (renderer instanceof ICTabRendering) {
					((ICTabRendering) renderer).setUnselectedHotTabsColorBackground(newColor);
				}
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

}
