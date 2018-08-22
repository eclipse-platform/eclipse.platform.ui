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

	@Override
	public CSSPrimitiveValue getColor() {
		return color;
	}

	@Override
	public void setColor(CSSPrimitiveValue color) {
		this.color = color;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public String getStyle() {
		return style;
	}

	@Override
	public void setStyle(String style) {
		this.style = style;
	}

}
