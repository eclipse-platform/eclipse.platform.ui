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
	void applyCSSPropertyFont(Object element, CSSValue value,
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
	void applyCSSPropertyFontFamily(Object element, CSSValue value,
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
	void applyCSSPropertyFontSize(Object element, CSSValue value,
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
	void applyCSSPropertyFontSizeAdjust(Object element, CSSValue value,
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
	void applyCSSPropertyFontStretch(Object element, CSSValue value,
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
	void applyCSSPropertyFontStyle(Object element, CSSValue value,
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
	void applyCSSPropertyFontVariant(Object element, CSSValue value,
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
	void applyCSSPropertyFontWeight(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontFamily(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontSize(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontAdjust(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontStretch(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontStyle(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontVariant(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	String retrieveCSSPropertyFontWeight(Object element, String pseudo,
			CSSEngine engine) throws Exception;

}
