/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.css2;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

/**
 * Simple {@link CSSPrimitiveValue} implementation.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 *
 */
public class CSS2PrimitiveValueImpl implements CSSPrimitiveValue {

	private String s = null;

	private float v = -9999;

	private int i = -9999;

	private short primitiveType;

	public CSS2PrimitiveValueImpl(String s) {
		this.s = s;
		this.primitiveType = CSSPrimitiveValue.CSS_IDENT;
	}

	public CSS2PrimitiveValueImpl(float v) {
		this.v = v;
		this.primitiveType = CSSPrimitiveValue.CSS_NUMBER;
	}

	public CSS2PrimitiveValueImpl(int i) {
		this.i = i;
		this.primitiveType = CSSPrimitiveValue.CSS_NUMBER;
	}

	@Override
	public Counter getCounterValue() throws DOMException {
		return null;
	}

	@Override
	public float getFloatValue(short word0) throws DOMException {
		if (v != -9999)
			return v;
		return i;
	}

	@Override
	public short getPrimitiveType() {
		return primitiveType;
	}

	@Override
	public Rect getRectValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringValue() throws DOMException {
		return s;
	}

	@Override
	public void setFloatValue(short word0, float f) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStringValue(short word0, String s) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getCssText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public short getCssValueType() {
		return CSSValue.CSS_PRIMITIVE_VALUE;
	}

	@Override
	public void setCssText(String s) throws DOMException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		if (s != null)
			return s;
		if (v != -9999)
			return v + "";
		if (i != -9999)
			return i + "";
		return super.toString();
	}
}
