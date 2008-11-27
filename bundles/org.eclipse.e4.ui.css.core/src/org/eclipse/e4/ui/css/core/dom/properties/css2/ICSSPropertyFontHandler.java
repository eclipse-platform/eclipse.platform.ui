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

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

/**
 * CSS2 Font Property Handler.
 * 
 * @see http://www.w3schools.com/css/css_reference.asp#font
 */
public interface ICSSPropertyFontHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all of the properties for a font in one
	 * declaration. Available values are=font-style font-variant font-weight
	 * font-size/line-height font-family caption icon menu message-box
	 * small-caption status-bar
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyFont(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * A prioritized list of font family names and/or generic family names for
	 * an element.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontFamily(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the size of a font.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontSize(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Specifies an aspect value for an element that will preserve the x-height
	 * of the first-choice font.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontSizeAdjust(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Condenses or expands the current font-family.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontStretch(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the style of the font.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontStyle(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Displays text in a small-caps font or a normal font.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontVariant(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the weight of a font.
	 * 
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyFontWeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception;

}
