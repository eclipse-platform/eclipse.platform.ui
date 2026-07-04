/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

/**
 * Applies the CSS properties that map directly onto a single
 * {@link ICTabRendering} setter of a {@link CTabFolder} renderer.
 */
public class CSSPropertyCTabFolderRendererSWTHandler extends AbstractCSSPropertySWTHandler {

	private static final Map<String, BiConsumer<ICTabRendering, Color>> COLOR_SETTERS = Map.of(
			"swt-inner-keyline-color", ICTabRendering::setInnerKeyline, //$NON-NLS-1$
			"swt-outer-keyline-color", ICTabRendering::setOuterKeyline, //$NON-NLS-1$
			"swt-tab-outline", ICTabRendering::setTabOutline, //$NON-NLS-1$
			"swt-unselected-hot-tab-color-background", ICTabRendering::setUnselectedHotTabsColorBackground); //$NON-NLS-1$

	private static final Map<String, BiConsumer<ICTabRendering, Boolean>> BOOLEAN_SETTERS = Map.of(
			"swt-draw-custom-tab-content-background", ICTabRendering::setDrawCustomTabContentBackground); //$NON-NLS-1$

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(control instanceof CTabFolder folder) || !(value instanceof CssPrimitive)) {
			return;
		}
		BiConsumer<ICTabRendering, Color> colorSetter = COLOR_SETTERS.get(property);
		if (colorSetter != null) {
			Color color = (Color) engine.convert(value, Color.class, control.getDisplay());
			if (folder.getRenderer() instanceof ICTabRendering renderer) {
				colorSetter.accept(renderer, color);
			}
			return;
		}
		BiConsumer<ICTabRendering, Boolean> booleanSetter = BOOLEAN_SETTERS.get(property);
		if (booleanSetter != null) {
			Boolean enabled = (Boolean) engine.convert(value, Boolean.class, control.getDisplay());
			if (folder.getRenderer() instanceof ICTabRendering renderer) {
				booleanSetter.accept(renderer, enabled);
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}
}
