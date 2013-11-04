/*******************************************************************************
 *  Copyright (c) 2009 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.dom.properties.css2;

import org.eclipse.e4.ui.css.core.dom.properties.ICSSPropertyHandler;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.w3c.dom.css.CSSValue;

/**
 * CSS Border Margin Handler.
 *
 * @see http://www.w3schools.com/css/css_reference.asp#margin
 */

public interface ICSSPropertyMarginHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all four margins in one declaration.
	 * Available values are
	 * {margin-top, margin-right, margin-bottom, margin-left}
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	//TODO support in future values {inherit}
	public void applyCSSPropertyMargin(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the top margin. Available values are {length}
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyMarginTop(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the right margin. Available values are {length}
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyMarginRight(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the bottom margin. Available values are {length}
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyMarginBottom(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;


	/**
	 * Sets the left margin. Available values are {length}
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	//TODO support in future values {auto, %, inherit}
	public void applyCSSPropertyMarginLeft(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;


	public String retrieveCSSPropertyMargin(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMarginTop(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMarginRight(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMarginBottom(Object element,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyMarginLeft(Object element,
			String pseudo, CSSEngine engine) throws Exception;

}
