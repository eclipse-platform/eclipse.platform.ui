/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
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

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

/**
 * CSS2 Dimension Property Handler.
 *
 * @see <a href="https://www.w3schools.com/css/css_dimension.asp">w3schools</a>
 */
public interface ICSSPropertyDimensionHandler extends ICSSPropertyHandler {

	/**
	 * Sets the height of an element. Available values are=auto length %
	 */
	public void applyCSSPropertyHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the distance between lines. Available values are=normal number
	 * length %
	 */
	public void applyCSSPropertyLineHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the maximum height of an element. Available values are= none length %
	 */
	public void applyCSSPropertyMaxHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the maximum width of an element. Available values are=none length %
	 */
	public void applyCSSPropertyMaxWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the minimum height of an element. Available values are=length %
	 */
	public void applyCSSPropertyMinHeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the minimum width of an element. Available values are=length %
	 */
	public void applyCSSPropertyMinWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of an element. Available values are=auto % length
	 */
	public void applyCSSPropertyWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyHeight(Object widget, String property,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMaxHeight(Object widget, String property,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMinHeight(Object widget, String property,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMinWidth(Object widget, String property,
			String pseudo, CSSEngine engine) throws Exception;
}
