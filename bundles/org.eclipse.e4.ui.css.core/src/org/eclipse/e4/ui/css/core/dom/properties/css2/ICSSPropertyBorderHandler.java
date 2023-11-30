/*******************************************************************************
 * Copyright (c) 2008, 2018 Angelo Zerr and others.
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
 * CSS2 Border Property Handler.
 *
 * @see <a href="https://www.w3schools.com/css/css_border.asp">w3schools</a>
 */
public interface ICSSPropertyBorderHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all of the properties for the four
	 * borders in one declaration. Available values are= border-width
	 * border-style border-color
	 */
	void applyCSSPropertyBorder(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the bottom
	 * border in one declaration. Available values are=border-bottom-width
	 * border-style border-color
	 */
	void applyCSSPropertyBorderBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the bottom border. Available values are=border-color
	 */
	void applyCSSPropertyBorderBottomColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the bottom border. Available values are=border-style
	 */
	void applyCSSPropertyBorderBottomStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the bottom border. Available values are= thin medium
	 * thick length
	 */
	void applyCSSPropertyBorderBottomWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the four borders, can have from one to four colors.
	 * Available values are=color
	 */
	void applyCSSPropertyBorderColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the left
	 * border in one declaration. Available values are=border-left-width
	 * border-style border-color
	 */
	void applyCSSPropertyBorderLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the left border. Available values are=border-color
	 */
	void applyCSSPropertyBorderLeftColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the left border. Available values are=border-style
	 */
	void applyCSSPropertyBorderLeftStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the left border. Available values are=thin medium thick
	 * length
	 */
	void applyCSSPropertyBorderLeftWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the right
	 * border in one declaration. Available values are=border-right-width
	 * border-style border-color
	 */
	void applyCSSPropertyBorderRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the right border. Available values are=border-color
	 */
	void applyCSSPropertyBorderRightColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the right border. Available values are=border-style
	 */
	void applyCSSPropertyBorderRightStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the right border.Available values are= thin medium
	 * thick length
	 */
	void applyCSSPropertyBorderRightWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the four borders, can have from one to four styles.
	 * Available values are=none hidden dotted dashed solid double groove ridge
	 * inset outset
	 */
	void applyCSSPropertyBorderStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the top border
	 * in one declaration. Available values are=border-top-width border-style
	 * border-color
	 */
	void applyCSSPropertyBorderTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the top border. Available values are=border-color
	 */
	void applyCSSPropertyBorderTopColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the top border. Available values are=border-style
	 */
	void applyCSSPropertyBorderTopStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the top border. Available values are=thin medium thick
	 * length
	 */
	void applyCSSPropertyBorderTopWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting the width of the four borders in one
	 * declaration, can have from one to four values. Available values are=thin
	 * medium thick length
	 */
	void applyCSSPropertyBorderWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorder(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderBottomColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderBottomStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderBottomWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderColor(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderLeftColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderLeftStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderLeftWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderRight(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderRightColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderRightStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderRightWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderTop(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderTopColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderTopStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderTopWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyBorderWidth(Object element, String pseudo,
			CSSEngine engine) throws Exception;

}
