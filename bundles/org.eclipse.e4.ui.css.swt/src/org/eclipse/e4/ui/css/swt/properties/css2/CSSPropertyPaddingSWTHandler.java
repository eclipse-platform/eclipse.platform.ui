/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.CSS2PaddingPropertiesImpl;

import java.lang.reflect.Method;
import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.e4.ui.widgets.CTabFolder;
import org.eclipse.e4.ui.widgets.CTabFolderRenderer;
import org.eclipse.swt.widgets.Widget;
import org.w3c.css.sac.CSSException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSPropertyPaddingSWTHandler extends
		AbstractCSSPropertyPaddingHandler {

	public final static ICSSPropertyPaddingHandler INSTANCE = new CSSPropertyPaddingSWTHandler();
	
	private final static int TOP = 0;
	private final static int RIGHT = 1;
	private final static int BOTTOM = 2;
	private final static int LEFT = 3;

	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {
		Widget widget = SWTElementHelpers.getWidget(element);
		if (widget == null)
			return false;
				
		super.applyCSSProperty(element, property, value, pseudo, engine);
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyPaddingHandler#applyCSSPropertyPadding(java.lang.Object, org.w3c.dom.css.CSSValue, java.lang.String, org.eclipse.e4.ui.css.core.engine.CSSEngine)
	 * If single value then assigned to all four paddings
	 * If four values then assigned top/right/bottom/left
	 * If three values then assigned top=v1, left=v2, right=v2, bottom=v3
	 * If two values then assigned top/bottom=v1, left/right=v2
	 */
	public void applyCSSPropertyPadding(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		
		CSS2PaddingPropertiesImpl padding = new CSS2PaddingPropertiesImpl();
		// If single value then assigned to all four paddings
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			padding.top = padding.bottom = padding.left = padding.right = value;
			setPadding(element, padding, pseudo);
			return;
		}
		
		if(value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList valueList = (CSSValueList) value;
			int length = valueList.getLength();

			if(length < 2 || length > 4)
				throw new CSSException("Invalid padding property list length");
			
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
			
			setPadding(element, padding, pseudo);
		} else {
			throw new CSSException("Invalid padding property value");
		}
	}

	public void applyCSSPropertyPaddingTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, value, pseudo);
	}

	public void applyCSSPropertyPaddingRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, value, pseudo);
	}

	public void applyCSSPropertyPaddingBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, value, pseudo);
	}

	public void applyCSSPropertyPaddingLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, value, pseudo);
	}

	public String retrieveCSSPropertyPadding(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyPaddingTop(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyPaddingRight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyPaddingBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	public String retrieveCSSPropertyPaddingLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void setPadding(Object element, CSSValue value, String pseudo) {
//		if(value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE)
//			return;
		Widget widget = SWTElementHelpers.getWidget(element);
		 // TBD: is there a CTF equivalent ?
		CSS2PaddingPropertiesImpl padding = (CSS2PaddingPropertiesImpl) value;
		CSSValue vTop = padding.top;
		CSSValue vRight = padding.right;
		CSSValue vBottom = padding.bottom;
		CSSValue vLeft = padding.left;
		
		if ((vTop.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vTop).getPrimitiveType() == CSSPrimitiveValue.CSS_PX &&
			(vBottom.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vBottom).getPrimitiveType() == CSSPrimitiveValue.CSS_PX &&
			(vLeft.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vLeft).getPrimitiveType() == CSSPrimitiveValue.CSS_PX &&
			(vRight.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) && ((CSSPrimitiveValue) vRight).getPrimitiveType() == CSSPrimitiveValue.CSS_PX) {
			
			if (widget instanceof CTabFolder) {
				CTabFolder folder = (CTabFolder) widget;
				CTabFolderRenderer renderer = ((CTabFolder) folder).getRenderer();
				if (renderer == null) return;
				try {
					int top = (int) ((CSSPrimitiveValue) vTop).getFloatValue(CSSPrimitiveValue.CSS_PX);
					int right = (int) ((CSSPrimitiveValue) vRight).getFloatValue(CSSPrimitiveValue.CSS_PX);
					int bottom = (int) ((CSSPrimitiveValue) vBottom).getFloatValue(CSSPrimitiveValue.CSS_PX);
					int left = (int) ((CSSPrimitiveValue) vLeft).getFloatValue(CSSPrimitiveValue.CSS_PX);
					Method m = renderer.getClass().getMethod("setPadding", new Class[]{int.class, int.class, int.class, int.class});
					m.invoke(renderer, left, right, top, bottom);
				} catch (Exception e) {
					
				}
			}
		}
	}
}
