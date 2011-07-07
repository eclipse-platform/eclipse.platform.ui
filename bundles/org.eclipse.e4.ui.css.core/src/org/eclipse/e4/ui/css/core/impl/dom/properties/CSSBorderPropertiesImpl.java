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
package org.eclipse.e4.ui.css.core.impl.dom.properties;

import org.eclipse.e4.ui.css.core.dom.properties.CSSBorderProperties;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * {@link CSSBorderProperties} implementation.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSBorderPropertiesImpl implements CSSBorderProperties {

	private CSSPrimitiveValue color;

	private int width = 0;

	private String style;

	public CSSPrimitiveValue getColor() {
		return color;
	}

	public void setColor(CSSPrimitiveValue color) {
		this.color = color;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

}
