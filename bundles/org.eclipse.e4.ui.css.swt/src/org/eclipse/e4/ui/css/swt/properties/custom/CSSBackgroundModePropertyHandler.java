/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSBackgroundModePropertyHandler extends
AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE || !(control instanceof Composite composite)) {
			return;
		}
		String stringValue = value.getCssText().toLowerCase();

		switch (stringValue) {
		case "default":
			composite.setBackgroundMode(SWT.INHERIT_DEFAULT);
			break;
		case "force":
			composite.setBackgroundMode(SWT.INHERIT_FORCE);
			break;
		case "none":
			composite.setBackgroundMode(SWT.INHERIT_NONE);
			break;
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof Composite composite) {
			switch (composite.getBackgroundMode()) {
			case SWT.INHERIT_DEFAULT:
				return "default";
			case SWT.INHERIT_FORCE:
				return "force";
			case SWT.INHERIT_NONE:
				return "none";
			}
		}
		return null;
	}

}
