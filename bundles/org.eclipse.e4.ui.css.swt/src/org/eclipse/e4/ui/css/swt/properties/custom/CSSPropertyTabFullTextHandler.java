/*******************************************************************************
 * Copyright (c) 2009, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.properties.custom;

import java.util.Arrays;
import java.util.Optional;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.swt.properties.AbstractCSSPropertySWTHandler;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.css.CSSValue;

public class CSSPropertyTabFullTextHandler extends AbstractCSSPropertySWTHandler {

	private static int MIN_VIEW_CHARS = 1;

	@Override
	protected void applyCSSProperty(Control control, String property, CSSValue value, String pseudo, CSSEngine engine)
			throws Exception {
		boolean showFullText = (Boolean) engine.convert(value, Boolean.class, null);

		if (control instanceof CTabFolder) {
			CTabFolder folder = (CTabFolder) control;
			if (showFullText == true) {
				Optional<Integer> max = Arrays.stream(folder.getItems()).map(CTabItem::getText).map(String::length)
						.max(Integer::compare);
				folder.setMinimumCharacters(max.orElseGet(() -> 9999));
			} else {
				folder.setMinimumCharacters(MIN_VIEW_CHARS);
			}
		}

	}

	@Override
	protected String retrieveCSSProperty(Control control, String property, String pseudo, CSSEngine engine)
			throws Exception {
		return null;
	}

}