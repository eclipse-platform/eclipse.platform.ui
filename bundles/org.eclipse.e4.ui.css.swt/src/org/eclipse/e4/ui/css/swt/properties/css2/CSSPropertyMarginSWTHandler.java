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
package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyMarginHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyMarginHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.CSSSWTConstants;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Widget;
import org.w3c.css.sac.CSSException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;

public class CSSPropertyMarginSWTHandler extends
		AbstractCSSPropertyMarginHandler {

	public final static ICSSPropertyMarginHandler INSTANCE = new CSSPropertyMarginSWTHandler();

	private final static int TOP = 0;
	private final static int RIGHT = 1;
	private final static int BOTTOM = 2;
	private final static int LEFT = 3;

	@Override
	public boolean applyCSSProperty(Object element, String property,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception {

		super.applyCSSProperty(element, property, value, pseudo, engine);
		return true;
	}

	@Override
	public void applyCSSPropertyMargin(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {

		// If single value then assigned to all four margins
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			setMargin(element, TOP, value, pseudo);
			setMargin(element, RIGHT, value, pseudo);
			setMargin(element, BOTTOM, value, pseudo);
			setMargin(element, LEFT, value, pseudo);
			return;
		}

		if(value.getCssValueType() == CSSValue.CSS_VALUE_LIST) {
			CSSValueList valueList = (CSSValueList) value;
			int length = valueList.getLength();

			if(length < 2 || length > 4) {
				throw new CSSException("Invalid margin property list length");
			}

			switch (length) {
			case 4:
				// If four values then assigned top/right/bottom/left
				setMargin(element, TOP, valueList.item(0), pseudo);
				setMargin(element, RIGHT, valueList.item(1), pseudo);
				setMargin(element, BOTTOM, valueList.item(2), pseudo);
				setMargin(element, LEFT, valueList.item(3), pseudo);
				break;
			case 3:
				// If three values then assigned top=v1, left=v2, right=v2, bottom=v3
				setMargin(element, TOP, valueList.item(0), pseudo);
				setMargin(element, RIGHT, valueList.item(1), pseudo);
				setMargin(element, BOTTOM, valueList.item(2), pseudo);
				setMargin(element, LEFT, valueList.item(1), pseudo);
			case 2:
				// If two values then assigned top/bottom=v1, left/right=v2
				setMargin(element, TOP, valueList.item(0), pseudo);
				setMargin(element, RIGHT, valueList.item(1), pseudo);
				setMargin(element, BOTTOM, valueList.item(0), pseudo);
				setMargin(element, LEFT, valueList.item(1), pseudo);
			}
		} else {
			throw new CSSException("Invalid margin property value");
		}
	}

	@Override
	public void applyCSSPropertyMarginTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setMargin(element, TOP, value, pseudo);
	}

	@Override
	public void applyCSSPropertyMarginRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setMargin(element, RIGHT, value, pseudo);
	}

	@Override
	public void applyCSSPropertyMarginBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setMargin(element, BOTTOM, value, pseudo);
	}

	@Override
	public void applyCSSPropertyMarginLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setMargin(element, LEFT, value, pseudo);
	}

	@Override
	public String retrieveCSSPropertyMargin(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyMarginTop(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyMarginRight(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyMarginBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String retrieveCSSPropertyMarginLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	private GridLayout getLayout(Control control) {
		if (control == null) {
			return null;
		}
		Composite parent = control.getParent();
		if (parent == null) {
			return null;
		}
		if(parent.getData(CSSSWTConstants.MARGIN_WRAPPER_KEY) == null) {
			return null;
		}

		Layout layout = parent.getLayout();
		if (layout == null || ! (layout instanceof GridLayout)) {
			return null;
		}
		return (GridLayout) layout;
	}

	private void setMargin(Object element, int side, CSSValue value, String pseudo) {
		if(value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE) {
			return;
		}
		int pixelValue = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PX);

		Widget widget = SWTElementHelpers.getWidget(element);

		if(! (widget instanceof Control)) {
			return;
		}

		GridLayout layout = getLayout((Control) widget);
		if(layout == null) {
			return;
		}
		switch (side) {
		case TOP:
			layout.marginTop = pixelValue;
			break;
		case RIGHT:
			layout.marginRight = pixelValue;
			break;
		case BOTTOM:
			layout.marginBottom = pixelValue;
			break;
		case LEFT:
			layout.marginLeft = pixelValue;
			break;
		}
	}
}
