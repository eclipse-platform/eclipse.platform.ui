/*******************************************************************************
 * Copyright (c) 2017 Fabian Pfaff and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Fabian Pfaff <fabian.pfaff@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.dom.IHeaderCustomizationElement;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.css.CSSValue;

/**
 * A handler which allows the styling of IHeaderCustomizationElements.
 */
public class CSSPropertyHeaderHandler implements ICSSPropertyHandler {

	private static final String SWT_HEADER_COLOR = "swt-header-color"; //$NON-NLS-1$
	private static final String SWT_HEADER_BACKGROUND_COLOR = "swt-header-background-color"; //$NON-NLS-1$

	@Override
	public boolean applyCSSProperty(Object element, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		if (!(element instanceof IHeaderCustomizationElement &&  element instanceof ElementAdapter)) {
			return false;
		}
		IHeaderCustomizationElement headerCustomizationElement = (IHeaderCustomizationElement) element;
		ElementAdapter elementAdapter = (ElementAdapter) element;
		Object nativeWidget = elementAdapter.getNativeWidget();
		if (!(nativeWidget instanceof Widget)) {
			return false;
		}
		Widget widget = (Widget) nativeWidget;
		return setHeaderColor(property, value, engine, headerCustomizationElement, widget);
	}

	private boolean setHeaderColor(String property, CSSValue value, CSSEngine engine,
			IHeaderCustomizationElement headerCustomizationElement, Widget widget) throws Exception {
		if (SWT_HEADER_COLOR.equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, widget.getDisplay());
			headerCustomizationElement.setHeaderColor(newColor);
			return true;
		} else if (SWT_HEADER_BACKGROUND_COLOR.equals(property)
				&& value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Color newColor = (Color) engine.convert(value, Color.class, widget.getDisplay());
			headerCustomizationElement.setHeaderBackgroundColor(newColor);
			return true;
		}
		return false;
	}

	@Override
	public String retrieveCSSProperty(Object element, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}
