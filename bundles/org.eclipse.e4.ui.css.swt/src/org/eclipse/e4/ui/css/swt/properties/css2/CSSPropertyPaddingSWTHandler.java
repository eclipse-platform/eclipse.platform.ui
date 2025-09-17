/*******************************************************************************
 * Copyright (c) 2010, 2018 IBM Corporation and others.
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
package org.eclipse.e4.ui.css.swt.properties.css2;

import java.lang.reflect.Method;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2PaddingPropertiesImpl;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderRenderer;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Widget;
import org.w3c.css.sac.CSSException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSPropertyPaddingSWTHandler extends
AbstractCSSPropertyPaddingHandler {

	public static final ICSSPropertyPaddingHandler INSTANCE = new CSSPropertyPaddingSWTHandler();

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget == null) {
			return false;
		}

		return super.applyCSSProperty(element, property, value, pseudo, engine);
	}

	@Override
	public void applyCSSPropertyPadding(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {

		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		// If single value then assigned to all four paddings
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.top = padding.bottom = padding.left = padding.right = value;
			setPadding(element, padding);
			return;
		}

		if(value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList valueList = (CSSValueList) value;
			int length = valueList.getLength();

			if(length < 2 || length > 4) {
				throw new CSSException("Invalid padding property list length");
			}

			switch (length) {
			case 4:
				// If four values then assigned top/right/bottom/left
				padding.top = valueList.item(0);
				padding.right = valueList.item(1);
				padding.bottom = valueList.item(2);
				padding.left = valueList.item(3);
				break;
			case 3:
				// If three values then assigned top=v1, left=v2, right=v2, bottom=v3
				padding.top = valueList.item(0);
				padding.right =  valueList.item(1);
				padding.bottom = valueList.item(2);
				padding.left = valueList.item(1);
				break;
			case 2:
				// If two values then assigned top/bottom=v1, left/right=v2
				padding.top = valueList.item(0);
				padding.right = valueList.item(1);
				padding.bottom = valueList.item(0);
				padding.left = valueList.item(1);
			}

			setPadding(element, padding);
		} else {
			throw new CSSException("Invalid padding property value");
		}
	}

	@Override
	public void applyCSSPropertyPaddingTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.top = value;
			setPadding(element, padding);
		}
	}

	@Override
	public void applyCSSPropertyPaddingRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.right = value;
			setPadding(element, padding);
		}
	}

	@Override
	public void applyCSSPropertyPaddingBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.bottom = value;
			setPadding(element, padding);
		}
	}

	@Override
	public void applyCSSPropertyPaddingLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.left = value;
			setPadding(element, padding);
		}
	}

	@Override
	public String retrieveCSSPropertyPadding(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyPaddingTop(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyPaddingRight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyPaddingBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyPaddingLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private void setPadding(Object element, CSSValue value) {
		Widget widget = SWTElementHelpers.getWidget(element);

		if (widget instanceof CTabFolder folder) {
			CTabFolderRenderer renderer = folder.getRenderer();
			if (renderer == null) {
				return;
			}

			try {
				Method m = renderer.getClass().getMethod("getPadding");
				Rectangle pad = (Rectangle) m.invoke(renderer);

				// TBD: is there a CTF equivalent ?
				CSS2PaddingPropertiesImpl padding = (CSS2PaddingPropertiesImpl) value;
				CSSValue vTop = padding.top;
				CSSValue vRight = padding.right;
				CSSValue vBottom = padding.bottom;
				CSSValue vLeft = padding.left;

				int top = pad.x, right = pad.y, bottom = pad.width, left = pad.height;

				if (vTop != null && (vTop.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vTop).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
					top = (int) ((CSSPrimitiveValue) vTop).getFloatValue(CSSPrimitiveValue.CSS_PX);
				}

				if (vRight != null && (vRight.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vRight).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
					right = (int) ((CSSPrimitiveValue) vRight).getFloatValue(CSSPrimitiveValue.CSS_PX);
				}

				if (vBottom != null && (vBottom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vBottom).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
					bottom = (int) ((CSSPrimitiveValue) vBottom).getFloatValue(CSSPrimitiveValue.CSS_PX);
				}

				if (vLeft != null && (vLeft.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vLeft).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
					left = (int) ((CSSPrimitiveValue) vLeft).getFloatValue(CSSPrimitiveValue.CSS_PX);
				}

				if (top != pad.x || right != pad.y || bottom != pad.width
						|| left != pad.height) {
					Method m2 = renderer.getClass().getMethod("setPadding", int.class, int.class, int.class, int.class);
					m2.invoke(renderer, left, right, top, bottom);
				}
			} catch (Exception e) {

			}
		}
	}
}
