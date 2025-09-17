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
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 422702
 *******************************************************************************/

package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabPositionSWTHandler extends
AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		if (!(control instanceof CTabFolder tabFolder) || value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE
				|| ((CSSPrimitiveValue) value).getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
			return;
		}

		String position = ((CSSPrimitiveValue) value).getStringValue();
		switch (position.toLowerCase()) {
		case "bottom":
			tabFolder.setTabPosition(SWT.BOTTOM);
			break;
		case "top":
			tabFolder.setTabPosition(SWT.TOP);
			break;
		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		if (control instanceof CTabFolder folder) {
			int position = folder.getTabPosition();

			if (position == SWT.TOP) {
				return "top";
			} else {
				return "bottom";
			}
		}
		return null;
	}

}
