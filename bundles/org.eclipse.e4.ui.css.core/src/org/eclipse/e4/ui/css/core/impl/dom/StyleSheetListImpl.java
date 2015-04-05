/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     IBM Corporation - ongoing development
 *******************************************************************************/

package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.stylesheets.StyleSheet;
import org.w3c.dom.stylesheets.StyleSheetList;

/**
 * {@link StyleSheetList} implementation. It provides the abstraction of an
 * ordered collection of style sheets.
 */
public class StyleSheetListImpl implements StyleSheetList {

	private List<StyleSheet> styleSheets;


	@Override
	public int getLength() {
		return (styleSheets != null) ? styleSheets.size() : 0;
	}

	@Override
	public StyleSheet item(int index) {
		return (styleSheets != null) ? (StyleSheet) styleSheets.get(index) : null;
	}

	/**
	 * Add {@link StyleSheet} to the collection of style sheets
	 *
	 * @param styleSheet
	 */
	public void addStyleSheet(StyleSheet styleSheet) {
		if (styleSheets == null) {
			styleSheets = new ArrayList<>();
		}
		styleSheets.add(styleSheet);
	}

	/**
	 * Remove all style sheet.
	 */
	public void removeAllStyleSheets() {
		styleSheets = null;
	}
}
