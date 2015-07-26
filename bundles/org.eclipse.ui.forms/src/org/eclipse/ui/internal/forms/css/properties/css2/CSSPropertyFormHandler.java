/*******************************************************************************
 * Copyright (c) 2015 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.forms.css.properties.css2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.e4.ui.css.core.dom.properties.Gradient;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.Form;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyFormHandler extends AbstractCSSPropertySWTHandler {

	// Constants to customize the Form (IFormColors constants).
	public static final String TB_TOGGLE_HOVER = "tb-toggle-hover-color"; //$NON-NLS-1$
	public static final String TB_TOGGLE = "tb-toggle-color"; //$NON-NLS-1$
	public static final String H_HOVER_FULL = "h-hover-full-color"; //$NON-NLS-1$
	public static final String H_HOVER_LIGHT = "h-hover-light-color"; //$NON-NLS-1$
	public static final String H_BOTTOM_KEYLINE_2 = "h-bottom-keyline-2-color"; //$NON-NLS-1$
	public static final String H_BOTTOM_KEYLINE_1 = "h-bottom-keyline-1-color"; //$NON-NLS-1$

	// Constant to customize:
	// org.eclipse.ui.forms.widgets.Form.setTextBackground(Color[], int[],
	// boolean)
	public static final String TEXT_BACKGROUND_COLOR = "text-background-color"; //$NON-NLS-1$

	private static final Map<String, String> propertyToHeadProperty = new HashMap<>();

	static {
		propertyToHeadProperty.put(H_BOTTOM_KEYLINE_1, IFormColors.H_BOTTOM_KEYLINE1);
		propertyToHeadProperty.put(H_BOTTOM_KEYLINE_2, IFormColors.H_BOTTOM_KEYLINE2);
		propertyToHeadProperty.put(H_HOVER_LIGHT, IFormColors.H_HOVER_LIGHT);
		propertyToHeadProperty.put(H_HOVER_FULL, IFormColors.H_HOVER_FULL);
		propertyToHeadProperty.put(TB_TOGGLE, IFormColors.TB_TOGGLE);
		propertyToHeadProperty.put(TB_TOGGLE_HOVER, IFormColors.TB_TOGGLE_HOVER);
	}

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (control instanceof Form) {
			Form form = (Form) control;
			if (TEXT_BACKGROUND_COLOR.equals(property)) {
				if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
					Color color = (Color) engine.convert(value, Color.class, form.getDisplay());
					// When a single color is received, make it 100% with that
					// single color.
					form.setTextBackground(new Color[] { color }, new int[] { 100 }, true);

				} else if (value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
					Gradient grad = (Gradient) engine.convert(value, Gradient.class, form.getDisplay());
					if (grad == null) {
						return;
					}
					List<CSSPrimitiveValue> values = grad.getValues();
					List<Color> colors = new ArrayList<>(values.size());
					for (CSSPrimitiveValue cssValue : values) {
						if (cssValue != null) {
							if (cssValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
								Color color = (Color) engine.convert(cssValue, Color.class, form.getDisplay());
								colors.add(color);
							}
						}
					}

					if (colors.size() > 0) {
						List<Integer> list = grad.getPercents();
						int[] percents = new int[list.size()];
						for (int i = 0; i < percents.length; i++) {
							percents[i] = list.get(i).intValue();
						}
						form.setTextBackground(colors.toArray(new Color[0]), percents,
								grad.getVerticalGradient());
					}
				}

			} else {
				String headProperty = propertyToHeadProperty.get(property);
				if (headProperty != null) {
					if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
						Color color = (Color) engine.convert(value, Color.class, form.getDisplay());
						form.setHeadColor(headProperty, color);
					}
				}
			}
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
