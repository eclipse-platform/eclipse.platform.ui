/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyClassificationHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyClassificationHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.CSSSWTCursorHelper;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyClassificationSWTHandler extends
AbstractCSSPropertyClassificationHandler {

	public final static ICSSPropertyClassificationHandler INSTANCE = new CSSPropertyClassificationSWTHandler();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			super.applyCSSProperty(control, property, value, pseudo, engine);
			return true;
		}
		return false;

	}

	@Override
	public String retrieveCSSProperty(Object element, String property,
			String pseudo, CSSEngine engine) throws Exception {
		Control control = SWTElementHelpers.getControl(element);
		if (control != null) {
			return super.retrieveCSSProperty(control, property, pseudo, engine);
		}
		return null;
	}

	@Override
	public void applyCSSPropertyCursor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			final Control control = (Control) element;
			Cursor cursor = (Cursor) engine.convert(value, Cursor.class,
					control.getDisplay());
			CSSSWTCursorHelper.storeDefaultCursor(control);
			control.setCursor(cursor);
		}
	}

	@Override
	public void applyCSSPropertyVisibility(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			Control control = (Control) element;
			CSSPrimitiveValue primitiveValue = (CSSPrimitiveValue) value;
			String visibility = primitiveValue.getStringValue();
			if ("hidden".equals(visibility)) {
				control.setVisible(false);
			} else if ("collapse".equals(visibility)) {
				// TODO : manage collapse
				control.setVisible(false);
			} else {
				control.setVisible(true);
			}
		}
	}

	@Override
	public String retrieveCSSPropertyCursor(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		Control control = (Control) element;
		Cursor cursor = null;
		try {
			cursor = control.getCursor();
		} catch (Throwable e) {
			//TODO replace with eclipse logging
			//			if (logger.isWarnEnabled())
			//				logger
			//						.warn("Impossible to manage cursor, This SWT version doesn't support control.getCursor() Method");
		}
		return engine.convert(cursor, Cursor.class, null);
	}

	@Override
	public String retrieveCSSPropertyVisibility(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// Control control = (Control) element;
		// if (control.isVisible())
		return "visible";
		// return "hidden";
	}
}
