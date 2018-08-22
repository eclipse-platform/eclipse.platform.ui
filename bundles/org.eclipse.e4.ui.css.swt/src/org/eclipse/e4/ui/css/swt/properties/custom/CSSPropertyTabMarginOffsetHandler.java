/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 *
 * This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License 2.0 which accompanies this distribution, and is
t https://www.eclipse.org/legal/epl-2.0/
t
t SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabMarginOffsetHandler extends
AbstractCSSPropertySWTHandler {

	@Override
	protected void applyCSSProperty(Control control, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		//		if (! (control instanceof ETabFolder))
		//			return;
		//
		//		if ((value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) &&
		//				( ((CSSPrimitiveValue) value).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) ) {
		//			int offset = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PX);
		//			((ETabFolder) control).setTabMarginOffset(offset);
		//		}
	}

	@Override
	protected String retrieveCSSProperty(Control control, String property,
			String pseudo, CSSEngine engine) throws Exception {
		//		if (control instanceof ETabFolder) {
		//			ETabFolder folder = (ETabFolder)control;
		//			return Integer.toString( folder.getTabMarginOffset());
		//		}
		return null;
	}

}
