/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.io.Serializable;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.css.CSSValueList;
import org.w3c.dom.css.Counter;
import org.w3c.dom.css.RGBColor;
import org.w3c.dom.css.Rect;

public class CSSValueImpl extends AbstractCSSNode implements CSSPrimitiveValue,
CSSValueList, Serializable {
	PLACEHOLDER; // this class is a stub, needs to be written

	public CSSValueImpl(LexicalUnit parsePropertyValue) {
		// TODO Auto-generated constructor stub
	}

	public Counter getCounterValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public float getFloatValue(short arg0) throws DOMException {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getPrimitiveType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public RGBColor getRGBColorValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Rect getRectValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getStringValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setFloatValue(short arg0, float arg1) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public void setStringValue(short arg0, String arg1) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public String getCssText() {
		// TODO Auto-generated method stub
		return null;
	}

	public short getCssValueType() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setCssText(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

	public CSSValue item(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
