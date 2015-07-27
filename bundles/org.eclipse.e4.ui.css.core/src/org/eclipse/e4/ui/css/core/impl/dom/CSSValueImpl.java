/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import org.eclipse.e4.ui.css.core.exceptions.DOMExceptionImpl;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

public abstract class CSSValueImpl extends AbstractCSSNode implements CSSPrimitiveValue, CSSValue {

	// W3C CSSValue API methods

	@Override
	public String getCssText() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public short getCssValueType() {
		return CSS_PRIMITIVE_VALUE;
	}

	@Override
	public void setCssText(String cssText) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// W3C CSSPrimitiveValue API methods

	@Override
	public short getPrimitiveType() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public Counter getCounterValue() throws DOMException {
		throw new DOMExceptionImpl(DOMException.INVALID_ACCESS_ERR, DOMExceptionImpl.COUNTER_ERROR);
	}

	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		throw new DOMExceptionImpl(DOMException.INVALID_ACCESS_ERR, DOMExceptionImpl.RGBCOLOR_ERROR);
	}

	@Override
	public Rect getRectValue() throws DOMException {
		throw new DOMExceptionImpl(DOMException.INVALID_ACCESS_ERR, DOMExceptionImpl.RECT_ERROR);
	}

	@Override
	public String getStringValue() throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public void setFloatValue(short arg0, float arg1) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	@Override
	public void setStringValue(short arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}

	// Additional methods

	@Override
	public float getFloatValue(short valueType) throws DOMException {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}


}