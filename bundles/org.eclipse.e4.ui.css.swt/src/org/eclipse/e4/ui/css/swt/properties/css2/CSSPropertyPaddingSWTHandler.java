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

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.widgets.Widget;
import org.w3c.css.sac.CSSException;
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
		
		// If single value then assigned to all four paddings
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			setPadding(element, TOP, value, pseudo);
			setPadding(element, RIGHT, value, pseudo);
			setPadding(element, BOTTOM, value, pseudo);
			setPadding(element, LEFT, value, pseudo);
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
				setPadding(element, TOP, valueList.item(0), pseudo);
				setPadding(element, RIGHT, valueList.item(1), pseudo);
				setPadding(element, BOTTOM, valueList.item(2), pseudo);
				setPadding(element, LEFT, valueList.item(3), pseudo);				
				break;
			case 3:
				// If three values then assigned top=v1, left=v2, right=v2, bottom=v3
				setPadding(element, TOP, valueList.item(0), pseudo);
				setPadding(element, RIGHT, valueList.item(1), pseudo);
				setPadding(element, BOTTOM, valueList.item(2), pseudo);
				setPadding(element, LEFT, valueList.item(1), pseudo);
			case 2:
				// If two values then assigned top/bottom=v1, left/right=v2
				setPadding(element, TOP, valueList.item(0), pseudo);
				setPadding(element, RIGHT, valueList.item(1), pseudo);
				setPadding(element, BOTTOM, valueList.item(0), pseudo);
				setPadding(element, LEFT, valueList.item(1), pseudo);
			}
		} else {
			throw new CSSException("Invalid padding property value");
		}
	}

	public void applyCSSPropertyPaddingTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, TOP, value, pseudo);
	}

	public void applyCSSPropertyPaddingRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, RIGHT, value, pseudo);
	}

	public void applyCSSPropertyPaddingBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, BOTTOM, value, pseudo);
	}

	public void applyCSSPropertyPaddingLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, LEFT, value, pseudo);
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
	
	private void setPadding(Object element, int side, CSSValue value, String pseudo) {
		if(value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE)
			return;
		Widget widget = SWTElementHelpers.getWidget(element);
		 // TBD: is there a CTF equivalent ?
	}
}
