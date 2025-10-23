/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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

	private final CSSPrimitiveValue red;
	private final CSSPrimitiveValue green;
	private final CSSPrimitiveValue blue;

	public RGBColorImpl(LexicalUnit lexicalUnit) {
		LexicalUnit nextUnit = lexicalUnit.getParameters();
		red = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		green = new Measure(nextUnit);
		nextUnit = nextUnit.getNextLexicalUnit().getNextLexicalUnit();
		blue = new Measure(nextUnit);
	}

	@Override
	public CSSPrimitiveValue getRed() {
		return red;
	}

	@Override
	public CSSPrimitiveValue getGreen() {
		return green;
	}

	@Override
	public CSSPrimitiveValue getBlue() {
		return blue;
	}

	@Override
	public RGBColor getRGBColorValue() throws DOMException {
		return this;
	}

	@Override
	public short getPrimitiveType() {
		return CSS_RGBCOLOR;
	}

	@Override
	public String getCssText() {
		return "rgb(" + red.getCssText() + ", " + green.getCssText() + ", "
				+ blue.getCssText() + ")";
	}
}
