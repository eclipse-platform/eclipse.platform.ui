/*******************************************************************************
 * Copyright (c) 2008, 2014 Angelo Zerr and others.
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
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.css2;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.RGBColor;

/**
 * Simple {@link RGBColor} implementation.
 *
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 */
public class CSS2RGBColorImpl implements RGBColor {

	private CSSPrimitiveValue red;
	private CSSPrimitiveValue green;
	private CSSPrimitiveValue blue;

	public CSS2RGBColorImpl(int r, int g, int b) {
		red = new CSS2PrimitiveValueImpl(r);
		green = new CSS2PrimitiveValueImpl(g);
		blue = new CSS2PrimitiveValueImpl(b);
	}

	@Override
	public CSSPrimitiveValue getBlue() {
		return blue;
	}

	@Override
	public CSSPrimitiveValue getGreen() {
		return green;
	}

	@Override
	public CSSPrimitiveValue getRed() {
		return red;
	}

}
