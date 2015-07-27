/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.renderers.swt;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;

/**
 * Helper class for retrieving CSS values for a particular control so it can be
 * used by renderers.
 */
public class CSSEngineHelper {
	private IEclipseContext context;
	private Control control;
	private IStylingEngine engine;
	private CSSStyleDeclaration style;

	/**
	 * Create a CSSEngineHelper that can retrieve engine values for a particular
	 * control.
	 *
	 * @param context
	 * @param control
	 */
	public CSSEngineHelper(IEclipseContext context, Control control) {
		this.context = context;
		this.control = control;
		initialize();
	}

	private void initialize() {
		this.engine = context.get(IStylingEngine.class);
		if (engine != null) {
			style = engine.getStyle(control);
		}
	}

	/**
	 * Return the margin bottom as specified in the stylesheet for this control,
	 * or the defaultValue if none is specified.
	 *
	 * @param defaultValue
	 * @return the margin in pixels
	 */
	public int getMarginBottom(int defaultValue) {
		if (style == null)
			return defaultValue;
		return getPxValue(
				style.getPropertyCSSValue("margin-bottom"), defaultValue); //$NON-NLS-1$
	}

	/**
	 * Return the margin top as specified in the stylesheet for this control, or
	 * the defaultValue if none is specified.
	 *
	 * @param defaultValue
	 * @return the margin in pixels
	 */
	public int getMarginTop(int defaultValue) {
		if (style == null)
			return defaultValue;
		return getPxValue(style.getPropertyCSSValue("margin-top"), defaultValue); //$NON-NLS-1$
	}

	/**
	 * Return the margin left as specified in the stylesheet for this control,
	 * or the defaultValue if none is specified.
	 *
	 * @param defaultValue
	 * @return the margin in pixels
	 */
	public int getMarginLeft(int defaultValue) {
		if (style == null)
			return defaultValue;
		return getPxValue(
				style.getPropertyCSSValue("margin-left"), defaultValue); //$NON-NLS-1$
	}

	/**
	 * Return the margin right as specified in the stylesheet for this control,
	 * or the defaultValue if none is specified.
	 *
	 * @param defaultValue
	 * @return the margin in pixels
	 */
	public int getMarginRight(int defaultValue) {
		if (style == null)
			return defaultValue;
		return getPxValue(
				style.getPropertyCSSValue("margin-right"), defaultValue); //$NON-NLS-1$
	}

	private int getPxValue(CSSValue value, int defaultValue) {
		if (value != null
				&& (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE)
				&& (((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_PX)) {
			return (int) ((CSSPrimitiveValue) value)
					.getFloatValue(CSSPrimitiveValue.CSS_PX);
		}
		return defaultValue;
	}
}
