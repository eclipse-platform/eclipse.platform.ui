/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
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
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.w3c.dom.css.CSSValue;

public interface CSS2FontProperties extends CSSValue {

	CssPrimitive getFamily();

	void setFamily(CssPrimitive family);

	CssPrimitive getSize();

	void setSize(CssPrimitive size);

	/**
	 * Whether {@link #getSize()} stems from a parsed CSS declaration rather
	 * than mirroring the widget's current font. Font definitions only
	 * override the height when the size was not set by CSS.
	 */
	boolean isSizeFromCSS();

	void setSizeFromCSS(boolean sizeFromCSS);

	CssPrimitive getSizeAdjust();

	void setSizeAdjust(CssPrimitive sizeAdjust);

	CssPrimitive getWeight();

	void setWeight(CssPrimitive weight);

	CssPrimitive getStyle();

	void setStyle(CssPrimitive style);

	CssPrimitive getVariant();

	void setVariant(CssPrimitive variant);

	CssPrimitive getStretch();

	void setStretch(CssPrimitive stretch);
}
