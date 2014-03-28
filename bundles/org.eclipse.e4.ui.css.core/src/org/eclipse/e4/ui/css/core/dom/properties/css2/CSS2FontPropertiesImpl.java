/*******************************************************************************
 * Copyright (c) 2008 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;

public class CSS2FontPropertiesImpl implements CSS2FontProperties {

	private CSSPrimitiveValue family;

	private CSSPrimitiveValue size;

	private CSSPrimitiveValue sizeAdjust;

	private CSSPrimitiveValue weight;

	private CSSPrimitiveValue style;

	private CSSPrimitiveValue variant;

	private CSSPrimitiveValue stretch;

	@Override
	public CSSPrimitiveValue getFamily() {
		return family;
	}

	@Override
	public void setFamily(CSSPrimitiveValue family) {
		this.family = family;
	}

	@Override
	public CSSPrimitiveValue getSize() {
		return size;
	}

	@Override
	public void setSize(CSSPrimitiveValue size) {
		this.size = size;
	}

	@Override
	public CSSPrimitiveValue getSizeAdjust() {
		return sizeAdjust;
	}

	@Override
	public void setSizeAdjust(CSSPrimitiveValue sizeAdjust) {
		this.sizeAdjust = sizeAdjust;
	}

	@Override
	public CSSPrimitiveValue getWeight() {
		return weight;
	}

	@Override
	public void setWeight(CSSPrimitiveValue weight) {
		this.weight = weight;
	}

	@Override
	public CSSPrimitiveValue getStyle() {
		return style;
	}

	@Override
	public void setStyle(CSSPrimitiveValue style) {
		this.style = style;
	}

	@Override
	public CSSPrimitiveValue getVariant() {
		return variant;
	}

	@Override
	public void setVariant(CSSPrimitiveValue variant) {
		this.variant = variant;
	}

	@Override
	public CSSPrimitiveValue getStretch() {
		return stretch;
	}

	@Override
	public void setStretch(CSSPrimitiveValue stretch) {
		this.stretch = stretch;
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
