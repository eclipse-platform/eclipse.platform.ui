/*******************************************************************************
 * Copyright (c) 2016 Fabio Zadrozny and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabio Zadrozny <fabiofz@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.dom.Measure;
import org.eclipse.e4.ui.css.swt.dom.StyledTextElement;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.css.CSSValue;

@SuppressWarnings("restriction")
public class CSSPropertyStyledTextScrollbarSWTHandler implements ICSSPropertyHandler {

	public static final String SWT_SCROLLBAR_THEMED = "swt-scrollbar-themed"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_BACKGROUND_COLOR = "swt-scrollbar-background-color"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_FOREGROUND_COLOR = "swt-scrollbar-foreground-color"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_WIDTH = "swt-scrollbar-width"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_MOUSE_NEAR_SCROLL_WIDTH = "swt-scrollbar-mouse-near-scroll-width"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_VERTICAL_VISIBLE = "swt-scrollbar-vertical-visible"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_HORIZONTAL_VISIBLE = "swt-scrollbar-horizontal-visible"; //$NON-NLS-1$
	public static final String SWT_SCROLLBAR_BORDER_RADIUS = "swt-scrollbar-border-radius"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {

		// sanity checks for the CSS property
		if (!(element instanceof StyledTextElement)) {
			return false;
		}
		if (!(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)) {
			return false;
		}

		StyledTextElement styledTextElement = (StyledTextElement) element;
		StyledText styledText = styledTextElement.getStyledText();

		switch (property) {
		case SWT_SCROLLBAR_BACKGROUND_COLOR:
			Color bgColor = (Color) engine.convert(value, Color.class, styledText.getDisplay());
			styledTextElement.setScrollBarBackgroundColor(bgColor);
			break;
		case SWT_SCROLLBAR_FOREGROUND_COLOR:
			Color fgColor = (Color) engine.convert(value, Color.class, styledText.getDisplay());
			styledTextElement.setScrollBarForegroundColor(fgColor);
			break;
		case SWT_SCROLLBAR_WIDTH:
			if (value instanceof Measure) {
				Measure measure = (Measure) value;
				int width = (int) measure.getFloatValue(LexicalUnit.SAC_PIXEL);
				styledTextElement.setScrollBarWidth(width);
			}
			break;
		case SWT_SCROLLBAR_MOUSE_NEAR_SCROLL_WIDTH:
			if (value instanceof Measure) {
				Measure measure = (Measure) value;
				int width = (int) measure.getFloatValue(LexicalUnit.SAC_PIXEL);
				styledTextElement.setMouseNearScrollScrollBarWidth(width);
			}
			break;
		case SWT_SCROLLBAR_BORDER_RADIUS:
			if (value instanceof Measure) {
				Measure measure = (Measure) value;
				int radius = (int) measure.getFloatValue(LexicalUnit.SAC_PIXEL);
				styledTextElement.setScrollBarBorderRadius(radius);
			}
			break;
		case SWT_SCROLLBAR_VERTICAL_VISIBLE:
			Boolean verticalVisible = (Boolean) engine.convert(value, Boolean.class, styledText.getDisplay());
			styledTextElement.setVerticalScrollBarVisible(verticalVisible);
			break;
		case SWT_SCROLLBAR_HORIZONTAL_VISIBLE:
			Boolean horizontalVisible = (Boolean) engine.convert(value, Boolean.class, styledText.getDisplay());
			styledTextElement.setHorizontalScrollBarVisible(horizontalVisible);
			break;
		case SWT_SCROLLBAR_THEMED:
			String cssText = value.getCssText();
			styledTextElement.setScrollBarThemed(cssText);
			break;
		default:
			return false;
		}

		return true;
	}

}
