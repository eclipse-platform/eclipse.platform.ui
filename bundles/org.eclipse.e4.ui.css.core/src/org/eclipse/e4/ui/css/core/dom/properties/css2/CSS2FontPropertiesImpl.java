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
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.w3c.dom.DOMException;
import org.eclipse.e4.ui.css.core.impl.dom.CssValues.CssPrimitive;
import org.w3c.dom.css.CSSValue;

public class CSS2FontPropertiesImpl implements CSS2FontProperties {

	private CssPrimitive family;

	private CssPrimitive size;

	private boolean sizeFromCSS;

	private CssPrimitive weight;

	private CssPrimitive style;

	@Override
	public CssPrimitive getFamily() {
		return family;
	}

	@Override
	public void setFamily(CssPrimitive family) {
		this.family = family;
	}

	@Override
	public CssPrimitive getSize() {
		return size;
	}

	@Override
	public void setSize(CssPrimitive size) {
		this.size = size;
	}

	@Override
	public boolean isSizeFromCSS() {
		return sizeFromCSS;
	}

	@Override
	public void setSizeFromCSS(boolean sizeFromCSS) {
		this.sizeFromCSS = sizeFromCSS;
	}

	@Override
	public CssPrimitive getWeight() {
		return weight;
	}

	@Override
	public void setWeight(CssPrimitive weight) {
		this.weight = weight;
	}

	@Override
	public CssPrimitive getStyle() {
		return style;
	}

	@Override
	public void setStyle(CssPrimitive style) {
		this.style = style;
	}

	@Override
	public String getCssText() {
		return null;
	}

	@Override
	public short getCssValueType() {
		return CSSValue.CSS_CUSTOM;
	}

	@Override
	public void setCssText(String arg0) throws DOMException {
	}



}
