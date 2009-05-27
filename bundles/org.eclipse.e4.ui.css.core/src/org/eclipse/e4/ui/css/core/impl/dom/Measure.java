/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
 
package org.eclipse.e4.ui.css.core.impl.dom;

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSValue;

public class Measure extends CSSValueImpl {
	
	LexicalUnit value;

	public Measure(LexicalUnit value) {
		super();
		this.value = value;
	}

	/**
	 * Return a float representation of the receiver's value.
	 * @param valueType a short representing the value type, see {@link CSSValue#getCssValueType()}
	 */
	public float getFloatValue(short valueType) throws DOMException {
		//If it's actually a SAC_INTEGER return the integer value, callers tend to expect and cast
		//There is no getIntegerFloat(short)
		//TODO Not sure the purpose of arg valyeType, its not referenced in this method
		if(value.getLexicalUnitType() == LexicalUnit.SAC_INTEGER)
			return value.getIntegerValue();
		//TODO not sure what to do if it's not one of the lexical unit types that are specified in LexicalUnit#getFloatValue()
		//ie. SAC_DEGREE, SAC_GRADIAN, SAC_RADIAN, SAC_MILLISECOND, SAC_SECOND, SAC_HERTZ or SAC_KILOHERTZ
		return value.getFloatValue();
	}

	/**
	 * Return an int representation of the receiver's value.
	 * @param valueType a short representing the value type, see {@link CSSValue#getCssValueType()}
	 */
	public int getIntegerValue(short valueType) throws DOMException {
		return value.getIntegerValue();
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSPrimitiveValue#getStringValue()
	 */
	public String getStringValue() throws DOMException {
		if((value.getLexicalUnitType() == LexicalUnit.SAC_IDENT)
				|| (value.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE)
				|| (value.getLexicalUnitType() == LexicalUnit.SAC_URI))
			return value.getStringValue();
		// TODO There are more cases to catch of getLexicalUnitType()
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSPrimitiveValue#getPrimitiveType()
	 */
	public short getPrimitiveType() {
		switch (value.getLexicalUnitType()) {
		case LexicalUnit.SAC_IDENT:
			return CSS_IDENT;
		case LexicalUnit.SAC_PIXEL:
			return CSS_PX;
		case LexicalUnit.SAC_INTEGER:
		case LexicalUnit.SAC_REAL:
			return CSS_NUMBER;
		case LexicalUnit.SAC_URI:
			return CSS_URI;
		case LexicalUnit.SAC_PERCENTAGE:
			return CSS_PERCENTAGE;
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("NOT YET IMPLEMENTED");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSValue#getCssText()
	 */
	public String getCssText() {
		if(getPrimitiveType() == CSS_NUMBER)
			return String.valueOf(value.getIntegerValue());
		return value.getStringValue();
	}
}
