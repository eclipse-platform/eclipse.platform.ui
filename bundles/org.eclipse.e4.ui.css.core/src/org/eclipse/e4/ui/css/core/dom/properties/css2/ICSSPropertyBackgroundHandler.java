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
 * CSS2 Background Property Handler.
 *
 * @see <a href="https://www.w3schools.com/css/css_background.asp">w3schools</a>
 */
public interface ICSSPropertyBackgroundHandler extends ICSSPropertyHandler {

	/**
	 * A shorthand property for setting all background properties in one
	 * declaration Available values = background-color background-image
	 * background-repeat background-attachment background-position
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @throws Exception
	 */
	public void applyCSSPropertyBackground(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets whether a background image is fixed or scrolls with the rest of the
	 * page. Available values are=scroll,fixed
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyBackgroundAttachment(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the background color of an element. Available values are= color-rgb,
	 * color-hex, color-name, transparent
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyBackgroundColor(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets an image as the background. Available values=url(URL), none
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyBackgroundImage(Object element, CSSValue value,
			String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets the starting position of a background image. Available values=top
	 * left,top center,top right,center left,center center,center right,bottom
	 * left,bottom center,bottom right,x% y%,xpos ypos
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyBackgroundPosition(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	/**
	 * Sets if/how a background image will be repeated. Available
	 * values=repeat,repeat-x,repeat-y,no-repeat
	 *
	 * @param element
	 * @param value
	 * @param pseudo
	 * @param engine
	 * @return
	 * @throws Exception
	 */
	public void applyCSSPropertyBackgroundRepeat(Object element,
			CSSValue value, String pseudo, CSSEngine engine) throws Exception;

	@Deprecated(forRemoval = true)
	public default String retrieveCSSPropertyBackgroundAttachment(Object element,
			String pseudo,
			CSSEngine engine) throws Exception{
		return null;
	}

	@Deprecated(forRemoval = true)
	public String retrieveCSSPropertyBackgroundColor(Object element,
			String pseudo,
			CSSEngine engine) throws Exception;

	@Deprecated(forRemoval = true)
	public default String retrieveCSSPropertyBackgroundImage(Object element,
			String pseudo,
			CSSEngine engine) throws Exception  {
		// TODO : manage path of Image.
		return "none";
	}

	@Deprecated(forRemoval = true)
	public default String retrieveCSSPropertyBackgroundPosition(Object element,
			String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

	@Deprecated(forRemoval = true)
	public default String retrieveCSSPropertyBackgroundRepeat(Object element,
			String pseudo,
			CSSEngine engine) throws Exception {
		return null;
	}

}
