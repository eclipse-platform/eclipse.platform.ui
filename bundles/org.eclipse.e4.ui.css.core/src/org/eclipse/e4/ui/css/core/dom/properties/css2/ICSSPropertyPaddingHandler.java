/*******************************************************************************
 *  Copyright (c) 2009, 2013 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Padding Handler.
 *
 * @see <a href="https://www.w3schools.com/css/css_padding.asp">w3schools</a>
 */

public interface ICSSPropertyPaddingHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all four paddings in one declaration.
	 * Available values are
	 * {padding-top, padding-right, padding-bottom, padding-left}
	 */
	//TODO support in future values {inherit}
	public void applyCSSPropertyPadding(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the top padding. Available values are {length}
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyPaddingTop(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the right padding. Available values are {length}
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyPaddingRight(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the bottom padding. Available values are {length}
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyPaddingBottom(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the left padding. Available values are {length}
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyPaddingLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;


	public String retrieveCSSPropertyPadding(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyPaddingTop(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyPaddingRight(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyPaddingBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyPaddingLeft(Object element,
			String pseudo, CSSEngine engine) throws Exception;

}
