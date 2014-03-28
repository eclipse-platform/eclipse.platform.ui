/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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

import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

public class RGBColorImpl extends CSSValueImpl implements RGBColor {

	private CSSPrimitiveValue red;
	private CSSPrimitiveValue green;
	private CSSPrimitiveValue blue;

	public RGBColorImpl(LexicalUnit lexicalUnit) {
		LexicalUnit nextUnit = lexicalUnit.getParameters();
		red = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		green = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		blue = new Measure(nextUnit);
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.RGBColor#getRed()
	 */
	@Override
	public CSSPrimitiveValue getRed() {
		return red;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.RGBColor#getGreen()
	 */
	@Override
	public CSSPrimitiveValue getGreen() {
		return green;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.RGBColor#getBlue()
	 */
	@Override
	public CSSPrimitiveValue getBlue() {
		return blue;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSValue#getRGBColorValue()
	 */
	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		return this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSValue#getPrimitiveType()
	 */
	@Override
	public short getPrimitiveType() {
		return CSS_RGBCOLOR;
	}

	/*
	 * (non-Javadoc)
	 * @see org.w3c.dom.css.CSSValue#getCssText()
	 */
	@Override
	public String getCssText() {
		return "rgb(" + red.getCssText() + ", " + green.getCssText() + ", "
				+ blue.getCssText() + ")";
	}
}
