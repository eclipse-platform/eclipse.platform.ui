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

	public CSSPrimitiveValue getFamily() {
		return family;
	}

	public void setFamily(CSSPrimitiveValue family) {
		this.family = family;
	}

	public CSSPrimitiveValue getSize() {
		return size;
	}

	public void setSize(CSSPrimitiveValue size) {
		this.size = size;
	}

	public CSSPrimitiveValue getSizeAdjust() {
		return sizeAdjust;
	}

	public void setSizeAdjust(CSSPrimitiveValue sizeAdjust) {
		this.sizeAdjust = sizeAdjust;
	}

	public CSSPrimitiveValue getWeight() {
		return weight;
	}

	public void setWeight(CSSPrimitiveValue weight) {
		this.weight = weight;
	}

	public CSSPrimitiveValue getStyle() {
		return style;
	}

	public void setStyle(CSSPrimitiveValue style) {
		this.style = style;
	}

	public CSSPrimitiveValue getVariant() {
		return variant;
	}

	public void setVariant(CSSPrimitiveValue variant) {
		this.variant = variant;
	}

	public CSSPrimitiveValue getStretch() {
		return stretch;
	}

	public void setStretch(CSSPrimitiveValue stretch) {
		this.stretch = stretch;
	}

	public String getCssText() {
		return null;
	}

	public short getCssValueType() {
		return CSSValue.CSS_CUSTOM;
	}

	public void setCssText(String arg0) throws DOMException {
	}
	
	
	
}
