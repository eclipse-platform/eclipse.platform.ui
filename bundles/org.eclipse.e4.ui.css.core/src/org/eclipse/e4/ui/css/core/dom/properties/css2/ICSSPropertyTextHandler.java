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
 * CSS2 Text Property Handler.
 *
 * @see http://www.w3schools.com/css/css_reference.asp#text
 */
public interface ICSSPropertyTextHandler extends ICSSPropertyHandler {

	/**
	 * Sets the color of a text.
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Controls the letters in an element. Available values are : none
	 * capitalize uppercase lowercase
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyTextTransform(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyColor(Object element, String pseudo,
			CSSEngine engine) throws Exception;

	public String retrieveCSSPropertyTextTransform(Object element,
			String pseudo, CSSEngine engine) throws Exception;
}
