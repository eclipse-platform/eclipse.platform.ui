/*******************************************************************************
 * Copyright (c) 2008, 2013 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * @see http://www.w3schools.com/css/css_reference.asp#border
 */
public interface ICSSPropertyBorderHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all of the properties for the four
	 * borders in one declaration. Available values are= border-width
	 * border-style border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorder(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the bottom
	 * border in one declaration. Available values are=border-bottom-width
	 * border-style border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderBottom(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the bottom border. Available values are=border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderBottomColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the bottom border. Available values are=border-style
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderBottomStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the bottom border. Available values are= thin medium
	 * thick length
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderBottomWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the four borders, can have from one to four colors.
	 * Available values are=color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the left
	 * border in one declaration. Available values are=border-left-width
	 * border-style border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the left border. Available values are=border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderLeftColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the left border. Available values are=border-style
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderLeftStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the left border. Available values are=thin medium thick
	 * length
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderLeftWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the right
	 * border in one declaration. Available values are=border-right-width
	 * border-style border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderRight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the right border. Available values are=border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderRightColor(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the right border. Available values are=border-style
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderRightStyle(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the right border.Available values are= thin medium
	 * thick length
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderRightWidth(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the four borders, can have from one to four styles.
	 * Available values are=none hidden dotted dashed solid double groove ridge
	 * inset outset
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting all of the properties for the top border
	 * in one declaration. Available values are=border-top-width border-style
	 * border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderTop(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the color of the top border. Available values are=border-color
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderTopColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the top border. Available values are=border-style
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderTopStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the width of the top border. Available values are=thin medium thick
	 * length
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderTopWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A shorthand property for setting the width of the four borders in one
	 * declaration, can have from one to four values. Available values are=thin
	 * medium thick length
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBorderWidth(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorder(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderBottomColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderBottomStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderBottomWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderColor(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderLeft(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderLeftColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderLeftStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderLeftWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderRight(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderRightColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderRightStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderRightWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderTop(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderTopColor(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderTopStyle(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderTopWidth(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyBorderWidth(Object element, String pseudo,
			CSSEngine engine) throws Exception;

}
