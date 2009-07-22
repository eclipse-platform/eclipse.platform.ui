package org.eclipse.e4.ui.css.swt.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.css2.AbstractCSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.dom.properties.css2.ICSSPropertyPaddingHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.helpers.SWTElementHelpers;
import org.eclipse.e4.ui.widgets.ETabFolder;
import org.eclipse.e4.ui.widgets.ETabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
		
		// If single value then assigned to all four paddings
		if(value.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
			setPadding(element, TOP, value);
			setPadding(element, RIGHT, value);
			setPadding(element, BOTTOM, value);
			setPadding(element, LEFT, value);
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
				setPadding(element, TOP, valueList.item(0));
				setPadding(element, RIGHT, valueList.item(1));
				setPadding(element, BOTTOM, valueList.item(2));
				setPadding(element, LEFT, valueList.item(3));				
				break;
			case 3:
				// If three values then assigned top=v1, left=v2, right=v2, bottom=v3
				setPadding(element, TOP, valueList.item(0));
				setPadding(element, RIGHT, valueList.item(1));
				setPadding(element, BOTTOM, valueList.item(2));
				setPadding(element, LEFT, valueList.item(1));
			case 2:
				// If two values then assigned top/bottom=v1, left/right=v2
				setPadding(element, TOP, valueList.item(0));
				setPadding(element, RIGHT, valueList.item(1));
				setPadding(element, BOTTOM, valueList.item(0));
				setPadding(element, LEFT, valueList.item(1));
			}
		} else {
			throw new CSSException("Invalid padding property value");
		}
	}

	public void applyCSSPropertyPaddingTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, TOP, value);
	}

	public void applyCSSPropertyPaddingRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, RIGHT, value);
	}

	public void applyCSSPropertyPaddingBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, BOTTOM, value);
	}

	public void applyCSSPropertyPaddingLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception {
		setPadding(element, LEFT, value);
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
	
	private void setPadding(Object element, int side, CSSValue value) {
		if(value.getCssValueType() != CSSValue.CSS_PRIMITIVE_VALUE)
			return;
		Widget widget = SWTElementHelpers.getWidget(element);

		if(! (widget instanceof ETabItem)) {
			return;
		}
		
		ETabItem item = (ETabItem) widget;
		ETabFolder folder = item.getETabParent();
		
		int pixelValue = (int) ((CSSPrimitiveValue) value).getFloatValue(CSSPrimitiveValue.CSS_PX);

		switch (side) {
		case TOP:
			folder.setTabTopPadding(pixelValue);
			break;
		case BOTTOM:
			folder.setTabBottomPadding(pixelValue);
			break;
		}
	}
}
