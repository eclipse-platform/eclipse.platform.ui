/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.definition;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyFontHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontProperties;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2FontPropertiesImpl;
import org.eclipse.e4.ui.css.core.engine.CSSElementContext;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.definition.FontDefinitionElement;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTFontHelper;
import org.eclipse.e4.ui.internal.css.swt.definition.IFontDefinitionOverridable;
import org.eclipse.swt.graphics.FontData;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyFontDefinitionHandler extends AbstractCSSPropertyFontHandler {
	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (element instanceof FontDefinitionElement) {
			CSS2FontProperties properties = new CSS2FontPropertiesImpl();
			IFontDefinitionOverridable definition = (IFontDefinitionOverridable) ((FontDefinitionElement) element).getNativeWidget();
			FontData baseFontData = getBaseFontData(definition, engine.getCSSElementContext(element));

			super.applyCSSProperty(properties, property, value, pseudo, engine);
			setFontProperties(definition, CSSSWTFontHelper.resolveRelativeSize(properties, baseFontData));
			return true;
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		return null;
	}

	private void setFontProperties(IFontDefinitionOverridable definition, CSS2FontProperties properties) {
		definition.setValue(new FontData[] { CSSSWTFontHelper.getFontData(properties, getFontData(definition)) });
	}

	/**
	 * The font the definition was registered with, so that a relative size scales
	 * the registered font instead of the override applied by an earlier style.
	 */
	private static FontData getBaseFontData(IFontDefinitionOverridable definition, CSSElementContext context) {
		FontData baseFontData = CSSSWTFontHelper.getBaseFontData(context);
		// an overridden definition still carries the override of an earlier pass,
		// unless it was reset to its registered value in between
		if (baseFontData == null || !definition.isOverridden()) {
			baseFontData = getFontData(definition);
			CSSSWTFontHelper.setBaseFontData(context, baseFontData);
		}
		return baseFontData;
	}

	private static FontData getFontData(IFontDefinitionOverridable definition) {
		FontData[] value = definition.getValue();
		return value != null && value.length > 0 ? value[0] : null;
	}

	@Override
	public String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Override
	public String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}
}
