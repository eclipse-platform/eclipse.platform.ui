/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTColorHelper;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.e4.ui.internal.css.swt.ICTabRendering;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertye4SelectedTabFillHandler extends
AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder)) {
			return;
		}
		CTabFolder folder = (CTabFolder) control;
		CTabFolderRenderer renderer = folder.getRenderer();
		if (!(renderer instanceof ICTabRendering)) {
			return;
		}
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class,
					control.getDisplay());
			if (newColor == null) {
				return;
			}
			((ICTabRendering) renderer).setSelectedTabFill(newColor);
		} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			Gradient grad = (Gradient) engine.convert(value, Gradient.class,
					control.getDisplay());
			if (grad == null || grad.getRGBs().isEmpty()) {
				return;
			}
			Color[] colors = CSSSWTColorHelper.getSWTColors(grad,
					folder.getDisplay(), engine);
			int[] percents = CSSSWTColorHelper.getPercents(grad);
			((ICTabRendering) renderer).setSelectedTabFill(colors, percents);
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
